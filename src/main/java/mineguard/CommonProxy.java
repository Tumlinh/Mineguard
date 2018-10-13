package mineguard;

import java.io.File;
import mineguard.entity.EntityBodyguard;
import mineguard.handler.EntityEvents;
import mineguard.init.ModConfig;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.EntityRegistry;

public class CommonProxy
{
    public void preInit(FMLPreInitializationEvent event)
    {
        // Load configuration
        File configFile = new File(event.getModConfigurationDirectory(), "mineguard.cfg");
        new ModConfig(configFile);

        // Register entities
        EntityRegistry.registerModEntity(new ResourceLocation(Mineguard.MODID, "bodyguard"), EntityBodyguard.class,
                "bodyguard", 0, Mineguard.instance, 80, 3, false, 0, 0);

        // Register events
        MinecraftForge.EVENT_BUS.register(new EntityEvents());
    }
}
