package mineguard.handler;

import mineguard.client.ClientProxy;
import mineguard.client.gui.troop.GuiTroopController;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent.KeyInputEvent;

public class KeyboardEvents
{
    @SubscribeEvent
    public void onEvent(KeyInputEvent event)
    {
        if (ClientProxy.TROOP_CONTROLLER_KEYBINDING.isPressed())
            Minecraft.getMinecraft().displayGuiScreen(new GuiTroopController());
    }
}
