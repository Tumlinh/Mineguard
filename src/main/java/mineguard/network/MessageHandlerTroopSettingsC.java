package mineguard.network;

import mineguard.client.ClientProxy;
import mineguard.network.MessageTroopSettings.Type;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessageHandlerTroopSettingsC implements IMessageHandler<MessageTroopSettings, IMessage>
{

    @Override
    public IMessage onMessage(MessageTroopSettings message, MessageContext ctx)
    {
        Minecraft.getMinecraft().addScheduledTask(new Runnable()
        {
            @Override
            public void run()
            {
                if (message.getType() == Type.DATA)
                    ClientProxy.setSettings(message.getSettings());
            }
        });

        return null;
    }

}
