package dev.africa.pandaware.impl.module.render;

import dev.africa.pandaware.Client;
import dev.africa.pandaware.api.interfaces.MinecraftInstance;
import dev.africa.pandaware.api.module.Module;
import dev.africa.pandaware.api.module.interfaces.Category;
import dev.africa.pandaware.api.module.interfaces.ModuleInfo;
import org.lwjgl.input.Keyboard;

@ModuleInfo(name = "Click GUI", description = "Opens gui", category = Category.VISUAL, key = Keyboard.KEY_RSHIFT)
public class ClickGUIModule extends Module {

    @Override
    public void onEnable() {
        mc.displayGuiScreen(Client.getInstance().getClickGUI());

        this.toggle(false);
    }
}
