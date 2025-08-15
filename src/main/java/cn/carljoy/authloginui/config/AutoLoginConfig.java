package cn.carljoy.authloginui.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AutoLoginConfig {
    public static final AutoLoginConfig INSTANCE = new AutoLoginConfig();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String CONFIG_FILE = "authloginui.json";
    
    // 配置选项
    public List<String> autoPopIps = new ArrayList<>();
    public String defaultUsername = "";
    public String defaultPassword = "";
    public boolean rememberCredentials = false;
    public boolean autoLogin = false;
    public String loginCommand = "/l";
    public String registerCommand = "/reg";
    
    private AutoLoginConfig() {
        // 默认添加一些常见的服务器IP
        autoPopIps.add("localhost");
        autoPopIps.add("127.0.0.1");
    }
    
    /**
     * 加载配置文件
     */
    public void load() {
        File configDir = FabricLoader.getInstance().getConfigDir().toFile();
        File configFile = new File(configDir, CONFIG_FILE);
        
        if (!configFile.exists()) {
            save(); // 创建默认配置文件
            return;
        }
        
        try (FileReader reader = new FileReader(configFile)) {
            AutoLoginConfig loaded = GSON.fromJson(reader, AutoLoginConfig.class);
            if (loaded != null) {
                this.autoPopIps = loaded.autoPopIps != null ? loaded.autoPopIps : new ArrayList<>();
                this.defaultUsername = loaded.defaultUsername != null ? loaded.defaultUsername : "";
                this.defaultPassword = loaded.defaultPassword != null ? loaded.defaultPassword : "";
                this.rememberCredentials = loaded.rememberCredentials;
                this.autoLogin = loaded.autoLogin;
                this.loginCommand = loaded.loginCommand != null ? loaded.loginCommand : "/l";
                this.registerCommand = loaded.registerCommand != null ? loaded.registerCommand : "/reg";
            }
        } catch (IOException e) {
            System.err.println("Failed to load AuthLoginUI config: " + e.getMessage());
        }
    }
    
    /**
     * 保存配置文件
     */
    public void save() {
        File configDir = FabricLoader.getInstance().getConfigDir().toFile();
        if (!configDir.exists()) {
            configDir.mkdirs();
        }
        
        File configFile = new File(configDir, CONFIG_FILE);
        
        try (FileWriter writer = new FileWriter(configFile)) {
            GSON.toJson(this, writer);
        } catch (IOException e) {
            System.err.println("Failed to save AuthLoginUI config: " + e.getMessage());
        }
    }
    
    /**
     * 添加服务器IP到自动弹出列表
     */
    public void addAutoPopIp(String ip) {
        if (!autoPopIps.contains(ip)) {
            autoPopIps.add(ip);
            save();
        }
    }
    
    /**
     * 移除服务器IP从自动弹出列表
     */
    public void removeAutoPopIp(String ip) {
        if (autoPopIps.remove(ip)) {
            save();
        }
    }
    
    /**
     * 检查是否应该为指定IP显示登录界面
     */
    public boolean shouldShowLoginGui(String serverAddress) {
        if (serverAddress == null) return false;
        return autoPopIps.stream().anyMatch(serverAddress::contains);
    }
}