package mineguard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import mineguard.entity.EntityBodyguard;
import mineguard.init.ModConfig;
import mineguard.settings.*;
import mineguard.util.EntityUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;

public class Troop
{
    private EntityPlayer master;
    private String masterName;
    private Settings settings;

    // For computing serial numbers (id) when spawning bodyguards
    private int maxIndex = -1;

    private List<EntityBodyguard> bodyguards = new ArrayList<EntityBodyguard>()
    {
        private static final long serialVersionUID = 1L;

        public boolean add(EntityBodyguard newBodyguard)
        {
            // Check UUID to avoid duplicates of the same entity (it happens when
            // loading/unloading worlds)
            ListIterator<EntityBodyguard> bgIterator = this.listIterator();
            while (bgIterator.hasNext()) {
                if (bgIterator.next().getUniqueID().equals(newBodyguard.getUniqueID())) {
                    bgIterator.set(newBodyguard);
                    return true;
                }
            }
            bgIterator.add(newBodyguard);
            return true;
        }
    };

    private static Map<String, Troop> troops = new HashMap<String, Troop>();

    public Troop(String masterName)
    {
        master = EntityUtil.getPlayerFromName(masterName);
        this.masterName = masterName;
        settings = new Settings(this);
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
        masterName = name;
        master = EntityUtil.getPlayerFromName(name);
    }

    public Settings getSettings()
    {
        return settings;
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

    public static Map<String, Troop> getTroops()
    {
        return troops;
    }

    public void addBodyguard(EntityBodyguard bg)
    {
        bodyguards.add(bg);
        Collections.sort(bodyguards, new Comparator<EntityBodyguard>()
        {
            @Override
            public int compare(EntityBodyguard bg1, EntityBodyguard bg2)
            {
                return bg1.getId() < bg2.getId() ? -1 : bg1.getId() == bg2.getId() ? 0 : 1;
            }
        });

        if (bg.getId() > maxIndex)
            maxIndex = bg.getId();
    }

    public int getBodyguardCount()
    {
        return bodyguards.size();
    }

    public int getBodyguardPos(EntityBodyguard bodyguard) throws PositionNotFoundException
    {
        for (int i = 0; i < bodyguards.size(); i++) {
            if (bodyguards.get(i) == bodyguard)
                return i;
        }
        throw new PositionNotFoundException();
    }

    public void summonBodyguards(World world, BlockPos pos, int count) throws TroopInOtherDimensionException
    {
        if (bodyguards.isEmpty())
            settings.setDimension(world.provider.getDimension());
        else if (world.provider.getDimension() != settings.getDimension())
            throw new TroopInOtherDimensionException();

        int maxIndexOld = maxIndex;
        for (int i = maxIndexOld + 1; i <= maxIndexOld + count; i++)
            EntityUtil.summonBodyguard(this, i, world, pos);
    }

    public void removeBodyguard(EntityBodyguard bodyguard)
    {
        Iterator<EntityBodyguard> bgIterator = bodyguards.iterator();
        while (bgIterator.hasNext()) {
            if (bgIterator.next().equals(bodyguard))
                bgIterator.remove();
        }
    }

    public void removeBodyguards()
    {
        // TODO: rm troop completely?
        for (EntityBodyguard bodyguard : bodyguards)
            bodyguard.setDead();
        this.resetBodyguardList();
    }

    public void resetBodyguardList()
    {
        bodyguards.clear();
        maxIndex = -1;
    }

    public void reform()
    {
        for (int i = 0; i < bodyguards.size(); i++)
            this.reformBodyguard(i);
    }

    // Handle bodyguard movements inside a formation
    public boolean reformBodyguard(int index)
    {
        EntityBodyguard bodyguard = bodyguards.get(index);
        if (bodyguard == null)
            return false;

        Vec3i center = settings.getCenter();
        if (center == null)
            return false;

        double posX = 0, posY = center.getY(), posZ = 0;
        double perimeter, linearPos;
        double size = settings.getSize();

        if (settings.getFormation() == Formation.SQUARE) {
            // Size is half of side
            perimeter = 8 * size;
            linearPos = perimeter * index / bodyguards.size();

            // Compatible with the circle formation
            if (linearPos < size) {
                posX = center.getX() + size;
                posZ = center.getZ() - linearPos;
            } else if (linearPos >= size && linearPos < 3 * size) {
                posX = center.getX() - linearPos + 2 * size;
                posZ = center.getZ() - size;
            } else if (linearPos >= 3 * size && linearPos < 5 * size) {
                posX = center.getX() - size;
                posZ = center.getZ() + linearPos - 4 * size;
            } else if (linearPos >= 5 * size && linearPos < 7 * size) {
                posX = center.getX() + linearPos - 6 * size;
                posZ = center.getZ() + size;
            } else if (linearPos >= 7 * size && linearPos < perimeter) {
                posX = center.getX() + size;
                posZ = center.getZ() - linearPos + 8 * size;
            }
        } else if (settings.getFormation() == Formation.CIRCLE) {
            // Size is radius
            linearPos = 2.0 * Math.PI * index / bodyguards.size();

            posX = center.getX() + size * Math.cos(linearPos);
            posZ = center.getZ() - size * Math.sin(linearPos);
        }

        return bodyguard.getNavigator().tryMoveToXYZ(posX, posY, posZ, ModConfig.BODYGUARD_SPEED_TARGET);
    }

    public void updateNames()
    {
        for (EntityBodyguard bodyguard : bodyguards)
            bodyguard.updateName();
    }

    public void updateHelmets()
    {
        for (EntityBodyguard bodyguard : bodyguards)
            bodyguard.putOnColorizedHelmet();
    }

    public void give(String name)
    {
        if (!masterName.equals(name)) {
            Troop receivingTroop = Troop.getTroop(name);

            // Update bodyguards
            for (EntityBodyguard bodyguard : bodyguards) {
                bodyguard.setTroop(receivingTroop);
                bodyguard.putOnColorizedHelmet();
                // No need to change bg's master, this is done by writeToNBT()

                receivingTroop.addBodyguard(bodyguard);
            }

            this.resetBodyguardList();
        }
    }

    @Override
    public String toString()
    {
        String ret = "master=" + masterName + " bg_count=" + bodyguards.size() + "\n" + settings;
        return ret;
    }

    public class PositionNotFoundException extends Exception
    {
        private static final long serialVersionUID = 1L;

    }

    public class TroopInOtherDimensionException extends Exception
    {
        private static final long serialVersionUID = 1L;
    }
}
