package mineguard.client.renderer.entity;

import mineguard.Mineguard;
import mineguard.entity.EntityBodyguard;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.layers.LayerArrow;
import net.minecraft.client.renderer.entity.layers.LayerBipedArmor;
import net.minecraft.client.renderer.entity.layers.LayerHeldItem;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.IRenderFactory;

public class RenderBodyguard extends RenderLiving<EntityBodyguard>
{
    private static final ResourceLocation BODYGUARD_TEXTURE = new ResourceLocation(Mineguard.MODID,
            "textures/entity/bodyguard.png");

    public RenderBodyguard(RenderManager renderManager)
    {
        super(renderManager, new ModelBiped(0.0F, 0.0F, 64, 64), 0.5F);
        this.addLayer(new LayerBipedArmor(this));
        this.addLayer(new LayerArrow(this));
        this.addLayer(new LayerHeldItem(this));
    }

    /**
     * Returns the location of an entity's texture. Doesn't seem to be called unless
     * you call Render.bindEntityTexture.
     */
    protected ResourceLocation getEntityTexture(EntityBodyguard bodyguard)
    {
        return BODYGUARD_TEXTURE;
    }

    /**
     * Allows the render to do state modifications necessary before the model is
     * rendered.
     */
    protected void preRenderCallback(EntityBodyguard entity, float partialTickTime)
    {
        // Set the same render size than the player's (1.8 blocks high)
        GlStateManager.scale(0.9375F, 0.9375F, 0.9375F);
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
