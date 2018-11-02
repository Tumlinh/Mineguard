package mineguard.client;

import mineguard.CommonProxy;
import mineguard.client.renderer.entity.RenderBodyguard;
import mineguard.entity.EntityBodyguard;
import mineguard.handler.KeyboardEvents;
import mineguard.handler.TextureRegister;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.lwjgl.input.Keyboard;

public class ClientProxy extends CommonProxy
{
    public static KeyBinding TROOP_CONTROLLER_KEYBINDING = new KeyBinding("key.troopController", Keyboard.KEY_G,
            "key.categories.ui"); // TODO: client config

    @Override
    public void preInit(FMLPreInitializationEvent event)
    {
        super.preInit(event);

        // Register events
        MinecraftForge.EVENT_BUS.register(new TextureRegister());
        MinecraftForge.EVENT_BUS.register(new KeyboardEvents());

        // Register renderers
        RenderingRegistry.registerEntityRenderingHandler(EntityBodyguard.class,
                RenderBodyguard.RenderBodyguardFactory.INSTANCE);

        // Register key bindings
        ClientRegistry.registerKeyBinding(TROOP_CONTROLLER_KEYBINDING);
    }
}
