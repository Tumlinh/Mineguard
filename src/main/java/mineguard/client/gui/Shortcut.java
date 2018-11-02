package mineguard.client.gui;

import mineguard.client.gui.troop.button.GuiButtonSetting;

public class Shortcut
{
    public GuiButtonSetting button;
    public boolean closeWindow;

    public Shortcut(GuiButtonSetting button, boolean closeWindow)
    {
        this.button = button;
        this.closeWindow = closeWindow;
    }
}
