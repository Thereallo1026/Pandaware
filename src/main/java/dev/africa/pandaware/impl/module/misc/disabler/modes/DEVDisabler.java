package dev.africa.pandaware.impl.module.misc.disabler.modes;

import dev.africa.pandaware.api.event.Event;
import dev.africa.pandaware.api.event.interfaces.EventCallback;
import dev.africa.pandaware.api.event.interfaces.EventHandler;
import dev.africa.pandaware.api.module.mode.ModuleMode;
import dev.africa.pandaware.impl.event.player.MotionEvent;
import dev.africa.pandaware.impl.module.misc.disabler.DisablerModule;
import net.minecraft.network.play.client.C0CPacketInput;
import net.minecraft.network.play.client.C18PacketSpectate;

public class DEVDisabler extends ModuleMode<DisablerModule> {
    public DEVDisabler(String name, DisablerModule parent) {
        super(name, parent);
    }

    @EventHandler
    EventCallback<MotionEvent> onPacket = event -> {
        if (event.getEventState() == Event.EventState.PRE) {
            mc.thePlayer.sendQueue.getNetworkManager().sendPacketNoEvent(new C0CPacketInput());
            mc.thePlayer.sendQueue.getNetworkManager().sendPacketNoEvent(new C18PacketSpectate(mc.thePlayer.getUniqueID()));
        }
    };
}
