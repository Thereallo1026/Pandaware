package dev.africa.pandaware.utils.player;

import dev.africa.pandaware.api.interfaces.MinecraftInstance;
import dev.africa.pandaware.impl.event.player.MoveEvent;
import lombok.experimental.UtilityClass;
import net.minecraft.client.Minecraft;
import net.minecraft.potion.Potion;
import net.minecraft.util.MathHelper;

@UtilityClass
public class MovementUtils implements MinecraftInstance {
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
        return Math.sqrt(distX * distX + distZ * distZ);
    }

    public double getBps() {
        return getSpeedDistance() * 20;
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
            mc.thePlayer.movementInput.moveForward *= Math.sin(0.6398355709958845);
            mc.thePlayer.movementInput.moveStrafe *= Math.cos(0.6398355709958845);
        }

        if (moveEvent != null) {
            moveEvent.x = mc.thePlayer.motionX = mc.thePlayer.movementInput.moveForward * movementSpeed * -Math.sin(Math.toRadians(mc.thePlayer.rotationYaw))
                    + mc.thePlayer.movementInput.moveStrafe * movementSpeed * Math.cos(Math.toRadians(mc.thePlayer.rotationYaw));
            moveEvent.z = mc.thePlayer.motionZ = mc.thePlayer.movementInput.moveForward * movementSpeed * Math.cos(Math.toRadians(mc.thePlayer.rotationYaw))
                    - mc.thePlayer.movementInput.moveStrafe * movementSpeed * -Math.sin(Math.toRadians(mc.thePlayer.rotationYaw));
        } else {
            mc.thePlayer.motionX = mc.thePlayer.movementInput.moveForward * movementSpeed * -Math.sin(Math.toRadians(mc.thePlayer.rotationYaw))
                    + mc.thePlayer.movementInput.moveStrafe * movementSpeed * Math.cos(Math.toRadians(mc.thePlayer.rotationYaw));
            mc.thePlayer.motionZ = mc.thePlayer.movementInput.moveForward * movementSpeed * Math.cos(Math.toRadians(mc.thePlayer.rotationYaw))
                    - mc.thePlayer.movementInput.moveStrafe * movementSpeed * -Math.sin(Math.toRadians(mc.thePlayer.rotationYaw));
        }
    }

    public static double getSpeed() {
        return mc.thePlayer == null ? 0 : Math.sqrt(mc.thePlayer.motionX * mc.thePlayer.motionX
                + mc.thePlayer.motionZ * mc.thePlayer.motionZ);
    }

    public static double getSpeed(MoveEvent moveEvent) {
        return mc.thePlayer == null ? 0 : Math.sqrt(moveEvent.x * moveEvent.x + moveEvent.z * moveEvent.z);
    }

    public void slowdown() {
        double baseSpeed = 0.19;

        if (mc.thePlayer != null && mc.thePlayer.isPotionActive(Potion.moveSpeed)) {
            int amplifier = mc.thePlayer.getActivePotionEffect(Potion.moveSpeed).getAmplifier();
            baseSpeed *= 1.0 + 0.2 * (amplifier + 1);
        }

        strafe(MathHelper.clamp_double(getSpeed(), 0, baseSpeed));
    }

    public double getLastDistance() {
        return Math.hypot(mc.thePlayer.posX - mc.thePlayer.prevPosX, mc.thePlayer.posZ - mc.thePlayer.prevPosZ);
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
}
