package dev.africa.pandaware.impl.module.combat;

import dev.africa.pandaware.Client;
import dev.africa.pandaware.api.event.Event;
import dev.africa.pandaware.api.event.interfaces.EventCallback;
import dev.africa.pandaware.api.event.interfaces.EventHandler;
import dev.africa.pandaware.api.interfaces.MinecraftInstance;
import dev.africa.pandaware.api.module.Module;
import dev.africa.pandaware.api.module.interfaces.Category;
import dev.africa.pandaware.api.module.interfaces.ModuleInfo;
import dev.africa.pandaware.impl.event.player.*;
import dev.africa.pandaware.impl.event.render.RenderEvent;
import dev.africa.pandaware.impl.setting.BooleanSetting;
import dev.africa.pandaware.impl.setting.EnumSetting;
import dev.africa.pandaware.impl.setting.NumberRangeSetting;
import dev.africa.pandaware.impl.setting.NumberSetting;
import dev.africa.pandaware.impl.ui.UISettings;
import dev.africa.pandaware.utils.math.TimeHelper;
import dev.africa.pandaware.utils.network.ProtocolUtils;
import dev.africa.pandaware.utils.player.RotationUtils;
import dev.africa.pandaware.utils.render.ColorUtils;
import dev.africa.pandaware.utils.math.random.RandomUtils;
import dev.africa.pandaware.utils.math.vector.Vec2f;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemSword;
import net.minecraft.network.play.client.C02PacketUseEntity;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.util.*;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Getter
@ModuleInfo(name = "Kill Aura", shortcut = {"aura", "ka", "niggaslayer"}, description = "Attacks surrounding entities", category = Category.COMBAT)
public class KillAuraModule extends Module {
    private final TimeHelper timer = new TimeHelper();
    private final List<EntityLivingBase> entities = new ArrayList<>();
    private EntityLivingBase target;
    private int arrayIndex;

    private Vec2f rotationVector = new Vec2f(0, 0);
    private Vec2f packetRotationVector = new Vec2f(0, 0);

    private float smoothCamYaw;
    private float smoothCamPitch;
    private float smoothCamPartialTicks;
    private float smoothCamFilterX;
    private float smoothCamFilterY;
    private final MouseFilter filterX = new MouseFilter();
    private final MouseFilter filterY = new MouseFilter();

    private final BooleanSetting rotate = new BooleanSetting("Rotate", true);

    private final EnumSetting<TargetMode> targetMode = new EnumSetting<>("Target Mode", TargetMode.SINGLE);
    private final EnumSetting<SortingMode> sortingMode = new EnumSetting<>("Sorting Mode", SortingMode.DISTANCE);

    private final EnumSetting<AimMode> aimMode
            = new EnumSetting<>("Aim Mode", AimMode.NORMAl, this.rotate::getValue);
    private final EnumSetting<ClickMode> clickMode
            = new EnumSetting<>("Click Mode", ClickMode.SECURE_RANDOM);
    private final EnumSetting<RangeCalculation> rangeCalculationMode
            = new EnumSetting<>("Range Calculation Mode", RangeCalculation.RAYTRACE);
    private final EnumSetting<RenderEvent.Type> rotationEvent
            = new EnumSetting<>("Rotation Event Type", RenderEvent.Type.FRAME, this.rotate::getValue);
    private final EnumSetting<dev.africa.pandaware.api.event.Event.EventState> eventType
            = new EnumSetting<>("Event Type", dev.africa.pandaware.api.event.Event.EventState.PRE);
    private final EnumSetting<RotationUtils.RotationAt> lookAt
            = new EnumSetting<>("Look At", RotationUtils.RotationAt.CHEST, this.rotate::getValue);
    private final BooleanSetting autoBlock = new BooleanSetting("Auto Block", false);
    private final EnumSetting<AutoBlockMode> autoBlockMode
            = new EnumSetting<>("Auto Block Mode", AutoBlockMode.NORMAL, this.autoBlock::getValue);
    private final EnumSetting<dev.africa.pandaware.api.event.Event.EventState> blockState
            = new EnumSetting<>("Block State", dev.africa.pandaware.api.event.Event.EventState.POST, this.autoBlock::getValue);
    private final EnumSetting<dev.africa.pandaware.api.event.Event.EventState> unblockState
            = new EnumSetting<>("Unblock State", dev.africa.pandaware.api.event.Event.EventState.POST, this.autoBlock::getValue);
    private final EnumSetting<Strafemode> strafeMode
            = new EnumSetting<>("Strafe Mode", Strafemode.NONE);

    private final BooleanSetting cinematic = new BooleanSetting("Cinematic", false, this.rotate::getValue);
    private final NumberSetting range =
            new NumberSetting("Range", 6, 0.1, 4.5, 0.01);
    private final NumberRangeSetting aps =
            new NumberRangeSetting("APS", 20, 0, 9, 11, 0.5);
    private final NumberSetting cinematicSpeed = new NumberSetting("Cinematic Speed",
            1, 0, 0.05f, 0.01f, this.cinematic::getValue);
    private final NumberSetting switchSpeed = new NumberSetting("Switch Speed",
            20, 1, 3, 1, () -> this.targetMode.getValue() == TargetMode.SWITCH);
    private final NumberSetting attackAngle = new NumberSetting("Attack Angle",
            180, 1, 180, 1, this.rotate::getValue);
    private final NumberSetting hitChance = new NumberSetting("Hit Chance",
            100, 0, 100, 1);

    private final BooleanSetting middleRotation
            = new BooleanSetting("Middle Rotation", true, this.rotate::getValue);
    private final BooleanSetting randomizeAimPoint
            = new BooleanSetting("Randomize Aim Point", false, this.rotate::getValue);
    private final BooleanSetting gcd = new BooleanSetting("GCD", true, this.rotate::getValue);
    private final EnumSetting<GCDMode> gcdMode = new EnumSetting<>("GCD Mode", GCDMode.ADVANCED,
            () -> this.gcd.getValue() && this.rotate.getValue());
    private final BooleanSetting rotationFix = new BooleanSetting("Rotation Fix", true, this.rotate::getValue);
    private final BooleanSetting cinematicFilterAfterRotation
            = new BooleanSetting("Cinematic Filter After Rotation", false,
            () -> this.cinematic.getValue() && this.rotationEvent.getValue() == RenderEvent.Type.RENDER_3D && this.rotate.getValue());

    private final BooleanSetting keepRotation
            = new BooleanSetting("Keep Rotation", false, this.rotate::getValue);
    private final BooleanSetting keepSprint
            = new BooleanSetting("Keep Sprint", true);
    private final BooleanSetting swing
            = new BooleanSetting("Swing", true);
    private final BooleanSetting sprint
            = new BooleanSetting("Sprint", true);

    private final BooleanSetting players
            = new BooleanSetting("Players", true);
    private final BooleanSetting clientUsers
            = new BooleanSetting("Client users", true);
    private final BooleanSetting invisibles
            = new BooleanSetting("Invisibles", true);
    private final BooleanSetting mobs
            = new BooleanSetting("Mobs", false);
    private final BooleanSetting teams
            = new BooleanSetting("Teams", false,
            this.players::getValue);

    private final BooleanSetting esp = new BooleanSetting("ESP", true).setSaveConfig(false);

    private final TimeHelper clickTimer = new TimeHelper();
    private int blockCount;
    private double increaseClicks;
    private int nextClickTime;
    private long nextLUp;
    private long nextLDown;
    private long nextDrop;
    private long nextExhaust;
    private double dropRate;
    private boolean dropping;
    private final SecureRandom random = new SecureRandom();

    public KillAuraModule() {
        this.registerSettings(
                this.targetMode,
                this.sortingMode,
                this.aimMode,
                this.clickMode,
                this.rangeCalculationMode,
                this.rotationEvent,
                this.eventType,
                this.lookAt,
                this.gcdMode,
                this.autoBlockMode,
                this.blockState,
                this.unblockState,
                this.strafeMode,
                this.range,
                this.aps,
                this.cinematicSpeed,
                this.switchSpeed,
                this.attackAngle,
                this.hitChance,
                this.autoBlock,
                this.rotate,
                this.middleRotation,
                this.randomizeAimPoint,
                this.gcd,
                this.rotationFix,
                this.cinematic,
                this.cinematicFilterAfterRotation,
                this.keepRotation,
                this.keepSprint,
                this.swing,
                this.sprint,
                this.players,
                this.clientUsers,
                this.invisibles,
                this.mobs,
                this.teams,
                this.esp
        );
    }

    @Override
    public void onEnable() {
        super.onEnable();

        this.blockCount = 0;
        this.target = null;
        this.filterX.reset();
        this.filterY.reset();
        this.rotationVector = null;
        this.arrayIndex = 0;
        this.increaseClicks = 0;
        this.nextClickTime = 0;
        this.nextLUp = 0;
        this.nextLDown = 0;
        this.nextDrop = 0;
        this.nextExhaust = 0;
        this.dropRate = 0;
        this.dropping = false;
    }

    @Override
    public void onDisable() {
        super.onDisable();

        this.blockCount = 0;
        this.target = null;
        this.filterX.reset();
        this.filterY.reset();
        this.rotationVector = null;
        this.arrayIndex = 0;
        this.increaseClicks = 0;
        this.nextClickTime = 0;
        this.nextLUp = 0;
        this.nextLDown = 0;
        this.nextDrop = 0;
        this.nextExhaust = 0;
        this.dropRate = 0;
        this.dropping = false;

        mc.thePlayer.setAnimateBlocking(false);
        if (mc.thePlayer.isBlockingSword()) {
            mc.playerController.onStoppedUsingItem(mc.thePlayer);
            mc.gameSettings.keyBindUseItem.pressed = false;
            mc.thePlayer.setBlockingSword(false);
        }
    }

    private Vec2f generateRotations(RenderEvent event) {
        if (this.target == null) return new Vec2f(0, 0);

        RotationUtils.RotationAt rotationAt = (this.randomizeAimPoint.getValue() ?
                RotationUtils.RotationAt.values()
                        [RandomUtils.nextInt(0, RotationUtils.RotationAt.values().length - 1)] :
                this.lookAt.getValue());
        Vec2f generated = (this.middleRotation.getValue() ?
                RotationUtils.getMiddlePointRotations(this.target, rotationAt) :
                RotationUtils.getRotations(this.target, rotationAt));

        float sensitivity = mc.gameSettings.mouseSensitivity;

        if (this.rotationFix.getValue() && ((this.gcdMode.getValue() != GCDMode.MODULO3 &&
                this.gcdMode.getValue() != GCDMode.ADVANCED) || !this.gcd.getValue())) {
            generated.setX(generated.getX() - this.packetRotationVector.getX());
            generated.setY(generated.getY() - this.packetRotationVector.getY());
        }

        switch (this.aimMode.getValue()) {
            case NORMAl: {
                generated.setX(generated.getX());
                generated.setY(MathHelper.clamp_float(generated.getY(), -90, 90));
                break;
            }
            case RANDOMIZE: {
                generated.setX(generated.getX() + RandomUtils.nextFloat(-4, 4));
                generated.setY(MathHelper.clamp_float(generated.getY() + RandomUtils.nextFloat(-7, 7), -90, 90));
                break;
            }
            case ROUND: {
                generated.setX((float) Math.round(generated.getX()));
                generated.setY(MathHelper.clamp_float(Math.round(generated.getY()), -90, 90));
                break;
            }
        }

        float f = this.mc.gameSettings.mouseSensitivity * 0.6F + 0.2F;
        float gcd = f * f * f * 1.2F;
        if (this.gcd.getValue()) {
            switch (this.gcdMode.getValue()) {
                case NORMAl: {
                    generated.setX(generated.getX() - generated.getX() % gcd);
                    generated.setY(generated.getY() - generated.getY() % gcd);
                    break;
                }
                case ADVANCED: {
                    float yaw = generated.getX();
                    float pitch = generated.getY();

                    switch (RandomUtils.nextInt(1, 10)) {
                        case 1: {
                            float fixedYaw = yaw - this.packetRotationVector.getX();
                            fixedYaw -= fixedYaw % gcd;
                            yaw = this.packetRotationVector.getX() + (Math.round(fixedYaw) * .99F);

                            float fixedPitch = pitch - this.packetRotationVector.getY();
                            fixedPitch -= fixedPitch % gcd;
                            pitch = this.packetRotationVector.getY() + (fixedPitch * .5F);
                            break;
                        }
                        case 2: {
                            yaw -= (yaw % gcd) - f;

                            float fixedPitch = pitch - this.packetRotationVector.getY();
                            fixedPitch -= fixedPitch % gcd;
                            pitch = this.packetRotationVector.getY() + (fixedPitch / 2);
                            break;
                        }
                        case 3: {
                            pitch -= (pitch % gcd) - f;

                            float fixedYaw = yaw - this.packetRotationVector.getX();
                            fixedYaw -= fixedYaw % gcd;
                            yaw = (float) (this.packetRotationVector.getX() + (Math.floor(fixedYaw) * .99F));
                            break;
                        }
                        case 4: {
                            float fixedYaw = yaw - this.packetRotationVector.getX();
                            fixedYaw -= (fixedYaw % gcd) - f;
                            yaw = this.packetRotationVector.getX() + fixedYaw;

                            float fixedPitch = pitch - this.packetRotationVector.getY();
                            fixedPitch -= (fixedPitch % gcd) - f;
                            pitch = this.packetRotationVector.getY() + fixedPitch;
                            break;
                        }
                        case 5: {
                            float fixedYaw = yaw - this.packetRotationVector.getX();
                            fixedYaw -= (fixedYaw % gcd) - (sensitivity * 1.5);
                            yaw = this.packetRotationVector.getX() + fixedYaw;

                            float fixedPitch = pitch - this.packetRotationVector.getY();
                            fixedPitch -= (fixedPitch % gcd) - (sensitivity * 1.5);
                            pitch = this.packetRotationVector.getY() + fixedPitch;
                            break;
                        }
                        case 6: {
                            float fixedYaw = yaw - this.packetRotationVector.getX();
                            fixedYaw -= fixedYaw % gcd;
                            yaw = this.packetRotationVector.getX() + fixedYaw;

                            float fixedPitch = pitch - this.packetRotationVector.getY();
                            fixedPitch -= fixedPitch % gcd;
                            pitch = this.packetRotationVector.getY() + fixedPitch;
                            break;
                        }
                        case 7: {
                            float fixedYaw = yaw - this.packetRotationVector.getX();
                            fixedYaw -= fixedYaw % gcd;
                            yaw = this.packetRotationVector.getX() + fixedYaw;

                            float fixedPitch = pitch - this.packetRotationVector.getY();
                            fixedPitch -= fixedPitch % gcd;
                            pitch = this.packetRotationVector.getY() + (fixedPitch / 2);
                            break;
                        }
                        case 8: {
                            float fixedYaw = yaw - this.packetRotationVector.getX();
                            fixedYaw -= (fixedYaw % gcd) - f;
                            yaw = this.packetRotationVector.getX() + fixedYaw;

                            float fixedPitch = pitch - this.packetRotationVector.getY();
                            fixedPitch -= (fixedPitch % gcd) - f;
                            pitch = this.packetRotationVector.getY() + (fixedPitch / 2);
                            break;
                        }
                        case 9: {
                            float fixedYaw = yaw - this.packetRotationVector.getX();
                            fixedYaw -= (fixedYaw % gcd) - f;
                            yaw = this.packetRotationVector.getX() + (fixedYaw / 2);

                            float fixedPitch = pitch - this.packetRotationVector.getY();
                            fixedPitch -= (fixedPitch % gcd) - f;
                            pitch = this.packetRotationVector.getY() + fixedPitch;
                            break;
                        }

                        case 10: {
                            yaw -= (yaw % gcd) - f;
                            pitch -= (pitch % gcd) - f;
                            break;
                        }
                    }

                    generated.setX(yaw);
                    generated.setY(pitch);
                    break;
                }
                case MODULO: {
                    double fixedGcdDivision = (ThreadLocalRandom.current().nextBoolean() ? 45 : 90);
                    double fixedGcd = gcd / fixedGcdDivision + gcd;

                    generated.setX((float) (generated.getX() - Math.floor(generated.getX()) % fixedGcd));
                    generated.setY((float) (generated.getY() - Math.floor(generated.getY()) % fixedGcd));
                    break;
                }
                case MODULO1: {
                    generated.setX(generated.getX() - ((generated.getX() % gcd) - f));
                    generated.setY(generated.getY() - ((generated.getY() % gcd) - f));
                    break;
                }
                case MODULO2: {
                    float yaw = generated.getX();
                    float pitch = generated.getY();

                    float fixedYaw = yaw;
                    fixedYaw -= (fixedYaw % gcd) - f;
                    yaw = fixedYaw;
                    float fixedPitch = pitch;
                    fixedPitch -= (fixedPitch % gcd) - f;
                    pitch = fixedPitch;

                    generated.setX(yaw);
                    generated.setY(MathHelper.clamp_float(pitch, -90, 90));
                    break;
                }
                case MODULO3: {
                    float yaw = generated.getX();
                    float pitch = generated.getY();

                    float fixedYaw = yaw - this.packetRotationVector.getX();
                    fixedYaw -= (fixedYaw % gcd) - f;
                    yaw = (fixedYaw / 2) + this.packetRotationVector.getX();

                    float fixedPitch = pitch - this.packetRotationVector.getY();
                    fixedPitch -= (fixedPitch % gcd) - f;
                    pitch = fixedPitch + this.packetRotationVector.getY();

                    generated.setX(yaw);
                    generated.setY(MathHelper.clamp_float(pitch, -90, 90));
                    break;
                }
                case PERFECT: {
                    float fixedYaw = generated.getX() - this.packetRotationVector.getX();
                    fixedYaw -= fixedYaw % gcd;
                    float yaw = this.packetRotationVector.getX() + (Math.round(fixedYaw) * .99F);
                    float fixedPitch = generated.getY() - this.packetRotationVector.getY();
                    fixedPitch -= fixedPitch % gcd;
                    float pitch = this.packetRotationVector.getY() + (fixedPitch * .5F);

                    generated.setX(yaw);
                    generated.setY(MathHelper.clamp_float(pitch, -90, 90));
                    break;
                }
                case HI: {
                    float deltaYaw = generated.getX() - this.packetRotationVector.getX();
                    float deltaPitch = generated.getY() - this.packetRotationVector.getY();

                    int deltaX = (int) Math.floor((deltaYaw / .15) / (Math.pow(f, 3) * 8));
                    int deltaY = (int) Math.floor((deltaPitch / .15) / (Math.pow(f, 3) * 8));

                    float clampedX = deltaX * gcd;
                    float clampedY = deltaY * gcd;

                    clampedX -= clampedX % gcd;
                    clampedY -= clampedY % gcd;

                    float f2 = f * f * f * 8.0F;

                    clampedX += f2;
                    clampedY += f2;

                    generated.setX(this.packetRotationVector.getX() + clampedX);
                    generated.setY(MathHelper.clamp_float(this.packetRotationVector.getY() + clampedY, -90, 90));
                    break;
                }
            }
        }

        if (this.rotationFix.getValue() && ((this.gcdMode.getValue() != GCDMode.MODULO3 &&
                this.gcdMode.getValue() != GCDMode.ADVANCED) || !this.gcd.getValue())) {
            generated.setX(generated.getX() + this.packetRotationVector.getX());
            generated.setY(generated.getY() + this.packetRotationVector.getY());
        }

        if (this.cinematic.getValue()) {
            if (!this.cinematicFilterAfterRotation.getValue()) {
                this.smoothCamFilterX = this.filterX.smooth(this.smoothCamYaw,
                        this.cinematicSpeed.getValue().floatValue() * gcd);
                this.smoothCamFilterY = this.filterY.smooth(this.smoothCamPitch,
                        this.cinematicSpeed.getValue().floatValue() * gcd);
                this.smoothCamPartialTicks = 0.0F;
                this.smoothCamYaw = 0.0F;
                this.smoothCamPitch = 0.0F;
            }

            this.smoothCamYaw += generated.getX();
            this.smoothCamPitch += generated.getY();

            float smoothedPartialTicks = event.getPartialTicks() - this.smoothCamPartialTicks;

            this.smoothCamPartialTicks = event.getPartialTicks();

            generated.setX(this.smoothCamFilterX * smoothedPartialTicks);
            generated.setY(this.smoothCamFilterY * smoothedPartialTicks);
        } else {
            if (!this.cinematicFilterAfterRotation.getValue()) {
                this.smoothCamFilterX = 0.0F;
                this.smoothCamFilterY = 0.0F;
                this.filterX.reset();
                this.filterY.reset();
            }

            this.smoothCamYaw = 0.0F;
            this.smoothCamPitch = 0.0F;
        }

        generated.setY(MathHelper.clamp_float(generated.getY(), -90, 90));
        return generated;
    }

    @EventHandler
    EventCallback<RenderEvent> onRender = event -> {
        if (event.getType() == this.rotationEvent.getValue()) {
            if (this.target != null && this.rotate.getValue()) {
                this.rotationVector = this.generateRotations(event);

                if (this.cinematicFilterAfterRotation.getValue()) {
                    if (this.cinematic.getValue()) {
                        float f = this.mc.gameSettings.mouseSensitivity * 0.6F + 0.2F;
                        float gcd = f * f * f * 8.0F;

                        this.smoothCamFilterX = this.filterX.smooth(this.smoothCamYaw,
                                cinematicSpeed.getValue().floatValue() * gcd);
                        this.smoothCamFilterY = this.filterY.smooth(this.smoothCamPitch,
                                cinematicSpeed.getValue().floatValue() * gcd);
                        this.smoothCamPartialTicks = 0.0F;
                        this.smoothCamYaw = 0.0F;
                        this.smoothCamPitch = 0.0F;
                    } else {
                        this.smoothCamFilterX = 0.0F;
                        this.smoothCamFilterY = 0.0F;
                        this.filterX.reset();
                        this.filterY.reset();
                    }
                }
            }
        }

        if (event.getType() == RenderEvent.Type.RENDER_3D) {
            if (this.target == null) {
                return;
            }

            if (esp.getValue()) {
                GlStateManager.pushMatrix();
                if (this.targetMode.getValue() == TargetMode.MULTI) {
                    this.entities.forEach(target -> {
                        this.drawCircle(target, target.width, false);
                        this.drawCircle(target, target.width, true);
                    });
                } else {
                    this.drawCircle(this.target, this.target.width, false);
                    this.drawCircle(this.target, this.target.width, true);
                }
                GlStateManager.popMatrix();
            }
        }
    };

    public double ticks = 0;
    public long lastFrame = 0;

    public void drawCircle(Entity entity, double rad, boolean shade) {
        ticks += .004 * (System.currentTimeMillis() - lastFrame);

        lastFrame = System.currentTimeMillis();

        GL11.glPushMatrix();
        GL11.glDisable(3553);
        GL11.glEnable(2848);
        GL11.glEnable(2832);
        GL11.glEnable(3042);
        GL11.glBlendFunc(770, 771);
        GL11.glHint(3154, 4354);
        GL11.glHint(3155, 4354);
        GL11.glHint(3153, 4354);
        GL11.glDisable(2929);
        GL11.glDepthMask(false);
        if (shade) GL11.glShadeModel(GL11.GL_SMOOTH);
        GlStateManager.disableCull();

        double x = entity.lastTickPosX + (entity.posX - entity.lastTickPosX)
                * Minecraft.getMinecraft().timer.renderPartialTicks - RenderManager.renderPosX;
        double y = (entity.lastTickPosY + (entity.posY - entity.lastTickPosY)
                * Minecraft.getMinecraft().timer.renderPartialTicks - RenderManager.renderPosY) + Math.sin(ticks) + 1;
        double z = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ)
                * Minecraft.getMinecraft().timer.renderPartialTicks - RenderManager.renderPosZ;

        GL11.glBegin(GL11.GL_TRIANGLE_STRIP);

        Color color = Color.WHITE;
        double TAU = Math.PI * 2.D;
        for (float i = 0; i < TAU; i += TAU / 64.F) {

            double vecX = x + rad * Math.cos(i);
            double vecZ = z + rad * Math.sin(i);

            color = ColorUtils.getColorSwitch(
                    UISettings.FIRST_COLOR,
                    UISettings.SECOND_COLOR,
                    3000, (int) i * 60, 55, 4
            );

            if (shade) {
                ColorUtils.glColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 40));

                GL11.glVertex3d(vecX, y - Math.sin(ticks + 1) / 2.7f, vecZ);
            }

            ColorUtils.glColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 150));

            GL11.glVertex3d(vecX, y, vecZ);
        }

        GL11.glColor4f(1, 1, 1, 1);
        GL11.glEnd();
        if (shade) GL11.glShadeModel(GL11.GL_FLAT);
        GL11.glDepthMask(true);
        GL11.glEnable(2929);
        GlStateManager.enableCull();
        GL11.glDisable(2848);
        GL11.glEnable(2832);
        GL11.glEnable(3553);
        GL11.glPopMatrix();

        GlStateManager.pushAttribAndMatrix();
        GL11.glPushMatrix();
        mc.entityRenderer.disableLightmap();
        GL11.glDisable(3553);
        GL11.glEnable(3042);
        GL11.glBlendFunc(770, 771);
        GL11.glDisable(2929);
        GL11.glEnable(2848);
        GL11.glDepthMask(false);
        GL11.glPushMatrix();
        GL11.glLineWidth(2);
        ColorUtils.glColor(color);
        GL11.glBegin(1);
        for (int i = 0; i <= 90; ++i) {
            GL11.glVertex3d(x + rad * Math.cos((double) i * (Math.PI * 2) / 45), y, z + rad * Math.sin((double) i * (Math.PI * 2) / 45));
        }
        ColorUtils.glColor(Color.WHITE);
        GL11.glEnd();
        GL11.glPopMatrix();
        GL11.glDepthMask(true);
        GL11.glDisable(2848);
        GL11.glEnable(2929);
        GL11.glDisable(3042);
        GL11.glEnable(3553);
        GL11.glPopMatrix();
        mc.entityRenderer.disableLightmap();
        GlStateManager.disableLighting();
        GlStateManager.popAttribAndMatrix();
    }

    @EventHandler
    EventCallback<UpdateEvent> onUpdate = event -> {
        if (this.target != null && !this.sprint.getValue()) {
            mc.thePlayer.setSprinting(false);
            mc.gameSettings.keyBindSprint.pressed = false;
        }
    };

    @EventHandler
    EventCallback<MotionEvent> onMotion = event -> {
        if (event.getEventState() == dev.africa.pandaware.api.event.Event.EventState.PRE) {
            this.target = this.getTarget(this.range.getValue().floatValue());

            if (this.target != null && RotationUtils.getYawRotationDifference(this.target) >
                    this.attackAngle.getValue().doubleValue()) {
                this.target = null;
            }
        }

        if (this.autoBlock.getValue() && this.target != null) {
            if (event.getEventState() == this.blockState.getValue()) {
                this.block();
            }
        }

        if (event.getEventState() == this.eventType.getValue()) {
            if (this.target == null) {
                this.clickTimer.reset();
                this.increaseClicks = 0;
                this.nextClickTime = 0;
                this.blockCount = 0;
                this.nextLUp = 0;
                this.nextLDown = 0;
                this.nextDrop = 0;
                this.nextExhaust = 0;
                this.dropRate = 0;
                this.dropping = false;
                if (!mc.gameSettings.keyBindUseItem.pressed) {
                    mc.thePlayer.setAnimateBlocking(false);
                }

                if (mc.thePlayer.isBlockingSword()) {
                    mc.gameSettings.keyBindUseItem.pressed = false;
                    mc.thePlayer.setBlockingSword(false);
                }

                this.rotationVector = null;
                if (mc.thePlayer.isBlockingSword()) {
                    mc.playerController.onStoppedUsingItem(mc.thePlayer);
                    mc.thePlayer.setBlockingSword(false);
                }
            } else {
                if (this.rotationVector != null || !this.rotate.getValue()) {
                    if (this.rotate.getValue()) {
                        if (event.getEventState() == dev.africa.pandaware.api.event.Event.EventState.POST || this.keepRotation.getValue()) {
                            mc.thePlayer.rotationYaw = this.rotationVector.getX();
                            mc.thePlayer.rotationPitch = this.rotationVector.getY();
                        } else {
                            event.setYaw(this.rotationVector.getX());
                            event.setPitch(this.rotationVector.getY());
                        }
                    }

                    if (this.shouldClickMouse() && RandomUtils.nextInt(0, 100) <= this.hitChance.getValue().intValue()) {
                        this.attack(this.target);

                        mc.thePlayer.resetCooldown();
                        this.timer.reset();
                    }
                }
            }
        }

        if (this.autoBlock.getValue() && event.getEventState() == this.unblockState.getValue() && this.target != null) {
            this.unblock();
        }
    };

    @EventHandler
    EventCallback<PacketEvent> onPacket = event -> {
        if (event.getPacket() instanceof C03PacketPlayer) {
            C03PacketPlayer packet = (C03PacketPlayer) event.getPacket();

            if (packet.getRotating()) {
                this.packetRotationVector = new Vec2f(packet.getYaw(), packet.getPitch());
            }
        }
    };

    private void block() {
        boolean swordBlock = mc.thePlayer.getHeldItem() != null &&
                mc.thePlayer.getHeldItem().getItem() instanceof ItemSword;
        if (swordBlock) {
            switch (this.autoBlockMode.getValue()) {
                case FAKE:
                    mc.thePlayer.setAnimateBlocking(true);
                    break;

                case NORMAL:
                    if (!mc.thePlayer.isBlockingSword()) {
                        mc.thePlayer.sendQueue.getNetworkManager()
                                .sendPacketNoEvent(new C08PacketPlayerBlockPlacement(new BlockPos(-1, -1, -1),
                                        255, mc.thePlayer.inventory.getCurrentItem(),
                                        0.0F, 0.0F, 0.0F));

                        mc.thePlayer.setBlockingSword(true);
                    }
                    break;

                case SYNC:
                    if (!mc.thePlayer.isBlockingSword()) {
                        mc.playerController.syncCurrentPlayItem();
                        mc.thePlayer.sendQueue
                                .addToSendQueue(new C08PacketPlayerBlockPlacement(mc.thePlayer.getHeldItem()));

                        mc.thePlayer.setBlockingSword(true);
                    }

                    this.blockCount++;
                    break;
            }
        } else {
            mc.thePlayer.setAnimateBlocking(true);
            mc.thePlayer.setBlockingSword(false);
        }
    }

    private void unblock() {
        boolean swordBlock = mc.thePlayer.getHeldItem() != null &&
                mc.thePlayer.getHeldItem().getItem() instanceof ItemSword;

        if (swordBlock) {
            switch (this.autoBlockMode.getValue()) {
                case FAKE:
                    mc.thePlayer.setAnimateBlocking(false);
                    break;

                case NORMAL: {
                    if (mc.thePlayer.isBlockingSword()) {
                        mc.thePlayer.sendQueue.getNetworkManager().sendPacketNoEvent(new
                                C07PacketPlayerDigging(C07PacketPlayerDigging.Action.RELEASE_USE_ITEM,
                                new BlockPos(-1, -1, -1), EnumFacing.DOWN));
                        mc.thePlayer.setBlockingSword(false);
                    }
                    break;
                }

                case SYNC: {
                    if (mc.thePlayer.isBlockingSword() && this.blockCount > 2) {
                        mc.thePlayer.sendQueue.addToSendQueue(new C07PacketPlayerDigging(C07PacketPlayerDigging
                                .Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN));
                        mc.thePlayer.setBlockingSword(false);

                        this.blockCount = 0;
                    }
                    break;
                }
            }
        } else {
            mc.thePlayer.setAnimateBlocking(true);
            mc.thePlayer.setBlockingSword(false);
        }
    }

    @EventHandler
    EventCallback<StrafeEvent> onStrafe = event -> {
        if (this.target != null && this.rotationVector != null && this.rotate.getValue() && mc.thePlayer != null) {
            switch (this.strafeMode.getValue()) {
                case STRICT:
                    event.setYaw(this.rotationVector.getX());
                    break;
                case SILENT:
                    this.silentLegitMovement(event, this.rotationVector.getX());
                    break;
            }
        }
    };

    @EventHandler
    EventCallback<JumpEvent> onJump = event -> {
        if (this.target != null && this.rotationVector != null && this.rotate.getValue() && mc.thePlayer != null) {
            switch (this.strafeMode.getValue()) {
                case STRICT:
                    event.setYaw(this.rotationVector.getX());
                    break;
            }
        }
    };

    private void attack(EntityLivingBase entity) {
        if (this.autoBlockMode.getValue() == AutoBlockMode.SYNC && this.autoBlock.getValue() &&
                !(mc.thePlayer.getHeldItem() != null && mc.thePlayer.getHeldItem().getItem() instanceof ItemSword)) {
            mc.thePlayer.setBlockingSword(false);
            this.blockCount = 0;
        }

        if (!this.autoBlock.getValue() || this.blockCount > RandomUtils.nextInt(1, 2)
                || this.autoBlockMode.getValue() != AutoBlockMode.SYNC ||
                !(mc.thePlayer.getHeldItem().getItem() instanceof ItemSword)) {
            if (this.targetMode.getValue() == TargetMode.MULTI) {
                this.entities.forEach(this::doCallEventAndAttack);
            } else {
                this.doCallEventAndAttack(entity);
            }
        }
    }

    private void doCallEventAndAttack(Entity entity) {
        AttackEvent attackEvent = new AttackEvent(entity, dev.africa.pandaware.api.event.Event.EventState.PRE);
        Client.getInstance().getEventDispatcher().dispatch(attackEvent);

        if (ProtocolUtils.isOneDotEight() && this.swing.getValue()) {
            mc.thePlayer.swingItem();
        }

        if (this.keepSprint.getValue()) {
            mc.thePlayer.sendQueue.getNetworkManager().sendPacket(new C02PacketUseEntity(entity, C02PacketUseEntity.Action.ATTACK));
        } else {
            mc.thePlayer.motionX *= 0.7F;
            mc.thePlayer.motionZ *= 0.7F;

            mc.playerController.attackEntity(mc.thePlayer, entity);
        }

        if (!ProtocolUtils.isOneDotEight() && this.swing.getValue()) {
            mc.thePlayer.swingItem();
        }

        attackEvent.setEventState(Event.EventState.POST);
        Client.getInstance().getEventDispatcher().dispatch(attackEvent);
    }

    private EntityLivingBase getTarget(float distance) {
        this.entities.clear();

        for (Entity entity : mc.theWorld.loadedEntityList) {
            if (this.isValid(entity)) {
                boolean hittable = true;
                if (this.rangeCalculationMode.getValue() == RangeCalculation.RAYTRACE) {
                    AxisAlignedBB targetBox = entity.getEntityBoundingBox();
                    Vec2f rotation = RotationUtils.getRotations((EntityLivingBase) entity);
                    Vec3 origin = mc.thePlayer.getPositionEyes(1.0f);
                    Vec3 look = entity.getVectorForRotation(rotation.getY(), rotation.getX());

                    look = origin.addVector(
                            look.xCoord * distance,
                            look.yCoord * distance,
                            look.zCoord * distance
                    );

                    MovingObjectPosition collision = targetBox.calculateIntercept(origin, look);

                    if (collision == null) {
                        hittable = false;
                    }
                } else {
                    if (mc.thePlayer.getDistanceToEntity(entity) >= distance) {
                        hittable = false;
                    }
                }

                if (hittable) {
                    this.entities.add((EntityLivingBase) entity);
                }
            }
        }
        this.entities.sort(Comparator.comparing(entityLivingBase -> {
            switch (this.sortingMode.getValue()) {
                case DISTANCE:
                    return mc.thePlayer.getDistanceToEntity(entityLivingBase);
                case HEALTH:
                    return entityLivingBase.getHealth();
                default:
                    return (float) RotationUtils.getRotationDifference(entityLivingBase);
            }
        }));

        if (mc.thePlayer.ticksExisted % this.switchSpeed.getValue().intValue() == 0) {
            this.arrayIndex++;
        }
        if (this.arrayIndex >= this.entities.size()) {
            this.arrayIndex = 0;
        }
        int finalIndex = this.targetMode.getValue() == TargetMode.SWITCH
                ? MathHelper.clamp_int(this.arrayIndex, 0, this.entities.size()) : 0;

        return this.entities.size() > 0 ? this.entities.get(finalIndex) : null;
    }

    private boolean isValid(Entity entity) {
        boolean valid = entity instanceof EntityLivingBase;

        if (entity != null) {
            if (entity == mc.thePlayer) {
                valid = false;
            }

            if (entity instanceof EntityPlayer) {
                if (!this.players.getValue() ||
                        Client.getInstance().getIgnoreManager().isIgnoreBoth((EntityPlayer) entity)) {
                    valid = false;
                }
            }

            if (!entity.isEntityAlive() || entity.isDead) {
                valid = false;
            }

            if (entity.isInvisible() && !this.invisibles.getValue()) {
                valid = false;
            }

            if ((entity instanceof EntityMob || entity instanceof EntityVillager) && !this.mobs.getValue()) {
                valid = false;
            }

            if (entity instanceof EntityArmorStand) {
                valid = false;
            }
        }

        return valid;
    }

    private boolean shouldClickMouse() {
        switch (this.clickMode.getValue()) {
            case RANDOM: {
                double time = RandomUtils.nextDouble(this.aps.getFirstValue().doubleValue(),
                        this.aps.getSecondValue().doubleValue());

                return this.timer.reach((float) (1000L / time));
            }

            case SECURE_RANDOM: {
                double min = this.aps.getFirstValue().doubleValue();
                double max = this.aps.getSecondValue().doubleValue();

                double time = MathHelper.clamp_double(
                        min + ((max - min) * new SecureRandom().nextDouble()), min, max);

                return this.timer.reach((float) (1000L / time));
            }

            case INCREASE: {
                double min = this.aps.getFirstValue().doubleValue();
                double max = this.aps.getSecondValue().doubleValue();

                if (this.increaseClicks > min) {
                    this.increaseClicks -= RandomUtils.nextDouble(0.2, 0.45);
                }
                if (this.clickTimer.reach(this.nextClickTime)) {
                    this.nextClickTime = RandomUtils.nextInt(30, 50);

                    this.increaseClicks += RandomUtils.nextDouble(0.2, 0.45);
                    this.clickTimer.reset();
                }
                this.increaseClicks = MathHelper.clamp_double(this.increaseClicks, 0, max);

                return this.timer.reach((float) (1000L / this.increaseClicks));
            }

            case DROP: {
                double randomTime = 0;
                if (this.clickTimer.reach(this.nextClickTime)) {
                    this.nextClickTime = RandomUtils.nextInt(450, 900);

                    randomTime -= RandomUtils.nextDouble(3, 5);
                    this.clickTimer.reset();
                }

                double min = this.aps.getFirstValue().doubleValue();
                double max = this.aps.getSecondValue().doubleValue();

                double time = (min + ((max - min) * new SecureRandom().nextDouble())) + randomTime;

                return this.timer.reach((float) (1000L / time));
            }

            case SPIKE: {
                double randomTime = 0;
                if (this.clickTimer.reach(this.nextClickTime)) {
                    this.nextClickTime = RandomUtils.nextInt(450, 900);

                    randomTime += RandomUtils.nextDouble(3, 5);
                    this.clickTimer.reset();
                }

                double min = this.aps.getFirstValue().doubleValue();
                double max = this.aps.getSecondValue().doubleValue();

                double time = (min + ((max - min) * new SecureRandom().nextDouble())) + randomTime;

                return this.timer.reach((float) (1000L / time));
            }

            case DROP_INCREASE: {
                double min = this.aps.getFirstValue().doubleValue();
                double max = this.aps.getSecondValue().doubleValue();

                if (this.increaseClicks > min) {
                    this.increaseClicks -= RandomUtils.nextDouble(0.2, 0.45);
                }
                if (this.clickTimer.reach(this.nextClickTime)) {
                    this.nextClickTime = RandomUtils.nextInt(30, 50);

                    this.increaseClicks += RandomUtils.nextDouble(0.2, 0.45);
                    this.clickTimer.reset();
                }
                if (RandomUtils.nextInt(0, 10) == RandomUtils.nextInt(0, 10)) {
                    this.increaseClicks -= RandomUtils.nextDouble(1.2, 1.7);
                }

                this.increaseClicks = MathHelper.clamp_double(this.increaseClicks, 0, max);

                return this.timer.reach((float) (1000L / this.increaseClicks));
            }

            case ONE_DOT_NINE_PLUS: {
                float delay = mc.thePlayer.getCooledAttackStrength(0.0f);

                return delay > 0.9f;
            }
        }

        return false;
    }

    private void silentLegitMovement(StrafeEvent event, float eventYaw) {
        int dif = (int) ((MathHelper.wrapAngleTo180_double(mc.thePlayer.rotationYaw - eventYaw - 23.5f - 135) + 180) / 45);

        float strafe = event.getStrafe();
        float forward = event.getForward();
        float friction = event.getFriction();

        float calcForward = 0f;
        float calcStrafe = 0f;

        switch (dif) {
            case 0: {
                calcForward = forward;
                calcStrafe = strafe;
                break;
            }
            case 1: {
                calcForward += forward;
                calcStrafe -= forward;
                calcForward += strafe;
                calcStrafe += strafe;
                break;
            }
            case 2: {
                calcForward = strafe;
                calcStrafe = -forward;
                break;
            }
            case 3: {
                calcForward -= forward;
                calcStrafe -= forward;
                calcForward += strafe;
                calcStrafe -= strafe;
                break;
            }
            case 4: {
                calcForward = -forward;
                calcStrafe = -strafe;
                break;
            }
            case 5: {
                calcForward -= forward;
                calcStrafe += forward;
                calcForward -= strafe;
                calcStrafe -= strafe;
                break;
            }
            case 6: {
                calcForward = -strafe;
                calcStrafe = forward;
                break;
            }
            case 7: {
                calcForward += forward;
                calcStrafe += forward;
                calcForward -= strafe;
                calcStrafe += strafe;
                break;
            }
        }

        if (calcForward > 1f || calcForward < 0.9f && calcForward > 0.3f || calcForward < -1f || calcForward > -0.9f && calcForward < -0.3f) {
            calcForward *= 0.5f;
        }

        if (calcStrafe > 1f || calcStrafe < 0.9f && calcStrafe > 0.3f || calcStrafe < -1f || calcStrafe > -0.9f && calcStrafe < -0.3f) {
            calcStrafe *= 0.5f;
        }

        float d = calcStrafe * calcStrafe + calcForward * calcForward;

        if (d >= 1.0E-4f) {
            d = MathHelper.sqrt_float(d);
            if (d < 1.0f) d = 1.0f;
            d = friction / d;
            calcStrafe *= d;
            calcForward *= d;
            float yawSin = MathHelper.sin((float) (eventYaw * Math.PI / 180f));
            float yawCos = MathHelper.cos((float) (eventYaw * Math.PI / 180f));
            mc.thePlayer.motionX += calcStrafe * yawCos - calcForward * yawSin;
            mc.thePlayer.motionZ += calcForward * yawCos + calcStrafe * yawSin;
        }

        event.cancel();
    }


    @AllArgsConstructor
    private enum GCDMode {
        NORMAl("Normal"),
        ADVANCED("Advanced"),
        MODULO("Modulo"),
        MODULO1("Modulo1"),
        MODULO2("Modulo2"),
        MODULO3("Modulo3"),
        PERFECT("Perfect"),
        HI("Hi");

        private final String label;
    }

    @AllArgsConstructor
    private enum AimMode {
        NORMAl("Normal"),
        ROUND("Round"),
        RANDOMIZE("Randomize");
        private final String label;
    }

    @AllArgsConstructor
    private enum TargetMode {
        SINGLE("Single"),
        SWITCH("Switch"),
        MULTI("Multi");

        private final String label;
    }

    @AllArgsConstructor
    private enum SortingMode {
        DISTANCE("Distance"),
        HEALTH("Health"),
        AIM("Aim");

        private final String label;
    }

    @AllArgsConstructor
    private enum RangeCalculation {
        NORMAL("Normal"),
        RAYTRACE("Raytrace");

        private final String label;
    }

    @AllArgsConstructor
    private enum AutoBlockMode {
        NORMAL("Normal"),
        SYNC("Sync"),
        FAKE("Fake");

        private final String label;
    }

    @AllArgsConstructor
    private enum Strafemode {
        SILENT("Silent"),
        STRICT("Strict"),
        NONE("None");

        private final String label;
    }

    @AllArgsConstructor
    private enum ClickMode {
        RANDOM("Random"),
        SECURE_RANDOM("Secure Random"),
        INCREASE("Increase"),
        DROP("Drop"),
        SPIKE("Spike"),
        DROP_INCREASE("Drop Increase"),
        ONE_DOT_NINE_PLUS("1.9+");

        private String label;
    }

    @Override
    public String getSuffix() {
        return this.eventType.getValue().getLabel() + " §7" + this.entities.size();
    }
}
