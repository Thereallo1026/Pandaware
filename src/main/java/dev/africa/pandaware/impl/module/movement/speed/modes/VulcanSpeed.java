package dev.africa.pandaware.impl.module.movement.speed.modes;

import dev.africa.pandaware.api.event.interfaces.EventCallback;
import dev.africa.pandaware.api.event.interfaces.EventHandler;
import dev.africa.pandaware.api.module.mode.ModuleMode;
import dev.africa.pandaware.impl.event.player.MoveEvent;
import dev.africa.pandaware.impl.module.movement.speed.SpeedModule;
import dev.africa.pandaware.impl.setting.BooleanSetting;
import dev.africa.pandaware.utils.player.MovementUtils;
import net.minecraft.potion.Potion;

public class VulcanSpeed extends ModuleMode<SpeedModule> {
    private final BooleanSetting timerSpeed = new BooleanSetting("Timer", false);

    @EventHandler
    EventCallback<MoveEvent> onMove = event -> {
        if (mc.thePlayer.onGround && MovementUtils.isMoving()) {
            if (this.timerSpeed.getValue()) {
                mc.timer.timerSpeed = 0.9f;
            }
            mc.thePlayer.jump();
            event.y = mc.thePlayer.motionY = 0.42f;
            MovementUtils.strafe(event, MovementUtils.getBaseMoveSpeed() * (mc.thePlayer.isPotionActive(Potion.moveSpeed) ? 1.8 : 2.1));
        } else if (mc.thePlayer.getAirTicks() == 5 && MovementUtils.isMoving()) {
            if (this.timerSpeed.getValue() && mc.thePlayer.fallDistance < 1) {
                mc.timer.timerSpeed = 1.35f;
            }
            event.y = mc.thePlayer.motionY = -0.42f;
        } else {
            if (mc.timer.timerSpeed == 1.3f && mc.thePlayer.fallDistance > 1) {
                mc.timer.timerSpeed = 1;
            }
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
