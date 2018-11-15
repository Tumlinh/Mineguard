package mineguard.init;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import mineguard.Mineguard;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.event.ConfigChangedEvent.OnConfigChangedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public abstract class ModConfig
{
    public Configuration config;

    public ModConfig(File configFile)
    {
        config = new Configuration(configFile);
        config.load();
        load();
        config.save();
    }

    protected abstract void load();

    public List<ConfigCategory> getCategories()
    {
        List<ConfigCategory> categories = new ArrayList<ConfigCategory>();
        for (String categoryName : config.getCategoryNames())
            categories.add(config.getCategory(categoryName));

        return categories;
    }

    @SubscribeEvent
    public void onConfigurationChangedEvent(OnConfigChangedEvent event)
    {
        if (event.getModID().equals(Mineguard.MODID)) {
            load();
            config.save();
        }
    }
}
