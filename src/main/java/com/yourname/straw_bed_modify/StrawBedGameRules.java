package com.yourname.straw_bed_modify;

import net.fabricmc.fabric.api.gamerule.v1.GameRuleBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.gamerules.GameRule;
import net.minecraft.world.level.gamerules.GameRuleCategory;

/**
 * Custom game rules for the Straw Bed mod.
 * All defaults match vanilla straw bed behavior — opt-in to new features.
 */
public class StrawBedGameRules {

    /** Custom category so the three rules appear together in the game rules screen. */
    public static final GameRuleCategory CATEGORY =
        GameRuleCategory.register(Identifier.fromNamespaceAndPath(
            Straw_bed_modify.MOD_ID, "straw_bed"));

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
     * Trigger static initializer to register game rules.
     */
    public static void initialize() {
        // Static fields are initialized when the class is loaded
    }
}
