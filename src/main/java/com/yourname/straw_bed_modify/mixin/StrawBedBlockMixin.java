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
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Bed destruction behaviour for straw beds.
 *
 * <p><b>Where the vanish gate lives:</b> {@code destroyOnLeave} is deliberately <em>not</em>
 * intercepted here. The {@code straw_bed_vanish} rule is applied once, in
 * {@code AbstractBedBlockMixin#onGetBedRule}, by rewriting {@code BedRule.destroyOnLeave}.
 * Vanilla reaches {@code destroyOnLeave} only from {@code AbstractBedBlock.onStopSleeping},
 * and only after that flag reads true, so a second gate here would never change the outcome
 * — it would just be a second place to update.
 */
@Mixin(StrawBedBlock.class)
public abstract class StrawBedBlockMixin {

    /**
     * Destroy the bed when used in dangerous dimensions (Nether/End).
     * The blast is controlled by two game rules:
     * <ul>
     *   <li>{@code straw_bed_explode} — {@code false} breaks the bed quietly (vanilla straw
     *       bed behavior), {@code true} explodes like a vanilla bed</li>
     *   <li>{@code straw_bed_equiv} — blast power used when exploding; its {@code default}
     *       setting resolves to vanilla's 5.0</li>
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

        // Only explode if the game rule allows it; power comes from straw_bed_equiv
        if (level instanceof ServerLevel serverLevel) {
            var gameRules = serverLevel.getGameRules();
            if (gameRules.get(StrawBedGameRules.STRAW_BED_EXPLODE)) {
                Vec3 explosionPos = Vec3.atCenterOf(pos);
                level.explode(
                    null,
                    level.damageSources().badRespawnPointExplosion(explosionPos),
                    null,
                    explosionPos,
                    StrawBedGameRules.resolveExplosionPower(gameRules),
                    true,
                    Level.ExplosionInteraction.BLOCK
                );
            }
        }
        cir.setReturnValue(InteractionResult.SUCCESS_SERVER);
    }
}
