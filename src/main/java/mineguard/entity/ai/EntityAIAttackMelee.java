package mineguard.entity.ai;

import mineguard.entity.EntityBodyguard;
import mineguard.init.ModConfig;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.util.EnumHand;

public class EntityAIAttackMelee extends net.minecraft.entity.ai.EntityAIAttackMelee
{
    private EntityBodyguard attacker;
    private final int attackInterval;
    private float defenseRatio;

    public EntityAIAttackMelee(EntityBodyguard bodyguard, double speedIn, boolean useLongMemory)
    {
        super(bodyguard, speedIn, useLongMemory);

        attacker = bodyguard;
        attackInterval = (int) (1.0D
                / bodyguard.getEntityAttribute(SharedMonsterAttributes.ATTACK_SPEED).getAttributeValue() * 20.0D);
        defenseRatio = ModConfig.BODYGUARD_DEFENSE_RATIO;
    }

    @Override
    public void resetTask()
    {
        super.resetTask();
        attacker.setShield(false);
    }

    @Override
    protected void checkAndPerformAttack(EntityLivingBase target, double targetDistance)
    {
        // Bodyguard can attack XOR defend using shield
        // However there is a short time when bodyguard cannot attack nor defend

        double reach = this.getAttackReachSqr(target);
        // Target in reach
        if (targetDistance <= reach) {
            if (attackTick <= attackInterval && attackTick >= attackInterval * (1.0F - defenseRatio)) {
                ((EntityBodyguard) attacker).setShield(true);
            } else {
                ((EntityBodyguard) attacker).setShield(false);

                if (this.attackTick <= 0) {
                    attackTick = attackInterval;
                    attacker.swingArm(EnumHand.MAIN_HAND);
                    attacker.attackEntityAsMob(target);
                }
            }
        }

        // Target out of reach
        else {
            attacker.setShield(true);
        }
    }
}
