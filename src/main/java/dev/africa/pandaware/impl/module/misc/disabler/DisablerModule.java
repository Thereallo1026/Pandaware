package dev.africa.pandaware.impl.module.misc.disabler;

import dev.africa.pandaware.api.module.Module;
import dev.africa.pandaware.api.module.interfaces.ModuleInfo;
import dev.africa.pandaware.impl.module.misc.disabler.modes.BlocksMCDisabler;
import dev.africa.pandaware.impl.module.misc.disabler.modes.FuncraftDisabler;
import dev.africa.pandaware.impl.module.misc.disabler.modes.NoSprintDisabler;
import dev.africa.pandaware.impl.module.misc.disabler.modes.WatchdogDisabler;

@ModuleInfo(name = "Disabler")
public class DisablerModule extends Module {
    public DisablerModule() {
        this.registerModes(
                new NoSprintDisabler("No Sprint", this),
                new BlocksMCDisabler("Blocks MC", this),
                new FuncraftDisabler("Funcraft", this),
                new WatchdogDisabler("Watchdog", this)
        );
    }

    @Override
    public String getSuffix() {
        String add = this.getCurrentMode().getInformationSuffix() != null ? " "
                + this.getCurrentMode().getInformationSuffix() : "";

        return this.getCurrentMode().getName() + add;
    }
}
