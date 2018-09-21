package mineguard.settings;

import java.io.File;
import java.io.IOException;
import mineguard.Troop;
import mineguard.init.ModConfig;
import mineguard.util.NBTUtil;
import net.minecraft.nbt.NBTTagCompound;

public class Settings
{
    private Troop troop;

    // Settings
    private Behaviour behaviour = Behaviour.DEFENSIVE;
    private boolean displayName = true;
    private boolean follow = true;
    private Formation formation = Formation.SQUARE;
    private String nameFormat = "Agent%d";
    private double size = 10.0;

    public Settings(Troop troop)
    {
        this.troop = troop;
        this.readFromNBT();
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
        if (follow)
            troop.reform();
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
        troop.reform();
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
        troop.reform();
    }

    public void writeToNBT()
    {
        // Build player settings
        NBTTagCompound playerSettings = new NBTTagCompound();
        playerSettings.setByte("Behaviour", (byte) Behaviour.get(behaviour.getText()).getId());
        playerSettings.setBoolean("DisplayName", displayName);
        playerSettings.setBoolean("Follow", follow);
        playerSettings.setByte("Formation", (byte) Formation.get(formation.getText()).getId());
        playerSettings.setString("NameFormat", nameFormat);
        playerSettings.setDouble("Size", size);

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
    }

    @Override
    public String toString()
    {
        return "follow=" + follow + " displayName=" + displayName + " formation=" + formation.getText()
                + " nameFormat='" + nameFormat + "' size=" + size + " behaviour=" + behaviour.getText();
    }
}
