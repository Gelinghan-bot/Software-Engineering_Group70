package util;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import model.TAUser;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

// JSON文件读写工具类：实现TA用户的增、查，全队所有模块的文件操作都可基于此扩展
public class JsonFileUtil {
    // 数据文件路径：项目根目录的data/ta_users.json（必须和目录结构一致）
    private static final String TA_USER_FILE_PATH = "data/ta_users.json";
    private static final Gson gson = new Gson();

    // 静态代码块：程序启动时自动创建data目录和JSON文件（避免文件不存在报错）
    static {
        File file = new File(TA_USER_FILE_PATH);
        File parentDir = file.getParentFile();
        if (!parentDir.exists()) {
            parentDir.mkdirs(); // 创建多级目录
        }
        if (!file.exists()) {
            try {
                file.createNewFile();
                // 新文件写入空的数组，避免GSON解析报错
                writeTAUsers(new ArrayList<>());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // 读取所有TA用户：从JSON文件转成List<TAUser>
    public static List<TAUser> readTAUsers() {
        try (FileReader reader = new FileReader(TA_USER_FILE_PATH)) {
            // GSON解析JSON数组为List
            return gson.fromJson(reader, new TypeToken<List<TAUser>>() {}.getType());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return new ArrayList<>();
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    // 写入TA用户：把List<TAUser>转成JSON写入文件（覆盖式写入）
    public static void writeTAUsers(List<TAUser> taUserList) {
        try (FileWriter writer = new FileWriter(TA_USER_FILE_PATH)) {
            // 格式化JSON，方便人工查看（后期可去掉format，提升性能）
            gson.toJson(taUserList, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 新增TA用户：校验学号唯一后添加，返回true=成功，false=学号已存在
    public static boolean addTAUser(TAUser newTA) {
        List<TAUser> taList = readTAUsers();
        // 学号唯一校验：遍历判断是否已存在
        for (TAUser ta : taList) {
            if (ta.getStudentId().equals(newTA.getStudentId())) {
                return false;
            }
        }
        // 新增并写入文件
        taList.add(newTA);
        writeTAUsers(taList);
        return true;
    }

    // 根据学号和密码查询TA：登录校验用，返回null=账号密码错误
    public static TAUser getTAByStudentIdAndPwd(String studentId, String password) {
        List<TAUser> taList = readTAUsers();
        for (TAUser ta : taList) {
            if (ta.getStudentId().equals(studentId) && ta.getPassword().equals(password)) {
                return ta;
            }
        }
        return null;
    }
}