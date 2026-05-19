package com.nopo.silly.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.nopo.features.silly.SmallPlayers;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.animal.parrot.ParrotModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.ParrotRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.entity.state.ParrotRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.animal.parrot.Parrot;

@Environment(EnvType.CLIENT)
public class ParrotOnBabyShoulderLayer extends RenderLayer<AvatarRenderState, PlayerModel> {
	private final ParrotModel model;

	public ParrotOnBabyShoulderLayer(RenderLayerParent<AvatarRenderState, PlayerModel> renderLayerParent, EntityModelSet entityModelSet) {
		super(renderLayerParent);
		this.model = new ParrotModel(entityModelSet.bakeLayer(ModelLayers.PARROT));
	}

	public void submit(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int i, AvatarRenderState avatarRenderState, float f, float g) {
		if (!avatarRenderState.getDataOrDefault(SmallPlayers.getKey(), false)) return;
		Parrot.Variant variant = avatarRenderState.parrotOnLeftShoulder;
		if (variant != null) {
			this.submitOnShoulder(poseStack, submitNodeCollector, i, avatarRenderState, variant, f, g, true);
		}

		Parrot.Variant variant2 = avatarRenderState.parrotOnRightShoulder;
		if (variant2 != null) {
			this.submitOnShoulder(poseStack, submitNodeCollector, i, avatarRenderState, variant2, f, g, false);
		}
	}

	private void submitOnShoulder(
		PoseStack poseStack,
		SubmitNodeCollector submitNodeCollector,
		int i,
		AvatarRenderState playerRenderState,
		Parrot.Variant variant,
		float f,
		float g,
		boolean left
	) {
		poseStack.pushPose();
		float yOffset = playerRenderState.isCrouching ? 0.75F : 0.55F;
		yOffset -= 0.8f;
		float xOffset = 0.3F;
		if (!left) xOffset *= -1f;
		poseStack.translate(xOffset, yOffset, 0.0F);
		poseStack.scale(0.7f, 0.7f, 1);
		ParrotRenderState parrotRenderState = new ParrotRenderState();
		parrotRenderState.pose = ParrotModel.Pose.ON_SHOULDER;
		parrotRenderState.ageInTicks = playerRenderState.ageInTicks;
		parrotRenderState.walkAnimationPos = playerRenderState.walkAnimationPos;
		parrotRenderState.walkAnimationSpeed = playerRenderState.walkAnimationSpeed;
		parrotRenderState.yRot = f;
		parrotRenderState.xRot = g;
		submitNodeCollector.submitModel(
			this.model,
			parrotRenderState,
			poseStack,
			this.model.renderType(ParrotRenderer.getVariantTexture(variant)),
			i,
			OverlayTexture.NO_OVERLAY,
			playerRenderState.outlineColor,
			null
		);
		poseStack.popPose();
	}
}
