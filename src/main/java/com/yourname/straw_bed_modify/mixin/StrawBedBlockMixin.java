package com.yourname.straw_bed_modify.mixin;

import com.yourname.straw_bed_modify.StrawBedGameRules;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.StrawBedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(StrawBedBlock.class)
public abstract class StrawBedBlockMixin {

    /**
     * Destroy the bed when used in dangerous dimensions (Nether/End).
     * Explosion is controlled by the {@code straw_bed_explode} game rule:
     * <ul>
     *   <li>{@code true}  — explode (like vanilla BedBlock)</li>
     *   <li>{@code false} — break quietly (vanilla straw bed behavior)</li>
     * </ul>
     */
    @Inject(method = "destroyOnUse", at = @At("HEAD"), cancellable = true)
    private void onDestroyOnUse(BlockState state, Level level, BlockPos pos,
                                 Player player,
                                 CallbackInfoReturnable<InteractionResult> cir) {
        // Remove both bed halves
        level.removeBlock(pos, false);
        Direction facing = state.getValue(StrawBedBlock.FACING);
        BlockPos otherPos = pos.relative(facing.getOpposite());
        BlockState otherState = level.getBlockState(otherPos);
        if (otherState.is(state.getBlock())) {
            level.removeBlock(otherPos, false);
        }

        // Only explode if the game rule allows it
        if (level instanceof ServerLevel serverLevel
            && serverLevel.getGameRules().get(StrawBedGameRules.STRAW_BED_EXPLODE)) {
            Vec3 explosionPos = Vec3.atCenterOf(pos);
            level.explode(
                null,
                level.damageSources().badRespawnPointExplosion(explosionPos),
                null,
                explosionPos,
                5.0F,
                true,
                Level.ExplosionInteraction.BLOCK
            );
        }
        cir.setReturnValue(InteractionResult.SUCCESS_SERVER);
    }

    /**
     * When vanish is disabled (default), prevent the bed from being destroyed.
     * When vanish is enabled, let vanilla behavior execute.
     */
    @Inject(method = "destroyOnLeave", at = @At("HEAD"), cancellable = true)
    private void onDestroyOnLeave(Level level, BlockPos pos, CallbackInfo ci) {
        if (level instanceof ServerLevel serverLevel) {
            boolean shouldVanish = serverLevel.getGameRules()
                .get(StrawBedGameRules.STRAW_BED_VANISH);
            if (!shouldVanish) {
                ci.cancel();
            }
        }
    }
}
