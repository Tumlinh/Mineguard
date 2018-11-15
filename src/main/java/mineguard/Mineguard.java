package mineguard;

import java.io.File;
import mineguard.client.gui.GuiHandler;
import mineguard.command.CommandMineguard;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;

@Mod(modid = Mineguard.MODID, name = Mineguard.NAME, version = Mineguard.VERSION, acceptableRemoteVersions = "*", guiFactory = Mineguard.GUI_FACTORY)

public class Mineguard
{
    public static final String MODID = "mineguard";
    public static final String NAME = "Mineguard";
    public static final String VERSION = "@MOD_VERSION@";
    public static final String GUI_FACTORY = "mineguard.client.gui.GuiFactory";

    @Instance(MODID)
    public static Mineguard instance;

    @SidedProxy(clientSide = "mineguard.client.ClientProxy", serverSide = "mineguard.CommonProxy")
    public static CommonProxy proxy;

    public File configDirectory;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        configDirectory = new File(event.getModConfigurationDirectory(), Mineguard.MODID);
        proxy.preInit(event);
    }

    @EventHandler
    public void init(FMLInitializationEvent event)
    {
        // Register GUI handlers
        NetworkRegistry.INSTANCE.registerGuiHandler(instance, new GuiHandler());
    }

    @EventHandler
    public void serverStarting(FMLServerStartingEvent event)
    {
        event.registerServerCommand(new CommandMineguard());
    }
}
