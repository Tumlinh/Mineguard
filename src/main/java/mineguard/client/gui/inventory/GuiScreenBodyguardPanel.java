package mineguard.client.gui.inventory;

import java.util.List;
import mineguard.client.renderer.entity.RenderBodyguard;
import mineguard.entity.EntityBodyguard;
import mineguard.handler.TextureRegister;
import mineguard.inventory.ContainerBodyguardInventory;
import mineguard.util.ItemUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderManager;
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

        // Render entity miniature, free of name tag and health bar
        renderMiniature(guiLeft + 51, guiTop + 75, 30, (float) (guiLeft + 51) - oldMouseX,
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

    private static void renderMiniature(int posX, int posY, int scale, float mouseX, float mouseY,
            EntityBodyguard entity)
    {
        GlStateManager.enableColorMaterial();
        GlStateManager.pushMatrix();
        GlStateManager.translate((float) posX, (float) posY, 50.0F);
        GlStateManager.scale((float) (-scale), (float) scale, (float) scale);
        GlStateManager.rotate(180.0F, 0.0F, 0.0F, 1.0F);
        float yawOffset = entity.renderYawOffset;
        float yaw = entity.rotationYaw;
        float pitch = entity.rotationPitch;
        float prevYawHead = entity.prevRotationYawHead;
        float yawHead = entity.rotationYawHead;
        GlStateManager.rotate(135.0F, 0.0F, 1.0F, 0.0F);
        RenderHelper.enableStandardItemLighting();
        GlStateManager.rotate(-135.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(-((float) Math.atan((double) (mouseY / 40.0F))) * 20.0F, 1.0F, 0.0F, 0.0F);
        entity.renderYawOffset = (float) Math.atan((double) (mouseX / 40.0F)) * 20.0F;
        entity.rotationYaw = (float) Math.atan((double) (mouseX / 40.0F)) * 40.0F;
        entity.rotationPitch = -((float) Math.atan((double) (mouseY / 40.0F))) * 20.0F;
        entity.rotationYawHead = entity.rotationYaw;
        entity.prevRotationYawHead = entity.rotationYaw;
        GlStateManager.translate(0.0F, 0.0F, 0.0F);
        RenderManager renderManager = Minecraft.getMinecraft().getRenderManager();
        renderManager.setPlayerViewY(180.0F);
        renderManager.setRenderShadow(false);
        RenderBodyguard.setRenderingMiniature(true);
        renderManager.renderEntity(entity, 0.0D, 0.0D, 0.0D, 0.0F, 1.0F, false);
        RenderBodyguard.setRenderingMiniature(false);
        renderManager.setRenderShadow(true);
        entity.renderYawOffset = yawOffset;
        entity.rotationYaw = yaw;
        entity.rotationPitch = pitch;
        entity.prevRotationYawHead = prevYawHead;
        entity.rotationYawHead = yawHead;
        GlStateManager.popMatrix();
        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableRescaleNormal();
        GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
        GlStateManager.disableTexture2D();
        GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
    }
}
