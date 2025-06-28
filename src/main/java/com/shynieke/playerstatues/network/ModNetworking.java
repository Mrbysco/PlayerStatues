package com.shynieke.playerstatues.network;

import com.shynieke.playerstatues.PlayerStatuesMod;
import com.shynieke.playerstatues.network.message.PlayerStatueScreenMessage;
import com.shynieke.playerstatues.network.message.PlayerStatueSyncMessage;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class ModNetworking {
	private static final String PROTOCOL_VERSION = "1";
	public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
			new ResourceLocation(PlayerStatuesMod.MOD_ID, "main"),
			() -> PROTOCOL_VERSION,
			PROTOCOL_VERSION::equals,
			PROTOCOL_VERSION::equals
	);

	private static int id = 0;

	public static void init() {
		CHANNEL.registerMessage(id++, PlayerStatueSyncMessage.class, PlayerStatueSyncMessage::encode, PlayerStatueSyncMessage::decode, PlayerStatueSyncMessage::handle);
		CHANNEL.registerMessage(id++, PlayerStatueScreenMessage.class, PlayerStatueScreenMessage::encode, PlayerStatueScreenMessage::decode, PlayerStatueScreenMessage::handle);
	}
}
