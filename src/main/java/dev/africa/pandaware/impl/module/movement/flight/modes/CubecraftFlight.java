package dev.africa.pandaware.impl.module.movement.flight.modes;

import dev.africa.pandaware.Client;
import dev.africa.pandaware.api.event.interfaces.EventCallback;
import dev.africa.pandaware.api.event.interfaces.EventHandler;
import dev.africa.pandaware.api.module.mode.ModuleMode;
import dev.africa.pandaware.impl.event.player.CollisionEvent;
import dev.africa.pandaware.impl.event.player.MoveEvent;
import dev.africa.pandaware.impl.event.player.PacketEvent;
import dev.africa.pandaware.impl.module.movement.flight.FlightModule;
import dev.africa.pandaware.impl.setting.NumberSetting;
import dev.africa.pandaware.impl.ui.notification.Notification;
import dev.africa.pandaware.utils.player.MovementUtils;
import lombok.var;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.server.S01PacketJoinGame;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import net.minecraft.util.AxisAlignedBB;

public class CubecraftFlight extends ModuleMode<FlightModule> {
    private final NumberSetting speed = new NumberSetting("Speed", 10, 0.1, 1, 0.1);

    public CubecraftFlight(String name, FlightModule parent) {
        super(name, parent);

        this.registerSettings(this.speed);
    }

    private boolean canFly;

    @EventHandler
    EventCallback<CollisionEvent> onCollision = event -> {
        if (mc.thePlayer != null && mc.thePlayer.fallDistance >= 2.7 && !canFly) {
            event.setCollisionBox(new AxisAlignedBB(-100, -2, -100, 100, 1, 100)
                    .offset(event.getBlockPos().getX(), event.getBlockPos().getY(), event.getBlockPos().getZ()));
            mc.thePlayer.sendQueue.getNetworkManager().sendPacketNoEvent(new C03PacketPlayer(true));
        }
    };

    @EventHandler
    EventCallback<MoveEvent> onMove = event -> {
        if (mc.thePlayer.hurtTime > 0) canFly = true;
        if (canFly) {
            event.y = mc.thePlayer.motionY = mc.gameSettings.keyBindJump.isKeyDown() ? 1
                    : mc.gameSettings.keyBindSneak.isKeyDown() ? -1 : 0.0F;
            MovementUtils.strafe(event, this.speed.getValue().doubleValue());
        }
    };

    @EventHandler
    EventCallback<PacketEvent> onPacket = event -> {
        if (event.getPacket() instanceof S01PacketJoinGame) this.getParent().toggle(false);
        if (event.getPacket() instanceof S08PacketPlayerPosLook && mc.thePlayer != null &&
                mc.thePlayer.ticksExisted > 15) {
            var packet = (S08PacketPlayerPosLook) event.getPacket();

            mc.thePlayer.sendQueue.getNetworkManager().sendPacket(new C03PacketPlayer.C06PacketPlayerPosLook(
                    packet.getX(), packet.getY(), packet.getZ(),
                    packet.getYaw(), packet.getPitch(), false
            ));

            event.cancel();
        }
    };

    @Override
    public void onEnable() {
        Client.getInstance().getNotificationManager().addNotification(Notification.Type.INFORMATION, "Take damage to fly", 2);
        canFly = false;
    }
}
