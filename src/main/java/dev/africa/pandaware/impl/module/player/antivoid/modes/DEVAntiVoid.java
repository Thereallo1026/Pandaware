package dev.africa.pandaware.impl.module.player.antivoid.modes;

import dev.africa.pandaware.api.event.interfaces.EventCallback;
import dev.africa.pandaware.api.event.interfaces.EventHandler;
import dev.africa.pandaware.api.module.mode.ModuleMode;
import dev.africa.pandaware.impl.event.player.PacketEvent;
import dev.africa.pandaware.impl.module.player.antivoid.AntiVoidModule;
import dev.africa.pandaware.utils.player.PlayerUtils;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.util.BlockPos;

public class DEVAntiVoid extends ModuleMode<AntiVoidModule> {
    private double posX;
    private double posY;
    private double posZ;

    @EventHandler
    EventCallback<PacketEvent> onPacket = event -> {
        if (event.getPacket() instanceof C03PacketPlayer) {
            if (PlayerUtils.isMathGround() && PlayerUtils.isBlockUnder()) {
                posX = mc.thePlayer.posX;
                posY = mc.thePlayer.posY;
                posZ = mc.thePlayer.posZ;
            }

            if (!PlayerUtils.isBlockUnder()) {
                if (mc.thePlayer.fallDistance >= this.getParent().getFallDistance().getValue().floatValue()) {
                    mc.thePlayer.sendQueue.getNetworkManager().sendPacketNoEvent(new C03PacketPlayer.C04PacketPlayerPosition(
                            posX, posY + 0.05, posZ, false));
                    mc.thePlayer.inventory.currentItem = getItemSlot();
                    mc.thePlayer.swingItem();
                    mc.thePlayer.sendQueue.addToSendQueue(new C08PacketPlayerBlockPlacement(
                            new BlockPos(posX, posY - 1.94D, posZ), 1, mc.thePlayer.inventory.getCurrentItem(),
                            0.0F, 0.0F, 0.0F)
                    );
                    mc.thePlayer.sendQueue.getNetworkManager().sendPacketNoEvent(new C03PacketPlayer.C05PacketPlayerLook
                            (mc.thePlayer.rotationYaw, 90f, false));
                }
            }
        }
    };

    int getItemSlot() {
        int itemCount = -1;

        for (int i = 8; i >= 0; i--) {
            ItemStack itemStack = mc.thePlayer.inventory.getStackInSlot(i);

            if (itemStack != null && itemStack.getItem() instanceof ItemBlock) {
                itemCount = i;
            }
        }

        return itemCount;
    }

    public DEVAntiVoid(String name, AntiVoidModule parent) {
        super(name, parent);
    }

    @Override
    public void onEnable() {
        posX = posY = posZ = 0;
    }
}
