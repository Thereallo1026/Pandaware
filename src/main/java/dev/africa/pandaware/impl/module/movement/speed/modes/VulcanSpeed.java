package dev.africa.pandaware.impl.module.movement.speed.modes;

import dev.africa.pandaware.api.event.interfaces.EventCallback;
import dev.africa.pandaware.api.event.interfaces.EventHandler;
import dev.africa.pandaware.api.module.mode.ModuleMode;
import dev.africa.pandaware.impl.event.player.MoveEvent;
import dev.africa.pandaware.impl.module.movement.speed.SpeedModule;
import dev.africa.pandaware.utils.player.MovementUtils;
import dev.africa.pandaware.utils.player.PlayerUtils;

public class VulcanSpeed extends ModuleMode<SpeedModule> {
    private double airTicks;
    @EventHandler
    EventCallback<MoveEvent> onMove = event -> {
        if (mc.thePlayer.getAirTicks() > 0) {
            airTicks++;
        }
        if (mc.thePlayer.onGround && mc.isMoveMoving()) {
            mc.gameSettings.keyBindJump.pressed = true;
            event.y = mc.thePlayer.motionY = 0.42f + PlayerUtils.getJumpBoostMotion();
            if (airTicks >= 7) {
                MovementUtils.strafe(0.60695f);
            } else if (airTicks >= 3) {
                MovementUtils.strafe(0.55f);
            } else {
                MovementUtils.strafe(0.7022f);
            }
            airTicks = 0;
        } else if (mc.thePlayer.getAirTicks() == 5) {
            event.y = mc.thePlayer.motionY = -0.42f;
        }
    };

    public VulcanSpeed(String name, SpeedModule parent) {
        super(name, parent);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        airTicks = 3;
    }

    @Override
    public void onDisable() {
        super.onDisable();
        if (mc.gameSettings.keyBindJump.pressed) mc.gameSettings.keyBindJump.pressed = false;
    }
}
