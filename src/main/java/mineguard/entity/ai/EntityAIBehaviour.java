package mineguard.entity.ai;

import com.google.common.base.Predicate;
import java.util.List;
import javax.annotation.Nullable;
import mineguard.Troop;
import mineguard.entity.EntityBodyguard;
import mineguard.settings.Behaviour;
import mineguard.util.AIUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.util.math.AxisAlignedBB;

public class EntityAIBehaviour extends EntityAIBase
{
    private EntityBodyguard bg;
    private Troop troop;

    private static enum State
    {
        IDLE,
        FIGHTING
    }

    private State state = State.IDLE;

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
        return troop != null && troop.getSettings().getBehaviour() != Behaviour.STILL;
    }

    @Override
    public boolean shouldContinueExecuting()
    {
        // Workaround to make this task stop from time to time
        return false;
    }

    @Override
    public void startExecuting()
    {
        // Defensive mode
        if (troop.getSettings().getBehaviour() == Behaviour.DEFENSIVE && troop.getMaster() != null) {
            // Get nearby entities
            AxisAlignedBB box = new AxisAlignedBB(troop.getMaster().posX - boxSize, troop.getMaster().posY - boxSize,
                    troop.getMaster().posZ - boxSize, troop.getMaster().posX + boxSize,
                    troop.getMaster().posY + boxSize, troop.getMaster().posZ + boxSize);
            List<Entity> nearbyEntities = bg.world.getEntitiesInAABBexcluding(troop.getMaster(), box,
                    new Predicate<Entity>()
                    {
                        public boolean apply(@Nullable Entity entity)
                        {
                            return AIUtil.isHostile(troop, entity);
                            /*
                             * return (entity instanceof EntityLivingBase) && entity != troop.getMaster() &&
                             * !(entity instanceof EntityPlayer && ((EntityPlayer) entity).isSpectator());
                             */
                        }
                    });

            // Pick the most dangerous entity (cf. heuristics)
            nearbyEntities.sort(new AIUtil.DistanceSorter(troop.getMaster(), bg));
            if (!nearbyEntities.isEmpty()) {
                bg.setAttackTarget((EntityLivingBase) nearbyEntities.get(0));
                state = State.FIGHTING;
            } else if (state == State.FIGHTING) {
                bg.reform();
                state = State.IDLE;
            }
        }

        // TODO: Aggressive
        // Like defensive. If there are no threats, attack potential threats (mobs,
        // iron golems, players not whitelisted)
        else if (troop.getSettings().getBehaviour() == Behaviour.AGGRESSIVE) {

        }

        // TODO: Berserker
        // Attack any nearby entity except master (do not give a shit about him)
        else if (troop.getSettings().getBehaviour() == Behaviour.BERSERKER) {

        }
    }
}
