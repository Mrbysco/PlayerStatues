package com.shynieke.playerstatues.config;

import com.shynieke.playerstatues.PlayerStatuesMod;
import com.shynieke.playerstatues.client.renderer.PlayerBER;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;
import net.minecraftforge.common.ForgeConfigSpec.IntValue;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import org.apache.commons.lang3.tuple.Pair;

public class PlayerStatuesConfig {

	public static class Client {
		public final BooleanValue allowScrolling;
		public final IntValue defaultSkin;

		Client(ForgeConfigSpec.Builder builder) {
			builder.comment("Client settings")
					.push("Client");

			allowScrolling = builder
					.comment("Allow scrolling to increase / decrease an angle value in the posing screen")
					.define("allowScrolling", true);

			defaultSkin = builder
					.comment("The id of the default skin to use when a player has no skin set or the skin is not found.\n" +
							"0 = alex_slim, 1 = ari_slim, 2 = efe_slim, 3 = kai_slim, 4 = makena_slim, 5 = noor_slim,\n" +
							"6 = steve_slim, 7 = sunny_slim, 8 = zuri_slim, 9 = alex_wide, 10 = ari_wide, 11 = efe_wide,\n" +
							"12 = kai_wide, 13 = makena_wide, 14 = noor_wide, 15 = steve_wide, 16 = sunny_wide, 17 = zuri_wide")
					.defineInRange("defaultSkin", 6, 0, 17);

			builder.pop();
		}
	}

	public static final ForgeConfigSpec clientSpec;
	public static final Client CLIENT;

	static {
		final Pair<Client, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(Client::new);
		clientSpec = specPair.getRight();
		CLIENT = specPair.getLeft();
	}

	@SubscribeEvent
	public static void onLoad(final ModConfigEvent configEvent) {
		PlayerBER.defaultTexture = DefaultPlayerSkin.DEFAULT_SKINS[CLIENT.defaultSkin.get()].texture();
	}

	@SubscribeEvent
	public static void onLoad(final ModConfigEvent.Loading configEvent) {
		PlayerStatuesMod.LOGGER.debug("Loaded Player Statues' config file {}", configEvent.getConfig().getFileName());
	}

	@SubscribeEvent
	public static void onFileChange(final ModConfigEvent.Reloading configEvent) {
		PlayerStatuesMod.LOGGER.warn("Player Statues' config just got changed on the file system!");
	}
}
