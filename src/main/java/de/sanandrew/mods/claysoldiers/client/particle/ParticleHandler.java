/* ******************************************************************************************************************
   * Authors:   SanAndreasP
   * Copyright: SanAndreasP
   * License:   Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International
   *                http://creativecommons.org/licenses/by-nc-sa/4.0/
   *******************************************************************************************************************/
package de.sanandrew.mods.claysoldiers.client.particle;

import de.sanandrew.mods.claysoldiers.api.doll.IDollType;
import de.sanandrew.mods.claysoldiers.registry.mount.EnumGeckoType;
import de.sanandrew.mods.claysoldiers.registry.mount.EnumTurtleType;
import de.sanandrew.mods.claysoldiers.registry.mount.EnumWoolBunnyType;
import de.sanandrew.mods.claysoldiers.registry.team.TeamRegistry;
import de.sanandrew.mods.claysoldiers.registry.mount.EnumClayHorseType;
import de.sanandrew.mods.claysoldiers.util.EnumParticle;
import de.sanandrew.mods.sanlib.lib.Tuple;
import de.sanandrew.mods.sanlib.lib.util.MiscUtils;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.ParticleBlockDust;
import net.minecraft.client.particle.ParticleBreaking;
import net.minecraft.client.particle.ParticleCrit;
import net.minecraft.client.particle.ParticleHeart;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.EnumMap;
import java.util.UUID;

@SideOnly(Side.CLIENT)
@SuppressWarnings("unused")
public final class ParticleHandler
{
    private static final EnumMap<EnumParticle, ParticleFunc> PARTICLES = new EnumMap<>(EnumParticle.class);
    private static Minecraft mc;

    static {
        PARTICLES.put(EnumParticle.TEAM_BREAK, ParticleHandler::spawnBreakParticle);
        PARTICLES.put(EnumParticle.HORSE_BREAK, ParticleHandler::spawnBreakParticle);
        PARTICLES.put(EnumParticle.TURTLE_BREAK, ParticleHandler::spawnBreakParticle);
        PARTICLES.put(EnumParticle.BUNNY_BREAK, ParticleHandler::spawnBreakParticle);
        PARTICLES.put(EnumParticle.GECKO_BREAK, ParticleHandler::spawnBreakParticle);
        PARTICLES.put(EnumParticle.ITEM_BREAK, ParticleHandler::spawnItemParticle);
        PARTICLES.put(EnumParticle.CRITICAL, ParticleHandler::spawnCriticalParticle);
        PARTICLES.put(EnumParticle.HEARTS, ParticleHandler::spawnHealingParticle);
        PARTICLES.put(EnumParticle.SHOCKWAVE, ParticleHandler::spawnShockwaveParticle);
        PARTICLES.put(EnumParticle.FIREWORK, ParticleHandler::spawnFireworks);
    }

    public static void spawn(EnumParticle particle, int dim, double x, double y, double z, Object... additData) {
        if( mc == null ) {
            mc = Minecraft.getMinecraft();
        }
        if( mc.world != null ) {
            PARTICLES.get(particle).accept(particle, dim, x, y, z, new Tuple(additData));
        }
    }

    private static void spawnBreakParticle(EnumParticle particle, int dim, double x, double y, double z, Tuple additData) {
        IDollType type = null;
        switch( particle ) {
            case TEAM_BREAK: if( additData.checkValue(0, val -> val instanceof UUID) ) type = TeamRegistry.INSTANCE.getTeam(additData.<UUID>getValue(0)); break;
            case HORSE_BREAK: if( additData.checkValue(0, val -> val instanceof Integer) ) type = EnumClayHorseType.VALUES[additData.<Integer>getValue(0)]; break;
            case TURTLE_BREAK: if( additData.checkValue(0, val -> val instanceof Integer) ) type = EnumTurtleType.VALUES[additData.<Integer>getValue(0)]; break;
            case BUNNY_BREAK: if( additData.checkValue(0, val -> val instanceof Integer) ) type = EnumWoolBunnyType.VALUES[additData.<Integer>getValue(0)]; break;
            case GECKO_BREAK: if( additData.checkValue(0, val -> val instanceof Integer) ) type = EnumGeckoType.VALUES[additData.<Integer>getValue(0)]; break;
        }

        if( type != null ) {
            for( int i = 0; i < 20; i++ ) {
                mc.effectRenderer.addEffect(new ParticleDollBreaking(mc.world, x, y, z, type));
            }
        }
    }

    private static void spawnFireworks(EnumParticle particle, int dim, double x, double y, double z, Tuple additData) {
        if( additData.checkValue(0, val -> val instanceof NBTTagCompound) ) {
            mc.world.makeFireworks(x, y, z, 0.0D, 0.0D, 0.0D, additData.getValue(0));
        }
    }

    private static void spawnItemParticle(EnumParticle particle, int dim, double x, double y, double z, Tuple additData) {
        if( additData.checkValue(0, val -> val instanceof Integer) ) {
            ParticleBreaking.Factory bf = new ParticleBreaking.Factory();
            for( int i = 0; i < 20; i++ ) {
                mc.effectRenderer.addEffect(bf.createParticle(0, mc.world, x, y, z, 0.0D, 0.0D, 0.0D, additData.<Integer>getValue(0)));
            }
        }
    }

    private static void spawnCriticalParticle(EnumParticle particle, int dim, double x, double y, double z, Tuple additData) {
        ParticleCrit.Factory cf = new ParticleCrit.Factory();
        for( int i = 0; i < 20; i++ ) {
            mc.effectRenderer.addEffect(cf.createParticle(0, mc.world, x, y, z, MiscUtils.RNG.randomFloat() * 0.5D, 0.0D, MiscUtils.RNG.randomFloat() * 0.5D));
        }
    }

    private static void spawnHealingParticle(EnumParticle particle, int dim, double x, double y, double z, Tuple additData) {
        ParticleHeart.Factory cf = new ParticleHeart.Factory();
        for( int i = 0; i < 5; i++ ) {
            mc.effectRenderer.addEffect(cf.createParticle(0, mc.world, x, y, z, MiscUtils.RNG.randomFloat() * 0.5D, 0.0D, MiscUtils.RNG.randomFloat() * 0.5D));
        }
    }

    private static void spawnShockwaveParticle(EnumParticle particle, int dim, double x, double y, double z, Tuple additData) {
        ParticleBlockDust.Factory cf = new ParticleBlockDust.Factory();

        for( int i = 0; i < 75; i++ ) {
            double velX = MiscUtils.RNG.randomGaussian() * 0.15D;
            double velY = MiscUtils.RNG.randomGaussian() * 0.15D;
            double velZ = MiscUtils.RNG.randomGaussian() * 0.15D;

            mc.effectRenderer.addEffect(cf.createParticle(0, mc.world, x, y, z, velX, velY, velZ, Block.getStateId(Blocks.QUARTZ_BLOCK.getDefaultState())));
        }
    }
}
