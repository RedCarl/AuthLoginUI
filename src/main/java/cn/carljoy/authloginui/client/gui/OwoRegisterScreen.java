package cn.carljoy.authloginui.client.gui;

import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.*;
import net.minecraft.text.Text;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import cn.carljoy.authloginui.client.AuthloginuiClient;
import org.jetbrains.annotations.NotNull;

public class OwoRegisterScreen extends BaseOwoScreen<FlowLayout> {
    private String errorMessage = "";
    
    public OwoRegisterScreen() {
        super();
    }
    
    @Override
    protected @NotNull OwoUIAdapter<FlowLayout> createAdapter() {
        return OwoUIAdapter.create(this, Containers::verticalFlow);
    }
    
    @Override
    protected void build(FlowLayout rootComponent) {
        // 创建居中的主容器
        FlowLayout centerContainer = Containers.verticalFlow(Sizing.content(), Sizing.content());
        centerContainer.surface(Surface.DARK_PANEL);
        centerContainer.padding(Insets.of(35));
        centerContainer.positioning(Positioning.relative(50, 50)); // 居中定位
        centerContainer.margins(Insets.of(15)); // 添加外边距
        
        // 标题 - 使用更大的字体
        centerContainer.child(
                Components.label(Text.literal("用户注册").styled(style -> style.withBold(true)))
                        .color(Color.WHITE)
                        .margins(Insets.of(0, 0, 0, 0))
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
        
        // 确认密码标签
        centerContainer.child(
            Components.label(Text.literal("确认密码:"))
                .color(Color.ofArgb(0xFFCCCCCC))
                .margins(Insets.of(5, 0, 0, 0))
        );
        
        // 确认密码输入框
        var confirmPasswordField = Components.textBox(Sizing.fixed(200))
            .id("confirm-password-field")
            .margins(Insets.of(2, 0, 0, 0));
        
        centerContainer.child(confirmPasswordField);
        
        // 注册按钮
        var registerButton = Components.button(Text.literal("注册"), button -> {
            if (passwordField instanceof io.wispforest.owo.ui.component.TextBoxComponent passwordBox &&
                confirmPasswordField instanceof io.wispforest.owo.ui.component.TextBoxComponent confirmBox) {
                var password = passwordBox.getText();
                var confirmPassword = confirmBox.getText();
                
                // 验证输入
                if (password.trim().isEmpty() || confirmPassword.trim().isEmpty()) {
                    setErrorMessage("密码字段不能为空");
                    return;
                }
                
                if (password.length() < 6) {
                    setErrorMessage("密码长度至少6位");
                    return;
                }
                
                if (!password.equals(confirmPassword)) {
                    setErrorMessage("两次输入的密码不一致");
                    return;
                }
                
                // 清除错误信息
                setErrorMessage("");
                
                // 发送注册请求
                String registerMessage = "REGISTER," + password;
                AuthloginuiClient.PrismAuthPayload payload = new AuthloginuiClient.PrismAuthPayload(registerMessage);
                ClientPlayNetworking.send(payload);
            }
        })
        .sizing(Sizing.fixed(200), Sizing.fixed(30))
        .margins(Insets.of(5, 0, 0, 0));
        
        centerContainer.child(registerButton);
        
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
        // 处理Tab键切换
        if (keyCode == 258) { // TAB key
            var passwordField = uiAdapter.rootComponent.childById(Component.class, "password-field");
            var confirmPasswordField = uiAdapter.rootComponent.childById(Component.class, "confirm-password-field");
            if (passwordField instanceof io.wispforest.owo.ui.component.TextBoxComponent passwordBox &&
                confirmPasswordField instanceof io.wispforest.owo.ui.component.TextBoxComponent confirmBox) {
                // 简单的焦点切换逻辑
                return true;
            }
        }
        
        // 处理回车键注册
        if (keyCode == 257) { // ENTER key
            var passwordField = uiAdapter.rootComponent.childById(Component.class, "password-field");
            var confirmPasswordField = uiAdapter.rootComponent.childById(Component.class, "confirm-password-field");
            if (passwordField instanceof io.wispforest.owo.ui.component.TextBoxComponent passwordBox &&
                confirmPasswordField instanceof io.wispforest.owo.ui.component.TextBoxComponent confirmBox) {
                var password = passwordBox.getText();
                var confirmPassword = confirmBox.getText();
                if (!password.trim().isEmpty() && !confirmPassword.trim().isEmpty() && 
                    password.equals(confirmPassword) && password.length() >= 6) {
                    setErrorMessage("");
                    String registerMessage = "REGISTER," + password;
                    AuthloginuiClient.PrismAuthPayload payload = new AuthloginuiClient.PrismAuthPayload(registerMessage);
                    ClientPlayNetworking.send(payload);
                    return true;
                }
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
    
    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }
    
    @Override
    public void close() {
        super.close();
    }
    
    @Override
    public boolean shouldPause() {
        return false;
    }
    
    // 设置错误信息
    public void setErrorMessage(String message) {
        this.errorMessage = message;
        var errorLabel = uiAdapter.rootComponent.childById(Component.class, "error-label");
        if (errorLabel instanceof io.wispforest.owo.ui.component.LabelComponent label) {
            label.text(Text.literal(message));
        }
    }
    
    // 清除错误信息
    public void clearErrorMessage() {
        setErrorMessage("");
    }
}