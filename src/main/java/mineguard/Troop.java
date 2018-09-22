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
        this.masterName = name;
        this.master = EntityUtil.getPlayerFromName(name);
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

    public int getBodyguardPos(EntityBodyguard bodyguard) throws PositionNotFoundException
    {
        for (int i = 0; i < bodyguards.size(); i++) {
            if (bodyguards.get(i) == bodyguard)
                return i;
        }
        throw new PositionNotFoundException();
    }

    public void summonBodyguards(World world, BlockPos pos, int count)
    {
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
        if (master != null) {
            for (int i = 0; i < bodyguards.size(); i++)
                this.reformBodyguard(i);
        }
    }

    // Handle bodyguard movements inside a formation
    public boolean reformBodyguard(int index)
    {
        EntityBodyguard bodyguard = bodyguards.get(index);
        if (master == null || bodyguard == null)
            return false;

        double posX = 0, posY = master.posY, posZ = 0;
        double perimeter, linearPos;
        double size = settings.getSize();

        if (settings.getFormation() == Formation.SQUARE) {
            // Size is half of side
            perimeter = 8 * size;
            linearPos = perimeter * index / bodyguards.size();

            // Compatible with the circle formation
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
            linearPos = 2.0 * Math.PI * index / bodyguards.size();

            posX = master.posX + size * Math.cos(linearPos);
            posZ = master.posZ - size * Math.sin(linearPos);
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
        if (masterName != name) {
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
        String ret = "master=" + masterName + " " + settings + " bg_count=" + bodyguards.size();
        for (EntityBodyguard bodyguard : bodyguards)
            ret += "\n" + bodyguard;
        return ret;
    }
}
