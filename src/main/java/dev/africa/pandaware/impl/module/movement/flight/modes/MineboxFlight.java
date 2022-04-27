package dev.africa.pandaware.impl.module.movement.flight.modes;

import dev.africa.pandaware.api.event.interfaces.EventCallback;
import dev.africa.pandaware.api.event.interfaces.EventHandler;
import dev.africa.pandaware.api.module.mode.ModuleMode;
import dev.africa.pandaware.impl.event.player.MoveEvent;
import dev.africa.pandaware.impl.event.player.PacketEvent;
import dev.africa.pandaware.impl.module.movement.flight.FlightModule;
import dev.africa.pandaware.utils.player.MovementUtils;
import net.minecraft.network.play.client.C02PacketUseEntity;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.client.C0APacketAnimation;
import net.minecraft.network.play.client.C0BPacketEntityAction;

public class MineboxFlight extends ModuleMode<FlightModule> {
    public MineboxFlight(String name, FlightModule parent) {
        super(name, parent);
    }

    @EventHandler
    EventCallback<PacketEvent> onPacket = event -> {
        if (event.getPacket() instanceof C03PacketPlayer || event.getPacket() instanceof C02PacketUseEntity ||
                event.getPacket() instanceof C0BPacketEntityAction || event.getPacket() instanceof C0APacketAnimation) {
            event.cancel();
        }
    };

    @EventHandler
    EventCallback<MoveEvent> onMove = event -> {
        event.y = mc.thePlayer.motionY = mc.gameSettings.keyBindJump.isKeyDown() ? 0.7
                : mc.gameSettings.keyBindSneak.isKeyDown() ? -this.getParent().getSpeed().getValue().doubleValue() : 0.0F;
        MovementUtils.strafe(event, this.getParent().getSpeed().getValue().doubleValue());
    };
}
