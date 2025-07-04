package com.shynieke.playerstatues.client.renderer;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.minecraft.MinecraftProfileTexture.Type;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.shynieke.playerstatues.client.model.PlayerStatueModel;
import com.shynieke.playerstatues.entity.PlayerStatuesStatue;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.client.renderer.entity.layers.ElytraLayer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.client.resources.SkinManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class PlayerStatueRenderer extends LivingEntityRenderer<PlayerStatuesStatue, PlayerStatueModel> {
	private final PlayerStatueModel playerModel;
	private final PlayerStatueModel slimPlayerModel;
	public static final ResourceLocation defaultTexture = DefaultPlayerSkin.getDefaultSkin();

	public PlayerStatueRenderer(EntityRendererProvider.Context context) {
		this(context, false);
	}

	public PlayerStatueRenderer(EntityRendererProvider.Context context, boolean slim) {
		super(context, new PlayerStatueModel(context.bakeLayer(ModelLayers.PLAYER), slim), 0.0F);
		this.playerModel = new PlayerStatueModel(context.bakeLayer(ModelLayers.PLAYER), false);
		this.slimPlayerModel = new PlayerStatueModel(context.bakeLayer(ModelLayers.PLAYER_SLIM), true);

		this.addLayer(new HumanoidArmorLayer<>(this,
				new HumanoidModel<>(context.bakeLayer(slim ? ModelLayers.PLAYER_SLIM_INNER_ARMOR : ModelLayers.PLAYER_INNER_ARMOR)),
				new HumanoidModel<>(context.bakeLayer(slim ? ModelLayers.PLAYER_SLIM_OUTER_ARMOR : ModelLayers.PLAYER_OUTER_ARMOR)), context.getModelManager()));
		this.addLayer(new ItemInHandLayer<>(this, context.getItemInHandRenderer()));
		this.addLayer(new ElytraLayer<>(this, context.getModelSet()));
		this.addLayer(new CustomHeadLayer<>(this, context.getModelSet(), context.getItemInHandRenderer()));
	}

	@NotNull
	@Override
	public ResourceLocation getTextureLocation(PlayerStatuesStatue playerStatue) {
		return playerStatue.getGameProfile()
				.map(this::getSkin)
				.orElse(defaultTexture);
	}

	private ResourceLocation getSkin(GameProfile gameProfile) {
		if (!gameProfile.isComplete()) {
			return defaultTexture;
		} else {
			final Minecraft minecraft = Minecraft.getInstance();
			SkinManager skinManager = minecraft.getSkinManager();
			final Map<Type, MinecraftProfileTexture> loadSkinFromCache = skinManager.getInsecureSkinInformation(gameProfile); // returned map may or may not be typed
			if (loadSkinFromCache.containsKey(Type.SKIN)) {
				return skinManager.registerTexture(loadSkinFromCache.get(Type.SKIN), Type.SKIN);
			} else {
				return DefaultPlayerSkin.getDefaultSkin(gameProfile.getId());
			}
		}
	}

	@Override
	public void render(PlayerStatuesStatue playerStatue, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int packedLightIn) {
		this.model = playerStatue.isSlim() ? this.slimPlayerModel : playerModel;
		poseStack.translate(0, playerStatue.getYOffsetData(), 0);
		if (playerStatue.clientLock > 0) {
			playerStatue.xRotO = playerStatue.yBodyRot;
			playerStatue.yHeadRotO = playerStatue.yHeadRot;
		}
		super.render(playerStatue, entityYaw, partialTicks, poseStack, bufferSource, packedLightIn);
	}

	@Override
	protected boolean shouldShowName(PlayerStatuesStatue playerStatue) {
		return playerStatue.isCustomNameVisible();
	}

	@Override
	protected void setupRotations(PlayerStatuesStatue playerStatue, PoseStack poseStack, float ageInTicks, float rotationYaw, float partialTicks) {
		poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - rotationYaw));
		float f = (float) (playerStatue.level().getGameTime() - playerStatue.punchCooldown) + partialTicks;
		if (f < 5.0F) {
			poseStack.mulPose(Axis.YP.rotationDegrees(Mth.sin(f / 1.5F * (float) Math.PI) * 3.0F));
		}

		if (isPlayerUpsideDown(playerStatue)) {
			poseStack.translate(0.0D, (double) (playerStatue.getBbHeight() + 0.1F), 0.0D);
			poseStack.mulPose(Axis.ZP.rotationDegrees(180.0F));
		}
	}

	@Override
	protected void scale(PlayerStatuesStatue playerStatue, PoseStack poseStack, float partialTickTime) {
		float f = 0.9375F;
		poseStack.scale(f, f, f);
	}

	public static boolean isPlayerUpsideDown(PlayerStatuesStatue playerStatue) {
		if (playerStatue.getGameProfile().isPresent()) {
			GameProfile profile = playerStatue.getGameProfile().get();
			String s = ChatFormatting.stripFormatting(profile.getName());
			if ("Dinnerbone".equals(s) || "Grumm".equals(s)) {
				return true;
			}
		}

		return false;
	}
}
