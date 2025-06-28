package com.shynieke.playerstatues.datagen.server;

import com.shynieke.playerstatues.registry.ModEntities;
import com.shynieke.playerstatues.registry.ModRegistry;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.data.loot.EntityLootSubProvider;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

public class ModLootProvider extends LootTableProvider {
	public ModLootProvider(PackOutput packOutput) {
		super(packOutput, Set.of(), List.of(
				new SubProviderEntry(ModBLockLoot::new, LootContextParamSets.BLOCK),
				new SubProviderEntry(ModEntityLoot::new, LootContextParamSets.ENTITY)
		));
	}

	@Override
	protected void validate(Map<ResourceLocation, LootTable> map, @NotNull ValidationContext validationContext) {
		map.forEach((name, table) -> table.validate(validationContext));
	}

	private static class ModBLockLoot extends BlockLootSubProvider {

		protected ModBLockLoot() {
			super(Set.of(), FeatureFlags.REGISTRY.allFlags());
		}

		@Override
		protected void generate() {
			this.add(ModRegistry.PLAYER_STATUE.get(), createNameableBlockEntityTable(ModRegistry.PLAYER_STATUE.get()));
		}

		@NotNull
		@Override
		protected Iterable<Block> getKnownBlocks() {
			return (Iterable<Block>) ModRegistry.BLOCKS.getEntries().stream().map(RegistryObject::get)::iterator;
		}
	}

	private static class ModEntityLoot extends EntityLootSubProvider {
		protected ModEntityLoot() {
			super(FeatureFlags.REGISTRY.allFlags());
		}

		@Override
		public void generate() {
			this.add(ModEntities.PLAYER_STATUE_ENTITY.get(), LootTable.lootTable());
		}

		@Override
		protected boolean canHaveLootTable(@NotNull EntityType<?> entitytype) {
			return true;
		}

		@NotNull
		@Override
		protected Stream<EntityType<?>> getKnownEntityTypes() {
			return ModEntities.ENTITY_TYPES.getEntries().stream().map(RegistryObject::get);
		}
	}
}
