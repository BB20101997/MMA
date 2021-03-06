package de.webtwob.mbma.api.interfaces.capability;

import de.webtwob.mbma.api.capability.APICapabilities;

import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;

/**
 * Created by BB20101997 on 19. Mär. 2017.
 */
public interface ICraftingRequest {
    
    static ICraftingRequest getCraftingRequest(ItemStack stack) {
        return !(stack == null || stack.isEmpty()) ?
                stack.getCapability(APICapabilities.CAPABILITY_CRAFTING_REQUEST, null) : null;
    }
    
    @Nonnull
    ItemStack getRequest();
    
    void setRequest(@Nonnull ItemStack stack);
    
    default boolean isCompleted() {
        return getRequest().isEmpty() || getQuantity() <= 0;
    }
    
    int getQuantity();
    
    void setQuantity(int amount);
    
    default void reduceQuantity(int i) {
        setQuantity(getQuantity() - i);
    }
    
}
