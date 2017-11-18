package de.webtwob.mbma.core.common.tileentity;

import de.webtwob.mbma.api.crafting.ItemStackContainer;
import de.webtwob.mbma.api.interfaces.capability.IBlockPosProvider;
import de.webtwob.mbma.api.interfaces.tileentity.IItemMoveRequest;
import de.webtwob.mbma.api.property.MBMAProperties;
import de.webtwob.mbma.api.registries.MultiBlockGroupType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static net.minecraftforge.fml.common.registry.GameRegistry.ObjectHolder;

/**
 * Created by BB20101997 on 25. Okt. 2017.
 */
public class TileEntityStorageIndexer extends MultiBlockTileEntity {
    
    @ObjectHolder("mbmacore:storage")
    private static final MultiBlockGroupType          MANAGER_STORAGE       = null;
    private static       Capability<IItemHandler>     capabilityItemHandler = null;
    private              LinkedList<IItemMoveRequest> requests              = new LinkedList<>();
    private              NonNullList<ItemStack>       storageLinks          = NonNullList.create();
    
    @CapabilityInject(IItemHandler.class)
    private static void setCapabilityItemHandler(Capability<IItemHandler> itemHandlerCapability) {
        capabilityItemHandler = itemHandlerCapability;
    }
    
    @Override
    public MultiBlockGroupType getGroupType() {
        return MANAGER_STORAGE;
    }
    
    @Override
    public void update() {
        super.update();
        
        if(!requests.isEmpty()) {
            IItemMoveRequest request = requests.pollFirst();
            switch(request.getType()){
                
                case REQUEST_ITEMS:
                    handleRequest(request);
                    break;
                case DEPOSIT_ITEMS:
                    handleDeposit(request);
                    break;
                default:
                    //for future cases we don't do anything yet
            }
            request.passOnRequest();
        }
        
    }
    
    private void handleDeposit(final IItemMoveRequest request) {
        ItemStackContainer container = request.getItemContainer();
        for(IItemHandler handler : getInventories()){
            container.setItemStack(ItemHandlerHelper.insertItem(handler, container.getItemStack(), false));
            if(container.getItemStack().isEmpty()) {
                return;
            }
        }
    }
    
    private void handleRequest(final IItemMoveRequest request) {
        ItemStackContainer container = request.getItemContainer();
        for(IItemHandler handler : getInventories()){
            for(int i = 0; i < handler.getSlots(); i++){
                ItemStack stackInSlot = handler.getStackInSlot(i);
                ItemStack inContainer = container.getItemStack();
                if(stackInSlot.isItemEqual(request.getRequest()) && (inContainer.isEmpty()||ItemHandlerHelper.canItemStacksStack(stackInSlot,inContainer))){
                    if(!inContainer.isEmpty()){
                        int spaceLeft = inContainer.getMaxStackSize()-inContainer.getCount();
                        inContainer.grow(handler.extractItem(i,spaceLeft,false).getCount());
                    }else{
                        container.setItemStack(inContainer = handler.extractItem(i,request.getRequest().getMaxStackSize(),false));
                    }
                }
                if(inContainer.getCount()>=inContainer.getMaxStackSize()){
                    return;
                }
            }
        }
    }
    
    private List<IItemHandler> getInventories() {
        List<IItemHandler> handlerList = new ArrayList<>();
        if(capabilityItemHandler == null) {
            return handlerList;
        }
        for(ItemStack stack : storageLinks){
            BlockPos pos = IBlockPosProvider.getBlockPos(stack);
            if(pos != null) {
                IBlockState state = world.getBlockState(pos);
                if(state.getPropertyKeys().contains(MBMAProperties.CONNECTED) && state.getValue(
                        MBMAProperties.CONNECTED)) {
                    if(state.getPropertyKeys().contains(MBMAProperties.FACING)) {
                        EnumFacing facing = state.getValue(MBMAProperties.FACING);
                        TileEntity te     = world.getTileEntity(pos.offset(facing));
                        if(te != null) {
                            IItemHandler handler = te.getCapability(capabilityItemHandler, facing.getOpposite());
                            if(handler != null) {
                                handlerList.add(handler);
                            }
                        }
                    }
                }
            }
        }
        return handlerList;
    }
    
    /**
     * Adds a IItemMoveRequest to the queue of requests to be handled
     * Requests are handled on this TileEntity update Method being called  usually every Tick
     *
     * @param request the request to add
     */
    public void addItemMoveRequest(IItemMoveRequest request) {
        requests.add(request);
    }
    
}
