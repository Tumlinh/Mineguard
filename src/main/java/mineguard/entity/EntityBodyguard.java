package mineguard.entity;

import java.util.Arrays;
import java.util.IllegalFormatException;
import java.util.Random;
import javax.annotation.Nullable;
import mineguard.Mineguard;
import mineguard.Troop;
import mineguard.client.gui.troop.GuiHandler;
import mineguard.entity.ai.EntityAIAttackMelee;
import mineguard.entity.ai.EntityAIBehaviour;
import mineguard.entity.ai.EntityAIReform;
import mineguard.init.ModConfigServer;
import mineguard.util.EntityUtil;
import mineguard.util.ItemUtil;
import mineguard.util.ItemUtil.WeaponClass;
import mineguard.util.NBTUtil;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIOpenDoor;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagLong;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.pathfinding.PathNavigateGround;
import net.minecraft.tileentity.BannerPattern;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;

public class EntityBodyguard extends EntityCreature
{
    public static final DataParameter<Float> HEALTH = EntityDataManager.<Float>createKey(EntityBodyguard.class,
            DataSerializers.FLOAT);
    public static final DataParameter<String> MASTER_NAME = EntityDataManager.<String>createKey(EntityBodyguard.class,
            DataSerializers.STRING);

    private int id = NBTUtil.UNDEFINED;
    private Troop troop;
    private IInventory inventory;
    private boolean containerOpen = false;
    private long timeSinceRegeneration;
    public EntityAIReform reformTask;
    public EntityAIBehaviour behaviourTask;

    // Called when spawning entities from NBT or hatching egg
    public EntityBodyguard(World worldIn)
    {
        super(worldIn);
        this.setSize(0.6F, 1.8F);
        this.enablePersistence();
        this.setCanPickUpLoot(true);
        timeSinceRegeneration = 0;

        ((PathNavigateGround) this.getNavigator()).setCanSwim(true);
        ((PathNavigateGround) this.getNavigator()).setBreakDoors(true);

        // Force equipment drop on death without damaging it, except for the helmet
        Arrays.fill(inventoryHandsDropChances, 2.0F);
        Arrays.fill(inventoryArmorDropChances, 2.0F);
        inventoryArmorDropChances[EntityEquipmentSlot.HEAD.getIndex()] = 0.0F;
    }

    // Called when spawning new bodyguards as part of a troop
    public EntityBodyguard(World worldIn, int id, Troop troop)
    {
        this(worldIn);
        this.id = id;
        this.troop = troop;
        if (troop != null)
            troop.addBodyguard(this);

        // Name bodyguard
        this.updateName();

        // Put on armor
        this.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, new ItemStack(Items.DIAMOND_SWORD));
        this.setItemStackToSlot(EntityEquipmentSlot.FEET, new ItemStack(Items.DIAMOND_BOOTS));
        this.setItemStackToSlot(EntityEquipmentSlot.CHEST, new ItemStack(Items.DIAMOND_CHESTPLATE));
        this.setItemStackToSlot(EntityEquipmentSlot.LEGS, new ItemStack(Items.DIAMOND_LEGGINGS));
        this.putOnColorizedHelmet();
        this.putOnFunkyShield();
    }

    public int getId()
    {
        return id;
    }

    public void setId(int id)
    {
        this.id = id;
    }

    public Troop getTroop()
    {
        return troop;
    }

    public void setTroop(Troop troop)
    {
        this.troop = troop;
    }

    public IInventory getInventory()
    {
        return inventory;
    }

    public void setInventory(IInventory inventory)
    {
        this.inventory = inventory;
    }

    public void updateName()
    {
        try {
            this.setCustomNameTag(String.format(troop.getSettings().getNameFormat(), id));
        } catch (IllegalFormatException e) {
            System.out.println(troop.getSettings().getNameFormat());
        }
        this.setAlwaysRenderNameTag(troop.getSettings().isDisplayName());
    }

    public void putOnColorizedHelmet()
    {
        ItemStack helmet = new ItemStack(Items.LEATHER_HELMET);

        // Make helmet unbreakable
        NBTTagCompound compound = new NBTTagCompound();
        compound.setBoolean("Unbreakable", true);
        helmet.setTagCompound(compound);

        Items.LEATHER_HELMET.setColor(helmet, troop.getSettings().getColor());
        this.setItemStackToSlot(EntityEquipmentSlot.HEAD, helmet);
    }

    public void putOnFunkyShield()
    {
        NBTTagList patterns = new NBTTagList();
        int patternIterations = new Random().nextInt(4);
        for (int i = 0; i < patternIterations; i++) {
            BannerPattern pattern = BannerPattern.values()[new Random().nextInt(BannerPattern.values().length)];
            int color = new Random().nextInt(16);

            NBTTagCompound patternCompound = new NBTTagCompound();
            patternCompound.setString("Pattern", pattern.getHashname());
            patternCompound.setInteger("Color", color);
            patterns.appendTag(patternCompound);
        }

        int bannerColor = new Random().nextInt(16);
        ItemStack shield = new ItemStack(Items.SHIELD);
        NBTTagCompound shieldCompound = shield.getOrCreateSubCompound("BlockEntityTag");
        shieldCompound.setTag("Patterns", patterns);
        shieldCompound.setInteger("Base", bannerColor);

        this.setItemStackToSlot(EntityEquipmentSlot.OFFHAND, shield);
    }

    @Override
    protected void initEntityAI()
    {
        reformTask = new EntityAIReform(this, 30);
        behaviourTask = new EntityAIBehaviour(this);

        this.tasks.taskEntries.clear();
        this.targetTasks.taskEntries.clear();
        this.tasks.addTask(1, new EntityAISwimming(this));
        this.tasks.addTask(2, new EntityAIAttackMelee(this, ModConfigServer.BODYGUARD_NAVIGATION_SPEED, false));
        this.tasks.addTask(3, reformTask);
        this.tasks.addTask(4, new EntityAIOpenDoor(this, true));
        this.targetTasks.addTask(1, behaviourTask);

    }

    @Override
    protected void applyEntityAttributes()
    {
        super.applyEntityAttributes();
        EntityUtil.setEntityAttribute(this, SharedMonsterAttributes.ATTACK_DAMAGE,
                ModConfigServer.BODYGUARD_ATTACK_DAMAGE);
        EntityUtil.setEntityAttribute(this, SharedMonsterAttributes.ATTACK_SPEED,
                ModConfigServer.BODYGUARD_ATTACK_SPEED);
        EntityUtil.setEntityAttribute(this, SharedMonsterAttributes.KNOCKBACK_RESISTANCE,
                ModConfigServer.BODYGUARD_KNOCKBACK_RESISTANCE);
        EntityUtil.setEntityAttribute(this, SharedMonsterAttributes.MAX_HEALTH, ModConfigServer.BODYGUARD_MAX_HEALTH);
        EntityUtil.setEntityAttribute(this, SharedMonsterAttributes.MOVEMENT_SPEED,
                ModConfigServer.BODYGUARD_MOVEMENT_SPEED);
        EntityUtil.setEntityAttribute(this, SharedMonsterAttributes.FOLLOW_RANGE,
                ModConfigServer.BODYGUARD_FOLLOW_RANGE);
    }

    protected void updateAITasks()
    {
        dataManager.set(HEALTH, Float.valueOf(this.getHealth()));
        dataManager.set(MASTER_NAME, troop != null ? troop.getMasterName() : "");
    }

    protected void entityInit()
    {
        super.entityInit();
        dataManager.register(HEALTH, Float.valueOf(this.getHealth()));
        dataManager.register(MASTER_NAME, troop != null ? troop.getMasterName() : "");
    }

    // XXX: debugging
    @Override
    public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, @Nullable IEntityLivingData livingdata)
    {
        livingdata = super.onInitialSpawn(difficulty, livingdata);
        System.out.println("<<<spawn: " + this);
        return livingdata;
    }

    @Override
    public boolean getCanSpawnHere()
    {
        return super.getCanSpawnHere() && this.getEntityBoundingBox().minY >= 80;
    }

    @Override
    public void onEntityUpdate()
    {
        super.onEntityUpdate();

        if (dataManager.get(HEALTH) < this.getMaxHealth()) {
            timeSinceRegeneration++;

            // Regenerate hitpoints
            if (timeSinceRegeneration >= ModConfigServer.BODYGUARD_REGENERATION_TIME) {
                this.heal(1.0F);
                timeSinceRegeneration = 0;
            }
        } else {
            timeSinceRegeneration = 0;
        }
    }

    @Override
    public void onDeath(DamageSource cause)
    {
        super.onDeath(cause);

        // Update troop
        if (!world.isRemote && troop != null)
            troop.removeBodyguard(this);
    }

    @Override
    public Entity changeDimension(int dimensionIn)
    {
        // Allowing a single bodyguard to change dimension through portals is
        // undesirable
        return null;
    }

    @Override
    public boolean canDropLoot()
    {
        return true;
    }

    @Override
    public boolean canBeLeashedTo(EntityPlayer player)
    {
        return false;
    }

    @Override
    protected float getWaterSlowDown()
    {
        return 0.85F;
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound)
    {
        super.readEntityFromNBT(compound);
        if (compound.hasKey("Id", new NBTTagInt(0).getId()))
            this.setId(compound.getInteger("Id"));
        if (compound.hasKey("Master", new NBTTagString().getId())) {
            // Add bodyguard to troop
            this.setTroop(Troop.getTroop(compound.getString("Master")));
            troop.addBodyguard(this);
        }
        if (compound.hasKey("LastRegenerationTime", new NBTTagLong(0).getId()))
            timeSinceRegeneration = compound.getLong("TimeSinceRegeneration");
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound)
    {
        super.writeEntityToNBT(compound);
        if (troop != null) {
            compound.setInteger("Id", id);
            compound.setString("Master", troop.getMasterName());
            compound.setLong("TimeSinceRegeneration", timeSinceRegeneration);
        }
    }

    @Override
    public boolean attackEntityAsMob(Entity entityIn)
    {
        float attackDamage = (float) this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue();
        int knockbackModifier = 0;
        if (entityIn instanceof EntityLivingBase) {
            attackDamage += EnchantmentHelper.getModifierForCreature(this.getHeldItemMainhand(),
                    ((EntityLivingBase) entityIn).getCreatureAttribute());
            knockbackModifier += EnchantmentHelper.getKnockbackModifier(this);
        }

        boolean flag = entityIn.attackEntityFrom(DamageSource.causeMobDamage(this), attackDamage);
        if (flag) {
            if (knockbackModifier > 0 && entityIn instanceof EntityLivingBase) {
                ((EntityLivingBase) entityIn).knockBack(this, (float) knockbackModifier * 0.5F,
                        (double) MathHelper.sin(this.rotationYaw * 0.017453292F),
                        (double) (-MathHelper.cos(this.rotationYaw * 0.017453292F)));
                this.motionX *= 0.6D;
                this.motionZ *= 0.6D;
            }

            int fireModifier = EnchantmentHelper.getFireAspectModifier(this);
            if (fireModifier > 0)
                entityIn.setFire(fireModifier * 4);

            if (entityIn instanceof EntityPlayer) {
                EntityPlayer entityplayer = (EntityPlayer) entityIn;
                ItemStack mainHandItem = this.getHeldItemMainhand();
                ItemStack activeItem = entityplayer.isHandActive() ? entityplayer.getActiveItemStack()
                        : ItemStack.EMPTY;
                if (!mainHandItem.isEmpty() && !activeItem.isEmpty()
                        && mainHandItem.getItem().canDisableShield(mainHandItem, activeItem, entityplayer, this)
                        && activeItem.getItem().isShield(activeItem, entityplayer)) {
                    float f1 = 0.25F + (float) EnchantmentHelper.getEfficiencyModifier(this) * 0.05F;
                    if (this.rand.nextFloat() < f1) {
                        entityplayer.getCooldownTracker().setCooldown(activeItem.getItem(), 100);
                        this.world.setEntityState(entityplayer, (byte) 30);
                    }
                }
            }

            this.applyEnchantments(this, entityIn);
        }

        return flag;
    }

    @Override
    protected void playHurtSound(DamageSource source)
    {
        if (this.canBlockDamageSource(source))
            this.playSound(SoundEvents.ITEM_SHIELD_BLOCK, 1.0F, 0.8F + world.rand.nextFloat() * 0.4F);
        else
            super.playHurtSound(source);
    }

    @Override
    protected boolean canEquipItem(ItemStack stack)
    {
        // Prevent replacing helmet in case one is picked up
        return dataManager.get(MASTER_NAME) != "" || getSlotForItemStack(stack) != EntityEquipmentSlot.HEAD;
    }

    @Override
    protected void updateEquipmentIfNeeded(EntityItem itemEntity)
    {
        ItemStack stackOnFloor = itemEntity.getItem();
        Item itemOnFloor = stackOnFloor.getItem();
        ItemStack sameItemStack = this.getItemStackFromSlot(getSlotForItemStack(stackOnFloor));
        ItemStack stackToDrop = null;
        EntityEquipmentSlot targetSlot = null;

        // Process weapons (armor equipment is not considered a weapon)
        if (ItemUtil.isWeapon(itemOnFloor)) {
            ItemStack mainHand = this.getItemStackFromSlot(EntityEquipmentSlot.MAINHAND);
            ItemStack offHand = this.getItemStackFromSlot(EntityEquipmentSlot.OFFHAND);
            WeaponClass mainHandClass = ItemUtil.getWeaponClass(mainHand.getItem());
            WeaponClass offHandClass = ItemUtil.getWeaponClass(offHand.getItem());
            WeaponClass itemOnFloorClass = ItemUtil.getWeaponClass(itemOnFloor);
            double itemOnFloorScore = ItemUtil.getWeaponScore(this, stackOnFloor);
            double mainHandScore = ItemUtil.getWeaponScore(this, mainHand);
            double offHandScore = ItemUtil.getWeaponScore(this, offHand);

            if (mainHand.isEmpty()) {
                targetSlot = EntityEquipmentSlot.MAINHAND;
            } else if (offHand.isEmpty()) {
                targetSlot = EntityEquipmentSlot.OFFHAND;
            } else {
                // One hand is not a weapon: keep it (can be used by the player to make
                // bodyguards carry items)
                if (!ItemUtil.isWeapon(mainHand.getItem())) {
                    // Try to drop the other hand
                    if ((itemOnFloorClass == offHandClass && offHandScore < itemOnFloorScore)
                            || (itemOnFloorClass != WeaponClass.SHIELD && offHandClass == WeaponClass.SHIELD)) {
                        stackToDrop = offHand;
                        targetSlot = EntityEquipmentSlot.OFFHAND;
                    }
                } else if (!ItemUtil.isWeapon(offHand.getItem())) {
                    // Try to drop the other hand
                    if ((itemOnFloorClass == mainHandClass && mainHandScore < itemOnFloorScore)
                            || (itemOnFloorClass != WeaponClass.SHIELD && mainHandClass == WeaponClass.SHIELD)) {
                        stackToDrop = mainHand;
                        targetSlot = EntityEquipmentSlot.MAINHAND;
                    }
                }

                // Every hand is a weapon
                else {
                    // Both weapons in hands belong to the same class
                    if (mainHandClass == offHandClass) {
                        // Find the worst weapon in hands and try to replace it
                        if (offHandScore <= mainHandScore
                                && ((itemOnFloorClass == mainHandClass && offHandScore < itemOnFloorScore)
                                        || (itemOnFloorClass != mainHandClass))) {
                            stackToDrop = offHand;
                            targetSlot = EntityEquipmentSlot.OFFHAND;
                        } else if ((itemOnFloorClass == mainHandClass && mainHandScore < itemOnFloorScore)
                                || (itemOnFloorClass != mainHandClass)) {
                            stackToDrop = mainHand;
                            targetSlot = EntityEquipmentSlot.MAINHAND;
                        }
                    }

                    // Weapons in hands belong to different classes
                    else {
                        if (itemOnFloorClass == offHandClass && offHandScore < itemOnFloorScore) {
                            stackToDrop = offHand;
                            targetSlot = EntityEquipmentSlot.OFFHAND;
                        } else if (itemOnFloorClass == mainHandClass && mainHandScore < itemOnFloorScore) {
                            stackToDrop = mainHand;
                            targetSlot = EntityEquipmentSlot.MAINHAND;
                        }
                    }
                }
            }
        }

        // Process armor equipment
        else if (itemOnFloor instanceof ItemArmor) {
            if (!(sameItemStack.getItem() instanceof ItemArmor)) {
                targetSlot = getSlotForItemStack(stackOnFloor);
            } else if (!EnchantmentHelper.hasBindingCurse(sameItemStack)) {
                double itemOnFloorScore = ItemUtil.getArmorScore(this, stackOnFloor);
                double currentItemScore = ItemUtil.getArmorScore(this, sameItemStack);
                if (currentItemScore < itemOnFloorScore) {
                    stackToDrop = sameItemStack;
                    targetSlot = getSlotForItemStack(stackOnFloor);
                }
            }
        }

        if (targetSlot != null && this.canEquipItem(stackOnFloor) && !this.isContainerOpen()) {
            double dropChance;
            switch (targetSlot.getSlotType()) {
            case HAND:
                dropChance = (double) this.inventoryHandsDropChances[targetSlot.getIndex()];
                break;
            case ARMOR:
                dropChance = (double) this.inventoryArmorDropChances[targetSlot.getIndex()];
                break;
            default:
                dropChance = 2.0D;
            }

            // Drop item stack
            if (stackToDrop != null && !stackToDrop.isEmpty() && (double) this.rand.nextFloat() < dropChance)
                this.entityDropItem(stackToDrop, 0.0F);

            // Equip stack on floor
            this.setItemStackToSlot(targetSlot, stackOnFloor);
            this.onItemPickup(itemEntity, stackOnFloor.getCount());
            itemEntity.setDead();

            ItemUtil.swapHandsIfNeeded(this);
        }
    }

    public boolean canInteractWith(EntityPlayer playerIn)
    {
        return this.isEntityAlive() && dataManager.get(MASTER_NAME) == playerIn.getName();
    }

    @Override
    public boolean processInteract(EntityPlayer player, EnumHand hand)
    {
        ItemStack itemStack = player.getHeldItem(hand);
        if (itemStack.interactWithEntity(player, this, hand))
            return true;

        Item item = itemStack.getItem();
        if (!itemStack.isEmpty()) {
            if (dataManager.get(MASTER_NAME) == "" && item == Items.GOLD_INGOT) {
                // Give bodyguard to player
                if (!player.capabilities.isCreativeMode)
                    itemStack.shrink(1);

                this.give(Troop.getTroop(player.getName()));
                return true;
            } else if (item instanceof ItemFood && dataManager.get(HEALTH) < this.getMaxHealth()) {
                // Handle eating
                float regeneration = ((ItemFood) item).getHealAmount(null);
                this.heal(regeneration);

                if (!player.capabilities.isCreativeMode)
                    itemStack.shrink(1);

                return true;
            }
        }

        // Open panel
        EntityUtil.setInteractionTarget(this);
        if (this.canInteractWith(player)) {
            this.setContainerOpen(true);
            player.openGui(Mineguard.instance, GuiHandler.GUI_ENUM.BODYGUARD_PANEL.ordinal(), world, 0, 0, 0);
            return true;
        }

        return false;
    }

    public boolean isContainerOpen()
    {
        return containerOpen;
    }

    public void setContainerOpen(boolean containerOpen)
    {
        this.containerOpen = containerOpen;
    }

    public void give(Troop receivingTroop)
    {
        if (!world.isRemote) {
            this.setTroop(receivingTroop);
            this.putOnColorizedHelmet();
            // No need to change bg's master, this is done by writeToNBT()

            receivingTroop.addBodyguard(this);
        }
    }

    public void setShield(boolean enable)
    {
        ItemStack offHand = this.getItemStackFromSlot(EntityEquipmentSlot.OFFHAND);
        if (offHand.getItem().getItemUseAction(offHand) != EnumAction.BLOCK)
            return;

        if (enable)
            this.setActiveHand(EnumHand.OFF_HAND);
        else
            this.resetActiveHand();
    }

    public boolean isShieldActive()
    {
        return isActiveItemStackBlocking();
    }

    @Override
    public boolean isActiveItemStackBlocking()
    {
        // Override shield cooldown of 5 ticks
        if (this.isHandActive() && !this.activeItemStack.isEmpty()) {
            Item item = this.activeItemStack.getItem();
            return item.getItemUseAction(this.activeItemStack) == EnumAction.BLOCK;
        }
        return false;
    }

    @Override
    public String toString()
    {
        return super.toString() + " " + this.tasks.taskEntries.size() + " " + this.targetTasks.taskEntries.size() + " "
                + (troop == null ? "no_troop" : troop.getMasterName()) + " alive=" + this.isEntityAlive() + " uuid="
                + this.getUniqueID() + " hp=" + this.getHealth();
    }
}
