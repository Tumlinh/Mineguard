package mineguard.util;

import java.util.Comparator;
import mineguard.Troop;
import mineguard.entity.EntityBodyguard;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;

public class AIUtil
{
    // True if entity belongs to the same troop
    public static boolean isFellow(Troop troop, Entity entity)
    {
        if (entity instanceof EntityBodyguard)
            return ((EntityBodyguard) entity).getTroop() == troop;
        return false;
    }

    public static boolean isHostile(Troop troop, Entity entity)
    {
        Entity target = null;
        if (entity instanceof EntityLiving) {
            target = ((EntityLiving) entity).getAttackTarget();
        } else {
            // TODO: check for EntityArrow, explosion?, etc. (any dangerous entity for the
            // master)
        }
        return target != null && (target == troop.getMaster() || isFellow(troop, target));
    }

    public static class DistanceSorter implements Comparator<Entity>
    {
        private final Entity master;
        private final Entity bodyguard;
        private final double weight = 0.2;

        public DistanceSorter(Entity master, Entity bodyguard)
        {
            this.master = master;
            this.bodyguard = bodyguard;
        }

        public int compare(Entity entity1, Entity entity2)
        {
            /*
             * Heuristics: priority depends on two elements: the distance between the entity
             * and the master, and the distance between the entity and the bodyguard
             */
            double d0 = master.getDistanceSq(entity1) + bodyguard.getDistanceSq(entity1) * weight;
            double d1 = master.getDistanceSq(entity2) + bodyguard.getDistanceSq(entity2) * weight;

            if (d0 < d1) {
                return -1;
            } else {
                return d0 > d1 ? 1 : 0;
            }
        }
    }
}