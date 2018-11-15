package mineguard.entity.ai;

import mineguard.entity.EntityGuard;
import mineguard.init.ModConfigServer;
import mineguard.troop.Troop;
import mineguard.troop.Troop.GuardNotFoundException;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class EntityAIReform extends EntityAIBase
{
    private EntityGuard guard;
    private int timeToRecalcPath;
    private Vec3d center;
    private double maxDist;

    public EntityAIReform(EntityGuard guard, double maxDist)
    {
        this.guard = guard;
        this.maxDist = maxDist;
        this.setMutexBits(3);
    }

    @Override
    public boolean shouldExecute()
    {
        if (guard != null && guard.getTroop() != null) {
            center = guard.getTroop().getSettings().getCenter();
            return center != null;
        } else {
            return false;
        }
    }

    @Override
    public void updateTask()
    {
        if (--this.timeToRecalcPath <= 0) {
            this.timeToRecalcPath = 5;

            // Reform guard
            Troop troop = guard.getTroop();
            Vec3d pos;
            try {
                pos = troop.getPosInFormation(troop.getSettings().getFormation(), troop.getGuardIndex(guard));
            } catch (GuardNotFoundException e) {
                return;
            }

            if (guard.getDistance(pos.x, pos.y, pos.z) <= maxDist) {
                guard.getNavigator().tryMoveToXYZ(pos.x, pos.y, pos.z, ModConfigServer.GUARD_NAVIGATION_SPEED);
            } else {
                // Look for friendly block in a cube around expected position
                int x = MathHelper.floor(pos.x);
                int y = MathHelper.floor(pos.y);
                int z = MathHelper.floor(pos.z);

                for (int i = -2; i <= 2; i++) {
                    for (int j = -1; j <= 1; j++) {
                        for (int k = -2; k <= 2; k++) {
                            if (this.isTeleportFriendlyBlock(x + i, y + j, z + k)) {
                                guard.setLocationAndAngles((double) ((float) (x + i) + 0.5F), (double) y + j,
                                        (double) ((float) (z + k) + 0.5F), guard.rotationYaw, guard.rotationPitch);
                                guard.getNavigator().clearPath();
                                return;
                            }
                        }
                    }
                }
            }
        }

    }

    protected boolean isTeleportFriendlyBlock(int x, int y, int z)
    {
        BlockPos blockpos = new BlockPos(x, y - 1, z);
        IBlockState iblockstate = guard.world.getBlockState(blockpos);
        return iblockstate.getBlockFaceShape(guard.world, blockpos, EnumFacing.DOWN) == BlockFaceShape.SOLID
                && iblockstate.canEntitySpawn(guard) && guard.world.isAirBlock(blockpos.up())
                && guard.world.isAirBlock(blockpos.up(2));
    }
}
