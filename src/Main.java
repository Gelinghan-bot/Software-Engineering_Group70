import view.TALoginFrame;

import javax.swing.*;

// 程序入口类：全队所有功能都从这里启动，必须放在src根目录
public class Main {
    public static void main(String[] args) {
        // Swing界面在主线程外运行，避免卡顿
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                // 启动直接打开TA登录窗口
                new TALoginFrame();
            }
        });
    }
}