package model;

import java.io.Serializable;

// TA用户实体类，和JSON字段一一对应
public class TAUser implements Serializable {
    private String studentId; // 学号（唯一主键，核心）
    private String name;      // 姓名
    private String password;  // 密码
    private String email;     // 联系邮箱

    // 无参构造（GSON解析必备）
    public TAUser() {}

    // 有参构造
    public TAUser(String studentId, String name, String password, String email) {
        this.studentId = studentId;
        this.name = name;
        this.password = password;
        this.email = email;
    }

    // Getter和Setter（必须全写，GSON读写需要）
    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}