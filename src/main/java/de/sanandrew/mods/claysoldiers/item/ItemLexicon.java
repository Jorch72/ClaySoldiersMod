/* ******************************************************************************************************************
   * Authors:   SanAndreasP
   * Copyright: SanAndreasP
   * License:   Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International
   *                http://creativecommons.org/licenses/by-nc-sa/4.0/
   *******************************************************************************************************************/
package de.sanandrew.mods.claysoldiers.item;

import de.sanandrew.mods.claysoldiers.api.CsmConstants;
import de.sanandrew.mods.claysoldiers.util.ClaySoldiersMod;
import de.sanandrew.mods.claysoldiers.util.CsmCreativeTabs;
import de.sanandrew.mods.claysoldiers.util.GuiHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.internal.FMLNetworkHandler;

public class ItemLexicon
        extends Item
{
    public ItemLexicon() {
        super();
        this.setCreativeTab(CsmCreativeTabs.MISC);
        this.setUnlocalizedName(CsmConstants.ID + ":lexicon");
        this.setMaxDamage(0);
        this.setRegistryName(CsmConstants.ID, "lexicon");
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
        if( worldIn.isRemote ) {
            FMLNetworkHandler.openGui(playerIn, ClaySoldiersMod.instance, GuiHandler.GUI_LEXICON, worldIn, 0, 0, 0);
        }

        return super.onItemRightClick(worldIn, playerIn, handIn);
    }
}
