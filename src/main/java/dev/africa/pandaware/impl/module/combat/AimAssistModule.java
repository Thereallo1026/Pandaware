package dev.africa.pandaware.impl.module.combat;

import dev.africa.pandaware.api.event.Event;
import dev.africa.pandaware.api.event.interfaces.EventCallback;
import dev.africa.pandaware.api.event.interfaces.EventHandler;
import dev.africa.pandaware.api.module.Module;
import dev.africa.pandaware.api.module.interfaces.Category;
import dev.africa.pandaware.api.module.interfaces.ModuleInfo;
import dev.africa.pandaware.impl.event.player.MotionEvent;
import dev.africa.pandaware.impl.setting.NumberSetting;
import dev.africa.pandaware.utils.math.vector.Vec2f;
import dev.africa.pandaware.utils.player.RotationUtils;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MathHelper;

import java.util.Comparator;

@Getter
@ModuleInfo(name = "Aim Assist", description = "Aims at children", category = Category.COMBAT)
public class AimAssistModule extends Module {

    private final NumberSetting distance = new NumberSetting("Distance", 10D, 1D, 4D, 0.5D);
    private final NumberSetting smoothing = new NumberSetting("Smoothing", 2F, 0.1F, 2F, 0.1F);

    public static int mouseX, mouseY;

    public AimAssistModule() {
        this.registerSettings(
                this.distance,
                this.smoothing
        );
    }

    @Override
    public void onEnable() {
        super.onEnable();
    }

    @Override
    public void onDisable() {
        mouseX = mouseY = 0;
        super.onDisable();
    }

    @EventHandler
    EventCallback<MotionEvent> onRender = event -> {
        if (event.getEventState() != Event.EventState.PRE) return;

        double distance = this.distance.getValue().doubleValue();

        EntityPlayer target = mc.theWorld.playerEntities.stream().filter(entityPlayer -> entityPlayer != mc.thePlayer
                        && entityPlayer.isEntityAlive()
                        && mc.thePlayer.canEntityBeSeen(entityPlayer)
                        && entityPlayer.getDistanceSqToEntity(mc.thePlayer) < (distance * distance))
                .min(Comparator.comparingDouble(player -> player.getDistanceSqToEntity(mc.thePlayer))).orElse(null);

        if (target == null) {

            mouseX = mouseY = 0;

            return;
        }

        Vec2f targetRotations = RotationUtils.getRotations(target);
        Vec2f currentRotations = new Vec2f(mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch);

        float offsetX = MathHelper.wrapAngleTo180_float(targetRotations.getX() - currentRotations.getX());
        float offsetY = MathHelper.clamp_float(targetRotations.getY() - currentRotations.getY(), -90F, 90F);

        float fpt = Minecraft.getDebugFPS() / 20F;

        float smoothing = this.smoothing.getValue().floatValue();

        float smoothX = (offsetX / smoothing) / fpt;
        float smoothY = (offsetY / smoothing) / fpt;

        float f = this.mc.gameSettings.mouseSensitivity * 0.6F + 0.2F;
        float f1 = f * f * f * 8.0F;

        mouseX = Math.round(smoothX / f1);
    };
}