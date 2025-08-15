package cn.carljoy.authloginui.listener;

import cn.carljoy.authloginui.lib.LoginScreen;
import cn.carljoy.authloginui.lib.RegisterScreen;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

public class ChatMessageListener {
    // 静态变量定义检测字符串
    private static final String[] LOGIN_TRIGGERS = {
        "/login",
        "请输入密码",
        "Please login",
        "请登录",
        "输入密码登录"
    };
    
    private static final String[] REGISTER_TRIGGERS = {
        "/register",
        "请注册",
        "Please register",
        "输入密码注册",
        "请设置密码"
    };
    
    private static final String[] ERROR_MESSAGES = {
        "密码错误",
        "Password incorrect",
        "Wrong password",
        "密码不正确",
        "登录失败"
    };
    
    private static final String[] SUCCESS_MESSAGES = {
        "登录成功",
        "Login successful",
        "Successfully logged in",
        "注册成功",
        "Register successful"
    };
    
    private static LoginScreen currentLoginScreen = null;
    private static RegisterScreen currentRegisterScreen = null;
    private static boolean isLoginServerMode = false; // 是否为登录服务器模式
    
    public static void register() {
        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
            if (overlay) return; // 忽略覆盖消息
            if (!isLoginServerMode) return; // 只在登录服务器模式下处理消息
            
            String messageText = message.getString().toLowerCase();
            String originalMessage = message.getString();
            
            // 输出聊天消息调试信息
            System.out.println("[AuthLoginUI] 收到聊天消息: " + originalMessage);
            MinecraftClient client = MinecraftClient.getInstance();
            
            // 检测登录触发器
            for (String trigger : LOGIN_TRIGGERS) {
                if (messageText.contains(trigger.toLowerCase())) {
                    System.out.println("[AuthLoginUI] 检测到登录触发器: " + trigger);
                    client.execute(() -> {
                        currentLoginScreen = new LoginScreen(Text.literal("登录验证"));
                        client.setScreen(currentLoginScreen);
                        System.out.println("[AuthLoginUI] 已打开登录界面");
                    });
                    return;
                }
            }
            
            // 检测注册触发器
            for (String trigger : REGISTER_TRIGGERS) {
                if (messageText.contains(trigger.toLowerCase())) {
                    System.out.println("[AuthLoginUI] 检测到注册触发器: " + trigger);
                    client.execute(() -> {
                        currentRegisterScreen = new RegisterScreen(Text.literal("注册账户"));
                        client.setScreen(currentRegisterScreen);
                        System.out.println("[AuthLoginUI] 已打开注册界面");
                    });
                    return;
                }
            }
            
            // 检测错误消息
            for (String error : ERROR_MESSAGES) {
                if (messageText.contains(error.toLowerCase())) {
                    System.out.println("[AuthLoginUI] 检测到错误消息: " + error);
                    client.execute(() -> {
                        if (currentLoginScreen != null && client.currentScreen == currentLoginScreen) {
                            currentLoginScreen.showError("密码错误，请重新输入");
                            System.out.println("[AuthLoginUI] 在登录界面显示错误消息");
                        }
                        if (currentRegisterScreen != null && client.currentScreen == currentRegisterScreen) {
                            currentRegisterScreen.showError("注册失败，请重试");
                            System.out.println("[AuthLoginUI] 在注册界面显示错误消息");
                        }
                    });
                    return;
                }
            }
            
            // 检测成功消息
            for (String success : SUCCESS_MESSAGES) {
                if (messageText.contains(success.toLowerCase())) {
                    System.out.println("[AuthLoginUI] 检测到成功消息: " + success);
                    client.execute(() -> {
                        if (currentLoginScreen != null && client.currentScreen == currentLoginScreen) {
                            client.setScreen(null);
                            currentLoginScreen = null;
                            System.out.println("[AuthLoginUI] 登录成功，关闭登录界面");
                        }
                        if (currentRegisterScreen != null && client.currentScreen == currentRegisterScreen) {
                            client.setScreen(null);
                            currentRegisterScreen = null;
                            System.out.println("[AuthLoginUI] 注册成功，关闭注册界面");
                        }
                    });
                    return;
                }
            }
        });
    }
    
    public static void clearCurrentScreens() {
        currentLoginScreen = null;
        currentRegisterScreen = null;
    }
    
    /**
     * 设置登录服务器模式
     * @param enabled 是否启用登录服务器模式
     */
    public static void setLoginServerMode(boolean enabled) {
        isLoginServerMode = enabled;
    }
}