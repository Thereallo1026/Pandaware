package dev.africa.pandaware.impl.module.movement.speed.modes;

import dev.africa.pandaware.api.event.interfaces.EventCallback;
import dev.africa.pandaware.api.event.interfaces.EventHandler;
import dev.africa.pandaware.api.module.mode.ModuleMode;
import dev.africa.pandaware.impl.event.player.MoveEvent;
import dev.africa.pandaware.impl.module.movement.speed.SpeedModule;
import dev.africa.pandaware.impl.setting.BooleanSetting;
import dev.africa.pandaware.utils.player.MovementUtils;

public class VulcanSpeed extends ModuleMode<SpeedModule> {
    private final BooleanSetting timerSpeed = new BooleanSetting("Timer", false);

    @EventHandler
    EventCallback<MoveEvent> onMove = event -> {
        if (mc.thePlayer.onGround && mc.isMoveMoving()) {
            if (this.timerSpeed.getValue()) {
                mc.timer.timerSpeed = 0.9f;
            }
            mc.gameSettings.keyBindJump.pressed = true;
            event.y = mc.thePlayer.motionY = 0.42f;
            MovementUtils.strafe(MovementUtils.getBaseMoveSpeed() * 2.11);
        } else if (mc.thePlayer.getAirTicks() == 5) {
            if (this.timerSpeed.getValue() && mc.thePlayer.fallDistance < 1) {
                mc.timer.timerSpeed = 1.3f;
            }
            event.y = mc.thePlayer.motionY = -0.42f;
        } else {
            if (mc.thePlayer.fallDistance > 1) {
                mc.timer.timerSpeed = 1;
            }
            mc.gameSettings.keyBindJump.pressed = false;
        }
    };

    @Override
    public void onDisable() {
        mc.gameSettings.keyBindJump.pressed = false;
    }

    public VulcanSpeed(String name, SpeedModule parent) {
        super(name, parent);

        this.registerSettings(this.timerSpeed);
    }
}
