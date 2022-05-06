package dev.africa.pandaware.impl.module.misc.disabler.modes;

import dev.africa.pandaware.api.event.interfaces.EventCallback;
import dev.africa.pandaware.api.event.interfaces.EventHandler;
import dev.africa.pandaware.api.module.mode.ModuleMode;
import dev.africa.pandaware.impl.event.player.PacketEvent;
import dev.africa.pandaware.impl.module.misc.disabler.DisablerModule;
import dev.africa.pandaware.utils.client.ServerUtils;
import net.minecraft.network.play.client.*;
import net.minecraft.network.play.server.S01PacketJoinGame;

public class HypixelDisabler extends ModuleMode<DisablerModule> {
    private double packets;
    @EventHandler
    EventCallback<PacketEvent> onPacket = event -> {
        if (ServerUtils.isOnServer("mc.hypixel.net") && !(ServerUtils.compromised)) {
            if (event.getPacket() instanceof S01PacketJoinGame) {
                packets = 0;
            }

            if (event.getPacket() instanceof C03PacketPlayer && !(packets > 97)) {
                packets++;
            }

            if (mc.gameSettings.keyBindAttack.pressed && !(packets > 97)) mc.gameSettings.keyBindAttack.pressed = false; //prevents making ghost blocks while cancelling c07

            if ((event.getPacket() instanceof C03PacketPlayer || event.getPacket() instanceof C07PacketPlayerDigging ||
                    event.getPacket() instanceof C08PacketPlayerBlockPlacement ||
                    event.getPacket() instanceof C0APacketAnimation ||
                    event.getPacket() instanceof C09PacketHeldItemChange) && !(packets > 97)) { //hypixel checks for all of these before disabling checks now
                mc.thePlayer.inventory.currentItem = 0; //used for C09 to prevent desync
                event.cancel();
            }
        }
    };

    public HypixelDisabler(String name, DisablerModule parent) {
        super(name, parent);
    }

    @Override
    public void onEnable() {
        packets = 0;
    }
}
