package mineguard;

import java.io.File;
import mineguard.entity.EntityBodyguard;
import mineguard.handler.EntityEvents;
import mineguard.init.ModConfigServer;
import mineguard.network.PacketHandler;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.init.Biomes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.EntityRegistry;

public class CommonProxy
{
    public void preInit(FMLPreInitializationEvent event)
    {
        // Load configuration
        File configFile = new File(Mineguard.instance.configDirectory, "gameplay.cfg");
        new ModConfigServer(configFile);

        // Register entities
        EntityRegistry.registerModEntity(new ResourceLocation(Mineguard.MODID, "bodyguard"), EntityBodyguard.class,
                "bodyguard", 0, Mineguard.instance, 80, 3, false, 0, 0);

        // Register events
        MinecraftForge.EVENT_BUS.register(new EntityEvents());

        // Register spawns
        Biome[] biomes = { Biomes.BEACH, Biomes.BIRCH_FOREST, Biomes.BIRCH_FOREST_HILLS, Biomes.DESERT,
                Biomes.DESERT_HILLS, Biomes.EXTREME_HILLS, Biomes.FOREST, Biomes.FOREST_HILLS, Biomes.MESA,
                Biomes.PLAINS, Biomes.ROOFED_FOREST, Biomes.SAVANNA, Biomes.TAIGA, Biomes.TAIGA_HILLS };
        EntityRegistry.addSpawn(EntityBodyguard.class, 3, 1, 3, EnumCreatureType.CREATURE, biomes);

        // Register packets
        PacketHandler.init();
    }
}
