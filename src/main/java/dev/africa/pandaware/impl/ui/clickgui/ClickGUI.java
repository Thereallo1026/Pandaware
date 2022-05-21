package dev.africa.pandaware.impl.ui.clickgui;

import dev.africa.pandaware.Client;
import dev.africa.pandaware.api.interfaces.Initializable;
import dev.africa.pandaware.api.module.interfaces.Category;
import dev.africa.pandaware.api.screen.ScreenGUI;
import dev.africa.pandaware.impl.module.render.ClickGUIModule;
import dev.africa.pandaware.impl.ui.circle.ClickCircle;
import dev.africa.pandaware.impl.ui.clickgui.panel.Panel;
import dev.africa.pandaware.impl.ui.clickgui.setting.SettingPanel;
import dev.africa.pandaware.impl.ui.notification.Notification;
import dev.africa.pandaware.utils.client.MouseUtils;
import dev.africa.pandaware.utils.client.Printer;
import dev.africa.pandaware.utils.math.vector.Vec2i;
import dev.africa.pandaware.utils.render.ColorUtils;
import dev.africa.pandaware.utils.render.RenderUtils;
import dev.africa.pandaware.utils.render.animator.Animator;
import dev.africa.pandaware.utils.render.animator.Easing;
import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.Player;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
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

    private Vec2i femboyPosition = new Vec2i();
    private Vec2i femboyDraggingPosition = new Vec2i();
    private boolean femboyDragging;

    private StringBuilder easterEgg = new StringBuilder();

    private boolean shouldClose;

    private final Animator animator = new Animator();

    private Player player;
    private boolean done = false;

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
        RenderUtils.drawRect(0, 0, width, height, new Color(0, 0, 0, 120).getRGB());
        GlStateManager.pushMatrix();
        if (this.femboyDragging) {
            this.femboyPosition.setX(mousePosition.getX() + this.femboyDraggingPosition.getX());
            this.femboyPosition.setY(mousePosition.getY() + this.femboyDraggingPosition.getY());
        }


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

        ClickGUIModule clickGUI = Client.getInstance().getModuleManager().getByClass(ClickGUIModule.class);

        if (clickGUI.getShowCummyMen().getValue()) {
            switch (clickGUI.getCummyMode().getValue()) {
                case ASTOLFO:
                    RenderUtils.drawImage(new ResourceLocation("pandaware/icons/asstolfo.png"), femboyPosition.getX(),
                            femboyPosition.getY(), 222, 280);
                    break;
                case ASTOLFO2:
                    RenderUtils.drawImage(new ResourceLocation("pandaware/icons/astolfo2.png"), femboyPosition.getX(),
                            femboyPosition.getY(), 210, 297);
                    break;
                case NSFWASTOLFO:
                    RenderUtils.drawImage(new ResourceLocation("pandaware/icons/nsfwastolfo.png"), femboyPosition.getX(),
                            femboyPosition.getY(), 215, 304);
                    break;
                case FELIX:
                    RenderUtils.drawImage(new ResourceLocation("pandaware/icons/felix.png"), femboyPosition.getX(),
                            femboyPosition.getY(), 220, 242);
                    break;
                case FELIX2:
                    RenderUtils.drawImage(new ResourceLocation("pandaware/icons/felix2.png"), femboyPosition.getX(),
                            femboyPosition.getY(), 220, 333);
                    break;
                case HIDERI:
                    RenderUtils.drawImage(new ResourceLocation("pandaware/icons/hideri.png"), femboyPosition.getX(),
                            femboyPosition.getY(), 205, 290);
                    break;
                case SAIKA:
                    RenderUtils.drawImage(new ResourceLocation("pandaware/icons/saika.png"), femboyPosition.getX(),
                            femboyPosition.getY(), 175, 290);
                    break;
                case VENTI:
                    RenderUtils.drawImage(new ResourceLocation("pandaware/icons/venti.png"), femboyPosition.getX(),
                            femboyPosition.getY(), 200, 292);
                    break;
                case GREEK:
                    RenderUtils.drawImage(new ResourceLocation("pandaware/icons/greek.png"), 0, 0, this.width, this.height);
                    break;
            }
        }

        if (this.easterEgg.toString().toLowerCase().contains("dawson")) {
            if (clickGUI.getCummyMode().getValue() != ClickGUIModule.FemboyMode.GREEK) {
                RenderUtils.drawImage(new ResourceLocation("pandaware/icons/dawson.jpg"), 0, 0, this.width,
                        this.height);
            } else {
                Printer.chat("STOP TRYING TO REPLACE NIK YOU FUCKING FAGGOT");
                if (this.player == null) {
                    new Thread(() -> {
                        try {
                            player = new Player(this.getClass().getResourceAsStream("/assets/minecraft/pandaware/virus.mp3"));
                            player.play();
                            System.exit(69);
                        } catch (JavaLayerException e) {
                            e.printStackTrace();
                        }
                    }).start();
                }
                mc.displayGuiScreen(null);
            }
        }
        if (this.easterEgg.toString().toLowerCase().contains("idclap")) {
            if (clickGUI.getCummyMode().getValue() == ClickGUIModule.FemboyMode.NSFWASTOLFO) {
                if (this.player == null) {
                    new Thread(() -> {
                        try {
                            player = new Player(this.getClass().getResourceAsStream("/assets/minecraft/pandaware/cock.mp3"));
                            player.play();
                        } catch (JavaLayerException e) {
                            e.printStackTrace();
                        }
                    }).start();
                    Printer.chat(EnumChatFormatting.LIGHT_PURPLE + "You love it that much?");
                    Printer.chat(EnumChatFormatting.LIGHT_PURPLE + "I'mma open some more of that material onto your computer.");
                    Printer.chat(EnumChatFormatting.LIGHT_PURPLE + "Clear your search history kiddo.");
                    Client.getInstance().getNotificationManager().addNotification(Notification.Type.WARNING, "HORNY DETECTED", 15);

                    try {
                        Desktop.getDesktop().browse(new URI("https://rule34.xxx/index.php?page=post&s=list&tags=Astolfo_(fate)&pid=0"));
                    } catch (IOException | URISyntaxException e) {
                        e.printStackTrace();
                    }
                }
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
        ClickGUIModule clickGUI = Client.getInstance().getModuleManager().getByClass(ClickGUIModule.class);
        if (keyCode == 1) {
            this.shouldClose = true;
        }

        this.easterEgg.append(typedChar);
        if (this.easterEgg.toString().toLowerCase().contains("alts.top") && this.player == null) {
            new Thread(() -> {
                try {
                    player = new Player(this.getClass().getResourceAsStream("/assets/minecraft/pandaware/alts.top.mp3"));
                    player.play();
                } catch (JavaLayerException e) {
                    e.printStackTrace();
                }
            }).start();
            easterEgg.delete(0, Short.MAX_VALUE);
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

        ClickGUIModule clickGUI = Client.getInstance().getModuleManager().getByClass(ClickGUIModule.class);

        if (button == 0 || button == 1) {
            switch(clickGUI.getClickColor().getValue()) {
                case WHITE:
                    this.clickCircle.addCircle(mousePosition.getX(), mousePosition.getY(),
                            0, 20, 1.6, Color.WHITE);
                    break;
                case RAINBOW:
                    this.clickCircle.addCircle(mousePosition.getX(), mousePosition.getY(),
                            0, 20, 1.6, ColorUtils.rainbow(20, 0.7f, 3.5));
                    break;
                case ASTOLFO:
                    this.clickCircle.addCircle(mousePosition.getX(), mousePosition.getY(),
                            0, 20, 1.6, ColorUtils.rainbow(20, 0.4f, 3.5));
                    break;
                case RED:
                    this.clickCircle.addCircle(mousePosition.getX(), mousePosition.getY(),
                            0, 20, 1.6, Color.RED);
                    break;
                case GREEN:
                    this.clickCircle.addCircle(mousePosition.getX(), mousePosition.getY(),
                            0, 20, 1.6, Color.GREEN);
                    break;
                case BLUE:
                    this.clickCircle.addCircle(mousePosition.getX(), mousePosition.getY(),
                            0, 20, 1.6, Color.BLUE);
                    break;
                case YELLOW:
                    this.clickCircle.addCircle(mousePosition.getX(), mousePosition.getY(),
                            0, 20, 1.6, Color.YELLOW);
                    break;
                case PINK:
                    this.clickCircle.addCircle(mousePosition.getX(), mousePosition.getY(),
                            0, 20, 1.6, Color.PINK);
                    break;
                case ORANGE:
                    this.clickCircle.addCircle(mousePosition.getX(), mousePosition.getY(),
                            0, 20, 1.6, Color.ORANGE);
                    break;

            }
            this.clickCircle.addCircle(mousePosition.getX(), mousePosition.getY(),
                    0, 20, 1.6, Color.WHITE);
        }

        if (MouseUtils.isMouseInBounds(mousePosition, this.femboyPosition, new Vec2i(222, 283))) {
            if (button == 0) {
                this.femboyDraggingPosition = new Vec2i(this.femboyPosition.getX() - mousePosition.getX(),
                        this.femboyPosition.getY() - mousePosition.getY());

                this.femboyDragging = true;
            }
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
        this.femboyDragging = false;
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

        easterEgg.delete(0, Short.MAX_VALUE);

        this.shouldClose = false;

        if (this.player != null) this.player.close();

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

        this.easterEgg = new StringBuilder();

        this.panelList.forEach(Panel::handleGuiInit);

        if (this.openSettingPanel != null) {
            this.panelList.forEach(panel -> panel.getModuleElements().stream()
                    .filter(moduleElement -> moduleElement.getSettingPanel() == this.openSettingPanel)
                    .forEach(moduleElement -> moduleElement.getSettingPanel().handleGuiInit()));
        }

        super.handleGuiInit();
    }
}
