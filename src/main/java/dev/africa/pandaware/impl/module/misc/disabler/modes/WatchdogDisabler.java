package dev.africa.pandaware.impl.module.misc.disabler.modes;

import dev.africa.pandaware.Client;
import dev.africa.pandaware.api.event.interfaces.EventCallback;
import dev.africa.pandaware.api.event.interfaces.EventHandler;
import dev.africa.pandaware.api.module.mode.ModuleMode;
import dev.africa.pandaware.impl.event.player.PacketEvent;
import dev.africa.pandaware.impl.module.misc.disabler.DisablerModule;
import dev.africa.pandaware.impl.ui.notification.Notification;
import dev.africa.pandaware.utils.client.HypixelUtils;
import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraft.network.play.client.*;
import net.minecraft.network.play.server.S01PacketJoinGame;

public class WatchdogDisabler extends ModuleMode<DisablerModule> {
    public WatchdogDisabler(String name, DisablerModule parent) {
        super(name, parent);
    }

    private double packets;
    @EventHandler
    EventCallback<PacketEvent> onPacket = event -> {
        if (mc.getCurrentServerData() != null && mc.getCurrentServerData().serverIP.contains("hypixel.net") &&
                !(mc.currentScreen instanceof GuiMultiplayer) && !(HypixelUtils.compromised)) {
            if (event.getPacket() instanceof S01PacketJoinGame) {
                packets = 0;
            }

            if (event.getPacket() instanceof C03PacketPlayer && !(packets > 90)) {
                packets++;
            }

            if (mc.gameSettings.keyBindAttack.pressed && !(packets > 90)) mc.gameSettings.keyBindAttack.pressed = false; //prevents making ghost blocks while cancelling c07

            if ((event.getPacket() instanceof C03PacketPlayer || event.getPacket() instanceof C07PacketPlayerDigging ||
                    event.getPacket() instanceof C08PacketPlayerBlockPlacement ||
                    event.getPacket() instanceof C0APacketAnimation ||
                    event.getPacket() instanceof C09PacketHeldItemChange) && !(packets > 90)) { //hypixel checks for all of these before disabling checks now
                mc.thePlayer.inventory.currentItem = 0; //used for C09 to prevent desync until the slot is updated
                event.cancel();
            }

            if (packets == 0) {
                Client.getInstance().getNotificationManager().addNotification(Notification.Type.INFORMATION, "Disabling Watchdog...", 5);
            }
            if (packets == 90) {
                Client.getInstance().getNotificationManager().addNotification(Notification.Type.INFORMATION, "Watchdog is coping", 1);
            }
        }
    };

    @Override
    public void onEnable() {
        packets = 0;
    }
}