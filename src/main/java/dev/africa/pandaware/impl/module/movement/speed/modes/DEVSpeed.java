package dev.africa.pandaware.impl.module.movement.speed.modes;

import dev.africa.pandaware.api.event.Event;
import dev.africa.pandaware.api.event.interfaces.EventCallback;
import dev.africa.pandaware.api.event.interfaces.EventHandler;
import dev.africa.pandaware.api.module.mode.ModuleMode;
import dev.africa.pandaware.impl.event.player.MotionEvent;
import dev.africa.pandaware.impl.module.movement.speed.SpeedModule;
import dev.africa.pandaware.utils.math.TimeHelper;
import dev.africa.pandaware.utils.math.apache.ApacheMath;
import dev.africa.pandaware.utils.player.MovementUtils;

public class DEVSpeed extends ModuleMode<SpeedModule> {
    public DEVSpeed(String name, SpeedModule parent) {
        super(name, parent);
    }

    private double counter;

    private final TimeHelper timer = new TimeHelper();

    @EventHandler
    EventCallback<MotionEvent> onMove = event -> {
        if (event.getEventState() == Event.EventState.PRE) {
            this.counter++;
            float yaw = MovementUtils.getDirection();
            double x = -ApacheMath.sin(ApacheMath.toRadians(yaw));
            double z = ApacheMath.cos(ApacheMath.toRadians(yaw));
            event.setOnGround(true);
            mc.thePlayer.motionY = 0.0D;
            if (MovementUtils.isMoving()) {
                if (this.counter == 1) {
                    mc.thePlayer.motionX = (x * 0.8D);
                    mc.thePlayer.motionZ = (z * 0.8D);
                } else if (this.counter == 5) {
                    mc.thePlayer.motionX = (x * 0.75D);
                    mc.thePlayer.motionZ = (z * 0.75D);
                } else if (this.counter == 10) {
                    mc.thePlayer.motionX = (x * 0.5D);
                    mc.thePlayer.motionZ = (z * 0.5D);
                } else if (this.counter == 15) {
                    mc.thePlayer.motionX = (x * 0.25D);
                    mc.thePlayer.motionZ = (z * 0.25D);
                } else if (this.counter >= 20) {
                    mc.thePlayer.motionX = (x * 0.0D);
                    mc.thePlayer.motionZ = (z * 0.0D);
                }
            }
            if (timer.reach(2040)) {
                this.counter = 0;
                timer.reset();
            }
        }
    };
}