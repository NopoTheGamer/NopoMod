package com.nopo.silly.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.nopo.features.silly.SmallPlayers;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.player.PlayerCapeModel;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.EquipmentAssetManager;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.player.PlayerSkin;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.Equippable;

public class BabyCapeLayer extends RenderLayer<AvatarRenderState, PlayerModel> {
	private final HumanoidModel<AvatarRenderState> model;
	private final EquipmentAssetManager equipmentAssets;

	public BabyCapeLayer(
		RenderLayerParent<AvatarRenderState, PlayerModel> renderLayerParent, EntityModelSet entityModelSet, EquipmentAssetManager equipmentAssetManager
	) {
		super(renderLayerParent);
		this.model = new PlayerCapeModel(entityModelSet.bakeLayer(ModelLayers.PLAYER_CAPE));
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
		Boolean isBaby = avatarRenderState.getDataOrDefault(SmallPlayers.getKey(), false);
		if (!avatarRenderState.isInvisible && avatarRenderState.showCape && isBaby) {
			PlayerSkin playerSkin = avatarRenderState.skin;
			if (playerSkin.cape() != null) {
				if (!this.hasLayer(avatarRenderState.chestEquipment, EquipmentClientInfo.LayerType.WINGS)) {
					poseStack.pushPose();
					poseStack.translate(0, .75f, -.1);
					if (this.hasLayer(avatarRenderState.chestEquipment, EquipmentClientInfo.LayerType.HUMANOID)) {
						poseStack.translate(0.0F, -0.053125F, 0.06875F);
					}

					submitNodeCollector.submitModel(
						this.model,
						avatarRenderState,
						poseStack,
						RenderTypes.entitySolid(playerSkin.cape().texturePath()),
						i,
						OverlayTexture.NO_OVERLAY,
						avatarRenderState.outlineColor,
						null
					);
					poseStack.popPose();
				}
			}
		}
	}
}
