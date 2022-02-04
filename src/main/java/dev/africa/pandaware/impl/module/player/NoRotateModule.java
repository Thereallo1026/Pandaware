package dev.africa.pandaware.impl.module.player;

import dev.africa.pandaware.api.event.interfaces.EventCallback;
import dev.africa.pandaware.api.event.interfaces.EventHandler;
import dev.africa.pandaware.api.interfaces.MinecraftInstance;
import dev.africa.pandaware.api.module.Module;
import dev.africa.pandaware.api.module.interfaces.Category;
import dev.africa.pandaware.api.module.interfaces.ModuleInfo;
import dev.africa.pandaware.impl.event.player.PacketEvent;
import lombok.var;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;

@ModuleInfo(name = "No Rotate", category = Category.PLAYER)
public class NoRotateModule extends Module {
    @EventHandler
    EventCallback<PacketEvent> onPacket = event -> {
        if (mc.thePlayer != null && event.getPacket() instanceof S08PacketPlayerPosLook) {
            var packet = (S08PacketPlayerPosLook) event.getPacket();

            packet.setYaw(mc.thePlayer.rotationYaw);
            packet.setPitch(mc.thePlayer.rotationPitch);

            event.setPacket(packet);
        }
    };
}
