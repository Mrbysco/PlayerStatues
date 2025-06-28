package com.shynieke.playerstatues.registry;

import com.shynieke.playerstatues.PlayerStatuesMod;
import com.shynieke.playerstatues.entity.PlayerStatuesStatue;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@Mod.EventBusSubscriber(modid = PlayerStatuesMod.MOD_ID)
public class ModEntities {
	public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, PlayerStatuesMod.MOD_ID);

	public static final RegistryObject<EntityType<PlayerStatuesStatue>> PLAYER_STATUE_ENTITY = ENTITY_TYPES.register("player_statue",
			() -> EntityType.Builder.<PlayerStatuesStatue>of(PlayerStatuesStatue::new, MobCategory.MISC)
					.sized(0.6F, 1.8F).clientTrackingRange(10).build("player_statue"));

	public static void registerEntityAttributes(EntityAttributeCreationEvent event) {
		event.put(ModEntities.PLAYER_STATUE_ENTITY.get(), PlayerStatuesStatue.createLivingAttributes().build());
	}
}
