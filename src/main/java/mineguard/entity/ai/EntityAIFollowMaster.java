package mineguard.entity.ai;

import mineguard.Troop;
import mineguard.entity.EntityBodyguard;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.player.EntityPlayer;

public class EntityAIFollowMaster extends EntityAIBase
{
    private EntityBodyguard bg;
    private Troop troop;
    private double lastPosX, lastPosY, lastPosZ;
    // TODO: move distanceThreshold to configuration
    // XXX: distanceThreshold should be smaller than "formation size"
    private double distanceThreshold = 3.0;

    public EntityAIFollowMaster(EntityBodyguard bg)
    {
        this.bg = bg;
        troop = bg.getTroop();
        if (troop != null) {
            EntityPlayer master = troop.getMaster();
            if (master != null) {
                this.lastPosX = master.posX;
                this.lastPosY = master.posY;
                this.lastPosZ = master.posZ;
            }
        }
        this.setMutexBits(3);
    }

    @Override
    public boolean shouldExecute()
    {
        troop = bg.getTroop();
        if (troop == null || !troop.getSettings().isFollowing() || troop.getMaster() == null)
            return false;
        else {
            EntityPlayer master = troop.getMaster();
            double deltaDist = master.getDistance(lastPosX, lastPosY, lastPosZ);
            if (deltaDist >= distanceThreshold) {
                lastPosX = master.posX;
                lastPosY = master.posY;
                lastPosZ = master.posZ;
                return true;
            } else {
                return false;
            }
        }
    }

    @Override
    public void startExecuting()
    {
        // Reform bodyguard
        bg.reform();
    }
}
