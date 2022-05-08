package dev.africa.pandaware.impl.module.movement;

import dev.africa.pandaware.api.event.Event;
import dev.africa.pandaware.api.event.interfaces.EventCallback;
import dev.africa.pandaware.api.event.interfaces.EventHandler;
import dev.africa.pandaware.api.module.Module;
import dev.africa.pandaware.api.module.event.TaskedEventListener;
import dev.africa.pandaware.api.module.interfaces.Category;
import dev.africa.pandaware.api.module.interfaces.ModuleInfo;
import dev.africa.pandaware.impl.event.player.MotionEvent;
import dev.africa.pandaware.impl.event.player.MoveEvent;
import dev.africa.pandaware.impl.event.player.SafeWalkEvent;
import dev.africa.pandaware.impl.event.player.UpdateEvent;
import dev.africa.pandaware.impl.event.render.RenderEvent;
import dev.africa.pandaware.impl.font.Fonts;
import dev.africa.pandaware.impl.setting.BooleanSetting;
import dev.africa.pandaware.impl.setting.EnumSetting;
import dev.africa.pandaware.impl.setting.NumberSetting;
import dev.africa.pandaware.impl.ui.UISettings;
import dev.africa.pandaware.switcher.ViaMCP;
import dev.africa.pandaware.utils.client.ServerUtils;
import dev.africa.pandaware.utils.math.TimeHelper;
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
import net.minecraft.block.BlockAir;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C0APacketAnimation;
import net.minecraft.potion.Potion;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Vec3;
import org.lwjgl.input.Keyboard;

@Getter
@ModuleInfo(name = "Scaffold", category = Category.MOVEMENT)
public class ScaffoldModule extends Module {
    private final EnumSetting<ScaffoldMode> scaffoldMode = new EnumSetting<>("Mode", ScaffoldMode.HYPIXEL);
    private final BooleanSetting itemSpoof = new BooleanSetting("Item Spoof", true);
    private final BooleanSetting tower = new BooleanSetting("Tower", true);
    private final EnumSetting<RotationMode> rotationMode = new EnumSetting<>("Rotation mode", RotationMode.NEW);
    private final BooleanSetting sprint = new BooleanSetting("Sprint", true);
    private final EnumSetting<SpoofMode> spoofMode = new EnumSetting<>("Spoof mode", SpoofMode.SPOOF,
            this.itemSpoof::getValue);
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

    private BlockEntry blockEntry;
    private BlockEntry aimBlockEntry;
    private Vec2f rotations;
    private int lastSlot;
    private double startY;
    private Vec2f smoothRotations;
    private Vec2f currentRotation;
    private final TimeHelper towerTimer = new TimeHelper();
    boolean shouldRun2;

    public ScaffoldModule() {
        this.registerSettings(
                this.scaffoldMode,
                this.spoofMode,
                this.rotationMode,
                this.towerMode,
                this.swing,
                this.rotate,
                this.overwriteAura,
                this.keepRotation,
                this.itemSpoof,
                this.replaceBlocks,
                this.downwards,
                this.tower,
                this.towerMove,
                this.sprint,
                this.safeWalk,
                this.useSpeed,
                this.speedModifier,
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
        this.shouldRun2 = false;

        this.lastSlot = mc.thePlayer.inventory.currentItem;
    }

    @Override
    public void onDisable() {
        mc.timer.timerSpeed = 1.0F;
        this.blockEntry = null;
        this.aimBlockEntry = null;
        this.rotations = null;

        if (this.itemSpoof.getValue() && this.spoofMode.getValue() == SpoofMode.SWITCH) {
            mc.thePlayer.inventory.currentItem = this.lastSlot;
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


        if (this.itemSpoof.getValue() && this.spoofMode.getValue() == SpoofMode.SWITCH && slot != -1) {
            mc.thePlayer.inventory.currentItem = slot;
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

        this.blockEntry = this.find(new Vec3(0, 0, 0));

        if (this.aimBlockEntry == null || this.blockEntry != null) {
            this.aimBlockEntry = this.blockEntry;
        }

        if (this.blockEntry == null) return;
        if (mc.theWorld.getBlockState(blockPos).getBlock() == Blocks.air) {
            this.place(slot, this.itemSpoof.getValue());
        }
    };

    @EventHandler
    EventCallback<MotionEvent> onMotion = event -> {
        if (event.getEventState() == Event.EventState.POST) return;

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

                        float smoothness = 90f;

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
    };

    @EventHandler
    EventCallback<MoveEvent> onMove = event -> {
        if (getItemSlot(true) == -1) return;
        switch (scaffoldMode.getValue()) {
            case HYPIXEL:
                if (mc.thePlayer.onGround && mc.isMoveMoving() && !Keyboard.isKeyDown(mc.gameSettings.keyBindSneak.getKeyCode())
                        && !Keyboard.isKeyDown(mc.gameSettings.keyBindJump.getKeyCode())) {
                    event.y = mc.thePlayer.motionY = 0.4f;
                    MovementUtils.strafe((MovementUtils.getBaseMoveSpeed() * 0.75) *
                            (this.useSpeed.getValue() ? this.speedModifier.getValue().floatValue() : 1));
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
                            } else if (mc.thePlayer.getAirTicks() == 5) {
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
                            boolean onHypixel = ServerUtils.isOnServer("mc.hypixel.net") || ServerUtils.isOnServer("hypixel.net");

                            if (!mc.isMoveMoving()) {
                                MovementUtils.strafe(event, 0);
                            } else {
                                MovementUtils.strafe(event, (mc.thePlayer.getDiagonalTicks() > 0 ? 0.158 : 0.205));
                            }

                            double offset = onHypixel ? 0.41 : 0.15;

                            boolean towerMove = PlayerUtils.isOnGround(offset) && mc.isMoveMoving();
                            boolean shouldRun = !mc.isMoveMoving() || towerMove;

                            if (shouldRun && mc.thePlayer.onGround && mc.gameSettings.keyBindJump.isKeyDown()) {
                                shouldRun2 = true;
                            } else if (!mc.gameSettings.keyBindJump.isKeyDown()) {
                                shouldRun2 = false;
                            }

                            if (this.blockEntry != null && shouldRun2) {
                                event.y = mc.thePlayer.motionY = 0.419999999f;

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

    private void place(int slot, boolean spoofItem) {
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

                this.blockEntry = this.find(new Vec3(0, -1, 0));

                if (BlockUtils.getBlockAtPos(blockBelow1) == Blocks.air) {
                    this.placeBlock(this.blockEntry, slot, spoofItem);
                }
            }

            return;
        }

        this.placeBlock(this.blockEntry, slot, spoofItem);
    }

    private void placeBlock(BlockEntry blockEntry, int slot, boolean spoofItem) {
        if (blockEntry == null || slot == -1) return;

        if (spoofItem && this.spoofMode.getValue() == SpoofMode.SPOOF) {
            this.lastSlot = mc.thePlayer.inventory.currentItem;
            mc.thePlayer.inventory.currentItem = slot;
        }

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

        if (spoofItem && this.spoofMode.getValue() == SpoofMode.SPOOF) {
            mc.thePlayer.inventory.currentItem = this.lastSlot;
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

    public BlockEntry find(Vec3 offset3) {
        BlockPos position = new BlockPos(mc.thePlayer.getPositionVector().add(offset3)).offset(EnumFacing.DOWN);
        for (EnumFacing facing : EnumFacing.values()) {
            BlockPos offset = position.offset(facing);

            boolean canGoDown = this.downwards.getValue() && Keyboard.isKeyDown(mc.gameSettings.keyBindSneak.getKeyCode())
                    && mc.currentScreen == null;

            if (canGoDown) {
                offset = offset.down();
            }

            if (mc.theWorld.getBlockState(offset).getBlock() instanceof BlockAir
                    || !rayTrace(mc.thePlayer.getLook(0.0f),
                    getPositionByFace(offset, invert[facing.ordinal()])))
                continue;

            return new BlockEntry(offset, invert[facing.ordinal()]);
        }

        BlockPos[] offsets = new BlockPos[]{new BlockPos(-1, 0, 0),
                new BlockPos(1, 0, 0), new BlockPos(0, 0, -1), new BlockPos(0, 0, 1)};

        for (BlockPos offset : offsets) {
            BlockPos offsetPos = position.add(offset.getX(), 0, offset.getZ());

            boolean canGoDown = this.downwards.getValue() && Keyboard.isKeyDown(mc.gameSettings.keyBindSneak.getKeyCode())
                    && mc.currentScreen == null;

            if (canGoDown) {
                offsetPos = offsetPos.down();
            }

            if (!(mc.theWorld.getBlockState(offsetPos).getBlock() instanceof BlockAir)) continue;
            for (EnumFacing facing : EnumFacing.values()) {
                BlockPos offset2 = offsetPos.offset(facing);
                if (mc.theWorld.getBlockState(offset2).getBlock() instanceof BlockAir
                        || !rayTrace(mc.thePlayer.getLook(0.0f),
                        getPositionByFace(offset, invert[facing.ordinal()])))
                    continue;

                return new BlockEntry(offset2, invert[facing.ordinal()]);
            }
        }
        return null;
    }

    public boolean rayTrace(Vec3 origin, Vec3 position) {
        Vec3 difference = position.subtract(origin);
        int steps = 10;
        double x = difference.xCoord / (double) steps;
        double y = difference.yCoord / (double) steps;
        double z = difference.zCoord / (double) steps;
        Vec3 point = origin;

        for (int i = 0; i < steps; ++i) {
            BlockPos blockPosition = new BlockPos(point = point.addVector(x, y, z));
            IBlockState blockState = mc.theWorld.getBlockState(blockPosition);
            if (blockState.getBlock() instanceof BlockLiquid || blockState.getBlock() instanceof BlockAir) continue;
            AxisAlignedBB boundingBox = blockState.getBlock()
                    .getCollisionBoundingBox(mc.theWorld, blockPosition, blockState);
            if (boundingBox == null) {
                boundingBox = new AxisAlignedBB(0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
            }
            if (!boundingBox.offset(blockPosition.getX(), blockPosition.getY(), blockPosition.getZ()).isVecInside(point))
                continue;
            return false;
        }

        return true;
    }

    public Vec3 getPositionByFace(BlockPos position, EnumFacing facing) {
        Vec3 offset = new Vec3((double) facing.getDirectionVec().getX() / 2.0,
                (double) facing.getDirectionVec().getY() / 2.0, (double) facing.getDirectionVec().getZ() / 2.0);
        Vec3 point = new Vec3((double) position.getX() + 0.5,
                (double) position.getY() + 0.75, (double) position.getZ() + 0.5);
        return point.add(offset);
    }

    public final EnumFacing[] invert = new EnumFacing[]{
            EnumFacing.UP, EnumFacing.DOWN,
            EnumFacing.SOUTH, EnumFacing.NORTH,
            EnumFacing.EAST, EnumFacing.WEST};

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
        HYPIXEL("Hypixel");

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