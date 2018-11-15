package mineguard.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.UUID;

import mineguard.entity.EntityGuard;
import mineguard.troop.Troop;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

public class EntityUtil
{
    // Dirty inter-class communication
    // Forge does not seem to allow passing an entity to the GuiHandler
    private static EntityGuard interactionTarget;

    public static void setEntityAttribute(EntityLivingBase entity, IAttribute attribute, double value)
    {
        if (entity.getAttributeMap().getAttributeInstanceByName(attribute.getName()) == null)
            entity.getAttributeMap().registerAttribute(attribute);
        entity.getEntityAttribute(attribute).setBaseValue(value);
    }

    public static EntityGuard findGuard(UUID Uuid)
    {
        // XXX: Are we supposed to look into every server && world to find the guards?
        MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
        Entity entity = server.getEntityFromUuid(Uuid);
        if (entity != null && entity instanceof EntityGuard)
            return (EntityGuard) entity;
        return null;
    }

    public static Entity summonGuard(Troop troop, int guardIndex, World world, BlockPos pos)
    {
        Entity guard = null;
        Constructor<?> constructor = ReflectionHelper.findConstructor(EntityGuard.class, World.class, int.class,
                troop.getClass());
        try {
            guard = (Entity) constructor.newInstance(world, guardIndex, troop);
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                | InvocationTargetException e) {
            e.printStackTrace();
        }

        guard.setLocationAndAngles(pos.getX(), pos.getY(), pos.getZ(), 0.0F, 0.0F);
        if (guard instanceof EntityLiving)
            ((EntityLiving) guard).onInitialSpawn(world.getDifficultyForLocation(new BlockPos(guard)),
                    (IEntityLivingData) null);
        world.spawnEntity(guard);

        return guard;
    }

    public static EntityPlayer getPlayerFromName(String name)
    {
        World[] worlds = null;
        MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
        if (server == null)
            worlds = new World[] { Minecraft.getMinecraft().world };
        else
            worlds = server.worlds;

        for (World world : worlds) {
            EntityPlayer player = world.getPlayerEntityByName(name);
            if (player != null)
                return player;
        }
        return null;
    }

    public static EntityGuard getInteractionTarget()
    {
        return interactionTarget;
    }

    public static void setInteractionTarget(EntityGuard interactionTargetIn)
    {
        interactionTarget = interactionTargetIn;
    }
}
