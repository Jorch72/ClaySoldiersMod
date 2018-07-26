/* ******************************************************************************************************************
   * Authors:   SanAndreasP
   * Copyright: SanAndreasP
   * License:   Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International
   *                http://creativecommons.org/licenses/by-nc-sa/4.0/
   *******************************************************************************************************************/
package de.sanandrew.mods.claysoldiers.entity.ai;

import de.sanandrew.mods.claysoldiers.api.doll.ItemDoll;
import de.sanandrew.mods.claysoldiers.api.entity.mount.IMount;
import de.sanandrew.mods.claysoldiers.api.entity.soldier.ISoldier;
import de.sanandrew.mods.claysoldiers.api.entity.soldier.upgrade.EnumUpgradeType;
import de.sanandrew.mods.claysoldiers.api.entity.soldier.upgrade.ISoldierUpgrade;
import de.sanandrew.mods.claysoldiers.entity.soldier.EntityClaySoldier;
import de.sanandrew.mods.claysoldiers.registry.ItemRegistry;
import de.sanandrew.mods.claysoldiers.registry.upgrade.UpgradeRegistry;
import de.sanandrew.mods.claysoldiers.registry.upgrade.Upgrades;
import de.sanandrew.mods.sanlib.lib.util.ItemStackUtils;
import de.sanandrew.mods.sanlib.lib.util.MiscUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.AxisAlignedBB;

import java.util.List;

public abstract class EntityAISearchTarget<T extends Entity>
        extends EntityAIBase
{
    final EntityClaySoldier taskOwner;
    final Class<T> toScanEntityType;

    T target;

    EntityAISearchTarget(EntityClaySoldier soldier, Class<T> toScanEntityType) {
        this.taskOwner = soldier;
        this.toScanEntityType = toScanEntityType;
        this.setMutexBits(MutexBits.TARGETING);
    }

    @Override
    public boolean shouldExecute() {
        if( this.hasTarget() ) {
            return false;
        }

        List<T> list = this.taskOwner.world.getEntitiesWithinAABB(this.toScanEntityType, getTargetableArea(this.taskOwner), this::canFollow);

        if( list.isEmpty() ) {
            return false;
        } else {
            this.target = list.get(MiscUtils.RNG.randomInt(list.size()));
            return true;
        }
    }

    boolean hasTarget() {
        return hasFollowTarget(this.taskOwner);
    }

    void setTarget(T target) {
        this.taskOwner.followingEntity = target;
    }

    abstract boolean canFollow(T entity);

    @Override
    public void resetTask() {
        this.target = null;
    }

    @Override
    public void updateTask() {
        if( this.target != null ) {
            this.setTarget(this.target);
        }
    }

    static boolean hasFollowTarget(EntityClaySoldier soldier) {
        return (soldier.followingEntity != null && soldier.followingEntity.isEntityAlive())
                       || (soldier.followingBlock != null && soldier.world.isBlockLoaded(soldier.followingBlock) && soldier.world.getChunkFromBlockCoords(soldier.followingBlock).isLoaded());
    }

    static AxisAlignedBB getTargetableArea(EntityClaySoldier soldier) {
        return soldier.getEntityBoundingBox().grow(soldier.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).getAttributeValue());
    }

    public static class Mount
            extends EntityAISearchTarget<EntityLivingBase>
    {
        public Mount(EntityClaySoldier soldier) {
            super(soldier, EntityLivingBase.class);
        }

        @Override
        boolean canFollow(EntityLivingBase entity) {
            return !this.taskOwner.isRiding() && entity instanceof IMount && entity.isEntityAlive() && this.taskOwner.canEntityBeSeen(entity)
                           && !this.taskOwner.hasUpgrade(Upgrades.MH_BONE, EnumUpgradeType.MAIN_HAND)
                           && ((IMount) entity).getMaxPassengers() > entity.getPassengers().size();
        }
    }

    public static class King
            extends EntityAISearchTarget<EntityLivingBase>
    {
        public King(EntityClaySoldier soldier) {
            super(soldier, EntityLivingBase.class);
        }

        @Override
        boolean canFollow(EntityLivingBase entity) {
            return this.isKingOfGroup(entity) && entity.isEntityAlive() && this.taskOwner.canEntityBeSeen(entity) && entity.getDistanceSq(this.taskOwner) > 1.0D;
        }

        private boolean isKingOfGroup(Entity e) {
            if( e instanceof ISoldier && e != this.taskOwner ) {
                ISoldier soldier = (ISoldier) e;
                return soldier.getSoldierTeam() == this.taskOwner.getSoldierTeam() && soldier.hasUpgrade(Upgrades.MC_GOLDNUGGET, EnumUpgradeType.MISC);
            }

            return false;
        }
    }

    public static class Fallen
            extends EntityAISearchTarget<EntityItem>
    {
        public Fallen(EntityClaySoldier soldier) {
            super(soldier, EntityItem.class);
        }

        @Override
        boolean canFollow(EntityItem entity) {
            return entity != null && entity.isEntityAlive() && !entity.cannotPickup()
                           && this.isValidItem(entity.getItem())
                           && this.taskOwner.canEntityBeSeen(entity);
        }

        private boolean isValidItem(ItemStack stack) {
            return (this.taskOwner.hasUpgrade(Upgrades.MC_CLAY, EnumUpgradeType.MISC) && stack.getItem() instanceof ItemDoll
                                && ((ItemDoll<?, ?>) stack.getItem()).canBeResurrected(stack, this.taskOwner))
                           || (this.taskOwner.hasUpgrade(Upgrades.MC_GHASTTEAR, EnumUpgradeType.MISC) && ItemStackUtils.isItem(stack, ItemRegistry.DOLL_BRICK_SOLDIER));
        }
    }

    public static class Upgrade
            extends EntityAISearchTarget<EntityItem>
    {
        public Upgrade(EntityClaySoldier soldier) {
            super(soldier, EntityItem.class);
        }

        @Override
        boolean canFollow(EntityItem entity) {
            if( entity != null && entity.isEntityAlive() && !entity.cannotPickup() ) {
                ISoldierUpgrade upgrade = UpgradeRegistry.INSTANCE.getUpgrade(entity.getItem());
                return upgrade != null && this.taskOwner.canEntityBeSeen(entity) && this.taskOwner.canPickupUpgrade(upgrade, entity.getItem());
            }

            return false;
        }
    }

    public static class Enemy
            extends EntityAISearchTarget<EntityLivingBase>
    {
        public Enemy(EntityClaySoldier soldier) {
            super(soldier, EntityLivingBase.class);
        }

        @Override
        boolean canFollow(EntityLivingBase entity) {
            return this.taskOwner.isEnemyValid(entity);
        }

        @Override
        boolean hasTarget() {
            return this.taskOwner.getAttackTarget() != null && this.taskOwner.getAttackTarget().isEntityAlive();
        }

        @Override
        void setTarget(EntityLivingBase target) {
            this.taskOwner.setAttackTarget(target);
        }
    }
}
