package com.shynieke.playerstatues.config;

import com.shynieke.playerstatues.PlayerStatuesMod;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import org.apache.commons.lang3.tuple.Pair;

public class PlayerStatuesConfig {

	public static class Client {
		public final BooleanValue allowScrolling;

		Client(ForgeConfigSpec.Builder builder) {
			builder.comment("Client settings")
					.push("Client");

			allowScrolling = builder
					.comment("Allow scrolling to increase / decrease an angle value in the posing screen")
					.define("allowScrolling", true);

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
	public static void onLoad(final ModConfigEvent.Loading configEvent) {
		PlayerStatuesMod.LOGGER.debug("Loaded Player Statues' config file {}", configEvent.getConfig().getFileName());
	}

	@SubscribeEvent
	public static void onFileChange(final ModConfigEvent.Reloading configEvent) {
		PlayerStatuesMod.LOGGER.warn("Player Statues' config just got changed on the file system!");
	}
}
