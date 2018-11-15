package mineguard.entity.ai;

import com.google.common.base.Predicate;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;

import mineguard.entity.EntityGuard;
import mineguard.troop.Troop;
import mineguard.troop.settings.Behaviour;
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
    private EntityGuard guard;
    private Troop troop;

    private static double boxSize = 20.0;

    public EntityAIBehaviour(EntityGuard guard)
    {
        this.guard = guard;
        troop = guard.getTroop();
        this.setMutexBits(3);
    }

    @Override
    public boolean shouldExecute()
    {
        troop = guard.getTroop();
        return troop != null;
    }

    @Override
    public void updateTask()
    {
        if (guard.getAttackTarget() != null && !guard.getAttackTarget().isEntityAlive())
            guard.setAttackTarget((EntityLivingBase) null);

        if (troop.getSettings().getBehaviour() != Behaviour.BERSERKER
                && !AIUtil.tasksContain(guard.tasks, guard.reformTask))
            guard.tasks.addTask(3, guard.reformTask);

        // Aggressive mode: if no nearby hostile targets, attack any nearby target
        // sorted by priority
        if (troop.getSettings().getBehaviour() == Behaviour.AGGRESSIVE) {
            List<Entity> nearbyHostileTargets = this.getNearbyHostileTargets();

            // Sort entities by dangerousness (cf. heuristics)
            nearbyHostileTargets.sort(new AIUtil.DistanceSorter(troop.getMaster(), guard));

            if (!nearbyHostileTargets.isEmpty()) {
                attackEntity(nearbyHostileTargets.get(0), guard);
            } else {
                List<Entity> nearbyTargets = this.getNearbyTargets();
                nearbyTargets.sort(new AIUtil.DistanceSorter(troop.getMaster(), guard));

                if (!nearbyTargets.isEmpty())
                    attackEntity(nearbyTargets.get(0), guard);
            }
        }

        // Berserker mode: attack any nearby target except master
        else if (troop.getSettings().getBehaviour() == Behaviour.BERSERKER) {
            // Stop reforming
            guard.tasks.removeTask(guard.reformTask);

            List<Entity> nearbyTargets = this.getNearbyTargets();
            nearbyTargets.sort(new AIUtil.DistanceSorter((Entity) null, guard));

            if (!nearbyTargets.isEmpty())
                attackEntity(nearbyTargets.get(0), guard);
        }

        // Defensive mode: attack nearby hostile targets sorted by priority
        else if (troop.getSettings().getBehaviour() == Behaviour.DEFENSIVE) {
            List<Entity> nearbyHostileTargets = this.getNearbyHostileTargets();
            nearbyHostileTargets.sort(new AIUtil.DistanceSorter(troop.getMaster(), guard));

            if (!nearbyHostileTargets.isEmpty())
                attackEntity(nearbyHostileTargets.get(0), guard);
        }

        else if (troop.getSettings().getBehaviour() == Behaviour.STILL) {
            guard.setAttackTarget((EntityLivingBase) null);
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
        if (center == null)
            return new ArrayList<Entity>();

        AxisAlignedBB box = new AxisAlignedBB(center.x - boxSize, center.y - boxSize, center.z - boxSize,
                center.x + boxSize, center.y + boxSize, center.z + boxSize);
        return guard.world.getEntitiesInAABBexcluding(troop.getMaster(), box, predicate);
    }

    private static void attackEntity(Entity target, EntityGuard guard)
    {
        if (target instanceof EntityLivingBase) {
            guard.setAttackTarget((EntityLivingBase) target);
            ItemUtil.swapHandsIfNeeded(guard);
        }
    }
}
