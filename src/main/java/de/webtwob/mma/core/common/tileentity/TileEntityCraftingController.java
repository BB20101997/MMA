package de.webtwob.mma.core.common.tileentity;

import de.webtwob.mma.api.enums.MachineState;
import de.webtwob.mma.api.interfaces.capability.IBlockPosProvider;
import de.webtwob.mma.api.interfaces.capability.ICraftingRecipe;
import de.webtwob.mma.api.interfaces.capability.ICraftingRequest;
import de.webtwob.mma.api.interfaces.capability.ICraftingRequestProvider;
import de.webtwob.mma.api.registries.MultiBlockGroupType;

import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.fml.common.registry.GameRegistry.ObjectHolder;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by BB20101997 on 25. Okt. 2017.
 */
public class TileEntityCraftingController extends MultiBlockTileEntity {
    
    @ObjectHolder("mmacore:crafting")
    public static final MultiBlockGroupType MANAGER_CRAFTING = null;
    private static final int WAIT_TIME = 20;
    private static Capability<ICraftingRecipe> capabilityRecipe = null;
    private static Capability<ICraftingRequestProvider> capabilityRequestProvider = null;
    private static Capability<ICraftingRequest> capabilityRequest = null;
    private final List<Function<TileEntityCraftingController, String>> ERROR_SOLVED = new LinkedList<>();
    private final List<Function<TileEntityCraftingController, String>> WAIT_CONDITION = new LinkedList<>();
    private final List<String> errors = new LinkedList<>();
    private final List<String> waiting = new LinkedList<>();
    @Nonnull
    private MachineState state = MachineState.IDLE;
    private int pause = WAIT_TIME;
    private NonNullList<ItemStack> queueLinkCards = NonNullList.create();
    private NonNullList<ItemStack> patternLinkCards = NonNullList.create();
    @Nonnull
    private ItemStack currentRequest = ItemStack.EMPTY;
    
    @CapabilityInject(ICraftingRecipe.class)
    private static void setCapabilityRecipe(Capability<ICraftingRecipe> recipeCapability) {
        capabilityRecipe = recipeCapability;
    }
    
    @CapabilityInject(ICraftingRequest.class)
    private static void setRequestCapability(Capability<ICraftingRequest> requestCapability) {
        capabilityRequest = requestCapability;
    }
    
    @CapabilityInject(ICraftingRequestProvider.class)
    private static void setRequestProviderCapability(Capability<ICraftingRequestProvider> requestProviderCapability) {
        capabilityRequestProvider = requestProviderCapability;
    }
    
    @Nonnull
    public MachineState getState() {
        return state;
    }
    
    private void setState(@Nonnull MachineState state) {
        this.state = state;
        markDirty();
    }
    
    @Override
    public MultiBlockGroupType getGroupType() {
        return MANAGER_CRAFTING;
    }
    
    @Override
    public void update() {
        super.update();
        if (pause <= 0) {
            switch (state) {
                case IDLE:
                    idle();
                    break;
                case RUNNING:
                    if (currentRequest.isEmpty()) {
                        //we don't have a task therefor we stop working
                        setState(MachineState.IDLE);
                        markDirty();
                        break;
                    }
                    //TODO
                    //continue active planning
                    break;
                case WAITING:
                    waitOrError(waiting, WAIT_CONDITION);
                    break;
                case PROBLEM:
                    waitOrError(errors, ERROR_SOLVED);
                    break;
            }
        } else {
            pause--;
        }
    }
    
    private void waitOrError(
            final List<String> descriptions, final List<Function<TileEntityCraftingController, String>> functions
    ) {
        descriptions.clear();
        //noinspection ResultOfMethodCallIgnored
        functions.stream()
                .map(e -> e.apply(this))
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(() -> descriptions));
        if (descriptions.isEmpty()) {
            functions.clear();
            setState(MachineState.IDLE);
        } else {
            pause = WAIT_TIME;
        }
    }
    
    /**
     * What to do in update when in the IDLE state
     */
    private void idle() {
        if (currentRequest.isEmpty()) {
            
            //do we have at least one linked queue
            if (!getRequestProviders().findAny().isPresent()) {
                ERROR_SOLVED.add(
                        t -> !t.getRequestProviders().findAny().isPresent() ? "mmacor:error.desc.noqueues" : null);
                setState(MachineState.PROBLEM);
                return;
            }
            
            //find and get first request
            if (getNewRequest()) {
                setState(MachineState.RUNNING);
                return;
            }
            pause = WAIT_TIME;
        } else {
            setState(MachineState.RUNNING);
        }
    }
    
    private boolean canHandleRequest(final ItemStack stack) {
        ICraftingRequest request = ICraftingRequest.getCraftingRequest(stack);
        //noinspection SimplifiableIfStatement
        if (request.isCompleted()) {
            return false;
        }
        return (patternLinkCards.stream()
                .map(IBlockPosProvider::getBlockPos)
                .filter(Objects::nonNull)
                .map(world::getTileEntity)
                .filter(TileEntityPatternStore.class::isInstance) //TODO not nice to have hardcoded PatternStore should be expandable
                .map(TileEntityPatternStore.class::cast)
                .map(TileEntityPatternStore::getPatternList)
                .flatMap(List::stream)
                .map(itemStack -> itemStack.getCapability(capabilityRecipe, null))
                .filter(Objects::nonNull)
                .anyMatch(recipe -> doesRecipeProduceProduct(recipe, stack)));
    }
    
    private boolean doesRecipeProduceProduct(ICraftingRecipe recipe, ItemStack stack) {
        return Arrays.stream(recipe.getOutputs()).anyMatch(itemStack -> itemStack.equals(stack));
    }
    
    private Stream<ICraftingRequestProvider> getRequestProviders() {
        if (capabilityRequestProvider == null) {
            return Stream.empty();
        }
        return queueLinkCards.stream()
                .map(IBlockPosProvider::getBlockPos)
                .filter(Objects::nonNull)
                .map(world::getTileEntity)
                .filter(Objects::nonNull)
                .map(te -> te.getCapability(capabilityRequestProvider, null))
                .filter(Objects::nonNull);
    }
    
    @SuppressWarnings("BooleanMethodNameMustStartWithQuestion")
    private boolean getNewRequest() {
        currentRequest = getRequestProviders().map(cap -> cap.getRequestIfRequirementHolds(this::canHandleRequest))
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(ItemStack.EMPTY);
        return !currentRequest.isEmpty();
    }
    
}