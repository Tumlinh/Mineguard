package mineguard.entity.ai;

import mineguard.entity.EntityGuard;
import mineguard.init.ModConfigServer;
import mineguard.util.ItemUtil;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumHand;

public class EntityAIAttackMelee extends net.minecraft.entity.ai.EntityAIAttackMelee
{
    private EntityGuard attacker;
    private float defenseRatio;

    public EntityAIAttackMelee(EntityGuard guard, double speedIn, boolean useLongMemory)
    {
        super(guard, speedIn, useLongMemory);
        attacker = guard;
        defenseRatio = ModConfigServer.GUARD_DEFENSE_RATIO;
    }

    @Override
    public void resetTask()
    {
        attacker.setShield(false);
    }

    @Override
    protected void checkAndPerformAttack(EntityLivingBase target, double targetDistance)
    {
        // Guard can attack XOR defend using shield
        // However there is a short time when guard cannot attack nor defend

        int attackInterval = (int) (1.0F / ItemUtil.getAttackSpeed(attacker, attacker.getHeldItemMainhand()) * 20.0F);
        double reach = this.getAttackReachSqr(target);
        // Target in reach
        if (targetDistance <= reach) {
            if (attackTick <= attackInterval && attackTick >= attackInterval * defenseRatio) {
                ((EntityGuard) attacker).setShield(false);
            } else {
                ((EntityGuard) attacker).setShield(true);

                if (this.attackTick <= 0) {
                    attackTick = attackInterval;
                    attacker.swingArm(EnumHand.MAIN_HAND);
                    attacker.attackEntityAsMob(target);

                    attacker.setShield(false);
                }
            }
        }

        // Target out of reach
        else {
            attacker.setShield(true);
        }
    }
}
