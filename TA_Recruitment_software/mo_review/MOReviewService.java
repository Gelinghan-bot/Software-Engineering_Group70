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
import java.util.Set;

import java.util.Properties;
import java.io.InputStream;
import java.io.FileInputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.OutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class MOReviewService {
    private final PositionRepository positionRepository;
    private final ApplicationRepository applicationRepository;
    private final UserRepository userRepository;
    private final SessionManager sessionManager;

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

    public MOReviewService(
        PositionRepository positionRepository,
        ApplicationRepository applicationRepository,
        UserRepository userRepository,
        SessionManager sessionManager
    ) {
        this.positionRepository = positionRepository;
        this.applicationRepository = applicationRepository;
        this.userRepository = userRepository;
        this.sessionManager = sessionManager;
    }

    public List<Application> listApplicationsForPosition(String token, String positionId) {
        SessionContext session = sessionManager.requireSession(token);
        if (session.getRole() != Role.MO && session.getRole() != Role.ADMIN) {
            throw new AppException("Permission denied. Only MO and ADMIN can review applications.");
        }
        String checkedPositionId = ValidationUtil.requireNotBlank(positionId, "Position ID");
        Position position = positionRepository.findById(checkedPositionId)
            .orElseThrow(() -> new AppException("Position not found."));
        
        // Admin can review any position; MO can only review their own
        if (session.getRole() == Role.MO && !position.getPublishedByUserId().equals(session.getUserId())) {
            throw new AppException("Permission denied. You can only review your own positions.");
        }
        return applicationRepository.findByPosition(checkedPositionId);
    }

    public List<Application> listAllApplicationsOfMyPositions(String token) {
        SessionContext session = sessionManager.requireSession(token);
        if (session.getRole() != Role.MO && session.getRole() != Role.ADMIN) {
            throw new AppException("Permission denied. Only MO and ADMIN can review applications.");
        }
        
        // Admin can see all applications; MO can only see their own positions' applications
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

    public Optional<User> getApplicantInfo(String applicantUserId) {
        return userRepository.findByUserId(applicantUserId);
    }

    public Optional<Position> getPositionInfo(String positionId) {
        return positionRepository.findById(positionId);
    }

    public List<ApplicationStatus> getValidNextStatuses(ApplicationStatus current) {
        List<ApplicationStatus> next = VALID_TRANSITIONS.get(current);
        return next != null ? new ArrayList<>(next) : Collections.emptyList();
    }

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

        // Admin can update any application; MO can only update for their own positions
        if (session.getRole() == Role.MO && !position.getPublishedByUserId().equals(session.getUserId())) {
            throw new AppException("Permission denied. You can only update applications for your own positions.");
        }

        List<ApplicationStatus> validNext = getValidNextStatuses(app.getStatus());
        if (!validNext.contains(newStatus)) {
            throw new AppException("Invalid status transition: " + app.getStatus() + " -> " + newStatus
                + ". Allowed: " + validNext);
        }

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

        // Auto-close position and reject remaining TAs if hired headcount reached limit
        if (newStatus == ApplicationStatus.HIRED && position.getHeadcount() > 0) {
            checkAndClosePosition(token, position, now);
        }

        return app;
    }

    private void checkAndClosePosition(String token, Position position, String now) {
        List<Application> allApps = applicationRepository.findByPosition(position.getPositionId());
        long hiredCount = allApps.stream().filter(a -> a.getStatus() == ApplicationStatus.HIRED).count();

        if (hiredCount >= position.getHeadcount()) {
            // Reached capacity. Close position.
            position.setStatus(PositionStatus.CLOSED);
            positionRepository.save(position);

            // Reject all remaining pending/shortlisted/interviewed/offered
            for (Application a : allApps) {
                if (a.getStatus() != ApplicationStatus.HIRED && a.getStatus() != ApplicationStatus.REJECTED) {
                    List<ApplicationStatus> validNext = getValidNextStatuses(a.getStatus());
                    if (validNext.contains(ApplicationStatus.REJECTED)) {
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
                    }
                }
            }
        }
    }

    public Application updateApplicationStatus(String token, String applicationId,
                                                ApplicationStatus newStatus) {
        return updateApplicationStatus(token, applicationId, newStatus, null);
    }

    public void sortBySubmissionTime(List<Application> apps, boolean ascending) {
        apps.sort((a, b) -> {
            String t1 = a.getSubmissionTime() != null ? a.getSubmissionTime() : "";
            String t2 = b.getSubmissionTime() != null ? b.getSubmissionTime() : "";
            return ascending ? t1.compareTo(t2) : t2.compareTo(t1);
        });
    }

    public void sortByMajor(List<Application> apps) {
        apps.sort((a, b) -> {
            String m1 = getMajorForApp(a);
            String m2 = getMajorForApp(b);
            return m1.compareTo(m2);
        });
    }

    private String getMajorForApp(Application app) {
        Optional<User> user = userRepository.findByUserId(app.getApplicantUserId());
        return user.map(u -> u.getMajor() != null ? u.getMajor() : "").orElse("");
    }

    public String simulateAIComparison(List<Application> selectedApps) throws Exception {
        if (selectedApps == null || selectedApps.isEmpty()) return "No candidates selected.";
        
        // 1. 读取 API KEY 和 模型配置
        Properties aiConfig = loadAiConfig();
        String apiKey = aiConfig.getProperty("ALIYUN_API_KEY");
        String modelName = aiConfig.getProperty("ALIYUN_MODEL", "qwen-plus"); // 如果没配，默认用 qwen-plus

        if (apiKey == null || apiKey.isEmpty() || apiKey.equals("YOUR_API_KEY_HERE")) {
            throw new Exception("API KEY 未配置！请在 TA_Recruitment_software/data/ai-config.properties 中填入您的 API KEY。");
        }

        String positionId = selectedApps.get(0).getPositionId();
        Position position = positionRepository.findById(positionId).orElse(null);
        String jobTitle = position != null ? position.getJobTitle() : "Unknown Position";
        String requirements = position != null ? position.getRequirements() : "None";
        
        // 2. 构造 Prompt
        String systemPrompt = "你是一名非常专业的高校助教招聘 HR。请帮我对比以下候选人，指出各自的优劣势，并给出一个明确的推荐排名。必须使用中文回复。";
        
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
        
        // 3. 调用阿里云 API
        return callAliyunBailianAPI(apiKey, modelName, systemPrompt, userPrompt.toString());
    }

    private Properties loadAiConfig() {
        Properties prop = new Properties();
        try (InputStream input = new FileInputStream("data/ai-config.properties")) {
            prop.load(input);
        } catch (Exception ex) {
            // 文件不存在或读取失败，返回空属性
        }
        return prop;
    }

    private String callAliyunBailianAPI(String apiKey, String modelName, String systemPrompt, String userPrompt) throws Exception {
        URL url = new URL("https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Authorization", "Bearer " + apiKey);
        conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
        conn.setDoOutput(true);

        // 拼接最简单的 JSON 以避免引入额外的 Jar 包
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

    private String escapeJson(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "");
    }

    private String extractContentFromJson(String json) {
        // 这是为了不引新 Jar 包写的一个极其简易的 JSON 截取方法
        String marker = "\"content\":\"";
        int start = json.indexOf(marker);
        if (start == -1) return "解析返回结果失败。";
        start += marker.length();
        
        // 找到 content 后面的结束位置 (遇到 "})
        int end = json.indexOf("\"}", start);
        if (end == -1) return json;
        
        String content = json.substring(start, end);
        // 处理大模型返回的转义换行符和引号
        return content.replace("\\n", "\n").replace("\\\"", "\"").replace("\\\\", "\\");
    }
}
