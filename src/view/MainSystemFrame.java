package view;

import model.CurrentLoginUser;
import model.TAUser;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

// 系统主界面：登录成功后跳转，可给队友的资料/岗位模块添加按钮入口
public class MainSystemFrame extends JFrame {
    public MainSystemFrame() {
        // 界面基础设置
        setTitle("BUPT International School TA Recruitment System - Main Interface");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // 窗口居中
        setLayout(new BorderLayout());

        // 获取当前登录TA信息
        TAUser currentTA = CurrentLoginUser.getInstance().getCurrentTA();

        // 欢迎面板
        JPanel welcomePanel = new JPanel();
        welcomePanel.add(new JLabel("Welcome, " + currentTA.getName() + " (Student ID: " + currentTA.getStudentId() + ")"));
        add(welcomePanel, BorderLayout.NORTH);

        // 功能按钮面板（队友模块可在此添加按钮，如「我的资料」「查看岗位」）
        JPanel functionPanel = new JPanel();
        functionPanel.add(new JLabel("TA Function Area - To be extended by team members"));
        add(functionPanel, BorderLayout.CENTER);

        // 退出登录按钮
        JButton logoutBtn = new JButton("Logout");
        logoutBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // 清空登录状态
                CurrentLoginUser.getInstance().logout();
                // 关闭主界面，返回登录界面
                dispose();
                new TALoginFrame().setVisible(true);
                JOptionPane.showMessageDialog(null, "Logout successfully!");
            }
        });
        JPanel logoutPanel = new JPanel();
        logoutPanel.add(logoutBtn);
        add(logoutPanel, BorderLayout.SOUTH);

        setVisible(true);
    }
}