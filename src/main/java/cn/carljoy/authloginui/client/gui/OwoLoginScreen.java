package cn.carljoy.authloginui.client.gui;

import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.*;
import net.minecraft.text.Text;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import cn.carljoy.authloginui.client.AuthloginuiClient;

public class OwoLoginScreen extends BaseOwoScreen<FlowLayout> {
    private String errorMessage = "";
    
    public OwoLoginScreen() {
        super();
    }
    
    @Override
    protected OwoUIAdapter<FlowLayout> createAdapter() {
        return OwoUIAdapter.create(this, Containers::verticalFlow);
    }
    
    @Override
    protected void build(FlowLayout rootComponent) {
        // 设置Minecraft风格的背景
        rootComponent.surface(Surface.flat(0xFF2F2F2F)); // 深灰色背景，更贴合MC风格
        
        // 创建居中的主容器
        FlowLayout centerContainer = Containers.verticalFlow(Sizing.content(), Sizing.content());
        centerContainer.surface(Surface.flat(0xC0000000).and(Surface.outline(0xFF8B8B8B))); // 半透明黑色背景加灰色边框
        centerContainer.padding(Insets.of(35));
        centerContainer.positioning(Positioning.relative(50, 50)); // 居中定位
        centerContainer.margins(Insets.of(15)); // 添加外边距
        
        // 标题 - 使用更大的字体
        centerContainer.child(
                Components.label(Text.literal("用户登录").styled(style -> style.withBold(true)))
                        .color(Color.WHITE)
                        .margins(Insets.of(0, 20, 0, 0))
        );
        
        
        // 密码标签
        centerContainer.child(
            Components.label(Text.literal("密码:"))
                .color(Color.ofArgb(0xFFCCCCCC))
                .margins(Insets.of(5, 0, 0, 0))
        );
        
        // 密码输入框
        var passwordField = Components.textBox(Sizing.fixed(200))
            .id("password-field")
            .margins(Insets.of(2, 0, 0, 0));
        
        centerContainer.child(passwordField);
        
        // 登录按钮
        var loginButton = Components.button(Text.literal("登录"), button -> {
            if (passwordField instanceof io.wispforest.owo.ui.component.TextBoxComponent textBox) {
                var password = textBox.getText();
                if (password.trim().isEmpty()) {
                    setErrorMessage("密码不能为空");
                    return;
                }
                
                // 清除错误信息
                setErrorMessage("");
                
                // 发送登录请求
                String loginMessage = "LOGIN," + password;
                AuthloginuiClient.PrismAuthPayload payload = new AuthloginuiClient.PrismAuthPayload(loginMessage);
                ClientPlayNetworking.send(payload);
            }
        })
        .sizing(Sizing.fixed(200), Sizing.fixed(30))
        .margins(Insets.of(5, 0, 0, 0));
        
        centerContainer.child(loginButton);
        
        // 错误信息标签
        var errorLabel = Components.label(Text.literal(errorMessage))
            .color(Color.ofArgb(0xFFFF5555))
            .id("error-label")
            .margins(Insets.of(5, 0, 0, 0));
        
        centerContainer.child(errorLabel);
        
        // 将居中容器添加到根组件
        rootComponent.child(centerContainer);
    }
    
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // 处理回车键登录
        if (keyCode == 257) { // ENTER key
            var passwordField = uiAdapter.rootComponent.childById(Component.class, "password-field");
            if (passwordField instanceof io.wispforest.owo.ui.component.TextBoxComponent textBox) {
                var password = textBox.getText();
                if (!password.trim().isEmpty()) {
                    setErrorMessage("");
                    String loginMessage = "LOGIN," + password;
                    AuthloginuiClient.PrismAuthPayload payload = new AuthloginuiClient.PrismAuthPayload(loginMessage);
                    ClientPlayNetworking.send(payload);
                    return true;
                }
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
    
    @Override
    public boolean shouldCloseOnEsc() {
        // 防止玩家使用ESC键关闭GUI
        return false;
    }
    
    @Override
    public void close() {
        // 防止GUI被关闭
        // 不调用super.close()，这样GUI就无法被关闭
    }
    
    @Override
    public boolean shouldPause() {
        // 不暂停游戏
        return false;
    }
    
    // 设置错误信息
    public void setErrorMessage(String message) {
        this.errorMessage = message;
        if (uiAdapter != null && uiAdapter.rootComponent != null) {
            var errorLabel = uiAdapter.rootComponent.childById(Component.class, "error-label");
            if (errorLabel instanceof io.wispforest.owo.ui.component.LabelComponent label) {
                label.text(Text.literal(message));
            }
        }
    }
    
    // 清除错误信息
    public void clearErrorMessage() {
        setErrorMessage("");
    }
}