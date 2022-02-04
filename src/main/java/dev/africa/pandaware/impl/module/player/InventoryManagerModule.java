package dev.africa.pandaware.impl.module.player;

import dev.africa.pandaware.Client;
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
import dev.africa.pandaware.utils.player.block.BlockUtils;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.init.Blocks;
import net.minecraft.item.*;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;

@ModuleInfo(name = "Inventory Manager", category = Category.PLAYER)
public class InventoryManagerModule extends Module {

    private static final int BLOCK_CAP = 1024, WEAPON_SLOT = 36, PICKAXE_SLOT = 37, AXE_SLOT = 38, SHOVEL_SLOT = 39;
    private final TimeHelper timer = new TimeHelper();

    private final NumberSetting delay = new NumberSetting("Delay", 1000, 0, 81);
    private final NumberSetting randomMax = new NumberSetting("Random Max", 1000, 0, 50);
    private final NumberSetting randomMin = new NumberSetting("Random Min", 1000, 0, 0);
    private final BooleanSetting random = new BooleanSetting("Randomization", false);
    private final BooleanSetting archery = new BooleanSetting("Clean Bows-Arrows", true);
    private final BooleanSetting food = new BooleanSetting("Clean food", true);
    private final BooleanSetting sword = new BooleanSetting("Prefer swords", true);
    private final BooleanSetting keepEmpty = new BooleanSetting("Keep Empty", false);
    private final BooleanSetting openInventory = new BooleanSetting("Open Inventory", false);

    public long lastClean;

    public InventoryManagerModule() {
        this.registerSettings(this.delay, this.randomMax, this.randomMin, this.random,
                this.archery, this.food, this.sword, this.keepEmpty, this.openInventory);
    }

    @EventHandler
    EventCallback<UpdateEvent> onUpdate = event -> {
        final long time = delay.getValue().intValue() + (random.getValue() ? RandomUtils.nextInt(randomMin.getValue().intValue(), randomMax.getValue().intValue()) : 0);
        final AutoArmorModule autoArmor = Client.getInstance().getModuleManager().getByClass(AutoArmorModule.class);
        final long autoArmorDelay = autoArmor.delayVal;

        if (timer.reach(autoArmorDelay) && autoArmor.getData().isEnabled()) {
            if (!autoArmor.getOpenInventory().getValue() || mc.currentScreen instanceof GuiInventory) {
                if (mc.currentScreen == null || mc.currentScreen instanceof GuiInventory || mc.currentScreen instanceof GuiChat) {
                    getBestArmor();
                }
            }
        }
        if (autoArmor.getData().isEnabled()) {
            for (int type = 1; type < 5; type++) {
                if (mc.thePlayer.inventoryContainer.getSlot(4 + type).getHasStack()) {
                    ItemStack is = mc.thePlayer.inventoryContainer.getSlot(4 + type).getStack();
                    if (!isBestArmor(is, type)) {
                        return;
                    }
                } else if (invContainsType(type - 1)) {
                    return;
                }
            }
        }

        if (this.openInventory.getValue() && !(mc.currentScreen instanceof GuiInventory))
            return;

        if (mc.currentScreen == null || mc.currentScreen instanceof GuiInventory || mc.currentScreen instanceof GuiChat) {
            if (timer.reach(time)) {
                if (!mc.thePlayer.inventoryContainer.getSlot(WEAPON_SLOT).getHasStack()) {
                    getBestWeapon(WEAPON_SLOT);
                } else {
                    if (!isBestWeapon(mc.thePlayer.inventoryContainer.getSlot(WEAPON_SLOT).getStack())) {
                        getBestWeapon(WEAPON_SLOT);
                    }
                }
            }

            if (timer.reach(time))
                getBestPickaxe(PICKAXE_SLOT);

            if (timer.reach(time))
                getBestShovel(SHOVEL_SLOT);

            if (timer.reach(time))
                getBestAxe(AXE_SLOT);

            if (timer.reach(time)) {
                for (int i = 9; i < 45; i++) {
                    if (mc.thePlayer.inventoryContainer.getSlot(i).getHasStack()) {
                        ItemStack is = mc.thePlayer.inventoryContainer.getSlot(i).getStack();
                        if (shouldDrop(is, i)) {
                            drop(i);
                            timer.reset();
                            lastClean = System.currentTimeMillis();
                            if (time > 0) {
                                break;
                            }
                        }
                    }
                }
            }
        }
    };

    public void shiftClick(int slot) {
        mc.playerController.windowClick(mc.thePlayer.inventoryContainer.windowId, slot, 0, 1, mc.thePlayer);
        lastClean = System.currentTimeMillis();
    }

    public void swap(int slot1, int hotbarSlot) {
        mc.playerController.windowClick(mc.thePlayer.inventoryContainer.windowId, slot1, hotbarSlot, 2, mc.thePlayer);
        lastClean = System.currentTimeMillis();
    }

    public void drop(int slot) {
        mc.playerController.windowClick(mc.thePlayer.inventoryContainer.windowId, slot, 1, 4, mc.thePlayer);
        lastClean = System.currentTimeMillis();
    }

    public boolean isBestWeapon(ItemStack stack) {
        float damage = getDamage(stack);
        for (int i = 9; i < 45; i++) {
            if (mc.thePlayer.inventoryContainer.getSlot(i).getHasStack()) {
                ItemStack is = mc.thePlayer.inventoryContainer.getSlot(i).getStack();
                if (getDamage(is) > damage && (is.getItem() instanceof ItemSword || !sword.getValue()))
                    return false;
            }
        }
        return stack.getItem() instanceof ItemSword || !sword.getValue();
    }

    public void getBestWeapon(int slot) {
        for (int i = 9; i < 45; i++) {
            if (mc.thePlayer.inventoryContainer.getSlot(i).getHasStack()) {
                ItemStack is = mc.thePlayer.inventoryContainer.getSlot(i).getStack();
                if (isBestWeapon(is) && getDamage(is) > 0 && (is.getItem() instanceof ItemSword || !sword.getValue())) {
                    swap(i, slot - 36);
                    timer.reset();
                    break;
                }
            }
        }
    }

    private float getDamage(ItemStack stack) {
        float damage = 0;
        Item item = stack.getItem();
        if (item instanceof ItemTool) {
            ItemTool tool = (ItemTool) item;
            damage += tool.getDamageVsEntity();
        }
        if (item instanceof ItemSword) {
            ItemSword sword = (ItemSword) item;
            damage += sword.getAttackDamage();
        }
        damage += EnchantmentHelper.getEnchantmentLevel(Enchantment.sharpness.effectId, stack) * 1.25f +
                EnchantmentHelper.getEnchantmentLevel(Enchantment.fireAspect.effectId, stack) * 0.01f;
        return damage;
    }

    public boolean shouldDrop(ItemStack stack, int slot) {
        String itemName = stack.getItem().getUnlocalizedName().toLowerCase();
        if (stack.getDisplayName().toLowerCase().contains("(right click)")) {
            return false;
        }
        if (stack.getDisplayName().toLowerCase().contains("§k||")) {
            return false;
        }
        if ((slot == WEAPON_SLOT && isBestWeapon(mc.thePlayer.inventoryContainer.getSlot(WEAPON_SLOT).getStack())) ||
                (slot == PICKAXE_SLOT && isBestPickaxe(mc.thePlayer.inventoryContainer.getSlot(PICKAXE_SLOT).getStack()) && PICKAXE_SLOT >= 0) ||
                (slot == AXE_SLOT && isBestAxe(mc.thePlayer.inventoryContainer.getSlot(AXE_SLOT).getStack()) && AXE_SLOT >= 0) ||
                (slot == SHOVEL_SLOT && isBestShovel(mc.thePlayer.inventoryContainer.getSlot(SHOVEL_SLOT).getStack()) && SHOVEL_SLOT >= 0)) {
            return false;
        }
        if (stack.getItem() instanceof ItemArmor) {
            for (int type = 1; type < 5; type++) {
                if (mc.thePlayer.inventoryContainer.getSlot(4 + type).getHasStack()) {
                    ItemStack is = mc.thePlayer.inventoryContainer.getSlot(4 + type).getStack();
                    if (isBestArmor(is, type)) {
                        continue;
                    }
                }
                if (isBestArmor(stack, type)) {
                    return false;
                }
            }
        }
        if (stack.getItem() instanceof ItemBlock &&
                (getBlockCount() > BLOCK_CAP ||
                        BlockUtils.INVALID_BLOCKS.contains(((ItemBlock) stack.getItem()).getBlock()))) {
            return true;
        }

        if (stack.getItem() instanceof ItemPotion) {
            return isBadPotion(stack);
        }

        if (stack.getItem() instanceof ItemFood && food.getValue() && !(stack.getItem() instanceof ItemAppleGold)) {
            return true;
        }

        if (stack.getItem() instanceof ItemHoe || stack.getItem() instanceof ItemTool || stack.getItem() instanceof ItemSword || stack.getItem() instanceof ItemArmor) {
            return true;
        }

        if ((stack.getItem() instanceof ItemBow || itemName.contains("arrow")) && archery.getValue()) {
            return true;
        }

        if ((itemName.contains("bowl") ||
                (itemName.contains("bucket") && !itemName.contains("water") &&
                        !itemName.contains("lava") && !itemName.contains("milk")) ||
                (stack.getItem() instanceof ItemGlassBottle && !keepEmpty.getValue())) && !keepEmpty.getValue()) {
            return true;
        }
        return (itemName.contains("tnt")) ||
                (itemName.contains("stick")) ||
                (itemName.contains("egg")) ||
                (itemName.contains("string")) ||
                (itemName.contains("cake")) ||
                (itemName.contains("mushroom") && !itemName.contains("stew")) ||
                (itemName.contains("flint")) ||
                (itemName.contains("dyepowder")) ||
                (itemName.contains("feather")) ||
                (itemName.contains("chest") && !stack.getDisplayName().toLowerCase().contains("collect")) ||
                (itemName.contains("snow")) ||
                (itemName.contains("fish")) ||
                (itemName.contains("enchant")) ||
                (itemName.contains("exp")) ||
                (itemName.contains("shears")) ||
                (itemName.contains("anvil")) ||
                (itemName.contains("torch")) ||
                (itemName.contains("seeds")) ||
                (itemName.contains("leather")) ||
                (itemName.contains("reeds")) ||
                (itemName.contains("skull")) ||
                (itemName.contains("record")) ||
                (itemName.contains("snowball")) ||
                (itemName.contains("piston"));
    }

    private int getBlockCount() {
        int blockCount = 0;
        for (int i = 0; i < 45; i++) {
            if (mc.thePlayer.inventoryContainer.getSlot(i).getHasStack()) {
                ItemStack is = mc.thePlayer.inventoryContainer.getSlot(i).getStack();
                Item item = is.getItem();
                if (is.getItem() instanceof ItemBlock && !BlockUtils.INVALID_BLOCKS.contains(((ItemBlock) item).getBlock())) {
                    blockCount += is.stackSize;
                }
            }
        }
        return blockCount;
    }

    private void getBestPickaxe(int slot) {
        for (int i = 9; i < 45; i++) {
            if (mc.thePlayer.inventoryContainer.getSlot(i).getHasStack()) {
                ItemStack is = mc.thePlayer.inventoryContainer.getSlot(i).getStack();
                if (isBestPickaxe(is) && PICKAXE_SLOT != i) {
                    if (!isBestWeapon(is)) {
                        if (!mc.thePlayer.inventoryContainer.getSlot(PICKAXE_SLOT).getHasStack()) {
                            swap(i, PICKAXE_SLOT - 36);
                            timer.reset();
                            if (delay.getValue().longValue() > 0)
                                return;
                        } else if (!isBestPickaxe(mc.thePlayer.inventoryContainer.getSlot(PICKAXE_SLOT).getStack())) {
                            swap(i, PICKAXE_SLOT - 36);
                            timer.reset();
                            if (delay.getValue().longValue() > 0)
                                return;
                        }
                    }
                }
            }
        }
    }

    private void getBestShovel(int slot) {
        for (int i = 9; i < 45; i++) {
            if (mc.thePlayer.inventoryContainer.getSlot(i).getHasStack()) {
                ItemStack is = mc.thePlayer.inventoryContainer.getSlot(i).getStack();
                if (isBestShovel(is) && SHOVEL_SLOT != i) {
                    if (!isBestWeapon(is)) {
                        if (!mc.thePlayer.inventoryContainer.getSlot(SHOVEL_SLOT).getHasStack()) {
                            swap(i, SHOVEL_SLOT - 36);
                            timer.reset();
                            if (delay.getValue().longValue() > 0)
                                return;
                        } else if (!isBestShovel(mc.thePlayer.inventoryContainer.getSlot(SHOVEL_SLOT).getStack())) {
                            swap(i, SHOVEL_SLOT - 36);
                            timer.reset();
                            if (delay.getValue().longValue() > 0)
                                return;
                        }
                    }
                }
            }
        }
    }

    private void getBestAxe(int slot) {
        for (int i = 9; i < 45; i++) {
            if (mc.thePlayer.inventoryContainer.getSlot(i).getHasStack()) {
                ItemStack is = mc.thePlayer.inventoryContainer.getSlot(i).getStack();
                if (isBestAxe(is) && AXE_SLOT != i) {
                    if (!isBestWeapon(is)) {
                        if (!mc.thePlayer.inventoryContainer.getSlot(AXE_SLOT).getHasStack()) {
                            swap(i, AXE_SLOT - 36);
                            timer.reset();
                            if (delay.getValue().longValue() > 0)
                                return;
                        } else if (!isBestAxe(mc.thePlayer.inventoryContainer.getSlot(AXE_SLOT).getStack())) {
                            swap(i, AXE_SLOT - 36);
                            timer.reset();
                            if (delay.getValue().longValue() > 0)
                                return;
                        }
                    }
                }
            }
        }
    }

    private boolean isBestPickaxe(ItemStack stack) {
        Item item = stack.getItem();
        if (!(item instanceof ItemPickaxe))
            return false;
        float value = getToolEffect(stack);
        for (int i = 9; i < 45; i++) {
            if (mc.thePlayer.inventoryContainer.getSlot(i).getHasStack()) {
                ItemStack is = mc.thePlayer.inventoryContainer.getSlot(i).getStack();
                if (getToolEffect(is) > value && is.getItem() instanceof ItemPickaxe) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean isBestShovel(ItemStack stack) {
        Item item = stack.getItem();
        if (!(item instanceof ItemSpade))
            return false;
        float value = getToolEffect(stack);
        for (int i = 9; i < 45; i++) {
            if (mc.thePlayer.inventoryContainer.getSlot(i).getHasStack()) {
                ItemStack is = mc.thePlayer.inventoryContainer.getSlot(i).getStack();
                if (getToolEffect(is) > value && is.getItem() instanceof ItemSpade) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean isBestAxe(ItemStack stack) {
        Item item = stack.getItem();
        if (!(item instanceof ItemAxe))
            return false;
        float value = getToolEffect(stack);
        for (int i = 9; i < 45; i++) {
            if (mc.thePlayer.inventoryContainer.getSlot(i).getHasStack()) {
                ItemStack is = mc.thePlayer.inventoryContainer.getSlot(i).getStack();
                if (getToolEffect(is) > value && is.getItem() instanceof ItemAxe && !isBestWeapon(stack)) {
                    return false;
                }
            }
        }
        return true;
    }

    private float getToolEffect(ItemStack stack) {
        Item item = stack.getItem();
        if (!(item instanceof ItemTool))
            return 0;
        String name = item.getUnlocalizedName();
        ItemTool tool = (ItemTool) item;
        float value;
        if (item instanceof ItemPickaxe) {
            value = tool.getStrVsBlock(stack, Blocks.stone);
            if (name.toLowerCase().contains("gold")) {
                value -= 5;
            }
        } else if (item instanceof ItemSpade) {
            value = tool.getStrVsBlock(stack, Blocks.dirt);
            if (name.toLowerCase().contains("gold")) {
                value -= 5;
            }
        } else if (item instanceof ItemAxe) {
            value = tool.getStrVsBlock(stack, Blocks.log);
            if (name.toLowerCase().contains("gold")) {
                value -= 5;
            }
        } else
            return 1f;
        value += EnchantmentHelper.getEnchantmentLevel(Enchantment.efficiency.effectId, stack) * 0.0075D;
        value += EnchantmentHelper.getEnchantmentLevel(Enchantment.unbreaking.effectId, stack) / 100d;
        return value;
    }

    private boolean isBadPotion(ItemStack stack) {
        if (stack != null && stack.getItem() instanceof ItemPotion) {
            final ItemPotion potion = (ItemPotion) stack.getItem();
            if (potion.getEffects(stack) == null && !potion.hasEffect(stack)) {
                return true;
            }
            for (final Object o : potion.getEffects(stack)) {
                final PotionEffect effect = (PotionEffect) o;
                if (effect.getPotionID() == Potion.poison.getId() || effect.getPotionID() == Potion.harm.getId() || effect.getPotionID() == Potion.moveSlowdown.getId() || effect.getPotionID() == Potion.weakness.getId()) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean invContainsType(int type) {

        for (int i = 9; i < 45; i++) {
            if (mc.thePlayer.inventoryContainer.getSlot(i).getHasStack()) {
                ItemStack is = mc.thePlayer.inventoryContainer.getSlot(i).getStack();
                Item item = is.getItem();
                if (item instanceof ItemArmor) {
                    ItemArmor armor = (ItemArmor) item;
                    if (type == armor.armorType) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private void getBestArmor() {
        for (int type = 1; type < 5; type++) {
            if (mc.thePlayer.inventoryContainer.getSlot(4 + type).getHasStack()) {
                ItemStack is = mc.thePlayer.inventoryContainer.getSlot(4 + type).getStack();
                if (isBestArmor(is, type)) {
                    continue;
                } else {
                    drop(4 + type);
                }
            }
            for (int i = 9; i < 45; i++) {
                if (mc.thePlayer.inventoryContainer.getSlot(i).getHasStack()) {
                    ItemStack is = mc.thePlayer.inventoryContainer.getSlot(i).getStack();
                    if (isBestArmor(is, type) && getProtection(is) > 0) {
                        shiftClick(i);
                        timer.reset();
                        if (delay.getValue().longValue() > 0)
                            return;
                    }
                }
            }
        }
    }

    private boolean isBestArmor(ItemStack stack, int type) {
        float prot = getProtection(stack);
        String strType = "";
        if (type == 1) {
            strType = "helmet";
        } else if (type == 2) {
            strType = "chestplate";
        } else if (type == 3) {
            strType = "leggings";
        } else if (type == 4) {
            strType = "boots";
        }
        if (!stack.getUnlocalizedName().contains(strType)) {
            return false;
        }
        for (int i = 5; i < 45; i++) {
            if (mc.thePlayer.inventoryContainer.getSlot(i).getHasStack()) {
                ItemStack is = mc.thePlayer.inventoryContainer.getSlot(i).getStack();
                if (getProtection(is) > prot && is.getUnlocalizedName().contains(strType))
                    return false;
            }
        }
        return true;
    }

    private float getProtection(ItemStack stack) {
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
