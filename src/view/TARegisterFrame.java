package view;

import model.TAUser;
import util.JsonFileUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

// TA注册窗口
public class TARegisterFrame extends JFrame {
    // 界面组件（全局定义，方便事件监听）
    private JTextField studentIdField;
    private JTextField nameField;
    private JPasswordField passwordField;
    private JPasswordField confirmPwdField;
    private JTextField emailField;
    private JButton registerBtn;
    private JButton backToLoginBtn;

    public TARegisterFrame() {
        // 界面基础设置
        setTitle("TA Recruitment System - Register");
        setSize(400, 350);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new GridLayout(7, 2, 10, 10)); // 7行2列，间距10
        setResizable(false); // 禁止窗口缩放

        // 初始化组件
        initComponents();
        // 绑定事件
        bindEvents();

        setVisible(true);
    }

    // 初始化界面组件
    private void initComponents() {
        // 添加标签和输入框
        add(new JLabel("Student ID (Unique):"));
        studentIdField = new JTextField();
        add(studentIdField);

        add(new JLabel("Name:"));
        nameField = new JTextField();
        add(nameField);

        add(new JLabel("Password:"));
        passwordField = new JPasswordField();
        add(passwordField);

        add(new JLabel("Confirm Password:"));
        confirmPwdField = new JPasswordField();
        add(confirmPwdField);

        add(new JLabel("Email:"));
        emailField = new JTextField();
        add(emailField);

        // 按钮
        registerBtn = new JButton("Register");
        add(registerBtn);

        backToLoginBtn = new JButton("Back to Login");
        add(backToLoginBtn);

        // 给所有组件添加边距，美化界面
        for (Component c : getContentPane().getComponents()) {
            ((JComponent) c).setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        }
    }

    // 绑定按钮事件
    private void bindEvents() {
        // 注册按钮点击事件
        registerBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // 获取输入值
                String studentId = studentIdField.getText().trim();
                String name = nameField.getText().trim();
                String password = new String(passwordField.getPassword()).trim();
                String confirmPwd = new String(confirmPwdField.getPassword()).trim();
                String email = emailField.getText().trim();

                // 1. 非空校验
                if (studentId.isEmpty() || name.isEmpty() || password.isEmpty() || confirmPwd.isEmpty() || email.isEmpty()) {
                    JOptionPane.showMessageDialog(null, "All fields cannot be empty!", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // 2. 密码一致性校验
                if (!password.equals(confirmPwd)) {
                    JOptionPane.showMessageDialog(null, "Password and confirm password do not match!", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // 3. 密码长度校验（至少6位）
                if (password.length() < 6) {
                    JOptionPane.showMessageDialog(null, "Password must be at least 6 characters!", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // 4. 创建TA用户对象，调用工具类新增
                TAUser newTA = new TAUser(studentId, name, password, email);
                boolean isSuccess = JsonFileUtil.addTAUser(newTA);

                // 结果提示
                if (isSuccess) {
                    JOptionPane.showMessageDialog(null, "Register successfully! Please login.", "Success", JOptionPane.INFORMATION_MESSAGE);
                    // 关闭注册窗口，打开登录窗口
                    dispose();
                    new TALoginFrame().setVisible(true);
                } else {
                    JOptionPane.showMessageDialog(null, "Student ID already exists!", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // 返回登录按钮事件
        backToLoginBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
                new TALoginFrame().setVisible(true);
            }
        });
    }
}