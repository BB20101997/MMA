package de.webtwob.mbma.core.common.block;

import de.webtwob.mbma.core.common.creativetab.MBMACreativeTab;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;

/**
 * Created by BB20101997 on 06. Nov. 2017.
 */
public class BlockPatternStorage extends Block {
    
    public BlockPatternStorage(){
        super(Material.IRON);
        
        setCreativeTab(MBMACreativeTab.MBMATab);
    }
}