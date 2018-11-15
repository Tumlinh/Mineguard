package mineguard.client.gui;

import java.util.ArrayList;
import java.util.List;
import mineguard.Mineguard;
import mineguard.client.ClientProxy;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.config.DummyConfigElement;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.fml.client.config.IConfigElement;
import org.apache.commons.lang3.text.WordUtils;

public class GuiConfigClient extends GuiConfig
{
    public GuiConfigClient(GuiScreen parentScreen)
    {
        super(parentScreen, GuiConfigClient.getConfigElements(), Mineguard.MODID, false, false, "Mineguard");
    }

    private static List<IConfigElement> getConfigElements()
    {
        List<IConfigElement> list = new ArrayList<IConfigElement>();
        Configuration config = ClientProxy.clientConfig.config;

        for (String categoryName : config.getCategoryNames()) {
            ConfigCategory category = config.getCategory(categoryName);
            List<IConfigElement> elements = new ConfigElement(category).getChildElements();
            list.add(new DummyConfigElement.DummyCategoryElement(WordUtils.capitalize(categoryName), "", elements));
        }

        return list;
    }
}
