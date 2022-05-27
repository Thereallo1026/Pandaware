package dev.africa.pandaware.impl.module.player.nofall.modes;

import dev.africa.pandaware.Client;
import dev.africa.pandaware.api.event.Event;
import dev.africa.pandaware.api.event.interfaces.EventCallback;
import dev.africa.pandaware.api.event.interfaces.EventHandler;
import dev.africa.pandaware.api.module.mode.ModuleMode;
import dev.africa.pandaware.impl.event.player.MotionEvent;
import dev.africa.pandaware.impl.module.movement.flight.FlightModule;
import dev.africa.pandaware.impl.module.movement.flight.modes.VulcanFlight;
import dev.africa.pandaware.impl.module.player.nofall.NoFallModule;
import dev.africa.pandaware.impl.module.render.HUDModule;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;

public class VulcanNoFall extends ModuleMode<NoFallModule> {
    private boolean fixed;

    @EventHandler
    EventCallback<MotionEvent> onMotion = event -> {
        FlightModule fly = Client.getInstance().getModuleManager().getByClass(FlightModule.class);
        if (fly.getData().isEnabled()) return;
        if (event.getEventState() == Event.EventState.PRE) {
            if (mc.thePlayer.fallDistance > 2f) {
                fixed = false;
                mc.timer.timerSpeed = 0.9f;
            }
            if (mc.thePlayer.fallDistance > 3.5f) {
                mc.thePlayer.sendQueue.getNetworkManager().sendPacketNoEvent(new C03PacketPlayer(true));
                mc.thePlayer.motionY = -0.1f;
                mc.thePlayer.fallDistance = 0;
            }
            if (mc.thePlayer.onGround && !fixed) {
                fixed = true;
                mc.timer.timerSpeed = 1f;
            }
        }
    };

    public VulcanNoFall(String name, NoFallModule parent) {
        super(name, parent);
    }
}
