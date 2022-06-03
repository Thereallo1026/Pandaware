package dev.africa.pandaware.impl.module.movement.jesus;

import dev.africa.pandaware.api.event.interfaces.EventCallback;
import dev.africa.pandaware.api.event.interfaces.EventHandler;
import dev.africa.pandaware.api.module.mode.ModuleMode;
import dev.africa.pandaware.impl.event.game.KeyEvent;
import dev.africa.pandaware.impl.event.player.CollisionEvent;
import dev.africa.pandaware.impl.event.player.MotionEvent;
import dev.africa.pandaware.impl.event.player.PacketEvent;
import dev.africa.pandaware.impl.module.movement.JesusModule;
import dev.africa.pandaware.utils.player.MovementUtils;
import dev.africa.pandaware.utils.player.PlayerUtils;
import net.minecraft.block.BlockLiquid;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.util.AxisAlignedBB;
import org.lwjgl.input.Keyboard;

public class VanillaJesus extends ModuleMode<JesusModule> {
    public VanillaJesus(String name, JesusModule parent) {
        super(name, parent);
    }

    @EventHandler
    EventCallback<CollisionEvent> onCollide = event -> {
        if (event.getBlock() instanceof BlockLiquid && PlayerUtils.checkWithLiquid(0.1f)) {
            if (mc.thePlayer != null && !mc.thePlayer.isSneaking()) {
                event.setCollisionBox(new AxisAlignedBB(event.getBlockPos(), event.getBlockPos().add(1, 1, 1)));
            }
        }
    };

    @EventHandler
    EventCallback<PacketEvent> onPacket = event -> {
        if (mc.thePlayer != null) {
            if (PlayerUtils.inLiquid()
                    && !mc.thePlayer.isSneaking()
                    && mc.thePlayer.fallDistance == 0.0f
                    && event.getPacket() instanceof C03PacketPlayer) {
                C03PacketPlayer c03PacketPlayer = event.getPacket();

                if (!MovementUtils.isMoving() && PlayerUtils.checkWithLiquid(0.1f)) event.cancel();

                c03PacketPlayer.setOnGround(mc.thePlayer.ticksExisted % 2 != 0);
                c03PacketPlayer.setY(c03PacketPlayer.getPositionY() + (mc.thePlayer.ticksExisted % 2 == 0 ? 0.00001 : -0.00001));
            }
        }
    };

    @EventHandler
    EventCallback<MotionEvent> onMotion = event -> {
        if (mc.thePlayer.isSneaking() || !PlayerUtils.inLiquid()) return;
        mc.thePlayer.motionY = 0.1F;
        mc.gameSettings.keyBindJump.pressed = false;
    };

    @EventHandler
    EventCallback<KeyEvent> onKey = event -> {
        if (PlayerUtils.checkWithLiquid(0.1f) && Keyboard.isKeyDown(mc.gameSettings.keyBindJump.getKeyCode())) {
            mc.gameSettings.keyBindJump.pressed = false;
            event.cancel();
        }
    };
}
