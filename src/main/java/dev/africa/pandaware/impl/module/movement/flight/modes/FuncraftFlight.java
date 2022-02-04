package dev.africa.pandaware.impl.module.movement.flight.modes;

import dev.africa.pandaware.api.event.Event;
import dev.africa.pandaware.api.event.interfaces.EventCallback;
import dev.africa.pandaware.api.event.interfaces.EventHandler;
import dev.africa.pandaware.api.module.mode.ModuleMode;
import dev.africa.pandaware.impl.event.player.MotionEvent;
import dev.africa.pandaware.impl.event.player.MoveEvent;
import dev.africa.pandaware.impl.module.movement.flight.FlightModule;
import dev.africa.pandaware.utils.math.random.RandomUtils;
import dev.africa.pandaware.utils.player.MovementUtils;
import dev.africa.pandaware.utils.player.PlayerUtils;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.potion.Potion;

public class FuncraftFlight extends ModuleMode<FlightModule> {
    public FuncraftFlight(String name, FlightModule parent) {
        super(name, parent);
    }

    private int stage;
    private double moveSpeed;
    private double lastDistance;
    private boolean jumped;

    @Override
    public void onEnable() {
        this.stage = 0;
        this.moveSpeed = MovementUtils.getBaseMoveSpeed();
        this.lastDistance = this.moveSpeed;
        this.jumped = !mc.thePlayer.onGround;
    }

    @EventHandler
    EventCallback<MotionEvent> onMotion = event -> {
        if (event.getEventState() == Event.EventState.PRE) {
            this.lastDistance = MovementUtils.getLastDistance();

            event.setOnGround(true);

            event.setY(event.getY() + RandomUtils.nextFloat(1.0E-9F, 1.0E-5F));
        }
    };

    @EventHandler
    EventCallback<MoveEvent> onMove = event -> {
        event.y = mc.thePlayer.motionY = -0.00001;

        if (this.stage < 40) {
            mc.timer.timerSpeed = 1.3f;
        } else {
            mc.timer.timerSpeed = 1f;
        }

        switch (this.stage) {
            case 0:
                for (int i = 0; i < 4; i++) {
                    mc.thePlayer.sendQueue.getNetworkManager().sendPacket(new C03PacketPlayer.C04PacketPlayerPosition(
                            mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, true
                    ));
                }
                double motion = 0.42F;
                motion += PlayerUtils.getJumpBoostMotion();

                event.y = mc.thePlayer.motionY = motion;

                this.moveSpeed = MovementUtils.getBaseMoveSpeed() * (mc.thePlayer.isPotionActive(Potion.moveSpeed)
                        ? 1.85f : 2.05f);
                break;

            case 1:
                this.moveSpeed *= 2.8f;
                break;

            case 2:
                this.moveSpeed = this.lastDistance - (0.09 * (this.lastDistance - MovementUtils.getBaseMoveSpeed()));
                break;

            default:
                this.moveSpeed = this.lastDistance - (this.lastDistance / 119.0f);
                break;
        }

        if (this.jumped) {
            this.moveSpeed = 0;
        }

        this.moveSpeed = Math.max(this.moveSpeed, MovementUtils.getBaseMoveSpeed());
        MovementUtils.strafe(event, this.moveSpeed);

        this.stage++;
    };
}
