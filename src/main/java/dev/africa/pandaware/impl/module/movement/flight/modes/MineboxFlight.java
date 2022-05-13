package dev.africa.pandaware.impl.module.movement.flight.modes;

import dev.africa.pandaware.Client;
import dev.africa.pandaware.api.event.interfaces.EventCallback;
import dev.africa.pandaware.api.event.interfaces.EventHandler;
import dev.africa.pandaware.api.module.mode.ModuleMode;
import dev.africa.pandaware.impl.event.player.MotionEvent;
import dev.africa.pandaware.impl.event.player.MoveEvent;
import dev.africa.pandaware.impl.event.player.PacketEvent;
import dev.africa.pandaware.impl.event.player.UpdateEvent;
import dev.africa.pandaware.impl.module.movement.flight.FlightModule;
import dev.africa.pandaware.impl.setting.NumberSetting;
import dev.africa.pandaware.impl.ui.notification.Notification;
import dev.africa.pandaware.utils.client.Location;
import dev.africa.pandaware.utils.client.ServerUtils;
import dev.africa.pandaware.utils.player.MovementUtils;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.*;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import net.minecraft.util.BlockPos;

public class MineboxFlight extends ModuleMode<FlightModule> {
    private final NumberSetting speed = new NumberSetting("Speed", 10, 0.1, 1, 0.1);

    private int stage;

    private Location lastTick;

    private boolean canExploit = false, wait = false;
    private int exploitTicks;

    public MineboxFlight(String name, FlightModule parent) {
        super(name, parent);

        this.registerSettings(this.speed);
    }

    @EventHandler
    EventCallback<PacketEvent> onPacket = event -> {
        if (event.getPacket() instanceof S08PacketPlayerPosLook) {

            if (!this.canExploit) {
                this.exploitTicks = 20;
                return;
            }

            S08PacketPlayerPosLook s08PacketPlayerPosLook = (S08PacketPlayerPosLook) event.getPacket();

            event.cancel();
            mc.thePlayer.sendQueue.getNetworkManager().sendPacketNoEvent(new C03PacketPlayer
                    .C06PacketPlayerPosLook(s08PacketPlayerPosLook.getX(),
                    s08PacketPlayerPosLook.getY(),
                    s08PacketPlayerPosLook.getZ(),
                    s08PacketPlayerPosLook.getYaw(),
                    s08PacketPlayerPosLook.getPitch(), true));
        }

        if (this.canExploit &&
                (event.getPacket() instanceof C03PacketPlayer || event.getPacket() instanceof C02PacketUseEntity)) {
            event.cancel();
        }
    };

    @EventHandler
    EventCallback<MoveEvent> onMove = event -> {
        if (this.wait && !this.canExploit) {
            if (mc.thePlayer.onGround) {
                event.y = mc.thePlayer.motionY = 0.42f;
            } else {

                event.y = mc.thePlayer.motionY -= 0.2f;

                if (mc.thePlayer.fallDistance > 0) {
                    mc.thePlayer.sendQueue.getNetworkManager().sendPacketNoEvent(new C03PacketPlayer.
                            C04PacketPlayerPosition(mc.thePlayer.posX,
                            mc.thePlayer.posY - 0.46f, mc.thePlayer.posZ, false));

                    this. canExploit = true;
                }
            }

            return;
        }

        if (!this.canExploit) {

            if (this.exploitTicks > 0) {
                event.x = mc.thePlayer.motionX = mc.thePlayer.motionZ = event.z = 0;
            } else {
                MovementUtils.strafe(event, MovementUtils.getBaseMoveSpeed());
            }
            return;
        }


        if (this.stage > 0) {

            event.y = mc.thePlayer.motionY = (GameSettings.isKeyDown(mc.gameSettings.keyBindJump) ? 0.42f :
                    (GameSettings.isKeyDown(mc.gameSettings.keyBindSneak) ? -0.42f : (mc.thePlayer.onGround ? 0.42f : 0f)));

            if (mc.isMoveMoving()) {
                MovementUtils.strafe(event, this.speed.getValue().doubleValue());
            }
        } else {
            event.x = mc.thePlayer.motionX = 0;
            event.z = mc.thePlayer.motionZ = 0;
        }
    };

    @EventHandler
    EventCallback<UpdateEvent> onUpdate = event -> {
        if (this.canExploit && mc.thePlayer.ticksExisted % 2 == 0) {
            this.lastTick = new Location(mc.thePlayer);
        }
    };

    @EventHandler
    EventCallback<MotionEvent> onMotion = event -> {
        event.setY(Math.floor(event.getY()));

        if (!this.canExploit) {

            if (!mc.thePlayer.onGround) {
                event.setOnGround(true);
            }

            mc.thePlayer.motionY = 0f;

            if (this.exploitTicks > 0) {

                switch (this.exploitTicks--) {

                    case 15: {
                        this.wait = true;
                        break;
                    }

                    case 19: {
                        mc.thePlayer.sendQueue.getNetworkManager().sendPacketNoEvent(new C08PacketPlayerBlockPlacement(new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - 1.5, mc.thePlayer.posZ), 1, new ItemStack(Blocks.stone.getItem(mc.theWorld, new BlockPos(-1, -1, -1))), 0, 0.94f, 0));
                        mc.thePlayer.sendQueue.getNetworkManager().sendPacketNoEvent(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY + 3.05, mc.thePlayer.posZ, false));
                        mc.thePlayer.sendQueue.getNetworkManager().sendPacketNoEvent(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, false));
                        mc.thePlayer.sendQueue.getNetworkManager().sendPacketNoEvent(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY + 0.41999998688697815, mc.thePlayer.posZ, true));
                        break;
                    }
                }
            }
        }

        if (this.canExploit) {
            if (this.stage++ < 2) {
                event.setOnGround(false);
            } else {
                mc.timer.timerSpeed = 1f;
            }
        }
    };

    @Override
    public void onEnable() {
        this.lastTick = null;

        this.stage = 0;

        if (!ServerUtils.isOnServer("juega.minebox.es")) {
            parent.toggle(false);

            Client.getInstance().getNotificationManager().addNotification(Notification.Type.ERROR, "Minebox only", 1);
        }

        this.exploitTicks = 0;
        this.canExploit = false;
        this.wait = false;
    }

    @Override
    public void onDisable() {
        if (this.lastTick != null) {
            mc.thePlayer.setPosition(this.lastTick.getX(), this.lastTick.getY() + .42f, this.lastTick.getZ());
        }

        mc.timer.timerSpeed = 1f;
        MovementUtils.strafe(0);
    }
}
