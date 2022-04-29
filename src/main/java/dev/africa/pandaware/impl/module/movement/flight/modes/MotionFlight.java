package dev.africa.pandaware.impl.module.movement.flight.modes;

import dev.africa.pandaware.api.event.interfaces.EventCallback;
import dev.africa.pandaware.api.event.interfaces.EventHandler;
import dev.africa.pandaware.api.module.mode.ModuleMode;
import dev.africa.pandaware.impl.event.player.MoveEvent;
import dev.africa.pandaware.impl.module.movement.flight.FlightModule;
import dev.africa.pandaware.impl.setting.NumberSetting;
import dev.africa.pandaware.utils.player.MovementUtils;

public class MotionFlight extends ModuleMode<FlightModule> {
    private final NumberSetting speed = new NumberSetting("Speed", 10, 0.1, 1, 0.1);

    public MotionFlight(String name, FlightModule parent) {
        super(name, parent);

        this.registerSettings(this.speed);
    }

    @EventHandler
    EventCallback<MoveEvent> onMove = event -> {
        event.y = mc.thePlayer.motionY = mc.gameSettings.keyBindJump.isKeyDown() ? this.speed.getValue().floatValue()
                : mc.gameSettings.keyBindSneak.isKeyDown() ? -this.speed.getValue().floatValue() : 0.0F;
        MovementUtils.strafe(event, this.speed.getValue().doubleValue());
    };
}
