package dev.africa.pandaware.impl.module.movement;

import dev.africa.pandaware.api.module.Module;
import dev.africa.pandaware.api.module.interfaces.Category;
import dev.africa.pandaware.api.module.interfaces.ModuleInfo;
import dev.africa.pandaware.impl.setting.NumberSetting;

@ModuleInfo(name = "Timer", category = Category.MOVEMENT)
public class TimerModule extends Module {

    private final NumberSetting speed = new NumberSetting("Speed", 10, 0.1, 2, 0.01);

    public TimerModule() {
        this.registerSettings(this.speed);
    }

    @Override
    public void onEnable() {
        mc.timer.timerSpeed = this.speed.getValue().floatValue();
    }

    @Override
    public void onDisable() {
        mc.timer.timerSpeed = 1F;
    }
}