package mineguard.network;

import mineguard.Mineguard;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public class PacketHandler
{
    public static final SimpleNetworkWrapper INSTANCE = new SimpleNetworkWrapper(Mineguard.MODID);

    public static void init()
    {
        INSTANCE.registerMessage(MessageHandlerTroopSettingsC.class, MessageTroopSettings.class, 0, Side.CLIENT);
        INSTANCE.registerMessage(MessageHandlerTroopSettingsS.class, MessageTroopSettings.class, 1, Side.SERVER);
    }
}
