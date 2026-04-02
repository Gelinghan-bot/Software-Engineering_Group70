package TA_Recruitment_software.admin_system.repository;

import TA_Recruitment_software.admin_system.foundation.FileStorageUtil;
import TA_Recruitment_software.admin_system.model.Application;
import TA_Recruitment_software.admin_system.model.ApplicationStatus;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ApplicationRepository {
    private static final String FILE_NAME = "applications.csv";
    private static final String HEADER =
        "applicationId,applicantUserId,positionId,status,submissionTime,updatedTime";

    public List<Application> findAll() {
        List<List<String>> rows = FileStorageUtil.readRows(FILE_NAME, HEADER);
        List<Application> applications = new ArrayList<>();
        for (List<String> row : rows) {
            applications.add(fromRow(row));
        }
        return applications;
    }

    public Optional<Application> findById(String applicationId) {
        return findAll().stream().filter(a -> a.getApplicationId().equals(applicationId)).findFirst();
    }

    public List<Application> findByApplicant(String applicantUserId) {
        List<Application> result = new ArrayList<>();
        for (Application app : findAll()) {
            if (app.getApplicantUserId().equals(applicantUserId)) {
                result.add(app);
            }
        }
        return result;
    }

    public List<Application> findByPosition(String positionId) {
        List<Application> result = new ArrayList<>();
        for (Application app : findAll()) {
            if (app.getPositionId().equals(positionId)) {
                result.add(app);
            }
        }
        return result;
    }

    public Optional<Application> findByApplicantAndPosition(String applicantUserId, String positionId) {
        return findAll().stream()
            .filter(a -> a.getApplicantUserId().equals(applicantUserId) && a.getPositionId().equals(positionId))
            .findFirst();
    }

    public void save(Application target) {
        List<Application> applications = findAll();
        boolean updated = false;
        for (int i = 0; i < applications.size(); i++) {
            if (applications.get(i).getApplicationId().equals(target.getApplicationId())) {
                applications.set(i, target);
                updated = true;
                break;
            }
        }
        if (!updated) {
            applications.add(target);
        }
        saveAll(applications);
    }

    private void saveAll(List<Application> applications) {
        List<List<String>> rows = new ArrayList<>();
        for (Application app : applications) {
            rows.add(toRow(app));
        }
        FileStorageUtil.writeRows(FILE_NAME, HEADER, rows);
    }

    private List<String> toRow(Application app) {
        List<String> row = new ArrayList<>();
        row.add(app.getApplicationId());
        row.add(nvl(app.getApplicantUserId()));
        row.add(nvl(app.getPositionId()));
        row.add(app.getStatus().name());
        row.add(nvl(app.getSubmissionTime()));
        row.add(nvl(app.getUpdatedTime()));
        return row;
    }

    private Application fromRow(List<String> row) {
        Application app = new Application();
        app.setApplicationId(cell(row, 0));
        app.setApplicantUserId(cell(row, 1));
        app.setPositionId(cell(row, 2));
        app.setStatus(ApplicationStatus.valueOf(cell(row, 3)));
        app.setSubmissionTime(cell(row, 4));
        app.setUpdatedTime(cell(row, 5));
        return app;
    }

    private String cell(List<String> row, int index) {
        if (index >= row.size()) {
            return "";
        }
        return row.get(index);
    }

    private String nvl(String value) {
        return value == null ? "" : value;
    }
}
