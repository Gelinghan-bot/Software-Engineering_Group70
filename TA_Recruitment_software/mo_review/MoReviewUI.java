package TA_Recruitment_software.mo_review;

import TA_Recruitment_software.RecruitmentSystemContext;
import TA_Recruitment_software.admin_system.foundation.AppException;
import TA_Recruitment_software.admin_system.foundation.UIStyle;
import TA_Recruitment_software.admin_system.model.Application;
import TA_Recruitment_software.admin_system.model.ApplicationStatus;
import TA_Recruitment_software.admin_system.model.Position;
import TA_Recruitment_software.admin_system.model.User;
import java.awt.*;
import java.util.ArrayList;
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
            reviewPanel.setBackground(UIStyle.BG_PAGE);
            reviewPanel.setBorder(UIStyle.pagePadding());

            // --- Top: Sort controls + Note search ---
            JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 5));
            topPanel.setBackground(UIStyle.BG_PAGE);
            topPanel.add(new JLabel("Sort by:"));
            JButton sortTimeNewBtn = UIStyle.createSecondaryButton("Time (Newest)");
            JButton sortTimeOldBtn = UIStyle.createSecondaryButton("Time (Oldest)");
            JButton sortMajorBtn = UIStyle.createSecondaryButton("Major");
            JButton resetFilterBtn = UIStyle.createSecondaryButton("Show All");
            topPanel.add(sortTimeNewBtn);
            topPanel.add(sortTimeOldBtn);
            topPanel.add(sortMajorBtn);
            topPanel.add(new JSeparator(SwingConstants.VERTICAL));
            topPanel.add(new JLabel("Search Notes:"));
            JTextField noteSearchField = UIStyle.createTextField(12);
            JButton searchNoteBtn = UIStyle.createAccentButton("Search");
            topPanel.add(noteSearchField);
            topPanel.add(searchNoteBtn);
            topPanel.add(resetFilterBtn);
            reviewPanel.add(topPanel, BorderLayout.NORTH);

            // --- Center: Application table ---
            String[] columns = {"App ID", "Job Title", "TA Name", "Student ID", "Major", "CV", "Status", "Submit Time"};
            DefaultTableModel model = new DefaultTableModel(columns, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };

            // displayedApps tracks the currently visible subset (for row-index lookups)
            final List<Application> displayedApps = new ArrayList<>(applications);
            populateTable(model, displayedApps, service);

            JTable table = UIStyle.createStyledTable(model);
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

            JScrollPane scrollPane = UIStyle.wrapTableInScrollPane(table);
            reviewPanel.add(scrollPane, BorderLayout.CENTER);

            // --- Bottom: Action buttons ---
            JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 10));
            btnPanel.setBackground(UIStyle.BG_PAGE);
            JButton viewDetailBtn = UIStyle.createPrimaryButton("View Details");
            JButton updateStatusBtn = UIStyle.createAccentButton("Update Status");
            JButton viewHistoryBtn = UIStyle.createSecondaryButton("View Status History");
            JButton notesBtn = UIStyle.createSecondaryButton("Interview Notes");
            JButton inviteBtn = UIStyle.createSecondaryButton("Invite Candidates");
            JButton aiCompareBtn = UIStyle.createAccentButton("AI Compare Candidates");
            JButton closeBtn = UIStyle.createSecondaryButton("Back");

            btnPanel.add(viewDetailBtn);
            btnPanel.add(updateStatusBtn);
            btnPanel.add(viewHistoryBtn);
            btnPanel.add(notesBtn);
            btnPanel.add(inviteBtn);
            btnPanel.add(aiCompareBtn);
            btnPanel.add(closeBtn);
            reviewPanel.add(btnPanel, BorderLayout.SOUTH);

            // --- Sort button actions ---
            sortTimeNewBtn.addActionListener(e -> {
                service.sortBySubmissionTime(applications, false);
                displayedApps.clear();
                displayedApps.addAll(applications);
                refreshTable(model, displayedApps, service);
            });
            sortTimeOldBtn.addActionListener(e -> {
                service.sortBySubmissionTime(applications, true);
                displayedApps.clear();
                displayedApps.addAll(applications);
                refreshTable(model, displayedApps, service);
            });
            sortMajorBtn.addActionListener(e -> {
                service.sortByMajor(applications);
                displayedApps.clear();
                displayedApps.addAll(applications);
                refreshTable(model, displayedApps, service);
            });

            // --- Note keyword search ---
            searchNoteBtn.addActionListener(e -> {
                String keyword = noteSearchField.getText().trim();
                if (keyword.isEmpty()) {
                    JOptionPane.showMessageDialog(parent, "Please enter a keyword to search.");
                    return;
                }
                List<String> matchedIds = context.getInterviewNoteService().searchByKeyword(token, keyword);
                displayedApps.clear();
                for (Application app : applications) {
                    if (matchedIds.contains(app.getApplicationId())) {
                        displayedApps.add(app);
                    }
                }
                refreshTable(model, displayedApps, service);
                if (displayedApps.isEmpty()) {
                    JOptionPane.showMessageDialog(parent, "No notes found containing: " + keyword);
                }
            });
            resetFilterBtn.addActionListener(e -> {
                noteSearchField.setText("");
                displayedApps.clear();
                displayedApps.addAll(applications);
                refreshTable(model, displayedApps, service);
            });

            // --- View Details action ---
            viewDetailBtn.addActionListener(e -> {
                int row = table.getSelectedRow();
                if (row < 0) {
                    JOptionPane.showMessageDialog(parent, "Please select an application first.");
                    return;
                }
                Application app = displayedApps.get(row);
                showApplicationDetail(parent, service, app);
            });

            // --- Update Status action ---
            updateStatusBtn.addActionListener(e -> {
                int row = table.getSelectedRow();
                if (row < 0) {
                    JOptionPane.showMessageDialog(parent, "Please select an application first.");
                    return;
                }
                Application app = displayedApps.get(row);
                handleStatusUpdate(parent, context, token, app, applications, displayedApps, model, service);
            });

            // --- View History action ---
            viewHistoryBtn.addActionListener(e -> {
                int row = table.getSelectedRow();
                if (row < 0) {
                    JOptionPane.showMessageDialog(parent, "Please select an application first.");
                    return;
                }
                Application app = displayedApps.get(row);
                showStatusHistory(parent, app);
            });

            // --- Interview Notes action ---
            notesBtn.addActionListener(e -> {
                int row = table.getSelectedRow();
                if (row < 0) {
                    JOptionPane.showMessageDialog(parent, "Please select an application first.");
                    return;
                }
                Application app = displayedApps.get(row);
                showInterviewNotesDialog(parent, context, token, app);
            });

            // --- Invite Candidates action ---
            inviteBtn.addActionListener(e -> {
                int row = table.getSelectedRow();
                if (row < 0) {
                    JOptionPane.showMessageDialog(parent, "Please select an application row first to determine the position.");
                    return;
                }
                Application app = displayedApps.get(row);
                showInviteCandidatesDialog(parent, context, token, app.getPositionId(), service);
            });

            // --- AI Compare action ---
            aiCompareBtn.addActionListener(e -> {
                int[] rows = table.getSelectedRows();
                if (rows.length < 2) {
                    JOptionPane.showMessageDialog(parent, "Please select at least two candidates to compare (use Ctrl/Cmd + click).", "Info", JOptionPane.INFORMATION_MESSAGE);
                    return;
                }
                List<Application> selectedApps = new ArrayList<>();
                for (int row : rows) {
                    selectedApps.add(displayedApps.get(row));
                }
                JDialog loadingDialog = new JDialog(parent, "AI Comparison in Progress...", true);
                JPanel p = new JPanel(new BorderLayout(10, 10));
                p.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
                p.add(new JLabel("AI is analysing CVs and position requirements, please wait..."), BorderLayout.NORTH);
                JProgressBar progressBar = new JProgressBar();
                progressBar.setIndeterminate(true);
                p.add(progressBar, BorderLayout.CENTER);
                loadingDialog.add(p);
                loadingDialog.pack();
                loadingDialog.setLocationRelativeTo(parent);
                loadingDialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

                SwingWorker<String, Void> worker = new SwingWorker<String, Void>() {
                    @Override
                    protected String doInBackground() throws Exception {
                        return service.simulateAIComparison(selectedApps);
                    }

                    @Override
                    protected void done() {
                        loadingDialog.dispose();
                        try {
                            String result = get();
                            JTextArea textArea = new JTextArea(result);
                            textArea.setEditable(false);
                            textArea.setFont(new Font("Monospaced", Font.PLAIN, 13));
                            textArea.setLineWrap(true);
                            textArea.setWrapStyleWord(true);
                            textArea.setCaretPosition(0);
                            JScrollPane resultScrollPane = new JScrollPane(textArea);
                            resultScrollPane.setPreferredSize(new Dimension(650, 500));
                            JOptionPane.showMessageDialog(parent, resultScrollPane, "AI Candidate Comparison Report", JOptionPane.PLAIN_MESSAGE);
                        } catch (Exception ex) {
                            String errorMsg = ex.getMessage();
                            if (ex.getCause() != null) errorMsg = ex.getCause().getMessage();
                            JOptionPane.showMessageDialog(parent, "Error during AI comparison: " + errorMsg, "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                };
                worker.execute();
                loadingDialog.setVisible(true);
            });

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

        StringBuilder sb = new StringBuilder();
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            sb.append("=== Applicant Profile ===\n\n");
            sb.append("Full Name: ").append(nvl(user.getFullName())).append("\n\n");
            sb.append("Student ID: ").append(nvl(user.getStudentId())).append("\n\n");
            sb.append("Major: ").append(nvl(user.getMajor())).append("\n\n");
            sb.append("Email: ").append(nvl(user.getEmail())).append("\n\n");
            sb.append("Phone: ").append(nvl(user.getPhone())).append("\n\n");
            sb.append("Skills: ").append(nvl(user.getSkills())).append("\n\n");
            sb.append("CV File: ").append(nvl(user.getCvFilePath())).append("\n");
        } else {
            sb.append("Applicant info not available.\n");
        }

        JTextArea textArea = new JTextArea(sb.toString());
        textArea.setEditable(false);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 15));
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setCaretPosition(0);

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getHorizontalScrollBar().setUnitIncrement(16);
        scrollPane.setPreferredSize(new Dimension(500, 350));
        
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.add(scrollPane, BorderLayout.CENTER);
        
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton openCvBtn = new JButton("Open CV file");
        
        if (userOpt.isPresent() && userOpt.get().getCvFilePath() != null && !userOpt.get().getCvFilePath().trim().isEmpty()) {
            openCvBtn.addActionListener(e -> {
                String stored = userOpt.get().getCvFilePath();
                java.nio.file.Path path = java.nio.file.Paths.get(stored.trim());
                if (!java.nio.file.Files.isRegularFile(path)) {
                    JOptionPane.showMessageDialog(panel, "CV file not found at:\n" + path.toAbsolutePath(), "Missing file", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                if (!java.awt.Desktop.isDesktopSupported()) {
                    JOptionPane.showMessageDialog(panel, "Desktop API not supported on this environment.");
                    return;
                }
                try {
                    java.awt.Desktop.getDesktop().open(path.toFile());
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(panel, "Could not open file: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            });
        } else {
            openCvBtn.setEnabled(false);
        }
        
        btnPanel.add(openCvBtn);
        panel.add(btnPanel, BorderLayout.SOUTH);

        JOptionPane.showMessageDialog(parent, panel, "Applicant Details", JOptionPane.PLAIN_MESSAGE);
    }

    private static void handleStatusUpdate(Component parent, RecruitmentSystemContext context,
                                            String token, Application app,
                                            List<Application> allApps, List<Application> displayedApps,
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
        if (selected == null) return;

        // Confirm before applying change
        int confirm = JOptionPane.showConfirmDialog(
            parent,
            "Change status from " + app.getStatus().name() + " to " + selected.name() + "?",
            "Confirm Status Change",
            JOptionPane.YES_NO_OPTION
        );
        if (confirm != JOptionPane.YES_OPTION) return;

        // Optional note for all transitions
        String note = JOptionPane.showInputDialog(parent,
            "Add a note for this status change (optional):",
            "Status Note",
            JOptionPane.PLAIN_MESSAGE);
        if (note != null && note.trim().isEmpty()) note = null;

        try {
            Application updated = service.updateApplicationStatus(token, app.getApplicationId(), selected, note);
            replaceInList(allApps, app, updated);
            replaceInList(displayedApps, app, updated);
            refreshTable(model, displayedApps, service);
            JOptionPane.showMessageDialog(parent,
                "Application status updated to " + selected.name(),
                "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (AppException ex) {
            JOptionPane.showMessageDialog(parent, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static void replaceInList(List<Application> list, Application old, Application updated) {
        int idx = list.indexOf(old);
        if (idx >= 0) list.set(idx, updated);
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

    private static void showInterviewNotesDialog(Component parent, RecruitmentSystemContext context,
                                                  String token, Application app) {
        Optional<InterviewNote> existing = context.getInterviewNoteService().findNote(token, app.getApplicationId());

        JTextArea noteArea = new JTextArea(12, 40);
        noteArea.setLineWrap(true);
        noteArea.setWrapStyleWord(true);
        noteArea.setFont(new Font("Monospaced", Font.PLAIN, 13));

        JLabel timestampLabel = new JLabel(" ");
        timestampLabel.setFont(new Font("Arial", Font.ITALIC, 11));
        timestampLabel.setForeground(Color.GRAY);

        if (existing.isPresent()) {
            noteArea.setText(existing.get().getNoteContent());
            timestampLabel.setText("Last updated: " + existing.get().getLastUpdatedAt());
        } else {
            timestampLabel.setText("No note yet.");
        }

        JScrollPane noteScroll = new JScrollPane(noteArea);

        JPanel dialogPanel = new JPanel(new BorderLayout(5, 5));
        dialogPanel.add(new JLabel("Private interview notes for: " + app.getApplicationId()), BorderLayout.NORTH);
        dialogPanel.add(noteScroll, BorderLayout.CENTER);
        dialogPanel.add(timestampLabel, BorderLayout.SOUTH);
        dialogPanel.setPreferredSize(new Dimension(500, 300));

        int result = JOptionPane.showConfirmDialog(parent, dialogPanel,
            "Interview Notes (Private)", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            try {
                InterviewNote saved = context.getInterviewNoteService()
                    .saveOrUpdate(token, app.getApplicationId(), noteArea.getText());
                JOptionPane.showMessageDialog(parent,
                    "Note saved at " + saved.getLastUpdatedAt(), "Saved", JOptionPane.INFORMATION_MESSAGE);
            } catch (AppException ex) {
                JOptionPane.showMessageDialog(parent, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private static void showInviteCandidatesDialog(Component parent, RecruitmentSystemContext context,
                                                    String token, String positionId, MOReviewService service) {
        List<MoInvitationService.EligibleTA> eligible;
        try {
            eligible = context.getMoInvitationService().listEligibleTAs(token, positionId);
        } catch (AppException ex) {
            JOptionPane.showMessageDialog(parent, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (eligible.isEmpty()) {
            JOptionPane.showMessageDialog(parent, "No eligible TAs found (all TAs may have reached the 3-course limit).");
            return;
        }

        Optional<Position> posOpt = service.getPositionInfo(positionId);
        String jobTitle = posOpt.map(Position::getJobTitle).orElse(positionId);

        String[] columnNames = {"Select", "Name", "Student ID", "Major", "Hired This Semester"};
        Object[][] data = new Object[eligible.size()][5];
        for (int i = 0; i < eligible.size(); i++) {
            MoInvitationService.EligibleTA et = eligible.get(i);
            data[i][0] = Boolean.FALSE;
            data[i][1] = et.user.getFullName();
            data[i][2] = et.user.getStudentId();
            data[i][3] = et.user.getMajor() != null ? et.user.getMajor() : "";
            data[i][4] = et.hiredCount + " / 3";
        }

        DefaultTableModel tableModel = new DefaultTableModel(data, columnNames) {
            @Override public Class<?> getColumnClass(int col) {
                return col == 0 ? Boolean.class : String.class;
            }
            @Override public boolean isCellEditable(int row, int col) { return col == 0; }
        };
        JTable taTable = new JTable(tableModel);
        taTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        taTable.getColumnModel().getColumn(1).setPreferredWidth(120);
        taTable.getColumnModel().getColumn(2).setPreferredWidth(100);
        taTable.getColumnModel().getColumn(3).setPreferredWidth(80);
        taTable.getColumnModel().getColumn(4).setPreferredWidth(100);

        JScrollPane tableScroll = new JScrollPane(taTable);
        tableScroll.setPreferredSize(new Dimension(500, 200));

        JTextArea msgArea = new JTextArea(4, 40);
        msgArea.setLineWrap(true);
        msgArea.setWrapStyleWord(true);
        msgArea.setFont(new Font("Arial", Font.PLAIN, 13));
        JScrollPane msgScroll = new JScrollPane(msgArea);

        JPanel dialogPanel = new JPanel(new BorderLayout(5, 8));
        dialogPanel.add(new JLabel("Invite TAs to apply for: " + jobTitle + " (" + positionId + ")"), BorderLayout.NORTH);
        dialogPanel.add(tableScroll, BorderLayout.CENTER);
        JPanel bottomPanel = new JPanel(new BorderLayout(3, 3));
        bottomPanel.add(new JLabel("Invitation message (optional):"), BorderLayout.NORTH);
        bottomPanel.add(msgScroll, BorderLayout.CENTER);
        dialogPanel.add(bottomPanel, BorderLayout.SOUTH);

        int result = JOptionPane.showConfirmDialog(parent, dialogPanel,
            "Invite Candidates", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result != JOptionPane.OK_OPTION) return;

        String message = msgArea.getText().trim();
        int sentCount = 0;
        List<String> errors = new ArrayList<>();
        for (int i = 0; i < eligible.size(); i++) {
            if (Boolean.TRUE.equals(tableModel.getValueAt(i, 0))) {
                try {
                    context.getMoInvitationService().sendInvitation(
                        token, eligible.get(i).user.getUserId(), positionId, message);
                    sentCount++;
                } catch (AppException ex) {
                    errors.add(eligible.get(i).user.getFullName() + ": " + ex.getMessage());
                }
            }
        }

        if (sentCount == 0 && errors.isEmpty()) {
            JOptionPane.showMessageDialog(parent, "No candidates were selected.");
        } else if (errors.isEmpty()) {
            JOptionPane.showMessageDialog(parent, "Invitation sent to " + sentCount + " candidate(s).", "Success", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(parent,
                "Sent: " + sentCount + "\nFailed:\n" + String.join("\n", errors),
                "Partial Success", JOptionPane.WARNING_MESSAGE);
        }
    }

    private static String nvl(String value) {
        return value == null || value.isEmpty() ? "N/A" : value;
    }
}
