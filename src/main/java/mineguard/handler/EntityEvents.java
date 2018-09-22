package mineguard.handler;

import mineguard.Troop;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;

public class EntityEvents
{
    @SubscribeEvent
    public void onEntityJoinWorld(EntityJoinWorldEvent event)
    {
        if (event.getEntity() instanceof EntityPlayerMP) {
            // Associate player
            EntityPlayerMP player = (EntityPlayerMP) event.getEntity();
            Troop troop = Troop.getTroop(player.getName());
            troop.setMaster(player.getName());
            troop.reform();
        }
    }

    // TODO: remove dead bodyguards from troop
}
