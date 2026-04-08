package view;

import model.CurrentLoginUser;
import model.TAUser;
import util.JsonFileUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

// TA登录窗口：程序默认启动窗口
public class TALoginFrame extends JFrame {
    // 界面组件
    private JTextField studentIdField;
    private JPasswordField passwordField;
    private JButton loginBtn;
    private JButton registerBtn;

    public TALoginFrame() {
        // 界面基础设置
        setTitle("TA Recruitment System - Login");
        setSize(400, 250);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new GridLayout(4, 2, 10, 10));
        setResizable(false);

        // 初始化组件
        initComponents();
        // 绑定事件
        bindEvents();

        setVisible(true);
    }

    // 初始化组件
    private void initComponents() {
        add(new JLabel("Student ID:"));
        studentIdField = new JTextField();
        add(studentIdField);

        add(new JLabel("Password:"));
        passwordField = new JPasswordField();
        add(passwordField);

        loginBtn = new JButton("Login");
        add(loginBtn);

        registerBtn = new JButton("Register Now");
        add(registerBtn);

        // 组件边距美化
        for (Component c : getContentPane().getComponents()) {
            ((JComponent) c).setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        }
    }

    // 绑定事件
    private void bindEvents() {
        // 登录按钮事件
        loginBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // 获取输入值
                String studentId = studentIdField.getText().trim();
                String password = new String(passwordField.getPassword()).trim();

                // 非空校验
                if (studentId.isEmpty() || password.isEmpty()) {
                    JOptionPane.showMessageDialog(null, "Student ID and Password cannot be empty!", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // 调用工具类校验账号密码
                TAUser ta = JsonFileUtil.getTAByStudentIdAndPwd(studentId, password);
                if (ta != null) {
                    // 登录成功：设置全局登录状态
                    CurrentLoginUser.getInstance().login(ta);
                    JOptionPane.showMessageDialog(null, "Login successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    // 关闭登录窗口，打开主界面
                    dispose();
                    new MainSystemFrame().setVisible(true);
                } else {
                    // 登录失败
                    JOptionPane.showMessageDialog(null, "Invalid Student ID or Password!", "Error", JOptionPane.ERROR_MESSAGE);
                    // 清空密码框
                    passwordField.setText("");
                }
            }
        });

        // 注册按钮事件
        registerBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
                new TARegisterFrame().setVisible(true);
            }
        });
    }
}