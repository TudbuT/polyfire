package tudbut.mod.polyfire.utils;

import net.minecraft.block.Block;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Container;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import tudbut.mod.polyfire.PolyFire;
import tudbut.tools.Lock;

import java.util.Objects;

public class InventoryUtils {
    
    public static final int OFFHAND_SLOT = 45;
    private static final Lock swapLock = new Lock();
    
    public static Integer getSlotWithItem(Container inv, Item item, int amount) {
        return getSlotWithItem(inv, item, Utils.range(0, 8), amount, amount);
    }
    
    public static Integer getSlotWithItem(Container inv, Item item, int[] not, int amountMin, int amountMax) {
        for (int i = 0; i < inv.getInventory().size(); i++) {
            a:
            {
                for (int j = 0; j < not.length; j++) {
                    if (i == not[j])
                        break a;
                }
                
                ItemStack stack = inv.getSlot(i).getStack();
                if (stack.getItem().equals(item) && stack.getCount() >= amountMin && stack.getCount() <= amountMax)
                    return i;
            }
        }
        return null;
    }
    public static Integer getSlotWithItem(Container inv, Block item, int amount) {
        return getSlotWithItem(inv, item, Utils.range(0, 8), amount, amount);
    }
    
    public static Integer getSlotWithItem(Container inv, Block item, int[] not, int amountMin, int amountMax) {
        for (int i = 0; i < inv.getInventory().size(); i++) {
            a:
            {
                for (int j = 0; j < not.length; j++) {
                    if (i == not[j])
                        break a;
                }
                
                ItemStack stack = inv.getSlot(i).getStack();
                if(stack.getItem().getRegistryName() != null)
                    if (stack.getItem().getRegistryName().toString().equals(Objects.requireNonNull(item.getRegistryName()).toString()) && stack.getCount() >= amountMin && stack.getCount() <= amountMax)
                        return i;
            }
        }
        return null;
    }
    
    public static int getItemAmount(Container inv, Item item) {
        int c = 0;
        for (int i = 0; i < inv.getInventory().size(); i++) {
            ItemStack stack = inv.getSlot(i).getStack();
            if (stack.getItem().equals(item))
                c += stack.getCount();
            
        }
        return c;
    }
    
    // Select hotbar slot
    public static void setCurrentSlot(int id) {
        if(PolyFire.player.inventory.currentItem != id) {
            PolyFire.player.inventory.currentItem = id;
            PolyFire.player.connection.sendPacket(new CPacketHeldItemChange(id));
        }
    }
    
    // Get selected hotbar slot
    public static int getCurrentSlot() {
        return PolyFire.player.inventory.currentItem;
    }
    
    // Drop contents of a slot
    public static void drop(int slot) {
        clickSlot(slot, ClickType.THROW, 1);
    }
    
    // Virtually clicks a slot
    public static void clickSlot(int slot, ClickType type, int key) {
        PolyFire.mc.playerController.windowClick(PolyFire.mc.player.inventoryContainer.windowId, slot, key, type, PolyFire.mc.player);
    }
    
    // This only swaps between a slot and a hotbar slot!
    public static void swap(int slot, int hotbarSlot) {
        clickSlot(slot, ClickType.SWAP, hotbarSlot);
    }
    
    // Swap two items in inventory
    public static void inventorySwap(int slot0, int slot1, long mainDelay, long postDelay, long cooldownDelay) {
        // Swapping may not be done in separate threads!
        swapLock.waitHere();
        
        // Make other threads wait
        swapLock.lock();
        
        // "slot1" must not be set to 8, it will not be able to switch!
        if (slot1 == 8 + 36) {
            // Exchange values of slot0 and slot1
            int i = slot0;
            slot1 = slot0;
            slot0 = i;
        }

        try {
            // Check for a GUIScreen that would block switching
            GuiScreen screen = PolyFire.mc.currentScreen;
            boolean doResetScreen = false;
            if (screen instanceof GuiContainer && !(screen instanceof GuiInventory)) {
                // If the current GUIScreen blocks switching, close it
                PolyFire.player.closeScreen();
                Thread.sleep(500);
                doResetScreen = true;
            }

            swap(slot0, 8);
            Thread.sleep(mainDelay);
            swap(slot1, 8);
            Thread.sleep(postDelay);
            swap(slot0, 8);
            Thread.sleep(cooldownDelay);
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        // Enable the next swapping operation
        swapLock.unlock();
    }
}
