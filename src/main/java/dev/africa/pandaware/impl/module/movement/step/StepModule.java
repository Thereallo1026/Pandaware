package dev.africa.pandaware.impl.module.movement.step;


import dev.africa.pandaware.api.module.Module;
import dev.africa.pandaware.api.module.interfaces.Category;
import dev.africa.pandaware.api.module.interfaces.ModuleInfo;
import dev.africa.pandaware.impl.module.movement.step.modes.NCPStep;
import dev.africa.pandaware.impl.module.movement.step.modes.VanillaStep;
import dev.africa.pandaware.impl.module.movement.step.modes.VerusStep;
import dev.africa.pandaware.impl.module.movement.step.modes.VulcanStep;
import dev.africa.pandaware.impl.setting.BooleanSetting;
import dev.africa.pandaware.impl.setting.NumberSetting;
import lombok.Getter;

@Getter
@ModuleInfo(name = "Step", category = Category.MOVEMENT)
public class StepModule extends Module {
    private NumberSetting stepHeight = new NumberSetting("Height", 2.0, 1, 1, 0.5);
    private NumberSetting stepTimer = new NumberSetting("Timer", 1.0, 0.1, 0.5, 0.1,
            () -> !this.difStepTimer.getValue());
    private BooleanSetting difStepTimer = new BooleanSetting("Use Different Timer", false);
    private NumberSetting stepTimer10 = new NumberSetting("1 Block Timer", 1.0, 0.1, 0.5, 0.1,
            this.difStepTimer::getValue);
    private NumberSetting stepTimer15 = new NumberSetting("1.5 Block Timer", 1.0, 0.1, 0.5, 0.1,
            this.difStepTimer::getValue);
    private NumberSetting stepTimer20 = new NumberSetting("2 Block Timer", 1.0, 0.1, 0.5, 0.1,
            this.difStepTimer::getValue);


    public StepModule() {
        this.registerModes(
                new NCPStep("NCP", this),
                new VanillaStep("Vanilla", this),
                new VerusStep("Verus", this),
                new VulcanStep("Vulcan", this)
        );
        this.registerSettings(
                this.stepHeight,
                this.stepTimer,
                this.difStepTimer,
                this.stepTimer10,
                this.stepTimer15,
                this.stepTimer20
        );
    }

    @Override
    public String getSuffix() {
        String add = this.getCurrentMode().getInformationSuffix() != null ? " "
                + this.getCurrentMode().getInformationSuffix() : "";

        return this.getCurrentMode().getName() + add;
    }
}
