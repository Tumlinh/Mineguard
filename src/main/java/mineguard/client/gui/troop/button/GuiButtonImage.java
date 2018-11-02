package mineguard.client.gui.troop.button;

import mineguard.Mineguard;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

public abstract class GuiButtonImage extends GuiButtonSetting
{
    private static final ResourceLocation TROOP_CONTROLLER_GUI_TEXTURES = new ResourceLocation(
            Mineguard.MODID + ":textures/gui/troop_controller.png");
    private static final int WIDTH = 20;
    private static final int HEIGHT = 18;
    private static final int TEXTURE_HOVERED_Y = 19;
    private static final int TEXTURE_SELECTED_Y = 38;

    private int textureX;
    private int textureY;
    private boolean selected;

    public GuiButtonImage(int buttonId, int x, int y, int textureX, int textureY, String hoveringText)
    {
        super(buttonId, x, y, WIDTH, HEIGHT, "", hoveringText);
        this.textureX = textureX;
        this.textureY = textureY;
    }

    public void setTexture(int textureX, int textureY)
    {
        this.textureX = textureX;
        this.textureY = textureY;
    }

    public boolean isSelected()
    {
        return selected;
    }

    public void setSelected(boolean selected)
    {
        this.selected = selected;
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks)
    {
        if (visible) {
            this.setHovered(mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height);
            mc.getTextureManager().bindTexture(TROOP_CONTROLLER_GUI_TEXTURES);
            GlStateManager.disableDepth();

            int i = textureY;
            if (this.isHovered())
                i += TEXTURE_HOVERED_Y;
            else if (this.isSelected())
                i += TEXTURE_SELECTED_Y;

            drawTexturedModalRect(x, y, textureX, i, width, height);
            GlStateManager.enableDepth();
        }
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY)
    {
        if (mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height)
            this.performAction();
    }

    @Override
    public void performAction()
    {
        this.setSelected(true);
        this.writeSetting();
    }
}
