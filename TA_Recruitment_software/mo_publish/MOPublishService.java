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

public class MOPublishService {
    private final PositionRepository positionRepository;
    private final UserRepository userRepository;
    private final SessionManager sessionManager;
    private final TaNotificationService taNotificationService;
    private final ApplicationRepository applicationRepository;

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

    private Properties loadAiConfig() {
        Properties prop = new Properties();
        try (InputStream input = new FileInputStream("data/ai-config.properties")) {
            prop.load(input);
        } catch (Exception ex) { }
        return prop;
    }

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

    private String escapeJson(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "");
    }

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

    private String extractTag(String source, String startTag, String endTag) {
        int start = source.indexOf(startTag);
        int end = source.indexOf(endTag);
        if (start != -1 && end != -1 && end > start) {
            return source.substring(start + startTag.length(), end).trim();
        }
        return "";
    }
}