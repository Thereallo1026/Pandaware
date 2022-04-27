package dev.africa.pandaware.impl.module.movement.speed;

import dev.africa.pandaware.api.module.Module;
import dev.africa.pandaware.api.module.interfaces.Category;
import dev.africa.pandaware.api.module.interfaces.ModuleInfo;
import dev.africa.pandaware.impl.module.movement.speed.modes.*;
import dev.africa.pandaware.impl.setting.NumberSetting;
import dev.africa.pandaware.utils.player.MovementUtils;
import lombok.Getter;

@Getter
@ModuleInfo(name = "Speed", shortcut = {"zoom", "crack", "escapedapolice"}, category = Category.MOVEMENT)
public class SpeedModule extends Module {
    public SpeedModule() {
        this.registerModes(
                new BhopSpeed("Bhop", this),
                new NCPSpeed("NCP", this),
                new HypixelSpeed("Hypixel", this),
                new TickSpeed("Tick Speed", this),
                new SentinelSpeed("Sentinel", this),
                new VerusSpeed("Verus", this),
                new VulcanSpeed("Vulcan", this),
                new FuncraftSpeed("Funcraft", this),
                new CustomSpeed("Custom", this),
                new DEVSpeed("DEV", this)
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
