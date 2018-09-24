package mineguard.entity;

import java.util.IllegalFormatException;
import mineguard.Troop;
import mineguard.Troop.PositionNotFoundException;
import mineguard.entity.ai.EntityAIBehaviour;
import mineguard.entity.ai.EntityAIReform;
import mineguard.init.ModConfig;
import mineguard.util.EntityUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIAttackMelee;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.monster.EntityWitherSkeleton;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.pathfinding.PathNavigateGround;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;

public class EntityBodyguard extends EntityWitherSkeleton
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
    }

    // Called when spawning new bodyguards as part of a troop
    public EntityBodyguard(World worldIn, int id, Troop troop)
    {
        super(worldIn);
        this.id = id;
        this.troop = troop;
        if (troop != null)
            troop.addBodyguard(this);

        this.enablePersistence();

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

    public void reform()
    {
        if (troop != null)
            try {
                troop.reformBodyguard(troop.getBodyguardPos(this));
            } catch (PositionNotFoundException e) {
                e.printStackTrace();
            }
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
        this.tasks.addTask(1, new EntityAIAttackMelee(this, ModConfig.BODYGUARD_SPEED_TARGET, false));
        this.tasks.addTask(2, new EntityAIReform(this));
        this.tasks.addTask(3, new EntityAISwimming(this));
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
    public boolean isImmuneToExplosions()
    {
        return true;
    }

    @Override
    public boolean canDropLoot()
    {
        return false;
    }

    @Override
    public void playLivingSound()
    {
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound)
    {
        super.readEntityFromNBT(compound);
        if (compound.hasKey("Id", new NBTTagInt(0).getId())) {
            id = compound.getInteger("Id");
        }
        if (compound.hasKey("Master", new NBTTagString().getId())) {
            troop = Troop.getTroop(compound.getString("Master"));

            // Add bodyguard to troop
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
    public String toString()
    {
        return super.toString() + " " + this.tasks.taskEntries.size() + " " + this.targetTasks.taskEntries.size() + " "
                + (troop == null ? "no_troop" : troop.getMasterName()) + " alive=" + this.isEntityAlive() + " uuid="
                + this.getUniqueID();
    }
}
