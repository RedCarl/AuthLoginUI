package cn.carljoy.authloginui.mixin;

import cn.carljoy.authloginui.client.gui.OwoLoginScreen;
import cn.carljoy.authloginui.client.gui.OwoRegisterScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.ChatHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChatHud.class)
public class ChatHudMixin {
    
    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void hideChatDuringAuth(CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        
        // 如果当前屏幕是登录或注册界面，则隐藏聊天栏
        if (client.currentScreen instanceof OwoLoginScreen || 
            client.currentScreen instanceof OwoRegisterScreen) {
            ci.cancel();
        }
    }
    
}