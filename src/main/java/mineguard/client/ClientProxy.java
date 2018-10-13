package mineguard.client;

import mineguard.CommonProxy;
import mineguard.client.renderer.entity.RenderBodyguard;
import mineguard.entity.EntityBodyguard;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class ClientProxy extends CommonProxy
{
    @Override
    public void preInit(FMLPreInitializationEvent event)
    {
        super.preInit(event);

        // Register renderers
        RenderingRegistry.registerEntityRenderingHandler(EntityBodyguard.class,
                RenderBodyguard.RenderBodyguardFactory.INSTANCE);
    }
}
