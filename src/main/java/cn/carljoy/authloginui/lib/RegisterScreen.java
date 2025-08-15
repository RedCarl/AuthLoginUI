package cn.carljoy.authloginui.lib;

import cn.carljoy.authloginui.velocity.VelocityMessageHandler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class RegisterScreen extends Screen {
    private static final String REGISTER_COMMAND = "/register";
    
    private TextFieldWidget passwordField;
    private TextFieldWidget confirmPasswordField;
    private String errorMessage = "";
    private int errorTimer = 0;

    public RegisterScreen(Text title) {
        super(title);
    }

    @Override
    protected void init() {
        // 第一次密码输入框
        passwordField = new TextFieldWidget(textRenderer, width/2-100, height/2-25, 200, 20, Text.literal("密码"));
        passwordField.setPlaceholder(Text.literal("请输入密码"));
        passwordField.setMaxLength(50);
        addDrawableChild(passwordField);
        
        // 确认密码输入框
        confirmPasswordField = new TextFieldWidget(textRenderer, width/2-100, height/2+5, 200, 20, Text.literal("确认密码"));
        confirmPasswordField.setPlaceholder(Text.literal("请再次输入密码"));
        confirmPasswordField.setMaxLength(50);
        addDrawableChild(confirmPasswordField);
        
        // 注册按钮
        addDrawableChild(ButtonWidget.builder(Text.literal("注册"), b -> sendRegister())
                .dimensions(width/2-50, height/2+35, 100, 20).build());
        
        setInitialFocus(passwordField);
    }
    
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // 渲染背景
        renderBackground(context, mouseX, mouseY, delta);
        
        // 渲染标题
        context.drawCenteredTextWithShadow(textRenderer, title, width/2, height/2-65, 0xFFFFFF);
        
        // 渲染标签
        context.drawTextWithShadow(textRenderer, "密码:", width/2-100, height/2-40, 0xFFFFFF);
        context.drawTextWithShadow(textRenderer, "确认密码:", width/2-100, height/2-10, 0xFFFFFF);
        
        // 渲染错误消息
        if (!errorMessage.isEmpty() && errorTimer > 0) {
            context.drawCenteredTextWithShadow(textRenderer, errorMessage, width/2, height/2+65, 0xFF5555);
            errorTimer--;
        }
        
        super.render(context, mouseX, mouseY, delta);
    }
    
    private void sendRegister() {
        String password = passwordField.getText().trim();
        String confirmPassword = confirmPasswordField.getText().trim();
        
        // 验证密码
        if (password.isEmpty()) {
            showError("请输入密码");
            return;
        }
        
        if (confirmPassword.isEmpty()) {
            showError("请确认密码");
            return;
        }
        
        if (!password.equals(confirmPassword)) {
            showError("两次输入的密码不一致");
            confirmPasswordField.setText("");
            return;
        }
        
        if (password.length() < 4) {
            showError("密码长度至少4位");
            return;
        }
        
        // 优先尝试使用Velocity插件消息通道
        if (VelocityMessageHandler.isVelocityMode()) {
            String username = MinecraftClient.getInstance().getSession().getUsername();
            VelocityMessageHandler.sendAuthRequest(username, password, true);
            System.out.println("[AuthLoginUI] 通过Velocity通道发送注册请求");
        } else {
            // 回退到聊天命令方式
            String cmd = REGISTER_COMMAND + " " + password + " " + confirmPassword;
            if (MinecraftClient.getInstance().player != null) {
                MinecraftClient.getInstance().player.networkHandler.sendChatCommand(cmd.substring(1)); // 移除开头的/
                System.out.println("[AuthLoginUI] 通过聊天命令发送注册请求");
            }
        }
        
        // 清空密码框但不关闭界面，等待服务器响应
        passwordField.setText("");
        confirmPasswordField.setText("");
    }
    
    public void showError(String message) {
        this.errorMessage = message;
        this.errorTimer = 100; // 显示5秒 (20 ticks/秒 * 5)
    }
    
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ENTER) {
            sendRegister();
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            close();
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_TAB) {
            // Tab键在两个输入框之间切换
            if (getFocused() == passwordField) {
                setFocused(confirmPasswordField);
            } else {
                setFocused(passwordField);
            }
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
    
    @Override
    public boolean shouldPause() {
        return false; // 不暂停游戏
    }
}