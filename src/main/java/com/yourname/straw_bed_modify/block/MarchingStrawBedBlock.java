package com.yourname.straw_bed_modify.block;

import com.yourname.straw_bed_modify.StrawBedGameRules;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.stats.Stats;
import net.minecraft.world.attribute.BedRule;
import net.minecraft.world.attribute.EnvironmentAttribute;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.StrawBedBlock;

/**
 * 行军稻草床 (Marching Straw Bed)
 *
 * Behavior:
 * - Functions like a regular bed (sleep at night, set spawn point).
 * - Does NOT vanish after the player finishes sleeping (unlike vanilla straw bed).
 *   This is the key difference — the bed persists after use.
 * - Spawn point setting is controlled by {@code /strawbed spawnpoint}.
 * - Extends {@link StrawBedBlock} to inherit the vanilla straw bed appearance, shape,
 *   and placement logic, but overrides key methods to behave like a regular {@code BedBlock}.
 */
public class MarchingStrawBedBlock extends StrawBedBlock {

    public MarchingStrawBedBlock(Properties properties) {
        super(properties);
    }

    /**
     * Always prevent the bed from being destroyed when the player wakes up.
     * The marching straw bed persists after sleep — regardless of game rules.
     */
    @Override
    protected void destroyOnLeave(Level level, BlockPos pos) {
        // No-op: marching straw bed never vanishes after sleep
    }

    /**
     * Use the regular bed environment attribute instead of the straw bed one.
     * This ensures {@code startSleepInBed} treats this as a normal bed,
     * not as a straw bed (which would trigger instant night-skip).
     */
    @Override
    protected EnvironmentAttribute<BedRule> getBedEnvironmentAttribute() {
        return EnvironmentAttributes.BED_RULE;
    }

    /**
     * Track sleep statistics as a regular bed, not a straw bed.
     */
    @Override
    public Identifier getSleptInBedStatType() {
        return Stats.SLEEP_IN_BED;
    }

    /**
     * Build the BedRule directly from game rules so the block works without
     * environment-attribute data files.
     * <p>
     * Key behaviors:
     * <ul>
     *   <li>Spawn point controlled by {@code /strawbed spawnpoint}, but NEVER in Nether/End</li>
     *   <li>In Nether/End: destroyed on use (explosion controlled by {@code /strawbed explode})</li>
     *   <li>In Overworld: works like a regular bed (sleep at night, set spawn)</li>
     *   <li>Never vanishes after sleep in any dimension</li>
     * </ul>
     */
    @Override
    public BedRule getBedRule(Level level, BlockPos pos) {
        boolean canSetSpawn = false;
        boolean isDangerous = false;

        if (level instanceof ServerLevel serverLevel) {
            isDangerous = serverLevel.dimension() == Level.NETHER
                || serverLevel.dimension() == Level.END;
            canSetSpawn = serverLevel.getGameRules()
                .get(StrawBedGameRules.STRAW_BED_SET_SPAWN)
                && !isDangerous;  // never set spawn in Nether/End
        }

        // In dangerous dimensions: destroy on use (triggers mixin for explosion control)
        // In Overworld: behave like a regular bed (no destruction)
        // Never vanish after sleep in any dimension
        return new BedRule(
            isDangerous ? BedRule.Rule.NEVER : BedRule.Rule.WHEN_DARK,
            canSetSpawn ? BedRule.Rule.ALWAYS : BedRule.Rule.NEVER,
            isDangerous,   // destroyOnUse — destroy in Nether/End (explosion via /strawbed explode)
            false,         // destroyOnLeave — never vanish after sleep
            java.util.Optional.empty()
        );
    }
}
