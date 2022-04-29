package dev.africa.pandaware.impl.module.movement.speed.modes;

import dev.africa.pandaware.api.event.interfaces.EventCallback;
import dev.africa.pandaware.api.event.interfaces.EventHandler;
import dev.africa.pandaware.api.module.mode.ModuleMode;
import dev.africa.pandaware.impl.event.player.MoveEvent;
import dev.africa.pandaware.impl.module.movement.speed.SpeedModule;
import dev.africa.pandaware.utils.player.MovementUtils;
import net.minecraft.potion.Potion;

public class BlocksMCSpeed extends ModuleMode<SpeedModule> {
    public BlocksMCSpeed(String name, SpeedModule parent) {
        super(name, parent);
    }

    @EventHandler
    EventCallback<MoveEvent> onMove = event -> {
        if (mc.isMoveMoving()) {
            if (mc.thePlayer.onGround) {
                double speedAmplifier = (mc.thePlayer.isPotionActive(Potion.moveSpeed)
                        ? ((mc.thePlayer.getActivePotionEffect(Potion.moveSpeed).getAmplifier() + 1) * 0.025) : 0);
                event.y = mc.thePlayer.motionY = 0.42f;
                MovementUtils.strafe(0.49 + speedAmplifier);
            } else {
                MovementUtils.strafe(MovementUtils.getSpeed());
            }
        }
    };
}
