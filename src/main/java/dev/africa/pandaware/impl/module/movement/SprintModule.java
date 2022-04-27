package dev.africa.pandaware.impl.module.movement;

import dev.africa.pandaware.Client;
import dev.africa.pandaware.api.event.Event;
import dev.africa.pandaware.api.event.interfaces.EventCallback;
import dev.africa.pandaware.api.event.interfaces.EventHandler;
import dev.africa.pandaware.api.module.Module;
import dev.africa.pandaware.api.module.interfaces.Category;
import dev.africa.pandaware.api.module.interfaces.ModuleInfo;
import dev.africa.pandaware.impl.event.player.MotionEvent;
import dev.africa.pandaware.impl.event.player.PacketEvent;
import dev.africa.pandaware.impl.setting.BooleanSetting;
import net.minecraft.network.play.client.C0BPacketEntityAction;

@ModuleInfo(name = "Sprint", category = Category.MOVEMENT)
public class SprintModule extends Module {
    private final BooleanSetting omniSprint = new BooleanSetting("OmniSprint", false);
    private final BooleanSetting applySpeed = new BooleanSetting("Apply Speed", false);
    private final BooleanSetting cancel = new BooleanSetting("Cancel Sprint", false);

    @EventHandler
    EventCallback<MotionEvent> onMotion = event -> {
        if (event.getEventState() == Event.EventState.PRE) {
            if (Client.getInstance().getModuleManager().getByClass(ScaffoldModule.class).getData().isEnabled()) return;
            if (omniSprint.getValue() && mc.isMoveMoving()) {
                mc.thePlayer.setSprinting(true);
            } else if (mc.thePlayer.moveForward > 0) {
                mc.thePlayer.setSprinting(true);
            }
            if (applySpeed.getValue() && !mc.thePlayer.isSneaking() && !mc.thePlayer.isCollidedHorizontally
                && mc.isMoveMoving() && !(mc.thePlayer.getGroundTicks() < 2)) {
            }
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
