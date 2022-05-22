package dev.africa.pandaware.impl.module.render;

import dev.africa.pandaware.Client;
import dev.africa.pandaware.api.module.Module;
import dev.africa.pandaware.api.module.interfaces.Category;
import dev.africa.pandaware.api.module.interfaces.ModuleInfo;
import dev.africa.pandaware.impl.setting.BooleanSetting;
import dev.africa.pandaware.impl.setting.EnumSetting;
import dev.africa.pandaware.impl.ui.clickgui.GUIClickColor;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.lwjgl.input.Keyboard;

@Getter
@ModuleInfo(name = "Click GUI", description = "STOP LOOKING AT FEMBOYS FAGGOT", category = Category.VISUAL, key = Keyboard.KEY_RSHIFT)
public class ClickGUIModule extends Module {
    private final BooleanSetting showCummyMen = new BooleanSetting("Show Femboys", true);
    private final EnumSetting<FemboyMode> cummyMode = new EnumSetting<>("Femboy Mode", FemboyMode.GREEK,
            this.showCummyMen::getValue);
    private final EnumSetting<GUIClickColor> clickColor = new EnumSetting<>("Click Color", GUIClickColor.WHITE);

    public ClickGUIModule() {
        this.registerSettings(
                this.showCummyMen,
                this.cummyMode,
                this.clickColor

        );
    }

    @Override
    public void onEnable() {
        mc.displayGuiScreen(Client.getInstance().getClickGUI());

        this.toggle(false);
    }

    @AllArgsConstructor
    public enum FemboyMode {
        ASTOLFO("Astolfo"),
        ASTOLFO2("Astolfo 2"),
        NSFWASTOLFO("Astolfo (18+)"),
        FELIX("Felix"),
        FELIX2("Felix 2"),
        HIDERI("Hideri"),
        SAIKA("Saika"),
        VENTI("Venti"),
        GREEK("Greek"),
        PANDA("APandaWithAKnife");

        private final String label;
    }
}
