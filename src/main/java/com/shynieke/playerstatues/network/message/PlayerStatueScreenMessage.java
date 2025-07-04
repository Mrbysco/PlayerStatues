package com.shynieke.playerstatues.network.message;

import com.shynieke.playerstatues.entity.PlayerStatuesStatue;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkEvent.Context;

import java.util.function.Supplier;

public class PlayerStatueScreenMessage {
	private final int entityID;

	private PlayerStatueScreenMessage(FriendlyByteBuf buf) {
		this.entityID = buf.readInt();
	}

	public PlayerStatueScreenMessage(int playerUUID) {
		this.entityID = playerUUID;
	}

	public void encode(FriendlyByteBuf buf) {
		buf.writeInt(entityID);
	}

	public static PlayerStatueScreenMessage decode(final FriendlyByteBuf packetBuffer) {
		return new PlayerStatueScreenMessage(packetBuffer.readInt());
	}

	public void handle(Supplier<Context> context) {
		Context ctx = context.get();
		ctx.enqueueWork(() -> {
			if (ctx.getDirection().getReceptionSide().isClient()) {
				Minecraft mc = Minecraft.getInstance();
				Entity entity = mc.level.getEntity(entityID);
				if (entity instanceof PlayerStatuesStatue playerStatue) {
					com.shynieke.playerstatues.client.screen.PlayerPoseScreen.openScreen(playerStatue);
				}
			}
		});
		ctx.setPacketHandled(true);
	}
}
