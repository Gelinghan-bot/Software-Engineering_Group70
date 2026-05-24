package TA_Recruitment_software.mo_publish;

import TA_Recruitment_software.admin_system.foundation.AppException;
import TA_Recruitment_software.admin_system.foundation.IdGenerator;
import TA_Recruitment_software.admin_system.foundation.ValidationUtil;
import TA_Recruitment_software.admin_system.model.Position;
import TA_Recruitment_software.admin_system.model.PositionStatus;
import TA_Recruitment_software.admin_system.model.Role;
import TA_Recruitment_software.admin_system.model.User;
import TA_Recruitment_software.admin_system.model.Application;
import TA_Recruitment_software.admin_system.model.ApplicationStatus;
import TA_Recruitment_software.admin_system.repository.ApplicationRepository;
import TA_Recruitment_software.admin_system.repository.PositionRepository;
import TA_Recruitment_software.admin_system.repository.UserRepository;
import TA_Recruitment_software.auth.SessionContext;
import TA_Recruitment_software.auth.SessionManager;
import TA_Recruitment_software.auth.TaNotificationService;
import TA_Recruitment_software.ta_jobs.CurrentSemesterStore;
import TA_Recruitment_software.ta_jobs.PositionSemesterStore;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Service layer for Module Organizer (MO) job publishing and management operations.
 * <p>
 * This class provides business logic for publishing, updating, and closing job positions,
 * as well as AI-powered job description generation. All operations require MO or ADMIN role
 * authentication via session token validation.
 * </p>
 * <p>
 * Key features:
 * <ul>
 *   <li>Publish new TA positions with validation and semester registration</li>
 *   <li>Update position details, deadline, or close positions</li>
 *   <li>Auto-close expired positions when listing</li>
 *   <li>Generate AI-assisted job descriptions via Alibaba Cloud DashScope API</li>
 * </ul>
 * </p>
 *
 * @author Group70
 * @see Position
 * @see SessionManager
 */
public class MOPublishService {
    private final PositionRepository positionRepository;
    private final UserRepository userRepository;
    private final SessionManager sessionManager;
    private final TaNotificationService taNotificationService;
    private final ApplicationRepository applicationRepository;

    /**
     * Constructs a MOPublishService with required dependencies.
     *
     * @param positionRepository     repository for position CRUD operations
     * @param userRepository         repository for user queries
     * @param sessionManager         session manager for authentication
     * @param taNotificationService  service for sending notifications to applicants
     * @param applicationRepository  repository for application queries
     */
    public MOPublishService(
        PositionRepository positionRepository,
        UserRepository userRepository,
        SessionManager sessionManager,
        TaNotificationService taNotificationService,
        ApplicationRepository applicationRepository
    ) {
        this.positionRepository = positionRepository;
        this.userRepository = userRepository;
        this.sessionManager = sessionManager;
        this.taNotificationService = taNotificationService;
        this.applicationRepository = applicationRepository;
    }

    /**
     * Publishes a new TA position after validating all input fields and checking permissions.
     * <p>
     * The position is saved with OPEN status and registered with the specified semester.
     * Only MO and ADMIN roles can publish positions.
     * </p>
     *
     * @param token              valid session token for authentication
     * @param jobTitle           the job title (max 200 chars)
     * @param grade              target student grade (e.g., "Year 2", "All Grades")
     * @param major              target major (e.g., "Computer Science", "All majors")
     * @param jobType            job category (e.g., "Grading", "Lab Support")
     * @param jobDescription     detailed job description (max 3000 chars)
     * @param requirements       applicant requirements (max 3000 chars)
     * @param interviewLocation  interview venue location
     * @param deadline           application deadline in "yyyy-MM-dd" format
     * @param semester           academic semester (e.g., "2025-Fall"); uses current semester if empty
     * @param headcount          number of TAs needed (must be positive)
     * @return the saved {@link Position} entity with generated positionId
     * @throws AppException if permission denied, validation fails, or publisher not found
     */
    public Position publishPosition(
        String token,
        String jobTitle,
        String grade,
        String major,
        String jobType,
        String jobDescription,
        String requirements,
        String interviewLocation,
        String deadline,
        String semester,
        int headcount
    ) {
        SessionContext session = sessionManager.requireSession(token);
        if (session.getRole() != Role.MO && session.getRole() != Role.ADMIN) {
            throw new AppException("Permission denied. Only MO and ADMIN can publish positions.");
        }
        User publisher = userRepository.findByUserId(session.getUserId())
            .orElseThrow(() -> new AppException("Account not found."));

        String checkedTitle = ValidationUtil.sanitizeText(jobTitle != null ? jobTitle.replace("\n", " ").replace("\r", "") : "", "Job title", 200);
        String checkedGrade = ValidationUtil.sanitizeText(grade, "Grade", 50);
        String checkedMajor = ValidationUtil.sanitizeText(major, "Major", 50);
        String checkedType = ValidationUtil.sanitizeText(jobType, "Job type", 80);
        String checkedDesc = ValidationUtil.sanitizeText(jobDescription != null ? jobDescription.replace("\n", " ").replace("\r", "") : "", "Job description", 3000);

        String checkedReq = ValidationUtil.sanitizeText(requirements != null ? requirements.replace("\n", " ").replace("\r", "") : "", "Requirements", 3000);
        String checkedLoc = ValidationUtil.sanitizeText(interviewLocation, "Interview location", 100);
        LocalDate checkedDeadline = ValidationUtil.validateDate(deadline, "Deadline");
        ValidationUtil.ensureTodayOrFuture(checkedDeadline, "Deadline");

        if (headcount <= 0) {
            throw new AppException("Headcount must be positive.");
        }

        Position position = new Position();
        position.setPositionId(IdGenerator.nextId("POS"));
        position.setJobTitle(checkedTitle);
        position.setGrade(checkedGrade);
        position.setMajor(checkedMajor);
        position.setJobType(checkedType);
        position.setResponsibleMO(publisher.getFullName());
        position.setJobDescription(checkedDesc);
        position.setRequirements(checkedReq);
        position.setInterviewLocation(checkedLoc);
        position.setDeadline(checkedDeadline.toString());
        position.setPublishedByUserId(publisher.getUserId());
        position.setStatus(PositionStatus.OPEN);
        position.setHeadcount(headcount);

        positionRepository.save(position);
        String sem = (semester != null && !semester.trim().isEmpty()) ? semester.trim() : CurrentSemesterStore.readCurrentSemester();
        PositionSemesterStore.register(position.getPositionId(), sem);
        return position;
    }

    /**
     * Updates the application deadline of an existing position and notifies all applicants.
     * <p>
     * Only the position owner (MO) or ADMIN can update the deadline.
     * The new deadline must be today or in the future.
     * </p>
     *
     * @param token        valid session token
     * @param positionId   the position to update
     * @param newDeadline  new deadline in "yyyy-MM-dd" format
     * @return the updated {@link Position} entity
     * @throws AppException if permission denied, position not found, or date validation fails
     */
    public Position updateDeadline(String token, String positionId, String newDeadline) {
        SessionContext session = sessionManager.requireSession(token);
        if (session.getRole() != Role.MO && session.getRole() != Role.ADMIN) {
            throw new AppException("Permission denied. Only MO and ADMIN can update positions.");
        }
        Position position = positionRepository.findById(ValidationUtil.requireNotBlank(positionId, "Position ID"))
            .orElseThrow(() -> new AppException("Position not found."));

        if (session.getRole() == Role.MO && !position.getPublishedByUserId().equals(session.getUserId())) {
            throw new AppException("Permission denied. You can only update your own positions.");
        }
        LocalDate checkedDate = ValidationUtil.validateDate(newDeadline, "Deadline");
        ValidationUtil.ensureTodayOrFuture(checkedDate, "Deadline");
        position.setDeadline(checkedDate.toString());
        positionRepository.save(position);

        String jobTitle = position.getJobTitle();
        String deadlineStr = checkedDate.toString();
        for (Application app : applicationRepository.findByPosition(positionId)) {
            taNotificationService.pushDeadlineExtendedNotification(
                app.getApplicantUserId(), positionId, jobTitle, deadlineStr);
        }
        return position;
    }

    /**
     * Updates multiple fields of an existing position and notifies applicants of changes.
     * <p>
     * Tracks which fields changed to build a human-readable summary for notifications.
     * Only the position owner (MO) or ADMIN can update the position.
     * </p>
     *
     * @param token              valid session token
     * @param positionId         the position to update
     * @param jobTitle           new job title
     * @param grade              new target grade
     * @param major              new target major
     * @param jobType            new job category
     * @param jobDescription     new job description
     * @param requirements       new applicant requirements
     * @param interviewLocation  new interview location
     * @param deadline           new deadline
     * @param headcount          new headcount
     * @return the updated {@link Position} entity
     * @throws AppException if permission denied, position not found, or validation fails
     */
    public Position updatePositionDetails(
        String token,
        String positionId,
        String jobTitle,
        String grade,
        String major,
        String jobType,
        String jobDescription,
        String requirements,
        String interviewLocation,
        String deadline,
        int headcount
    ) {
        SessionContext session = sessionManager.requireSession(token);
        if (session.getRole() != Role.MO && session.getRole() != Role.ADMIN) {
            throw new AppException("Permission denied. Only MO and ADMIN can update positions.");
        }
        
        Position position = positionRepository.findById(ValidationUtil.requireNotBlank(positionId, "Position ID"))
            .orElseThrow(() -> new AppException("Position not found."));

        if (session.getRole() == Role.MO && !position.getPublishedByUserId().equals(session.getUserId())) {
            throw new AppException("Permission denied. You can only update your own positions.");
        }

        String checkedTitle = ValidationUtil.sanitizeText(jobTitle != null ? jobTitle.replace("\n", " ").replace("\r", "") : "", "Job title", 200);
        String checkedGrade = ValidationUtil.sanitizeText(grade, "Grade", 50);
        String checkedMajor = ValidationUtil.sanitizeText(major, "Major", 50);
        String checkedType = ValidationUtil.sanitizeText(jobType, "Job type", 80);
        String checkedDesc = ValidationUtil.sanitizeText(jobDescription != null ? jobDescription.replace("\n", " ").replace("\r", "") : "", "Job description", 3000);

        String checkedReq = ValidationUtil.sanitizeText(requirements != null ? requirements.replace("\n", " ").replace("\r", "") : "", "Requirements", 3000);
        String checkedLoc = ValidationUtil.sanitizeText(interviewLocation, "Interview location", 100);
        LocalDate checkedDeadline = ValidationUtil.validateDate(deadline, "Deadline");
        ValidationUtil.ensureTodayOrFuture(checkedDeadline, "Deadline");

        if (headcount <= 0) {
            throw new AppException("Headcount must be positive.");
        }

        // Track what changed to build a human-readable summary for the notification note
        java.util.List<String> changes = new java.util.ArrayList<>();
        if (!checkedTitle.equals(position.getJobTitle())) changes.add("Title: \"" + checkedTitle + "\"");
        LocalDate oldDeadline = LocalDate.parse(position.getDeadline());
        if (!checkedDeadline.equals(oldDeadline)) changes.add("Deadline: " + checkedDeadline);
        if (!checkedDesc.equals(position.getJobDescription())) changes.add("Description updated");
        if (!checkedReq.equals(position.getRequirements())) changes.add("Requirements updated");
        if (!checkedLoc.equals(position.getInterviewLocation())) changes.add("Location: \"" + checkedLoc + "\"");
        if (headcount != position.getHeadcount()) changes.add("Headcount: " + headcount);

        position.setJobTitle(checkedTitle);
        position.setGrade(checkedGrade);
        position.setMajor(checkedMajor);
        position.setJobType(checkedType);
        position.setJobDescription(checkedDesc);
        position.setRequirements(checkedReq);
        position.setInterviewLocation(checkedLoc);
        position.setDeadline(checkedDeadline.toString());
        position.setHeadcount(headcount);

        positionRepository.save(position);

        if (!changes.isEmpty()) {
            String summary = String.join("; ", changes);
            for (Application app : applicationRepository.findByPosition(positionId)) {
                taNotificationService.pushPositionUpdatedNotification(
                    app.getApplicantUserId(), positionId, checkedTitle, summary);
            }
        }
        return position;
    }

    /**
     * Closes a position, making it no longer accept new applications.
     * <p>
     * Notifies all applicants with pending/shortlisted/interviewed/offered status
     * that the position has been closed.
     * Only the position owner (MO) or ADMIN can close positions.
     * </p>
     *
     * @param token        valid session token
     * @param positionId   the position to close
     * @return the closed {@link Position} entity
     * @throws AppException if permission denied or position not found
     */
    public Position closePosition(String token, String positionId) {
        SessionContext session = sessionManager.requireSession(token);
        if (session.getRole() != Role.MO && session.getRole() != Role.ADMIN) {
            throw new AppException("Permission denied. Only MO and ADMIN can close positions.");
        }
        Position position = positionRepository.findById(ValidationUtil.requireNotBlank(positionId, "Position ID"))
            .orElseThrow(() -> new AppException("Position not found."));

        if (session.getRole() == Role.MO && !position.getPublishedByUserId().equals(session.getUserId())) {
            throw new AppException("Permission denied. You can only close your own positions.");
        }
        position.setStatus(PositionStatus.CLOSED);
        positionRepository.save(position);

        String jobTitle = position.getJobTitle();
        for (Application app : applicationRepository.findByPosition(positionId)) {
            ApplicationStatus s = app.getStatus();
            if (s == ApplicationStatus.PENDING || s == ApplicationStatus.SUBMITTED
                    || s == ApplicationStatus.SHORTLISTED || s == ApplicationStatus.INTERVIEWED
                    || s == ApplicationStatus.OFFERED) {
                taNotificationService.pushPositionClosedNotification(app.getApplicantUserId(), positionId, jobTitle);
            }
        }
        return position;
    }

    /**
     * Lists all positions owned by the current MO, or all positions if ADMIN.
     * <p>
     * Automatically checks and updates expired positions (deadline passed) to EXPIRED status.
     * </p>
     *
     * @param token  valid session token
     * @return list of positions accessible to the current user
     * @throws AppException if permission denied
     */
    public List<Position> listMyPositions(String token) {
        SessionContext session = sessionManager.requireSession(token);
        if (session.getRole() != Role.MO && session.getRole() != Role.ADMIN) {
            throw new AppException("Permission denied. Only MO and ADMIN can manage positions.");
        }
        
        List<Position> positions;
        if (session.getRole() == Role.ADMIN) {
            positions = positionRepository.findAll();
        } else {
            positions = positionRepository.findByPublisher(session.getUserId());
        }

        // Dynamically check and update expired positions
        LocalDate today = LocalDate.now();
        boolean changed = false;
        for (Position p : positions) {
            if (p.getStatus() == PositionStatus.OPEN) {
                LocalDate dl = LocalDate.parse(p.getDeadline());
                if (dl.isBefore(today)) {
                    p.setStatus(PositionStatus.EXPIRED);
                    positionRepository.save(p);
                    changed = true;
                }
            }
        }
        
        return positions;
    }

    /**
     * Generates a job description template using Alibaba Cloud DashScope AI API.
     * <p>
     * Sends a prompt to the AI model (default: qwen-plus) with the provided job context,
     * and parses the response to extract title, description, and requirements wrapped in
     * XML-style tags (<TITLE>, <DESC>, <REQ>).
     * </p>
     * <p>
     * Requires a valid API key configured in {@code data/ai-config.properties}.
     * </p>
     *
     * @param grade       target student grade
     * @param major       target major
     * @param jobType     job category
     * @param courseName  course name for context
     * @return a map with keys "title", "desc", "req" containing AI-generated content
     * @throws Exception if API key not configured or API call fails
     * @see #loadAiConfig()
     * @see #callAliyunBailianAPI(String, String, String, String)
     */
    public Map<String, String> generateAITemplate(String grade, String major, String jobType, String courseName) throws Exception {
        Properties aiConfig = loadAiConfig();
        String apiKey = aiConfig.getProperty("ALIYUN_API_KEY");
        String modelName = aiConfig.getProperty("ALIYUN_MODEL", "qwen-plus");
        if (apiKey == null || apiKey.isEmpty() || apiKey.equals("YOUR_API_KEY_HERE")) {
            throw new Exception("API KEY 未配置！请在 TA_Recruitment_software/data/ai-config.properties 中填入您的 API KEY。");
        }
        
        String actualCourse = (courseName == null || courseName.trim().isEmpty()) ? "Unknown Course" : courseName.trim();

        String systemPrompt = "You are a professional university HR specializing in Teaching Assistant recruitment. You need to help the Module Organizer (MO) automatically draft a professional and detailed TA job posting.";
        String userPrompt = String.format("We need to recruit a TA for Course: [%s]. The target students are Grade: [%s], Major: [%s]. The main job type is: [%s].\n" + 
            "Please help me draft the following three parts:\n" + 
            "1. Job Title\n2. Job Description\n3. Job Requirements\n\n" + 
            "Requirement: You MUST strictly follow the format below (return directly without markdown code blocks or extra conversational text):\n" + 
            "<TITLE>Your generated job title</TITLE>\n" + 
            "<DESC>Your generated job description...</DESC>\n" + 
            "<REQ>Your generated job requirements...</REQ>", 
            actualCourse,
            "All Grades".equals(grade) ? "Any" : grade, 
            "All majors".equals(major) ? "Any" : major, 
            "All Categories".equals(jobType) ? "Daily assistance" : jobType);

        String rawResponse = callAliyunBailianAPI(apiKey, modelName, systemPrompt, userPrompt);
        
        Map<String, String> result = new HashMap<>();
        result.put("title", extractTag(rawResponse, "<TITLE>", "</TITLE>"));
        result.put("desc", extractTag(rawResponse, "<DESC>", "</DESC>"));
        result.put("req", extractTag(rawResponse, "<REQ>", "</REQ>"));
        
        return result;
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
        } catch (Exception ex) { }
        return prop;
    }

    /**
     * Calls the Alibaba Cloud DashScope Chat Completions API.
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
            String content = extractContentFromJson(response.toString());
            return content;
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
     * @return the content value, or empty string if parsing fails
     */
    private String extractContentFromJson(String json) {
        String marker = "\"content\":\"";
        int start = json.indexOf(marker);
        if (start == -1) return "";
        start += marker.length();
        int end = json.indexOf("\"}", start);
        if (end == -1) return json;
        String content = json.substring(start, end);
        return content.replace("\\n", "\n").replace("\\\"", "\"").replace("\\\\", "\\");
    }

    /**
     * Extracts text between XML-style start and end tags.
     *
     * @param source   source string containing tags
     * @param startTag opening tag (e.g., "<TITLE>")
     * @param endTag   closing tag (e.g., "</TITLE>")
     * @return trimmed content between tags, or empty string if tags not found
     */
    private String extractTag(String source, String startTag, String endTag) {
        int start = source.indexOf(startTag);
        int end = source.indexOf(endTag);
        if (start != -1 && end != -1 && end > start) {
            return source.substring(start + startTag.length(), end).trim();
        }
        return "";
    }
}
