package mineguard.client.gui.troop.button;

import org.lwjgl.input.Keyboard;

import mineguard.client.ClientProxy;

public class GuiButtonSize extends GuiButtonRange
{
    public GuiButtonSize(int buttonId, int x, int y, int width, int height, String rangeName, int minValue,
            int maxValue)
    {
        super(buttonId, x, y, width, height, rangeName, minValue, maxValue);
        this.readSetting();
    }

    public GuiButtonSize(int buttonId, String rangeName, int minValue, int maxValue)
    {
        this(buttonId, 0, 0, 0, 0, rangeName, minValue, maxValue);
    }

    @Override
    public void onKeyTyped(int keyCode)
    {
        if (keyCode == Keyboard.KEY_ADD)
            sliderValue++;
        else if (keyCode == Keyboard.KEY_SUBTRACT)
            sliderValue--;

        super.onKeyTyped(keyCode);
    }

    @Override
    public void readSetting()
    {
        sliderValue = (int) ClientProxy.getSettings().getSize();
        this.updateSlider();
    }

    @Override
    public void writeSetting()
    {
        ClientProxy.getSettings().setSize(sliderValue);
    }
}
