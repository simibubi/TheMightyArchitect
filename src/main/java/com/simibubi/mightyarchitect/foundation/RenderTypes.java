package com.simibubi.mightyarchitect.foundation;

import com.mojang.blaze3d.systems.RenderSystem;
import com.simibubi.mightyarchitect.AllSpecialTextures;

import net.minecraft.client.renderer.RenderState;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.util.ResourceLocation;

public class RenderTypes extends RenderState {

	protected static final RenderState.CullState DISABLE_CULLING = new NoCullState();

	public static RenderType getOutlineTranslucent(ResourceLocation texture, boolean cull) {
		RenderType.State rendertype$state = RenderType.State.builder()
			.setTextureState(new RenderState.TextureState(texture, false, false))
			.setTransparencyState(TRANSLUCENT_TRANSPARENCY)
			.setDiffuseLightingState(DIFFUSE_LIGHTING)
			.setAlphaState(DEFAULT_ALPHA)
			.setCullState(cull ? CULL : DISABLE_CULLING)
			.setLightmapState(LIGHTMAP)
			.setOverlayState(OVERLAY)
			.createCompositeState(true);
		return RenderType.create("outline_translucent" + (cull ? "_cull" : ""),
			DefaultVertexFormats.NEW_ENTITY, 7, 256, true, true, rendertype$state);
	}

	private static final RenderType OUTLINE_SOLID =
		RenderType.create("outline_solid", DefaultVertexFormats.NEW_ENTITY, 7, 256, true,
			false, RenderType.State.builder()
				.setTextureState(new RenderState.TextureState(AllSpecialTextures.BLANK.getLocation(), false, false))
				.setTransparencyState(NO_TRANSPARENCY)
				.setDiffuseLightingState(DIFFUSE_LIGHTING)
				.setLightmapState(LIGHTMAP)
				.setOverlayState(OVERLAY)
				.createCompositeState(true));

	public static RenderType getGlowingSolid(ResourceLocation texture) {
		RenderType.State rendertype$state = RenderType.State.builder()
			.setTextureState(new RenderState.TextureState(texture, false, false))
			.setTransparencyState(NO_TRANSPARENCY)
			.setDiffuseLightingState(NO_DIFFUSE_LIGHTING)
			.setLightmapState(LIGHTMAP)
			.setOverlayState(OVERLAY)
			.createCompositeState(true);
		return RenderType.create("glowing_solid", DefaultVertexFormats.NEW_ENTITY, 7, 256,
			true, false, rendertype$state);
	}

	public static RenderType getGlowingTranslucent(ResourceLocation texture) {
		RenderType.State rendertype$state = RenderType.State.builder()
			.setTextureState(new RenderState.TextureState(texture, false, false))
			.setTransparencyState(TRANSLUCENT_TRANSPARENCY)
			.setDiffuseLightingState(NO_DIFFUSE_LIGHTING)
			.setAlphaState(DEFAULT_ALPHA)
			.setCullState(DISABLE_CULLING)
			.setLightmapState(LIGHTMAP)
			.setOverlayState(OVERLAY)
			.createCompositeState(true);
		return RenderType.create("glowing_translucent", DefaultVertexFormats.NEW_ENTITY, 7,
			256, true, true, rendertype$state);
	}

	private static final RenderType GLOWING_SOLID = RenderTypes.getGlowingSolid(PlayerContainer.BLOCK_ATLAS);
	private static final RenderType GLOWING_TRANSLUCENT =
		RenderTypes.getGlowingTranslucent(PlayerContainer.BLOCK_ATLAS);

	private static final RenderType ITEM_PARTIAL_SOLID =
		RenderType.create("item_solid", DefaultVertexFormats.NEW_ENTITY, 7, 256, true,
			false, RenderType.State.builder()
				.setTextureState(new RenderState.TextureState(PlayerContainer.BLOCK_ATLAS, false, false))
				.setTransparencyState(NO_TRANSPARENCY)
				.setDiffuseLightingState(DIFFUSE_LIGHTING)
				.setLightmapState(LIGHTMAP)
				.setOverlayState(OVERLAY)
				.createCompositeState(true));

	private static final RenderType ITEM_PARTIAL_TRANSLUCENT = RenderType.create("entity_translucent",
		DefaultVertexFormats.NEW_ENTITY, 7, 256, true, true, RenderType.State.builder()
			.setTextureState(new RenderState.TextureState(PlayerContainer.BLOCK_ATLAS, false, false))
			.setTransparencyState(TRANSLUCENT_TRANSPARENCY)
			.setDiffuseLightingState(DIFFUSE_LIGHTING)
			.setAlphaState(DEFAULT_ALPHA)
			.setCullState(DISABLE_CULLING)
			.setLightmapState(LIGHTMAP)
			.setOverlayState(OVERLAY)
			.createCompositeState(true));

	public static RenderType getItemPartialSolid() {
		return ITEM_PARTIAL_SOLID;
	}
	
	public static RenderType getItemPartialTranslucent() {
		return ITEM_PARTIAL_TRANSLUCENT;
	}

	public static RenderType getOutlineSolid() {
		return OUTLINE_SOLID;
	}

	public static RenderType getGlowingSolid() {
		return GLOWING_SOLID;
	}

	public static RenderType getGlowingTranslucent() {
		return GLOWING_TRANSLUCENT;
	}

	protected static class NoCullState extends RenderState.CullState {
		public NoCullState() {
			super(false);
		}

		@Override
		public void setupRenderState() {
			RenderSystem.disableCull();
		}
	}

	// Mmm gimme those protected fields
	public RenderTypes() {
		super(null, null, null);
	}
}
