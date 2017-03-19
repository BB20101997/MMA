package de.webtwob.mbma.api.capability.factory;

import de.webtwob.mbma.api.capability.interfaces.ICraftingRequest;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.concurrent.Callable;

/**
 * Created by BB20101997 on 19. Mär. 2017.
 */
public class CraftingRequestFactory implements Callable<ICraftingRequest> {

    @Override
    public ICraftingRequest call() throws Exception {
        return new ICraftingRequest() {

            private ItemStack stack = ItemStack.EMPTY;

            @Nonnull
            @Override
            public ItemStack getRequest() {
                return stack;
            }

            @Override
            public void setRequest(@Nonnull ItemStack itemStack) {
                stack = itemStack;
            }
        };
    }
}