package dev.africa.pandaware.impl.module.player;

import dev.africa.pandaware.api.event.interfaces.EventCallback;
import dev.africa.pandaware.api.event.interfaces.EventHandler;
import dev.africa.pandaware.api.module.Module;
import dev.africa.pandaware.api.module.interfaces.Category;
import dev.africa.pandaware.api.module.interfaces.ModuleInfo;
import dev.africa.pandaware.impl.event.player.CollisionEvent;
import dev.africa.pandaware.impl.event.player.MoveEvent;
import dev.africa.pandaware.utils.player.MovementUtils;
import net.minecraft.util.AxisAlignedBB;

@ModuleInfo(name = "Phase", category = Category.PLAYER)
public class PhaseModule extends Module { //TODO: Actually make it work
    @EventHandler
    EventCallback<CollisionEvent> onTouchGrass = event -> {
        if (mc.thePlayer != null && mc.theWorld != null && mc.thePlayer.isSneaking() && mc.thePlayer.isCollidedHorizontally) {
            event.setCollisionBox(new AxisAlignedBB(-100, -2, -100, 100, 1, 100)
                    .offset(event.getBlockPos().getX() - 1, event.getBlockPos().getY() + 1, event.getBlockPos().getZ() - 1));
        }
    };

    @EventHandler
    EventCallback<MoveEvent> onMove = event -> {
        double x = -Math.sin(mc.thePlayer.rotationYaw) * 0.01;
        double z = -Math.sin(mc.thePlayer.rotationYaw) * 0.01;
        if (mc.thePlayer.isSneaking() && MovementUtils.isMoving()) {
            MovementUtils.strafe(event, MovementUtils.getBaseMoveSpeed());
            mc.thePlayer.setPositionAndUpdate(mc.thePlayer.posX + x, mc.thePlayer.posY + 0.01, mc.thePlayer.posZ + z);
        }
    };
}
