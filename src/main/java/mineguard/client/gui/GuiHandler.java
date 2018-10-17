package mineguard.client.gui;

import mineguard.client.gui.inventory.GuiScreenBodyguardPanel;
import mineguard.entity.EntityBodyguard;
import mineguard.inventory.ContainerBodyguardInventory;
import mineguard.util.EntityUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

public class GuiHandler implements IGuiHandler
{
    public enum GUI_ENUM
    {
        BODYGUARD_PANEL
    }

    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z)
    {
        if (ID == GUI_ENUM.BODYGUARD_PANEL.ordinal()) {
            EntityBodyguard bodyguard = EntityUtil.getInteractionTarget();
            if (bodyguard != null)
                return new ContainerBodyguardInventory(player.inventory, bodyguard);
        }

        return null;
    }

    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z)
    {
        if (ID == GUI_ENUM.BODYGUARD_PANEL.ordinal()) {
            EntityBodyguard bodyguard = EntityUtil.getInteractionTarget();
            if (bodyguard != null)
                return new GuiScreenBodyguardPanel(player.inventory, bodyguard);
        }

        return null;
    }
}
