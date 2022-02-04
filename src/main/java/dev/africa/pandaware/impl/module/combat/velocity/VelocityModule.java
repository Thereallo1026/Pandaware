package dev.africa.pandaware.impl.module.combat.velocity;


import dev.africa.pandaware.api.module.Module;
import dev.africa.pandaware.api.module.interfaces.Category;
import dev.africa.pandaware.api.module.interfaces.ModuleInfo;
import dev.africa.pandaware.impl.module.combat.velocity.modes.CustomVelocity;
import dev.africa.pandaware.impl.module.combat.velocity.modes.GroundVelocity;
import dev.africa.pandaware.impl.module.combat.velocity.modes.PacketVelocity;
import dev.africa.pandaware.impl.module.combat.velocity.modes.TickVelocity;

@ModuleInfo(name = "Velocity", category = Category.COMBAT)
public class VelocityModule extends Module {
    public VelocityModule() {
        this.registerModes(
                new PacketVelocity("Packet", this),
                new CustomVelocity("Custom", this),
                new GroundVelocity("Ground", this),
                new TickVelocity("Tick", this)
        );
    }

    @Override
    public String getSuffix() {
        return this.getCurrentMode().getName();
    }
}
