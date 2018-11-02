package mineguard.client.gui.troop.button;

import mineguard.settings.Settings;
import net.minecraft.client.gui.GuiButton;

public abstract class GuiButtonSetting extends GuiButton
{
    public static Settings settings;

    private boolean hovered;
    private String hoveringText;

    public GuiButtonSetting(int buttonId, int x, int y, int width, int height, String buttonText, String hoveringText)
    {
        super(buttonId, x, y, width, height, buttonText);
        this.hoveringText = hoveringText;
    }

    public boolean isHovered()
    {
        return hovered;
    }

    public void setHovered(boolean hovered)
    {
        this.hovered = hovered;
    }

    public String getHoveringText()
    {
        return hoveringText;
    }

    public void onKeyTyped(int keyCode)
    {
        this.performAction();
    }

    public abstract void performAction();

    public abstract void readSetting();

    public abstract void writeSetting();
}
