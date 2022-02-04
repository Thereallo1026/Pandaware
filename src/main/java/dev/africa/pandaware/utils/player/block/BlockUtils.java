package dev.africa.pandaware.utils.player.block;

import dev.africa.pandaware.api.interfaces.MinecraftInstance;
import lombok.experimental.UtilityClass;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Vec3;

import java.util.Arrays;
import java.util.List;

@UtilityClass
public class BlockUtils implements MinecraftInstance {
    public void placeBlock(BlockPos blockPos, EnumFacing face, Vec3 vec, ItemStack stack) {
        if (mc.thePlayer != null && mc.theWorld != null) {
            mc.playerController.onPlayerRightClick(mc.thePlayer, mc.theWorld, stack, blockPos, face, vec);
        }
    }

    public final List<Block> INVALID_BLOCKS = Arrays.asList(
            Blocks.enchanting_table, Blocks.chest, Blocks.ender_chest,
            Blocks.trapped_chest, Blocks.anvil,
            Blocks.web, Blocks.torch, Blocks.crafting_table,
            Blocks.furnace, Blocks.waterlily, Blocks.dispenser,
            Blocks.stone_pressure_plate, Blocks.wooden_pressure_plate, Blocks.noteblock,
            Blocks.dropper, Blocks.tnt, Blocks.standing_banner, Blocks.cactus,
            Blocks.wall_banner, Blocks.redstone_torch, Blocks.air, Blocks.water,
            Blocks.fire, Blocks.flowing_water, Blocks.lava, Blocks.flowing_lava
    );
}
