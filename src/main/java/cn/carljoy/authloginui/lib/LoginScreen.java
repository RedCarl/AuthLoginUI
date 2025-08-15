package cn.carljoy.authloginui.lib;

import cn.carljoy.authloginui.velocity.VelocityMessageHandler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class LoginScreen extends Screen {
    private static final String LOGIN_COMMAND = "/login";
    
    private TextFieldWidget passwordField;
    private String errorMessage = "";
    private int errorTimer = 0;

    public LoginScreen(Text title) {
        super(title);
    }

    @Override
    protected void init() {
        // 密码输入框
        passwordField = new TextFieldWidget(textRenderer, width/2-100, height/2-10, 200, 20, Text.literal("密码"));
        passwordField.setPlaceholder(Text.literal("请输入密码"));
        passwordField.setMaxLength(50);
        addDrawableChild(passwordField);
        
        // 登录按钮
        addDrawableChild(ButtonWidget.builder(Text.literal("登录"), b -> sendLogin())
                .dimensions(width/2-50, height/2+25, 100, 20).build());
        
        setInitialFocus(passwordField);
    }
    
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // 渲染背景
        renderBackground(context, mouseX, mouseY, delta);
        
        // 渲染标题
        context.drawCenteredTextWithShadow(textRenderer, title, width/2, height/2-50, 0xFFFFFF);
        
        // 渲染密码标签
        context.drawTextWithShadow(textRenderer, "密码:", width/2-100, height/2-25, 0xFFFFFF);
        
        // 渲染错误消息
        if (!errorMessage.isEmpty() && errorTimer > 0) {
            context.drawCenteredTextWithShadow(textRenderer, errorMessage, width/2, height/2+50, 0xFF5555);
            errorTimer--;
        }
        
        super.render(context, mouseX, mouseY, delta);
    }
    
    private void sendLogin() {
        String password = passwordField.getText().trim();
        if (password.isEmpty()) {
            showError("请输入密码");
            return;
        }
        
        // 优先尝试使用Velocity插件消息通道
        if (VelocityMessageHandler.isVelocityMode()) {
            String username = MinecraftClient.getInstance().getSession().getUsername();
            VelocityMessageHandler.sendAuthRequest(username, password, false);
            System.out.println("[AuthLoginUI] 通过Velocity通道发送登录请求");
        } else {
            // 回退到聊天命令方式
            String cmd = LOGIN_COMMAND + " " + password;
            if (MinecraftClient.getInstance().player != null) {
                MinecraftClient.getInstance().player.networkHandler.sendChatCommand(cmd.substring(1)); // 移除开头的/
                System.out.println("[AuthLoginUI] 通过聊天命令发送登录请求");
            }
        }
        
        // 清空密码框但不关闭界面，等待服务器响应
        passwordField.setText("");
    }
    
    public void showError(String message) {
        this.errorMessage = message;
        this.errorTimer = 100; // 显示5秒 (20 ticks/秒 * 5)
        passwordField.setText(""); // 清空密码框
    }
    
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ENTER) {
            sendLogin();
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            close();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
    
    @Override
    public boolean shouldPause() {
        return false; // 不暂停游戏
    }
}