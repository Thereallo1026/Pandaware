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
        if (event.getBlock() instanceof BlockLiquid && PlayerUtils.checkWithLiquid(0.3f)) {
            if (mc.thePlayer != null && !mc.thePlayer.isSneaking()) {
                event.setCollisionBox(new AxisAlignedBB(event.getBlockPos(), event.getBlockPos().add(1, 1, 1)));
            }
        }
    };
}
