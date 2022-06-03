package dev.africa.pandaware.impl.module.movement.speed.modes;

import dev.africa.pandaware.api.event.Event;
import dev.africa.pandaware.api.event.interfaces.EventCallback;
import dev.africa.pandaware.api.event.interfaces.EventHandler;
import dev.africa.pandaware.api.module.mode.ModuleMode;
import dev.africa.pandaware.impl.event.game.TickEvent;
import dev.africa.pandaware.impl.event.player.MotionEvent;
import dev.africa.pandaware.impl.event.player.MoveEvent;
import dev.africa.pandaware.impl.module.movement.TargetStrafeModule;
import dev.africa.pandaware.impl.module.movement.speed.SpeedModule;
import dev.africa.pandaware.impl.setting.NumberSetting;
import dev.africa.pandaware.utils.client.ServerUtils;
import dev.africa.pandaware.utils.math.apache.ApacheMath;
import dev.africa.pandaware.utils.player.MovementUtils;
import dev.africa.pandaware.utils.player.PlayerUtils;
import net.minecraft.potion.Potion;

public class HypixelSpeed extends ModuleMode<SpeedModule> {
    private boolean jumped;
    private double lastDistance;
    private double movespeed;

    private final NumberSetting timer = new NumberSetting("Timer", 1.3, 1.0, 1, 0.1);

    public HypixelSpeed(String name, SpeedModule parent) {
        super(name, parent);

        this.registerSettings(
                this.timer
        );
    }


    @EventHandler
    EventCallback<MotionEvent> onMotion = event -> {
        if (event.getEventState() == Event.EventState.PRE) {
            this.lastDistance = ApacheMath.hypot(mc.thePlayer.posX - mc.thePlayer.prevPosX, mc.thePlayer.posZ - mc.thePlayer.prevPosZ);
        }
    };

    @EventHandler
    EventCallback<TickEvent> onTick = event -> {
        if ((ServerUtils.isOnServer("mc.hypixel.net") || ServerUtils.isOnServer("hypixel.net")) && !(ServerUtils.compromised)) {
            mc.timer.timerSpeed = this.timer.getValue().floatValue();
        }
    };

    @EventHandler
    protected final EventCallback<MoveEvent> onMove = event -> {
        if ((ServerUtils.isOnServer("mc.hypixel.net") || ServerUtils.isOnServer("hypixel.net")) &&
                !(ServerUtils.compromised) && MovementUtils.isMoving()) {
            if (PlayerUtils.inLiquid()) {
                this.getParent().toggle(false);
            }
            if (mc.thePlayer.onGround) {
                double motion = 0.41F;
                motion += PlayerUtils.getJumpBoostMotion();

                event.y = mc.thePlayer.motionY = motion;
                this.movespeed = (MovementUtils.getBaseMoveSpeed() * 1.95);
                this.jumped = true;
            } else if (this.jumped) {
                this.movespeed = this.lastDistance - 0.66F * (this.lastDistance - MovementUtils.getBaseMoveSpeed());
                this.jumped = false;
            } else {
                this.movespeed = this.lastDistance * (float) 0.91f;
                this.movespeed += mc.thePlayer.isPotionActive(Potion.moveSpeed) ? 0.042f : 0.033f;
                if (TargetStrafeModule.isStrafing() || mc.thePlayer.moveStrafing > 0) {
                    double multi = (MovementUtils.getSpeed() - this.lastDistance) * MovementUtils.getBaseMoveSpeed();

                    this.movespeed += multi;
                    this.movespeed -= 0.011f;
                }
            }

            MovementUtils.strafe(event, Math.max(this.movespeed, MovementUtils.getBaseMoveSpeed()));
        }
    };

    //AtomicReference<String> bobEsponja = new AtomicReference<>();
    public void onDisable() {
        this.lastDistance = 0;
        this.jumped = false;
        /*bobEsponja.set("0"); I found a better version and dont need rise anymore
        new Thread(() -> {
            try {
                Field bobesja = getClass().getDeclaredField("bobEsponja");
                AtomicReference<String> cumEsponja = (AtomicReference<String>) bobesja.get(getClass());
                this.movespeed = Double.parseDouble(String.valueOf(Long.parseLong(String.valueOf(cumEsponja.get()))));
            } catch (Exception e) {
                this.movespeed = 0;
                e.printStackTrace();
            }
        }).start();*/
        this.movespeed = 0;
    }

}
