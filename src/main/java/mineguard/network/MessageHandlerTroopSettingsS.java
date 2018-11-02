package mineguard.network;

import mineguard.Troop;
import mineguard.network.MessageTroopSettings.Type;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessageHandlerTroopSettingsS implements IMessageHandler<MessageTroopSettings, IMessage>
{

    @Override
    public IMessage onMessage(MessageTroopSettings message, MessageContext ctx)
    {
        FMLCommonHandler.instance().getMinecraftServerInstance().addScheduledTask(new Runnable()
        {
            @Override
            public void run()
            {
                EntityPlayerMP sender = ctx.getServerHandler().player;
                Troop senderTroop = Troop.getTroop(sender.getName());

                if (message.getType() == Type.DATA) {
                    senderTroop.getSettings().setSettings(message.getSettings());
                } else if (message.getType() == Type.REQ) {
                    MessageTroopSettings message = new MessageTroopSettings(Type.DATA, senderTroop.getSettings());
                    PacketHandler.INSTANCE.sendTo(message, sender);
                }
            }
        });

        return null;
    }
}
