package dev.africa.pandaware.impl.module.movement.longjump.modes;

import dev.africa.pandaware.api.event.Event;
import dev.africa.pandaware.api.event.interfaces.EventCallback;
import dev.africa.pandaware.api.event.interfaces.EventHandler;
import dev.africa.pandaware.api.module.mode.ModuleMode;
import dev.africa.pandaware.impl.event.player.MotionEvent;
import dev.africa.pandaware.impl.event.player.MoveEvent;
import dev.africa.pandaware.impl.module.movement.longjump.LongJumpModule;
import dev.africa.pandaware.utils.player.MovementUtils;
import net.minecraft.potion.Potion;

public class WatchdogLongJump extends ModuleMode<LongJumpModule> {
    public WatchdogLongJump(String name, LongJumpModule parent) {
        super(name, parent);
    }

    private boolean wasOnGround;
    private double lastDistance;

    @Override
    public void onEnable() {
        this.wasOnGround = mc.thePlayer.onGround;
        this.lastDistance = MovementUtils.getLastDistance();
    }

    @EventHandler
    EventCallback<MotionEvent> onMotion = event -> {
        if (event.getEventState() == Event.EventState.PRE) {
            this.lastDistance = MovementUtils.getLastDistance();
        }
    };

    @EventHandler
    EventCallback<MoveEvent> onMove = event -> {
        boolean hasSpeed = (mc.thePlayer.getActivePotionEffect(Potion.moveSpeed) != null);

        if (mc.thePlayer.onGround && this.wasOnGround) {
            event.y = mc.thePlayer.motionY = 0.42f;

            MovementUtils.strafe(event, this.getParent().getSpeed().getValue().doubleValue());
        }

        if (mc.thePlayer.moveStrafing != 0 && !mc.thePlayer.onGround) {
            MovementUtils.strafe(event, this.lastDistance * 0.95f);

            this.wasOnGround = true;
        }

        if (mc.thePlayer.fallDistance > 0) {
            if (mc.thePlayer.ticksExisted % 4 == 0 && mc.thePlayer.fallDistance < 1) {
                mc.thePlayer.motionY = -0.07225;
            } else if (mc.thePlayer.ticksExisted % 2 == 0 && mc.thePlayer.fallDistance < 1) {
                mc.thePlayer.motionY = -0.0762;
            } else if (mc.thePlayer.fallDistance < 1) {
                mc.thePlayer.motionY = -0.0787f;
            }
        }
    };
}
