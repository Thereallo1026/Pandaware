package dev.africa.pandaware.impl.module.movement.longjump.modes;

import dev.africa.pandaware.api.event.Event;
import dev.africa.pandaware.api.event.interfaces.EventCallback;
import dev.africa.pandaware.api.event.interfaces.EventHandler;
import dev.africa.pandaware.api.module.mode.ModuleMode;
import dev.africa.pandaware.impl.event.player.MotionEvent;
import dev.africa.pandaware.impl.event.player.MoveEvent;
import dev.africa.pandaware.impl.module.movement.longjump.LongJumpModule;
import dev.africa.pandaware.utils.client.ServerUtils;
import dev.africa.pandaware.utils.player.MovementUtils;
import dev.africa.pandaware.utils.player.PlayerUtils;
import net.minecraft.potion.Potion;

public class HypixelLongJump extends ModuleMode<LongJumpModule> {
    private double lastDistance;

    @EventHandler
    EventCallback<MotionEvent> onMotion = event -> {
        if (event.getEventState() == Event.EventState.PRE) {
            this.lastDistance = MovementUtils.getLastDistance();
        }
    };

    @EventHandler
    EventCallback<MoveEvent> onMove = event -> {
        if ((ServerUtils.isOnServer("mc.hypixel.net") || ServerUtils.isOnServer("hypixel.net")) && !ServerUtils.compromised) {

            if (mc.thePlayer.onGround && mc.isMoveMoving()) {
                MovementUtils.strafe(MovementUtils.getBaseMoveSpeed() * 2.2);
                mc.thePlayer.jump();
                event.y = mc.thePlayer.motionY = 0.42f + (mc.thePlayer.isPotionActive(Potion.jump)
                        ? PlayerUtils.getJumpBoostMotion() * 1.1 : 0);
            }
            if (mc.thePlayer.moveStrafing != 0 && !mc.thePlayer.onGround) {
                MovementUtils.strafe(event, this.lastDistance * 0.905f);
            }

            if (mc.thePlayer.fallDistance > 0 && mc.thePlayer.fallDistance < 0.25 && !mc.thePlayer.isPotionActive(Potion.jump)) {
                mc.thePlayer.motionY = 0.04f;
            }
        }
    };

    public HypixelLongJump(String name, LongJumpModule parent) {
        super(name, parent);
    }

    @Override
    public void onEnable() {
        this.lastDistance = MovementUtils.getLastDistance();
    }
}
