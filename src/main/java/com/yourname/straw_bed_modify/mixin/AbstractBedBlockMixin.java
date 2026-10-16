package com.yourname.straw_bed_modify.mixin;

import com.yourname.straw_bed_modify.StrawBedGameRules;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.attribute.BedRule;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractBedBlock;
import net.minecraft.world.level.block.Blocks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Override BedRule for straw beds to respect our game rules.
 * getBedRule() is defined in AbstractBedBlock, so we must target the parent class.
 *
 * <p><b>Precedence (intentional):</b> the mod's game rules deliberately win over the
 * data-driven vanilla {@code minecraft:gameplay/straw_bed_rule}, which since MC 26.3 is
 * configured per dimension via {@code dimension_type} JSON ({@code can_set_spawn},
 * {@code can_sleep}, {@code destroy_on_use}, {@code destroy_on_leave}).
 * The vanilla values read back for {@code canSetSpawn} and {@code destroyOnLeave} are
 * therefore discarded on purpose — this is <em>not</em> a bug, so do not "fix" it into an
 * intersection. Only {@code canSleep}, {@code destroyOnUse} and {@code errorMessage}
 * are passed through untouched.
 *
 * <p>This method is also the <b>only</b> place the {@code straw_bed_vanish} rule is applied.
 * Vanilla reads {@code destroyOnLeave} in {@code AbstractBedBlock.onStopSleeping} and calls
 * {@code destroyOnLeave} only when it is true, so gating that method again would be dead
 * weight.
 */
@Mixin(AbstractBedBlock.class)
public abstract class AbstractBedBlockMixin {

    @Inject(method = "getBedRule", at = @At("RETURN"), cancellable = true)
    private void onGetBedRule(Level level, BlockPos pos,
                               CallbackInfoReturnable<BedRule> cir) {
        if (!(level instanceof ServerLevel serverLevel)) return;
        if (!level.getBlockState(pos).is(Blocks.STRAW_BED)) return;

        BedRule original = cir.getReturnValue();
        boolean canSetSpawn = serverLevel.getGameRules()
            .get(StrawBedGameRules.STRAW_BED_SET_SPAWN)
            && !isDangerousDimension(serverLevel);  // never in Nether/End
        boolean shouldVanish = serverLevel.getGameRules()
            .get(StrawBedGameRules.STRAW_BED_VANISH);

        BedRule modified = new BedRule(
            original.canSleep(),
            canSetSpawn ? BedRule.Rule.ALWAYS : BedRule.Rule.NEVER,
            original.destroyOnUse(),  // always destroy — explosion controlled by mixin
            // Mod rule takes precedence over vanilla straw_bed_rule.destroy_on_leave — see class javadoc
            shouldVanish,
            original.errorMessage()
        );
        cir.setReturnValue(modified);
    }

    /** Spawn point must never be set in Nether or End, regardless of game rules. */
    private static boolean isDangerousDimension(ServerLevel level) {
        return level.dimension() == Level.NETHER || level.dimension() == Level.END;
    }
}
