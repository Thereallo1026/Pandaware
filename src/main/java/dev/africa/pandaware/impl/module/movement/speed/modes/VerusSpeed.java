package dev.africa.pandaware.impl.module.movement.speed.modes;

import dev.africa.pandaware.api.event.Event;
import dev.africa.pandaware.api.event.interfaces.EventCallback;
import dev.africa.pandaware.api.event.interfaces.EventHandler;
import dev.africa.pandaware.api.module.mode.ModuleMode;
import dev.africa.pandaware.impl.event.player.MotionEvent;
import dev.africa.pandaware.impl.event.player.MoveEvent;
import dev.africa.pandaware.impl.module.movement.speed.SpeedModule;
import dev.africa.pandaware.impl.setting.EnumSetting;
import dev.africa.pandaware.utils.player.MovementUtils;
import lombok.AllArgsConstructor;
import net.minecraft.potion.Potion;

public class VerusSpeed extends ModuleMode<SpeedModule> {
    private final EnumSetting<VerusMode> verusMode = new EnumSetting<>("Verus mode", VerusMode.BHOP);

    public VerusSpeed(String name, SpeedModule parent) {
        super(name, parent);

        this.registerSettings(this.verusMode);
    }

    private int stage;

    @Override
    public void onEnable() {
        this.stage = 0;
    }

    @EventHandler
    EventCallback<MotionEvent> onMotion = event -> {
        if (event.getEventState() == Event.EventState.PRE) {
            switch (this.verusMode.getValue()) {
                case BHOP:
                case SLAB:
                    if (mc.isMoveMoving()) {
                        double speedAmplifier = (mc.thePlayer.isPotionActive(Potion.moveSpeed)
                                ? ((mc.thePlayer.getActivePotionEffect(Potion.moveSpeed).getAmplifier() + 1) * 0.15) : 0);

                        mc.gameSettings.keyBindJump.pressed = false;
                        if (mc.thePlayer.onGround) {
                            mc.thePlayer.jump();
                            MovementUtils.strafe(0.48 + speedAmplifier);
                        } else {
                            if (this.verusMode.getValue() == VerusMode.SLAB) {
                                if (mc.thePlayer.getAirTicks() == 2) {
                                    mc.thePlayer.motionY = -0.0784000015258789;
                                }
                            }
                        }

                        MovementUtils.strafe(MovementUtils.getSpeed());
                    }
                    break;
            }
        }
    };

    @EventHandler
    EventCallback<MoveEvent> onMove = event -> {
        switch (this.verusMode.getValue()) {
            case BHOP:
            case SLAB:
                if (mc.isMoveMoving()) {
                    MovementUtils.strafe(event);
                }
                break;
            case Y_PORT:
                if (mc.isMoveMoving()) {
                    if (mc.thePlayer.isCollidedHorizontally) {
                        if (mc.thePlayer.onGround) {
                            this.stage = 0;
                            event.y = mc.thePlayer.motionY = 0.42F;
                        }
                    } else {
                        double speedAmplifier = (mc.thePlayer.isPotionActive(Potion.moveSpeed)
                                ? ((mc.thePlayer.getActivePotionEffect(Potion.moveSpeed).getAmplifier() + 1) * 0.1) : 0);

                        mc.gameSettings.keyBindJump.pressed = false;
                        double baseSpeed = 0.2873D;

                        if (mc.thePlayer.onGround) {
                            this.stage++;
                            double sped = 2.13 + speedAmplifier;
                            if (this.stage < 2) {
                                sped -= 0.4;
                            }

                            MovementUtils.strafe(event, baseSpeed * sped);
                            event.y = 0.42f;
                            mc.thePlayer.motionY = 0;
                        } else {
                            if (mc.thePlayer.getAirTicks() == 1) {
                                event.y = mc.thePlayer.motionY = -0.0784000015258789;
                            }
                        }

                        MovementUtils.strafe();
                    }
                } else {
                    this.stage = 0;
                }
                break;
        }
    };

    @AllArgsConstructor
    private enum VerusMode {
        BHOP("Bhop"),
        Y_PORT("YPort"),
        GROUND("Ground"),
        SLAB("Slab");

        private final String label;
    }

    @Override
    public String getInformationSuffix() {
        return this.verusMode.getValue().label;
    }
}
