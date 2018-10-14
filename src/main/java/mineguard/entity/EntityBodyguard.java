package mineguard.entity;

import java.util.IllegalFormatException;
import javax.annotation.Nullable;
import mineguard.Troop;
import mineguard.entity.ai.EntityAIBehaviour;
import mineguard.entity.ai.EntityAIReform;
import mineguard.init.ModConfig;
import mineguard.util.EntityUtil;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIAttackMelee;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.pathfinding.PathNavigateGround;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;

public class EntityBodyguard extends EntityCreature
{
    private int id;
    private Troop troop;

    // Called when spawning entities from NBT or hatching egg
    public EntityBodyguard(World worldIn)
    {
        // XXX: this constructor may cause unpredictable behaviours
        // TODO:
        // #1. Disable spawn from eggs
        // #2. Add bodyguard to player's troop (call other constructor)
        super(worldIn);
        this.setSize(0.6F, 1.8F);
        this.enablePersistence();
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

        ((PathNavigateGround) this.getNavigator()).setCanSwim(true);
    }

    public int getId()
    {
        return id;
    }

    public Troop getTroop()
    {
        return troop;
    }

    public void setTroop(Troop troop)
    {
        this.troop = troop;
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

    @Override
    protected void initEntityAI()
    {
        this.tasks.taskEntries.clear();
        this.targetTasks.taskEntries.clear();
        this.tasks.addTask(1, new EntityAISwimming(this));
        this.tasks.addTask(2, new EntityAIAttackMelee(this, ModConfig.BODYGUARD_SPEED_TARGET, false));
        this.tasks.addTask(3, new EntityAIReform(this, 30));
        this.targetTasks.addTask(1, new EntityAIBehaviour(this));
    }

    @Override
    protected void applyEntityAttributes()
    {
        super.applyEntityAttributes();
        EntityUtil.setEntityAttribute(this, SharedMonsterAttributes.ATTACK_DAMAGE, ModConfig.BODYGUARD_ATTACK_DAMAGE);
        EntityUtil.setEntityAttribute(this, SharedMonsterAttributes.ATTACK_SPEED, ModConfig.BODYGUARD_ATTACK_SPEED);
        EntityUtil.setEntityAttribute(this, SharedMonsterAttributes.KNOCKBACK_RESISTANCE,
                ModConfig.BODYGUARD_KNOCKBACK_RESISTANCE);
        EntityUtil.setEntityAttribute(this, SharedMonsterAttributes.MAX_HEALTH, ModConfig.BODYGUARD_MAX_HEALTH);
        EntityUtil.setEntityAttribute(this, SharedMonsterAttributes.MOVEMENT_SPEED, ModConfig.BODYGUARD_MOVEMENT_SPEED);
        EntityUtil.setEntityAttribute(this, SharedMonsterAttributes.FOLLOW_RANGE, ModConfig.BODYGUARD_FOLLOW_RANGE);
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
    public void onDeath(DamageSource cause)
    {
        super.onDeath(cause);

        // Update troop
        if (!world.isRemote)
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
        if (compound.hasKey("Id", new NBTTagInt(0).getId())) {
            id = compound.getInteger("Id");
        }
        if (compound.hasKey("Master", new NBTTagString().getId())) {
            // Add bodyguard to troop
            troop = Troop.getTroop(compound.getString("Master"));
            troop.addBodyguard(this);
        }
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound)
    {
        super.writeEntityToNBT(compound);
        if (troop != null) {
            compound.setInteger("Id", id);
            compound.setString("Master", troop.getMasterName());
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
    public String toString()
    {
        return super.toString() + " " + this.tasks.taskEntries.size() + " " + this.targetTasks.taskEntries.size() + " "
                + (troop == null ? "no_troop" : troop.getMasterName()) + " alive=" + this.isEntityAlive() + " uuid="
                + this.getUniqueID() + " armor_damages=" + " hp=" + this.getHealth();
    }
}
