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
import dev.africa.pandaware.impl.event.player.UpdateEvent;
import dev.africa.pandaware.impl.event.render.RenderEvent;
import dev.africa.pandaware.impl.font.Fonts;
import dev.africa.pandaware.impl.setting.BooleanSetting;
import dev.africa.pandaware.impl.setting.EnumSetting;
import dev.africa.pandaware.impl.setting.NumberSetting;
import dev.africa.pandaware.impl.ui.UISettings;
import dev.africa.pandaware.utils.network.ProtocolUtils;
import dev.africa.pandaware.utils.player.MovementUtils;
import dev.africa.pandaware.utils.player.RotationUtils;
import dev.africa.pandaware.utils.render.RenderUtils;
import dev.africa.pandaware.utils.render.StencilUtils;
import dev.africa.pandaware.utils.math.vector.Vec2f;
import dev.africa.pandaware.utils.player.block.BlockUtils;
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
import net.minecraft.network.play.client.C0APacketAnimation;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Vec3;
import org.lwjgl.input.Keyboard;

@ModuleInfo(name = "Scaffold", category = Category.MOVEMENT)
public class ScaffoldModule extends Module {
    private final BooleanSetting itemSpoof = new BooleanSetting("Item Spoof", true);
    private final BooleanSetting tower = new BooleanSetting("Tower", true);
    private final EnumSetting<RotationMode> rotationMode = new EnumSetting<>("Rotation mode", RotationMode.NEW);
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
    private final NumberSetting speedModifier = new NumberSetting("Speed modifier", 1.5, 0.1,
            1, 0.1);

    private BlockEntry blockEntry;
    private BlockEntry aimBlockEntry;
    private Vec2f rotations;
    private int lastSlot;

    public ScaffoldModule() {
        this.registerSettings(
                this.spoofMode,
                this.rotationMode,
                this.towerMode,
                this.swing,
                this.rotate,
                this.keepRotation,
                this.itemSpoof,
                this.downwards,
                this.tower,
                this.towerMove,
                this.speedModifier
        );

        this.setTaskedEvent(this.taskedEventListener);
    }

    @Override
    public void onEnable() {
        this.blockEntry = null;
        this.aimBlockEntry = null;
        this.rotations = null;

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
                            RenderUtils.renderItemOnScreenNoDepth(
                                    itemStack,
                                    (int) (width / 2) - 8, 4
                            );
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


        if (this.itemSpoof.getValue() && this.spoofMode.getValue() == SpoofMode.SWITCH && slot != -1) {
            mc.thePlayer.inventory.currentItem = slot;
        }

        BlockPos blockPos = new BlockPos(mc.thePlayer).down();
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

        if (this.blockEntry != null) {
            this.placeBlock(this.blockEntry, slot, this.itemSpoof.getValue());
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
                case FORWARD:
                    event.setPitch(90f);
                    break;
                case BACKWARDS:
                    event.setYaw(event.getYaw() - 180f);
                    event.setPitch(80.5f);
                    break;
            }
        }
    };

    @EventHandler
    EventCallback<MoveEvent> onMove = event -> {
        if (this.speedModifier.getValue().floatValue() != 1.0f) {
            MovementUtils.strafe(event, MovementUtils.getBaseMoveSpeed() * this.speedModifier.getValue().floatValue());
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
                    case FAST:
                        if (!mc.isMoveMoving()) {
                            MovementUtils.strafe(event, 0);
                        }

                        if (this.blockEntry != null) {
                            event.y = mc.thePlayer.motionY = (mc.isMoveMoving() ? 0.42f : 42f);
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
                }
            }
        }
    };

    private void placeBlock(BlockEntry blockEntry, int slot, boolean spoofItem) {
        if (blockEntry == null || slot == -1) {
            return;
        }

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

        ItemStack offHandStack = mc.thePlayer.getHeldItem();
        if (offHandStack != null && count && offHandStack.getItem() instanceof ItemBlock) {
            itemCount += offHandStack.stackSize;
        }

        return itemCount;
    }

    private BlockEntry getBlockEntry(BlockPos pos) {
        if (mc.theWorld.getBlockState(pos).getBlock() != Blocks.air) return null;

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
        FAST("Fast"),
        TELEPORT("Teleport");

        private final String label;
    }

    @AllArgsConstructor
    private enum RotationMode {
        SNAP("Snap"),
        OLD("Old"),
        NEW("New"),
        FORWARD("Forward"),
        BACKWARDS("Backwards");

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