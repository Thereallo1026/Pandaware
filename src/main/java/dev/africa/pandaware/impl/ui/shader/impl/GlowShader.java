package dev.africa.pandaware.impl.ui.shader.impl;

import dev.africa.pandaware.api.interfaces.MinecraftInstance;
import dev.africa.pandaware.impl.ui.shader.FramebufferShader;
import org.lwjgl.opengl.GL20;

public class GlowShader extends FramebufferShader {
    public static final GlowShader INSTANCE = new GlowShader();

    public GlowShader() {
        super("glow.frag");
    }

    @Override
    public void setupUniforms() {
        this.setupUniform("texture");
        this.setupUniform("texelSize");
        this.setupUniform("color");
        this.setupUniform("divider");
        this.setupUniform("radius");
        this.setupUniform("maxSample");
    }

    @Override
    public void updateUniforms() {
        GL20.glUniform1i(this.getUniform("texture"), 0);
        GL20.glUniform2f(this.getUniform("texelSize"), 1F / mc.displayWidth * (this.getRadius() * this.getQuality()),
                1F / mc.displayHeight * (this.getRadius() * this.getQuality()));
        GL20.glUniform3f(this.getUniform("color"), this.getRed(), this.getGreen(), this.getBlue());
        GL20.glUniform1f(this.getUniform("divider"), 140F);
        GL20.glUniform1f(this.getUniform("radius"), this.getRadius());
        GL20.glUniform1f(this.getUniform("maxSample"), 10F);
    }
}