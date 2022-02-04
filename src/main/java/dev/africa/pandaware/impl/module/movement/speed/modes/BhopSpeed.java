package dev.africa.pandaware.impl.module.movement.speed.modes;

import dev.africa.pandaware.api.event.interfaces.EventCallback;
import dev.africa.pandaware.api.event.interfaces.EventHandler;
import dev.africa.pandaware.api.module.mode.ModuleMode;
import dev.africa.pandaware.impl.event.player.MoveEvent;
import dev.africa.pandaware.impl.module.movement.speed.SpeedModule;
import dev.africa.pandaware.utils.player.MovementUtils;

public class BhopSpeed extends ModuleMode<SpeedModule> {
    public BhopSpeed(String name, SpeedModule parent) {
        super(name, parent);
    }

    @EventHandler
    EventCallback<MoveEvent> onMove = event -> {
        if (mc.thePlayer.onGround && mc.isMoveMoving()) {
            event.y = mc.thePlayer.motionY = 0.42F;
        }

        MovementUtils.strafe(event, this.getParent().getSpeed().getValue().floatValue());
    };
}
