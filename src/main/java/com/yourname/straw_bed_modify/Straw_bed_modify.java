package com.yourname.straw_bed_modify;

import com.yourname.straw_bed_modify.block.MarchingStrawBedBlock;
import com.yourname.straw_bed_modify.command.StrawBedCommand;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Straw_bed_modify implements ModInitializer {

    public static final String MOD_ID = "straw_bed_modify";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    // 行军稻草床 (Marching Straw Bed) — persists after sleep
    // Will be populated in onInitialize()
    public static Block MARCHING_STRAW_BED;
    public static Item MARCHING_STRAW_BED_ITEM;

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing Straw Bed Modify mod...");

        // Register game rules (spawnpoint, explode, vanish)
        StrawBedGameRules.initialize();
        LOGGER.info("Registered 3 game rules: straw_bed_set_spawn, straw_bed_explode, straw_bed_vanish");

        // Register marching straw bed block and item
        Identifier marchingBedId = Identifier.fromNamespaceAndPath(MOD_ID, "marching_straw_bed");

        // Create ResourceKeys needed for registration
        ResourceKey<Block> blockKey = ResourceKey.create(BuiltInRegistries.BLOCK.key(), marchingBedId);
        ResourceKey<Item> itemKey = ResourceKey.create(BuiltInRegistries.ITEM.key(), marchingBedId);

        MARCHING_STRAW_BED = Registry.register(
            BuiltInRegistries.BLOCK,
            blockKey,
            new MarchingStrawBedBlock(
                BlockBehaviour.Properties.ofFullCopy(Blocks.STRAW_BED).setId(blockKey))
        );
        MARCHING_STRAW_BED_ITEM = Registry.register(
            BuiltInRegistries.ITEM,
            itemKey,
            new BlockItem(MARCHING_STRAW_BED, new Item.Properties().setId(itemKey).useBlockDescriptionPrefix().stacksTo(16))
        );
        LOGGER.info("Registered marching straw bed block and item");

        // Register the /strawbed command
        CommandRegistrationCallback.EVENT.register((dispatcher, buildContext, selection) -> {
            StrawBedCommand.register(dispatcher);
        });
        LOGGER.info("Registered /strawbed command");

        // Register marching straw bed into the creative inventory, right after the vanilla straw bed
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.FUNCTIONAL_BLOCKS).register(output -> {
            output.insertAfter(net.minecraft.world.item.Items.STRAW_BED, MARCHING_STRAW_BED_ITEM);
        });
        LOGGER.info("Registered marching straw bed into creative inventory (after vanilla straw bed)");

        LOGGER.info("Straw Bed Modify mod initialized successfully!");
    }
}
