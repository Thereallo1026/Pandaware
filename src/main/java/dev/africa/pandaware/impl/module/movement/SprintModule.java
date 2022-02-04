package dev.africa.pandaware.impl.module.movement;

import dev.africa.pandaware.api.event.interfaces.EventCallback;
import dev.africa.pandaware.api.event.interfaces.EventHandler;
import dev.africa.pandaware.api.interfaces.MinecraftInstance;
import dev.africa.pandaware.api.module.Module;
import dev.africa.pandaware.api.module.interfaces.Category;
import dev.africa.pandaware.api.module.interfaces.ModuleInfo;
import dev.africa.pandaware.impl.event.player.UpdateEvent;

@ModuleInfo(name = "Sprint", category = Category.MOVEMENT)
public class SprintModule extends Module {

    @EventHandler
    EventCallback<UpdateEvent> onUpdate = event -> {
        if (mc.thePlayer.moveForward > 0 ) {
            mc.thePlayer.setSprinting(true);
        }
    };
}
