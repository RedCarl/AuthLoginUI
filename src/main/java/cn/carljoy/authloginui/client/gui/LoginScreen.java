package cn.carljoy.authloginui.client.gui;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import cn.carljoy.authloginui.client.AuthloginuiClient;

public class LoginScreen extends Screen {
    private TextFieldWidget passwordField;
    private ButtonWidget loginButton;
    private static final int FIELD_WIDTH = 200;
    private static final int FIELD_HEIGHT = 20;
    private static final int BUTTON_WIDTH = 200;
    private static final int BUTTON_HEIGHT = 20;

    public LoginScreen() {
        super(Text.literal("登录"));
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
                centerY - 30,
                FIELD_WIDTH,
                FIELD_HEIGHT,
                Text.literal("密码"));
        this.passwordField.setPlaceholder(Text.literal("请输入密码"));
        this.passwordField.setMaxLength(128);
        // 设置为密码模式（隐藏输入内容）
        this.passwordField.setRenderTextProvider((string, firstCharacterIndex) -> {
            return Text.literal("*".repeat(string.length())).asOrderedText();
        });
        this.addSelectableChild(this.passwordField);

        // 登录按钮
        this.loginButton = ButtonWidget.builder(
                Text.literal("登录"),
                button -> this.onLoginButtonPressed())
                .dimensions(centerX - BUTTON_WIDTH / 2, centerY + 10, BUTTON_WIDTH, BUTTON_HEIGHT)
                .build();
        this.addDrawableChild(this.loginButton);

        // 设置焦点到密码输入框
        this.setInitialFocus(this.passwordField);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // 渲染背景
        this.renderBackground(context, mouseX, mouseY, delta);

        // 渲染标题
        context.drawCenteredTextWithShadow(
                this.textRenderer,
                this.title,
                this.width / 2,
                this.height / 2 - 60,
                0xFFFFFF);

        // 渲染密码标签
        context.drawTextWithShadow(
                this.textRenderer,
                Text.literal("密码:"),
                this.width / 2 - FIELD_WIDTH / 2,
                this.height / 2 - 45,
                0xFFFFFF);

        super.render(context, mouseX, mouseY, delta);
    }

    private void onLoginButtonPressed() {
        String password = this.passwordField.getText().trim();

        if (password.isEmpty()) {
            // 可以在这里添加错误提示
            return;
        }

        // 发送登录请求到后端，格式：LOGIN,密码
        String loginMessage = "LOGIN," + password;
        AuthloginuiClient.PrismAuthPayload payload = new AuthloginuiClient.PrismAuthPayload(loginMessage);
        ClientPlayNetworking.send(payload);

        System.out.println("[AuthLoginUI] 发送登录请求: " + loginMessage);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // 处理回车键登录
        if (keyCode == 257) { // ENTER key
            this.onLoginButtonPressed();
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
}