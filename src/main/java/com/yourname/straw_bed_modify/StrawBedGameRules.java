package com.yourname.straw_bed_modify;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.serialization.Codec;
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

    /**
     * Highest blast power a player may set. The blast radius is {@code power * 2} blocks, so
     * this cap is a radius of 64 — raising it is not free: an explosion's cost grows with the
     * volume it sweeps, and large values stall the server tick loop.
     */
    public static final int EQUIV_MAX = 32;

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
     * anything else is clamped into {@code 0..}{@link #EQUIV_MAX}, so {@code 0} is a harmless
     * blast (sound and particles, no block damage).
     *
     * <p><b>Why this rule deliberately has no {@code .range()} despite documenting a cap:</b>
     * all game rules are persisted as a single {@code GameRuleMap} blob in
     * {@code data/minecraft/game_rules.dat}, decoded by a {@code Codec.dispatchedMap}. Narrowing
     * this rule's value codec would make an <em>already saved</em> out-of-range value fail to
     * decode — and one failing entry takes down the entire map, silently resetting every game
     * rule in that world. So instead: the codec stays permissive ({@code Codec.INT}), the cap is
     * enforced on input through the argument type, and {@link #resolveExplosionPower} clamps on
     * read. A world that already stored a larger value still loads, and that value simply
     * behaves as {@link #EQUIV_MAX}. Do not "tidy" this into {@code .range()}.
     */
    public static final GameRule<Integer> STRAW_BED_EQUIV =
        GameRuleBuilder.forInteger(EQUIV_DEFAULT)
            .argumentType(IntegerArgumentType.integer(0, EQUIV_MAX))
            .codec(Codec.INT)
            .category(CATEGORY)
            .buildAndRegister(Identifier.fromNamespaceAndPath(
                Straw_bed_modify.MOD_ID, "straw_bed_equiv"));

    /**
     * Resolve the blast power for the current world: map the {@code default} sentinel to the
     * vanilla value, and clamp anything else into {@code 0..}{@link #EQUIV_MAX}. The clamp is
     * what keeps a value stored by an older build (or hand-edited NBT) from stalling the server.
     */
    public static float resolveExplosionPower(GameRules gameRules) {
        int value = gameRules.get(STRAW_BED_EQUIV);
        if (value == EQUIV_DEFAULT) {
            return VANILLA_EXPLOSION_POWER;
        }
        return Math.min(Math.max(value, 0), EQUIV_MAX);
    }

    /**
     * Trigger static initializer to register game rules.
     */
    public static void initialize() {
        // Static fields are initialized when the class is loaded
    }
}