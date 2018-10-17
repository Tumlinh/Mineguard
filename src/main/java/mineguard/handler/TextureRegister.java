package mineguard.handler;

import mineguard.Mineguard;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class TextureRegister
{
    public static final String EMPTY_ARMOR_SLOT_MAINHAND_NAME = Mineguard.MODID + ":items/empty_armor_slot_mainhand";

    @SubscribeEvent
    public void onTextureStitchEventPre(TextureStitchEvent.Pre e)
    {
        TextureMap textureMap = e.getMap();
        textureMap.registerSprite(new ResourceLocation(EMPTY_ARMOR_SLOT_MAINHAND_NAME));
    }
}
