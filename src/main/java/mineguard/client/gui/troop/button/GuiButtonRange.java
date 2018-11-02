package mineguard.client.gui.troop.button;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.math.MathHelper;

public abstract class GuiButtonRange extends GuiButtonSetting
{
    public boolean dragging;
    private String rangeName;
    protected int sliderValue;
    protected final int minValue;
    protected final int maxValue;

    public GuiButtonRange(int buttonId, int x, int y, int width, int height, String rangeName, int minValue,
            int maxValue)
    {
        super(buttonId, x, y, width, height, "", "");
        this.rangeName = rangeName;
        this.minValue = minValue;
        this.maxValue = maxValue;
    }

    @Override
    protected int getHoverState(boolean mouseOver)
    {
        return 0;
    }

    protected void updateSlider()
    {
        sliderValue = MathHelper.clamp(sliderValue, minValue, maxValue);
        displayString = rangeName + ": " + sliderValue;
    }

    @Override
    protected void mouseDragged(Minecraft mc, int mouseX, int mouseY)
    {
        if (visible) {
            if (dragging) {
                sliderValue = (mouseX - (x + 4)) * maxValue / (width - 8);
                this.performAction();
            }
            mc.getTextureManager().bindTexture(BUTTON_TEXTURES);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            drawTexturedModalRect(x + (int) (sliderValue * (float) (width - 8)) / maxValue, y, 0, 66, 4, 20);
            drawTexturedModalRect(x + (int) (sliderValue * (float) (width - 8)) / maxValue + 4, y, 196, 66, 4, 20);
        }
    }

    @Override
    public boolean mousePressed(Minecraft mc, int mouseX, int mouseY)
    {
        if (super.mousePressed(mc, mouseX, mouseY)) {
            sliderValue = (mouseX - (x + 4)) * maxValue / (width - 8);
            this.performAction();
            dragging = true;
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY)
    {
        dragging = false;
        this.performAction();
    }

    @Override
    public void performAction()
    {
        this.updateSlider();
        this.writeSetting();
        super.writeSetting();
    }
}
