package dev.africa.pandaware.impl.module.movement;

import dev.africa.pandaware.Client;
import dev.africa.pandaware.api.event.interfaces.EventCallback;
import dev.africa.pandaware.api.event.interfaces.EventHandler;
import dev.africa.pandaware.api.module.Module;
import dev.africa.pandaware.api.module.interfaces.Category;
import dev.africa.pandaware.api.module.interfaces.ModuleInfo;
import dev.africa.pandaware.impl.event.player.MoveEvent;
import dev.africa.pandaware.impl.event.render.RenderEvent;
import dev.africa.pandaware.impl.module.combat.KillAuraModule;
import dev.africa.pandaware.impl.module.movement.flight.FlightModule;
import dev.africa.pandaware.impl.module.movement.speed.SpeedModule;
import dev.africa.pandaware.impl.setting.BooleanSetting;
import dev.africa.pandaware.impl.setting.NumberSetting;
import dev.africa.pandaware.utils.player.PlayerUtils;
import dev.africa.pandaware.utils.player.RotationUtils;
import dev.africa.pandaware.utils.render.ColorUtils;
import lombok.Getter;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import org.lwjgl.opengl.GL11;

import java.awt.*;

@ModuleInfo(name = "Target Strafe", category = Category.MOVEMENT)
public class TargetStrafeModule extends Module {
    private Entity entityLivingBase;
    private boolean shouldStrafe, canStrafe;
    private double moveDirection = 1.0D;
    @Getter
    private static boolean strafing;

    private final NumberSetting lineWidth = new NumberSetting("Line Width", 3, 0.1, 1.8).setSaveConfig(false);
    private final NumberSetting radius = new NumberSetting("Radius", 3, 0, 1.6).setSaveConfig(false);
    private final NumberSetting slowdown = new NumberSetting("Slowdown", 1, 0.1, 1);

    private final BooleanSetting pressSpaceOnly = new BooleanSetting("Press Space Only", false).setSaveConfig(false);
    private final BooleanSetting checkVoid = new BooleanSetting("Check Void", false);

    public TargetStrafeModule() {
        this.registerSettings(
                this.lineWidth,
                this.radius,
                this.slowdown,
                this.pressSpaceOnly,
                this.checkVoid
        );
    }

    @EventHandler
    EventCallback<RenderEvent> onRender = event -> {
        if (event.getType() == RenderEvent.Type.RENDER_3D) {
            KillAuraModule killAuraModule = Client.getInstance().getModuleManager()
                    .getByClass(KillAuraModule.class);
            this.entityLivingBase = killAuraModule.getTarget();

            this.canStrafe = GameSettings.isKeyDown(mc.gameSettings.keyBindJump) || !this.pressSpaceOnly.getValue();
            this.shouldStrafe = this.entityLivingBase != null && killAuraModule.getData().isEnabled();
            if (this.shouldStrafe) {
                this.drawCircle(
                        this.entityLivingBase,
                        this.lineWidth.getValue().floatValue(),
                        this.radius.getValue().floatValue() - 0.3
                );
            }
        }
    };

    public MoveEvent editMovement(double x, double y, double z) {
        double movementSpeed = Math.sqrt(x * x + z * z) * slowdown.getValue().doubleValue();
        boolean modulesEnabled = Client.getInstance().getModuleManager()
                        .getByClass(FlightModule.class).getData().isEnabled() ||
                Client.getInstance().getModuleManager()
                        .getByClass(SpeedModule.class).getData().isEnabled();

        if (this.canStrafe && mc.isMoveMoving() && modulesEnabled && this.shouldStrafe) {
            if ((!this.checkVoid.getValue() || PlayerUtils.isBlockUnder()) && !PlayerUtils.inLiquid() &&
                    !mc.thePlayer.isOnLadder()) {
                strafing = true;

                float strafeYaw;
                double strafeSpeed;
                double strafeForward;

                if (mc.thePlayer.isCollidedHorizontally || !PlayerUtils.isBlockUnder()) {
                    if (this.moveDirection == -1) {
                        this.moveDirection = 1;
                    } else if (this.moveDirection == 1) {
                        this.moveDirection = -1;
                    }
                }

                float rotations = RotationUtils.getRotations((EntityLivingBase) this.entityLivingBase).getX();
                double distanceDouble = mc.thePlayer.getDistanceSqToEntity(this.entityLivingBase);

                double forward = strafeForward = (distanceDouble <= (this.radius.getValue().doubleValue() * 2) ? 0 : 1);
                double strafe = strafeSpeed = moveDirection;

                float yaw = strafeYaw = rotations;

                if (strafeForward != 0.0D) {
                    if (strafeSpeed > 0.0D) {
                        yaw = strafeYaw + (float) -45;
                    } else if (strafeSpeed < 0.0D) {
                        yaw = strafeYaw + (float) 45;
                    }

                    strafe = 0.0D;
                }

                if (strafe > 0.0D) {
                    strafe = 1D;
                } else if (strafe < 0.0D) {
                    strafe = -1D;
                }

                double mx = Math.cos(Math.toRadians(yaw + 90));
                double mz = Math.sin(Math.toRadians(yaw + 90));

                x = mc.thePlayer.motionX = (forward * movementSpeed * mx + strafe * movementSpeed * mz) * 0.987;
                z = mc.thePlayer.motionZ = (forward * movementSpeed * mz - strafe * movementSpeed * mx) * 0.98;
            }
        }

        return new MoveEvent(x, y, z);
    }

    private void drawCircle(Entity entity, float lineWidth, double radius) {
        GlStateManager.pushAttribAndMatrix();
        GL11.glPushMatrix();
        mc.entityRenderer.disableLightmap();
        GL11.glDisable(3553);
        GL11.glEnable(3042);
        GL11.glBlendFunc(770, 771);
        GL11.glDisable(2929);
        GL11.glEnable(2848);
        GL11.glDepthMask(false);

        double posX = entity.lastTickPosX + (entity.posX - entity.lastTickPosX)
                * mc.timer.renderPartialTicks - mc.getRenderManager().viewerPosX;
        double posY = entity.lastTickPosY + (entity.posY - entity.lastTickPosY)
                * mc.timer.renderPartialTicks - mc.getRenderManager().viewerPosY;
        double posZ = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ)
                * mc.timer.renderPartialTicks - mc.getRenderManager().viewerPosZ;

        GL11.glPushMatrix();
        GL11.glLineWidth(lineWidth);
        GL11.glBegin(GL11.GL_LINE_STRIP);

        for (int i = 0; i <= 90; ++i) {
            ColorUtils.glColor(Color.WHITE);
            double div = 11D;

            GL11.glVertex3d(posX + radius * Math.cos((double) i * Math.PI * 2 / div),
                    posY, posZ + radius * Math.sin((double) i * Math.PI * 2 / div));
        }

        GL11.glEnd();
        GL11.glPopMatrix();
        GL11.glDepthMask(true);
        GL11.glDisable(2848);
        GL11.glEnable(2929);
        GL11.glDisable(3042);
        GL11.glEnable(3553);
        mc.entityRenderer.enableLightmap();
        GL11.glPopMatrix();
        GlStateManager.popAttribAndMatrix();
    }
}
