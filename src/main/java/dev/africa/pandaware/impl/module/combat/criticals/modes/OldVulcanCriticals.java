package dev.africa.pandaware.impl.module.combat.criticals.modes;

import dev.africa.pandaware.Client;
import dev.africa.pandaware.api.module.mode.ModuleMode;
import dev.africa.pandaware.impl.event.player.MotionEvent;
import dev.africa.pandaware.impl.module.combat.criticals.CriticalsModule;
import dev.africa.pandaware.impl.module.combat.criticals.ICriticalsMode;
import dev.africa.pandaware.impl.module.movement.flight.FlightModule;
import dev.africa.pandaware.utils.player.PlayerUtils;
import lombok.Getter;

public class OldVulcanCriticals extends ModuleMode<CriticalsModule> implements ICriticalsMode {
    public OldVulcanCriticals(String name, CriticalsModule parent) {
        super(name, parent);
    }

    @Getter
    private int stage;

    @Override
    public void handle(MotionEvent event, int ticksExisted) {
        if (mc.thePlayer == null || mc.theWorld == null) return;
        if (Client.getInstance().getModuleManager().getByClass(FlightModule.class).getData().isEnabled()) return;
        double yGround = mc.thePlayer.posY % 0.015625;
        if (this.isOnGround() && yGround >= 0 && yGround < .1 && !PlayerUtils.isBlockAbove(1)) {
            switch (stage++) {
                case 3:
                case 2: {
                    event.setY(event.getY() + 0.42f);
                    event.setOnGround(false);
                    break;
                }

                case 4:
                case 5: {
                    event.setY(event.getY() + 0.007531999805212);
                    event.setOnGround(false);
                    break;
                }

                case 6:
                case 7: {
                    event.setY(event.getY() + .00133597911214);
                    event.setOnGround(false);
                    break;
                }

                case 8:
                case 9: {
                    event.setY(event.getY() + 0.16610926093895);
                    event.setOnGround(false);
                    break;
                }
            }

            if (stage > 25) {
                stage = 0;
            }

            if (!mc.isMoveMoving() && mc.thePlayer.ticksExisted % 2 == 0) {
                double yaw = Math.toRadians(mc.thePlayer.rotationYaw);
                double amount = .1;
                double dX = -Math.sin(yaw) * amount;
                double dZ = Math.cos(yaw) * amount;

                event.setX(event.getX() + dX);
                event.setZ(event.getZ() + dZ);
            }

            event.setOnGround(event.isOnGround());
            event.setY(event.getY());
        }
    }

    @Override
    public void onEnable() {
        stage = 0;
    }

    @Override
    public void entityIsNull() {
        if (stage != 0) {
            this.stage++;
        }
    }

    private boolean isOnGround() {
        return !mc.theWorld.getCollidingBoundingBoxes(mc.thePlayer,
                mc.thePlayer.getEntityBoundingBox().offset(0.0D, -0.1, 0.0D)).isEmpty();
    }
}
