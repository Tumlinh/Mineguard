package mineguard.entity.ai;

import mineguard.entity.EntityBodyguard;
import mineguard.init.ModConfigServer;
import mineguard.util.ItemUtil;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumHand;

public class EntityAIAttackMelee extends net.minecraft.entity.ai.EntityAIAttackMelee
{
    private EntityBodyguard attacker;
    private float defenseRatio;

    public EntityAIAttackMelee(EntityBodyguard bodyguard, double speedIn, boolean useLongMemory)
    {
        super(bodyguard, speedIn, useLongMemory);
        attacker = bodyguard;
        defenseRatio = ModConfigServer.BODYGUARD_DEFENSE_RATIO;
    }

    @Override
    public void resetTask()
    {
        attacker.setShield(false);
    }

    @Override
    protected void checkAndPerformAttack(EntityLivingBase target, double targetDistance)
    {
        // Bodyguard can attack XOR defend using shield
        // However there is a short time when bodyguard cannot attack nor defend

        int attackInterval = (int) (1.0F / ItemUtil.getAttackSpeed(attacker, attacker.getHeldItemMainhand()) * 20.0F);
        double reach = this.getAttackReachSqr(target);
        // Target in reach
        if (targetDistance <= reach) {
            if (attackTick <= attackInterval && attackTick >= attackInterval * defenseRatio) {
                ((EntityBodyguard) attacker).setShield(false);
            } else {
                ((EntityBodyguard) attacker).setShield(true);

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
