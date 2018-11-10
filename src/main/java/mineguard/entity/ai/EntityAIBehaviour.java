package mineguard.entity.ai;

import com.google.common.base.Predicate;
import java.util.List;
import javax.annotation.Nullable;
import mineguard.Troop;
import mineguard.entity.EntityBodyguard;
import mineguard.settings.Behaviour;
import mineguard.util.AIUtil;
import mineguard.util.ItemUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;

public class EntityAIBehaviour extends EntityAIBase
{
    private EntityBodyguard bg;
    private Troop troop;

    private static double boxSize = 20.0;

    public EntityAIBehaviour(EntityBodyguard bg)
    {
        this.bg = bg;
        troop = bg.getTroop();
        this.setMutexBits(3);
    }

    @Override
    public boolean shouldExecute()
    {
        troop = bg.getTroop();
        return troop != null;
    }

    @Override
    public void updateTask()
    {
        if (bg.getAttackTarget() != null && !bg.getAttackTarget().isEntityAlive())
            bg.setAttackTarget((EntityLivingBase) null);

        // Aggressive mode: if no nearby hostile targets, attack any nearby target
        // sorted by priority
        if (troop.getSettings().getBehaviour() == Behaviour.AGGRESSIVE) {
            List<Entity> nearbyHostileTargets = this.getNearbyHostileTargets();

            // Sort entities by dangerousness (cf. heuristics)
            nearbyHostileTargets.sort(new AIUtil.DistanceSorter(troop.getMaster(), bg));

            if (!nearbyHostileTargets.isEmpty()) {
                attackEntity(nearbyHostileTargets.get(0), bg);
            } else {
                List<Entity> nearbyTargets = this.getNearbyTargets();
                nearbyTargets.sort(new AIUtil.DistanceSorter(troop.getMaster(), bg));

                if (!nearbyTargets.isEmpty())
                    attackEntity(nearbyTargets.get(0), bg);
            }
        }

        // Berserker mode: attack any nearby target except master
        else if (troop.getSettings().getBehaviour() == Behaviour.BERSERKER) {
            List<Entity> nearbyTargets = this.getNearbyTargets();
            nearbyTargets.sort(new AIUtil.DistanceSorter((Entity) null, bg));

            if (!nearbyTargets.isEmpty())
                attackEntity(nearbyTargets.get(0), bg);
        }

        // Defensive mode: attack nearby hostile targets sorted by priority
        else if (troop.getSettings().getBehaviour() == Behaviour.DEFENSIVE) {
            List<Entity> nearbyHostileTargets = this.getNearbyHostileTargets();
            nearbyHostileTargets.sort(new AIUtil.DistanceSorter(troop.getMaster(), bg));

            if (!nearbyHostileTargets.isEmpty())
                attackEntity(nearbyHostileTargets.get(0), bg);
        }

        else if (troop.getSettings().getBehaviour() == Behaviour.STILL) {
            bg.setAttackTarget((EntityLivingBase) null);
        }
    }

    private List<Entity> getNearbyHostileTargets()
    {
        List<Entity> nearbyTargets = this.getNearbyEntities(new Predicate<Entity>()
        {
            public boolean apply(@Nullable Entity entity)
            {
                return AIUtil.isHostile(troop, entity);
            }
        });
        return nearbyTargets;
    }

    private List<Entity> getNearbyTargets()
    {
        List<Entity> nearbyTargets = this.getNearbyEntities(new Predicate<Entity>()
        {
            public boolean apply(@Nullable Entity entity)
            {
                return (entity instanceof EntityLivingBase) && !(entity instanceof EntityPlayer)
                        && !AIUtil.isFellow(troop, entity);
            }
        });
        return nearbyTargets;
    }

    private List<Entity> getNearbyEntities(Predicate<Entity> predicate)
    {
        // Get nearby entities
        Vec3d center = troop.getSettings().getCenter();
        AxisAlignedBB box = new AxisAlignedBB(center.x - boxSize, center.y - boxSize, center.z - boxSize,
                center.x + boxSize, center.y + boxSize, center.z + boxSize);
        return bg.world.getEntitiesInAABBexcluding(troop.getMaster(), box, predicate);
    }

    private static void attackEntity(Entity target, EntityBodyguard bodyguard)
    {
        if (target instanceof EntityLivingBase) {
            bodyguard.setAttackTarget((EntityLivingBase) target);
            ItemUtil.swapHandsIfNeeded(bodyguard);
        }
    }
}
