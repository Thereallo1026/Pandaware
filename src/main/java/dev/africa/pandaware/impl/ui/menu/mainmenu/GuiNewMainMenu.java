package dev.africa.pandaware.impl.ui.menu.mainmenu;

import com.viaversion.viabackwards.api.entities.storage.EntityData;
import dev.africa.pandaware.Client;
import dev.africa.pandaware.impl.font.Fonts;
import dev.africa.pandaware.impl.font.renderer.TTFFontRenderer;
import dev.africa.pandaware.impl.ui.menu.button.CustomButton;
import dev.africa.pandaware.impl.ui.menu.button.CustomInvButton;
import dev.africa.pandaware.impl.ui.notification.Notification;
import dev.africa.pandaware.utils.math.TimeHelper;
import dev.africa.pandaware.utils.render.RenderUtils;
import net.minecraft.client.gui.*;
import net.minecraft.util.ResourceLocation;

import java.awt.*;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class GuiNewMainMenu extends GuiScreen {

    // TODO: Needs Improvement

    private TTFFontRenderer smallFont = Fonts.getInstance().getProductSansMedium();
    private TTFFontRenderer normFont = Fonts.getInstance().getProductSansMedium();
    private TTFFontRenderer bigFont = Fonts.getInstance().getProductSansBig();
    private TTFFontRenderer hugeFont = Fonts.getInstance().getProductSansVeryBig();

    @Override
    public void initGui() {
        try {
            smallFont = Fonts.getInstance().getProductSansMedium();
            normFont = Fonts.getInstance().getProductSansMedium();
            bigFont = Fonts.getInstance().getProductSansBig();
            hugeFont = Fonts.getInstance().getProductSansVeryBig();
        } catch (Exception e) {
            e.printStackTrace();
        }
        customButtons.add(new CustomButton(0, width / 2f - 85, 130, 175, 17, "Singleplayer", true));
        customButtons.add(new CustomButton(1, width / 2f - 85, 150, 175, 17, "Multiplayer", true));
        customButtons.add(new CustomButton(2, width / 2f - 85, 170, 175, 17, "Alt Manager", true));
        customButtons.add(new CustomButton(3, width / 2f - 85, 190, 175, 17, "Settings", true));
        customButtons.add(new CustomButton(4, width / 2f - 85, 210, 175, 17, "Quit", true));

        super.initGui();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.initGui();
        this.drawDefaultBackground();

        RenderUtils.drawHorizontalGradientRect(0, 0, width, height, new Color(156, 54, 97, 215), new Color(41, 76, 91, 100));

        RenderUtils.drawImage(new ResourceLocation("/pandaware/icons/panda.png"), width / 2f - 105, 35, 204, 90);

        hugeFont.drawCenteredRainbowStringWithShadow(Client.getInstance().getManifest().getClientName() + "   ", width / 2f, 20, 3, 3);
        normFont.drawCenteredStringWithShadow(Client.getInstance().getManifest().getClientVersion(), width / 2f + 40, 22, -1);

        smallFont.drawCenteredStringWithShadow("By Anticheat Alert & Others.", width / 2f, 10, -1);

        for (GuiButton guiButton : this.buttonList) {
            ((GuiButton) guiButton).drawButton(this.mc, mouseX, mouseY);
        }

        for (CustomButton customButton : this.customButtons) {
            ((CustomButton) customButton).drawButton(this.mc, mouseX, mouseY);
        }

        for (GuiLabel guiLabel : this.labelList) {
            ((GuiLabel) guiLabel).drawLabel(this.mc, mouseX, mouseY);
        }
    }

    @Override
    public void onGuiClosed() {
        Client.getInstance().getFileManager().saveAll();
        super.onGuiClosed();
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
    }

    @Override
    protected void actionPerformed(CustomButton button) throws IOException {
        switch (button.id) {
            case 0:
                mc.displayGuiScreen(new GuiSelectWorld(this));
                break;
            case 1:
                mc.displayGuiScreen(new GuiMultiplayer(this));
                break;
            case 2:
                mc.displayGuiScreen(Client.getInstance().getGuiAccountManager());
                break;
            case 3:
                mc.displayGuiScreen(new GuiOptions(this, this.mc.gameSettings));
                break;
            case 4:
                mc.shutdown();
                break;
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);

    }

    public static void wait(int ms)
    {
        try
        {
            Thread.sleep(ms);
        }
        catch(InterruptedException ex)
        {
            Thread.currentThread().interrupt();
        }
    }
}