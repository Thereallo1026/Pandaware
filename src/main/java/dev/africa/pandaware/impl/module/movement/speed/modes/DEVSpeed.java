package dev.africa.pandaware.impl.module.movement.speed.modes;

import dev.africa.pandaware.api.event.Event;
import dev.africa.pandaware.api.event.interfaces.EventCallback;
import dev.africa.pandaware.api.event.interfaces.EventHandler;
import dev.africa.pandaware.api.module.mode.ModuleMode;
import dev.africa.pandaware.impl.event.player.MotionEvent;
import dev.africa.pandaware.impl.event.player.PacketEvent;
import dev.africa.pandaware.impl.module.movement.speed.SpeedModule;
import dev.africa.pandaware.utils.player.MovementUtils;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemEnderPearl;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.*;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import net.minecraft.util.BlockPos;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class DEVSpeed extends ModuleMode<SpeedModule> {
    public DEVSpeed(String name, SpeedModule parent) {
        super(name, parent);
    }

    private final List<Packet<?>> packets = new CopyOnWriteArrayList<>();
    private boolean work;
    private int slot;
    private boolean teleport;

    @Override
    public void onEnable() {
        this.teleport = false;
        this.work = false;
        this.slot = this.getBlockStack();
    }

    @Override
    public void onDisable() {
        MovementUtils.strafe(0);
        this.work = false;
        this.poll();
    }

    @EventHandler
    EventCallback<MotionEvent> onMotion = event -> {
            if (event.getEventState() == Event.EventState.POST) return;

            if (this.slot != -1 && !this.work) {
                this.work = true;

                boolean switchSlot = mc.thePlayer.inventory.currentItem != this.slot;

                if (switchSlot) {
                    mc.thePlayer.sendQueue.getNetworkManager()
                            .sendPacketNoEvent(
                                    new C09PacketHeldItemChange(this.slot)
                            );
                }

                event.setPitch(90);

                mc.thePlayer.sendQueue.getNetworkManager().sendPacketNoEvent(
                        new C08PacketPlayerBlockPlacement(
                                new BlockPos(-1, -1, -1), 255,
                                mc.thePlayer.inventory.getStackInSlot(this.slot),
                                0, 0, 0)
                );

                event.setPitch(90);

                if (switchSlot) {
                    mc.thePlayer.sendQueue.getNetworkManager().sendPacketNoEvent(
                            new C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem)
                    );
                }

                this.teleport = true;
            }

            if (this.work) {
                mc.thePlayer.motionY = 0;
                MovementUtils.strafe(0.8);
            }
    };

    private int getBlockStack() {
        int slot = -1;
        for (int i = 0; i < 9; i++) {
            ItemStack itemStack = mc.thePlayer.inventory.getStackInSlot(i);

            if (itemStack != null && itemStack.getItem() instanceof ItemEnderPearl) {
                slot = i;
                break;
            }
        }
        return slot;
    }

    @EventHandler
    EventCallback<PacketEvent> onPacket = event -> {

        if (event.getPacket() instanceof S08PacketPlayerPosLook) {

            event.cancel();

            S08PacketPlayerPosLook s08PacketPlayerPosLook = event.getPacket();

            mc.thePlayer.sendQueue.getNetworkManager().sendPacketNoEvent(
                    new C03PacketPlayer.C06PacketPlayerPosLook(s08PacketPlayerPosLook.getX(),
                            s08PacketPlayerPosLook.getY(),
                            s08PacketPlayerPosLook.getZ(),
                            s08PacketPlayerPosLook.getYaw(),
                            s08PacketPlayerPosLook.getPitch(), true)
            );
        }

        if (this.teleport) {
            if (event.getPacket() instanceof C02PacketUseEntity
                    || event.getPacket() instanceof C08PacketPlayerBlockPlacement
                    || event.getPacket() instanceof C07PacketPlayerDigging) event.cancel();

            if (event.getPacket() instanceof C03PacketPlayer || event.getPacket() instanceof C00PacketKeepAlive
                    || event.getPacket() instanceof C0FPacketConfirmTransaction) {
                event.cancel();
                this.packets.add(event.getPacket());
            }
        }
    };

    private void poll() {
        this.packets.forEach(packet -> mc.thePlayer.sendQueue.getNetworkManager().sendPacketNoEvent(packet));
        this.packets.clear();
    }
}