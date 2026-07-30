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
