package dev.africa.pandaware.impl.module.movement.speed.modes;

import dev.africa.pandaware.api.event.Event;
import dev.africa.pandaware.api.event.interfaces.EventCallback;
import dev.africa.pandaware.api.event.interfaces.EventHandler;
import dev.africa.pandaware.api.module.mode.ModuleMode;
import dev.africa.pandaware.impl.event.player.MotionEvent;
import dev.africa.pandaware.impl.event.player.MoveEvent;
import dev.africa.pandaware.impl.module.movement.speed.SpeedModule;
import dev.africa.pandaware.impl.setting.EnumSetting;
import dev.africa.pandaware.utils.math.random.RandomUtils;
import dev.africa.pandaware.utils.player.MovementUtils;
import dev.africa.pandaware.utils.player.PlayerUtils;
import lombok.AllArgsConstructor;
import net.minecraft.potion.Potion;

public class VerusSpeed extends ModuleMode<SpeedModule> {
    private final EnumSetting<VerusMode> verusMode = new EnumSetting<>("Verus mode", VerusMode.BHOP);

    public VerusSpeed(String name, SpeedModule parent) {
        super(name, parent);

        this.registerSettings(this.verusMode);
    }

    private int stage;
    private double lastY;
    private boolean shouldSpeed;

    @Override
    public void onEnable() {
        this.shouldSpeed = false;
        this.stage = 0;
    }

    @Override
    public void onDisable() {
        if (verusMode.getValue() == VerusMode.GROUND) {
            mc.thePlayer.setPosition(mc.thePlayer.posX, lastY, mc.thePlayer.posZ);
        }
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
                            MovementUtils.strafe(0.49 + speedAmplifier);
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
                case GROUND:
                    if (event.getEventState() == Event.EventState.PRE) {
                        mc.gameSettings.keyBindJump.pressed = false;
                        boolean isSolidGround = (mc.thePlayer.posY == Math.round(mc.thePlayer.posY));
                        if (PlayerUtils.isMathGround()) {
                            event.setOnGround(true);

                            switch (this.stage++) {
                                case 0:
                                    MovementUtils.strafe(MovementUtils.getBaseMoveSpeed() * 0.4);
                                    break;
                                case 2:
                                case 1: {
                                    event.setOnGround(false);
                                    event.setY(event.getY() + 0.419999986886978);
                                    break;
                                }

                                case 4:
                                case 3: {
                                    event.setOnGround(false);
                                    event.setY(event.getY() + 0.341599985361099);
                                    break;
                                }

                                case 6:
                                case 5: {
                                    event.setOnGround(false);
                                    event.setY(event.getY() + 0.186367980844497);
                                    break;
                                }
                            }

                            this.stage++;
                            lastY = event.getY();

                            if (stage >= 24) {
                                stage = 0;
                                MovementUtils.strafe(MovementUtils.getBaseMoveSpeed());
                            }
                        } else if (!PlayerUtils.isMathGround() && !isSolidGround) {
                            stage = 0;
                            MovementUtils.strafe(MovementUtils.getBaseMoveSpeed());
                        }
                    }
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
            case YPORT:
                if (mc.isMoveMoving()) {
                    if (mc.thePlayer.isCollidedHorizontally) {
                        if (mc.thePlayer.onGround) {
                            this.stage = 0;
                            event.y = mc.thePlayer.motionY = 0.42F;
                        }
                    } else {
                        double speedAmplifier = (mc.thePlayer.isPotionActive(Potion.moveSpeed)
                                ? ((mc.thePlayer.getActivePotionEffect(Potion.moveSpeed).getAmplifier() + 1) * 0.2) : 0);

                        mc.gameSettings.keyBindJump.pressed = false;
                        double baseSpeed = 0.2873D;

                        if (mc.thePlayer.onGround) {
                            this.stage++;
                            double sped = 2.1 + speedAmplifier;
                            if (this.stage < 2) {
                                sped -= 0.4;
                            }

                            MovementUtils.strafe(event, baseSpeed * sped);
                            event.y = 0.42f;
                            mc.thePlayer.motionY = 0;
                        } else {
                            if (mc.thePlayer.getAirTicks() == 1) {
                                mc.thePlayer.motionY = -0.078400001525879;
                            }
                        }

                        MovementUtils.strafe();
                    }
                } else {
                    this.stage = 0;
                }
                break;
            case GROUND:
                if (mc.isMoveMoving()) {
                    this.shouldSpeed = true;
                }

                if (!mc.gameSettings.keyBindJump.pressed && this.shouldSpeed) {
                    if (stage >= 7 && mc.isMoveMoving()) {
                        float add = mc.thePlayer.isPotionActive(Potion.moveSpeed) ? 0.13f : -0.04f;
                        float sideways = (mc.gameSettings.keyBindLeft.pressed || mc.gameSettings.keyBindRight.pressed ||
                                mc.gameSettings.keyBindBack.pressed) ? mc.thePlayer.isPotionActive(Potion.moveSpeed) ? -0.12f : -0.06f : 0f;

                        if (mc.gameSettings.keyBindLeft.pressed || mc.gameSettings.keyBindRight.pressed) {
                            mc.thePlayer.setSprinting(false);
                        }

                        MovementUtils.strafe(event, MovementUtils.getSpeed(event) + ((0.25f + add) + sideways));
                        MovementUtils.strafe(event, MovementUtils.getSpeed(event) - RandomUtils.nextFloat(0.012f, 0.02f));
                    }
                }
        }
    };

    @AllArgsConstructor
    private enum VerusMode {
        BHOP("Bhop"),
        YPORT("YPort"),
        SLAB("Slab"),
        GROUND("Ground");

        private final String label;
    }

    @Override
    public String getInformationSuffix() {
        return this.verusMode.getValue().label;
    }
}
