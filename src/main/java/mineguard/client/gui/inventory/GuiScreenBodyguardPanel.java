package mineguard.client.gui.inventory;

import java.util.List;
import mineguard.entity.EntityBodyguard;
import mineguard.handler.TextureRegister;
import mineguard.inventory.ContainerBodyguardInventory;
import mineguard.util.ItemUtil;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
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
        mc.getTextureManager().bindTexture(new ResourceLocation(TextureRegister.BODYGUARD_INVENTORY));
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
        oldMouseX = (float) mouseX;
        oldMouseY = (float) mouseY;

        super.drawScreen(mouseX, mouseY, partialTicks);
        this.renderHoveredToolTip(mouseX, mouseY);
    }

    @Override
    public List<String> getItemToolTip(ItemStack itemStack)
    {
        List<String> list = ItemUtil.getTooltip(itemStack, bodyguard, this.mc.player,
                this.mc.gameSettings.advancedItemTooltips ? ITooltipFlag.TooltipFlags.ADVANCED
                        : ITooltipFlag.TooltipFlags.NORMAL);

        for (int i = 0; i < list.size(); i++) {
            if (i == 0)
                list.set(i, itemStack.getRarity().rarityColor + (String) list.get(i));
            else
                list.set(i, TextFormatting.GRAY + (String) list.get(i));
        }

        return list;
    }
}
