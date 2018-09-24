package mineguard.settings;

import java.io.File;
import java.io.IOException;
import mineguard.Troop;
import mineguard.init.ModConfig;
import mineguard.util.NBTUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.Vec3d;

public class Settings
{
    private Troop troop;

    // Settings
    private Behaviour behaviour = Behaviour.DEFENSIVE;
    private int color = 0x009900;
    private boolean displayName = true;
    private boolean follow = true;
    private Formation formation = Formation.SQUARE;
    private String nameFormat = "Agent%d";
    private double size = 10.0;

    private Vec3d center;
    private int dimension;

    public Settings(Troop troop)
    {
        this.troop = troop;
        this.readFromNBT();

        EntityPlayer master = troop.getMaster();
        if (master != null) {
            if (center == null) {
                center = new Vec3d(master.posX, master.posY, master.posZ);
                this.writeToNBT();
            }
        }
    }

    public Behaviour getBehaviour()
    {
        return behaviour;
    }

    public void setBehaviour(Behaviour behaviour)
    {
        if (this.behaviour != behaviour) {
            this.behaviour = behaviour;
            this.writeToNBT();
        }
    }

    public Vec3d getCenter()
    {
        return center;
    }

    public void setCenter(Vec3d center)
    {
        this.center = center;
    }

    public int getColor()
    {
        return color;
    }

    public void setColor(int color)
    {
        if (this.color != color) {
            this.color = color;
            this.writeToNBT();
            troop.updateHelmets();
        }
    }

    public int getDimension()
    {
        return dimension;
    }

    public void setDimension(int dimension)
    {
        if (this.dimension != dimension) {
            this.dimension = dimension;
            this.writeToNBT();
        }
    }

    public boolean isDisplayName()
    {
        return displayName;
    }

    public void setDisplayName(boolean displayName)
    {
        if (this.displayName != displayName) {
            this.displayName = displayName;
            this.writeToNBT();
            troop.updateNames();
        }
    }

    public boolean isFollowing()
    {
        return follow;
    }

    public void setFollow(boolean follow)
    {
        if (this.follow != follow) {
            this.follow = follow;
            this.writeToNBT();
        }
    }

    public Formation getFormation()
    {
        return formation;
    }

    public void setFormation(Formation formation)
    {
        if (this.formation != formation) {
            this.formation = formation;
            this.writeToNBT();
        }
    }

    public String getNameFormat()
    {
        return nameFormat;
    }

    public void setNameFormat(String nameFormat)
    {
        if (this.nameFormat != nameFormat) {
            this.nameFormat = nameFormat;
            this.writeToNBT();
            troop.updateNames();
        }
    }

    public double getSize()
    {
        return size;
    }

    public void setSize(double size)
    {
        if (this.size != size) {
            this.size = size;
            this.writeToNBT();
        }
    }

    public void writeToNBT()
    {
        // Build player settings
        NBTTagCompound playerSettings = new NBTTagCompound();
        playerSettings.setByte("Behaviour", (byte) Behaviour.get(behaviour.getText()).getId());
        playerSettings.setInteger("Color", color);
        playerSettings.setBoolean("DisplayName", displayName);
        playerSettings.setBoolean("Follow", follow);
        playerSettings.setByte("Formation", (byte) Formation.get(formation.getText()).getId());
        playerSettings.setString("NameFormat", nameFormat);
        playerSettings.setDouble("Size", size);

        playerSettings.setDouble("CenterX", center.x);
        playerSettings.setDouble("CenterY", center.y);
        playerSettings.setDouble("CenterZ", center.z);
        playerSettings.setInteger("Dimension", dimension);

        // Read full settings from NBT
        NBTTagCompound mainCompound = new NBTTagCompound();
        try {
            mainCompound = NBTUtil.readNBT(ModConfig.SETTINGS_FILE);
        } catch (IOException e) {
            File file = new File(ModConfig.SETTINGS_FILE);
            try {
                file.createNewFile();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }

        // Write new settings
        mainCompound.setTag(troop.getMasterName(), playerSettings);
        try {
            NBTUtil.writeNBT(ModConfig.SETTINGS_FILE, mainCompound);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void readFromNBT()
    {
        NBTTagCompound compound = null;
        try {
            compound = NBTUtil.readNBT(ModConfig.SETTINGS_FILE);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        // Read player settings
        compound = compound.getCompoundTag(troop.getMasterName());
        if (compound.hasKey("Behaviour"))
            behaviour = Behaviour.get(compound.getByte("Behaviour"));
        if (compound.hasKey("Color"))
            color = compound.getInteger("Color");
        if (compound.hasKey("DisplayName"))
            displayName = compound.getBoolean("DisplayName");
        if (compound.hasKey("Follow"))
            follow = compound.getBoolean("Follow");
        if (compound.hasKey("Formation"))
            formation = Formation.get(compound.getByte("Formation"));
        if (compound.hasKey("NameFormat"))
            nameFormat = compound.getString("NameFormat");
        if (compound.hasKey("Size"))
            size = compound.getDouble("Size");

        if (compound.hasKey("CenterX") && compound.hasKey("CenterY") && compound.hasKey("CenterZ"))
            center = new Vec3d(compound.getDouble("CenterX"), compound.getDouble("CenterY"),
                    compound.getDouble("CenterZ"));
        if (compound.hasKey("Dimension"))
            dimension = compound.getInteger("Dimension");
    }

    @Override
    public String toString()
    {
        return "follow=" + follow + " color=" + color + " displayName=" + displayName + " formation="
                + formation.getText() + " nameFormat='" + nameFormat + "' size=" + size + " behaviour="
                + behaviour.getText() + " center=" + center + " dimension=" + dimension;
    }
}
