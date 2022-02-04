package dev.africa.pandaware.impl.module.misc.disabler.modes;

import dev.africa.pandaware.api.module.mode.ModuleMode;
import dev.africa.pandaware.impl.module.misc.disabler.DisablerModule;

public class WatchdogDisabler extends ModuleMode<DisablerModule> {
    public WatchdogDisabler(String name, DisablerModule parent) {
        super(name, parent);
    }


}
