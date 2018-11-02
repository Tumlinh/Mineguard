package mineguard.client.gui.troop.button;

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
    }

    @Override
    public void readSetting()
    {
        this.setSelected(!settings.isFollowing());
    }

    @Override
    public void writeSetting()
    {
        settings.setFollow(!this.isSelected());
    }
}
