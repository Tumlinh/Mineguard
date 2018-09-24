package mineguard.handler;

import mineguard.Troop;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.Vec3i;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.living.LivingEvent;

public class EntityEvents
{
    @SubscribeEvent
    public void onPlayerJoinWorld(EntityJoinWorldEvent event)
    {
        if (event.getEntity() instanceof EntityPlayer) {
            // Associate player
            EntityPlayer player = (EntityPlayer) event.getEntity();
            Troop troop = Troop.getTroop(player.getName());
            troop.setMaster(player.getName());
        }
    }

    @SubscribeEvent
    public void onPlayerEvent(LivingEvent event)
    {
        if (event.getEntity() instanceof EntityPlayer) {
            EntityPlayer master = (EntityPlayer) event.getEntity();
            Troop troop = Troop.getTroop(master.getName());

            if (troop.getSettings().isFollowing())
                // Update center position
                troop.getSettings().setCenter(new Vec3i(master.posX, master.posY, master.posZ));
        }
    }

    @SubscribeEvent
    public void onPlayerSave(PlayerEvent.SaveToFile event)
    {
        // Save settings
        // TODO: ideally only save "center" and only if player has bodyguards
        Troop.getTroop(event.getEntityPlayer().getName()).getSettings().writeToNBT();
    }
}
