package TA_Recruitment_software.mo_review;

import TA_Recruitment_software.RecruitmentSystemContext;
import TA_Recruitment_software.admin_system.foundation.AppException;
import TA_Recruitment_software.admin_system.model.Application;
import TA_Recruitment_software.admin_system.model.ApplicationStatus;
import TA_Recruitment_software.admin_system.model.Position;
import TA_Recruitment_software.admin_system.model.User;
import java.awt.*;
import java.util.List;
import java.util.Optional;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class MoReviewUI {

    public static void showReviewDialog(JFrame parent, RecruitmentSystemContext context, String token) {
        if (token == null) {
            JOptionPane.showMessageDialog(parent, "Please login as MO or ADMIN.");
            return;
        }

        try {
            TA_Recruitment_software.admin_system.model.Role role = context.getSessionManager().requireSession(token).getRole();
            if (role != TA_Recruitment_software.admin_system.model.Role.MO && role != TA_Recruitment_software.admin_system.model.Role.ADMIN) {
                throw new AppException("Permission denied. Only MO and ADMIN can review applications.");
            }
            MOReviewService service = context.getMoReviewService();
            List<Application> applications = service.listAllApplicationsOfMyPositions(token);
            if (applications.isEmpty()) {
                JOptionPane.showMessageDialog(parent, "No applications received yet.");
                return;
            }

            service.sortBySubmissionTime(applications, false);

            Container contentPane = parent.getContentPane();
            BorderLayout layout = (BorderLayout) contentPane.getLayout();
            Component previousCenter = layout.getLayoutComponent(BorderLayout.CENTER);

            JPanel reviewPanel = new JPanel(new BorderLayout(5, 5));

            // --- Top: Sort controls ---
            JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            topPanel.add(new JLabel("Sort by:"));
            JButton sortTimeNewBtn = new JButton("Time (Newest)");
            JButton sortTimeOldBtn = new JButton("Time (Oldest)");
            JButton sortMajorBtn = new JButton("Major");
            topPanel.add(sortTimeNewBtn);
            topPanel.add(sortTimeOldBtn);
            topPanel.add(sortMajorBtn);
            reviewPanel.add(topPanel, BorderLayout.NORTH);

            // --- Center: Application table ---
            String[] columns = {"App ID", "Job Title", "TA Name", "Student ID", "Major", "CV", "Status", "Submit Time"};
            DefaultTableModel model = new DefaultTableModel(columns, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };

            populateTable(model, applications, service);

            JTable table = new JTable(model);
            table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
            table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
            table.getColumnModel().getColumn(0).setPreferredWidth(120);
            table.getColumnModel().getColumn(1).setPreferredWidth(130);
            table.getColumnModel().getColumn(2).setPreferredWidth(100);
            table.getColumnModel().getColumn(3).setPreferredWidth(80);
            table.getColumnModel().getColumn(4).setPreferredWidth(100);
            table.getColumnModel().getColumn(5).setPreferredWidth(60);
            table.getColumnModel().getColumn(6).setPreferredWidth(100);
            table.getColumnModel().getColumn(7).setPreferredWidth(140);

            JScrollPane scrollPane = new JScrollPane(table);
            scrollPane.getVerticalScrollBar().setUnitIncrement(16);
            scrollPane.getHorizontalScrollBar().setUnitIncrement(16);
            reviewPanel.add(scrollPane, BorderLayout.CENTER);

            // --- Bottom: Action buttons ---
            JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
            JButton viewDetailBtn = new JButton("View Details");
            JButton updateStatusBtn = new JButton("Update Status");
            JButton viewHistoryBtn = new JButton("View Status History");
            JButton aiCompareBtn = new JButton("✨ AI Compare Candidates");
            JButton closeBtn = new JButton("Close");

            btnPanel.add(viewDetailBtn);
            btnPanel.add(updateStatusBtn);
            btnPanel.add(viewHistoryBtn);
            btnPanel.add(aiCompareBtn);
            btnPanel.add(closeBtn);
            reviewPanel.add(btnPanel, BorderLayout.SOUTH);

            // --- Sort button actions ---
            sortTimeNewBtn.addActionListener(e -> {
                service.sortBySubmissionTime(applications, false);
                refreshTable(model, applications, service);
            });
            sortTimeOldBtn.addActionListener(e -> {
                service.sortBySubmissionTime(applications, true);
                refreshTable(model, applications, service);
            });
            sortMajorBtn.addActionListener(e -> {
                service.sortByMajor(applications);
                refreshTable(model, applications, service);
            });

            // --- View Details action ---
            viewDetailBtn.addActionListener(e -> {
                int row = table.getSelectedRow();
                if (row < 0) {
                    JOptionPane.showMessageDialog(parent, "Please select an application first.");
                    return;
                }
                Application app = applications.get(row);
                showApplicationDetail(parent, service, app);
            });

            // --- Update Status action ---
            updateStatusBtn.addActionListener(e -> {
                int row = table.getSelectedRow();
                if (row < 0) {
                    JOptionPane.showMessageDialog(parent, "Please select an application first.");
                    return;
                }
                Application app = applications.get(row);
                handleStatusUpdate(parent, context, token, app, applications, model, service);
            });

            // --- View History action ---
            viewHistoryBtn.addActionListener(e -> {
                int row = table.getSelectedRow();
                if (row < 0) {
                    JOptionPane.showMessageDialog(parent, "Please select an application first.");
                    return;
                }
                Application app = applications.get(row);
                showStatusHistory(parent, app);
            });

            // --- AI Compare action ---
            aiCompareBtn.addActionListener(e -> {
                int[] rows = table.getSelectedRows();
                if (rows.length < 2) {
                    JOptionPane.showMessageDialog(parent, "Please select at least two candidates to compare (use Ctrl/Cmd + click).", "Info", JOptionPane.INFORMATION_MESSAGE);
                    return;
                }
                
                List<Application> selectedApps = new java.util.ArrayList<>();
                for (int row : rows) {
                    selectedApps.add(applications.get(row));
                }
                
                // 1. 创建一个加载提示弹窗
                JDialog loadingDialog = new JDialog(parent, "AI 助手工作正在努力中...", true);
                JPanel p = new JPanel(new BorderLayout(10, 10));
                p.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
                p.add(new JLabel("✨ AI 小助理正在深度阅读简历与岗位信息，请稍候..."), BorderLayout.NORTH);
                JProgressBar progressBar = new JProgressBar();
                progressBar.setIndeterminate(true); // 让进度条来回滚动
                p.add(progressBar, BorderLayout.CENTER);
                loadingDialog.add(p);
                loadingDialog.pack();
                loadingDialog.setLocationRelativeTo(parent);
                loadingDialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE); // 防止用户手动关闭打断

                // 2. 利用 SwingWorker 开启后台线程，不卡死界面
                SwingWorker<String, Void> worker = new SwingWorker<String, Void>() {
                    @Override
                    protected String doInBackground() throws Exception {
                        return service.simulateAIComparison(selectedApps);
                    }

                    @Override
                    protected void done() {
                        loadingDialog.dispose(); // 请求完成，关掉加载动画
                        try {
                            String result = get(); // 获取 doInBackground 的返回值
                            
                            JTextArea textArea = new JTextArea(result);
                            textArea.setEditable(false);
                            textArea.setFont(new Font("Monospaced", Font.PLAIN, 13));
                            textArea.setLineWrap(true);
                            textArea.setWrapStyleWord(true);
                            textArea.setCaretPosition(0);

                            JScrollPane resultScrollPane = new JScrollPane(textArea);
                            resultScrollPane.setPreferredSize(new Dimension(650, 500));

                            JOptionPane.showMessageDialog(parent, resultScrollPane, "✨ AI Candidate Comparison Report", JOptionPane.PLAIN_MESSAGE);
                        } catch (Exception ex) {
                            String errorMsg = ex.getMessage();
                            if (ex.getCause() != null) {
                                errorMsg = ex.getCause().getMessage();
                            }
                            JOptionPane.showMessageDialog(parent, "Error during AI comparison: " + errorMsg, "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                };
                
                // 3. 执行后台任务，并展示加载弹窗（由于弹窗是模态的，它会阻塞在这里等待）
                worker.execute();
                loadingDialog.setVisible(true);
            });

            closeBtn.setText("Back");
            closeBtn.addActionListener(e -> {
                contentPane.remove(reviewPanel);
                if (previousCenter != null) {
                    contentPane.add(previousCenter, BorderLayout.CENTER);
                }
                parent.revalidate();
                parent.repaint();
            });

            if (previousCenter != null) {
                contentPane.remove(previousCenter);
            }
            contentPane.add(reviewPanel, BorderLayout.CENTER);
            parent.revalidate();
            parent.repaint();

        } catch (AppException ex) {
            JOptionPane.showMessageDialog(parent, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static void populateTable(DefaultTableModel model, List<Application> apps, MOReviewService service) {
        model.setRowCount(0);
        for (Application app : apps) {
            Optional<User> userOpt = service.getApplicantInfo(app.getApplicantUserId());
            Optional<Position> posOpt = service.getPositionInfo(app.getPositionId());

            String jobTitle = posOpt.map(Position::getJobTitle).orElse("N/A");
            String taName = userOpt.map(User::getFullName).orElse("N/A");
            String studentId = userOpt.map(User::getStudentId).orElse("N/A");
            String major = userOpt.map(u -> u.getMajor() != null ? u.getMajor() : "").orElse("");
            String cv = userOpt.map(u -> u.getCvFilePath() != null && !u.getCvFilePath().isEmpty() ? "Yes" : "No").orElse("No");

            model.addRow(new Object[]{
                app.getApplicationId(),
                jobTitle,
                taName,
                studentId,
                major,
                cv,
                app.getStatus().name(),
                app.getSubmissionTime()
            });
        }
    }

    private static void refreshTable(DefaultTableModel model, List<Application> apps, MOReviewService service) {
        populateTable(model, apps, service);
    }

    private static void showApplicationDetail(Component parent, MOReviewService service, Application app) {
        Optional<User> userOpt = service.getApplicantInfo(app.getApplicantUserId());
        Optional<Position> posOpt = service.getPositionInfo(app.getPositionId());

        StringBuilder sb = new StringBuilder();
        sb.append("=== Application Details ===\n\n");
        sb.append("Application ID: ").append(app.getApplicationId()).append("\n");
        sb.append("Status: ").append(app.getStatus().name()).append("\n");
        sb.append("Submit Time: ").append(nvl(app.getSubmissionTime())).append("\n");
        sb.append("Last Updated: ").append(nvl(app.getUpdatedTime())).append("\n\n");

        if (posOpt.isPresent()) {
            Position pos = posOpt.get();
            sb.append("=== Position Info ===\n");
            sb.append("Job Title: ").append(nvl(pos.getJobTitle())).append("\n");
            sb.append("Responsible MO: ").append(nvl(pos.getResponsibleMO())).append("\n");
            sb.append("Description: ").append(nvl(pos.getJobDescription())).append("\n");
            sb.append("Requirements: ").append(nvl(pos.getRequirements())).append("\n");
            sb.append("Job Type: ").append(nvl(pos.getJobType())).append("\n");
            sb.append("Deadline: ").append(nvl(pos.getDeadline())).append("\n");
            sb.append("Status: ").append(pos.getStatus()).append("\n\n");
        }

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            sb.append("=== Applicant Profile ===\n");
            sb.append("Full Name: ").append(nvl(user.getFullName())).append("\n");
            sb.append("Student ID: ").append(nvl(user.getStudentId())).append("\n");
            sb.append("Major: ").append(nvl(user.getMajor())).append("\n");
            sb.append("Email: ").append(nvl(user.getEmail())).append("\n");
            sb.append("Phone: ").append(nvl(user.getPhone())).append("\n");
            sb.append("Skills: ").append(nvl(user.getSkills())).append("\n");
            sb.append("CV File: ").append(nvl(user.getCvFilePath())).append("\n");
        } else {
            sb.append("Applicant info not available.\n");
        }

        if (app.getStatusNote() != null && !app.getStatusNote().isEmpty()) {
            sb.append("\n=== Latest Note ===\n");
            sb.append(app.getStatusNote()).append("\n");
        }

        JTextArea textArea = new JTextArea(sb.toString());
        textArea.setEditable(false);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 13));
        textArea.setCaretPosition(0);

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getHorizontalScrollBar().setUnitIncrement(16);
        scrollPane.setPreferredSize(new Dimension(550, 450));

        JOptionPane.showMessageDialog(parent, scrollPane, "Application Details", JOptionPane.PLAIN_MESSAGE);
    }

    private static void handleStatusUpdate(Component parent, RecruitmentSystemContext context,
                                            String token, Application app,
                                            List<Application> applications,
                                            DefaultTableModel model, MOReviewService service) {
        List<ApplicationStatus> validStatuses = service.getValidNextStatuses(app.getStatus());
        if (validStatuses.isEmpty()) {
            JOptionPane.showMessageDialog(parent,
                "Application status is " + app.getStatus().name() + ". No further status transitions are allowed.",
                "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        ApplicationStatus[] options = validStatuses.toArray(new ApplicationStatus[0]);
        ApplicationStatus selected = (ApplicationStatus) JOptionPane.showInputDialog(
            parent,
            "Current status: " + app.getStatus().name() + "\nSelect new status:",
            "Update Application Status",
            JOptionPane.QUESTION_MESSAGE,
            null,
            options,
            options[0]
        );

        if (selected == null) {
            return;
        }

        String note = null;
        if (selected == ApplicationStatus.REJECTED || selected == ApplicationStatus.OFFERED) {
            note = JOptionPane.showInputDialog(parent,
                "Add a note for " + selected.name() + " (optional):",
                "Status Note",
                JOptionPane.PLAIN_MESSAGE);
        }

        try {
            Application updated = service.updateApplicationStatus(token, app.getApplicationId(), selected, note);

            int idx = applications.indexOf(app);
            if (idx >= 0) {
                applications.set(idx, updated);
            }
            refreshTable(model, applications, service);

            JOptionPane.showMessageDialog(parent,
                "Application status updated to " + selected.name(),
                "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (AppException ex) {
            JOptionPane.showMessageDialog(parent, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static void showStatusHistory(Component parent, Application app) {
        String history = app.getStatusHistory();
        if (history == null || history.isEmpty()) {
            JOptionPane.showMessageDialog(parent, "No status change history.", "Status History", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Status Change History for Application: ").append(app.getApplicationId()).append("\n\n");

        String[] entries = history.split(" ;; ");
        for (int i = 0; i < entries.length; i++) {
            sb.append(i + 1).append(". ").append(entries[i]).append("\n");
        }

        JTextArea textArea = new JTextArea(sb.toString());
        textArea.setEditable(false);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 13));
        textArea.setCaretPosition(0);

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getHorizontalScrollBar().setUnitIncrement(16);
        scrollPane.setPreferredSize(new Dimension(500, 300));

        JOptionPane.showMessageDialog(parent, scrollPane, "Status History", JOptionPane.PLAIN_MESSAGE);
    }

    private static String nvl(String value) {
        return value == null || value.isEmpty() ? "N/A" : value;
    }
}
