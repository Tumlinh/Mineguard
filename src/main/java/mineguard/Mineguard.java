package mineguard;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;

@Mod(modid = Mineguard.MODID, name = Mineguard.NAME, version = Mineguard.VERSION, acceptableRemoteVersions = "*")

public class Mineguard
{
    public static final String MODID = "mineguard";
    public static final String NAME = "Mineguard";
    public static final String VERSION = "@MOD_VERSION@";

    @Instance(MODID)
    public static Mineguard instance;

    @SidedProxy(clientSide = "mineguard.client.ClientProxy", serverSide = "mineguard.CommonProxy")
    public static CommonProxy proxy;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        proxy.preInit(event);
    }

    @EventHandler
    public void serverStarting(FMLServerStartingEvent event)
    {
        event.registerServerCommand(new CommandBodyguard());
    }
}
