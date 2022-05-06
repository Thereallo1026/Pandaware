package dev.africa.pandaware.impl.module.movement.flight.modes;

import dev.africa.pandaware.api.event.Event;
import dev.africa.pandaware.api.event.interfaces.EventCallback;
import dev.africa.pandaware.api.event.interfaces.EventHandler;
import dev.africa.pandaware.api.module.mode.ModuleMode;
import dev.africa.pandaware.impl.event.player.MotionEvent;
import dev.africa.pandaware.impl.module.movement.flight.FlightModule;
import dev.africa.pandaware.utils.player.MovementUtils;
import net.minecraft.network.play.client.C03PacketPlayer;

public class VulcanFlight extends ModuleMode<FlightModule> {
    private double startY;
    @EventHandler
    EventCallback<MotionEvent> onMotion = event -> {
        if (event.getEventState() == Event.EventState.PRE) {
            if (mc.thePlayer.posY <= startY) MovementUtils.strafe(MovementUtils.getBaseMoveSpeed());
            if (mc.thePlayer.onGround) mc.thePlayer.motionY = 0.42f;
            if (mc.thePlayer.ticksExisted % 25 == 0) mc.thePlayer.motionY = -0.15;
            if (mc.thePlayer.fallDistance > 0.2 && mc.thePlayer.posY <= startY) {
                mc.timer.timerSpeed = 0.65f;
                mc.thePlayer.sendQueue.getNetworkManager().sendPacketNoEvent(new C03PacketPlayer(true));
                mc.thePlayer.motionY = -0.13;
                mc.thePlayer.fallDistance = 0;
            }
        }
    };

    public VulcanFlight(String name, FlightModule parent) {
        super(name, parent);
    }

    @Override
    public void onEnable() {
        startY = mc.thePlayer.posY;
    }
}
