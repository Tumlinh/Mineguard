package mineguard.entity.ai;

import mineguard.Troop;
import mineguard.Troop.BodyguardNotFoundException;
import mineguard.entity.EntityBodyguard;
import mineguard.init.ModConfigServer;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class EntityAIReform extends EntityAIBase
{
    private EntityBodyguard bg;
    private int timeToRecalcPath;
    private Vec3d center;
    private double maxDist;

    public EntityAIReform(EntityBodyguard bg, double maxDist)
    {
        this.bg = bg;
        this.maxDist = maxDist;
        this.setMutexBits(3);
    }

    @Override
    public boolean shouldExecute()
    {
        if (bg != null && bg.getTroop() != null) {
            center = bg.getTroop().getSettings().getCenter();
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

            // Reform bodyguard
            Troop troop = bg.getTroop();
            Vec3d pos;
            try {
                pos = troop.getPosInFormation(troop.getSettings().getFormation(), troop.getBodyguardIndex(bg));
            } catch (BodyguardNotFoundException e) {
                return;
            }

            if (bg.getDistance(pos.x, pos.y, pos.z) <= maxDist) {
                bg.getNavigator().tryMoveToXYZ(pos.x, pos.y, pos.z, ModConfigServer.BODYGUARD_NAVIGATION_SPEED);
            } else {
                // Look for friendly block in a cube around expected position
                int x = MathHelper.floor(pos.x);
                int y = MathHelper.floor(pos.y);
                int z = MathHelper.floor(pos.z);

                for (int i = -2; i <= 2; i++) {
                    for (int j = -1; j <= 1; j++) {
                        for (int k = -2; k <= 2; k++) {
                            if (this.isTeleportFriendlyBlock(x + i, y + j, z + k)) {
                                bg.setLocationAndAngles((double) ((float) (x + i) + 0.5F), (double) y + j,
                                        (double) ((float) (z + k) + 0.5F), bg.rotationYaw, bg.rotationPitch);
                                bg.getNavigator().clearPath();
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
        IBlockState iblockstate = bg.world.getBlockState(blockpos);
        return iblockstate.getBlockFaceShape(bg.world, blockpos, EnumFacing.DOWN) == BlockFaceShape.SOLID
                && iblockstate.canEntitySpawn(bg) && bg.world.isAirBlock(blockpos.up())
                && bg.world.isAirBlock(blockpos.up(2));
    }
}
