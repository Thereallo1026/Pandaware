package dev.africa.pandaware.impl.ui.shader;

import lombok.Getter;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.shader.Framebuffer;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

import java.awt.*;

@Getter
public abstract class FramebufferShader extends Shader {
    private Framebuffer framebuffer;
    private float red;
    private float green;
    private float blue;
    private float alpha = 1.0f;
    private float radius = 2.0f;
    private float quality = 1.0f;
    private boolean entityShadows;

    private boolean frameBufferSet;

    public FramebufferShader(String fragmentShader) {
        super(fragmentShader);
    }

    public void startDraw(float partialTicks) {
        GlStateManager.enableAlpha();
        GlStateManager.pushMatrix();
        GlStateManager.pushAttrib();

        if (!this.frameBufferSet) {
            this.frameBufferSet = true;

            this.framebuffer = this.setupFrameBuffer(this.framebuffer);
        }

        this.framebuffer.framebufferClear();
        this.framebuffer.bindFramebuffer(true);

        this.entityShadows = mc.gameSettings.field_181151_V;

        mc.gameSettings.field_181151_V = false;
        mc.entityRenderer.setupCameraTransform(partialTicks, 0);
    }

    public void stopDraw(Color color, float radius, float quality) {
        mc.gameSettings.field_181151_V = this.entityShadows;

        GL11.glEnable(3042);
        GL11.glBlendFunc(770, 771);

        mc.getFramebuffer().bindFramebuffer(true);

        this.red = (float) color.getRed() / 255.0f;
        this.green = (float) color.getGreen() / 255.0f;
        this.blue = (float) color.getBlue() / 255.0f;
        this.alpha = (float) color.getAlpha() / 255.0f;
        this.radius = radius;
        this.quality = quality;

        mc.entityRenderer.disableLightmap();

        RenderHelper.disableStandardItemLighting();

        this.startShader();

        mc.entityRenderer.setupOverlayRendering();

        this.drawFramebuffer(this.framebuffer);
        this.stopShader();

        mc.entityRenderer.disableLightmap();

        GlStateManager.popMatrix();
        GlStateManager.popAttrib();
    }

    public Framebuffer setupFrameBuffer(Framebuffer frameBuffer) {
        if (frameBuffer != null) {
            frameBuffer.deleteFramebuffer();
        }

        frameBuffer = new Framebuffer(mc.displayWidth, mc.displayHeight, true);

        return frameBuffer;
    }

    public void drawFramebuffer(Framebuffer framebuffer) {
        ScaledResolution scaledResolution = new ScaledResolution(mc);

        GL11.glBindTexture(3553, framebuffer.framebufferTexture);
        GL11.glBegin(7);
        GL11.glTexCoord2d(0.0, 1.0);
        GL11.glVertex2d(0.0, 0.0);
        GL11.glTexCoord2d(0.0, 0.0);
        GL11.glVertex2d(0.0, scaledResolution.getScaledHeight());
        GL11.glTexCoord2d(1.0, 0.0);
        GL11.glVertex2d(scaledResolution.getScaledWidth(), scaledResolution.getScaledHeight());
        GL11.glTexCoord2d(1.0, 1.0);
        GL11.glVertex2d(scaledResolution.getScaledWidth(), 0.0);
        GL11.glEnd();
        GL20.glUseProgram(0);
    }
}

