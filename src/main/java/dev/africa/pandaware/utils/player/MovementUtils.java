package dev.africa.pandaware.utils.player;

import dev.africa.pandaware.api.interfaces.MinecraftInstance;
import dev.africa.pandaware.impl.event.player.MoveEvent;
import dev.africa.pandaware.utils.math.MathUtils;
import dev.africa.pandaware.utils.math.apache.ApacheMath;
import dev.africa.pandaware.utils.math.random.RandomUtils;
import lombok.experimental.UtilityClass;
import net.minecraft.client.Minecraft;
import net.minecraft.potion.Potion;
import net.minecraft.util.MathHelper;

@UtilityClass
public class MovementUtils implements MinecraftInstance {

    public boolean isMoving() {
        return mc.thePlayer.movementInput.moveForward != 0 || mc.thePlayer.movementInput.moveStrafe != 0;
    }

    public double getBaseMoveSpeed() {
        double baseSpeed = 0.2873D;
        if (mc.thePlayer.isPotionActive(Potion.moveSpeed)) {
            int amplifier = mc.thePlayer.getActivePotionEffect(Potion.moveSpeed).getAmplifier();
            baseSpeed *= (1.0D + 0.2D * (amplifier + 1));
        }
        return baseSpeed;
    }

    public double getSpeedDistance() {
        double distX = mc.thePlayer.posX - mc.thePlayer.lastTickPosX;
        double distZ = mc.thePlayer.posZ - mc.thePlayer.lastTickPosZ;
        return ApacheMath.sqrt(distX * distX + distZ * distZ);
    }

    public double getBps() {
        return (getSpeedDistance() * 20) * mc.timer.timerSpeed;
    }

    public void strafe() {
        strafe(getSpeed());
    }

    public void strafe(MoveEvent event) {
        strafe(event, getSpeed());
    }

    public void strafe(double movementSpeed) {
        strafe(null, movementSpeed);
    }

    public void strafe(MoveEvent moveEvent, double movementSpeed) {
        if (mc.thePlayer.movementInput.moveForward > 0.0) {
            mc.thePlayer.movementInput.moveForward = (float) 1.0;
        } else if (mc.thePlayer.movementInput.moveForward < 0.0) {
            mc.thePlayer.movementInput.moveForward = (float) -1.0;
        }

        if (mc.thePlayer.movementInput.moveStrafe > 0.0) {
            mc.thePlayer.movementInput.moveStrafe = (float) 1.0;
        } else if (mc.thePlayer.movementInput.moveStrafe < 0.0) {
            mc.thePlayer.movementInput.moveStrafe = (float) -1.0;
        }

        if (mc.thePlayer.movementInput.moveForward == 0.0 && mc.thePlayer.movementInput.moveStrafe == 0.0) {
            mc.thePlayer.motionX = 0.0;
            mc.thePlayer.motionZ = 0.0;
        }

        if (mc.thePlayer.movementInput.moveForward != 0.0 && mc.thePlayer.movementInput.moveStrafe != 0.0) {
            mc.thePlayer.movementInput.moveForward *= ApacheMath.sin(0.6398355709958845);
            mc.thePlayer.movementInput.moveStrafe *= ApacheMath.cos(0.6398355709958845);
        }

        if (moveEvent != null) {
            moveEvent.x = mc.thePlayer.motionX = mc.thePlayer.movementInput.moveForward * movementSpeed * -ApacheMath.sin(ApacheMath.toRadians(mc.thePlayer.rotationYaw))
                    + mc.thePlayer.movementInput.moveStrafe * movementSpeed * ApacheMath.cos(ApacheMath.toRadians(mc.thePlayer.rotationYaw));
            moveEvent.z = mc.thePlayer.motionZ = mc.thePlayer.movementInput.moveForward * movementSpeed * ApacheMath.cos(ApacheMath.toRadians(mc.thePlayer.rotationYaw))
                    - mc.thePlayer.movementInput.moveStrafe * movementSpeed * -ApacheMath.sin(ApacheMath.toRadians(mc.thePlayer.rotationYaw));
        } else {
            mc.thePlayer.motionX = mc.thePlayer.movementInput.moveForward * movementSpeed * -ApacheMath.sin(ApacheMath.toRadians(mc.thePlayer.rotationYaw))
                    + mc.thePlayer.movementInput.moveStrafe * movementSpeed * ApacheMath.cos(ApacheMath.toRadians(mc.thePlayer.rotationYaw));
            mc.thePlayer.motionZ = mc.thePlayer.movementInput.moveForward * movementSpeed * ApacheMath.cos(ApacheMath.toRadians(mc.thePlayer.rotationYaw))
                    - mc.thePlayer.movementInput.moveStrafe * movementSpeed * -ApacheMath.sin(ApacheMath.toRadians(mc.thePlayer.rotationYaw));
        }
    }

    public static double getSpeed() {
        return mc.thePlayer == null ? 0 : ApacheMath.sqrt(mc.thePlayer.motionX * mc.thePlayer.motionX
                + mc.thePlayer.motionZ * mc.thePlayer.motionZ);
    }

    public static double getSpeed(MoveEvent moveEvent) {
        return mc.thePlayer == null ? 0 : ApacheMath.sqrt(moveEvent.x * moveEvent.x + moveEvent.z * moveEvent.z);
    }

    public void slowdown() {
        double baseSpeed = 0.1873;

        if (mc.thePlayer != null && mc.thePlayer.isPotionActive(Potion.moveSpeed)) {
            int amplifier = mc.thePlayer.getActivePotionEffect(Potion.moveSpeed).getAmplifier();
            baseSpeed *= 1.0 + 0.2 * (amplifier + 1);
        }

        strafe(MathHelper.clamp_double(getSpeed(), 0, baseSpeed));
    }

    public double getLastDistance() {
        return ApacheMath.hypot(mc.thePlayer.posX - mc.thePlayer.prevPosX, mc.thePlayer.posZ - mc.thePlayer.prevPosZ);
    }

    public double getDirection() {
        float rotationYaw = Minecraft.getMinecraft().thePlayer.rotationYaw;

        if (Minecraft.getMinecraft().thePlayer.moveForward < 0F)
            rotationYaw += 180F;

        float forward = 1F;
        if (Minecraft.getMinecraft().thePlayer.moveForward < 0F)
            forward = -0.5F;
        else if (Minecraft.getMinecraft().thePlayer.moveForward > 0F)
            forward = 0.5F;

        if (Minecraft.getMinecraft().thePlayer.moveStrafing > 0F)
            rotationYaw -= 90F * forward;

        if (Minecraft.getMinecraft().thePlayer.moveStrafing < 0F)
            rotationYaw += 90F * forward;

        return rotationYaw;
    }

    public double getLowHopMotion(double motion) {
        double base = MathUtils.roundToDecimal(mc.thePlayer.posY - (int) mc.thePlayer.posY, 2);

        if (base == 0.4) {
            return 0.31f + getHypixelFunny();
        } else if (base == 0.71) {
            return 0.04f + getHypixelFunny();
        } else if (base == 0.75) {
            return -0.2f + getHypixelFunny();
        } else if (base == 0.55) {
            return -0.19f + getHypixelFunny();
        } else if (base == 0.41) {
            return -0.2f + getHypixelFunny();
        }

        return motion;
    }

    public double getHypixelFunny() {
        double value = 1;
        for (int i = 0; i < RandomUtils.nextInt(4, 7); i++) {
            value *= ApacheMath.random();
        }
        return value;
    }

    public final double MODULO_GROUND = 1 / 64D;
}
