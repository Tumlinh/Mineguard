package mineguard.client.renderer.entity;

import java.awt.Color;
import mineguard.Mineguard;
import mineguard.entity.EntityGuard;
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

public class RenderGuard extends RenderLiving<EntityGuard>
{
    private static final ResourceLocation GUARD_TEXTURE = new ResourceLocation(Mineguard.MODID,
            "textures/entity/guard.png");
    private static boolean isRenderingMiniature = false;

    public RenderGuard(RenderManager renderManager)
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
    protected ResourceLocation getEntityTexture(EntityGuard guard)
    {
        return GUARD_TEXTURE;
    }

    @Override
    public ModelBiped getMainModel()
    {
        return (ModelBiped) super.getMainModel();
    }

    @Override
    public void doRender(EntityGuard entity, double x, double y, double z, float entityYaw, float partialTicks)
    {
        ModelBiped modelGuard = this.getMainModel();
        ItemStack itemStackMainHand = entity.getHeldItemMainhand();
        ItemStack itemStackOffHand = entity.getHeldItemOffhand();
        modelGuard.setVisible(true);
        modelGuard.isSneak = entity.isSneaking();
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
            modelGuard.rightArmPose = armPose1;
            modelGuard.leftArmPose = armPose2;
        } else {
            modelGuard.rightArmPose = armPose2;
            modelGuard.leftArmPose = armPose1;
        }

        super.doRender(entity, x, y, z, entityYaw, partialTicks);
    }

    @Override
    protected void preRenderCallback(EntityGuard entityIn, float partialTickTime)
    {
        // Set the same render size than the player's (1.8 blocks high)
        GlStateManager.scale(0.9375F, 0.9375F, 0.9375F);
    }

    @Override
    protected void renderLivingLabel(EntityGuard entityIn, String str, double x, double y, double z, int maxDistance)
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

    private void displayHealthBar(EntityGuard entityIn, String str, double x, double y, double z, int maxDistance)
    {
        double d0 = entityIn.getDistanceSq(this.renderManager.renderViewEntity);
        if (d0 <= (double) (maxDistance * maxDistance)) {
            boolean isSneaking = entityIn.isSneaking();
            float viewerYaw = this.renderManager.playerViewY;
            float viewerPitch = this.renderManager.playerViewX;
            boolean isThirdPersonFrontal = this.renderManager.options.thirdPersonView == 2;
            float f = entityIn.height + 0.5F - (isSneaking ? 0.25F : 0.0F);

            int barLength = 40;
            int hpLength = Math
                    .round(barLength * entityIn.getDataManager().get(EntityGuard.HEALTH) / entityIn.getMaxHealth());

            // Get troop color from helmet (disgusting but avoids sync)
            int barColor = Color.GREEN.getRGB();
            ItemStack helmet = entityIn.getItemStackFromSlot(EntityEquipmentSlot.HEAD);
            if (helmet != null && helmet.getItem() instanceof ItemArmor)
                barColor = ((ItemArmor) helmet.getItem()).getColor(helmet);

            // Force color opacity
            barColor = (0xff << 24) + (barColor & 0xffffff);

            if (!entityIn.getDataManager().get(EntityGuard.MASTER_NAME).isEmpty()) {
                GlStateManager.pushMatrix();
                GlStateManager.translate(x, y + f, z);
                GlStateManager.glNormal3f(0.0F, 1.0F, 0.0F);
                GlStateManager.rotate(-viewerYaw, 0.0F, 1.0F, 0.0F);
                GlStateManager.rotate((float) (isThirdPersonFrontal ? -1 : 1) * viewerPitch, 1.0F, 0.0F, 0.0F);
                GlStateManager.scale(0.025, 0.025, 0.025);
                GlStateManager.translate(0, entityIn.height - 8, 0);
                GlStateManager.disableLighting();
                GlStateManager.disableDepth();
                Gui.drawRect(barLength / 2 - hpLength, 0, barLength / 2, barLength / 7, barColor);
                Gui.drawRect(-barLength / 2, 0, barLength / 2 - hpLength, barLength / 7, 0x40000000);
                GlStateManager.enableDepth();
                GlStateManager.enableLighting();
                GlStateManager.popMatrix();
            }
        }
    }

    @Override
    // Gross hook but works best
    public void renderName(EntityGuard entityIn, double x, double y, double z)
    {
        if (!isRenderingMiniature) {
            if (this.canRenderName(entityIn))
                this.renderLivingLabel(entityIn, entityIn.getDisplayName().getFormattedText(), x, y, z, 64);

            if (this.canRenderHealthBar(entityIn))
                this.displayHealthBar(entityIn, entityIn.getDisplayName().getFormattedText(), x, y, z, 64);
        }
    }

    @Override
    protected boolean canRenderName(EntityGuard entityIn)
    {
        return ModConfigClient.DISPLAY_NAME && super.canRenderName(entityIn);
    }

    private boolean canRenderHealthBar(EntityGuard entityIn)
    {
        boolean alwaysRender = ModConfigClient.DISPLAY_HEALTH_BAR;
        return alwaysRender || entityIn == this.renderManager.pointedEntity;
    }

    public static class RenderGuardFactory implements IRenderFactory<EntityGuard>
    {
        public static final RenderGuardFactory INSTANCE = new RenderGuardFactory();

        @Override
        public Render<? super EntityGuard> createRenderFor(RenderManager manager)
        {
            return new RenderGuard(manager);
        }
    }
}
