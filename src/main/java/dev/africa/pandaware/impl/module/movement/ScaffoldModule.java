package dev.africa.pandaware.impl.module.movement;

import dev.africa.pandaware.Client;
import dev.africa.pandaware.api.event.Event;
import dev.africa.pandaware.api.event.interfaces.EventCallback;
import dev.africa.pandaware.api.event.interfaces.EventHandler;
import dev.africa.pandaware.api.module.Module;
import dev.africa.pandaware.api.module.event.TaskedEventListener;
import dev.africa.pandaware.api.module.interfaces.Category;
import dev.africa.pandaware.api.module.interfaces.ModuleInfo;
import dev.africa.pandaware.impl.event.player.*;
import dev.africa.pandaware.impl.event.render.RenderEvent;
import dev.africa.pandaware.impl.font.Fonts;
import dev.africa.pandaware.impl.module.movement.speed.SpeedModule;
import dev.africa.pandaware.impl.setting.BooleanSetting;
import dev.africa.pandaware.impl.setting.EnumSetting;
import dev.africa.pandaware.impl.setting.NumberSetting;
import dev.africa.pandaware.impl.ui.UISettings;
import dev.africa.pandaware.utils.client.ServerUtils;
import dev.africa.pandaware.utils.math.TimeHelper;
import dev.africa.pandaware.utils.math.apache.ApacheMath;
import dev.africa.pandaware.utils.math.random.RandomUtils;
import dev.africa.pandaware.utils.math.vector.Vec2f;
import dev.africa.pandaware.utils.network.ProtocolUtils;
import dev.africa.pandaware.utils.player.MovementUtils;
import dev.africa.pandaware.utils.player.PlayerUtils;
import dev.africa.pandaware.utils.player.RotationUtils;
import dev.africa.pandaware.utils.player.block.BlockUtils;
import dev.africa.pandaware.utils.render.RenderUtils;
import dev.africa.pandaware.utils.render.animator.Animator;
import dev.africa.pandaware.utils.render.animator.Easing;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C09PacketHeldItemChange;
import net.minecraft.network.play.client.C0APacketAnimation;
import net.minecraft.network.play.client.C0BPacketEntityAction;
import net.minecraft.potion.Potion;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Vec3;
import org.lwjgl.input.Keyboard;

@Getter
@ModuleInfo(name = "Scaffold", category = Category.MOVEMENT)
public class ScaffoldModule extends Module {
    private final EnumSetting<ScaffoldMode> scaffoldMode = new EnumSetting<>("Mode", ScaffoldMode.HYPIXEL);
    private final EnumSetting<HypixelMode> hypixelMode = new EnumSetting<>("Hypixel Mode", HypixelMode.NORMAL, () ->
            this.scaffoldMode.getValue() == ScaffoldMode.HYPIXEL);
    private final BooleanSetting tower = new BooleanSetting("Tower", true);
    private final EnumSetting<RotationMode> rotationMode = new EnumSetting<>("Rotation mode", RotationMode.NEW);
    private final BooleanSetting sprint = new BooleanSetting("Sprint", true);
    private final EnumSetting<SpoofMode> spoofMode = new EnumSetting<>("Spoof mode", SpoofMode.SPOOF);
    private final EnumSetting<TowerMode> towerMode = new EnumSetting<>("Tower mode", TowerMode.NCP,
            this.tower::getValue);
    private final BooleanSetting swing = new BooleanSetting("Swing", false);
    private final BooleanSetting rotate = new BooleanSetting("Rotate", true);
    private final BooleanSetting keepRotation = new BooleanSetting("Keep rotation", true,
            this.rotate::getValue);
    private final BooleanSetting downwards = new BooleanSetting("Downwards", true);
    private final BooleanSetting towerMove = new BooleanSetting("Tower move", true,
            this.tower::getValue);
    private final BooleanSetting useSpeed = new BooleanSetting("Use Speed Modifier", false);
    private final BooleanSetting safeWalk = new BooleanSetting("SafeWalk", true);
    private final BooleanSetting overwriteAura = new BooleanSetting("Overwrite Aura rotations", true);
    private final BooleanSetting replaceBlocks = new BooleanSetting("Replace Blocks", true);
    private final NumberSetting speedModifier = new NumberSetting("Speed modifier", 1.5, 0.1,
            1, 0.01, this.useSpeed::getValue);
    private final NumberSetting expand = new NumberSetting("Expand", 6, 0, 0, 0.1);
    private final NumberSetting timerSpeed = new NumberSetting("Timer", 5, 1, 1, 0.1);

    private BlockEntry blockEntry;
    private BlockEntry aimBlockEntry;
    private Vec2f rotations;
    private int lastSlot;
    private double startY;
    private Vec2f smoothRotations;
    private Vec2f currentRotation;
    private final TimeHelper towerTimer = new TimeHelper();
    private final TimeHelper vulcanTimer = new TimeHelper();
    private boolean jumped;
    private double lastDistance;
    private boolean sloted;
    private int c09slot;

    public ScaffoldModule() {
        this.registerSettings(
                this.scaffoldMode,
                this.hypixelMode,
                this.spoofMode,
                this.rotationMode,
                this.towerMode,
                this.swing,
                this.rotate,
                this.overwriteAura,
                this.keepRotation,
                this.replaceBlocks,
                this.downwards,
                this.tower,
                this.towerMove,
                this.sprint,
                this.safeWalk,
                this.useSpeed,
                this.speedModifier,
                this.timerSpeed,
                this.expand
        );

        this.setTaskedEvent(this.taskedEventListener);
    }

    @Override
    public void onEnable() {
        this.blockEntry = null;
        this.aimBlockEntry = null;
        this.rotations = null;
        this.currentRotation = null;
        this.startY = mc.thePlayer.posY;
        this.sloted = false;
        this.c09slot = mc.thePlayer.inventory.currentItem;
        this.lastSlot = mc.thePlayer.inventory.currentItem;
        mc.timer.timerSpeed = this.timerSpeed.getValue().floatValue();

        if (this.scaffoldMode.getValue() == ScaffoldMode.VULCAN) {
            if (!mc.thePlayer.onGround) {
                mc.thePlayer.motionX *= 0.7;
                mc.thePlayer.motionZ *= 0.7;
            }
        }
    }

    @Override
    public void onDisable() {
        mc.timer.timerSpeed = 1.0f;
        this.blockEntry = null;
        this.aimBlockEntry = null;
        this.rotations = null;

        if (this.scaffoldMode.getValue() == ScaffoldMode.VULCAN) {
            mc.thePlayer.sendQueue.getNetworkManager().sendPacketNoEvent(new C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.STOP_SNEAKING));
        }

        if (this.spoofMode.getValue() == SpoofMode.SWITCH) {
            mc.thePlayer.inventory.currentItem = this.lastSlot;
        } else {
            if (mc.thePlayer.inventory.currentItem != c09slot) mc.thePlayer.sendQueue.getNetworkManager().sendPacket(
                    new C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem));
        }
    }

    TaskedEventListener<ScaffoldModule> taskedEventListener
            = new TaskedEventListener<ScaffoldModule>("Tasked scaffold event", this) {
        private final Animator animator = new Animator();

        @EventHandler
        EventCallback<RenderEvent> onRender = event -> {
            if (event.getType() == RenderEvent.Type.RENDER_2D) {
                int slot = this.getModule().getItemSlot(false);
                float height = (slot != -1 ? 35 : 17);

                GlStateManager.pushMatrix();
                this.animator.setMin(0).setMax(1).setSpeed(3.3f);

                boolean toggled = this.getModule().getData().isEnabled();
                if (toggled && this.animator.getValue() <= 1F) {
                    this.animator.setEase(Easing.QUINTIC_OUT).setReversed(false).update();
                } else if (!toggled && this.animator.getValue() > 0F) {
                    this.animator.setEase(Easing.QUINTIC_IN).setReversed(true).update();
                }

                if (this.animator.getValue() > 0) {
                    float width = 30;
                    float x = event.getResolution().getScaledWidth() / 2f - (width / 2);
                    float y = event.getResolution().getScaledHeight() / 2f + 35;
                    GlStateManager.translate(x, y, 0);

                    if (this.animator.getValue() < 1D) {
                        GlStateManager.translate((width / 2f) * (1 - this.animator.getValue()),
                                (height / 2f) * (1 - this.animator.getValue()), 0);
                        GlStateManager.scale(this.animator.getValue(), this.animator.getValue(), this.animator.getValue());
                    }

                    RenderUtils.drawRoundedRect(0, 0, width, height, 4,
                            UISettings.INTERNAL_COLOR);

                    if (slot != -1) {
                        ItemStack itemStack = mc.thePlayer.inventory.getStackInSlot(slot);

                        if (itemStack.getItem() != null) {
                            RenderUtils.renderItemOnScreenNoDepth(itemStack, (int) (width / 2) - 8, 4);
                        }
                    }

                    Fonts.getInstance().getProductSansMedium().drawCenteredStringWithShadow(
                            String.valueOf(Math.max(this.getModule().getItemSlot(true), 0)),
                            width / 2, height - 13, -1
                    );
                }
                GlStateManager.popMatrix();
            }
        };
    };

    @EventHandler
    EventCallback<RenderEvent> onRender = event -> {
        if (event.getType() == RenderEvent.Type.FRAME) {
            if (this.rotate.getValue()) {
                if (this.aimBlockEntry != null && (this.blockEntry != null
                        || this.rotationMode.getValue() == RotationMode.NEW)) {
                    this.rotations = this.keepRotation.getValue() ?
                            RotationUtils.getBlockRotations(this.aimBlockEntry.getVector()) :
                            RotationUtils.getBlockRotations(this.aimBlockEntry.getPosition());
                }
            }
        }
    };

    @EventHandler
    EventCallback<UpdateEvent> onUpdate = event -> {
        if (startY > mc.thePlayer.posY) this.startY = ApacheMath.floor(mc.thePlayer.posY);
        this.blockEntry = null;
        int slot = this.getItemSlot(false);

        if (this.replaceBlocks.getValue() && slot == -1) {
            if (getBlockCount(true) > 0) {

                int toSwitch = 8;

                for (int i = 0; i < 9; i++) {
                    ItemStack itemStack = mc.thePlayer.inventory.mainInventory[i];
                    if (itemStack == null) {
                        toSwitch = i;
                        break;
                    }
                }

                mc.playerController.windowClick(mc.thePlayer.inventoryContainer.windowId, 0, toSwitch, 0, mc.thePlayer);
                mc.playerController.windowClick(mc.thePlayer.inventoryContainer.windowId, -999, 0, 0, mc.thePlayer);

                for (int i1 = 9; i1 < 45; ++i1) {
                    if (mc.thePlayer.inventoryContainer.getSlot(i1).getHasStack()) {
                        ItemStack is = this.mc.thePlayer.inventoryContainer.getSlot(i1).getStack();
                        if (is != null) {
                            if (!(is.getItem() instanceof ItemBlock) || !isValid(((ItemBlock) is.getItem()).getBlock()))
                                continue;

                            mc.playerController.windowClick(mc.thePlayer.inventoryContainer.windowId, i1, toSwitch, 2, mc.thePlayer);
                            break;
                        }
                    }
                }
            }
        }

        int oldSlot = mc.thePlayer.inventory.currentItem;

        if (slot != -1) {
            if (this.spoofMode.getValue() == SpoofMode.SWITCH) {
                mc.thePlayer.inventory.currentItem = slot;
            } else {
                if (!sloted || c09slot != slot) {
                    mc.thePlayer.sendQueue.getNetworkManager().sendPacket(new C09PacketHeldItemChange(slot));
                    c09slot = slot;
                    sloted = true;
                }
            }
        }

        if (Keyboard.isKeyDown(mc.gameSettings.keyBindSneak.getKeyCode()) && downwards.getValue() ||
                Keyboard.isKeyDown(mc.gameSettings.keyBindJump.getKeyCode())) {
            this.startY = Math.floor(mc.thePlayer.posY);
        }

        boolean canGoDown = this.downwards.getValue() && Keyboard.isKeyDown(mc.gameSettings.keyBindSneak.getKeyCode())
                && mc.currentScreen == null;

        double length = (this.tower.getValue() && !mc.gameSettings.keyBindJump.isKeyDown()
                || !this.tower.getValue()) ? this.expand.getValue().intValue() : 0;
        double dX = -Math.sin(Math.toRadians(MovementUtils.getDirection())) * length;
        double dZ = Math.cos(Math.toRadians(MovementUtils.getDirection())) * length;

        double y = !canGoDown ? this.startY : mc.thePlayer.posY;
        BlockPos blockPos = new BlockPos(mc.thePlayer.posX + dX, y, mc.thePlayer.posZ + dZ).down();

        if (this.downwards.getValue()) {
            if (Keyboard.isKeyDown(mc.gameSettings.keyBindSneak.getKeyCode()) && mc.currentScreen == null) {
                mc.gameSettings.keyBindSneak.pressed = false;
                blockPos = blockPos.down();
            }
        }

        this.blockEntry = this.getBlockEntry(blockPos);

        if (this.aimBlockEntry == null || this.blockEntry != null) {
            this.aimBlockEntry = this.blockEntry;
        }

        if (this.blockEntry == null) return;
        if (mc.theWorld.getBlockState(blockPos).getBlock() == Blocks.air) {
            this.place(slot);
        }
    };

    @EventHandler
    EventCallback<PacketEvent> onPacket = event -> {
        if (this.spoofMode.getValue() == SpoofMode.SPOOF) {
            if (event.getPacket() instanceof C09PacketHeldItemChange) {
                C09PacketHeldItemChange packet = event.getPacket();

                int slotId = this.getItemSlot(false);

                if (mc.thePlayer.inventory.currentItem == packet.getSlotId()) {
                    event.cancel();
                }
            }
        }
    };

    @EventHandler
    EventCallback<MotionEvent> onMotion = event -> {
        if (event.getEventState() == Event.EventState.POST) return;

        lastDistance = MovementUtils.getLastDistance();

        if (this.rotate.getValue() && this.rotations != null) {
            switch (this.rotationMode.getValue()) {
                case SNAP:
                case NEW:
                case OLD:
                    if (this.blockEntry != null || this.rotationMode.getValue() != RotationMode.SNAP) {
                        event.setYaw(this.rotations.getX());
                        event.setPitch(this.rotations.getY());
                    }
                    break;
                case FACING:
                    event.setPitch(90f);
                    break;
                case STATIC:
                    switch (this.aimBlockEntry.getFacing()) {
                        case NORTH:
                            event.setYaw(0);
                            break;

                        case SOUTH:
                            event.setYaw(180);
                            break;

                        case WEST:
                            event.setYaw(-90);
                            break;

                        case EAST:
                            event.setYaw(90);
                            break;
                    }
                    event.setPitch(80.5f);
                    break;
                case BACKWARDS:
                    event.setYaw(this.rotations.getX());
                    event.setPitch(80.5f);
                    break;

                case MMC:
                    switch (this.aimBlockEntry.getFacing()) {
                        case NORTH:
                            event.setYaw(0);
                            break;

                        case SOUTH:
                            event.setYaw(180);
                            break;

                        case WEST:
                            event.setYaw(-90);
                            break;

                        case EAST:
                            event.setYaw(90);
                            break;
                    }
                    event.setPitch(80f);
                    break;
                case SMOOTHGCD:
                    if (this.aimBlockEntry != null) {
                        if (this.smoothRotations == null) {
                            this.smoothRotations = new Vec2f();
                        }

                        this.smoothRotations.setY(90);

                        switch (this.aimBlockEntry.getFacing()) {
                            case NORTH:
                                this.smoothRotations.setX(0);
                                break;

                            case SOUTH:
                                this.smoothRotations.setX(180);
                                break;

                            case WEST:
                                this.smoothRotations.setX(-90);
                                break;

                            case EAST:
                                this.smoothRotations.setX(90);
                                break;
                        }

                        float smoothness = 70f;

                        if (this.currentRotation == null) {
                            this.currentRotation = new Vec2f(
                                    this.smoothRotations.getX(),
                                    this.smoothRotations.getY()
                            );
                        }

                        float smoothnessValue = 1 - (smoothness / 100f);

                        Vec2f smoothed = new Vec2f(
                                RotationUtils.updateRotation(
                                        this.currentRotation.getX(),
                                        this.smoothRotations.getX(),
                                        Math.max(1, 180 * smoothnessValue)
                                ),
                                RotationUtils.updateRotation(
                                        this.currentRotation.getY(),
                                        this.smoothRotations.getY(),
                                        Math.max(1, 90f * smoothnessValue)
                                )
                        );

                        float f = this.mc.gameSettings.mouseSensitivity * 0.6F + 0.2F;
                        float gcd = f * f * f * 1.2F;

                        smoothed.setX(smoothed.getX() - ((smoothed.getX() % gcd) - f));

                        this.currentRotation = smoothed;

                        event.setYaw(smoothed.getX());
                        event.setPitch(smoothed.getY());
                    }
                    break;
            }
        }

        if (this.scaffoldMode.getValue() == ScaffoldMode.HYPIXEL && this.hypixelMode.getValue() == HypixelMode.NORMAL) {
            if ((ServerUtils.isOnServer("mc.hypixel.net") || ServerUtils.isOnServer("hypixel.net")) && !(ServerUtils.compromised)) {
                if (mc.isMoveMoving() &&
                        !Client.getInstance().getModuleManager().getByClass(SpeedModule.class).getData().isEnabled()
                        && (!Keyboard.isKeyDown(mc.gameSettings.keyBindSneak.getKeyCode()) &&
                        !Keyboard.isKeyDown(mc.gameSettings.keyBindJump.getKeyCode())) && PlayerUtils.isMathGround() &&
                        mc.thePlayer.ticksExisted % 2 == 0) {
                    event.setY(event.getY() + 0.05);
                    event.setOnGround(false);
                }
            }
        }
    };

    @EventHandler
    EventCallback<MoveEvent> onMove = event -> {
        if (getItemSlot(true) == -1) return;
        switch (scaffoldMode.getValue()) {
            case HYPIXEL:
                switch (this.hypixelMode.getValue()) {
                    case NORMAL:
                        if (PlayerUtils.isMathGround() && !mc.gameSettings.keyBindSneak.isKeyDown()) {
                            MovementUtils.strafe(event, 0.287 *
                                    (this.useSpeed.getValue() ? this.speedModifier.getValue().floatValue() : 1));
                        } else if (Keyboard.isKeyDown(mc.gameSettings.keyBindSneak.getKeyCode())) {
                            mc.thePlayer.motionX *= 0.7;
                            mc.thePlayer.motionZ *= 0.7;
                        }
                        break;
                    case KEEPY:
                        if ((ServerUtils.isOnServer("mc.hypixel.net") || ServerUtils.isOnServer("hypixel.net")) && !(ServerUtils.compromised)) {
                            if (mc.thePlayer.onGround && mc.isMoveMoving() && !Keyboard.isKeyDown(mc.gameSettings.keyBindSneak.getKeyCode())
                                    && !Keyboard.isKeyDown(mc.gameSettings.keyBindJump.getKeyCode())) {
                                event.y = mc.thePlayer.motionY = 0.4f;

                                MovementUtils.strafe((MovementUtils.getBaseMoveSpeed()) * 0.75 *
                                        (mc.thePlayer.getDiagonalTicks() > 0 ? 0.65 : 1.05) *
                                        (mc.thePlayer.isPotionActive(Potion.moveSpeed) ? 0.45 : 0.75) *
                                        (this.useSpeed.getValue() ? this.speedModifier.getValue().floatValue() : 1));
                                jumped = true;
                            } else if (jumped) {
                                MovementUtils.strafe((lastDistance - 0.66F * (lastDistance - MovementUtils.getBaseMoveSpeed()))
                                        * (mc.thePlayer.isPotionActive(Potion.moveSpeed) ? 0.65 : 0.75));
                                jumped = false;
                            }
                            if (!Keyboard.isKeyDown(mc.gameSettings.keyBindJump.getKeyCode()) &&
                                    !Keyboard.isKeyDown(mc.gameSettings.keyBindSneak.getKeyCode())) {
                                event.y = mc.thePlayer.motionY = MovementUtils.getLowHopMotion(mc.thePlayer.motionY);
                            }
                        } else {
                            event.y = mc.thePlayer.motionY = Math.random() - (MovementUtils.getBaseMoveSpeed() / 6);
                        }
                        break;
                }
                break;
            case BLOCKSMC:
                if (mc.thePlayer.onGround && mc.isMoveMoving()) {
                    event.y = mc.thePlayer.motionY = 0.42f;
                    MovementUtils.strafe(event, 0.49);
                } else if (!mc.thePlayer.onGround && mc.isMoveMoving()) {
                    MovementUtils.strafe(event);
                }
                break;
            case NORMAL:
                mc.thePlayer.motionX *= (this.useSpeed.getValue() ? this.speedModifier.getValue().floatValue() : 1);
                mc.thePlayer.motionZ *= (this.useSpeed.getValue() ? this.speedModifier.getValue().floatValue() : 1);
                break;
            case VULCAN:
                if (mc.thePlayer.onGround && mc.isMoveMoving()) {
                    int slot = spoofMode.getValue() == SpoofMode.SPOOF ? c09slot : mc.thePlayer.inventory.currentItem;
                    if (mc.thePlayer.inventory.getStackInSlot(slot).stackSize >= 7) {
                        event.y = mc.thePlayer.motionY = 0.42f;
                        MovementUtils.strafe(event, MovementUtils.getBaseMoveSpeed() * 2.1);
                    } else {
                        MovementUtils.strafe(event, MovementUtils.getBaseMoveSpeed() * 0.7);
                    }
                    if (this.vulcanTimer.reach(500)) {
                        mc.thePlayer.sendQueue.getNetworkManager().sendPacketNoEvent(new C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.START_SNEAKING));
                        vulcanTimer.reset();
                    }
                    if (this.vulcanTimer.getMs() == 75 + RandomUtils.nextInt(0, 100)) {
                        mc.thePlayer.sendQueue.getNetworkManager().sendPacketNoEvent(new C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.STOP_SNEAKING));
                    }
                }
        }

        if (mc.isMoveMoving()) {
            mc.thePlayer.setSprinting(this.sprint.getValue());
        }

        if (this.tower.getValue() && mc.gameSettings.keyBindJump.isKeyDown()
                && (!mc.isMoveMoving() || this.towerMove.getValue())) {
            int count = this.getItemSlot(true);

            if (count > 0) {
                switch (this.towerMode.getValue()) {
                    case NCP:
                        if (!mc.isMoveMoving()) {
                            MovementUtils.strafe(event, 0);
                        }

                        if (this.blockEntry != null) {
                            event.y = mc.thePlayer.motionY = (mc.thePlayer.ticksExisted % 10 == 0 ? 0.02f : 0.42f);
                        }
                        break;

                    case VULCAN:
                        if (!mc.isMoveMoving()) {
                            MovementUtils.strafe(event, 0);
                        }
                        if (this.blockEntry != null) {
                            if (mc.thePlayer.onGround) {
                                event.y = mc.thePlayer.motionY = 0.42f;
                            } else if (mc.thePlayer.getAirTicks() == 5 && !mc.isMoveMoving()) {
                                event.y = mc.thePlayer.motionY = -0.4f;
                            }
                        }
                        break;

                    case FAST:
                        if (!mc.isMoveMoving()) {
                            MovementUtils.strafe(event, 0);
                        }

                        if (this.blockEntry != null) {
                            event.y = mc.thePlayer.motionY = (mc.isMoveMoving() ? 0.42f : 0);
                        }
                        break;

                    case HYPIXEL:
                        if (!mc.thePlayer.isPotionActive(Potion.jump)) {
                            boolean onHypixel = (ServerUtils.isOnServer("mc.hypixel.net") ||
                                    ServerUtils.isOnServer("hypixel.net")) && !ServerUtils.compromised;

                            if (!mc.isMoveMoving()) {
                                MovementUtils.strafe(event, 0);
                            } else {
                                MovementUtils.strafe(event, (mc.thePlayer.getDiagonalTicks() > 0 ? 0.158 : 0.205));
                            }

                            double offset = onHypixel ? 0.41 : 0.15;

                            boolean towerMove = PlayerUtils.isOnGround(offset) && mc.isMoveMoving();
                            boolean shouldRun = !mc.isMoveMoving() || towerMove;

                            if (this.blockEntry != null && shouldRun) {
                                event.y = mc.thePlayer.motionY = onHypixel ? 0.419f : 0.38f;

                                long stopTime = towerMove && mc.thePlayer.getDiagonalTicks() > 0 ? 250L : 1600L;

                                if (this.towerTimer.reach(stopTime)) {
                                    if (!onHypixel || mc.thePlayer.getDiagonalTicks() > 0) {
                                        event.y = mc.thePlayer.motionY = towerMove ? -0.1 : 0;
                                    }

                                    this.towerTimer.reset();
                                }
                            }
                        }
                        break;

                    case TELEPORT:
                        if (!mc.isMoveMoving()) {
                            MovementUtils.strafe(event, 0);
                        }

                        if (mc.theWorld.getBlockState(new BlockPos(mc.thePlayer).down()).getBlock() != Blocks.air) {
                            event.y = mc.thePlayer.motionY = 0;

                            if (mc.thePlayer.ticksExisted % 2 == 0) {
                                mc.thePlayer.setPosition(mc.thePlayer.posX, mc.thePlayer.posY + 1, mc.thePlayer.posZ);
                            }
                        }
                        break;

                    case VERUS:
                        if (!mc.isMoveMoving()) {
                            MovementUtils.strafe(event, 0);
                        }

                        if (this.blockEntry != null) {
                            event.y = mc.thePlayer.motionY = (mc.isMoveMoving() ? 0.5f : 0f);
                        }
                        break;

                    case LEGIT:
                        if (mc.gameSettings.keyBindJump.isKeyDown() && PlayerUtils.isMathGround()) {
                            mc.gameSettings.keyBindJump.pressed = true;
                        }
                        break;
                }
            }
        }
    };

    @EventHandler
    EventCallback<SafeWalkEvent> onSafeWalk = event -> {
        if (safeWalk.getValue() && !Keyboard.isKeyDown(mc.gameSettings.keyBindSneak.getKeyCode())) event.cancel();
    };

    private void place(int slot) {
        if (this.expand.getValue().doubleValue() > 0 && (this.tower.getValue()
                && !mc.gameSettings.keyBindJump.isKeyDown() || !this.tower.getValue())) {
            for (double i = 0; i < this.expand.getValue().doubleValue(); i += 0.5) {
                double dX = -Math.sin(Math.toRadians(MovementUtils.getDirection())) * i;
                double dZ = Math.cos(Math.toRadians(MovementUtils.getDirection())) * i;

                boolean canGoDown = this.downwards.getValue() && Keyboard.isKeyDown(mc.gameSettings.keyBindSneak.getKeyCode())
                        && mc.currentScreen == null;

                double blockBelowY = !canGoDown && mc.isMoveMoving() ? startY : mc.thePlayer.posY;

                BlockPos blockBelow1 = canGoDown ? new BlockPos(mc.thePlayer.posX + dX, mc.thePlayer.posY - 0.5,
                        mc.thePlayer.posZ + dZ).down()
                        : new BlockPos(mc.thePlayer.posX + dX, blockBelowY, mc.thePlayer.posZ + dZ).down();

                this.blockEntry = this.getBlockEntry(new BlockPos(blockBelow1));

                if (this.blockEntry != null) {
                    if (this.blockEntry.facing == EnumFacing.UP || this.blockEntry.facing == EnumFacing.DOWN) {
                        return;
                    }
                }

                if (BlockUtils.getBlockAtPos(blockBelow1) == Blocks.air) {
                    this.placeBlock(this.blockEntry, slot);
                }
            }

            return;
        }

        this.placeBlock(this.blockEntry, slot);
    }

    private void placeBlock(BlockEntry blockEntry, int slot) {
        if (blockEntry == null || slot == -1) return;

        if (ProtocolUtils.isOneDotEight()) {
            if (this.swing.getValue()) {
                mc.thePlayer.swingItem();
            } else {
                mc.thePlayer.sendQueue.getNetworkManager().sendPacket(new C0APacketAnimation());
            }
        }

        BlockUtils.placeBlock(
                this.blockEntry.getPosition(), this.blockEntry.getFacing(), this.blockEntry.getVector(),
                mc.thePlayer.inventory.getStackInSlot(slot)
        );

        if (!ProtocolUtils.isOneDotEight()) {
            if (this.swing.getValue()) {
                mc.thePlayer.swingItem();
            } else {
                mc.thePlayer.sendQueue.getNetworkManager().sendPacket(new C0APacketAnimation());
            }
        }
    }

    private int getItemSlot(boolean count) {
        int itemCount = (count ? 0 : -1);

        for (int i = 8; i >= 0; i--) {
            ItemStack itemStack = mc.thePlayer.inventory.getStackInSlot(i);

            if (itemStack != null && itemStack.getItem() instanceof ItemBlock) {
                if (count) {
                    itemCount += itemStack.stackSize;
                } else {
                    itemCount = i;
                }
            }
        }

        return itemCount;
    }

    private BlockEntry getBlockEntry(BlockPos pos) {

        if (isValid(mc.theWorld.getBlockState((pos.add(0, -1, 0))).getBlock())) {
            return new BlockEntry(pos.add(0, -1, 0), EnumFacing.UP);
        }
        if (isValid(mc.theWorld.getBlockState((pos.add(0, 1, 0))).getBlock())) {
            return new BlockEntry(pos.add(0, 1, 0), EnumFacing.DOWN);
        }
        if (isValid(mc.theWorld.getBlockState((pos.add(-1, 0, 0))).getBlock())) {
            return new BlockEntry(pos.add(-1, 0, 0), EnumFacing.EAST);
        }
        if (isValid(mc.theWorld.getBlockState((pos.add(1, 0, 0))).getBlock())) {
            return new BlockEntry(pos.add(1, 0, 0), EnumFacing.WEST);
        }
        if (isValid(mc.theWorld.getBlockState((pos.add(0, 0, 1))).getBlock())) {
            return new BlockEntry(pos.add(0, 0, 1), EnumFacing.NORTH);
        }
        if (isValid(mc.theWorld.getBlockState((pos.add(0, 0, -1))).getBlock())) {
            return new BlockEntry(pos.add(0, 0, -1), EnumFacing.SOUTH);
        }
        BlockPos pos1 = pos.add(-1, 0, 0);
        if (isValid(mc.theWorld.getBlockState((pos1.add(0, -1, 0))).getBlock())) {
            return new BlockEntry(pos1.add(0, -1, 0), EnumFacing.UP);
        }
        if (isValid(mc.theWorld.getBlockState((pos1.add(0, 1, 0))).getBlock())) {
            return new BlockEntry(pos1.add(0, 1, 0), EnumFacing.DOWN);
        }
        if (isValid(mc.theWorld.getBlockState((pos1.add(-1, 0, 0))).getBlock())) {
            return new BlockEntry(pos1.add(-1, 0, 0), EnumFacing.EAST);
        }
        if (isValid(mc.theWorld.getBlockState((pos1.add(1, 0, 0))).getBlock())) {
            return new BlockEntry(pos1.add(1, 0, 0), EnumFacing.WEST);
        }
        if (isValid(mc.theWorld.getBlockState((pos1.add(0, 0, 1))).getBlock())) {
            return new BlockEntry(pos1.add(0, 0, 1), EnumFacing.NORTH);
        }
        if (isValid(mc.theWorld.getBlockState((pos1.add(0, 0, -1))).getBlock())) {
            return new BlockEntry(pos1.add(0, 0, -1), EnumFacing.SOUTH);
        }


        BlockPos pos2 = pos.add(1, 0, 0);
        if (isValid(mc.theWorld.getBlockState((pos2.add(0, -1, 0))).getBlock())) {
            return new BlockEntry(pos2.add(0, -1, 0), EnumFacing.UP);
        }
        if (isValid(mc.theWorld.getBlockState((pos2.add(0, 1, 0))).getBlock())) {
            return new BlockEntry(pos2.add(0, 1, 0), EnumFacing.DOWN);
        }
        if (isValid(mc.theWorld.getBlockState((pos2.add(-1, 0, 0))).getBlock())) {
            return new BlockEntry(pos2.add(-1, 0, 0), EnumFacing.EAST);
        }
        if (isValid(mc.theWorld.getBlockState((pos2.add(1, 0, 0))).getBlock())) {
            return new BlockEntry(pos2.add(1, 0, 0), EnumFacing.WEST);
        }
        if (isValid(mc.theWorld.getBlockState((pos2.add(0, 0, 1))).getBlock())) {
            return new BlockEntry(pos2.add(0, 0, 1), EnumFacing.NORTH);
        }
        if (isValid(mc.theWorld.getBlockState((pos2.add(0, 0, -1))).getBlock())) {
            return new BlockEntry(pos2.add(0, 0, -1), EnumFacing.SOUTH);
        }


        BlockPos pos3 = pos.add(0, 0, 1);
        if (isValid(mc.theWorld.getBlockState((pos3.add(0, -1, 0))).getBlock())) {
            return new BlockEntry(pos3.add(0, -1, 0), EnumFacing.UP);
        }
        if (isValid(mc.theWorld.getBlockState((pos3.add(0, 1, 0))).getBlock())) {
            return new BlockEntry(pos3.add(0, 1, 0), EnumFacing.DOWN);
        }
        if (isValid(mc.theWorld.getBlockState((pos3.add(-1, 0, 0))).getBlock())) {
            return new BlockEntry(pos3.add(-1, 0, 0), EnumFacing.EAST);
        }
        if (isValid(mc.theWorld.getBlockState((pos3.add(1, 0, 0))).getBlock())) {
            return new BlockEntry(pos3.add(1, 0, 0), EnumFacing.WEST);
        }
        if (isValid(mc.theWorld.getBlockState((pos3.add(0, 0, 1))).getBlock())) {
            return new BlockEntry(pos3.add(0, 0, 1), EnumFacing.NORTH);
        }
        if (isValid(mc.theWorld.getBlockState((pos3.add(0, 0, -1))).getBlock())) {
            return new BlockEntry(pos3.add(0, 0, -1), EnumFacing.SOUTH);
        }

        BlockPos pos4 = pos.add(0, 0, -1);
        if (isValid(mc.theWorld.getBlockState((pos4.add(0, -1, 0))).getBlock())) {
            return new BlockEntry(pos4.add(0, -1, 0), EnumFacing.UP);
        }
        if (isValid(mc.theWorld.getBlockState((pos4.add(0, 1, 0))).getBlock())) {
            return new BlockEntry(pos4.add(0, 1, 0), EnumFacing.DOWN);
        }
        if (isValid(mc.theWorld.getBlockState((pos4.add(-1, 0, 0))).getBlock())) {
            return new BlockEntry(pos4.add(-1, 0, 0), EnumFacing.EAST);
        }
        if (isValid(mc.theWorld.getBlockState((pos4.add(1, 0, 0))).getBlock())) {
            return new BlockEntry(pos4.add(1, 0, 0), EnumFacing.WEST);
        }
        if (isValid(mc.theWorld.getBlockState((pos4.add(0, 0, 1))).getBlock())) {
            return new BlockEntry(pos4.add(0, 0, 1), EnumFacing.NORTH);
        }
        if (isValid(mc.theWorld.getBlockState((pos4.add(0, 0, -1))).getBlock())) {
            return new BlockEntry(pos4.add(0, 0, -1), EnumFacing.SOUTH);
        }

        if (isValid(mc.theWorld.getBlockState((pos1.add(0, -1, 0))).getBlock())) {
            return new BlockEntry(pos1.add(0, -1, 0), EnumFacing.UP);
        }
        if (isValid(mc.theWorld.getBlockState((pos1.add(0, 1, 0))).getBlock())) {
            return new BlockEntry(pos1.add(0, 1, 0), EnumFacing.DOWN);
        }
        if (isValid(mc.theWorld.getBlockState((pos1.add(-1, 0, 0))).getBlock())) {
            return new BlockEntry(pos1.add(-1, 0, 0), EnumFacing.EAST);
        }
        if (isValid(mc.theWorld.getBlockState((pos1.add(1, 0, 0))).getBlock())) {
            return new BlockEntry(pos1.add(1, 0, 0), EnumFacing.WEST);
        }
        if (isValid(mc.theWorld.getBlockState((pos1.add(0, 0, 1))).getBlock())) {
            return new BlockEntry(pos1.add(0, 0, 1), EnumFacing.NORTH);
        }
        if (isValid(mc.theWorld.getBlockState((pos1.add(0, 0, -1))).getBlock())) {
            return new BlockEntry(pos1.add(0, 0, -1), EnumFacing.SOUTH);
        }

        if (isValid(mc.theWorld.getBlockState((pos2.add(0, -1, 0))).getBlock())) {
            return new BlockEntry(pos2.add(0, -1, 0), EnumFacing.UP);
        }
        if (isValid(mc.theWorld.getBlockState((pos2.add(0, 1, 0))).getBlock())) {
            return new BlockEntry(pos2.add(0, 1, 0), EnumFacing.DOWN);
        }
        if (isValid(mc.theWorld.getBlockState((pos2.add(-1, 0, 0))).getBlock())) {
            return new BlockEntry(pos2.add(-1, 0, 0), EnumFacing.EAST);
        }
        if (isValid(mc.theWorld.getBlockState((pos2.add(1, 0, 0))).getBlock())) {
            return new BlockEntry(pos2.add(1, 0, 0), EnumFacing.WEST);
        }
        if (isValid(mc.theWorld.getBlockState((pos2.add(0, 0, 1))).getBlock())) {
            return new BlockEntry(pos2.add(0, 0, 1), EnumFacing.NORTH);
        }
        if (isValid(mc.theWorld.getBlockState((pos2.add(0, 0, -1))).getBlock())) {
            return new BlockEntry(pos2.add(0, 0, -1), EnumFacing.SOUTH);
        }

        if (isValid(mc.theWorld.getBlockState((pos3.add(0, -1, 0))).getBlock())) {
            return new BlockEntry(pos3.add(0, -1, 0), EnumFacing.UP);
        }
        if (isValid(mc.theWorld.getBlockState((pos3.add(0, 1, 0))).getBlock())) {
            return new BlockEntry(pos3.add(0, 1, 0), EnumFacing.DOWN);
        }
        if (isValid(mc.theWorld.getBlockState((pos3.add(-1, 0, 0))).getBlock())) {
            return new BlockEntry(pos3.add(-1, 0, 0), EnumFacing.EAST);
        }
        if (isValid(mc.theWorld.getBlockState((pos3.add(1, 0, 0))).getBlock())) {
            return new BlockEntry(pos3.add(1, 0, 0), EnumFacing.WEST);
        }
        if (isValid(mc.theWorld.getBlockState((pos3.add(0, 0, 1))).getBlock())) {
            return new BlockEntry(pos3.add(0, 0, 1), EnumFacing.NORTH);
        }
        if (isValid(mc.theWorld.getBlockState((pos3.add(0, 0, -1))).getBlock())) {
            return new BlockEntry(pos3.add(0, 0, -1), EnumFacing.SOUTH);
        }

        if (isValid(mc.theWorld.getBlockState((pos4.add(0, -1, 0))).getBlock())) {
            return new BlockEntry(pos4.add(0, -1, 0), EnumFacing.UP);
        }
        if (isValid(mc.theWorld.getBlockState((pos4.add(0, 1, 0))).getBlock())) {
            return new BlockEntry(pos4.add(0, 1, 0), EnumFacing.DOWN);
        }
        if (isValid(mc.theWorld.getBlockState((pos4.add(-1, 0, 0))).getBlock())) {
            return new BlockEntry(pos4.add(-1, 0, 0), EnumFacing.EAST);
        }
        if (isValid(mc.theWorld.getBlockState((pos4.add(1, 0, 0))).getBlock())) {
            return new BlockEntry(pos4.add(1, 0, 0), EnumFacing.WEST);
        }
        if (isValid(mc.theWorld.getBlockState((pos4.add(0, 0, 1))).getBlock())) {
            return new BlockEntry(pos4.add(0, 0, 1), EnumFacing.NORTH);
        }
        if (isValid(mc.theWorld.getBlockState((pos4.add(0, 0, -1))).getBlock())) {
            return new BlockEntry(pos4.add(0, 0, -1), EnumFacing.SOUTH);
        }

        BlockPos pos5 = pos.add(0, -1, 0);
        if (isValid(mc.theWorld.getBlockState((pos5.add(0, -1, 0))).getBlock())) {
            return new BlockEntry(pos5.add(0, -1, 0), EnumFacing.UP);
        }
        if (isValid(mc.theWorld.getBlockState((pos5.add(0, 1, 0))).getBlock())) {
            return new BlockEntry(pos5.add(0, 1, 0), EnumFacing.DOWN);
        }
        if (isValid(mc.theWorld.getBlockState((pos5.add(-1, 0, 0))).getBlock())) {
            return new BlockEntry(pos5.add(-1, 0, 0), EnumFacing.EAST);
        }
        if (isValid(mc.theWorld.getBlockState((pos5.add(1, 0, 0))).getBlock())) {
            return new BlockEntry(pos5.add(1, 0, 0), EnumFacing.WEST);
        }
        if (isValid(mc.theWorld.getBlockState((pos5.add(0, 0, 1))).getBlock())) {
            return new BlockEntry(pos5.add(0, 0, 1), EnumFacing.NORTH);
        }
        if (isValid(mc.theWorld.getBlockState((pos5.add(0, 0, -1))).getBlock())) {
            return new BlockEntry(pos5.add(0, 0, -1), EnumFacing.SOUTH);
        }

        BlockPos pos6 = pos5.add(1, 0, 0);
        if (isValid(mc.theWorld.getBlockState((pos6.add(0, -1, 0))).getBlock())) {
            return new BlockEntry(pos6.add(0, -1, 0), EnumFacing.UP);
        }
        if (isValid(mc.theWorld.getBlockState((pos6.add(0, 1, 0))).getBlock())) {
            return new BlockEntry(pos6.add(0, 1, 0), EnumFacing.DOWN);
        }
        if (isValid(mc.theWorld.getBlockState((pos6.add(-1, 0, 0))).getBlock())) {
            return new BlockEntry(pos6.add(-1, 0, 0), EnumFacing.EAST);
        }
        if (isValid(mc.theWorld.getBlockState((pos6.add(1, 0, 0))).getBlock())) {
            return new BlockEntry(pos6.add(1, 0, 0), EnumFacing.WEST);
        }
        if (isValid(mc.theWorld.getBlockState((pos6.add(0, 0, 1))).getBlock())) {
            return new BlockEntry(pos6.add(0, 0, 1), EnumFacing.NORTH);
        }
        if (isValid(mc.theWorld.getBlockState((pos6.add(0, 0, -1))).getBlock())) {
            return new BlockEntry(pos6.add(0, 0, -1), EnumFacing.SOUTH);
        }

        BlockPos pos7 = pos5.add(-1, 0, 0);
        if (isValid(mc.theWorld.getBlockState((pos7.add(0, -1, 0))).getBlock())) {
            return new BlockEntry(pos7.add(0, -1, 0), EnumFacing.UP);
        }
        if (isValid(mc.theWorld.getBlockState((pos7.add(0, 1, 0))).getBlock())) {
            return new BlockEntry(pos7.add(0, 1, 0), EnumFacing.DOWN);
        }
        if (isValid(mc.theWorld.getBlockState(pos7.add(-1, 0, 0)).getBlock())) {
            return new BlockEntry(pos7.add(-1, 0, 0), EnumFacing.EAST);
        }
        if (isValid(mc.theWorld.getBlockState(pos7.add(1, 0, 0)).getBlock())) {
            return new BlockEntry(pos7.add(1, 0, 0), EnumFacing.WEST);
        }
        if (isValid(mc.theWorld.getBlockState(pos7.add(0, 0, 1)).getBlock())) {
            return new BlockEntry(pos7.add(0, 0, 1), EnumFacing.NORTH);
        }
        if (isValid(mc.theWorld.getBlockState(pos7.add(0, 0, -1)).getBlock())) {
            return new BlockEntry(pos7.add(0, 0, -1), EnumFacing.SOUTH);
        }

        BlockPos pos8 = pos5.add(0, 0, 1);
        if (isValid(mc.theWorld.getBlockState(pos8.add(0, -1, 0)).getBlock())) {
            return new BlockEntry(pos8.add(0, -1, 0), EnumFacing.UP);
        }
        if (isValid(mc.theWorld.getBlockState((pos8.add(0, 1, 0))).getBlock())) {
            return new BlockEntry(pos8.add(0, 1, 0), EnumFacing.DOWN);
        }
        if (isValid(mc.theWorld.getBlockState(pos8.add(-1, 0, 0)).getBlock())) {
            return new BlockEntry(pos8.add(-1, 0, 0), EnumFacing.EAST);
        }
        if (isValid(mc.theWorld.getBlockState(pos8.add(1, 0, 0)).getBlock())) {
            return new BlockEntry(pos8.add(1, 0, 0), EnumFacing.WEST);
        }
        if (isValid(mc.theWorld.getBlockState(pos8.add(0, 0, 1)).getBlock())) {
            return new BlockEntry(pos8.add(0, 0, 1), EnumFacing.NORTH);
        }
        if (isValid(mc.theWorld.getBlockState(pos8.add(0, 0, -1)).getBlock())) {
            return new BlockEntry(pos8.add(0, 0, -1), EnumFacing.SOUTH);
        }

        BlockPos pos9 = pos5.add(0, 0, -1);
        if (isValid(mc.theWorld.getBlockState(pos9.add(0, -1, 0)).getBlock())) {
            return new BlockEntry(pos9.add(0, -1, 0), EnumFacing.UP);
        }
        if (isValid(mc.theWorld.getBlockState((pos9.add(0, 1, 0))).getBlock())) {
            return new BlockEntry(pos9.add(0, 1, 0), EnumFacing.DOWN);
        }
        if (isValid(mc.theWorld.getBlockState(pos9.add(-1, 0, 0)).getBlock())) {
            return new BlockEntry(pos9.add(-1, 0, 0), EnumFacing.EAST);
        }
        if (isValid(mc.theWorld.getBlockState(pos9.add(1, 0, 0)).getBlock())) {
            return new BlockEntry(pos9.add(1, 0, 0), EnumFacing.WEST);
        }
        if (isValid(mc.theWorld.getBlockState(pos9.add(0, 0, 1)).getBlock())) {
            return new BlockEntry(pos9.add(0, 0, 1), EnumFacing.NORTH);
        }
        if (isValid(mc.theWorld.getBlockState(pos9.add(0, 0, -1)).getBlock())) {
            return new BlockEntry(pos9.add(0, 0, -1), EnumFacing.SOUTH);
        }
        return null;
    }

    private int getBlockCount(Boolean count) {
        int itemCount = (count ? 0 : -1);

        for (int i = 36; i >= 0; i--) {
            ItemStack itemStack = mc.thePlayer.inventory.getStackInSlot(i);

            if (itemStack != null && itemStack.getItem() instanceof ItemBlock) {
                if (count) {
                    itemCount += itemStack.stackSize;
                } else {
                    itemCount = i;
                }
            }
        }

        return itemCount;
    }

    private boolean isValid(Block block) {
        return !BlockUtils.INVALID_BLOCKS.contains(block);
    }

    @AllArgsConstructor
    private enum SpoofMode {
        SWITCH("Switch"),
        SPOOF("Spoof");

        private final String label;
    }

    @AllArgsConstructor
    private enum TowerMode {
        NCP("NCP"),
        VULCAN("Vulcan"),
        FAST("Fast"),
        HYPIXEL("Hypixel"),
        VERUS("Verus"),
        LEGIT("Legit"),
        TELEPORT("Teleport");

        private final String label;
    }

    @AllArgsConstructor
    private enum RotationMode {
        SNAP("Snap"),
        OLD("Old"),
        NEW("New"),
        FACING("Facing"),
        STATIC("Static"),
        MMC("MMC"),
        BACKWARDS("Backwards"),
        SMOOTHGCD("Smooth GCD");

        private final String label;
    }

    @AllArgsConstructor
    private enum ScaffoldMode {
        NORMAL("Normal"),
        BLOCKSMC("BlocksMC"),
        HYPIXEL("Hypixel"),
        VULCAN("Vulcan");

        private final String label;
    }

    @AllArgsConstructor
    private enum HypixelMode {
        NORMAL("Normal"),
        KEEPY("KeepY");

        private final String label;
    }

    @Override
    public String getSuffix() {
        return this.rotationMode.getValue().label;
    }

    @AllArgsConstructor
    @Getter
    public static class CustomHitVec {
        private final float x;
        private final float y;
        private final float z;
        private final float scale;
    }

    @Getter
    @Setter
    public static class BlockEntry {
        private final BlockPos position;
        private final EnumFacing facing;
        private final Vec3 vector;

        public BlockEntry(BlockPos position, EnumFacing facing) {
            this.position = position;
            this.facing = facing;

            CustomHitVec customHitVec = new CustomHitVec(.5f, .5f, .5f, .5f);
            this.vector = new Vec3(position)
                    .add(new Vec3(customHitVec.getX(), customHitVec.getY(), customHitVec.getZ()))
                    .add(new Vec3(facing.getDirectionVec()).scale(customHitVec.getScale()));
        }
    }
}