package mineguard.inventory;

import javax.annotation.Nullable;
import mineguard.entity.EntityGuard;
import mineguard.handler.TextureRegister;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ContainerGuardInventory extends Container
{
    public static final EntityEquipmentSlot[] EQUIPMENT_ARMOR_SLOTS = new EntityEquipmentSlot[] {
            EntityEquipmentSlot.HEAD, EntityEquipmentSlot.CHEST, EntityEquipmentSlot.LEGS, EntityEquipmentSlot.FEET };
    public static final EntityEquipmentSlot[] EQUIPMENT_HAND_SLOTS = new EntityEquipmentSlot[] {
            EntityEquipmentSlot.MAINHAND, EntityEquipmentSlot.OFFHAND };
    private static final String[] EMPTY_HAND_SLOT_NAMES = { TextureRegister.EMPTY_ARMOR_SLOT_MAINHAND_NAME,
            "minecraft:items/empty_armor_slot_shield" };

    private EntityGuard guard;
    private IInventory guardInventory;

    public ContainerGuardInventory(IInventory playerInventory, EntityGuard guard)
    {
        this.guard = guard;

        // Setup guard inventory
        guardInventory = new InventoryBasic("GuardInventory", false, 6);
        int i = 0;
        for (EntityEquipmentSlot slot : EQUIPMENT_HAND_SLOTS)
            guardInventory.setInventorySlotContents(i++, guard.getItemStackFromSlot(slot));
        for (EntityEquipmentSlot slot : EQUIPMENT_ARMOR_SLOTS)
            guardInventory.setInventorySlotContents(i++, guard.getItemStackFromSlot(slot));

        // Add slots for guard equipment (hand)
        for (i = 0; i < 2; i++) {
            final EntityEquipmentSlot slot = EQUIPMENT_HAND_SLOTS[i];
            this.addSlotToContainer(new Slot(guardInventory, i, 77, 44 + i * 18)
            {
                @Nullable
                @SideOnly(Side.CLIENT)
                public String getSlotTexture()
                {
                    return EMPTY_HAND_SLOT_NAMES[slot.getIndex()];
                }
            });
        }

        // Add slots for guard equipment (armor)
        for (int j = 0; j < 4; j++) {
            final EntityEquipmentSlot slot = EQUIPMENT_ARMOR_SLOTS[j];
            this.addSlotToContainer(new Slot(guardInventory, j + i, 8, 8 + j * 18)
            {
                public int getSlotStackLimit()
                {
                    return 1;
                }

                public boolean isItemValid(ItemStack stack)
                {
                    return slot == EntityLiving.getSlotForItemStack(stack);
                }

                @Override
                public boolean canTakeStack(EntityPlayer playerIn)
                {
                    // Forbid removing the helmet
                    return slot != EntityEquipmentSlot.HEAD;
                }

                @Nullable
                @SideOnly(Side.CLIENT)
                public String getSlotTexture()
                {
                    return ItemArmor.EMPTY_SLOT_NAMES[slot.getIndex()];
                }
            });
        }

        // Insert player inventory
        for (int j = 0; j < 3; j++) {
            for (int k = 0; k < 9; k++) {
                this.addSlotToContainer(new Slot(playerInventory, k + (j + 1) * 9, 8 + k * 18, 84 + j * 18));
            }
        }

        // Insert player inventory (main bar)
        for (int j = 0; j < 9; j++) {
            this.addSlotToContainer(new Slot(playerInventory, j, 8 + j * 18, 142));
        }
    }

    @Override
    public void detectAndSendChanges()
    {
        super.detectAndSendChanges();

        // Sync guard equipment
        int i = 0;
        for (EntityEquipmentSlot slot : EQUIPMENT_HAND_SLOTS)
            guard.setItemStackToSlot(slot, guardInventory.getStackInSlot(i++));
        for (EntityEquipmentSlot slot : EQUIPMENT_ARMOR_SLOTS)
            guard.setItemStackToSlot(slot, guardInventory.getStackInSlot(i++));
    }

    @Override
    public boolean canInteractWith(EntityPlayer playerIn)
    {
        return guard.canInteractWith(playerIn);
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer playerIn, int index)
    {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.inventorySlots.get(index);

        if (slot != null && slot.getHasStack()) {
            ItemStack itemstack1 = slot.getStack();
            itemstack = itemstack1.copy();
            EntityEquipmentSlot entityequipmentslot = EntityLiving.getSlotForItemStack(itemstack);

            // Move equipment from guard inventory to player
            if (index >= 0 && index <= 5) {
                if (!this.mergeItemStack(itemstack1, 6, 42, false))
                    return ItemStack.EMPTY;
            }

            // Move armor equipment from player inventory to guard's
            else if (entityequipmentslot.getSlotType() == EntityEquipmentSlot.Type.ARMOR
                    && this.isEmpty(5 - entityequipmentslot.getIndex())) {
                int i = 5 - entityequipmentslot.getIndex();
                if (!this.mergeItemStack(itemstack1, i, i + 1, false))
                    return ItemStack.EMPTY;
            }

            // Move off-hand item (usually a shield) to guard's inventory
            else if (entityequipmentslot == EntityEquipmentSlot.OFFHAND && (this.isEmpty(0) || this.isEmpty(1))) {
                if (!this.mergeItemStack(itemstack1, 0, 2, true))
                    return ItemStack.EMPTY;
            }

            // Move main-hand item (can be anything) to guard's inventory
            else if (entityequipmentslot == EntityEquipmentSlot.MAINHAND && (this.isEmpty(0) || this.isEmpty(1))) {
                if (!this.mergeItemStack(itemstack1, 0, 2, false))
                    return ItemStack.EMPTY;
            }

            // Move any item from player's large inventory to main inventory
            else if (index >= 6 && index < 33) {
                if (!this.mergeItemStack(itemstack1, 33, 42, false))
                    return ItemStack.EMPTY;
            }

            // Move any item from player's main inventory to large inventory
            else if (index >= 33 && index < 42) {
                if (!this.mergeItemStack(itemstack1, 6, 33, false))
                    return ItemStack.EMPTY;
            }

            else if (!this.mergeItemStack(itemstack1, 6, 42, false)) {
                return ItemStack.EMPTY;
            }

            if (itemstack1.isEmpty())
                slot.putStack(ItemStack.EMPTY);
            else
                slot.onSlotChanged();

            if (itemstack1.getCount() == itemstack.getCount())
                return ItemStack.EMPTY;

            ItemStack itemstack2 = slot.onTake(playerIn, itemstack1);
            if (index == 0)
                playerIn.dropItem(itemstack2, false);
        }

        return itemstack;
    }

    @Override
    public void onContainerClosed(EntityPlayer playerIn)
    {
        guard.setContainerOpen(false);
    }

    private boolean isEmpty(int slotIndex)
    {
        return !((Slot) this.inventorySlots.get(slotIndex)).getHasStack();
    }

    public String toString()
    {
        String str = "";
        for (Slot slot : this.inventorySlots)
            str += slot.getStack() + " ";
        return str;
    }
}
