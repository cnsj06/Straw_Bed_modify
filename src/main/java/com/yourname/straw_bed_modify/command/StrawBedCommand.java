package com.yourname.straw_bed_modify.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
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
}
