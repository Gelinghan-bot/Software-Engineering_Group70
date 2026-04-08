package TA_Recruitment_software.admin_system.repository;

import TA_Recruitment_software.admin_system.foundation.FileStorageUtil;
import TA_Recruitment_software.admin_system.model.ApprovalStatus;
import TA_Recruitment_software.admin_system.model.Role;
import TA_Recruitment_software.admin_system.model.User;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserRepository {
    private static final String FILE_NAME = "users.csv";
    private static final String HEADER =
        "userId,role,accountId,passwordHash,fullName,studentId,department,major,email,phone,skills,cvFilePath,approvalStatus,enabled,createdAt";

    public List<User> findAll() {
        List<List<String>> rows = FileStorageUtil.readRows(FILE_NAME, HEADER);
        List<User> users = new ArrayList<>();
        for (List<String> row : rows) {
            users.add(fromRow(row));
        }
        return users;
    }

    public Optional<User> findByUserId(String userId) {
        return findAll().stream().filter(u -> u.getUserId().equals(userId)).findFirst();
    }

    public Optional<User> findByAccountId(String accountId) {
        return findAll().stream().filter(u -> u.getAccountId().equals(accountId)).findFirst();
    }

    public List<User> findByApprovalStatus(ApprovalStatus status) {
        List<User> result = new ArrayList<>();
        for (User user : findAll()) {
            if (user.getApprovalStatus() == status) {
                result.add(user);
            }
        }
        return result;
    }

    public void save(User target) {
        List<User> users = findAll();
        boolean updated = false;
        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).getUserId().equals(target.getUserId())) {
                users.set(i, target);
                updated = true;
                break;
            }
        }
        if (!updated) {
            users.add(target);
        }
        saveAll(users);
    }

    private void saveAll(List<User> users) {
        List<List<String>> rows = new ArrayList<>();
        for (User user : users) {
            rows.add(toRow(user));
        }
        FileStorageUtil.writeRows(FILE_NAME, HEADER, rows);
    }

    private List<String> toRow(User user) {
        List<String> row = new ArrayList<>();
        row.add(user.getUserId());
        row.add(user.getRole().name());
        row.add(user.getAccountId());
        row.add(user.getPasswordHash());
        row.add(nvl(user.getFullName()));
        row.add(nvl(user.getStudentId()));
        row.add(nvl(user.getDepartment()));
        row.add(nvl(user.getMajor()));
        row.add(nvl(user.getEmail()));
        row.add(nvl(user.getPhone()));
        row.add(nvl(user.getSkills()));
        row.add(nvl(user.getCvFilePath()));
        row.add(user.getApprovalStatus().name());
        row.add(Boolean.toString(user.isEnabled()));
        row.add(nvl(user.getCreatedAt()));
        return row;
    }

    private User fromRow(List<String> row) {
        User user = new User();
        user.setUserId(cell(row, 0));
        user.setRole(Role.valueOf(cell(row, 1)));
        user.setAccountId(cell(row, 2));
        user.setPasswordHash(cell(row, 3));
        user.setFullName(cell(row, 4));
        user.setStudentId(cell(row, 5));
        user.setDepartment(cell(row, 6));
        user.setMajor(cell(row, 7));
        user.setEmail(cell(row, 8));
        user.setPhone(cell(row, 9));
        user.setSkills(cell(row, 10));
        user.setCvFilePath(cell(row, 11));
        user.setApprovalStatus(ApprovalStatus.valueOf(cell(row, 12)));
        user.setEnabled(Boolean.parseBoolean(cell(row, 13)));
        user.setCreatedAt(cell(row, 14));
        return user;
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
