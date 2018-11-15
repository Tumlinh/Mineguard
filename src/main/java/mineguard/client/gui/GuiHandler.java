package mineguard.client.gui;

import mineguard.client.gui.inventory.GuiScreenGuardPanel;
import mineguard.entity.EntityGuard;
import mineguard.inventory.ContainerGuardInventory;
import mineguard.util.EntityUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

public class GuiHandler implements IGuiHandler
{
    public enum GUI_ENUM
    {
        GUARD_PANEL
    }

    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z)
    {
        if (ID == GUI_ENUM.GUARD_PANEL.ordinal()) {
            EntityGuard guard = EntityUtil.getInteractionTarget();
            if (guard != null)
                return new ContainerGuardInventory(player.inventory, guard);
        }

        return null;
    }

    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z)
    {
        if (ID == GUI_ENUM.GUARD_PANEL.ordinal()) {
            EntityGuard guard = EntityUtil.getInteractionTarget();
            if (guard != null)
                return new GuiScreenGuardPanel(player.inventory, guard);
        }

        return null;
    }
}
