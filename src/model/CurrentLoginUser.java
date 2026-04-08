package model;

// 全局登录状态：单例模式，确保整个程序只有一个登录实例，供资料/岗位模块获取当前TA信息
public class CurrentLoginUser {
    // 单例实例
    private static CurrentLoginUser instance;
    // 当前登录的TA信息
    private TAUser currentTA;
    // 登录状态
    private boolean isLogin;

    // 私有构造（禁止外部new）
    private CurrentLoginUser() {
        this.isLogin = false;
        this.currentTA = null;
    }

    // 获取单例实例（全队所有模块都通过此方法获取登录状态）
    public static CurrentLoginUser getInstance() {
        if (instance == null) {
            instance = new CurrentLoginUser();
        }
        return instance;
    }

    // 登录：设置TA信息和登录状态
    public void login(TAUser ta) {
        this.currentTA = ta;
        this.isLogin = true;
    }

    // 退出登录：清空状态
    public void logout() {
        this.currentTA = null;
        this.isLogin = false;
    }

    // Getter
    public TAUser getCurrentTA() { return currentTA; }
    public boolean isLogin() { return isLogin; }
}