package cn.carljoy.authloginui.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.util.Identifier;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.client.MinecraftClient;
import cn.carljoy.authloginui.client.gui.OwoLoginScreen;
import cn.carljoy.authloginui.client.gui.OwoRegisterScreen;
import java.util.Timer;
import java.util.TimerTask;

public class AuthloginuiClient implements ClientModInitializer {

    // 自定义插件消息通道标识符
    public static final Identifier IDENTIFIER = Identifier.of("custom", "prismauth");

    // 定时器用于持续发送消息
    private static Timer messageTimer = null;

    // 定义自定义Payload
    public record PrismAuthPayload(String data) implements CustomPayload {
        public static final CustomPayload.Id<PrismAuthPayload> ID = new CustomPayload.Id<>(IDENTIFIER);
        public static final PacketCodec<RegistryByteBuf, PrismAuthPayload> CODEC = PacketCodec.of(
                (value, buf) -> buf.writeString(value.data), // 写入UTF-8字符串
                buf -> new PrismAuthPayload(buf.readString()) // 读取UTF-8字符串
        );

        @Override
        public Id<? extends CustomPayload> getId() {
            return ID;
        }
    }

    @Override
    public void onInitializeClient() {
        // 注册自定义Payload类型（客户端到服务端）
        PayloadTypeRegistry.playC2S().register(PrismAuthPayload.ID, PrismAuthPayload.CODEC);
        
        // 注册自定义Payload类型（服务端到客户端）
        PayloadTypeRegistry.playS2C().register(PrismAuthPayload.ID, PrismAuthPayload.CODEC);

        // 注册插件消息接收器
        ClientPlayNetworking.registerGlobalReceiver(PrismAuthPayload.ID, (payload, context) -> {
            // 在主线程中处理接收到的消息
            context.client().execute(() -> {
                System.out.println("[AuthLoginUI] 收到来自Velocity的消息: " + payload.data());
                
                // 在这里处理接收到的消息
                handleVelocityMessage(payload.data());
            });
        });

        // 监听玩家加入游戏
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            System.out.println("[AuthLoginUI] 玩家加入游戏，开始发送插件消息");

            // 开始持续发送插件消息到 custom:prismauth 频道
            startContinuousPluginMessage();
        });

        // 监听断开连接
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            System.out.println("[AuthLoginUI] 玩家断开连接，停止发送插件消息");

            // 停止持续发送插件消息
            stopContinuousPluginMessage();
        });
    }

    /**
     * 开始持续发送插件消息到 custom:prismauth 频道
     */
    private static void startContinuousPluginMessage() {
        // 如果已经有定时器在运行，先停止它
        stopContinuousPluginMessage();

        messageTimer = new Timer("PrismAuth-PluginMessage-Timer", true);
        messageTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                try {
                    // 创建并发送插件消息
                    PrismAuthPayload payload = new PrismAuthPayload("heartbeat");
                    ClientPlayNetworking.send(payload);

                    System.out.println("[AuthLoginUI] 发送插件消息到 custom:prismauth 频道");
                } catch (Exception e) {
                    System.err.println("[AuthLoginUI] 发送插件消息时出错: " + e.getMessage());
                }
            }
        }, 1000, 5000); // 1秒后开始，每5秒发送一次

        System.out.println("[AuthLoginUI] 开始持续发送插件消息到 custom:prismauth 频道");
    }

    /**
     * 停止持续发送插件消息
     */
    private static void stopContinuousPluginMessage() {
        if (messageTimer != null) {
            messageTimer.cancel();
            messageTimer = null;
            System.out.println("[AuthLoginUI] 停止发送插件消息到 custom:prismauth 频道");
        }
    }
    
    /**
     * 处理从Velocity接收到的消息
     * @param message 接收到的消息内容
     */
    private static void handleVelocityMessage(String message) {
        System.out.println("[AuthLoginUI] 处理Velocity消息: " + message);
        
        MinecraftClient client = MinecraftClient.getInstance();
        
        // 根据消息内容执行不同的操作
        switch (message) {
            case "LOGIN_GUI":
                System.out.println("[AuthLoginUI] 显示登录界面");
                // 检查当前是否已经是登录界面，避免重复打开
                if (!(client.currentScreen instanceof OwoLoginScreen)) {
                    client.setScreen(new OwoLoginScreen());
                }
                break;
            case "REGISTER_GUI":
                System.out.println("[AuthLoginUI] 显示注册界面");
                // 检查当前是否已经是注册界面，避免重复打开
                if (!(client.currentScreen instanceof OwoRegisterScreen)) {
                    client.setScreen(new OwoRegisterScreen());
                }
                break;
            case "LOGIN_SUCCESS":
                System.out.println("[AuthLoginUI] 登录成功");
                // 关闭当前GUI
                if (client.currentScreen instanceof OwoLoginScreen) {
                    client.setScreen(null);
                }
                break;
            case "LOGIN_ERROR":
                System.out.println("[AuthLoginUI] 登录失败");
                // 显示错误提示，但保持GUI打开
                if (client.currentScreen instanceof OwoLoginScreen owoLoginScreen) {
                    owoLoginScreen.setErrorMessage("登录失败，请检查密码");
                }
                break;
            case "REGISTER_SUCCESS":
                System.out.println("[AuthLoginUI] 注册成功");
                // 关闭当前GUI
                if (client.currentScreen instanceof OwoRegisterScreen ) {
                    client.setScreen(null);
                }
                break;
            case "REGISTER_ERROR":
                System.out.println("[AuthLoginUI] 注册失败");
                // 显示错误提示，但保持GUI打开
                if (client.currentScreen instanceof OwoRegisterScreen owoRegisterScreen) {
                    owoRegisterScreen.setErrorMessage("注册失败，请重试");
                }
                break;
            case "auth_required":
                System.out.println("[AuthLoginUI] 服务器要求进行身份验证");
                // 兼容旧的消息格式
                break;
            case "auth_success":
                System.out.println("[AuthLoginUI] 身份验证成功");
                // 兼容旧的消息格式
                break;
            case "auth_failed":
                System.out.println("[AuthLoginUI] 身份验证失败");
                // 兼容旧的消息格式
                break;
            default:
                System.out.println("[AuthLoginUI] 未知消息类型: " + message);
                break;
        }
    }
}
