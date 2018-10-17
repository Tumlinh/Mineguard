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
        this.fontRenderer.drawString(bodyguard.getCustomNameTag(), 100, 8, 0x404040);
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
