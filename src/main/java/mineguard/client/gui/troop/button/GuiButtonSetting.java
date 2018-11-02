package mineguard.client.gui.troop.button;

import mineguard.client.ClientProxy;
import mineguard.network.MessageTroopSettings;
import mineguard.network.PacketHandler;
import mineguard.network.MessageTroopSettings.Type;
import net.minecraft.client.gui.GuiButton;

public abstract class GuiButtonSetting extends GuiButton
{
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

    public void writeSetting()
    {
        // Send settings to server
        MessageTroopSettings message = new MessageTroopSettings(Type.DATA, ClientProxy.getSettings());
        PacketHandler.INSTANCE.sendToServer(message);
    }
}
