package com.nstut.simplyscreens;

import com.mojang.logging.LogUtils;
import com.nstut.simplyscreens.blocks.ScreenBlock;
import com.nstut.simplyscreens.blocks.entities.ScreenBlockEntity;
import com.nstut.simplyscreens.client.ClientSetup;
import com.nstut.simplyscreens.network.PacketRegistries;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import com.nstut.simplyscreens.blocks.BlockRegistries;
import com.nstut.simplyscreens.blocks.entities.BlockEntityRegistries;
import com.nstut.simplyscreens.creative_tabs.CreativeTabRegistries;
import com.nstut.simplyscreens.items.ItemRegistries;
import org.slf4j.Logger;

@Mod(SimplyScreens.MOD_ID)
public class SimplyScreens {

    // Define mod id in a common place for everything to reference
    public static final String MOD_ID = "simplyscreens";
    // Directly reference a slf4j logger
    private static final Logger LOGGER = LogUtils.getLogger();

    public SimplyScreens(FMLJavaModLoadingContext context) {
        IEventBus modEventBus = context.getModEventBus();

        // Register the commonSetup method for mod loading
        modEventBus.addListener(this::commonSetup);
        MinecraftForge.EVENT_BUS.addListener(ClientSetup::setup);

        // Register blocks
        BlockRegistries.BLOCKS.register(modEventBus);

        // Register block entities
        BlockEntityRegistries.BLOCK_ENTITIES.register(modEventBus);

        // Register the Deferred Register to the mod event bus so items get registered
        ItemRegistries.ITEMS.register(modEventBus);

        // Register the Deferred Register to the mod event bus so tabs get registered
        CreativeTabRegistries.CREATIVE_MODE_TABS.register(modEventBus);

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);

        // Register the item to a creative tab
        modEventBus.addListener(this::addCreative);

        // Register our mod's ForgeConfigSpec so that Forge can create and load the config file for us
        context.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    @SubscribeEvent
    public void onBlockBroken(BlockEvent.BreakEvent event) {
        Level level = (Level) event.getLevel();

        if (level.isClientSide()) {
            return;
        }

        BlockPos pos = event.getPos();

        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof ScreenBlockEntity screenBlockEntity && screenBlockEntity.isAnchor()) {
            screenBlockEntity.findNewAnchor();
        }

        // Notify neighboring screen structures to update
        for (Direction direction : Direction.values()) {
            BlockPos neighborPos = pos.relative(direction);
            BlockEntity neighborBe = level.getBlockEntity(neighborPos);
            if (neighborBe instanceof ScreenBlockEntity screenBe) {
                screenBe.onNeighborRemoved();
            }
        }
    }

    @SubscribeEvent
    public void onBlockPlaced(BlockEvent.EntityPlaceEvent event) {
        BlockState placedBlock = event.getPlacedBlock();
        if (!(placedBlock.getBlock() instanceof ScreenBlock)) {
            return;
        }

        Level level = (Level) event.getLevel();
        BlockPos pos = event.getPos();

        if (level.isClientSide()) {
            return;
        }

        // Check all neighboring blocks for existing screen structures
        for (Direction direction : Direction.values()) {
            BlockPos neighborPos = pos.relative(direction);
            BlockEntity neighborBe = level.getBlockEntity(neighborPos);
            if (neighborBe instanceof ScreenBlockEntity neighborScreen) {
                neighborScreen.onNeighborPlaced(pos, direction.getOpposite());
            }
        }
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        PacketRegistries.register();
    }

    // Add the example block item to the building blocks tab
    private void addCreative(BuildCreativeModeTabContentsEvent event) {

    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {

    }

    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
    @Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {

        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {

        }
    }
}
