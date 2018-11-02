package mineguard.handler;

import mineguard.Troop;
import mineguard.client.ClientProxy;
import mineguard.network.MessageTroopSettings;
import mineguard.network.PacketHandler;
import mineguard.network.MessageTroopSettings.Type;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.Vec3d;
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

            if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT) {
                // Fetch settings from server
                if (ClientProxy.getSettings() == null) {
                    MessageTroopSettings message = new MessageTroopSettings(Type.REQ, null);
                    PacketHandler.INSTANCE.sendToServer(message);
                }
            }
        }
    }

    @SubscribeEvent
    public void onPlayerEvent(LivingEvent event)
    {
        if (event.getEntity() instanceof EntityPlayer) {
            EntityPlayer master = (EntityPlayer) event.getEntity();
            Troop troop = Troop.getTroop(master.getName());

            if (troop.getSettings().isFollowing() && master.dimension == troop.getSettings().getDimension())
                // Update troop center
                troop.getSettings().setCenter(new Vec3d(master.posX, master.posY, master.posZ));
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
