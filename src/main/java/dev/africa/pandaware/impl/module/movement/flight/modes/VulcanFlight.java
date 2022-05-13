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
            if (mc.thePlayer.posY < startY) {
                if (mc.thePlayer.ticksExisted % 3 != 0) {
                    mc.thePlayer.motionY = -0.097 - (1E-8D);
                } else {
                    mc.thePlayer.motionY += 0.0132;
                }
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
