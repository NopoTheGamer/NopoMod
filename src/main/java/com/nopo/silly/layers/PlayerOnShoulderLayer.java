package com.nopo.silly.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.nopo.NopoMod;
import com.nopo.features.silly.BabyDollModel;
import com.nopo.features.silly.SmallPlayers;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.EquipmentAssetManager;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.PlayerModelType;
import net.minecraft.world.entity.player.PlayerSkin;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.Equippable;

@Environment(EnvType.CLIENT)
public class PlayerOnShoulderLayer extends RenderLayer<AvatarRenderState, PlayerModel> {
	private final HumanoidModel<AvatarRenderState> model;
	private final HumanoidModel<AvatarRenderState> modelSlim;
	private final EquipmentAssetManager equipmentAssets;


	public PlayerOnShoulderLayer(RenderLayerParent<AvatarRenderState, PlayerModel> renderLayerParent, EntityModelSet entityModelSet, EquipmentAssetManager equipmentAssetManager) {
		super(renderLayerParent);
		this.model = new PlayerModel(entityModelSet.bakeLayer(ModelLayers.PLAYER), false);
		this.modelSlim = new PlayerModel(entityModelSet.bakeLayer(ModelLayers.PLAYER_SLIM), true);
		this.equipmentAssets = equipmentAssetManager;
	}

	private boolean hasLayer(ItemStack itemStack, EquipmentClientInfo.LayerType layerType) {
		Equippable equippable = itemStack.get(DataComponents.EQUIPPABLE);
		if (equippable != null && !equippable.assetId().isEmpty()) {
			EquipmentClientInfo equipmentClientInfo = this.equipmentAssets.get(equippable.assetId().get());
			return !equipmentClientInfo.getLayers(layerType).isEmpty();
		} else {
			return false;
		}
	}

	public void submit(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int i, AvatarRenderState avatarRenderState, float f, float g) {
		if (!avatarRenderState.getDataOrDefault(BabyDollModel.getKey(), false)) return;
		this.submitOnShoulder(poseStack, submitNodeCollector, i, avatarRenderState, f, g, NopoMod.config.getBabyDollConfig().getLeft());
	}

	private void submitOnShoulder(
		PoseStack poseStack,
		SubmitNodeCollector submitNodeCollector,
		int i,
		AvatarRenderState playerRenderState,
		float f,
		float g,
		boolean left
	) {
		poseStack.pushPose();

		Boolean isPlayerBaby = playerRenderState.getDataOrDefault(SmallPlayers.getKey(), false);
		float yOffset = playerRenderState.isCrouching ? -0.3F : -0.5F;
		if (isPlayerBaby) {
			yOffset = playerRenderState.isCrouching ? 0.75F : 0.55F;
		}
		if (!this.hasLayer(playerRenderState.chestEquipment, EquipmentClientInfo.LayerType.HUMANOID)) {
			yOffset += 0.05f;
		}
		float xOffset = isPlayerBaby ? 0.3F : 0.5F;
		if (!left) xOffset *= -1f;
		poseStack.translate(xOffset, yOffset, 0.0F);

		float scale = isPlayerBaby ? 0.25f : 0.5f;
		poseStack.scale(scale, scale, scale);

		AvatarRenderState sittingRenderState = new AvatarRenderState();
		sittingRenderState.ageInTicks = playerRenderState.ageInTicks;
		sittingRenderState.yRot = f;
		sittingRenderState.xRot = g;
		sittingRenderState.isPassenger = true;

		PlayerSkin playerSkin = playerRenderState.skin;
		Identifier identifier = playerSkin.body().texturePath();
		HumanoidModel<AvatarRenderState> dollModel = playerSkin.model() == PlayerModelType.SLIM ? modelSlim : model;

		submitNodeCollector.submitModel(
				dollModel,
				sittingRenderState,
				poseStack,
				dollModel.renderType(identifier),
				i,
				OverlayTexture.NO_OVERLAY,
				playerRenderState.outlineColor,
				null
		);
		poseStack.popPose();
	}
}
