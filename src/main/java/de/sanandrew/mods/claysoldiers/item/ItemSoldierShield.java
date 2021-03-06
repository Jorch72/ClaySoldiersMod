/* ******************************************************************************************************************
   * Authors:   SanAndreasP
   * Copyright: SanAndreasP
   * License:   Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International
   *                http://creativecommons.org/licenses/by-nc-sa/4.0/
   *******************************************************************************************************************/
package de.sanandrew.mods.claysoldiers.item;

import de.sanandrew.mods.claysoldiers.api.CsmConstants;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

public class ItemSoldierShield
        extends Item
{
    public ItemSoldierShield() {
        super();
        this.setUnlocalizedName(CsmConstants.ID + ":shield");
        this.setMaxDamage(0);
        this.setHasSubtypes(true);
        this.setRegistryName(CsmConstants.ID, "shield");
    }

    @Override
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) { }

    @Override
    protected boolean isInCreativeTab(CreativeTabs targetTab) {
        return false;
    }
}
