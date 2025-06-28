package com.shynieke.playerstatues.datagen.server;

import com.shynieke.playerstatues.PlayerStatuesMod;
import com.shynieke.playerstatues.registry.ModRegistry;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class ModItemTagProvider extends ItemTagsProvider {

	public ModItemTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider,
	                          TagsProvider<Block> blockTagProvider, ExistingFileHelper existingFileHelper) {
		super(output, lookupProvider, blockTagProvider.contentsGetter(), PlayerStatuesMod.MOD_ID, existingFileHelper);
	}

	@Override
	public void addTags(HolderLookup.@NotNull Provider lookupProvider) {
		this.tag(PlayerStatuesMod.UPGRADE_ITEM).add(Items.PHANTOM_MEMBRANE);

		this.tag(curiosTag("feet")).add(ModRegistry.PLAYER_STATUE.get().asItem());
		this.tag(curiosTag("belt")).add(ModRegistry.PLAYER_STATUE.get().asItem());
		this.tag(curiosTag("body")).add(ModRegistry.PLAYER_STATUE.get().asItem());
		this.tag(curiosTag("head")).add(ModRegistry.PLAYER_STATUE.get().asItem());
	}
	
	private TagKey<Item> curiosTag(String name) {
		return ItemTags.create(new ResourceLocation("curios", name));
	}
}
