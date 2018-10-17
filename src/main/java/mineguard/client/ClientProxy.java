package mineguard.client;

import mineguard.CommonProxy;
import mineguard.client.renderer.entity.RenderBodyguard;
import mineguard.entity.EntityBodyguard;
import mineguard.handler.TextureRegister;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.init.Biomes;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.EntityRegistry;

public class ClientProxy extends CommonProxy
{
    @Override
    public void preInit(FMLPreInitializationEvent event)
    {
        super.preInit(event);

        // Register events
        MinecraftForge.EVENT_BUS.register(new TextureRegister());

        // Register renderers
        RenderingRegistry.registerEntityRenderingHandler(EntityBodyguard.class,
                RenderBodyguard.RenderBodyguardFactory.INSTANCE);

        // Register spawns
        Biome[] biomes = { Biomes.BEACH, Biomes.BIRCH_FOREST, Biomes.BIRCH_FOREST_HILLS, Biomes.DESERT,
                Biomes.DESERT_HILLS, Biomes.EXTREME_HILLS, Biomes.FOREST, Biomes.FOREST_HILLS, Biomes.MESA,
                Biomes.PLAINS, Biomes.ROOFED_FOREST, Biomes.SAVANNA, Biomes.TAIGA, Biomes.TAIGA_HILLS };
        EntityRegistry.addSpawn(EntityBodyguard.class, 3, 1, 3, EnumCreatureType.CREATURE, biomes);
    }
}
