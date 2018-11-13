package mineguard.util;

import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.block.Block;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemHoe;
import net.minecraft.item.ItemShield;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.item.ItemTool;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntityBanner;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.translation.I18n;

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
        if (item instanceof ItemSword || item instanceof ItemTool || item instanceof ItemHoe)
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

    public static List<String> getTooltip(ItemStack itemStack, EntityLivingBase entity, EntityPlayer player,
            ITooltipFlag advanced)
    {
        DecimalFormat DECIMALFORMAT = new DecimalFormat("#.##");
        List<String> list = Lists.<String>newArrayList();
        String s = itemStack.getDisplayName();

        if (itemStack.hasDisplayName())
            s = TextFormatting.ITALIC + s;
        s = s + TextFormatting.RESET;

        if (advanced.isAdvanced()) {
            String s1 = "";

            if (!s.isEmpty()) {
                s = s + " (";
                s1 = ")";
            }

            int i = Item.getIdFromItem(itemStack.getItem());

            if (itemStack.getHasSubtypes())
                s = s + String.format("#%04d/%d%s", i, itemStack.getItemDamage(), s1);
            else
                s = s + String.format("#%04d%s", i, s1);
        } else if (!itemStack.hasDisplayName() && itemStack.getItem() == Items.FILLED_MAP) {
            s = s + " #" + itemStack.getItemDamage();
        }

        list.add(s);
        int i1 = 0;

        if (itemStack.hasTagCompound() && itemStack.getTagCompound().hasKey("HideFlags", 99))
            i1 = itemStack.getTagCompound().getInteger("HideFlags");

        if ((i1 & 32) == 0)
            itemStack.getItem().addInformation(itemStack, entity == null ? null : entity.world, list, advanced);

        if (itemStack.hasTagCompound()) {
            if ((i1 & 1) == 0) {
                NBTTagList nbttaglist = itemStack.getEnchantmentTagList();

                for (int j = 0; j < nbttaglist.tagCount(); j++) {
                    NBTTagCompound nbttagcompound = nbttaglist.getCompoundTagAt(j);
                    int k = nbttagcompound.getShort("id");
                    int l = nbttagcompound.getShort("lvl");
                    Enchantment enchantment = Enchantment.getEnchantmentByID(k);

                    if (enchantment != null)
                        list.add(enchantment.getTranslatedName(l));
                }
            }

            if (itemStack.getTagCompound().hasKey("display", 10)) {
                NBTTagCompound nbttagcompound1 = itemStack.getTagCompound().getCompoundTag("display");

                if (nbttagcompound1.hasKey("color", 3)) {
                    if (advanced.isAdvanced()) {
                        list.add(I18n.translateToLocalFormatted("item.color",
                                String.format("#%06X", nbttagcompound1.getInteger("color"))));
                    } else {
                        list.add(TextFormatting.ITALIC + I18n.translateToLocal("item.dyed"));
                    }
                }

                if (nbttagcompound1.getTagId("Lore") == 9) {
                    NBTTagList nbttaglist3 = nbttagcompound1.getTagList("Lore", 8);

                    if (!nbttaglist3.hasNoTags()) {
                        for (int l1 = 0; l1 < nbttaglist3.tagCount(); l1++) {
                            list.add(TextFormatting.DARK_PURPLE + "" + TextFormatting.ITALIC
                                    + nbttaglist3.getStringTagAt(l1));
                        }
                    }
                }
            }
        }

        for (EntityEquipmentSlot entityequipmentslot : EntityEquipmentSlot.values()) {
            Multimap<String, AttributeModifier> multimap = itemStack.getAttributeModifiers(entityequipmentslot);

            if (!multimap.isEmpty() && (i1 & 2) == 0) {
                list.add("");
                list.add(I18n.translateToLocal("item.modifiers." + entityequipmentslot.getName()));

                for (Entry<String, AttributeModifier> entry : multimap.entries()) {
                    AttributeModifier attributemodifier = entry.getValue();
                    double d0 = attributemodifier.getAmount();
                    boolean flag = false;

                    if (entity != null) {

                        if (attributemodifier.getID() == Item.ATTACK_DAMAGE_MODIFIER) {
                            d0 = d0 + entity.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getBaseValue();
                            d0 = d0 + (double) EnchantmentHelper.getModifierForCreature(itemStack,
                                    EnumCreatureAttribute.UNDEFINED);
                            flag = true;
                        } else if (attributemodifier.getID() == Item.ATTACK_SPEED_MODIFIER) {
                            d0 += entity.getEntityAttribute(SharedMonsterAttributes.ATTACK_SPEED).getBaseValue();
                            flag = true;
                        }
                    }

                    double d1;

                    if (attributemodifier.getOperation() != 1 && attributemodifier.getOperation() != 2)
                        d1 = d0;
                    else
                        d1 = d0 * 100.0D;

                    if (flag) {
                        list.add(" " + I18n.translateToLocalFormatted(
                                "attribute.modifier.equals." + attributemodifier.getOperation(),
                                DECIMALFORMAT.format(d1),
                                I18n.translateToLocal("attribute.name." + (String) entry.getKey())));
                    } else if (d0 > 0.0D) {
                        list.add(TextFormatting.BLUE + " " + I18n.translateToLocalFormatted(
                                "attribute.modifier.plus." + attributemodifier.getOperation(), DECIMALFORMAT.format(d1),
                                I18n.translateToLocal("attribute.name." + (String) entry.getKey())));
                    } else if (d0 < 0.0D) {
                        d1 = d1 * -1.0D;
                        list.add(TextFormatting.RED + " " + I18n.translateToLocalFormatted(
                                "attribute.modifier.take." + attributemodifier.getOperation(), DECIMALFORMAT.format(d1),
                                I18n.translateToLocal("attribute.name." + (String) entry.getKey())));
                    }
                }
            }
        }

        if (itemStack.hasTagCompound() && itemStack.getTagCompound().getBoolean("Unbreakable") && (i1 & 4) == 0)
            list.add(TextFormatting.BLUE + I18n.translateToLocal("item.unbreakable"));

        if (itemStack.hasTagCompound() && itemStack.getTagCompound().hasKey("CanDestroy", 9) && (i1 & 8) == 0) {
            NBTTagList nbttaglist1 = itemStack.getTagCompound().getTagList("CanDestroy", 8);

            if (!nbttaglist1.hasNoTags()) {
                list.add("");
                list.add(TextFormatting.GRAY + I18n.translateToLocal("item.canBreak"));

                for (int j1 = 0; j1 < nbttaglist1.tagCount(); j1++) {
                    Block block = Block.getBlockFromName(nbttaglist1.getStringTagAt(j1));

                    if (block != null)
                        list.add(TextFormatting.DARK_GRAY + block.getLocalizedName());
                    else
                        list.add(TextFormatting.DARK_GRAY + "missingno");
                }
            }
        }

        if (itemStack.hasTagCompound() && itemStack.getTagCompound().hasKey("CanPlaceOn", 9) && (i1 & 16) == 0) {
            NBTTagList nbttaglist2 = itemStack.getTagCompound().getTagList("CanPlaceOn", 8);

            if (!nbttaglist2.hasNoTags()) {
                list.add("");
                list.add(TextFormatting.GRAY + I18n.translateToLocal("item.canPlace"));

                for (int k1 = 0; k1 < nbttaglist2.tagCount(); k1++) {
                    Block block1 = Block.getBlockFromName(nbttaglist2.getStringTagAt(k1));

                    if (block1 != null)
                        list.add(TextFormatting.DARK_GRAY + block1.getLocalizedName());
                    else
                        list.add(TextFormatting.DARK_GRAY + "missingno");
                }
            }
        }

        if (advanced.isAdvanced()) {
            if (itemStack.isItemDamaged()) {
                list.add(I18n.translateToLocalFormatted("item.durability",
                        itemStack.getMaxDamage() - itemStack.getItemDamage(), itemStack.getMaxDamage()));
            }

            list.add(TextFormatting.DARK_GRAY
                    + ((ResourceLocation) Item.REGISTRY.getNameForObject(itemStack.getItem())).toString());

            if (itemStack.hasTagCompound()) {
                list.add(TextFormatting.DARK_GRAY + I18n.translateToLocalFormatted("item.nbt_tags",
                        itemStack.getTagCompound().getKeySet().size()));
            }
        }

        net.minecraftforge.event.ForgeEventFactory.onItemTooltip(itemStack, player, list, advanced);
        return list;
    }
}
