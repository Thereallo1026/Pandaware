package dev.africa.pandaware.impl.module.player;

import dev.africa.pandaware.api.event.interfaces.EventCallback;
import dev.africa.pandaware.api.event.interfaces.EventHandler;
import dev.africa.pandaware.api.interfaces.MinecraftInstance;
import dev.africa.pandaware.api.module.Module;
import dev.africa.pandaware.api.module.interfaces.Category;
import dev.africa.pandaware.api.module.interfaces.ModuleInfo;
import dev.africa.pandaware.impl.event.player.UpdateEvent;
import dev.africa.pandaware.impl.setting.BooleanSetting;
import dev.africa.pandaware.impl.setting.NumberSetting;
import dev.africa.pandaware.utils.math.TimeHelper;
import dev.africa.pandaware.utils.math.random.RandomUtils;
import lombok.Getter;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;

@Getter
@ModuleInfo(name = "Auto Armor", category = Category.PLAYER)
public class AutoArmorModule extends Module {

    private final int[] boots = new int[]{313, 309, 317, 305, 301};
    private final int[] chestplate = new int[]{311, 307, 315, 303, 299};
    private final int[] helmet = new int[]{310, 306, 314, 302, 298};
    private final int[] leggings = new int[]{312, 308, 316, 304, 300};
    private final TimeHelper timer = new TimeHelper();
    private final NumberSetting delay = new NumberSetting("Delay", 1000, 0, 80);
    private final NumberSetting randomMax = new NumberSetting("Random Max", 1000, 0, 50);
    private final NumberSetting randomMin = new NumberSetting("Random Min", 1000, 0, 0);
    private final BooleanSetting random = new BooleanSetting("Randomization", false);
    private final BooleanSetting openInventory = new BooleanSetting("Open Inventory", false);
    private double maxValue = -1.0D;
    private double mv;
    protected long delayVal;
    private int item = -1;
    private int num = 5;

    public static long lastCycle;

    public AutoArmorModule() {
        registerSettings(delay, randomMax, randomMin, random, openInventory);
    }

    @EventHandler
    EventCallback<UpdateEvent> onUpdate = event -> {
        if (this.openInventory.getValue() && !(mc.currentScreen instanceof GuiInventory))
            return;
        delayVal = delay.getValue().intValue() +
                (random.getValue() ? RandomUtils.nextInt(randomMin.getValue().intValue(), randomMax.getValue().intValue()) : 0);
        if (!mc.thePlayer.capabilities.isCreativeMode) {
            if (timer.reach(delayVal)) {
                this.maxValue = -1.0D;
                this.item = -1;

                for (int i = 9; i < 45; ++i) {
                    if (mc.thePlayer.inventoryContainer.getSlot(i).getStack() != null && this.canEquip(mc.thePlayer.inventoryContainer.getSlot(i).getStack()) != -1 && this.canEquip(mc.thePlayer.inventoryContainer.getSlot(i).getStack()) == this.num) {
                        lastCycle = System.currentTimeMillis();
                        this.change(this.num, i);
                    }
                }

                if (this.item != -1) {
                    if (mc.thePlayer.inventoryContainer.getSlot(this.item).getStack() != null) {
                        mc.playerController.windowClick(0, this.num, 0, 1, mc.thePlayer);
                    }

                    mc.playerController.windowClick(0, this.item, 0, 1, mc.thePlayer);

                    lastCycle = System.currentTimeMillis();
                }

                this.num = this.num == 8 ? 5 : ++this.num;
                this.timer.reset();
            }
        }
    };

    private int canEquip(ItemStack stack) {
        int[] var5;
        int var4 = (var5 = this.boots).length;

        int id4;
        int var3;
        for (var3 = 0; var3 < var4; ++var3) {
            id4 = var5[var3];
            stack.getItem();
            if (Item.getIdFromItem(stack.getItem()) == id4) {
                return 8;
            }
        }

        var4 = (var5 = this.leggings).length;

        for (var3 = 0; var3 < var4; ++var3) {
            id4 = var5[var3];
            stack.getItem();
            if (Item.getIdFromItem(stack.getItem()) == id4) {
                return 7;
            }
        }

        var4 = (var5 = this.chestplate).length;

        for (var3 = 0; var3 < var4; ++var3) {
            id4 = var5[var3];
            stack.getItem();
            if (Item.getIdFromItem(stack.getItem()) == id4) {
                return 6;
            }
        }

        var4 = (var5 = this.helmet).length;

        for (var3 = 0; var3 < var4; ++var3) {
            id4 = var5[var3];
            stack.getItem();
            if (Item.getIdFromItem(stack.getItem()) == id4) {
                return 5;
            }
        }

        return -1;
    }

    private void change(int numy, int i) {
        this.mv = this.maxValue == -1.0D ? (mc.thePlayer.inventoryContainer.getSlot(numy).getStack() != null ? this.getProtValue(mc.thePlayer.inventoryContainer.getSlot(numy).getStack()) : this.maxValue) : this.maxValue;
        if (this.mv <= this.getProtValue(mc.thePlayer.inventoryContainer.getSlot(i).getStack()) && this.mv != this.getProtValue(mc.thePlayer.inventoryContainer.getSlot(i).getStack())) {
            this.item = i;
            this.maxValue = this.getProtValue(mc.thePlayer.inventoryContainer.getSlot(i).getStack());
        }

    }

    private double getProtValue(ItemStack stack) {
        float prot = 0;
        if ((stack.getItem() instanceof ItemArmor)) {
            ItemArmor armor = (ItemArmor) stack.getItem();
            prot += armor.damageReduceAmount + (100 - armor.damageReduceAmount) * EnchantmentHelper.getEnchantmentLevel(Enchantment.protection.effectId, stack) * 0.0075D;
            prot += EnchantmentHelper.getEnchantmentLevel(Enchantment.blastProtection.effectId, stack) / 100d;
            prot += EnchantmentHelper.getEnchantmentLevel(Enchantment.fireProtection.effectId, stack) / 100d;
            prot += EnchantmentHelper.getEnchantmentLevel(Enchantment.thorns.effectId, stack) / 100d;
            prot += EnchantmentHelper.getEnchantmentLevel(Enchantment.unbreaking.effectId, stack) / 50d;
            prot += EnchantmentHelper.getEnchantmentLevel(Enchantment.projectileProtection.effectId, stack) / 100d;
        }
        return prot;
    }
}