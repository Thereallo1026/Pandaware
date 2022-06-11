package dev.africa.pandaware.impl.module.movement;

import dev.africa.pandaware.Client;
import dev.africa.pandaware.api.event.Event;
import dev.africa.pandaware.api.event.interfaces.EventCallback;
import dev.africa.pandaware.api.event.interfaces.EventHandler;
import dev.africa.pandaware.api.module.Module;
import dev.africa.pandaware.api.module.interfaces.Category;
import dev.africa.pandaware.api.module.interfaces.ModuleInfo;
import dev.africa.pandaware.impl.event.player.MotionEvent;
import dev.africa.pandaware.impl.event.player.MoveEvent;
import dev.africa.pandaware.impl.event.player.PacketEvent;
import dev.africa.pandaware.impl.setting.BooleanSetting;
import dev.africa.pandaware.utils.player.MovementUtils;
import dev.africa.pandaware.utils.player.PlayerUtils;
import lombok.Getter;
import net.minecraft.network.play.client.C0BPacketEntityAction;
import net.minecraft.potion.Potion;

@Getter
@ModuleInfo(name = "Sprint", category = Category.MOVEMENT)
public class SprintModule extends Module {
    private final BooleanSetting omniSprint = new BooleanSetting("OmniSprint", false);
    private final BooleanSetting applySpeed = new BooleanSetting("Apply Speed", false);
    private final BooleanSetting cancel = new BooleanSetting("Cancel Sprint", false);

    private boolean canSprint = mc.thePlayer != null && PlayerUtils.isMathGround() &&
            !mc.thePlayer.isPotionActive(Potion.blindness) && mc.thePlayer.getFoodStats().getFoodLevel() > 6 &&
            MovementUtils.isMoving() && !mc.thePlayer.isCollidedHorizontally;

    @EventHandler
    EventCallback<MotionEvent> onMotion = event -> {
        if (event.getEventState() == Event.EventState.PRE) {
            if (Client.getInstance().getModuleManager().getByClass(ScaffoldModule.class).getData().isEnabled()) return;
            if (omniSprint.getValue() && canSprint) {
                mc.thePlayer.setSprinting(true);
            } else if (mc.thePlayer.moveForward > 0 && canSprint) {
                mc.thePlayer.setSprinting(true);
            }
        }
    };

    @EventHandler
    EventCallback<MoveEvent> onMove = event -> {
        if (this.applySpeed.getValue() && canSprint && mc.thePlayer != null) {
            MovementUtils.strafe(event, MovementUtils.getBaseMoveSpeed());
        }
    };

    public SprintModule() {
        this.registerSettings(
                this.omniSprint,
                this.applySpeed,
                this.cancel
        );
    }

    @Override
    public void onEnable() {
        if (mc.thePlayer.isSprinting() && cancel.getValue()) {
            mc.thePlayer.sendQueue.getNetworkManager().sendPacketNoEvent(new C0BPacketEntityAction(mc.thePlayer,
                    C0BPacketEntityAction.Action.STOP_SPRINTING));
        }
    }

    @EventHandler
    EventCallback<PacketEvent> onPacket = event -> {
        if (event.getPacket() instanceof C0BPacketEntityAction && cancel.getValue()) {
            event.cancel();
        }
    };
}
