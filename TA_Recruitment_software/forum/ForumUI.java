package TA_Recruitment_software.forum;

import TA_Recruitment_software.RecruitmentSystemContext;
import java.awt.*;
import javax.swing.*;

public class ForumUI {

    public static void showForum(JFrame parent, RecruitmentSystemContext context, String token) {
        Container contentPane = parent.getContentPane();
        BorderLayout layout = (BorderLayout) contentPane.getLayout();
        Component centerComponent = layout.getLayoutComponent(BorderLayout.CENTER);
        
        if (centerComponent instanceof ForumPanel) {
            return; // Already showing this panel
        }
        
        ForumPanel forumPanel = new ForumPanel(parent, context, token, centerComponent);
        
        if (centerComponent != null) {
            contentPane.remove(centerComponent);
        }
        contentPane.add(forumPanel, BorderLayout.CENTER);
        parent.revalidate();
        parent.repaint();
    }
}