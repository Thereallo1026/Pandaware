package dev.africa.pandaware.impl.module.movement.speed;

import dev.africa.pandaware.api.interfaces.MinecraftInstance;
import dev.africa.pandaware.api.module.Module;
import dev.africa.pandaware.api.module.interfaces.Category;
import dev.africa.pandaware.api.module.interfaces.ModuleInfo;
import dev.africa.pandaware.impl.module.movement.speed.modes.*;
import dev.africa.pandaware.impl.setting.NumberSetting;
import dev.africa.pandaware.utils.player.MovementUtils;
import lombok.Getter;

@Getter
@ModuleInfo(name = "Speed", category = Category.MOVEMENT)
public class SpeedModule extends Module {
    private final NumberSetting speed = new NumberSetting("Speed", 10, 0, 1, 0.05);

    public SpeedModule() {
        this.registerModes(
                new VanillaSpeed("Vanilla", this),
                new BhopSpeed("Bhop", this),
                new SentinelSpeed("Sentinel", this),
                new VerusSpeed("Verus", this),
                new FuncraftSpeed("Funcraft", this),
                new WatchdogSpeed("Watchdog", this)
        );

        this.registerSettings(
                this.speed
        );
    }

    @Override
    public void onDisable() {
        MovementUtils.slowdown();
        mc.timer.timerSpeed = 1f;
    }

    @Override
    public String getSuffix() {
        String add = this.getCurrentMode().getInformationSuffix() != null ? " "
                + this.getCurrentMode().getInformationSuffix() : "";

        return this.getCurrentMode().getName() + add;
    }
}
