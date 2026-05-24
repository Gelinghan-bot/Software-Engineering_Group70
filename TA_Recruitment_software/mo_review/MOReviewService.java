package TA_Recruitment_software.mo_review;

import TA_Recruitment_software.admin_system.foundation.AppException;
import TA_Recruitment_software.admin_system.foundation.ValidationUtil;
import TA_Recruitment_software.admin_system.model.Application;
import TA_Recruitment_software.admin_system.model.ApplicationStatus;
import TA_Recruitment_software.admin_system.model.Position;
import TA_Recruitment_software.admin_system.model.PositionStatus;
import TA_Recruitment_software.admin_system.model.Role;
import TA_Recruitment_software.admin_system.model.User;
import TA_Recruitment_software.admin_system.repository.ApplicationRepository;
import TA_Recruitment_software.admin_system.repository.PositionRepository;
import TA_Recruitment_software.admin_system.repository.UserRepository;
import TA_Recruitment_software.auth.SessionContext;
import TA_Recruitment_software.auth.SessionManager;
import TA_Recruitment_software.auth.TaNotificationService;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;

/**
 * Service layer for Module Organizer (MO) application review and status management.
 * <p>
 * This class provides business logic for reviewing TA applications, managing application
 * status transitions, and performing AI-powered candidate comparison. All operations require
 * MO or ADMIN role authentication.
 * </p>
 * <p>
 * Key features:
 * <ul>
 *   <li>List and filter applications by position or by MO's owned positions</li>
 *   <li>Validate and execute application status transitions (e.g., PENDING → SHORTLISTED)</li>
 *   <li>Automatically close positions and reject remaining applicants when headcount is reached</li>
 *   <li>AI-powered candidate comparison via Alibaba Cloud DashScope API</li>
 *   <li>Sort applications by submission time or major</li>
 * </ul>
 * </p>
 * <p>
 * Status transition rules are defined in {@link #VALID_TRANSITIONS}:
 * <ul>
 *   <li>PENDING/SUBMITTED → SHORTLISTED or REJECTED</li>
 *   <li>SHORTLISTED → INTERVIEWED, OFFERED, or REJECTED</li>
 *   <li>INTERVIEWED → OFFERED or REJECTED</li>
 *   <li>OFFERED → HIRED or REJECTED</li>
 *   <li>APPROVED → HIRED or REJECTED</li>
 *   <li>REJECTED/HIRED are terminal states (no further transitions)</li>
 * </ul>
 * </p>
 *
 * @author Group70
 * @see Application
 * @see ApplicationStatus
 * @see SessionManager
 */
public class MOReviewService {
    private final PositionRepository positionRepository;
    private final ApplicationRepository applicationRepository;
    private final UserRepository userRepository;
    private final SessionManager sessionManager;
    private final TaNotificationService taNotificationService;

    /**
     * Valid state transition map defining allowed next statuses for each application status.
     */
    private static final Map<ApplicationStatus, List<ApplicationStatus>> VALID_TRANSITIONS;

    static {
        VALID_TRANSITIONS = new EnumMap<>(ApplicationStatus.class);
        VALID_TRANSITIONS.put(ApplicationStatus.PENDING,
            Arrays.asList(ApplicationStatus.SHORTLISTED, ApplicationStatus.REJECTED));
        VALID_TRANSITIONS.put(ApplicationStatus.SUBMITTED,
            Arrays.asList(ApplicationStatus.SHORTLISTED, ApplicationStatus.REJECTED));
        VALID_TRANSITIONS.put(ApplicationStatus.SHORTLISTED,
            Arrays.asList(ApplicationStatus.INTERVIEWED, ApplicationStatus.OFFERED, ApplicationStatus.REJECTED));
        VALID_TRANSITIONS.put(ApplicationStatus.INTERVIEWED,
            Arrays.asList(ApplicationStatus.OFFERED, ApplicationStatus.REJECTED));
        VALID_TRANSITIONS.put(ApplicationStatus.OFFERED,
            Arrays.asList(ApplicationStatus.HIRED, ApplicationStatus.REJECTED));
        VALID_TRANSITIONS.put(ApplicationStatus.APPROVED,
            Arrays.asList(ApplicationStatus.HIRED, ApplicationStatus.REJECTED));
        VALID_TRANSITIONS.put(ApplicationStatus.REJECTED, Collections.emptyList());
        VALID_TRANSITIONS.put(ApplicationStatus.HIRED, Collections.emptyList());
    }

    /**
     * Constructs a MOReviewService with required dependencies.
     *
     * @param positionRepository      repository for position queries
     * @param applicationRepository   repository for application CRUD operations
     * @param userRepository          repository for user queries
     * @param sessionManager          session manager for authentication
     * @param taNotificationService   service for sending notifications to applicants
     */
    public MOReviewService(
        PositionRepository positionRepository,
        ApplicationRepository applicationRepository,
        UserRepository userRepository,
        SessionManager sessionManager,
        TaNotificationService taNotificationService
    ) {
        this.positionRepository = positionRepository;
        this.applicationRepository = applicationRepository;
        this.userRepository = userRepository;
        this.sessionManager = sessionManager;
        this.taNotificationService = taNotificationService;
    }

    /**
     * Lists all applications for a specific position.
     * <p>
     * ADMIN can view applications for any position; MO can only view applications
     * for positions they published.
     * </p>
     *
     * @param token       valid session token
     * @param positionId  the position to query applications for
     * @return list of applications for the specified position
     * @throws AppException if permission denied or position not found
     */
    public List<Application> listApplicationsForPosition(String token, String positionId) {
        SessionContext session = sessionManager.requireSession(token);
        if (session.getRole() != Role.MO && session.getRole() != Role.ADMIN) {
            throw new AppException("Permission denied. Only MO and ADMIN can review applications.");
        }
        String checkedPositionId = ValidationUtil.requireNotBlank(positionId, "Position ID");
        Position position = positionRepository.findById(checkedPositionId)
            .orElseThrow(() -> new AppException("Position not found."));

        if (session.getRole() == Role.MO && !position.getPublishedByUserId().equals(session.getUserId())) {
            throw new AppException("Permission denied. You can only review your own positions.");
        }
        return applicationRepository.findByPosition(checkedPositionId);
    }

    /**
     * Lists all applications belonging to positions owned by the current MO.
     * <p>
     * ADMIN can see all applications across all positions; MO can only see applications
     * for positions they published.
     * </p>
     *
     * @param token  valid session token
     * @return list of applications for the MO's positions, or all applications for ADMIN
     * @throws AppException if permission denied
     */
    public List<Application> listAllApplicationsOfMyPositions(String token) {
        SessionContext session = sessionManager.requireSession(token);
        if (session.getRole() != Role.MO && session.getRole() != Role.ADMIN) {
            throw new AppException("Permission denied. Only MO and ADMIN can review applications.");
        }

        if (session.getRole() == Role.ADMIN) {
            return applicationRepository.findAll();
        }

        List<Position> ownedPositions = positionRepository.findByPublisher(session.getUserId());
        Set<String> positionIds = new HashSet<>();
        for (Position position : ownedPositions) {
            positionIds.add(position.getPositionId());
        }

        List<Application> result = new ArrayList<>();
        for (Application app : applicationRepository.findAll()) {
            if (positionIds.contains(app.getPositionId())) {
                result.add(app);
            }
        }
        return result;
    }

    /**
     * Retrieves applicant user information by user ID.
     *
     * @param applicantUserId  the applicant's user ID
     * @return Optional containing the User if found
     */
    public Optional<User> getApplicantInfo(String applicantUserId) {
        return userRepository.findByUserId(applicantUserId);
    }

    /**
     * Retrieves position information by position ID.
     *
     * @param positionId  the position ID
     * @return Optional containing the Position if found
     */
    public Optional<Position> getPositionInfo(String positionId) {
        return positionRepository.findById(positionId);
    }

    /**
     * Returns the list of valid next statuses for a given current application status.
     *
     * @param current  the current application status
     * @return list of allowed next statuses (empty if current is terminal)
     */
    public List<ApplicationStatus> getValidNextStatuses(ApplicationStatus current) {
        List<ApplicationStatus> next = VALID_TRANSITIONS.get(current);
        return next != null ? new ArrayList<>(next) : Collections.emptyList();
    }

    /**
     * Updates the status of an application with optional review note.
     * <p>
     * Validates that the status transition is allowed according to {@link #VALID_TRANSITIONS}.
     * Records the status change in the application's history and sends a notification to the applicant.
     * If the new status is HIRED and the position headcount is reached, automatically closes the
     * position and rejects all remaining non-hired applicants.
     * </p>
     *
     * @param token          valid session token
     * @param applicationId  the application to update
     * @param newStatus      the new status to set
     * @param note           optional review note (may be null)
     * @return the updated {@link Application} entity
     * @throws AppException if permission denied, application not found, or invalid status transition
     */
    public Application updateApplicationStatus(String token, String applicationId,
                                                ApplicationStatus newStatus, String note) {
        SessionContext session = sessionManager.requireSession(token);
        if (session.getRole() != Role.MO && session.getRole() != Role.ADMIN) {
            throw new AppException("Permission denied. Only MO and ADMIN can update application status.");
        }
        Application app = applicationRepository
            .findById(ValidationUtil.requireNotBlank(applicationId, "Application ID"))
            .orElseThrow(() -> new AppException("Application not found."));
        Position position = positionRepository.findById(app.getPositionId())
            .orElseThrow(() -> new AppException("Position not found for this application."));

        if (session.getRole() == Role.MO && !position.getPublishedByUserId().equals(session.getUserId())) {
            throw new AppException("Permission denied. You can only update applications for your own positions.");
        }

        List<ApplicationStatus> validNext = getValidNextStatuses(app.getStatus());
        if (!validNext.contains(newStatus)) {
            throw new AppException("Invalid status transition: " + app.getStatus() + " -> " + newStatus
                + ". Allowed: " + validNext);
        }

        ApplicationStatus oldStatus = app.getStatus();
        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        String historyEntry = now + " | " + app.getStatus() + " -> " + newStatus;
        if (note != null && !note.trim().isEmpty()) {
            historyEntry += " | Note: " + note.trim();
        }
        String existingHistory = app.getStatusHistory();
        if (existingHistory == null || existingHistory.isEmpty()) {
            app.setStatusHistory(historyEntry);
        } else {
            app.setStatusHistory(existingHistory + " ;; " + historyEntry);
        }

        app.setStatus(newStatus);
        app.setUpdatedTime(now);

        if (note != null && !note.trim().isEmpty()) {
            app.setStatusNote(note.trim());
        }

        applicationRepository.save(app);
        taNotificationService.pushStatusChangeNotification(
            app.getApplicantUserId(), app, oldStatus.name(), newStatus.name(), note);

        if (newStatus == ApplicationStatus.HIRED && position.getHeadcount() > 0) {
            checkAndClosePosition(token, position, now);
        }

        return app;
    }

    /**
     * Checks if a position has reached its headcount limit and closes it if so.
     * <p>
     * When the hired count equals or exceeds the position headcount, the position status
     * is set to CLOSED and all remaining non-hired, non-rejected applicants are automatically
     * rejected with a "Position Filled" note.
     * </p>
     *
     * @param token     valid session token
     * @param position  the position to check
     * @param now       current timestamp string for history recording
     */
    private void checkAndClosePosition(String token, Position position, String now) {
        List<Application> allApps = applicationRepository.findByPosition(position.getPositionId());
        long hiredCount = allApps.stream().filter(a -> a.getStatus() == ApplicationStatus.HIRED).count();

        if (hiredCount >= position.getHeadcount()) {
            position.setStatus(PositionStatus.CLOSED);
            positionRepository.save(position);

            for (Application a : allApps) {
                if (a.getStatus() != ApplicationStatus.HIRED && a.getStatus() != ApplicationStatus.REJECTED) {
                    List<ApplicationStatus> validNext = getValidNextStatuses(a.getStatus());
                    if (validNext.contains(ApplicationStatus.REJECTED)) {
                        ApplicationStatus prevStatus = a.getStatus();
                        String historyEntry = now + " | " + a.getStatus() + " -> " + ApplicationStatus.REJECTED + " | Note: Position Filled";
                        String existingHistory = a.getStatusHistory();
                        if (existingHistory == null || existingHistory.isEmpty()) {
                            a.setStatusHistory(historyEntry);
                        } else {
                            a.setStatusHistory(existingHistory + " ;; " + historyEntry);
                        }
                        a.setStatus(ApplicationStatus.REJECTED);
                        a.setStatusNote("Position Filled");
                        a.setUpdatedTime(now);
                        applicationRepository.save(a);
                        taNotificationService.pushStatusChangeNotification(
                            a.getApplicantUserId(), a, prevStatus.name(), ApplicationStatus.REJECTED.name(), "Position Filled");
                    }
                }
            }
        }
    }

    /**
     * Updates application status without a review note.
     * <p>
     * Convenience overload that delegates to {@link #updateApplicationStatus(String, String, ApplicationStatus, String)}
     * with a null note.
     * </p>
     *
     * @param token          valid session token
     * @param applicationId  the application to update
     * @param newStatus      the new status to set
     * @return the updated {@link Application} entity
     */
    public Application updateApplicationStatus(String token, String applicationId,
                                                ApplicationStatus newStatus) {
        return updateApplicationStatus(token, applicationId, newStatus, null);
    }

    /**
     * Sorts a list of applications by submission time.
     *
     * @param apps       the list of applications to sort (modified in place)
     * @param ascending  true for earliest-first order, false for latest-first
     */
    public void sortBySubmissionTime(List<Application> apps, boolean ascending) {
        apps.sort((a, b) -> {
            String t1 = a.getSubmissionTime() != null ? a.getSubmissionTime() : "";
            String t2 = b.getSubmissionTime() != null ? b.getSubmissionTime() : "";
            return ascending ? t1.compareTo(t2) : t2.compareTo(t1);
        });
    }

    /**
     * Sorts a list of applications by applicant's major in alphabetical order.
     *
     * @param apps  the list of applications to sort (modified in place)
     */
    public void sortByMajor(List<Application> apps) {
        apps.sort((a, b) -> {
            String m1 = getMajorForApp(a);
            String m2 = getMajorForApp(b);
            return m1.compareTo(m2);
        });
    }

    /**
     * Retrieves the major for an application's applicant.
     *
     * @param app  the application
     * @return the applicant's major, or empty string if not found
     */
    private String getMajorForApp(Application app) {
        Optional<User> user = userRepository.findByUserId(app.getApplicantUserId());
        return user.map(u -> u.getMajor() != null ? u.getMajor() : "").orElse("");
    }

    /**
     * Performs AI-powered comparison of selected TA candidates.
     * <p>
     * Sends candidate information (name, major, skills) along with the position requirements
     * to the Alibaba Cloud DashScope AI API, and returns a ranked comparison with strengths
     * and weaknesses for each candidate.
     * </p>
     * <p>
     * All selected applications must belong to the same position.
     * Requires a valid API key in {@code data/ai-config.properties}.
     * </p>
     *
     * @param selectedApps  list of applications to compare (must be from same position)
     * @return AI-generated comparison result in English
     * @throws Exception if candidates are from different positions, API key not configured, or API call fails
     */
    public String simulateAIComparison(List<Application> selectedApps) throws Exception {
        if (selectedApps == null || selectedApps.isEmpty()) return "No candidates selected.";

        String firstPositionId = selectedApps.get(0).getPositionId();
        for (Application app : selectedApps) {
            if (!app.getPositionId().equals(firstPositionId)) {
                throw new AppException("Please select TAs applying for the same position");
            }
        }

        Properties aiConfig = loadAiConfig();
        String apiKey = aiConfig.getProperty("ALIYUN_API_KEY");
        String modelName = aiConfig.getProperty("ALIYUN_MODEL", "qwen-plus");

        if (apiKey == null || apiKey.isEmpty() || apiKey.equals("YOUR_API_KEY_HERE")) {
            throw new Exception("API KEY 未配置！请在 TA_Recruitment_software/data/ai-config.properties 中填入您的 API KEY。");
        }

        String positionId = selectedApps.get(0).getPositionId();
        Position position = positionRepository.findById(positionId).orElse(null);
        String jobTitle = position != null ? position.getJobTitle() : "Unknown Position";
        String requirements = position != null ? position.getRequirements() : "None";
        // 2. 构造 Prompt
        String systemPrompt = "你是一名非常专业的高校助教招聘 HR。请帮我对比以下候选人，指出各自的优劣势，并给出一个明确的推荐排名。必须使用英文回复。";
        StringBuilder userPrompt = new StringBuilder();
        userPrompt.append("【岗位信息】\n");
        userPrompt.append("名称：").append(jobTitle).append("\n");
        userPrompt.append("要求：").append(requirements).append("\n\n");
        userPrompt.append("【候选人列表】\n");

        int count = 1;
        for (Application app : selectedApps) {
            Optional<User> userOpt = userRepository.findByUserId(app.getApplicantUserId());
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                userPrompt.append("候选人 ").append(count++).append(":\n");
                userPrompt.append("- 姓名：").append(user.getFullName()).append("\n");
                userPrompt.append("- 专业：").append(user.getMajor() != null ? user.getMajor() : "N/A").append("\n");
                userPrompt.append("- 特长/履历：").append(user.getSkills() != null ? user.getSkills() : "N/A").append("\n\n");
            }
        }

        return callAliyunBailianAPI(apiKey, modelName, systemPrompt, userPrompt.toString());
    }

    /**
     * Loads AI configuration from {@code data/ai-config.properties}.
     *
     * @return Properties object containing API key and model settings
     */
    private Properties loadAiConfig() {
        Properties prop = new Properties();
        try (InputStream input = new FileInputStream("data/ai-config.properties")) {
            prop.load(input);
        } catch (Exception ex) {
            // File not found or read failed, return empty properties
        }
        return prop;
    }

    /**
     * Calls the Alibaba Cloud DashScope Chat Completions API for AI-powered operations.
     *
     * @param apiKey       API authentication key
     * @param modelName    model name (e.g., "qwen-plus")
     * @param systemPrompt system message defining AI behavior
     * @param userPrompt   user input prompt
     * @return the AI-generated response content
     * @throws Exception if HTTP request fails or returns error status
     */
    private String callAliyunBailianAPI(String apiKey, String modelName, String systemPrompt, String userPrompt) throws Exception {
        URL url = new URL("https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Authorization", "Bearer " + apiKey);
        conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
        conn.setDoOutput(true);

        String jsonInputString = String.format(
            "{\"model\": \"%s\", \"messages\": [{\"role\": \"system\", \"content\": \"%s\"}, {\"role\": \"user\", \"content\": \"%s\"}]}",
            modelName, escapeJson(systemPrompt), escapeJson(userPrompt)
        );

        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = jsonInputString.getBytes("utf-8");
            os.write(input, 0, input.length);
        }

        int code = conn.getResponseCode();
        InputStream is = code >= 200 && code < 300 ? conn.getInputStream() : conn.getErrorStream();

        StringBuilder response = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is, "utf-8"))) {
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
        }

        if (code >= 200 && code < 300) {
            return extractContentFromJson(response.toString());
        } else {
            throw new Exception("API 调用失败 (HTTP " + code + "): " + response.toString());
        }
    }

    /**
     * Escapes special characters for JSON string embedding.
     *
     * @param s input string
     * @return escaped string safe for JSON
     */
    private String escapeJson(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "");
    }

    /**
     * Extracts the "content" field from a JSON chat completion response.
     *
     * @param json raw JSON response string
     * @return the content value, or error message if parsing fails
     */
    private String extractContentFromJson(String json) {
        String marker = "\"content\":\"";
        int start = json.indexOf(marker);
        if (start == -1) return "解析返回结果失败。";
        start += marker.length();

        int end = json.indexOf("\"}", start);
        if (end == -1) return json;

        String content = json.substring(start, end);
        return content.replace("\\n", "\n").replace("\\\"", "\"").replace("\\\\", "\\");
    }
}
