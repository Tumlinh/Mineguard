package mineguard.entity.ai;

import mineguard.entity.EntityBodyguard;
import net.minecraft.entity.ai.EntityAIBase;

public class EntityAIReform extends EntityAIBase
{
    private EntityBodyguard bg;

    public EntityAIReform(EntityBodyguard bg)
    {
        this.bg = bg;
        this.setMutexBits(3);
    }

    @Override
    public boolean shouldExecute()
    {
        return true;
    }

    @Override
    public void updateTask()
    {
        // Reform bodyguard
        bg.reform();
    }
}
