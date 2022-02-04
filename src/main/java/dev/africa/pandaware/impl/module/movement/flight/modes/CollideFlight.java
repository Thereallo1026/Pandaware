package dev.africa.pandaware.impl.module.movement.flight.modes;

import dev.africa.pandaware.api.event.interfaces.EventCallback;
import dev.africa.pandaware.api.event.interfaces.EventHandler;
import dev.africa.pandaware.api.module.mode.ModuleMode;
import dev.africa.pandaware.impl.event.player.CollisionEvent;
import dev.africa.pandaware.impl.module.movement.flight.FlightModule;
import net.minecraft.util.AxisAlignedBB;

public class CollideFlight extends ModuleMode<FlightModule> {
    public CollideFlight(String name, FlightModule parent) {
        super(name, parent);
    }

    @EventHandler
    EventCallback<CollisionEvent> onCollision = event -> {
        if (mc.thePlayer != null && !mc.thePlayer.isSneaking()) {
            event.setCollisionBox(new AxisAlignedBB(-100, -2, -100, 100, 1, 100)
                    .offset(event.getBlockPos().getX(), event.getBlockPos().getY(), event.getBlockPos().getZ()));
        }
    };
}
