package dev.africa.pandaware.impl.module.combat.antibot;

import dev.africa.pandaware.api.module.Module;
import dev.africa.pandaware.api.module.interfaces.Category;
import dev.africa.pandaware.api.module.interfaces.ModuleInfo;
import dev.africa.pandaware.impl.module.combat.antibot.modes.TicksAntiBot;

@ModuleInfo(name = "Anti Bot", category = Category.COMBAT)
public class AntiBotModule extends Module {
    public AntiBotModule() {
        this.registerModes(
                new TicksAntiBot("Ticks", this)
        );
    }
}
