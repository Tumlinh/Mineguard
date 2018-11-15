package mineguard.troop;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import mineguard.entity.EntityGuard;
import mineguard.init.ModConfigServer;
import mineguard.troop.settings.*;
import mineguard.util.EntityUtil;
import mineguard.util.NBTUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class Troop
{
    private EntityPlayer master;
    private String masterName;
    private Settings settings;

    // For computing serial numbers (id) when spawning guards
    private int maxIndex = -1;

    private List<EntityGuard> guards = new ArrayList<EntityGuard>()
    {
        private static final long serialVersionUID = 1L;

        public boolean add(EntityGuard newGuard)
        {
            // Check UUID to avoid duplicates of the same entity (it happens when
            // loading/unloading worlds)
            ListIterator<EntityGuard> guardIterator = this.listIterator();
            while (guardIterator.hasNext()) {
                if (guardIterator.next().getUniqueID().equals(newGuard.getUniqueID())) {
                    guardIterator.set(newGuard);
                    return true;
                }
            }
            guardIterator.add(newGuard);
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

    public void addGuard(EntityGuard guard)
    {
        if (guard.getId() == NBTUtil.UNDEFINED)
            guard.setId(++maxIndex);
        else if (guard.getId() > maxIndex)
            maxIndex = guard.getId();

        if (!guard.hasCustomName())
            guard.updateName();

        guards.add(guard);
        Collections.sort(guards, new Comparator<EntityGuard>()
        {
            @Override
            public int compare(EntityGuard guard1, EntityGuard guard2)
            {
                return guard1.getId() < guard2.getId() ? -1 : guard1.getId() == guard2.getId() ? 0 : 1;
            }
        });
    }

    public int getGuardCount()
    {
        return guards.size();
    }

    public EntityGuard getFirstGuard()
    {
        if (!guards.isEmpty())
            return guards.get(0);
        return null;
    }

    public int getGuardIndex(EntityGuard guard) throws GuardNotFoundException
    {
        for (int i = 0; i < guards.size(); i++) {
            if (guards.get(i) == guard)
                return i;
        }
        throw new GuardNotFoundException();
    }

    public void summonGuards(World world, BlockPos pos, int count)
            throws TroopInOtherDimensionException, GuardOverflowException
    {
        if (guards.size() + count > ModConfigServer.MAX_TROOP_SIZE)
            throw new GuardOverflowException();

        // Check dimension
        if (guards.isEmpty())
            settings.setDimension(world.provider.getDimension());
        else if (world.provider.getDimension() != settings.getDimension())
            throw new TroopInOtherDimensionException();

        int maxIndexOld = maxIndex;
        for (int i = maxIndexOld + 1; i <= maxIndexOld + count; i++)
            EntityUtil.summonGuard(this, i, world, pos);
    }

    public void removeGuard(EntityGuard guard)
    {
        Iterator<EntityGuard> guardIterator = guards.iterator();
        while (guardIterator.hasNext()) {
            if (guardIterator.next().equals(guard))
                guardIterator.remove();
        }
    }

    public void removeGuards()
    {
        for (EntityGuard guard : guards)
            guard.setDead();
        this.resetGuardList();
    }

    public void resetGuardList()
    {
        guards.clear();
        maxIndex = -1;
    }

    public Vec3d getPosInFormation(Formation formation, int index)
    {
        Vec3d center = settings.getCenter();
        if (center == null)
            return null;

        double posX = center.x, posY = center.y, posZ = center.z;
        double perimeter, linearPos;
        double size = settings.getSize();

        if (formation == Formation.SQUARE) {
            // Size is half of side
            perimeter = 8 * size;
            linearPos = perimeter * index / guards.size();

            // Compatible with the circle formation
            if (linearPos < size) {
                posX = center.x + size;
                posZ = center.z - linearPos;
            } else if (linearPos >= size && linearPos < 3 * size) {
                posX = center.x - linearPos + 2 * size;
                posZ = center.z - size;
            } else if (linearPos >= 3 * size && linearPos < 5 * size) {
                posX = center.x - size;
                posZ = center.z + linearPos - 4 * size;
            } else if (linearPos >= 5 * size && linearPos < 7 * size) {
                posX = center.x + linearPos - 6 * size;
                posZ = center.z + size;
            } else if (linearPos >= 7 * size && linearPos < perimeter) {
                posX = center.x + size;
                posZ = center.z - linearPos + 8 * size;
            }
        } else if (formation == Formation.CIRCLE) {
            // Size is radius
            linearPos = 2.0 * Math.PI * index / guards.size();

            posX = center.x + size * Math.cos(linearPos);
            posZ = center.z - size * Math.sin(linearPos);
        }
        return new Vec3d(posX, posY, posZ);
    }

    public void updateNames()
    {
        for (EntityGuard guard : guards)
            guard.updateName();
    }

    public void updateHelmets()
    {
        for (EntityGuard guard : guards)
            guard.putOnColorizedHelmet();
    }

    public void give(String name) throws GuardOverflowException
    {
        if (!masterName.equals(name)) {
            Troop receivingTroop = Troop.getTroop(name);

            if (receivingTroop.guards.size() + this.guards.size() > ModConfigServer.MAX_TROOP_SIZE)
                throw new GuardOverflowException();

            // Get guards
            for (EntityGuard guard : guards)
                guard.give(receivingTroop);

            this.resetGuardList();
        }
    }

    @Override
    public String toString()
    {
        String ret = "master=" + masterName + " g_count=" + guards.size() + "\n" + settings;
        // XXX: debugging
        ret += "\n";
        for (EntityGuard guard : guards)
            ret += guard.toString() + "\n";
        return ret;
    }

    public class GuardOverflowException extends Exception
    {
        private static final long serialVersionUID = 1L;

        @Override
        public String getMessage()
        {
            return "Failed adding guards: Limit reached";
        }
    }

    public class GuardNotFoundException extends Exception
    {
        private static final long serialVersionUID = 1L;

    }

    public class TroopInOtherDimensionException extends Exception
    {
        private static final long serialVersionUID = 1L;

        @Override
        public String getMessage()
        {
            return "Failed summoning guards: Troop is in another dimension";
        }
    }
}
