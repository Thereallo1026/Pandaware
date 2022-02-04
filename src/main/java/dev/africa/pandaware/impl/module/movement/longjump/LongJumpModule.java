package dev.africa.pandaware.impl.module.movement.longjump;

import dev.africa.pandaware.api.module.Module;
import dev.africa.pandaware.api.module.interfaces.Category;
import dev.africa.pandaware.api.module.interfaces.ModuleInfo;
import dev.africa.pandaware.impl.module.movement.longjump.modes.VanillaLongjump;
import dev.africa.pandaware.impl.module.movement.longjump.modes.WatchdogLongJump;
import dev.africa.pandaware.impl.setting.NumberSetting;
import dev.africa.pandaware.utils.player.MovementUtils;
import lombok.Getter;

@Getter
@ModuleInfo(name = "Long Jump", category = Category.MOVEMENT)
public class LongJumpModule extends Module {
    private final NumberSetting speed = new NumberSetting("Speed", 10, 0, 1, 0.05);

    public LongJumpModule() {
        this.registerModes(
                new VanillaLongjump("Vanilla", this),
                new WatchdogLongJump("Watchdog", this)
        );

        this.registerSettings(this.speed);
    }

    @Override
    public void onDisable() {
        MovementUtils.slowdown();
    }

    @Override
    public String getSuffix() {
        return this.getCurrentMode().getName();
    }
}
