package dev.africa.pandaware.impl.module.movement.flight.modes;

import dev.africa.pandaware.api.event.interfaces.EventCallback;
import dev.africa.pandaware.api.event.interfaces.EventHandler;
import dev.africa.pandaware.api.module.mode.ModuleMode;
import dev.africa.pandaware.impl.event.player.CollisionEvent;
import dev.africa.pandaware.impl.event.player.MoveEvent;
import dev.africa.pandaware.impl.module.movement.flight.FlightModule;
import dev.africa.pandaware.impl.setting.EnumSetting;
import lombok.AllArgsConstructor;
import net.minecraft.util.AxisAlignedBB;

public class CollideFlight extends ModuleMode<FlightModule> {
    private final EnumSetting<Mode> mode = new EnumSetting<>("Mode", Mode.NORMAL);

    private double startY;

    public CollideFlight(String name, FlightModule parent) {
        super(name, parent);

        this.registerSettings(this.mode);
    }

    @EventHandler
    EventCallback<CollisionEvent> onCollision = event -> {
        switch (this.mode.getValue()) {
            case NORMAL:
                if (mc.thePlayer != null && !mc.thePlayer.isSneaking()) {
                    event.setCollisionBox(new AxisAlignedBB(-100, -2, -100, 100, 1, 100)
                            .offset(event.getBlockPos().getX(), event.getBlockPos().getY(), event.getBlockPos().getZ()));
                }
                break;
            case JUMP:
                if (mc.gameSettings.keyBindSneak.isKeyDown()) {
                    startY = Math.floor(mc.thePlayer.posY);
                }
                if (mc.thePlayer != null && !mc.thePlayer.isSneaking() && event.getBlockPos().getY() <= startY - 1) {
                    event.setCollisionBox(new AxisAlignedBB(-100, -2, -100, 100, 1, 100)
                            .offset(event.getBlockPos().getX(), event.getBlockPos().getY(), event.getBlockPos().getZ()));
                    startY = Math.floor(mc.thePlayer.posY);
                }
                break;
        }
    };

    @EventHandler
    EventCallback<MoveEvent> onMove = event -> {
        if (this.mode.getValue() == Mode.JUMP) {
            if (mc.thePlayer.posY == startY) {
                event.y = mc.thePlayer.motionY = 0.42f;
            }
        }
    };

    @Override
    public void onEnable() {
        startY = Math.floor(mc.thePlayer.posY);
    }

    @Override
    public void onDisable() {
        if (mode.getValue() == Mode.JUMP) {
            mc.gameSettings.keyBindJump.pressed = false;
        }
    }

    @AllArgsConstructor
    private enum Mode {
        NORMAL("Normal"),
        JUMP("Jump");

        private final String label;
    }
}
