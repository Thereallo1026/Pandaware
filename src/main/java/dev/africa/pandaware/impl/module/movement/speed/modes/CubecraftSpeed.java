package dev.africa.pandaware.impl.module.movement.speed.modes;

import dev.africa.pandaware.api.event.Event;
import dev.africa.pandaware.api.event.interfaces.EventCallback;
import dev.africa.pandaware.api.event.interfaces.EventHandler;
import dev.africa.pandaware.api.module.mode.ModuleMode;
import dev.africa.pandaware.impl.event.player.MotionEvent;
import dev.africa.pandaware.impl.module.movement.speed.SpeedModule;
import dev.africa.pandaware.utils.player.MovementUtils;
import net.minecraft.potion.Potion;

public class CubecraftSpeed extends ModuleMode<SpeedModule> {
    public CubecraftSpeed(String name, SpeedModule parent) {
        super(name, parent);
    }

    @EventHandler
    EventCallback<MotionEvent> onMove = event -> {
        if (event.getEventState() == Event.EventState.PRE) {
            if (mc.isMoveMoving()) {
                double speedAmplifier = (mc.thePlayer.isPotionActive(Potion.moveSpeed)
                        ? ((mc.thePlayer.getActivePotionEffect(Potion.moveSpeed).getAmplifier() + 1) * 0.03) : 0);

                mc.gameSettings.keyBindJump.pressed = false;
                if (mc.thePlayer.onGround) {
                    mc.thePlayer.jump();
                    MovementUtils.strafe((mc.thePlayer.isPotionActive(Potion.moveSpeed) ?
                            0.59 : 0.47) + speedAmplifier);
                } else {
                    if (mc.thePlayer.getAirTicks() == 3) {
                        mc.thePlayer.motionY -= 0.07;
                    }

                    if (mc.thePlayer.getAirTicks() == 4) {
                        mc.thePlayer.motionY += 0.01;
                    }
                }

                MovementUtils.strafe(MovementUtils.getSpeed());
            }
        }
    };
}
