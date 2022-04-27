package dev.africa.pandaware.impl.module.player;

import dev.africa.pandaware.api.event.interfaces.EventCallback;
import dev.africa.pandaware.api.event.interfaces.EventHandler;
import dev.africa.pandaware.api.module.Module;
import dev.africa.pandaware.api.module.interfaces.ModuleInfo;
import dev.africa.pandaware.impl.event.game.TickEvent;
import dev.africa.pandaware.impl.setting.BooleanSetting;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;

@ModuleInfo(name = "AutoTool", shortcut = {"at"}, description = "skidded from Sigma 4.11 L")
public class AutoToolModule extends Module {
    private BooleanSetting onlyOnHold = new BooleanSetting("Only On Key Hold", false);

    public static void updateTool(BlockPos pos) {
        Block block = mc.theWorld.getBlockState(pos).getBlock();
        float strength = 1.0F;
        int bestItemIndex = -1;
        for (int i = 0; i < 9; i++) {
            ItemStack itemStack = mc.thePlayer.inventory.mainInventory[i];
            if (itemStack == null) {
                continue;
            }
            if ((itemStack.getStrVsBlock(block) > strength)) {
                strength = itemStack.getStrVsBlock(block);
                bestItemIndex = i;
            }
        }
        if (bestItemIndex != -1) {
            mc.thePlayer.inventory.currentItem = bestItemIndex;
        }
    }

    @EventHandler
    EventCallback<TickEvent> onTick = event -> {
        if (!mc.gameSettings.keyBindAttack.isKeyDown() ||
                onlyOnHold.getValue() && mc.gameSettings.keyBindAttack.isPressed() ||
                mc.objectMouseOver == null) return;
        BlockPos pos = mc.objectMouseOver.getBlockPos();
        if (pos == null) return;
        updateTool(pos);
    };
}
