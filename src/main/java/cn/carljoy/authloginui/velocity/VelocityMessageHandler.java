package cn.carljoy.authloginui.velocity;

import cn.carljoy.authloginui.lib.LoginScreen;
import cn.carljoy.authloginui.lib.RegisterScreen;
import cn.carljoy.authloginui.listener.ChatMessageListener;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.network.PacketByteBuf;
import io.netty.buffer.Unpooled;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;

// 移除了不再需要的IO导入

public class VelocityMessageHandler {
    // 插件消息通道标识符
    public static final Identifier AUTH_CHANNEL_ID = Identifier.of("authloginui", "auth");
    public static final Identifier VELOCITY_CHANNEL_ID = Identifier.of("velocity", "main");
    
    // 消息类型常量
    private static final String MSG_LOGIN_REQUIRED = "LOGIN_REQUIRED";
    private static final String MSG_REGISTER_REQUIRED = "REGISTER_REQUIRED";
    private static final String MSG_AUTH_SUCCESS = "AUTH_SUCCESS";
    private static final String MSG_AUTH_ERROR = "AUTH_ERROR";
    private static final String MSG_SERVER_INFO = "SERVER_INFO";
    
    private static LoginScreen currentLoginScreen = null;
    private static RegisterScreen currentRegisterScreen = null;
    private static boolean isVelocityMode = false;
    
    // 自定义Payload类
    public record AuthPayload(String messageType, String data) implements CustomPayload {
        public static final CustomPayload.Id<AuthPayload> ID = new CustomPayload.Id<>(AUTH_CHANNEL_ID);
        public static final PacketCodec<RegistryByteBuf, AuthPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.STRING, AuthPayload::messageType,
            PacketCodecs.STRING, AuthPayload::data,
            AuthPayload::new
        );
        
        @Override
        public Id<? extends CustomPayload> getId() {
            return ID;
        }
    }
    
    public record VelocityPayload(String messageType, String data) implements CustomPayload {
        public static final CustomPayload.Id<VelocityPayload> ID = new CustomPayload.Id<>(VELOCITY_CHANNEL_ID);
        public static final PacketCodec<RegistryByteBuf, VelocityPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.STRING, VelocityPayload::messageType,
            PacketCodecs.STRING, VelocityPayload::data,
            VelocityPayload::new
        );
        
        @Override
        public Id<? extends CustomPayload> getId() {
            return ID;
        }
    }

    /**
     * 注册插件消息监听器
     */
    public static void register() {
        // 注册CustomPayload类型
        PayloadTypeRegistry.playC2S().register(AuthPayload.ID, AuthPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(AuthPayload.ID, AuthPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(VelocityPayload.ID, VelocityPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(VelocityPayload.ID, VelocityPayload.CODEC);
        
        // 注册认证通道监听器（CustomPayload格式）
        ClientPlayNetworking.registerGlobalReceiver(AuthPayload.ID, (payload, context) -> {
            try {
                handleAuthMessage(context.client(), payload);
            } catch (Exception e) {
                System.err.println("[AuthLoginUI] 处理认证消息时出错: " + e.getMessage());
                e.printStackTrace();
            }
        });
        
        // 注册Velocity通道监听器（CustomPayload格式）
        ClientPlayNetworking.registerGlobalReceiver(VelocityPayload.ID, (payload, context) -> {
            try {
                handleVelocityMessage(context.client(), payload);
            } catch (Exception e) {
                System.err.println("[AuthLoginUI] 处理Velocity消息时出错: " + e.getMessage());
                e.printStackTrace();
            }
        });
        
        System.out.println("[AuthLoginUI] 插件消息监听器已注册");
    }
    
    /**
     * 处理认证相关消息（CustomPayload格式）
     */
    private static void handleAuthMessage(MinecraftClient client, AuthPayload payload) {
        try {
            String messageType = payload.messageType();
            String messageData = payload.data();
            
            System.out.println("[AuthLoginUI] 收到认证消息: " + messageType);
            
            client.execute(() -> {
                switch (messageType) {
                    case MSG_LOGIN_REQUIRED:
                        System.out.println("[AuthLoginUI] 需要登录: " + messageData);
                        // 启用聊天监听功能作为备用方案
                        ChatMessageListener.setLoginServerMode(true);
                        openLoginScreen(client, messageData);
                        break;
                        
                    case MSG_REGISTER_REQUIRED:
                        System.out.println("[AuthLoginUI] 需要注册: " + messageData);
                        // 启用聊天监听功能作为备用方案
                        ChatMessageListener.setLoginServerMode(true);
                        openRegisterScreen(client, messageData);
                        break;
                        
                    case MSG_AUTH_SUCCESS:
                        System.out.println("[AuthLoginUI] 认证成功");
                        closeCurrentScreens(client);
                        break;
                        
                    case MSG_AUTH_ERROR:
                        System.out.println("[AuthLoginUI] 认证错误: " + messageData);
                        showError(messageData);
                        break;
                        
                    case MSG_SERVER_INFO:
                        System.out.println("[AuthLoginUI] 服务器信息: " + messageData);
                        // 解析服务器信息，简单起见假设data格式为"type:isAuthServer"
                        String[] parts = messageData.split(":");
                        if (parts.length >= 2) {
                            boolean isAuthServer = Boolean.parseBoolean(parts[1]);
                            setVelocityMode(isAuthServer);
                        }
                        break;
                        
                    default:
                        System.out.println("[AuthLoginUI] 未知消息类型: " + messageType);
                        break;
                }
            });
        } catch (Exception e) {
            System.err.println("[AuthLoginUI] 处理认证消息时出错: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 处理认证相关消息（传统格式）
     */
    private static void handleAuthMessageLegacy(MinecraftClient client, PacketByteBuf buf) {
        try {
            byte[] data = new byte[buf.readableBytes()];
            buf.readBytes(data);
            
            ByteArrayInputStream byteStream = new ByteArrayInputStream(data);
            DataInputStream dataStream = new DataInputStream(byteStream);
            
            String messageType = dataStream.readUTF();
            String messageData = dataStream.readUTF();
            
            System.out.println("[AuthLoginUI] 收到认证消息: " + messageType);
            
            client.execute(() -> {
                switch (messageType) {
                    case MSG_LOGIN_REQUIRED:
                        System.out.println("[AuthLoginUI] 需要登录: " + messageData);
                        // 启用聊天监听功能作为备用方案
                        ChatMessageListener.setLoginServerMode(true);
                        openLoginScreen(client, messageData);
                        break;
                        
                    case MSG_REGISTER_REQUIRED:
                        System.out.println("[AuthLoginUI] 需要注册: " + messageData);
                        // 启用聊天监听功能作为备用方案
                        ChatMessageListener.setLoginServerMode(true);
                        openRegisterScreen(client, messageData);
                        break;
                        
                    case MSG_AUTH_SUCCESS:
                        System.out.println("[AuthLoginUI] 认证成功");
                        closeCurrentScreens(client);
                        break;
                        
                    case MSG_AUTH_ERROR:
                        System.out.println("[AuthLoginUI] 认证错误: " + messageData);
                        showError(messageData);
                        break;
                        
                    case MSG_SERVER_INFO:
                        System.out.println("[AuthLoginUI] 服务器信息: " + messageData);
                        // 解析服务器信息，简单起见假设data格式为"type:isAuthServer"
                        String[] parts = messageData.split(":");
                        if (parts.length >= 2) {
                            boolean isAuthServer = Boolean.parseBoolean(parts[1]);
                            setVelocityMode(isAuthServer);
                        }
                        break;
                        
                    default:
                        System.out.println("[AuthLoginUI] 未知消息类型: " + messageType);
                        break;
                }
            });
        } catch (Exception e) {
            System.err.println("[AuthLoginUI] 处理认证消息时出错: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 处理Velocity代理消息（CustomPayload格式）
     */
    private static void handleVelocityMessage(MinecraftClient client, VelocityPayload payload) {
        try {
            String messageType = payload.messageType();
            String messageData = payload.data();
            
            System.out.println("[AuthLoginUI] 收到Velocity消息 - 类型: " + messageType);
            
            // 处理不同的消息类型
            switch (messageType) {
                case "AuthLogin":
                    client.execute(() -> openLoginScreen(client, messageData));
                    break;
                    
                case "AuthRegister":
                    client.execute(() -> openRegisterScreen(client, messageData));
                    break;
                    
                case "AuthSuccess":
                    client.execute(() -> closeCurrentScreens(client));
                    break;
                    
                case "AuthError":
                    client.execute(() -> showError(messageData));
                    break;
                    
                default:
                    System.out.println("[AuthLoginUI] 未知Velocity消息类型: " + messageType);
                    break;
            }
        } catch (Exception e) {
            System.err.println("[AuthLoginUI] 处理Velocity消息时出错: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 处理Velocity代理消息（兼容BungeeCord格式，传统格式）
     */
    private static void handleVelocityMessageLegacy(MinecraftClient client, PacketByteBuf buf) {
        try {
            byte[] data = new byte[buf.readableBytes()];
            buf.readBytes(data);
            
            ByteArrayInputStream byteStream = new ByteArrayInputStream(data);
            DataInputStream dataStream = new DataInputStream(byteStream);
            
            String messageType = dataStream.readUTF();
            String messageData = dataStream.readUTF();
            
            System.out.println("[AuthLoginUI] 收到Velocity消息 - 类型: " + messageType);
            
            // 处理不同的消息类型
            switch (messageType) {
                case "AuthLogin":
                    client.execute(() -> openLoginScreen(client, messageData));
                    break;
                    
                case "AuthRegister":
                    client.execute(() -> openRegisterScreen(client, messageData));
                    break;
                    
                case "AuthSuccess":
                    client.execute(() -> closeCurrentScreens(client));
                    break;
                    
                case "AuthError":
                    client.execute(() -> showError(messageData));
                    break;
                    
                default:
                    System.out.println("[AuthLoginUI] 未知Velocity消息类型: " + messageType);
                    break;
            }
        } catch (Exception e) {
            System.err.println("[AuthLoginUI] 处理Velocity消息时出错: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 打开登录界面
     */
    private static void openLoginScreen(MinecraftClient client, String message) {
        currentLoginScreen = new LoginScreen(Text.literal(message.isEmpty() ? "登录验证" : message));
        client.setScreen(currentLoginScreen);
        System.out.println("[AuthLoginUI] 已通过Velocity消息打开登录界面");
    }
    
    /**
     * 打开注册界面
     */
    private static void openRegisterScreen(MinecraftClient client, String message) {
        currentRegisterScreen = new RegisterScreen(Text.literal(message.isEmpty() ? "注册账户" : message));
        client.setScreen(currentRegisterScreen);
        System.out.println("[AuthLoginUI] 已通过Velocity消息打开注册界面");
    }
    
    /**
     * 关闭当前界面
     */
    private static void closeCurrentScreens(MinecraftClient client) {
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
    }
    
    /**
     * 显示错误消息
     */
    private static void showError(String errorMessage) {
        if (currentLoginScreen != null) {
            currentLoginScreen.showError(errorMessage);
            System.out.println("[AuthLoginUI] 在登录界面显示错误: " + errorMessage);
        }
        if (currentRegisterScreen != null) {
            currentRegisterScreen.showError(errorMessage);
            System.out.println("[AuthLoginUI] 在注册界面显示错误: " + errorMessage);
        }
    }
    
    /**
     * 发送认证请求到Velocity（使用CustomPayload格式）
     */
    public static void sendAuthRequest(String username, String password, boolean isRegister) {
        if (!isVelocityMode) {
            System.out.println("[AuthLoginUI] 非Velocity模式，跳过发送认证请求");
            return;
        }
        
        try {
            String messageType = isRegister ? "REGISTER_REQUEST" : "LOGIN_REQUEST";
            String data = username + ":" + password;
            
            AuthPayload payload = new AuthPayload(messageType, data);
            ClientPlayNetworking.send(payload);
            
            System.out.println("[AuthLoginUI] 已发送" + (isRegister ? "注册" : "登录") + "请求到Velocity");
        } catch (Exception e) {
            System.err.println("[AuthLoginUI] 发送认证请求时出错: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 请求服务器信息（使用CustomPayload格式）
     */
    public static void requestServerInfo() {
        try {
            AuthPayload payload = new AuthPayload("REQUEST_SERVER_INFO", "");
            ClientPlayNetworking.send(payload);
            
            System.out.println("[AuthLoginUI] 已请求服务器信息");
        } catch (Exception e) {
            System.err.println("[AuthLoginUI] 请求服务器信息时出错: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 设置Velocity模式
     */
    public static void setVelocityMode(boolean enabled) {
        isVelocityMode = enabled;
        System.out.println("[AuthLoginUI] Velocity模式: " + (enabled ? "启用" : "禁用"));
    }
    
    /**
     * 检查是否为Velocity模式
     */
    public static boolean isVelocityMode() {
        return isVelocityMode;
    }
    
    /**
     * 清理当前界面状态
     */
    public static void clearCurrentScreens() {
        currentLoginScreen = null;
        currentRegisterScreen = null;
        System.out.println("[AuthLoginUI] 已清理Velocity界面状态");
    }
}