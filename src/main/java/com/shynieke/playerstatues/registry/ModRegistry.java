package com.shynieke.playerstatues.registry;

import com.shynieke.playerstatues.PlayerStatuesMod;
import com.shynieke.playerstatues.block.PlayerStatueBlock;
import com.shynieke.playerstatues.blockentity.PlayerBlockEntity;
import com.shynieke.playerstatues.item.PlayerStatueBlockItem;
import com.shynieke.playerstatues.item.PlayerStatueSpawnItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

public class ModRegistry {
	public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, PlayerStatuesMod.MOD_ID);
	public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, PlayerStatuesMod.MOD_ID);
	public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, PlayerStatuesMod.MOD_ID);

	public static final RegistryObject<Block> PLAYER_STATUE = registerPlayerStatue("player_statue", () ->
			new PlayerStatueBlock(blockBuilder()), itemBuilder());

	public static final RegistryObject<Item> PLAYER_STATUE_SPAWN_EGG = ITEMS.register("player_statue_spawn_egg", () -> new PlayerStatueSpawnItem(itemBuilder()));
//	public static final RegistryObject<Item> UPGRADE_CORE = ITEMS.register("upgrade_core", () -> new Item(itemBuilder().stacksTo(1)));
	public static final RegistryObject<BlockEntityType<PlayerBlockEntity>> PLAYER = BLOCK_ENTITIES.register("player", () -> BlockEntityType.Builder.of(PlayerBlockEntity::new,
			ModRegistry.PLAYER_STATUE.get()).build(null));

	public static <B extends Block> RegistryObject<B> registerPlayerStatue(String name, Supplier<? extends B> supplier, Item.Properties properties) {
		RegistryObject<B> block = ModRegistry.BLOCKS.register(name, supplier);
		ITEMS.register(name, () -> new PlayerStatueBlockItem(block.get(), properties));
		return block;
	}

	private static Block.Properties blockBuilder() {
		return Block.Properties.of().mapColor(MapColor.COLOR_PURPLE);
	}

	private static Item.Properties itemBuilder() {
		return new Item.Properties();
	}
}
