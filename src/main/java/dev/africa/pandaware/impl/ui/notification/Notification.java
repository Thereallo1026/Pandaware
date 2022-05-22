package dev.africa.pandaware.impl.ui.notification;

import dev.africa.pandaware.Client;
import dev.africa.pandaware.impl.font.Fonts;
import dev.africa.pandaware.impl.font.renderer.TTFFontRenderer;
import dev.africa.pandaware.manager.notification.NotificationManager;
import dev.africa.pandaware.utils.math.MathUtils;
import dev.africa.pandaware.utils.math.apache.ApacheMath;
import dev.africa.pandaware.utils.render.RenderUtils;
import dev.africa.pandaware.utils.render.StencilUtils;
import dev.africa.pandaware.utils.render.animator.Animator;
import dev.africa.pandaware.utils.render.animator.Easing;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.lang3.StringUtils;

import java.awt.*;

@Getter
public class Notification {
    private final Minecraft mc = Minecraft.getMinecraft();
    private final double animationStart;
    private final double currentTime;
    private final double duration;
    private final String text;
    private final Type type;
    private final Animator xAnimator;
    private final Animator yAnimator;

    private boolean shouldAnimateBack;
    private double animationValue;
    private double xAnimation;
    private boolean animated;
    private Color typeColor;

    private double rectPosition;
    private double maxWidth;

    public Notification(Type type, String text, double duration) {
        this.type = type;
        this.text = text;

        this.animationStart = 0.062f;
        this.animationValue = animationStart * RenderUtils.fpsMultiplier();
        this.currentTime = System.currentTimeMillis();
        this.duration = (duration * 1000);
        this.xAnimation = 0;
        this.typeColor = null;
        this.animated = false;
        this.shouldAnimateBack = false;
        this.xAnimator = new Animator();
        this.yAnimator = new Animator().setValue(1);
    }

    public void render(ScaledResolution scaledResolution, int yPosition, NotificationManager notificationManager) {
        //BAR TIME
        double time = ApacheMath.abs(currentTime - System.currentTimeMillis());

        //CURRENT FONT
        TTFFontRenderer font = Fonts.getInstance().getArialBdMedium();

        //CATEGORY NAME
        String categoryName = StringUtils.capitalize(type.toString().toLowerCase());

        String categoryText = categoryName + " (" + (MathUtils.roundToDecimal(MathHelper.clamp_double((duration - time) / 1000.0, 0, duration / 1000.0), 1)) + "s)";

        //idk i forgot
        int spacing = 18;
        //TEXT LENGTH
        double textLength = ApacheMath.max(ApacheMath.max(font.getStringWidth(text) + 20, font.getStringWidth(categoryText) + 13), 130);

        this.maxWidth = textLength;

        //ANIMATION SPEED
        animationValue = animationStart * RenderUtils.fpsMultiplier();

        //RENDERING POSITION
        double rectX = scaledResolution.getScaledWidth() - (8 * xAnimation) - (spacing * xAnimation) - (textLength * xAnimation),
                rectY = scaledResolution.getScaledHeight() - 70 - 20,
                reduction = MathHelper.clamp_double(1 - (time / duration), 0, 1);

        Easing easingMode = Easing.Elastic.QUINTIC_OUT;
        xAnimator.setEase(easingMode)
                .setMin(0)
                .setMax(1)
                .setSpeed(2f);

        if (!animated) {
            xAnimator.setReversed(false);
            xAnimator.update();
            xAnimation = xAnimator.getValue();

            //SETS ANIMATED BOOLEAN
            if (xAnimation >= 1) {
                animated = true;
            }
        } else {
            //SETS BOOLEAN FOR BACKWARDS ANIMATION
            if (reduction <= 0.01) {
                shouldAnimateBack = true;
            }
        }

        //SETS BACKWARDS ANIMATION
        if (shouldAnimateBack) {
            xAnimator.setEase(Easing.QUINTIC_IN);
            xAnimator.setMin(-0.02f).setSpeed(1.7f);

            xAnimator.setReversed(true);
            xAnimator.update();
            xAnimation = xAnimator.getValue();
//            xAnimation = MathHelper.clamp_double(xAnimation -= animationValue, 0, 1);
        }

        //REMOVES NOTIFICATION AFTER THE TIME HAS PASSED
        if (shouldAnimateBack && xAnimation <= 0.02f && animated) {
            notificationManager.getItems().remove(this);
            xAnimator.reset();
        }

        //GETS ICON AND SETS COLOR
        String icon = getIcon();

        this.rectPosition = rectX;

        //RENDERS BACKGROUND

        int rounding = mc.theWorld == null ? 0 : 3;
        int alpha = 120 / 2;

        StencilUtils.stencilStage(StencilUtils.StencilStage.ENABLE_MASK);
        RenderUtils.drawRoundedRect(this.rectPosition, rectY - yPosition, textLength + spacing,
                32, rounding, (new Color(0, 0, 0, alpha)));
        StencilUtils.stencilStage(StencilUtils.StencilStage.ENABLE_DRAW);

        RenderUtils.drawRoundedRect(this.rectPosition, rectY - yPosition, textLength + spacing,
                32, rounding, new Color(10, 10, 10, alpha));

        double bar = (rectX + (textLength + spacing) * (1 - reduction));
        //RENDERS STAY TIME BAR
        RenderUtils.drawRect(this.rectPosition, rectY + 30 - yPosition, bar, rectY + 32 - yPosition, typeColor.getRGB());
        StencilUtils.stencilStage(StencilUtils.StencilStage.DISABLE);

        //RENDERS ICON
        RenderUtils.drawImage(new ResourceLocation(icon), (float) this.rectPosition + 10 - 8, (float) (rectY - yPosition) + 2, 27, 27);

        font.drawStringWithShadow("§l" + StringUtils.capitalize(this.type.name().toLowerCase()),
                (float) this.rectPosition + spacing + 3 + 10, (float) (rectY - yPosition) + 4, -1);

        //RENDER TEXTS
        font.drawStringWithShadow(text, (float) this.rectPosition + spacing + 3 + 10, (float) (rectY - yPosition) + 17, -1);
    }

    //GETS ICON FROM PICKED CATEGORY
    private String getIcon() {
        String path = Client.getInstance().getManifest().getClientName().toLowerCase() + "/icons/notification/", icon = "";

        switch (type) {
            case WARNING:
                this.typeColor = new Color(166, 238, 252, 255);
                icon = path + "warning.png";
                break;
            case INFORMATION:
                this.typeColor = new Color(249, 107, 252, 255);
                icon = path + "information.png";
                break;
            case ERROR:
                this.typeColor = new Color(255, 79, 79, 255);
                icon = path + "error.png";
                break;
            case SUCCESS:
                this.typeColor = new Color(111, 255, 102, 255);
                icon = path + "success.png";
                break;
            case SPOTIFY:
                this.typeColor = new Color(30, 215, 96, 255);
                icon = Client.getInstance().getManifest().getClientName() + "/icons/spotify.png";
                break;
        }

        return icon;
    }

    //CATEGORY ENUMS
    public enum Type {
        WARNING, INFORMATION, ERROR, SUCCESS, SPOTIFY
    }
}
