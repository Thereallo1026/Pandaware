package dev.africa.pandaware.impl.cape.layer;

import dev.africa.pandaware.impl.cape.simulation.CapeHolder;
import dev.africa.pandaware.impl.cape.simulation.StickSimulation;
import dev.africa.pandaware.utils.render.matrix.Matrix4f;
import dev.africa.pandaware.utils.render.matrix.PoseStack;
import dev.africa.pandaware.utils.render.matrix.Vector3f;
import dev.africa.pandaware.utils.render.matrix.Vector4f;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;

public class SmoothCapeRenderer {
    public void renderSmoothCape(CustomCapeRenderLayer layer, AbstractClientPlayer abstractClientPlayer, float delta) {
        WorldRenderer worldrenderer = Tessellator.getInstance().getWorldRenderer();
        worldrenderer.begin(7, DefaultVertexFormats.field_181710_j);
        PoseStack poseStack = new PoseStack();
        poseStack.pushPose();

        Matrix4f oldPositionMatrix = null;
        for (int part = 0; part < CustomCapeRenderLayer.partCount; part++) {
            modifyPoseStack(layer, poseStack, abstractClientPlayer, delta, part);

            if (oldPositionMatrix == null) {
                oldPositionMatrix = poseStack.last().pose();
            }

            if (part == 0) {
                addTopVertex(worldrenderer, poseStack.last().pose(), oldPositionMatrix,
                        0.3F,
                        0,
                        0F,
                        -0.3F,
                        0,
                        -0.06F, part);
            }

            if (part == CustomCapeRenderLayer.partCount - 1) {
                addBottomVertex(worldrenderer, poseStack.last().pose(), poseStack.last().pose(),
                        0.3F,
                        (part + 1) * (0.96F / CustomCapeRenderLayer.partCount),
                        0F,
                        -0.3F,
                        (part + 1) * (0.96F / CustomCapeRenderLayer.partCount),
                        -0.06F, part);
            }

            addLeftVertex(worldrenderer, poseStack.last().pose(), oldPositionMatrix,
                    -0.3F,
                    (part + 1) * (0.96F / CustomCapeRenderLayer.partCount),
                    0F,
                    -0.3F,
                    part * (0.96F / CustomCapeRenderLayer.partCount),
                    -0.06F, part);

            addRightVertex(worldrenderer, poseStack.last().pose(), oldPositionMatrix,
                    0.3F,
                    (part + 1) * (0.96F / CustomCapeRenderLayer.partCount),
                    0F,
                    0.3F,
                    part * (0.96F / CustomCapeRenderLayer.partCount),
                    -0.06F, part);

            addBackVertex(worldrenderer, poseStack.last().pose(), oldPositionMatrix,
                    0.3F,
                    (part + 1) * (0.96F / CustomCapeRenderLayer.partCount),
                    -0.06F,
                    -0.3F,
                    part * (0.96F / CustomCapeRenderLayer.partCount),
                    -0.06F, part);

            addFrontVertex(worldrenderer, oldPositionMatrix, poseStack.last().pose(),
                    0.3F,
                    (part + 1) * (0.96F / CustomCapeRenderLayer.partCount),
                    0F,
                    -0.3F,
                    part * (0.96F / CustomCapeRenderLayer.partCount),
                    0F, part);

            oldPositionMatrix = poseStack.last().pose();
            poseStack.popPose();
        }
        Tessellator.getInstance().draw();
    }

    void modifyPoseStack(CustomCapeRenderLayer layer, PoseStack poseStack, AbstractClientPlayer abstractClientPlayer, float h, int part) {
        modifyPoseStackSimulation(layer, poseStack, abstractClientPlayer, h, part);
    }

    private void modifyPoseStackSimulation(CustomCapeRenderLayer layer, PoseStack poseStack, AbstractClientPlayer abstractClientPlayer, float delta, int part) {
        StickSimulation simulation = ((CapeHolder) abstractClientPlayer).getSimulation();
        poseStack.pushPose();
        poseStack.translate(0.0D, 0.0D, 0.125D);

        float z = simulation.points.get(part).getLerpX(delta) - simulation.points.get(0).getLerpX(delta);
        if (z > 0) {
            z = 0;
        }
        float y = simulation.points.get(0).getLerpY(delta) - part - simulation.points.get(part).getLerpY(delta);

        float sidewaysRotationOffset = 0;
        float partRotation = (float) -Math.atan2(y, z);
        partRotation = Math.max(partRotation, 0);
        if (partRotation != 0)
            partRotation = (float) (Math.PI - partRotation);
        partRotation *= 57.2958;
        partRotation *= 2;

        float height = 0;
        if (abstractClientPlayer.isSneaking()) {
            height += 25.0F;
            poseStack.translate(0, 0.15F, 0);
        }

        float naturalWindSwing = layer.getNatrualWindSwing(part);


        poseStack.mulPose(Vector3f.XP.rotationDegrees(6.0F + height + naturalWindSwing));
        poseStack.mulPose(Vector3f.ZP.rotationDegrees(sidewaysRotationOffset / 2.0F));
        poseStack.mulPose(Vector3f.YP.rotationDegrees(180.0F - sidewaysRotationOffset / 2.0F));
        poseStack.translate(0, y / CustomCapeRenderLayer.partCount, z / CustomCapeRenderLayer.partCount);
        poseStack.translate(0, /*-offset*/ +(0.48 / 16), -(0.48 / 16));
        poseStack.translate(0, part * 1f / CustomCapeRenderLayer.partCount, 0);
        poseStack.mulPose(Vector3f.XP.rotationDegrees(-partRotation));
        poseStack.translate(0, -part * 1f / CustomCapeRenderLayer.partCount, -part * (0) / CustomCapeRenderLayer.partCount);
        poseStack.translate(0, -(0.48 / 16), (0.48 / 16));
    }

    private static void addBackVertex(WorldRenderer worldrenderer, Matrix4f matrix, Matrix4f oldMatrix, float x1, float y1, float z1, float x2, float y2, float z2, int part) {
        float i;
        Matrix4f k;
        if (x1 < x2) {
            i = x1;
            x1 = x2;
            x2 = i;
        }

        if (y1 < y2) {
            i = y1;
            y1 = y2;
            y2 = i;

            k = matrix;
            matrix = oldMatrix;
            oldMatrix = k;
        }

        float minU = .015625F;
        float maxU = .171875F;

        float minV = .03125F;
        float maxV = .53125F;

        float deltaV = maxV - minV;
        float vPerPart = deltaV / CustomCapeRenderLayer.partCount;
        maxV = minV + (vPerPart * (part + 1));
        minV = minV + (vPerPart * part);


        vertex(worldrenderer, oldMatrix, x1, y2, z1).tex(maxU, minV).normal(1, 0, 0).endVertex();
        vertex(worldrenderer, oldMatrix, x2, y2, z1).tex(minU, minV).normal(1, 0, 0).endVertex();

        vertex(worldrenderer, matrix, x2, y1, z2).tex(minU, maxV).normal(1, 0, 0).endVertex();
        vertex(worldrenderer, matrix, x1, y1, z2).tex(maxU, maxV).normal(1, 0, 0).endVertex();
    }

    private static void addFrontVertex(WorldRenderer worldrenderer, Matrix4f matrix, Matrix4f oldMatrix, float x1, float y1, float z1, float x2, float y2, float z2, int part) {
        float i;
        Matrix4f k;
        if (x1 < x2) {
            i = x1;
            x1 = x2;
            x2 = i;
        }

        if (y1 < y2) {
            i = y1;
            y1 = y2;
            y2 = i;

            k = matrix;
            matrix = oldMatrix;
            oldMatrix = k;
        }

        float minU = .1875F;
        float maxU = .34375F;

        float minV = .03125F;
        float maxV = .53125F;

        float deltaV = maxV - minV;
        float vPerPart = deltaV / CustomCapeRenderLayer.partCount;
        maxV = minV + (vPerPart * (part + 1));
        minV = minV + (vPerPart * part);

        vertex(worldrenderer, oldMatrix, x1, y1, z1).tex(maxU, maxV).normal(1, 0, 0).endVertex();
        vertex(worldrenderer, oldMatrix, x2, y1, z1).tex(minU, maxV).normal(1, 0, 0).endVertex();

        vertex(worldrenderer, matrix, x2, y2, z2).tex(minU, minV).normal(1, 0, 0).endVertex();
        vertex(worldrenderer, matrix, x1, y2, z2).tex(maxU, minV).normal(1, 0, 0).endVertex();
    }

    private static void addLeftVertex(WorldRenderer worldrenderer, Matrix4f matrix, Matrix4f oldMatrix, float x1, float y1, float z1, float x2, float y2, float z2, int part) {
        float i;

        if (x1 < x2) {
            i = x1;

            x2 = i;
        }

        if (y1 < y2) {
            i = y1;
            y1 = y2;
            y2 = i;
        }

        float minU = 0;
        float maxU = .015625F;

        float minV = .03125F;
        float maxV = .53125F;

        float deltaV = maxV - minV;
        float vPerPart = deltaV / CustomCapeRenderLayer.partCount;
        maxV = minV + (vPerPart * (part + 1));
        minV = minV + (vPerPart * part);


        vertex(worldrenderer, matrix, x2, y1, z1).tex(maxU, maxV).normal(1, 0, 0).endVertex();
        vertex(worldrenderer, matrix, x2, y1, z2).tex(minU, maxV).normal(1, 0, 0).endVertex();

        vertex(worldrenderer, oldMatrix, x2, y2, z2).tex(minU, minV).normal(1, 0, 0).endVertex();
        vertex(worldrenderer, oldMatrix, x2, y2, z1).tex(maxU, minV).normal(1, 0, 0).endVertex();

    }

    private static void addRightVertex(WorldRenderer worldrenderer, Matrix4f matrix, Matrix4f oldMatrix, float x1, float y1, float z1, float x2, float y2, float z2, int part) {
        float i;
        if (x1 < x2) {
            i = x1;
            x2 = i;
        }

        if (y1 < y2) {
            i = y1;
            y1 = y2;
            y2 = i;
        }

        float minU = .171875F;
        float maxU = .1875F;

        float minV = .03125F;
        float maxV = .53125F;

        float deltaV = maxV - minV;
        float vPerPart = deltaV / CustomCapeRenderLayer.partCount;
        maxV = minV + (vPerPart * (part + 1));
        minV = minV + (vPerPart * part);


        vertex(worldrenderer, matrix, x2, y1, z2).tex(minU, maxV).normal(1, 0, 0).endVertex();
        vertex(worldrenderer, matrix, x2, y1, z1).tex(maxU, maxV).normal(1, 0, 0).endVertex();

        vertex(worldrenderer, oldMatrix, x2, y2, z1).tex(maxU, minV).normal(1, 0, 0).endVertex();
        vertex(worldrenderer, oldMatrix, x2, y2, z2).tex(minU, minV).normal(1, 0, 0).endVertex();

    }

    private static void addBottomVertex(WorldRenderer worldrenderer, Matrix4f matrix, Matrix4f oldMatrix, float x1, float y1, float z1, float x2, float y2, float z2, int part) {
        float i;
        if (x1 < x2) {
            i = x1;
            x1 = x2;
            x2 = i;
        }

        if (y1 < y2) {
            i = y1;
            y1 = y2;
            y2 = i;
        }

        float minU = .171875F;
        float maxU = .328125F;

        float minV = 0;
        float maxV = .03125F;

        float deltaV = maxV - minV;
        float vPerPart = deltaV / CustomCapeRenderLayer.partCount;
        maxV = minV + (vPerPart * (part + 1));
        minV = minV + (vPerPart * part);


        vertex(worldrenderer, oldMatrix, x1, y2, z2).tex(maxU, minV).normal(1, 0, 0).endVertex();
        vertex(worldrenderer, oldMatrix, x2, y2, z2).tex(minU, minV).normal(1, 0, 0).endVertex();

        vertex(worldrenderer, matrix, x2, y1, z1).tex(minU, maxV).normal(1, 0, 0).endVertex();
        vertex(worldrenderer, matrix, x1, y1, z1).tex(maxU, maxV).normal(1, 0, 0).endVertex();

    }

    private static WorldRenderer vertex(WorldRenderer worldrenderer, Matrix4f matrix4f, float f, float g, float h) {
        Vector4f vector4f = new Vector4f(f, g, h, 1.0F);
        vector4f.transform(matrix4f);
        worldrenderer.pos(vector4f.x(), vector4f.y(), vector4f.z());
        return worldrenderer;
    }

    private static void addTopVertex(WorldRenderer worldrenderer, Matrix4f matrix, Matrix4f oldMatrix, float x1, float y1, float z1, float x2, float y2, float z2, int part) {
        float i;
        if (x1 < x2) {
            i = x1;
            x1 = x2;
            x2 = i;
        }

        if (y1 < y2) {
            i = y1;
            y1 = y2;
            y2 = i;
        }

        float minU = .015625F;
        float maxU = .171875F;

        float minV = 0;
        float maxV = .03125F;

        float deltaV = maxV - minV;
        float vPerPart = deltaV / CustomCapeRenderLayer.partCount;
        maxV = minV + (vPerPart * (part + 1));
        minV = minV + (vPerPart * part);


        vertex(worldrenderer, oldMatrix, x1, y2, z1).tex(maxU, maxV).normal(0, 1, 0).endVertex();
        vertex(worldrenderer, oldMatrix, x2, y2, z1).tex(minU, maxV).normal(0, 1, 0).endVertex();

        vertex(worldrenderer, matrix, x2, y1, z2).tex(minU, minV).normal(0, 1, 0).endVertex();
        vertex(worldrenderer, matrix, x1, y1, z2).tex(maxU, minV).normal(0, 1, 0).endVertex();
    }
}
