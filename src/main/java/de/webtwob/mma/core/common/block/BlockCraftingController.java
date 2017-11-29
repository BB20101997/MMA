package de.webtwob.mma.core.common.block;

import de.webtwob.mma.api.enums.MachineState;
import de.webtwob.mma.api.interfaces.tileentity.IMultiBlockTile;
import de.webtwob.mma.api.property.MMAProperties;
import de.webtwob.mma.core.common.creativetab.MMACreativeTab;
import de.webtwob.mma.core.common.tileentity.TileEntityCraftingController;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Created by BB20101997 on 28. Okt. 2017.
 */
public class BlockCraftingController extends Block {
    
    public BlockCraftingController() {
        super(Material.IRON);
        setCreativeTab(MMACreativeTab.MMATab);
        
        IBlockState state = blockState.getBaseState();
        state = state.withProperty(MMAProperties.STATE, MachineState.IDLE);
        setDefaultState(state);
    }
    
    @Override
    public int getMetaFromState(IBlockState state) {
        return 0;
    }
    
    @Nonnull
    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, MMAProperties.STATE);
    }
    
    @Override
    public boolean hasTileEntity(IBlockState state) {
        return true;
    }
    
    @Nullable
    @Override
    public TileEntity createTileEntity(@Nonnull World world, @Nonnull IBlockState state) {
        return new TileEntityCraftingController();
    }
    
    /**
     * @deprecated
     */
    @Nonnull
    @SuppressWarnings("deprecation")
    @Deprecated
    @Override
    public IBlockState getActualState(@Nonnull IBlockState state, IBlockAccess worldIn, BlockPos pos) {
        return getExtendedState(state, worldIn, pos);
    }
    
    @Nonnull
    @Override
    public IBlockState getExtendedState(@Nonnull IBlockState state, IBlockAccess world, BlockPos pos) {
        TileEntity tileEntity = world.getTileEntity(pos);
        if (tileEntity instanceof TileEntityCraftingController) {
            return state.withProperty(MMAProperties.STATE, ((TileEntityCraftingController) tileEntity).getState());
        }
        return state.withProperty(MMAProperties.STATE, MachineState.PROBLEM);
    }
    
    @Override
    public void breakBlock(@Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull IBlockState state) {
        TileEntity tileEntity = worldIn.getTileEntity(pos);
        
        if (tileEntity instanceof IMultiBlockTile) {
            ((IMultiBlockTile) tileEntity).onBlockBreak(worldIn, pos);
        }
        
        super.breakBlock(worldIn, pos, state);
    }
}