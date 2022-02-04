package dev.africa.pandaware.impl.module.combat.velocity.modes;

import dev.africa.pandaware.api.event.interfaces.EventCallback;
import dev.africa.pandaware.api.event.interfaces.EventHandler;
import dev.africa.pandaware.api.module.mode.ModuleMode;
import dev.africa.pandaware.impl.event.player.PacketEvent;
import dev.africa.pandaware.impl.module.combat.velocity.VelocityModule;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import net.minecraft.network.play.server.S27PacketExplosion;

public class PacketVelocity extends ModuleMode<VelocityModule> {
    public PacketVelocity(String name, VelocityModule parent) {
        super(name, parent);
    }

    @EventHandler
    EventCallback<PacketEvent> onPacket = event -> {
        if (mc.thePlayer != null) {
            if (event.getPacket() instanceof S12PacketEntityVelocity && ((S12PacketEntityVelocity) event.getPacket())
                    .getEntityID() == mc.thePlayer.getEntityId() || event.getPacket() instanceof S27PacketExplosion) {
                event.cancel();
            }
        }
    };
}
