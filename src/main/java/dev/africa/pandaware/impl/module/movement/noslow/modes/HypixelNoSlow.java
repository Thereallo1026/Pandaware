package dev.africa.pandaware.impl.module.movement.noslow.modes;

import dev.africa.pandaware.api.event.Event;
import dev.africa.pandaware.api.event.interfaces.EventCallback;
import dev.africa.pandaware.api.event.interfaces.EventHandler;
import dev.africa.pandaware.api.module.mode.ModuleMode;
import dev.africa.pandaware.impl.event.player.MotionEvent;
import dev.africa.pandaware.impl.module.movement.noslow.NoSlowModule;

public class HypixelNoSlow extends ModuleMode<NoSlowModule> {
    public HypixelNoSlow(String name, NoSlowModule parent) {
        super(name, parent);
    }

    @EventHandler
    EventCallback<MotionEvent> onMotion = event -> {

        boolean usingItem = mc.thePlayer.isUsingItem() && mc.thePlayer.getCurrentEquippedItem() != null && mc.isMoveMoving();

        if (usingItem && event.getEventState() == Event.EventState.PRE &&
                mc.thePlayer.onGround && mc.thePlayer.ticksExisted % 2 == 0) {
            event.setY(event.getY() + 0.05);
            event.setOnGround(false);
        }
    };
}
