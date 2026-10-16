package com.yourname.straw_bed_modify;

import net.fabricmc.fabric.api.gamerule.v1.GameRuleBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.gamerules.GameRule;
import net.minecraft.world.level.gamerules.GameRuleCategory;
import net.minecraft.world.level.gamerules.GameRules;

/**
 * Custom game rules for the Straw Bed mod.
 * All defaults match vanilla straw bed behavior — opt-in to new features.
 */
public class StrawBedGameRules {

    /** Custom category so the four rules appear together in the game rules screen. */
    public static final GameRuleCategory CATEGORY =
        GameRuleCategory.register(Identifier.fromNamespaceAndPath(
            Straw_bed_modify.MOD_ID, "straw_bed"));

    /**
     * Sentinel stored in {@link #STRAW_BED_EQUIV} meaning "use the vanilla explosion power".
     * Exposed to players as the literal {@code default}.
     */
    public static final int EQUIV_DEFAULT = -1;

    /**
     * Vanilla's own bed explosion power — {@code BedBlock#destroyOnUse} calls
     * {@code level.explode(..., 5.0F, ...)}. This is what {@code equiv default} resolves to.
     */
    public static final float VANILLA_EXPLOSION_POWER = 5.0F;

    /** Highest blast power a player may set; also the upper bound of the game rule. */
    public static final int EQUIV_MAX = 64;

    /**
     * Whether sleeping in a straw bed sets the player's spawn point.
     * Default: false (matches vanilla straw bed behavior — does NOT set spawn)
     */
    public static final GameRule<Boolean> STRAW_BED_SET_SPAWN =
        GameRuleBuilder.forBoolean(false)
            .category(CATEGORY)
            .buildAndRegister(Identifier.fromNamespaceAndPath(
                Straw_bed_modify.MOD_ID, "straw_bed_set_spawn"));

    /**
     * Whether the straw bed explodes when used in the Nether or End.
     * Default: false (vanilla: straw bed breaks quietly without explosion)
     */
    public static final GameRule<Boolean> STRAW_BED_EXPLODE =
        GameRuleBuilder.forBoolean(false)
            .category(CATEGORY)
            .buildAndRegister(Identifier.fromNamespaceAndPath(
                Straw_bed_modify.MOD_ID, "straw_bed_explode"));

    /**
     * Whether the straw bed disappears after the player finishes sleeping.
     * Default: true (vanilla: straw bed is destroyed after leaving)
     */
    public static final GameRule<Boolean> STRAW_BED_VANISH =
        GameRuleBuilder.forBoolean(true)
            .category(CATEGORY)
            .buildAndRegister(Identifier.fromNamespaceAndPath(
                Straw_bed_modify.MOD_ID, "straw_bed_vanish"));

    /**
     * Blast power of the straw bed explosion, used only when {@link #STRAW_BED_EXPLODE} is true.
     * {@link #EQUIV_DEFAULT} (-1) means "use vanilla's {@link #VANILLA_EXPLOSION_POWER}";
     * any value in {@code 0..}{@link #EQUIV_MAX} is used verbatim, so {@code 0} is a
     * harmless blast (sound and particles, no block damage).
     *
     * <p>The range is kept signed on purpose: -1 is the {@code default} sentinel. Use
     * {@code /strawbed equiv default} rather than typing -1.
     */
    public static final GameRule<Integer> STRAW_BED_EQUIV =
        GameRuleBuilder.forInteger(EQUIV_DEFAULT)
            .range(EQUIV_DEFAULT, EQUIV_MAX)
            .category(CATEGORY)
            .buildAndRegister(Identifier.fromNamespaceAndPath(
                Straw_bed_modify.MOD_ID, "straw_bed_equiv"));

    /**
     * Resolve the blast power for the current world, mapping the {@code default} sentinel
     * to the vanilla value.
     */
    public static float resolveExplosionPower(GameRules gameRules) {
        int value = gameRules.get(STRAW_BED_EQUIV);
        return value == EQUIV_DEFAULT ? VANILLA_EXPLOSION_POWER : value;
    }

    /**
     * Trigger static initializer to register game rules.
     */
    public static void initialize() {
        // Static fields are initialized when the class is loaded
    }
}
