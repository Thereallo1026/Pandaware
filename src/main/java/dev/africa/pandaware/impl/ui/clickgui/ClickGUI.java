package dev.africa.pandaware.impl.ui.clickgui;

import dev.africa.pandaware.api.interfaces.Initializable;
import dev.africa.pandaware.api.module.interfaces.Category;
import dev.africa.pandaware.api.screen.ScreenGUI;
import dev.africa.pandaware.impl.ui.circle.ClickCircle;
import dev.africa.pandaware.impl.ui.clickgui.panel.Panel;
import dev.africa.pandaware.impl.ui.clickgui.setting.SettingPanel;
import dev.africa.pandaware.utils.math.vector.Vec2i;
import dev.africa.pandaware.utils.render.animator.Animator;
import dev.africa.pandaware.utils.render.animator.Easing;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.MathHelper;

import java.awt.*;
import java.util.LinkedList;
import java.util.List;

@Setter
@Getter
public class ClickGUI extends ScreenGUI implements Initializable {
    private final List<Panel> panelList = new LinkedList<>();
    private final ClickCircle clickCircle = new ClickCircle();

    private SettingPanel openSettingPanel;

    private Vec2i settingPanelPosition;
    private Vec2i settingPanelDraggingPosition;
    private boolean settingPanelDragging;
    private boolean settingPanelFirstOpen;

    private boolean shouldClose;

    private final Animator animator = new Animator();

    @Override
    public void init() {
        int x = 25, y = 25;

        for (Category value : Category.values()) {
            this.panelList.add(new Panel(
                    this, value,
                    new Vec2i(x, y),
                    new Vec2i(130, 300)
            ));

            x += 140;
        }
    }

    @Override
    public void handleRender(Vec2i mousePosition, float pTicks) {
        GlStateManager.pushMatrix();

        this.animator.setEase(this.shouldClose ? Easing.QUINTIC_OUT : Easing.QUINTIC_IN).setSpeed(3).setMin(0)
                .setReversed(!this.shouldClose).setMax(1).update();

        double translate = this.height * this.animator.getValue();
        if (this.animator.getValue() < 1) {
            GlStateManager.translate(0, translate, 0);
        }

        if (this.animator.getValue() >= 0.8 && this.shouldClose) {
            this.handleGuiClose();

            mc.displayGuiScreen(null);

            if (mc.currentScreen == null) {
                mc.setIngameFocus();
            }
        }

        this.panelList.forEach(panel -> panel.handleRender(mousePosition, pTicks));

        if (this.openSettingPanel != null) {
            this.panelList.forEach(panel -> panel.getModuleElements().stream()
                    .filter(moduleElement -> {
                        if (this.settingPanelDragging) {
                            moduleElement.getSettingPanel().setPosition(new Vec2i(
                                    mousePosition.getX() + this.settingPanelDraggingPosition.getX(),
                                    mousePosition.getY() + this.settingPanelDraggingPosition.getY()
                            ));

                            this.settingPanelPosition = moduleElement.getSettingPanel().getPosition();
                        }

                        moduleElement.getSettingPanel().setPosition(new Vec2i(
                                MathHelper.clamp_int(
                                        moduleElement.getSettingPanel().getPosition().getX(),
                                        0, this.width - 20
                                ),
                                MathHelper.clamp_int(
                                        moduleElement.getSettingPanel().getPosition().getY(),
                                        0, this.height - 20
                                )
                        ));

                        return moduleElement.getSettingPanel() == this.openSettingPanel;
                    }).forEach(moduleElement -> moduleElement.getSettingPanel().handleRender(mousePosition, pTicks)));
        }

        GlStateManager.popMatrix();

        this.clickCircle.render();
    }

    @Override
    public void handleKeyboard(char typedChar, int keyCode) {
        if (keyCode == 1) {
            this.shouldClose = true;
        }

        this.panelList.forEach(panel -> panel.handleKeyboard(typedChar, keyCode));

        if (this.openSettingPanel != null) {
            this.panelList.forEach(panel -> panel.getModuleElements().stream()
                    .filter(moduleElement -> moduleElement.getSettingPanel() == this.openSettingPanel)
                    .forEach(moduleElement -> moduleElement.getSettingPanel().handleKeyboard(typedChar, keyCode)));
        }

        super.handleKeyboard(typedChar, keyCode);
    }

    @Override
    public void handleClick(Vec2i mousePosition, int button) {
        if (button == 0 || button == 1) {
            this.clickCircle.addCircle(mousePosition.getX(), mousePosition.getY(),
                    0, 20, 1.6, Color.WHITE);
        }

        this.panelList.forEach(panel -> {
            panel.handleClick(mousePosition, button);

            panel.getModuleElements().stream()
                    .filter(moduleElement -> moduleElement.getSettingPanel() == this.openSettingPanel)
                    .forEach(moduleElement -> moduleElement.getSettingPanel().handleClick(mousePosition, button));
        });

        super.handleClick(mousePosition, button);
    }

    @Override
    public void handleRelease(Vec2i mousePosition, int state) {
        this.panelList.forEach(panel -> panel.handleRelease(mousePosition, state));

        if (this.openSettingPanel != null) {
            this.panelList.forEach(panel -> panel.getModuleElements().stream()
                    .filter(moduleElement -> moduleElement.getSettingPanel() == this.openSettingPanel)
                    .forEach(moduleElement -> moduleElement.getSettingPanel().handleRelease(mousePosition, state)));
        }

        super.handleRelease(mousePosition, state);
    }

    @Override
    public void handleScreenUpdate() {
        this.panelList.forEach(Panel::handleScreenUpdate);

        if (this.openSettingPanel != null) {
            this.panelList.forEach(panel -> panel.getModuleElements().stream()
                    .filter(moduleElement -> moduleElement.getSettingPanel() == this.openSettingPanel)
                    .forEach(moduleElement -> moduleElement.getSettingPanel().handleScreenUpdate()));
        }

        super.handleScreenUpdate();
    }

    @Override
    public void handleGuiClose() {
        this.shouldClose = false;

        this.animator.resetMax();

        this.panelList.forEach(Panel::handleGuiClose);

        if (this.openSettingPanel != null) {
            this.panelList.forEach(panel -> panel.getModuleElements().stream()
                    .filter(moduleElement -> moduleElement.getSettingPanel() == this.openSettingPanel)
                    .forEach(moduleElement -> moduleElement.getSettingPanel().handleGuiClose()));
        }

        super.handleGuiClose();
    }

    @Override
    public void handleGuiInit() {
        this.shouldClose = false;

        this.animator.resetMax();

        this.panelList.forEach(Panel::handleGuiInit);

        if (this.openSettingPanel != null) {
            this.panelList.forEach(panel -> panel.getModuleElements().stream()
                    .filter(moduleElement -> moduleElement.getSettingPanel() == this.openSettingPanel)
                    .forEach(moduleElement -> moduleElement.getSettingPanel().handleGuiInit()));
        }

        super.handleGuiInit();
    }
}
