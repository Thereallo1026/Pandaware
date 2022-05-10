package dev.africa.pandaware.impl.module.movement.speed.modes;

import dev.africa.pandaware.api.event.Event;
import dev.africa.pandaware.api.event.interfaces.EventCallback;
import dev.africa.pandaware.api.event.interfaces.EventHandler;
import dev.africa.pandaware.api.module.mode.ModuleMode;
import dev.africa.pandaware.impl.event.player.MotionEvent;
import dev.africa.pandaware.impl.event.player.MoveEvent;
import dev.africa.pandaware.impl.module.movement.speed.SpeedModule;
import dev.africa.pandaware.utils.client.ServerUtils;
import dev.africa.pandaware.utils.player.MovementUtils;
import dev.africa.pandaware.utils.player.PlayerUtils;
import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraft.potion.Potion;

public class HypixelSpeed extends ModuleMode<SpeedModule> {
    private boolean jumped;
    private double lastDistance;
    private double movespeed;
    @EventHandler
    EventCallback<MotionEvent> onMotion = event -> {
        if (mc.getCurrentServerData() != null && mc.getCurrentServerData().serverIP.endsWith("hypixel.net") &&
                !(mc.currentScreen instanceof GuiMultiplayer) && !(ServerUtils.compromised)) {
            if (event.getEventState() == Event.EventState.PRE) {
                if (mc.isMoveMoving()) {
                    this.lastDistance = MovementUtils.getLastDistance();
                    mc.gameSettings.keyBindJump.pressed = false;
                }
            }
        }
    };

    @EventHandler
    protected final EventCallback<MoveEvent> onMove = event -> {
        if (mc.getCurrentServerData() != null && mc.getCurrentServerData().serverIP.endsWith("hypixel.net") &&
                !(mc.currentScreen instanceof GuiMultiplayer) && !(ServerUtils.compromised)) {
            if (mc.thePlayer.isInWater() || mc.thePlayer.isInLava()) return;
            if (mc.thePlayer.onGround && mc.isMoveMoving()) {
                double motion = 0.4F;
                motion += PlayerUtils.getJumpBoostMotion();

                event.y = mc.thePlayer.motionY = motion;
                this.movespeed = (MovementUtils.getBaseMoveSpeed() * 1.73);
                this.jumped = true;
            } else if (this.jumped) {
                this.movespeed = this.lastDistance - 0.67F * (this.lastDistance - MovementUtils.getBaseMoveSpeed());
                this.jumped = false;
            } else {
                this.movespeed = this.lastDistance - this.lastDistance / 94.3;
                if (mc.thePlayer.moveStrafing > 0) {
                    double multi = (MovementUtils.getSpeed() - this.lastDistance) * MovementUtils.getBaseMoveSpeed();

                    this.movespeed += multi;
                    this.movespeed -= 0.013f;
                }
            }
            if (mc.thePlayer.getAirTicks() == 5 && !mc.thePlayer.isPotionActive(Potion.jump)) {
                event.y = mc.thePlayer.motionY = -0.02;
            }
            MovementUtils.strafe(event, this.movespeed = Math.max(this.movespeed, MovementUtils.getBaseMoveSpeed()));
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

    public HypixelSpeed(String name, SpeedModule parent) {
        super(name, parent);
    }
}
