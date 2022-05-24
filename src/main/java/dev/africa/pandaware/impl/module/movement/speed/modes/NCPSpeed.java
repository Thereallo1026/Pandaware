package dev.africa.pandaware.impl.module.movement.speed.modes;

import dev.africa.pandaware.api.event.Event;
import dev.africa.pandaware.api.event.interfaces.EventCallback;
import dev.africa.pandaware.api.event.interfaces.EventHandler;
import dev.africa.pandaware.api.module.mode.ModuleMode;
import dev.africa.pandaware.impl.event.player.MotionEvent;
import dev.africa.pandaware.impl.event.player.MoveEvent;
import dev.africa.pandaware.impl.module.movement.speed.SpeedModule;
import dev.africa.pandaware.impl.setting.BooleanSetting;
import dev.africa.pandaware.impl.setting.NumberSetting;
import dev.africa.pandaware.utils.player.MovementUtils;
import dev.africa.pandaware.utils.player.PlayerUtils;
import net.minecraft.potion.Potion;

public class NCPSpeed extends ModuleMode<SpeedModule> {
    private final NumberSetting acceleration = new NumberSetting("Acceleration", 10, 0, 2,0.1);
    private final NumberSetting deceleration = new NumberSetting("Deceleration", 10, 0, 2.4,0.1);
    private final BooleanSetting timer = new BooleanSetting("Timer", false);
    private final NumberSetting timerSpeed = new NumberSetting("Timer Speed", 2, 1, 1.3, 0.1,
            this.timer::getValue);

    private double moveSpeed;
    private boolean jumped;
    private double lastDistance;
    private boolean isTimer;

    @EventHandler
    EventCallback<MotionEvent> onMotion = event -> {
        if (event.getEventState() == Event.EventState.PRE) {
            lastDistance = MovementUtils.getLastDistance();
        }
    };
    @EventHandler
    EventCallback<MoveEvent> onMove = event -> {
        if (MovementUtils.isMoving() && !PlayerUtils.onLiquid() && !mc.thePlayer.isInLava() && !mc.thePlayer.isInWater()) {
            if (mc.thePlayer.ticksExisted % 10 == 0 && timer.getValue()) {
                if (isTimer) {
                    mc.timer.timerSpeed = 1f;
                    isTimer = false;
                } else {
                    mc.timer.timerSpeed = timerSpeed.getValue().floatValue();
                    isTimer = true;
                }
            }

            if (mc.thePlayer.onGround) {
                double motion = 0.4F;
                motion += PlayerUtils.getJumpBoostMotion();

                event.y = mc.thePlayer.motionY = motion;
                moveSpeed = MovementUtils.getBaseMoveSpeed() * this.acceleration.getValue().floatValue();
                jumped = true;
            } else if (jumped) {
                moveSpeed = lastDistance - 0.66F * (lastDistance - MovementUtils.getBaseMoveSpeed());
                jumped = false;
            } else {
                moveSpeed = lastDistance - lastDistance / (this.deceleration.getValue().floatValue() * 100);
            }
            if (mc.thePlayer.getAirTicks() == 5 && !mc.thePlayer.isPotionActive(Potion.jump)) {
                event.y = mc.thePlayer.motionY = -0.01;
            }
            MovementUtils.strafe(event, moveSpeed = Math.max(moveSpeed, MovementUtils.getBaseMoveSpeed()));
        }
    };

    public NCPSpeed(String name, SpeedModule parent) {
        super(name, parent);

        this.registerSettings(
                this.acceleration,
                this.deceleration,
                this.timer,
                this.timerSpeed
        );
    }

    @Override
    public void onDisable() {
        this.jumped = false;
        this.moveSpeed = 0;
        this.lastDistance = 0;
        this.isTimer = false;
        mc.timer.timerSpeed = 1f;
    }
}
