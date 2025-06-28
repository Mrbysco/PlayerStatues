package com.shynieke.playerstatues.datagen.client;

import com.shynieke.playerstatues.PlayerStatuesMod;
import com.shynieke.playerstatues.block.AbstractBaseBlock;
import com.shynieke.playerstatues.registry.ModRegistry;
import net.minecraft.core.Direction;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.RegistryObject;

public class ModBlockstateProvider extends BlockStateProvider {
	public ModBlockstateProvider(PackOutput packOutput, ExistingFileHelper helper) {
		super(packOutput, PlayerStatuesMod.MOD_ID, helper);
	}

	@Override
	protected void registerStatesAndModels() {
		for (RegistryObject<Block> registryObject : ModRegistry.BLOCKS.getEntries()) {
			if (registryObject.get() instanceof AbstractBaseBlock) {
				makeStatue(registryObject);
			}
		}
	}

	private void makeStatue(RegistryObject<Block> registryObject) {
		ModelFile model = models().getExistingFile(modLoc("block/" + registryObject.getId().getPath()));
		getVariantBuilder(registryObject.get())
				.partialState().with(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH)
				.modelForState().modelFile(model).addModel()
				.partialState().with(BlockStateProperties.HORIZONTAL_FACING, Direction.EAST)
				.modelForState().modelFile(model).rotationY(90).addModel()
				.partialState().with(BlockStateProperties.HORIZONTAL_FACING, Direction.SOUTH)
				.modelForState().modelFile(model).rotationY(180).addModel()
				.partialState().with(BlockStateProperties.HORIZONTAL_FACING, Direction.WEST)
				.modelForState().modelFile(model).rotationY(270).addModel();
	}
}
