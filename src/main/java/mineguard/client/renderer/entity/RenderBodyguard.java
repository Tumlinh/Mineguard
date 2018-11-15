package mineguard.client.renderer.entity;

import java.awt.Color;
import mineguard.Mineguard;
import mineguard.entity.EntityBodyguard;
import mineguard.init.ModConfigClient;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.layers.LayerArrow;
import net.minecraft.client.renderer.entity.layers.LayerBipedArmor;
import net.minecraft.client.renderer.entity.layers.LayerHeldItem;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHandSide;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.IRenderFactory;

public class RenderBodyguard extends RenderLiving<EntityBodyguard>
{
    private static final ResourceLocation BODYGUARD_TEXTURE = new ResourceLocation(Mineguard.MODID,
            "textures/entity/bodyguard.png");
    private static boolean isRenderingMiniature = false;

    public RenderBodyguard(RenderManager renderManager)
    {
        super(renderManager, new ModelBiped(0.0F, 0.0F, 64, 64), 0.5F);
        this.addLayer(new LayerBipedArmor(this));
        this.addLayer(new LayerArrow(this));
        this.addLayer(new LayerHeldItem(this));
    }

    public static void setRenderingMiniature(boolean rendering)
    {
        isRenderingMiniature = rendering;
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityBodyguard bodyguard)
    {
        return BODYGUARD_TEXTURE;
    }

    @Override
    public ModelBiped getMainModel()
    {
        return (ModelBiped) super.getMainModel();
    }

    @Override
    public void doRender(EntityBodyguard entity, double x, double y, double z, float entityYaw, float partialTicks)
    {
        ModelBiped modelBodyguard = this.getMainModel();
        ItemStack itemStackMainHand = entity.getHeldItemMainhand();
        ItemStack itemStackOffHand = entity.getHeldItemOffhand();
        modelBodyguard.setVisible(true);
        modelBodyguard.isSneak = entity.isSneaking();
        ModelBiped.ArmPose armPose1 = ModelBiped.ArmPose.EMPTY;
        ModelBiped.ArmPose armPose2 = ModelBiped.ArmPose.EMPTY;

        if (!itemStackMainHand.isEmpty()) {
            armPose1 = ModelBiped.ArmPose.ITEM;

            if (entity.getItemInUseCount() > 0) {
                EnumAction enumaction = itemStackMainHand.getItemUseAction();
                if (enumaction == EnumAction.BLOCK)
                    armPose1 = ModelBiped.ArmPose.BLOCK;
                else if (enumaction == EnumAction.BOW)
                    armPose1 = ModelBiped.ArmPose.BOW_AND_ARROW;
            }
        }

        if (!itemStackOffHand.isEmpty()) {
            armPose2 = ModelBiped.ArmPose.ITEM;

            if (entity.getItemInUseCount() > 0) {
                EnumAction enumaction1 = itemStackOffHand.getItemUseAction();
                if (enumaction1 == EnumAction.BLOCK)
                    armPose2 = ModelBiped.ArmPose.BLOCK;
                else if (enumaction1 == EnumAction.BOW)
                    armPose2 = ModelBiped.ArmPose.BOW_AND_ARROW;
            }
        }

        if (entity.getPrimaryHand() == EnumHandSide.RIGHT) {
            modelBodyguard.rightArmPose = armPose1;
            modelBodyguard.leftArmPose = armPose2;
        } else {
            modelBodyguard.rightArmPose = armPose2;
            modelBodyguard.leftArmPose = armPose1;
        }

        super.doRender(entity, x, y, z, entityYaw, partialTicks);
    }

    @Override
    protected void preRenderCallback(EntityBodyguard entityIn, float partialTickTime)
    {
        // Set the same render size than the player's (1.8 blocks high)
        GlStateManager.scale(0.9375F, 0.9375F, 0.9375F);
    }

    @Override
    protected void renderLivingLabel(EntityBodyguard entityIn, String str, double x, double y, double z,
            int maxDistance)
    {
        double d0 = entityIn.getDistanceSq(this.renderManager.renderViewEntity);
        if (d0 <= (double) (maxDistance * maxDistance)) {
            boolean isSneaking = entityIn.isSneaking();
            float viewerYaw = this.renderManager.playerViewY;
            float viewerPitch = this.renderManager.playerViewX;
            boolean isThirdPersonFrontal = this.renderManager.options.thirdPersonView == 2;
            float f = entityIn.height + 0.5F - (isSneaking ? 0.25F : 0.0F);

            int verticalShift = "deadmau5".equals(str) ? -10 : -0;
            if (this.canRenderHealthBar(entityIn))
                // Free up space for the health bar
                verticalShift -= 10;

            EntityRenderer.drawNameplate(this.getFontRendererFromRenderManager(), str, (float) x, (float) y + f,
                    (float) z, verticalShift, viewerYaw, viewerPitch, isThirdPersonFrontal, isSneaking);
        }
    }

    private void displayHealthBar(EntityBodyguard entityIn, String str, double x, double y, double z, int maxDistance)
    {
        double d0 = entityIn.getDistanceSq(this.renderManager.renderViewEntity);
        if (d0 <= (double) (maxDistance * maxDistance)) {
            boolean isSneaking = entityIn.isSneaking();
            float viewerYaw = this.renderManager.playerViewY;
            float viewerPitch = this.renderManager.playerViewX;
            boolean isThirdPersonFrontal = this.renderManager.options.thirdPersonView == 2;
            float f = entityIn.height + 0.5F - (isSneaking ? 0.25F : 0.0F);

            int barLength = 40;
            int greenLength = Math
                    .round(barLength * entityIn.getDataManager().get(EntityBodyguard.HEALTH) / entityIn.getMaxHealth());

            // Get troop color from helmet (disgusting but avoids sync)
            int barColor = Color.GREEN.getRGB();
            ItemStack helmet = entityIn.getItemStackFromSlot(EntityEquipmentSlot.HEAD);
            if (helmet != null && helmet.getItem() instanceof ItemArmor)
                barColor = ((ItemArmor) helmet.getItem()).getColor(helmet);

            // Force color opacity
            barColor = (0xff << 24) + (barColor & 0xffffff);

            if (entityIn.getDataManager().get(EntityBodyguard.MASTER_NAME) != "") {
                GlStateManager.pushMatrix();
                GlStateManager.translate(x, y + f, z);
                GlStateManager.glNormal3f(0.0F, 1.0F, 0.0F);
                GlStateManager.rotate(-viewerYaw, 0.0F, 1.0F, 0.0F);
                GlStateManager.rotate((float) (isThirdPersonFrontal ? -1 : 1) * viewerPitch, 1.0F, 0.0F, 0.0F);
                GlStateManager.scale(0.025, 0.025, 0.025);
                GlStateManager.translate(0, entityIn.height - 8, 0);
                GlStateManager.disableLighting();
                GlStateManager.disableDepth();
                Gui.drawRect(barLength / 2 - greenLength, 0, barLength / 2, barLength / 7, barColor);
                Gui.drawRect(-barLength / 2, 0, barLength / 2 - greenLength, barLength / 7, 0x40000000);
                GlStateManager.enableDepth();
                GlStateManager.enableLighting();
                GlStateManager.popMatrix();
            }
        }
    }

    @Override
    // Gross hook but works best
    public void renderName(EntityBodyguard entityIn, double x, double y, double z)
    {
        if (!isRenderingMiniature) {
            if (this.canRenderName(entityIn))
                this.renderLivingLabel(entityIn, entityIn.getDisplayName().getFormattedText(), x, y, z, 64);

            if (this.canRenderHealthBar(entityIn))
                this.displayHealthBar(entityIn, entityIn.getDisplayName().getFormattedText(), x, y, z, 64);
        }
    }

    @Override
    protected boolean canRenderName(EntityBodyguard entityIn)
    {
        return ModConfigClient.DISPLAY_NAME && super.canRenderName(entityIn);
    }

    private boolean canRenderHealthBar(EntityBodyguard entityIn)
    {
        boolean alwaysRender = ModConfigClient.DISPLAY_HEALTH_BAR;
        return alwaysRender || entityIn == this.renderManager.pointedEntity;
    }

    public static class RenderBodyguardFactory implements IRenderFactory<EntityBodyguard>
    {
        public static final RenderBodyguardFactory INSTANCE = new RenderBodyguardFactory();

        @Override
        public Render<? super EntityBodyguard> createRenderFor(RenderManager manager)
        {
            return new RenderBodyguard(manager);
        }
    }
}
