package mineguard.client.gui.troop.button;

import mineguard.client.ClientProxy;
import mineguard.troop.settings.Formation;

public class GuiButtonFormation extends GuiButtonImage
{
    private static GuiButtonImage lastSelectedButton;

    private Formation formation;

    public GuiButtonFormation(int buttonId, int x, int y, int textureX, int textureY, String hoveringText,
            Formation formation)
    {
        super(buttonId, x, y, textureX, textureY, hoveringText);
        this.formation = formation;
        this.readSetting();
    }

    public GuiButtonFormation(int buttonId, String hoveringText, Formation formation)
    {
        this(buttonId, 0, 0, 0, 0, hoveringText, formation);
    }

    @Override
    public void readSetting()
    {
        if (ClientProxy.getSettings().getFormation() == formation) {
            this.setSelected(true);
            lastSelectedButton = this;
        }
    }

    @Override
    public void writeSetting()
    {
        if (this.isSelected())
            ClientProxy.getSettings().setFormation(formation);

        // Unselect previous button
        if (lastSelectedButton != null && lastSelectedButton != this)
            lastSelectedButton.setSelected(false);
        lastSelectedButton = this;
    }
}
