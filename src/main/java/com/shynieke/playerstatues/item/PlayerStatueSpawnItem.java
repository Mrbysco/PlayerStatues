package com.shynieke.playerstatues.item;

import com.mojang.authlib.GameProfile;
import com.shynieke.playerstatues.entity.PlayerStatuesStatue;
import com.shynieke.playerstatues.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.UUID;

public class PlayerStatueSpawnItem extends Item {

	public PlayerStatueSpawnItem(Properties builder) {
		super(builder);
	}

	@NotNull
	@Override
	public InteractionResult useOn(UseOnContext context) {
		Level level = context.getLevel();
		if (!(level instanceof ServerLevel)) {
			return InteractionResult.SUCCESS;
		} else {
			ItemStack stack = context.getItemInHand();
			BlockPos pos = context.getClickedPos();
			Direction direction = context.getClickedFace();
			BlockState state = level.getBlockState(pos);

			BlockPos relativePos;
			if (state.getCollisionShape(level, pos).isEmpty()) {
				relativePos = pos;
			} else {
				relativePos = pos.relative(direction);
			}

			EntityType<?> type = ModEntities.PLAYER_STATUE_ENTITY.get();
			if (type.spawn((ServerLevel) level, stack, context.getPlayer(), relativePos, MobSpawnType.SPAWN_EGG, true, !Objects.equals(pos, relativePos) && direction == Direction.UP) instanceof PlayerStatuesStatue playerStatue) {
				float f = (float) Mth.floor((Mth.wrapDegrees(context.getRotation() - 180.0F) + 22.5F) / 45.0F) * 45.0F;

				if (!stack.hasCustomHoverName()) {
					if (context.getPlayer() != null) {
						playerStatue.setGameProfile(context.getPlayer().getGameProfile());
					} else {
						playerStatue.setGameProfile(new GameProfile((UUID) null, "shynieke"));
					}
				}
				playerStatue.setYRot(f);
				stack.shrink(1);
				level.playSound((Player) null, playerStatue.getX(), playerStatue.getY(), playerStatue.getZ(),
						SoundEvents.ARMOR_STAND_PLACE, SoundSource.BLOCKS, 0.75F, 0.8F);

				level.gameEvent(context.getPlayer(), GameEvent.ENTITY_PLACE, pos);
			}

			return InteractionResult.CONSUME;
		}
	}
}
