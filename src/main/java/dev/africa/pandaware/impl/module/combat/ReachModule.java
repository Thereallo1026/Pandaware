package dev.africa.pandaware.impl.module.combat;

import dev.africa.pandaware.api.module.Module;
import dev.africa.pandaware.api.module.interfaces.Category;
import dev.africa.pandaware.api.module.interfaces.ModuleInfo;
import dev.africa.pandaware.impl.setting.NumberSetting;
import lombok.Getter;

@Getter
@ModuleInfo(name = "Reach", description = "Huge arms, Just like Nik's dick", category = Category.COMBAT)
public class ReachModule extends Module {

    public static boolean enabled = false;
    public static final NumberSetting reach = new NumberSetting("Reach", 7D, 3D, 3.5D, 0.01D);

    public ReachModule() {
        this.registerSettings(
                reach
        );
    }

    @Override
    public void onEnable() {
        enabled = true;
        super.onEnable();
    }

    @Override
    public void onDisable() {
        enabled = false;
        super.onDisable();
    }
}