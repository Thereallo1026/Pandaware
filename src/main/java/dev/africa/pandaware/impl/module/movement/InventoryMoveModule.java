package dev.africa.pandaware.impl.module.movement;

import dev.africa.pandaware.api.event.interfaces.EventCallback;
import dev.africa.pandaware.api.event.interfaces.EventHandler;
import dev.africa.pandaware.api.interfaces.MinecraftInstance;
import dev.africa.pandaware.api.module.Module;
import dev.africa.pandaware.api.module.interfaces.Category;
import dev.africa.pandaware.api.module.interfaces.ModuleInfo;
import dev.africa.pandaware.impl.event.player.UpdateEvent;
import lombok.var;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.settings.KeyBinding;
import org.lwjgl.input.Keyboard;

@ModuleInfo(name = "Inventory Move", category = Category.MOVEMENT)
public class InventoryMoveModule extends Module {

    @EventHandler
    EventCallback<UpdateEvent> onUpdate = event -> {
        var keyBinds = new KeyBinding[]{
                mc.gameSettings.keyBindForward,
                mc.gameSettings.keyBindLeft,
                mc.gameSettings.keyBindRight,
                mc.gameSettings.keyBindBack,
                mc.gameSettings.keyBindJump
        };

        if (mc.currentScreen != null && !(mc.currentScreen instanceof GuiChat)) {
            for (KeyBinding keyBind : keyBinds) {
                keyBind.pressed = Keyboard.isKeyDown(keyBind.getKeyCode());
            }
        }
    };
}
