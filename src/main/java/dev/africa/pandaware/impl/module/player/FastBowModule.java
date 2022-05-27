package dev.africa.pandaware.impl.module.player;

import dev.africa.pandaware.api.event.interfaces.EventCallback;
import dev.africa.pandaware.api.event.interfaces.EventHandler;
import dev.africa.pandaware.api.module.Module;
import dev.africa.pandaware.api.module.interfaces.Category;
import dev.africa.pandaware.api.module.interfaces.ModuleInfo;
import dev.africa.pandaware.impl.event.player.UpdateEvent;
import dev.africa.pandaware.impl.setting.NumberSetting;
import net.minecraft.init.Items;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import org.lwjgl.input.Mouse;

@ModuleInfo(name = "FastBow", description = "Funny", category = Category.PLAYER)
public class FastBowModule extends Module {

    private final NumberSetting packets = new NumberSetting("Packets", 20, 1, 5, 1);

    public FastBowModule() {
        registerSettings(
                packets
        );
    }

    @EventHandler
    private final EventCallback<UpdateEvent> updateEventEventCallback = event -> {
        if (Mouse.isButtonDown(1) && mc.thePlayer.getHeldItem() != null && mc.thePlayer.getHeldItem().getItem() == Items.bow) {
            mc.thePlayer.sendQueue.getNetworkManager().sendPacketNoEvent(new C08PacketPlayerBlockPlacement(mc.thePlayer.getHeldItem()));
            for (int i = 0; i < packets.getValue().intValue(); i++) {
                mc.thePlayer.sendQueue.getNetworkManager().sendPacketNoEvent(new C03PacketPlayer.C06PacketPlayerPosLook(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch, true));
            }
            mc.thePlayer.sendQueue.getNetworkManager().sendPacketNoEvent(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN));
        }
    };

}
