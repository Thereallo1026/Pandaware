package dev.africa.pandaware.impl.module.misc.disabler.modes;

import dev.africa.pandaware.api.event.interfaces.EventCallback;
import dev.africa.pandaware.api.event.interfaces.EventHandler;
import dev.africa.pandaware.api.module.mode.ModuleMode;
import dev.africa.pandaware.impl.event.player.PacketEvent;
import dev.africa.pandaware.impl.module.misc.disabler.DisablerModule;
import net.minecraft.network.play.client.C03PacketPlayer;

public class C03ReplaceDisabler extends ModuleMode<DisablerModule> {
    public C03ReplaceDisabler(String name, DisablerModule parent) {
        super(name, parent);
    }

    @EventHandler
    EventCallback<PacketEvent> onPacket = event -> {
        if (event.getPacket() instanceof C03PacketPlayer.C04PacketPlayerPosition) {
            event.cancel();
            mc.thePlayer.sendQueue.getNetworkManager().sendPacketNoEvent(new C03PacketPlayer.C06PacketPlayerPosLook(
                    mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, mc.thePlayer.rotationYaw,
                    mc.thePlayer.rotationPitch, mc.thePlayer.onGround
            ));
        }
    };
}
