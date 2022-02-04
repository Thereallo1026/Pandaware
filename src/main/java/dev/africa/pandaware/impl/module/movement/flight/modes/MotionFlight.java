package dev.africa.pandaware.impl.module.movement.flight.modes;

import dev.africa.pandaware.api.event.interfaces.EventCallback;
import dev.africa.pandaware.api.event.interfaces.EventHandler;
import dev.africa.pandaware.api.module.mode.ModuleMode;
import dev.africa.pandaware.impl.event.player.MoveEvent;
import dev.africa.pandaware.impl.module.movement.flight.FlightModule;
import dev.africa.pandaware.utils.player.MovementUtils;

public class MotionFlight extends ModuleMode<FlightModule> {
    public MotionFlight(String name, FlightModule parent) {
        super(name, parent);
    }

    @EventHandler
    EventCallback<MoveEvent> onMove = event -> {
//        mc.timer.timerSpeed = 1.2F;

        event.y = mc.thePlayer.motionY = mc.gameSettings.keyBindJump.isKeyDown() ? 0.7
                : mc.gameSettings.keyBindSneak.isKeyDown() ? -0.7 : 0.0F;
        MovementUtils.strafe(event, this.getParent().getSpeed().getValue().doubleValue());
    };
}
