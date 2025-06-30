package com.shynieke.playerstatues.datagen.client;

import com.shynieke.playerstatues.PlayerStatuesMod;
import com.shynieke.playerstatues.registry.ModEntities;
import com.shynieke.playerstatues.registry.ModRegistry;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.LanguageProvider;

public class ModLanguageProvider extends LanguageProvider {

	public ModLanguageProvider(PackOutput packOutput) {
		super(packOutput, PlayerStatuesMod.MOD_ID, "en_us");
	}

	@Override
	protected void addTranslations() {
		addItem(ModRegistry.PLAYER_DOLL, "Player Doll");
		addItem(ModRegistry.PLAYER_STATUE_SPAWN_EGG, "Player Statue Spawn Egg");
		addBlock(ModRegistry.PLAYER_STATUE, "Player Statue");
		addEntityType(ModEntities.PLAYER_STATUE_ENTITY, "Player Statue");

		add("player_statues.pose.gui.title", "Player Statue");
		add("player_statues.pose.gui.label.small", "Small");
		add("player_statues.pose.gui.label.locked", "Locked");
		add("player_statues.pose.gui.label.name_visible", "Name Visible");
		add("player_statues.pose.gui.label.gravity", "Zero Gravity");
		add("player_statues.pose.gui.label.y_offset", "Y Offset");
		add("player_statues.pose.gui.label.rotation", "Rotation");
		add("player_statues.pose.gui.label.head", "Head");
		add("player_statues.pose.gui.label.body", "Body");
		add("player_statues.pose.gui.label.left_leg", "Left Leg");
		add("player_statues.pose.gui.label.right_leg", "Right Leg");
		add("player_statues.pose.gui.label.left_arm", "Left Arm");
		add("player_statues.pose.gui.label.right_arm", "Right Arm");
		add("player_statues.pose.gui.label.copy", "Copy");
		add("player_statues.pose.gui.label.paste", "Paste");
		add("player_statues.pose.gui.label.model_type", "Model Type");
		add("player_statues.pose.gui.label.position", "Position");
		add("player_statues.modeltype.AUTO", "Auto");
		add("player_statues.modeltype.DEFAULT", "Default");
		add("player_statues.modeltype.SLIM", "Slim");

		add("curios.identifier.feet", "Feet");
	}
}
