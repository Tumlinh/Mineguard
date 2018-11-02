package mineguard.client.gui.troop.button;

import mineguard.client.ClientProxy;

public class GuiButtonHalt extends GuiButtonImage
{
    public GuiButtonHalt(int buttonId, int x, int y, int textureX, int textureY, String hoveringText)
    {
        super(buttonId, x, y, textureX, textureY, hoveringText);
        this.readSetting();
    }

    public GuiButtonHalt(int buttonId, String hoveringText)
    {
        this(buttonId, 0, 0, 0, 0, hoveringText);
    }

    @Override
    public void performAction()
    {
        this.setSelected(!this.isSelected());
        this.writeSetting();
        super.writeSetting();
    }

    @Override
    public void readSetting()
    {
        this.setSelected(!ClientProxy.getSettings().isFollowing());
    }

    @Override
    public void writeSetting()
    {
        ClientProxy.getSettings().setFollow(!this.isSelected());
    }
}
