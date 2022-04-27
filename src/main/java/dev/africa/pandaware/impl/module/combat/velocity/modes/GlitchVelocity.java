package dev.africa.pandaware.impl.module.combat.velocity.modes;

import dev.africa.pandaware.api.event.interfaces.EventCallback;
import dev.africa.pandaware.api.event.interfaces.EventHandler;
import dev.africa.pandaware.api.module.mode.ModuleMode;
import dev.africa.pandaware.impl.event.player.PacketEvent;
import dev.africa.pandaware.impl.module.combat.velocity.VelocityModule;
import net.minecraft.network.play.server.S12PacketEntityVelocity;

public class GlitchVelocity extends ModuleMode<VelocityModule> {
    public GlitchVelocity(String name, VelocityModule parent) {
        super(name, parent);
    }

    @EventHandler
    EventCallback<PacketEvent> onPacket = event -> {
        if (mc.thePlayer != null) {
            if (event.getPacket() instanceof S12PacketEntityVelocity && ((S12PacketEntityVelocity) event.getPacket())
                    .getEntityID() == mc.thePlayer.getEntityId()) {
                event.cancel();
                mc.thePlayer.setPositionAndUpdate(mc.thePlayer.posX, mc.thePlayer.posY - 0.1, mc.thePlayer.posZ);
            }
        }
    };
}
