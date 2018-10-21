package mineguard.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemShield;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.item.ItemTool;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntityBanner;
import net.minecraft.util.EnumHand;

public class ItemUtil
{
    private static Map<String, Double> meleeEnchantments = new HashMap<String, Double>();
    private static Map<String, Double> rangeEnchantments = new HashMap<String, Double>();
    private static Map<String, Double> armorEnchantments = new HashMap<String, Double>();
    static {
        // Heuristical enchantment rating

        // Those values are homogeneous with damages per second per enchantment level
        meleeEnchantments.put("enchantment.damage.all", 0.0); // Should be set to 0 (enchantment already applied)
        meleeEnchantments.put("enchantment.damage.arthropods", 0.5);
        meleeEnchantments.put("enchantment.damage.undead", 1.5);
        meleeEnchantments.put("enchantment.fire", 2.0);
        meleeEnchantments.put("enchantment.knockback", 1.0);
        meleeEnchantments.put("enchantment.lootBonus", 0.3);

        // Those values are homogeneous with units per enchantment level
        rangeEnchantments.put("enchantment.arrowDamage", 1.0);
        rangeEnchantments.put("enchantment.arrowFire", 2.0);
        rangeEnchantments.put("enchantment.arrowKnockback", 0.5);

        // Those values are homogeneous with damages absorbed per enchantment level
        armorEnchantments.put("enchantment.protect.all", 2.0);
        armorEnchantments.put("enchantment.protect.explosion", 2.0);
        armorEnchantments.put("enchantment.protect.fall", 1.0);
        armorEnchantments.put("enchantment.protect.fire", 1.0);
        armorEnchantments.put("enchantment.protect.projectile", 2.0);
    }

    public enum WeaponClass
    {
        MELEE,
        RANGE,
        SHIELD,
        UNKNOWN
    }

    public static WeaponClass UNKNOWN_CLASS;

    public static WeaponClass getWeaponClass(Item item)
    {
        if (item instanceof ItemSword || item instanceof ItemTool)
            return WeaponClass.MELEE;
        else if (item instanceof ItemBow)
            return WeaponClass.RANGE;
        else if (item instanceof ItemShield)
            return WeaponClass.SHIELD;
        else
            return UNKNOWN_CLASS;
    }

    public static boolean isWeapon(Item item)
    {
        return Arrays.asList(WeaponClass.values()).contains(getWeaponClass(item));
    }

    public static void swapHands(EntityLivingBase entity)
    {
        ItemStack itemstack = entity.getHeldItem(EnumHand.OFF_HAND);
        entity.setHeldItem(EnumHand.OFF_HAND, entity.getHeldItem(EnumHand.MAIN_HAND));
        entity.setHeldItem(EnumHand.MAIN_HAND, itemstack);
    }

    public static void swapHandsIfNeeded(EntityLivingBase entity)
    {
        ItemStack mainHand = entity.getItemStackFromSlot(EntityEquipmentSlot.MAINHAND);
        ItemStack offHand = entity.getItemStackFromSlot(EntityEquipmentSlot.OFFHAND);
        WeaponClass mainHandClass = ItemUtil.getWeaponClass(mainHand.getItem());
        WeaponClass offHandClass = ItemUtil.getWeaponClass(offHand.getItem());
        double mainHandScore = ItemUtil.getWeaponScore(entity, mainHand);
        double offHandScore = ItemUtil.getWeaponScore(entity, offHand);

        if (mainHandClass == WeaponClass.SHIELD
                || (mainHandClass == WeaponClass.RANGE && offHandClass == WeaponClass.MELEE)
                || (mainHandClass == offHandClass && offHandScore > mainHandScore)
                || (!isWeapon(mainHand.getItem()) && offHandClass != WeaponClass.SHIELD)
                || (!isWeapon(offHand.getItem()) && offHandClass == WeaponClass.SHIELD)) {
            ItemUtil.swapHands(entity);
        }
    }

    public static boolean isColored(ItemStack stack)
    {
        NBTTagCompound nbttagcompound = stack.getSubCompound("BlockEntityTag");
        return nbttagcompound != null && nbttagcompound.hasKey("Base");
    }

    private static double getModifiedAttribute(EntityLivingBase entity, ItemStack itemStack, IAttribute attribute)
    {
        double value = entity.getEntityAttribute(attribute).getBaseValue();
        Collection<AttributeModifier> modifiers = itemStack.getItem()
                .getAttributeModifiers(EntityEquipmentSlot.MAINHAND, null).get(attribute.getName());
        for (AttributeModifier modifier : modifiers)
            value += modifier.getAmount();

        return value;
    }

    public static double getAttackDamage(EntityLivingBase entity, ItemStack weapon)
    {
        double value = getModifiedAttribute(entity, weapon, SharedMonsterAttributes.ATTACK_DAMAGE);
        value += EnchantmentHelper.getModifierForCreature(weapon, EnumCreatureAttribute.UNDEFINED);

        return value;
    }

    public static double getAttackSpeed(EntityLivingBase entity, ItemStack weapon)
    {
        return getModifiedAttribute(entity, weapon, SharedMonsterAttributes.ATTACK_SPEED);
    }

    public static double getWeaponScore(EntityLivingBase entity, ItemStack weapon)
    {
        double score = 0;
        Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(weapon);

        if (ItemUtil.getWeaponClass(weapon.getItem()) == WeaponClass.MELEE) {
            double weaponDamages = ItemUtil.getAttackDamage(entity, weapon);
            double weaponSpeed = ItemUtil.getAttackSpeed(entity, weapon);
            score = weaponDamages * weaponSpeed;

            // Apply enchantment modifiers
            for (Enchantment enchantment : enchantments.keySet())
                score += meleeEnchantments.get(enchantment.getName()) != null
                        ? meleeEnchantments.get(enchantment.getName()) * enchantments.get(enchantment) * weaponSpeed
                        : 0;
        } else if (ItemUtil.getWeaponClass(weapon.getItem()) == WeaponClass.RANGE) {
            // Apply enchantment modifiers
            for (Enchantment enchantment : enchantments.keySet())
                score += rangeEnchantments.get(enchantment.getName()) != null
                        ? rangeEnchantments.get(enchantment.getName()) * enchantments.get(enchantment)
                        : 0;
        } else if (ItemUtil.getWeaponClass(weapon.getItem()) == WeaponClass.SHIELD) {
            // Prioritise customised shields
            score = TileEntityBanner.getPatterns(weapon);
            score += ItemUtil.isColored(weapon) ? 1 : 0;
        }

        return score;
    }

    public static double getArmorScore(EntityLivingBase entity, ItemStack armor)
    {
        double score = ((ItemArmor) armor.getItem()).damageReduceAmount;

        // Apply enchantment modifiers
        Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(armor);
        for (Enchantment enchantment : enchantments.keySet())
            score += armorEnchantments.get(enchantment.getName()) != null
                    ? armorEnchantments.get(enchantment.getName()) * enchantments.get(enchantment)
                    : 0;

        return score;
    }
}
