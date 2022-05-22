package dev.africa.pandaware.impl.module.misc;

import dev.africa.pandaware.Client;
import dev.africa.pandaware.api.event.interfaces.EventCallback;
import dev.africa.pandaware.api.event.interfaces.EventHandler;
import dev.africa.pandaware.api.module.Module;
import dev.africa.pandaware.api.module.interfaces.Category;
import dev.africa.pandaware.api.module.interfaces.ModuleInfo;
import dev.africa.pandaware.impl.event.player.PacketEvent;
import dev.africa.pandaware.impl.module.movement.flight.FlightModule;
import dev.africa.pandaware.impl.module.movement.longjump.LongJumpModule;
import dev.africa.pandaware.impl.module.movement.speed.SpeedModule;
import dev.africa.pandaware.impl.setting.BooleanSetting;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;

@ModuleInfo(name = "Auto Disable", description = "stop flagging retard", category = Category.MISC)
public class AutoDisableModule extends Module {
    private final BooleanSetting speed = new BooleanSetting("Speed", false);
    private final BooleanSetting fly = new BooleanSetting("Fly", false);
    private final BooleanSetting longjump = new BooleanSetting("Longjump", false);

    public AutoDisableModule() {
        this.registerSettings(
                this.speed,
                this.fly,
                this.longjump
        );
    }

    @EventHandler
    EventCallback<PacketEvent> onPacket = event -> {
        if (event.getPacket() instanceof S08PacketPlayerPosLook) {
            FlightModule flightModule = Client.getInstance().getModuleManager().getByClass(FlightModule.class);
            if (flightModule.getData().isEnabled() && this.fly.getValue()) {
                flightModule.getData().setEnabled(false);
            }

            SpeedModule speedModule = Client.getInstance().getModuleManager().getByClass(SpeedModule.class);
            if (speedModule.getData().isEnabled() && this.speed.getValue()) {
                speedModule.getData().setEnabled(false);
            }

            LongJumpModule longJumpModule = Client.getInstance().getModuleManager().getByClass(LongJumpModule.class);
            if (longJumpModule.getData().isEnabled() && this.longjump.getValue()) {
                longJumpModule.getData().setEnabled(false);
            }
        }
    };
}
