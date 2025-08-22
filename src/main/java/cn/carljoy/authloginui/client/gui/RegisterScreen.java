package cn.carljoy.authloginui.client.gui;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import cn.carljoy.authloginui.client.AuthloginuiClient;

public class RegisterScreen extends Screen {
    private TextFieldWidget passwordField;
    private TextFieldWidget confirmPasswordField;
    private ButtonWidget registerButton;
    private static final int PANEL_WIDTH = 340;
    private static final int PANEL_HEIGHT = 220;
    private static final int FIELD_WIDTH = 200;
    private static final int FIELD_HEIGHT = 20;
    private static final int BUTTON_WIDTH = 100;
    private static final int BUTTON_HEIGHT = 20;
    
    // 颜色常量
    private static final int PANEL_COLOR = 0x88000000; // 半透明黑色背景
    private static final int BORDER_COLOR = 0xFF4A90E2; // 蓝色边框
    private static final int TITLE_COLOR = 0xFFFFFFFF; // 白色标题
    private static final int LABEL_COLOR = 0xFFCCCCCC; // 灰色标签
    private static final int ERROR_COLOR = 0xFFFF5555; // 红色错误提示
    
    private String errorMessage = "";

    public RegisterScreen() {
        super(Text.literal("用户注册"));
    }

    @Override
    protected void init() {
        super.init();
        
        int centerX = this.width / 2;
        int centerY = this.height / 2;
        
        // 密码输入框
        this.passwordField = new TextFieldWidget(
            this.textRenderer, 
            centerX - FIELD_WIDTH / 2, 
            centerY - 25, 
            FIELD_WIDTH, 
            FIELD_HEIGHT, 
            Text.literal("密码")
        );
        this.passwordField.setPlaceholder(Text.literal("请输入您的密码"));
        this.passwordField.setMaxLength(128);
        // 设置为密码模式（隐藏输入内容）
        this.passwordField.setRenderTextProvider((string, firstCharacterIndex) -> {
            return Text.literal("●".repeat(string.length())).asOrderedText();
        });
        this.addSelectableChild(this.passwordField);
        
        // 确认密码输入框
        this.confirmPasswordField = new TextFieldWidget(
            this.textRenderer, 
            centerX - FIELD_WIDTH / 2, 
            centerY + 15, 
            FIELD_WIDTH, 
            FIELD_HEIGHT, 
            Text.literal("确认密码")
        );
        this.confirmPasswordField.setPlaceholder(Text.literal("请再次输入密码"));
        this.confirmPasswordField.setMaxLength(128);
        // 设置为密码模式（隐藏输入内容）
        this.confirmPasswordField.setRenderTextProvider((string, firstCharacterIndex) -> {
            return Text.literal("●".repeat(string.length())).asOrderedText();
        });
        this.addSelectableChild(this.confirmPasswordField);
        
        // 注册按钮
        this.registerButton = ButtonWidget.builder(
            Text.literal("注册"),
            button -> this.onRegisterButtonPressed()
        )
        .dimensions(centerX - BUTTON_WIDTH / 2, centerY + 55, BUTTON_WIDTH, BUTTON_HEIGHT)
        .build();
        this.addDrawableChild(this.registerButton);
        
        // 设置初始焦点
        this.setInitialFocus(this.passwordField);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // 绘制模糊背景
        this.renderBackground(context, mouseX, mouseY, delta);
        
        int centerX = this.width / 2;
        int centerY = this.height / 2;
        
        // 绘制主面板背景
        int panelX = centerX - PANEL_WIDTH / 2;
        int panelY = centerY - PANEL_HEIGHT / 2;
        
        // 绘制面板阴影
        context.fill(panelX + 3, panelY + 3, panelX + PANEL_WIDTH + 3, panelY + PANEL_HEIGHT + 3, 0x44000000);
        
        // 绘制面板背景
        context.fill(panelX, panelY, panelX + PANEL_WIDTH, panelY + PANEL_HEIGHT, PANEL_COLOR);
        
        // 绘制面板边框
        context.drawBorder(panelX, panelY, PANEL_WIDTH, PANEL_HEIGHT, BORDER_COLOR);
        
        // 绘制标题
        Text titleText = Text.literal("📝 用户注册");
        int titleWidth = this.textRenderer.getWidth(titleText);
        context.drawTextWithShadow(
            this.textRenderer, 
            titleText, 
            centerX - titleWidth / 2, 
            centerY - 65, 
            TITLE_COLOR
        );
        
        // 绘制密码标签
        context.drawTextWithShadow(
            this.textRenderer, 
            Text.literal("密码:"), 
            centerX - FIELD_WIDTH / 2, 
            centerY - 40, 
            LABEL_COLOR
        );
        
        // 绘制确认密码标签
        context.drawTextWithShadow(
            this.textRenderer, 
            Text.literal("确认密码:"), 
            centerX - FIELD_WIDTH / 2, 
            centerY, 
            LABEL_COLOR
        );
        
        // 绘制组件
        super.render(context, mouseX, mouseY, delta);
        
        // 绘制错误信息
        if (!errorMessage.isEmpty()) {
            int errorWidth = this.textRenderer.getWidth(errorMessage);
            context.drawTextWithShadow(
                this.textRenderer, 
                Text.literal(errorMessage), 
                centerX - errorWidth / 2, 
                centerY + 85, 
                ERROR_COLOR
            );
        }
        
        // 绘制提示信息
        Text hintText = Text.literal("按 Tab 键切换输入框，Enter 键注册");
        int hintWidth = this.textRenderer.getWidth(hintText);
        context.drawText(
            this.textRenderer, 
            hintText, 
            centerX - hintWidth / 2, 
            centerY + 100, 
            0xFF888888, 
            false
        );
    }

    private void onRegisterButtonPressed() {
        String password = this.passwordField.getText().trim();
        String confirmPassword = this.confirmPasswordField.getText().trim();
        
        // 清除之前的错误信息
        this.errorMessage = "";
        
        // 验证输入
        if (password.isEmpty() || confirmPassword.isEmpty()) {
            this.errorMessage = "密码字段不能为空";
            return;
        }
        
        if (password.length() < 6) {
            this.errorMessage = "密码长度至少6位";
            return;
        }
        
        if (!password.equals(confirmPassword)) {
            this.errorMessage = "两次输入的密码不一致";
            return;
        }
        
        // 发送注册请求到后端，格式：REGISTER,密码,重复密码
        String registerMessage = "REGISTER," + password + "," + confirmPassword;
        AuthloginuiClient.PrismAuthPayload payload = new AuthloginuiClient.PrismAuthPayload(registerMessage);
        ClientPlayNetworking.send(payload);
        
        System.out.println("[AuthLoginUI] 发送注册请求: " + registerMessage);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // 处理Tab键在输入框之间切换
        if (keyCode == 258) { // TAB key
            if (this.passwordField.isFocused()) {
                this.setFocused(this.confirmPasswordField);
                return true;
            } else if (this.confirmPasswordField.isFocused()) {
                this.setFocused(this.passwordField);
                return true;
            }
        }
        
        // 处理回车键注册
        if (keyCode == 257) { // ENTER key
            this.onRegisterButtonPressed();
            return true;
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
    
    // 用于显示错误信息的方法
    public void setErrorMessage(String message) {
        this.errorMessage = message;
    }
    
    // 清除错误信息的方法
    public void clearErrorMessage() {
        this.errorMessage = "";
    }
}