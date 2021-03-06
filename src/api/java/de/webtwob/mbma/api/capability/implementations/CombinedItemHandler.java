package de.webtwob.mbma.api.capability.implementations;

import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * Created by BB20101997 on 17. Mär. 2017.
 */
public class CombinedItemHandler extends ItemHandlerWrapper {

    private final ItemStackHandler[] itemHandlers;

    public CombinedItemHandler(ItemStackHandler... handler) {
        itemHandlers = handler;
    }

    public CombinedItemHandler(List<NonNullList<ItemStack>> stacks) {
        ItemStackHandler[] handlers = new ItemStackHandler[stacks.size()];
        for (int i = 0; i < handlers.length; i++) {
            handlers[i] = new ItemStackHandler(stacks.get(i));
        }
        itemHandlers = handlers;
    }


    private ItemStackHandler getItemHandlerForSlot(int slot) {
        for (ItemStackHandler handler : itemHandlers) {
            if (slot < handler.getSlots()) {
                return handler;
            }
            slot -= handler.getSlots();
        }
        return null;
    }

    private int getHandlerOffset(IItemHandler handler) {
        int sum = 0;
        for (IItemHandler handler2 : itemHandlers) {
            if (handler == handler2) return sum;
            sum += handler2.getSlots();
        }
        return -1;
    }

    @Override
    public void setSize(int size) {
    }

    @Override
    public int getSlots() {
        int sum = 0;
        for (IItemHandler handler : itemHandlers) {
            sum += handler.getSlots();
        }
        return sum;
    }

    @Nonnull
    @Override
    public ItemStack getStackInSlot(int slot) {
        IItemHandler handler = getItemHandlerForSlot(slot);
        int offset = getHandlerOffset(handler);
        if (offset == -1 || handler == null) return ItemStack.EMPTY;
        return handler.getStackInSlot(slot - offset);
    }

    @Nonnull
    @Override
    public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
        IItemHandler handler = getItemHandlerForSlot(slot);
        int offset = getHandlerOffset(handler);
        if (offset == -1 || handler == null) return stack;
        return handler.insertItem(slot - offset, stack, simulate);
    }

    @Nonnull
    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        IItemHandler handler = getItemHandlerForSlot(slot);
        int offset = getHandlerOffset(handler);
        if (offset == -1 || handler == null) return ItemStack.EMPTY;
        return handler.extractItem(slot - offset, amount, simulate);
    }

    @Override
    public void setStackInSlot(int slot, @Nonnull ItemStack stack) {
        ItemStackHandler handler = getItemHandlerForSlot(slot);
        int offset = getHandlerOffset(handler);
        if (handler == null || offset == -1) return;
        handler.setStackInSlot(slot - offset, stack);
    }

    @Override
    public int getSlotLimit(int slot) {
        IItemHandler handler = getItemHandlerForSlot(slot);
        int offset = getHandlerOffset(handler);
        if (offset == -1 || handler == null) return 0;
        return handler.getSlotLimit(slot - offset);
    }
}
