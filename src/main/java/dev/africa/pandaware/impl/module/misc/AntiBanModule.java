package dev.africa.pandaware.impl.module.misc;

import dev.africa.pandaware.api.event.Event;
import dev.africa.pandaware.api.event.interfaces.EventCallback;
import dev.africa.pandaware.api.event.interfaces.EventHandler;
import dev.africa.pandaware.api.module.Module;
import dev.africa.pandaware.api.module.interfaces.Category;
import dev.africa.pandaware.api.module.interfaces.ModuleInfo;
import dev.africa.pandaware.impl.event.player.MotionEvent;
import dev.africa.pandaware.utils.client.Printer;

@ModuleInfo(name = "AntiBan", description = "SUMMER V5", category = Category.MISC)
public class AntiBanModule extends Module {
    @EventHandler
    EventCallback<MotionEvent> onMotion = event -> {
        if (event.getEventState() == Event.EventState.PRE) {
            Printer.chat("DONT USE ANTIBAN ON HYPIXEL");
            event.setY(event.getY());
            event.setX(event.getX());
            event.setZ(event.getZ());
            event.setYaw(event.getYaw());
            event.setPitch(event.getPitch());
            event.setOnGround(event.isOnGround());
        }
    };
}
