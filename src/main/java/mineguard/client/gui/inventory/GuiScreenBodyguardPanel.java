package mineguard.client.gui.inventory;

import mineguard.Mineguard;
import mineguard.entity.EntityBodyguard;
import mineguard.inventory.ContainerBodyguardInventory;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.inventory.IInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiScreenBodyguardPanel extends GuiContainer
{
    private EntityBodyguard bodyguard;
    private float oldMouseX;
    private float oldMouseY;

    public GuiScreenBodyguardPanel(IInventory playerInventory, EntityBodyguard bodyguard)
    {
        super(new ContainerBodyguardInventory(playerInventory, bodyguard));
        this.bodyguard = bodyguard;
        this.allowUserInput = false;
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
    {
        String nameTag = bodyguard.getCustomNameTag();
        int minX = 80;
        int maxX = 170;
        int maxLength = maxX - minX;

        // Shrink name tag
        int length = fontRenderer.getStringWidth(nameTag);
        if (length > maxLength) {
            while (length > maxLength) {
                nameTag = nameTag.substring(0, nameTag.length() - 1);
                length = fontRenderer.getStringWidth(nameTag + "...");
            }
            nameTag += "...";
        }

        // Center name tag horizontally
        fontRenderer.drawString(nameTag, minX + (maxLength - length) / 2, 8, 0x404040);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY)
    {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        mc.getTextureManager()
                .bindTexture(new ResourceLocation(Mineguard.MODID + ":textures/gui/container/bodyguard_inventory.png"));
        int marginHorizontal = (width - xSize) / 2;
        int marginVertical = (height - ySize) / 2;
        this.drawTexturedModalRect(marginHorizontal, marginVertical, 0, 0, xSize, ySize);

        // Draw entity preview
        GuiInventory.drawEntityOnScreen(guiLeft + 51, guiTop + 75, 30, (float) (guiLeft + 51) - oldMouseX,
                (float) (guiTop + 75 - 50) - oldMouseY, bodyguard);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        this.drawDefaultBackground();
        this.oldMouseX = (float) mouseX;
        this.oldMouseY = (float) mouseY;

        super.drawScreen(mouseX, mouseY, partialTicks);
        this.renderHoveredToolTip(mouseX, mouseY);
    }
}
