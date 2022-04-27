package net.minecraft.client.model;

import dev.africa.pandaware.Client;
import dev.africa.pandaware.impl.module.render.ChestESPModule;
import dev.africa.pandaware.impl.ui.UISettings;
import dev.africa.pandaware.utils.render.ColorUtils;
import org.lwjgl.opengl.GL11;

public class ModelChest extends ModelBase
{
    /** The chest lid in the chest's model. */
    public ModelRenderer chestLid = (new ModelRenderer(this, 0, 0)).setTextureSize(64, 64);

    /** The model of the bottom of the chest. */
    public ModelRenderer chestBelow;

    /** The chest's knob in the chest model. */
    public ModelRenderer chestKnob;

    public ModelChest()
    {
        this.chestLid.addBox(0.0F, -5.0F, -14.0F, 14, 5, 14, 0.0F);
        this.chestLid.rotationPointX = 1.0F;
        this.chestLid.rotationPointY = 7.0F;
        this.chestLid.rotationPointZ = 15.0F;
        this.chestKnob = (new ModelRenderer(this, 0, 0)).setTextureSize(64, 64);
        this.chestKnob.addBox(-1.0F, -2.0F, -15.0F, 2, 4, 1, 0.0F);
        this.chestKnob.rotationPointX = 8.0F;
        this.chestKnob.rotationPointY = 7.0F;
        this.chestKnob.rotationPointZ = 15.0F;
        this.chestBelow = (new ModelRenderer(this, 0, 19)).setTextureSize(64, 64);
        this.chestBelow.addBox(0.0F, 0.0F, 0.0F, 14, 10, 14, 0.0F);
        this.chestBelow.rotationPointX = 1.0F;
        this.chestBelow.rotationPointY = 6.0F;
        this.chestBelow.rotationPointZ = 1.0F;
    }

    /**
     * This method renders out all parts of the chest model.
     */
    public void renderAll()
    {
        this.chestKnob.rotateAngleX = this.chestLid.rotateAngleX;

        ChestESPModule chestESP = Client.getInstance().getModuleManager().getByClass(ChestESPModule.class);
        if (chestESP.getData().isEnabled()) {
            GL11.glPushMatrix();
            GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
            GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL);
            GL11.glDisable(GL11.GL_TEXTURE_2D);
            GL11.glDisable(GL11.GL_LIGHTING);
            GL11.glDisable(GL11.GL_DEPTH_TEST);
            GL11.glEnable(GL11.GL_LINE_SMOOTH);
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            GL11.glLineWidth(1);

            ColorUtils.glColor(UISettings.CURRENT_COLOR);
            this.chestLid.render(0.0625f);
            this.chestKnob.render(0.0625F);
            this.chestBelow.render(0.0625F);

            GL11.glPopAttrib();
            GL11.glPopMatrix();
        } else {
            this.chestLid.render(0.0625F);
            this.chestKnob.render(0.0625F);
            this.chestBelow.render(0.0625F);
        }
    }
}
