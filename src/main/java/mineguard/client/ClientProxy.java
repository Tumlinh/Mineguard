package mineguard.client;

import java.io.File;
import mineguard.CommonProxy;
import mineguard.Mineguard;
import mineguard.client.renderer.entity.RenderGuard;
import mineguard.entity.EntityGuard;
import mineguard.handler.KeyboardEvents;
import mineguard.handler.TextureRegister;
import mineguard.init.ModConfigClient;
import mineguard.troop.settings.Settings;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.lwjgl.input.Keyboard;

public class ClientProxy extends CommonProxy
{
    public static KeyBinding TROOP_CONTROLLER_KEYBINDING = new KeyBinding("key.troopController", Keyboard.KEY_G,
            "key.categories.ui");
    private static Settings SETTINGS;
    public static ModConfigClient clientConfig;

    @Override
    public void preInit(FMLPreInitializationEvent event)
    {
        super.preInit(event);

        // Load configuration
        File configFile = new File(Mineguard.configDirectory, "client.cfg");
        clientConfig = new ModConfigClient(configFile);

        // Register events
        MinecraftForge.EVENT_BUS.register(clientConfig);
        MinecraftForge.EVENT_BUS.register(new TextureRegister());
        MinecraftForge.EVENT_BUS.register(new KeyboardEvents());

        // Register renderers
        RenderingRegistry.registerEntityRenderingHandler(EntityGuard.class, RenderGuard.RenderGuardFactory.INSTANCE);

        // Register key bindings
        ClientRegistry.registerKeyBinding(TROOP_CONTROLLER_KEYBINDING);
    }

    public static Settings getSettings()
    {
        return SETTINGS;
    }

    public static void setSettings(Settings settings)
    {
        SETTINGS = settings;
    }
}
