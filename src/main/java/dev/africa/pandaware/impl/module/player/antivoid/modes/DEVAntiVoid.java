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
                    mc.thePlayer.sendQueue.getNetworkManager().sendPacketNoEvent(new C08PacketPlayerBlockPlacement(
                            new BlockPos(posX, posY, posX), 256, mc.thePlayer.inventory.getCurrentItem(),
                            (float) (posX - 0.1f), (float) (posY - 0.1f), (float) (posZ - 0.1f)
                    ));
                    mc.thePlayer.sendQueue.getNetworkManager().sendPacketNoEvent(new C03PacketPlayer.C06PacketPlayerPosLook(
                            posX, posY + 1, posZ, mc.thePlayer.rotationYaw - 180, 80.5f, true
                    ));
                    mc.thePlayer.fallDistance = 0;
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
