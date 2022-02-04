package dev.africa.pandaware.impl.module.movement.speed.modes;

import dev.africa.pandaware.api.event.interfaces.EventCallback;
import dev.africa.pandaware.api.event.interfaces.EventHandler;
import dev.africa.pandaware.api.module.mode.ModuleMode;
import dev.africa.pandaware.impl.event.player.MoveEvent;
import dev.africa.pandaware.impl.module.movement.speed.SpeedModule;
import dev.africa.pandaware.utils.player.MovementUtils;

public class VanillaSpeed extends ModuleMode<SpeedModule> {
    public VanillaSpeed(String name, SpeedModule parent) {
        super(name, parent);
    }

    @EventHandler
    EventCallback<MoveEvent> onMove = event ->
            MovementUtils.strafe(event, (mc.thePlayer.ticksExisted % 2 == 0 ? this.getParent().getSpeed().getValue().floatValue() : 0));
}
