package mineguard.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.UUID;
import mineguard.Troop;
import mineguard.entity.EntityBodyguard;
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
    private static EntityBodyguard interactionTarget;

    public static void setEntityAttribute(EntityLivingBase entity, IAttribute attribute, double value)
    {
        if (entity.getAttributeMap().getAttributeInstanceByName(attribute.getName()) == null)
            entity.getAttributeMap().registerAttribute(attribute);
        entity.getEntityAttribute(attribute).setBaseValue(value);
    }

    public static EntityBodyguard findBodyguard(UUID Uuid)
    {
        // XXX: Are we supposed to look into every server && world to find the
        // bodyguards?
        MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
        Entity entity = server.getEntityFromUuid(Uuid);
        if (entity != null && entity instanceof EntityBodyguard)
            return (EntityBodyguard) entity;
        return null;
    }

    public static Entity summonBodyguard(Troop troop, int bgIndex, World world, BlockPos pos)
    {
        Entity bodyguard = null;
        Constructor<?> constructor = ReflectionHelper.findConstructor(EntityBodyguard.class, World.class, int.class,
                troop.getClass());
        try {
            bodyguard = (Entity) constructor.newInstance(world, bgIndex, troop);
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                | InvocationTargetException e) {
            e.printStackTrace();
        }

        bodyguard.setLocationAndAngles(pos.getX(), pos.getY(), pos.getZ(), 0.0F, 0.0F);
        if (bodyguard instanceof EntityLiving)
            ((EntityLiving) bodyguard).onInitialSpawn(world.getDifficultyForLocation(new BlockPos(bodyguard)),
                    (IEntityLivingData) null);
        world.spawnEntity(bodyguard);

        return bodyguard;
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

    public static EntityBodyguard getInteractionTarget()
    {
        return interactionTarget;
    }

    public static void setInteractionTarget(EntityBodyguard interactionTargetIn)
    {
        interactionTarget = interactionTargetIn;
    }
}
