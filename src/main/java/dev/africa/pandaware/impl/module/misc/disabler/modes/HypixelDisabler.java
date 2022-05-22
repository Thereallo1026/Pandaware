package dev.africa.pandaware.impl.module.misc.disabler.modes;

import dev.africa.pandaware.api.event.interfaces.EventCallback;
import dev.africa.pandaware.api.event.interfaces.EventHandler;
import dev.africa.pandaware.api.module.mode.ModuleMode;
import dev.africa.pandaware.impl.event.game.TickEvent;
import dev.africa.pandaware.impl.event.player.PacketEvent;
import dev.africa.pandaware.impl.module.misc.disabler.DisablerModule;
import dev.africa.pandaware.utils.client.Printer;
import dev.africa.pandaware.utils.client.ServerUtils;
import dev.africa.pandaware.utils.player.MovementUtils;
import net.minecraft.network.play.client.*;
import net.minecraft.network.play.server.S01PacketJoinGame;

public class HypixelDisabler extends ModuleMode<DisablerModule> {
    public HypixelDisabler(String name, DisablerModule parent) {
        super(name, parent);
    }

    private int packets;

    @Override
    public void onEnable() {
        this.packets = 0;
    }

    @EventHandler
    EventCallback<TickEvent> onTick = event -> {
        if ((ServerUtils.isOnServer("mc.hypixel.net") || ServerUtils.isOnServer("hypixel.net")) && !(ServerUtils.compromised)) {
            if (this.packets <= 97) {
                mc.gameSettings.keyBindAttack.pressed = false;
                mc.gameSettings.keyBindUseItem.pressed = false;
            }
        }
    };

    @EventHandler
    EventCallback<PacketEvent> onPacket = event -> {
        if ((ServerUtils.isOnServer("mc.hypixel.net") || ServerUtils.isOnServer("hypixel.net")) && !(ServerUtils.compromised)) {
            if (event.getPacket() instanceof S01PacketJoinGame) {
                this.packets = 0;
            }

            /*
            HYPIXEL TIMER DISABLER GIVEN BY ALAN32. (Up to 1.3 Timer).
             */

            C03PacketPlayer c03 = null;
            if(event.getPacket() instanceof  C03PacketPlayer) {
                c03 = (C03PacketPlayer) event.getPacket();
            }
            if (event.getPacket() instanceof C03PacketPlayer && !(c03.isMoving()) && !(mc.thePlayer.isSwingInProgress || mc.thePlayer.isUsingItem())) {
                event.cancel();
            }

            if (this.packets <= 97 && (event.getPacket() instanceof C0FPacketConfirmTransaction ||
                    event.getPacket() instanceof C00PacketKeepAlive ||
                    event.getPacket() instanceof C03PacketPlayer ||
                    event.getPacket() instanceof C09PacketHeldItemChange ||
                    event.getPacket() instanceof C08PacketPlayerBlockPlacement ||
                    event.getPacket() instanceof C0CPacketInput ||
                    event.getPacket() instanceof C13PacketPlayerAbilities ||
                    event.getPacket() instanceof C07PacketPlayerDigging ||
                    event.getPacket() instanceof C0APacketAnimation ||
                    event.getPacket() instanceof C02PacketUseEntity)) {
                mc.thePlayer.inventory.currentItem = 0;
                event.cancel();

                if (event.getPacket() instanceof C03PacketPlayer) {
                    if (this.packets % 8 == 0) {
                        mc.thePlayer.sendQueue.getNetworkManager().sendPacketNoEvent(new C03PacketPlayer.C04PacketPlayerPosition(
                                mc.thePlayer.posX, mc.thePlayer.posY - (1 + MovementUtils.MODULO_GROUND), mc.thePlayer.posZ,
                                false
                        ));
                    }

                    this.packets++;
                }
            }
        } else {
            if (mc.thePlayer != null && mc.theWorld != null)
            mc.thePlayer.sendQueue.getNetworkManager().sendPacketNoEvent(new C03PacketPlayer(false));
        }
    };
}
