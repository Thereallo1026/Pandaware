package dev.africa.pandaware.impl.module.misc;

import dev.africa.pandaware.api.event.interfaces.EventCallback;
import dev.africa.pandaware.api.event.interfaces.EventHandler;
import dev.africa.pandaware.api.module.Module;
import dev.africa.pandaware.api.module.interfaces.ModuleInfo;
import dev.africa.pandaware.impl.event.player.PacketEvent;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;

@ModuleInfo(name = "Ghost Block", description = "does funny")
public class GhostBlockModule extends Module {
    @EventHandler
    EventCallback<PacketEvent> onPacket = event -> {
        if (event.getPacket() instanceof C07PacketPlayerDigging || event.getPacket() instanceof C08PacketPlayerBlockPlacement) {
            event.cancel();
        }
    };
}
