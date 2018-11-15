package mineguard.init;

import java.io.File;
import net.minecraftforge.common.config.Configuration;

public class ModConfigClient extends ModConfig
{
    public static int TROOP_CONTROLLER_ALPHA;
    public static boolean DISPLAY_HEALTH_BAR;
    public static boolean DISPLAY_NAME;

    public ModConfigClient(File configFile)
    {
        super(configFile);
    }

    @Override
    protected void load()
    {
        TROOP_CONTROLLER_ALPHA = config.getInt("TroopControllerAlpha", Configuration.CATEGORY_GENERAL, 0x50, 0, 255,
                "");
        DISPLAY_HEALTH_BAR = config.getBoolean("DisplayHealthBar", Configuration.CATEGORY_GENERAL, true, "");
        DISPLAY_NAME = config.getBoolean("DisplayName", Configuration.CATEGORY_GENERAL, true, "");
    }
}
