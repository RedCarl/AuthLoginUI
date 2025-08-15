package cn.carljoy.authloginui.client;

import cn.carljoy.authloginui.listener.ChatMessageListener;
import cn.carljoy.authloginui.velocity.VelocityMessageHandler;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;

public class AuthloginuiClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        // 注册聊天消息监听器
        ChatMessageListener.register();
        
        // 注册Velocity插件消息通道
        VelocityMessageHandler.register();
        
        // 监听玩家加入游戏，清理之前的界面状态
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            ChatMessageListener.clearCurrentScreens();
            
            // 输出调试信息
            System.out.println("[AuthLoginUI] ===== 服务器连接调试信息 =====");
            System.out.println("[AuthLoginUI] 已连接到服务器，等待Velocity消息判断服务器类型");
            
            // 默认禁用聊天监听功能，等待Velocity消息来决定
            ChatMessageListener.setLoginServerMode(false);
            
            // 请求Velocity服务器信息（如果支持）
            VelocityMessageHandler.requestServerInfo();
            
            System.out.println("[AuthLoginUI] ==================================");
        });
        
        // 监听断开连接，清理界面状态
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            ChatMessageListener.clearCurrentScreens();
            VelocityMessageHandler.clearCurrentScreens();
            VelocityMessageHandler.setVelocityMode(false);
        });
    }
    

}
