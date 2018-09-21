package mineguard;

import java.util.HashMap;
import java.util.Map;
import mineguard.entity.EntityBodyguard;
import mineguard.init.ModConfig;
import mineguard.settings.*;
import mineguard.util.EntityUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class Troop
{
    private EntityPlayer master;
    private String masterName;
    private Settings settings;

    private int maxIndex = -1;
    private Map<Integer, EntityBodyguard> bodyguards = new HashMap<Integer, EntityBodyguard>();
    private static Map<String, Troop> troops = new HashMap<String, Troop>();

    public Troop(String masterName)
    {
        master = EntityUtil.getPlayerFromName(masterName);
        this.masterName = masterName;
        settings = new Settings(this);
    }

    public static Troop getTroop(String playerName)
    {
        Troop troop = troops.get(playerName);
        if (troop == null) {
            troop = new Troop(playerName);
            troops.put(playerName, troop);
        }
        return troop;
    }

    public EntityPlayer getMaster()
    {
        return master;
    }

    public String getMasterName()
    {
        return masterName;
    }

    public void setMaster(String name)
    {
        this.masterName = name;
        this.master = EntityUtil.getPlayerFromName(name);
    }

    public Settings getSettings()
    {
        return settings;
    }

    public void addBodyguard(EntityBodyguard bg)
    {
        bodyguards.put(bg.getIndex(), bg);
        if (bg.getIndex() > maxIndex)
            maxIndex = bg.getIndex();
    }

    public void summonBodyguards(World world, BlockPos pos, int count)
    {
        int maxIndexOld = maxIndex;
        for (int i = maxIndexOld + 1; i <= maxIndexOld + count; i++)
            EntityUtil.summonBodyguard(this, i, world, pos);
    }

    public void removeBodyguards()
    {
        // TODO: rm troop completely?
        for (EntityBodyguard bodyguard : bodyguards.values())
            bodyguard.setDead();
        bodyguards.clear();
        maxIndex = -1;
    }

    public void reform()
    {
        if (master != null) {
            for (EntityBodyguard bodyguard : bodyguards.values())
                this.reformBodyguard(bodyguard.getIndex());
        }
    }

    // Handle bodyguard movements inside a formation
    public boolean reformBodyguard(int bgIndex)
    {
        double posX = 0, posY = master.posY, posZ = 0;
        double perimeter, linearPos;
        double size = settings.getSize();

        if (settings.getFormation() == Formation.SQUARE) {
            // Size is half of side
            perimeter = 8 * size;
            linearPos = perimeter * bgIndex / bodyguards.size();

            // Compatible with the circle algo
            if (linearPos < size) {
                posX = master.posX + size;
                posZ = master.posZ - linearPos;
            } else if (linearPos >= size && linearPos < 3 * size) {
                posX = master.posX - linearPos + 2 * size;
                posZ = master.posZ - size;
            } else if (linearPos >= 3 * size && linearPos < 5 * size) {
                posX = master.posX - size;
                posZ = master.posZ + linearPos - 4 * size;
            } else if (linearPos >= 5 * size && linearPos < 7 * size) {
                posX = master.posX + linearPos - 6 * size;
                posZ = master.posZ + size;
            } else if (linearPos >= 7 * size && linearPos < perimeter) {
                posX = master.posX + size;
                posZ = master.posZ - linearPos + 8 * size;
            }
        } else if (settings.getFormation() == Formation.CIRCLE) {
            // Size is radius
            linearPos = 2.0 * Math.PI * bgIndex / bodyguards.size();

            posX = master.posX + size * Math.cos(linearPos);
            posZ = master.posZ - size * Math.sin(linearPos);
        }

        EntityBodyguard bodyguard = bodyguards.get(bgIndex);
        if (bodyguard != null)
            return bodyguard.getNavigator().tryMoveToXYZ(posX, posY, posZ, ModConfig.BODYGUARD_SPEED_TARGET);
        return false;
    }

    public void updateNames()
    {
        for (EntityBodyguard bodyguard : bodyguards.values())
            bodyguard.updateName();
    }

    @Override
    public String toString()
    {
        String ret = "master=" + masterName + " " + settings + "\nbg_count=" + bodyguards.size();
        return ret;
    }
}
