package TA_Recruitment_software;

import TA_Recruitment_software.admin_system.foundation.AppException;
import TA_Recruitment_software.admin_system.model.Application;
import TA_Recruitment_software.admin_system.model.ApplicationStatus;
import TA_Recruitment_software.admin_system.model.Position;
import TA_Recruitment_software.admin_system.model.Role;
import TA_Recruitment_software.admin_system.model.TaWorkloadSummary;
import TA_Recruitment_software.admin_system.model.User;
import TA_Recruitment_software.auth.SessionContext;
import java.util.List;
import java.util.Scanner;

public class ConsoleMain {
    private final RecruitmentSystemContext context;
    private final Scanner scanner;

    public ConsoleMain() {
        this.context = new RecruitmentSystemContext();
        this.scanner = new Scanner(System.in);
    }

    public static void main(String[] args) {
        new ConsoleMain().run();
    }

    private void run() {
        System.out.println("TA Recruitment System (Text-File Version)");
        System.out.println("Default admin account: admin / Admin@123");

        while (true) {
            System.out.println("\n=== Main Menu ===");
            System.out.println("1. Register TA");
            System.out.println("2. Register MO");
            System.out.println("3. Login");
            System.out.println("4. Exit");
            String choice = read("Choose: ");
            try {
                if ("1".equals(choice)) {
                    registerTA();
                } else if ("2".equals(choice)) {
                    registerMO();
                } else if ("3".equals(choice)) {
                    login();
                } else if ("4".equals(choice)) {
                    System.out.println("Bye.");
                    return;
                } else {
                    System.out.println("Invalid choice.");
                }
            } catch (AppException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    private void registerTA() {
        User user = context.getAuthService().registerTA(
            read("Account ID: "),
            read("Password: "),
            read("Full Name: "),
            read("Student ID: "),
            read("Major: "),
            read("Email: "),
            read("Phone: ")
        );
        System.out.println("TA registered. Waiting for admin approval. User ID: " + user.getUserId());
    }

    private void registerMO() {
        User user = context.getAuthService().registerMO(
            read("Account ID: "),
            read("Password: "),
            read("Full Name: "),
            read("Department: "),
            read("Email: "),
            read("Phone: ")
        );
        System.out.println("MO registered. Waiting for admin approval. User ID: " + user.getUserId());
    }

    private void login() {
        String token = context.getAuthService().login(read("Account ID: "), read("Password: "));
        SessionContext session = context.getSessionManager().requireSession(token);
        Role role = session.getRole();
        if (role == Role.TA) {
            taMenu(token);
        } else if (role == Role.MO) {
            moMenu(token);
        } else {
            adminMenu(token);
        }
    }

    private void taMenu(String token) {
        while (true) {
            System.out.println("\n=== TA Menu ===");
            System.out.println("1. Update Profile");
            System.out.println("2. Upload/Replace CV");
            System.out.println("3. List Open Positions");
            System.out.println("4. Apply Position");
            System.out.println("5. My Applications");
            System.out.println("6. Logout");
            String choice = read("Choose: ");
            try {
                if ("1".equals(choice)) {
                    User user = context.getProfileService().updateProfile(
                        token,
                        read("Major: "),
                        read("Email: "),
                        read("Phone: "),
                        read("Skills: ")
                    );
                    System.out.println("Profile updated: " + user);
                } else if ("2".equals(choice)) {
                    User user = context.getProfileService().uploadCV(token, read("CV file path: "));
                    System.out.println("CV updated: " + user.getCvFilePath());
                } else if ("3".equals(choice)) {
                    List<Position> positions = context.getTaJobService().listAvailableJobs();
                    printList(positions);
                } else if ("4".equals(choice)) {
                    Application app = context.getTaJobService().applyForJob(token, read("Position ID: "));
                    System.out.println("Applied successfully: " + app);
                } else if ("5".equals(choice)) {
                    List<Application> apps = context.getTaJobService().listMyApplications(token);
                    printList(apps);
                } else if ("6".equals(choice)) {
                    context.getSessionManager().logout(token);
                    return;
                } else {
                    System.out.println("Invalid choice.");
                }
            } catch (AppException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    private void moMenu(String token) {
        while (true) {
            System.out.println("\n=== MO Menu ===");
            System.out.println("1. Publish Position");
            System.out.println("2. Update Position Deadline");
            System.out.println("3. Close Position");
            System.out.println("4. My Positions");
            System.out.println("5. Applications for One Position");
            System.out.println("6. All Applications of My Positions");
            System.out.println("7. Update Application Status");
            System.out.println("8. Logout");
            String choice = read("Choose: ");
            try {
                if ("1".equals(choice)) {
                    Position position = context.getMoPublishService().publishPosition(
                        token,
                        read("Job Title: "),
                        read("Job Type: "),
                        read("Description: "),
                        read("Requirements: "),
                        read("Interview Location: "),
                        read("Deadline (YYYY-MM-DD): "),
                        read("Semester (e.g. 2026-Spring): ")
                    );
                    System.out.println("Published: " + position);
                } else if ("2".equals(choice)) {
                    Position position = context.getMoPublishService()
                        .updateDeadline(token, read("Position ID: "), read("New deadline (YYYY-MM-DD): "));
                    System.out.println("Deadline updated: " + position);
                } else if ("3".equals(choice)) {
                    Position position = context.getMoPublishService().closePosition(token, read("Position ID: "));
                    System.out.println("Position closed: " + position);
                } else if ("4".equals(choice)) {
                    List<Position> positions = context.getMoPublishService().listMyPositions(token);
                    printList(positions);
                } else if ("5".equals(choice)) {
                    List<Application> apps = context.getMoReviewService()
                        .listApplicationsForPosition(token, read("Position ID: "));
                    printList(apps);
                } else if ("6".equals(choice)) {
                    List<Application> apps = context.getMoReviewService().listAllApplicationsOfMyPositions(token);
                    printList(apps);
                } else if ("7".equals(choice)) {
                    String rawStatus = read("New status(PENDING/APPROVED/REJECTED/HIRED): ");
                    ApplicationStatus status = ApplicationStatus.valueOf(rawStatus.trim().toUpperCase());
                    Application app = context.getMoReviewService()
                        .updateApplicationStatus(token, read("Application ID: "), status);
                    System.out.println("Status updated: " + app);
                } else if ("8".equals(choice)) {
                    context.getSessionManager().logout(token);
                    return;
                } else {
                    System.out.println("Invalid choice.");
                }
            } catch (IllegalArgumentException e) {
                System.out.println("Invalid status value.");
            } catch (AppException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    private void adminMenu(String token) {
        while (true) {
            System.out.println("\n=== Admin Menu ===");
            System.out.println("1. List Pending Users");
            System.out.println("2. Approve/Reject User");
            System.out.println("3. Enable/Disable User");
            System.out.println("4. Reset User Password");
            System.out.println("5. List All Users");
            System.out.println("6. Show TA Workload Summary");
            System.out.println("7. Edit User Information");
            System.out.println("8. Publish Position");
            System.out.println("9. List All Positions");
            System.out.println("10. Close Position");
            System.out.println("11. Logout");
            String choice = read("Choose: ");
            try {
                if ("1".equals(choice)) {
                    List<User> users = context.getAdminService().listPendingUsers(token);
                    printList(users);
                } else if ("2".equals(choice)) {
                    boolean approved = "Y".equalsIgnoreCase(read("Approve? (Y/N): "));
                    User user = context.getAdminService().approveUser(token, read("User ID: "), approved);
                    System.out.println("Updated: " + user);
                } else if ("3".equals(choice)) {
                    boolean enabled = "Y".equalsIgnoreCase(read("Enable? (Y/N): "));
                    User user = context.getAdminService().setUserEnabled(token, read("User ID: "), enabled);
                    System.out.println("Updated: " + user);
                } else if ("4".equals(choice)) {
                    User user = context.getAdminService()
                        .resetPassword(token, read("User ID: "), read("New password: "));
                    System.out.println("Password reset for: " + user.getAccountId());
                } else if ("5".equals(choice)) {
                    List<User> users = context.getAdminService().listAllUsers(token);
                    printList(users);
                } else if ("6".equals(choice)) {
                    List<TaWorkloadSummary> workloads = context.getAdminService().listTaWorkloadSummary(token);
                    printList(workloads);
                } else if ("7".equals(choice)) {
                    String userId = read("User ID: ");
                    List<User> allUsers = context.getAdminService().listAllUsers(token);
                    User targetUser = allUsers.stream().filter(u -> u.getUserId().equals(userId)).findFirst().orElse(null);
                    if (targetUser == null) {
                        System.out.println("User not found.");
                    } else {
                        System.out.println("Editing: " + targetUser.getAccountId());
                        String fullName = read("Full Name (press enter to skip): ").trim();
                        String email = read("Email (press enter to skip): ").trim();
                        String phone = read("Phone (press enter to skip): ").trim();
                        String major = read("Major/Department (press enter to skip): ").trim();
                        String skills = read("Skills (press enter to skip): ").trim();
                        
                        User updated = context.getAdminService().updateUserInfo(
                            token, userId, fullName.isEmpty() ? null : fullName,
                            email.isEmpty() ? null : email,
                            phone.isEmpty() ? null : phone,
                            major.isEmpty() ? null : major,
                            major.isEmpty() ? null : major,
                            skills.isEmpty() ? null : skills
                        );
                        System.out.println("Updated: " + updated);
                    }
                } else if ("8".equals(choice)) {
                    Position position = context.getMoPublishService().publishPosition(
                        token,
                        read("Job Title: "),
                        read("Job Type: "),
                        read("Job description: "),
                        read("Requirements: "),
                        read("Interview Location: "),
                        read("Deadline (YYYY-MM-DD): "),
                        read("Semester (e.g. 2026-Spring): ")
                    );
                    System.out.println("Published: " + position);
                } else if ("9".equals(choice)) {
                    List<Position> positions = context.getMoPublishService().listMyPositions(token);
                    printList(positions);
                } else if ("10".equals(choice)) {
                    Position position = context.getMoPublishService().closePosition(token, read("Position ID: "));
                    System.out.println("Position closed: " + position);
                } else if ("11".equals(choice)) {
                    context.getSessionManager().logout(token);
                    return;
                } else {
                    System.out.println("Invalid choice.");
                }
            } catch (AppException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    private String read(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine();
    }

    private void printList(List<?> list) {
        if (list == null || list.isEmpty()) {
            System.out.println("(empty)");
            return;
        }
        for (Object item : list) {
            System.out.println(item);
        }
    }
}
