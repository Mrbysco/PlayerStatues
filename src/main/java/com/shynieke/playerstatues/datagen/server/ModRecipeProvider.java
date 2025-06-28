package com.shynieke.playerstatues.datagen.server;

import com.shynieke.playerstatues.registry.ModRegistry;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class ModRecipeProvider extends RecipeProvider {

	public ModRecipeProvider(PackOutput packOutput) {
		super(packOutput);
	}

	@Override
	protected void buildRecipes(@NotNull Consumer<FinishedRecipe> consumer) {
		ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModRegistry.PLAYER_STATUE.get())
				.pattern("PPP")
				.pattern("PAP")
				.pattern("PPP")
				.define('P', ItemTags.PLANKS)
				.define('A', Items.ARMOR_STAND)
				.unlockedBy("has_planks", has(ItemTags.PLANKS))
				.unlockedBy("has_armor_stand", has(Items.ARMOR_STAND))
				.save(consumer);
	}
}
