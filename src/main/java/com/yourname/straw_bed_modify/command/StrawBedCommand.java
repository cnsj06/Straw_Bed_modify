package com.yourname.straw_bed_modify.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.yourname.straw_bed_modify.StrawBedGameRules;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.gamerules.GameRule;

/**
 * In-game command for controlling straw bed behavior.
 *
 * Usage:
 *   /strawbed spawnpoint &lt;true|false&gt;
 *   /strawbed explode    &lt;true|false&gt;
 *   /strawbed vanish     &lt;true|false&gt;
 *   /strawbed equiv      &lt;default|0-32&gt;
 */
public class StrawBedCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            Commands.literal("strawbed")
                .then(Commands.literal("spawnpoint")
                    .then(Commands.argument("value", BoolArgumentType.bool())
                        .executes(ctx -> setRule(ctx,
                            StrawBedGameRules.STRAW_BED_SET_SPAWN, "spawnpoint"))))
                .then(Commands.literal("explode")
                    .then(Commands.argument("value", BoolArgumentType.bool())
                        .executes(ctx -> setRule(ctx,
                            StrawBedGameRules.STRAW_BED_EXPLODE, "explode"))))
                .then(Commands.literal("vanish")
                    .then(Commands.argument("value", BoolArgumentType.bool())
                        .executes(ctx -> setRule(ctx,
                            StrawBedGameRules.STRAW_BED_VANISH, "vanish"))))
                .then(Commands.literal("equiv")
                    // "default" restores vanilla's blast power; the sentinel is never typed by hand
                    .then(Commands.literal("default")
                        .executes(ctx -> setEquiv(ctx, StrawBedGameRules.EQUIV_DEFAULT)))
                    .then(Commands.argument("value",
                            IntegerArgumentType.integer(0, StrawBedGameRules.EQUIV_MAX))
                        .executes(ctx -> setEquiv(ctx,
                            IntegerArgumentType.getInteger(ctx, "value")))))
        );
    }

    private static int setRule(CommandContext<CommandSourceStack> context,
                                GameRule<Boolean> rule, String key) {
        boolean value = BoolArgumentType.getBool(context, "value");
        ServerLevel level = context.getSource().getLevel();
        level.getGameRules().set(rule, value, context.getSource().getServer());

        String status = value ? "enabled" : "disabled";
        context.getSource().sendSuccess(
            () -> Component.translatable(
                "command.straw_bed_modify." + key + ".set", status),
            true
        );
        return 1;
    }

    /**
     * Set the explosion blast power. Returns 1 rather than the value itself, so that
     * {@code /strawbed equiv 0} still counts as a success for command blocks.
     */
    private static int setEquiv(CommandContext<CommandSourceStack> context, int value) {
        ServerLevel level = context.getSource().getLevel();
        level.getGameRules().set(StrawBedGameRules.STRAW_BED_EQUIV, value,
            context.getSource().getServer());

        Component status = value == StrawBedGameRules.EQUIV_DEFAULT
            ? Component.translatable("command.straw_bed_modify.equiv.default")
            : Component.literal(Integer.toString(value));
        context.getSource().sendSuccess(
            () -> Component.translatable("command.straw_bed_modify.equiv.set", status),
            true
        );
        return 1;
    }
}
