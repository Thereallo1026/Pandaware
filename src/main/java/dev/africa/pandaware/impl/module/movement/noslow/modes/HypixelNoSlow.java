package dev.africa.pandaware.impl.module.movement.noslow.modes;

import dev.africa.pandaware.Client;
import dev.africa.pandaware.api.event.Event;
import dev.africa.pandaware.api.event.interfaces.EventCallback;
import dev.africa.pandaware.api.event.interfaces.EventHandler;
import dev.africa.pandaware.api.module.mode.ModuleMode;
import dev.africa.pandaware.impl.event.player.MotionEvent;
import dev.africa.pandaware.impl.event.player.StepEvent;
import dev.africa.pandaware.impl.module.movement.noslow.NoSlowModule;
import dev.africa.pandaware.impl.module.movement.speed.SpeedModule;
import dev.africa.pandaware.impl.setting.BooleanSetting;
import dev.africa.pandaware.utils.client.ServerUtils;
import net.minecraft.client.gui.GuiMultiplayer;

public class HypixelNoSlow extends ModuleMode<NoSlowModule> {
    private final BooleanSetting slabSafe = new BooleanSetting("Slab Protection", false);

    public HypixelNoSlow(String name, NoSlowModule parent) {
        super(name, parent);

        this.registerSettings(this.slabSafe);
    }

    @EventHandler
    EventCallback<MotionEvent> onMotion = event -> {
        boolean usingItem = mc.thePlayer.isUsingItem() && mc.thePlayer.getCurrentEquippedItem() != null && mc.isMoveMoving();

        if (mc.getCurrentServerData() != null && mc.getCurrentServerData().serverIP.endsWith("hypixel.net") &&
                !(mc.currentScreen instanceof GuiMultiplayer) && !(ServerUtils.compromised)) {
            if (usingItem && mc.isMoveMoving() &&
                    !Client.getInstance().getModuleManager().getByClass(SpeedModule.class).getData().isEnabled()
                    && event.getEventState() == Event.EventState.PRE &&
                    mc.thePlayer.onGround && mc.thePlayer.ticksExisted % 2 == 0) {
                event.setY(event.getY() + 0.05);
                event.setOnGround(false);
            }
        }
    };

    @EventHandler
    EventCallback<StepEvent> onStep = event -> {
        boolean usingItem = mc.thePlayer.isUsingItem() && mc.thePlayer.getCurrentEquippedItem() != null && mc.isMoveMoving();

        if (mc.getCurrentServerData() != null && mc.getCurrentServerData().serverIP.endsWith("hypixel.net") &&
                !(mc.currentScreen instanceof GuiMultiplayer) && !(ServerUtils.compromised)) {
            if ((usingItem && mc.isMoveMoving()) && this.slabSafe.getValue() &&
                    !Client.getInstance().getModuleManager().getByClass(SpeedModule.class).getData().isEnabled()) {
                event.setStepHeight(0);
            }
        }
    };
}
