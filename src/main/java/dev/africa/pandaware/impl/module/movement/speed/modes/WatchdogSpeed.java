package dev.africa.pandaware.impl.module.movement.speed.modes;

import dev.africa.pandaware.api.event.Event;
import dev.africa.pandaware.api.event.interfaces.EventCallback;
import dev.africa.pandaware.api.event.interfaces.EventHandler;
import dev.africa.pandaware.api.module.mode.ModuleMode;
import dev.africa.pandaware.impl.event.player.MotionEvent;
import dev.africa.pandaware.impl.event.player.MoveEvent;
import dev.africa.pandaware.impl.module.movement.speed.SpeedModule;
import dev.africa.pandaware.utils.player.MovementUtils;
import dev.africa.pandaware.utils.player.PlayerUtils;
import net.minecraft.potion.Potion;

public class WatchdogSpeed extends ModuleMode<SpeedModule> {
    public WatchdogSpeed(String name, SpeedModule parent) {
        super(name, parent);
    }

    private double moveSpeed;
    private double lastDistance;
    private int stage;

    @Override
    public void onEnable() {
        this.stage = 0;
        this.moveSpeed = MovementUtils.getBaseMoveSpeed();
        this.lastDistance = MovementUtils.getLastDistance();
    }

    @EventHandler
    EventCallback<MotionEvent> onMotion = event -> {
        if (event.getEventState() == Event.EventState.PRE) {
            this.lastDistance = MovementUtils.getLastDistance();
        }
    };

    @EventHandler
    EventCallback<MoveEvent> onMove = event -> {
        boolean hasSpeed = mc.thePlayer.isPotionActive(Potion.moveSpeed);

        if (mc.thePlayer.onGround) {
            mc.timer.timerSpeed = 1.5F;
            double motion = 0.418341F;
            motion += PlayerUtils.getJumpBoostMotion();

            event.y = mc.thePlayer.motionY = motion;
            this.moveSpeed = MovementUtils.getBaseMoveSpeed() * (hasSpeed ? 2.1 : 2.2);
            this.stage = 1;
        } else if (this.stage == 1) {
            mc.timer.timerSpeed = 1.6F;
            this.moveSpeed = this.lastDistance - 0.665F * (this.lastDistance - MovementUtils.getBaseMoveSpeed());
            this.stage = 2;
        } else {
            mc.timer.timerSpeed = 1.7F;
            this.moveSpeed = this.lastDistance - this.lastDistance / 91;
        }

        this.moveSpeed = Math.max(this.moveSpeed, MovementUtils.getBaseMoveSpeed());
        if (mc.thePlayer.onGround || mc.thePlayer.fallDistance > 0) {
            MovementUtils.strafe(event, this.moveSpeed);
        }

        if (mc.thePlayer.getAirTicks() == 4) {
            mc.thePlayer.motionY = -0.0762;
        }
    };
}
