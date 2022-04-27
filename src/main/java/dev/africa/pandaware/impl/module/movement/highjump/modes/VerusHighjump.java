package dev.africa.pandaware.impl.module.movement.highjump.modes;

import dev.africa.pandaware.Client;
import dev.africa.pandaware.api.event.Event;
import dev.africa.pandaware.api.event.interfaces.EventCallback;
import dev.africa.pandaware.api.event.interfaces.EventHandler;
import dev.africa.pandaware.api.module.mode.ModuleMode;
import dev.africa.pandaware.impl.event.player.MotionEvent;
import dev.africa.pandaware.impl.event.player.MoveEvent;
import dev.africa.pandaware.impl.module.movement.highjump.HighJumpModule;
import dev.africa.pandaware.impl.ui.notification.Notification;
import dev.africa.pandaware.utils.player.MovementUtils;

public class VerusHighjump extends ModuleMode<HighJumpModule> {

    private int ticks;
    private double lastY;
    private double groundY;

    private boolean damage;
    @EventHandler
    EventCallback<MoveEvent> onMove = event -> {
        if (mc.thePlayer.posY % 0.015625 != 0) {
            parent.toggle(false);
            Client.getInstance().getNotificationManager().addNotification(Notification.Type.ERROR, "You must be on ground", 3);
            return;
        }

        if (this.damage) {
            event.x = mc.thePlayer.motionX = 0;
            event.z = mc.thePlayer.motionZ = 0;
        }
        if (mc.thePlayer.hurtTime == 9) {
            parent.toggle(false);

            MovementUtils.strafe(.5);
            event.y = mc.thePlayer.motionY = 1;
        }
    };
    @EventHandler
    EventCallback<MotionEvent> onMotion = event -> {
        if (event.getEventState() == Event.EventState.PRE) {
            if (this.damage) {

                event.setY(this.lastY);

                if (this.ticks < 17) {
                    event.setOnGround(false);
                    this.lastY = mc.thePlayer.posY + .5;
                } else {
                    if (this.ticks < 25) {
                        event.setOnGround(true);
                        this.lastY = this.groundY;
                    }

                    if (this.ticks > 25) {
                        this.damage = false;
                    }
                }

                this.ticks++;
            }
        }
    };

    public VerusHighjump(String name, HighJumpModule parent) {
        super(name, parent);
    }

    @Override
    public void onEnable() {
        this.ticks = 0;
        this.lastY = mc.thePlayer.posY;
        this.groundY = mc.thePlayer.posY;
        this.damage = true;
    }
}
