package mineguard.client.gui.troop.button;

import mineguard.client.ClientProxy;
import mineguard.settings.Behaviour;

public class GuiButtonBehaviour extends GuiButtonImage
{
    private static GuiButtonImage lastSelectedButton;

    private Behaviour behaviour;

    public GuiButtonBehaviour(int buttonId, int x, int y, int textureX, int textureY, String hoveringText,
            Behaviour behaviour)
    {
        super(buttonId, x, y, textureX, textureY, hoveringText);
        this.behaviour = behaviour;
        this.readSetting();
    }

    public GuiButtonBehaviour(int buttonId, String hoveringText, Behaviour behaviour)
    {
        this(buttonId, 0, 0, 0, 0, hoveringText, behaviour);
    }

    @Override
    public void readSetting()
    {
        if (ClientProxy.getSettings().getBehaviour() == behaviour) {
            this.setSelected(true);
            lastSelectedButton = this;
        }
    }

    @Override
    public void writeSetting()
    {
        if (this.isSelected())
            ClientProxy.getSettings().setBehaviour(behaviour);

        // Unselect previous button
        if (lastSelectedButton != null && lastSelectedButton != this)
            lastSelectedButton.setSelected(false);
        lastSelectedButton = this;
    }
}
