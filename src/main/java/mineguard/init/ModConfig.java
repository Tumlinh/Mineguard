package mineguard.init;

import java.io.File;
import net.minecraftforge.common.config.Configuration;

public abstract class ModConfig
{
    protected Configuration config;

    public ModConfig(File configFile)
    {
        config = new Configuration(configFile);
        config.load();
        load();
        config.save();
    }

    protected abstract void load();
}
