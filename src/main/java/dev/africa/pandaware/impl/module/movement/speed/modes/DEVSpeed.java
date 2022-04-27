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

public class DEVSpeed extends ModuleMode<SpeedModule> {
    public DEVSpeed(String name, SpeedModule parent) {
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
        if (mc.isMoveMoving()) {
            mc.timer.timerSpeed = 1.3f;

            if (mc.thePlayer.onGround) {
                double motion = 0.4f;
                motion += PlayerUtils.getJumpBoostMotion();

                event.y = mc.thePlayer.motionY = motion;
                this.moveSpeed = MovementUtils.getBaseMoveSpeed() * 1.58;
                this.stage = 1;
            } else if (this.stage == 1) {
                this.moveSpeed = this.lastDistance - 0.66D * (this.lastDistance - MovementUtils.getBaseMoveSpeed());
                this.stage = 2;
            } else {
                boolean strafing = mc.thePlayer.moveStrafing != 0;

                this.moveSpeed = this.lastDistance - MovementUtils.getBaseMoveSpeed() / ((strafing ? 4f : 140) -
                        (mc.thePlayer.fallDistance > 1.2 ? 50 : 0));
            }

            this.moveSpeed = Math.max(this.moveSpeed, MovementUtils.getBaseMoveSpeed());
            MovementUtils.strafe(event, this.moveSpeed);

            if (mc.thePlayer.getAirTicks() == 4 && !mc.thePlayer.isPotionActive(Potion.jump)) {
                mc.thePlayer.motionY = -0.0662;
            }
        } else {
            mc.timer.timerSpeed = 1.0F;
        }
    };
}
