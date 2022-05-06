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
    private double moveSpeed;
    @EventHandler
    EventCallback<MotionEvent> onMotion = event -> {
        if (mc.getCurrentServerData() != null && mc.getCurrentServerData().serverIP.endsWith("hypixel.net") &&
                !(mc.currentScreen instanceof GuiMultiplayer) && !(ServerUtils.compromised)) {
            if (event.getEventState() == Event.EventState.PRE) {
                if (mc.isMoveMoving()) {
                    lastDistance = MovementUtils.getLastDistance();
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
                moveSpeed = (MovementUtils.getBaseMoveSpeed() * 1.725);
                jumped = true;
            } else if (jumped) {
                moveSpeed = lastDistance - 0.67F * (lastDistance - MovementUtils.getBaseMoveSpeed());
                jumped = false;
            } else {
                if (mc.thePlayer.moveStrafing > 0) {
                    moveSpeed = lastDistance - lastDistance / 80;
                } else {
                    moveSpeed = lastDistance - lastDistance / 94.3;
                }
            }
            if (mc.thePlayer.getAirTicks() == 5 && !mc.thePlayer.isPotionActive(Potion.jump)) {
                event.y = mc.thePlayer.motionY = -0.02;
            }
            MovementUtils.strafe(event, moveSpeed = Math.max(moveSpeed, MovementUtils.getBaseMoveSpeed()));
        }
    };

    //AtomicReference<String> bobEsponja = new AtomicReference<>();
    public void onDisable() {
        lastDistance = 0;
        jumped = false;
        /*bobEsponja.set("0"); I found a better version and dont need rise anymore
        new Thread(() -> {
            try {
                Field bobesja = getClass().getDeclaredField("bobEsponja");
                AtomicReference<String> cumEsponja = (AtomicReference<String>) bobesja.get(getClass());
                moveSpeed = Double.parseDouble(String.valueOf(Long.parseLong(String.valueOf(cumEsponja.get()))));
            } catch (Exception e) {
                moveSpeed = 0;
                e.printStackTrace();
            }
        }).start();*/
        moveSpeed = 0;
    }

    public HypixelSpeed(String name, SpeedModule parent) {
        super(name, parent);
    }
}
