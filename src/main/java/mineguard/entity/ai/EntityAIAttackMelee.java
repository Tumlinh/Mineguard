package mineguard.entity.ai;

import mineguard.entity.EntityBodyguard;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.util.EnumHand;

public class EntityAIAttackMelee extends net.minecraft.entity.ai.EntityAIAttackMelee
{
    private final int attackInterval;
    private final float defenseIntervalRatio = 0.25F;

    public EntityAIAttackMelee(EntityBodyguard bodyguard, double speedIn, boolean useLongMemory)
    {
        super(bodyguard, speedIn, useLongMemory);
        attackInterval = (int) (1.0D
                / bodyguard.getEntityAttribute(SharedMonsterAttributes.ATTACK_SPEED).getAttributeValue() * 20.0D);
    }

    @Override
    protected void checkAndPerformAttack(EntityLivingBase target, double targetDistance)
    {
        // Bodyguard can attack XOR defend using shield
        // However there is a short time when bodyguard cannot attack nor defend
        if (attackTick <= attackInterval && attackTick >= attackInterval * defenseIntervalRatio) {
            ((EntityBodyguard) attacker).setShield(true);
        }

        else {
            ((EntityBodyguard) attacker).setShield(false);

            double reach = this.getAttackReachSqr(target);
            if (targetDistance <= reach && this.attackTick <= 0) {
                attackTick = attackInterval;
                attacker.swingArm(EnumHand.MAIN_HAND);
                attacker.attackEntityAsMob(target);
            }
        }
    }
}
