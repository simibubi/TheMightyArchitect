package com.simibubi.mightyarchitect.foundation;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.simibubi.mightyarchitect.AllSpecialTextures;

import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.simibubi.mightyarchitect.TheMightyArchitect;

import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.resources.ResourceLocation;

public class RenderTypes extends RenderStateShard {

	protected static final RenderStateShard.CullStateShard DISABLE_CULLING = new NoCullState();

	public static RenderType getOutlineSolid(ResourceLocation texture) {
		return RenderType.create(createLayerName("outline_solid"), DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 256, true,
				false, RenderType.CompositeState.builder()
						.setShaderState(RenderStateShard.RENDERTYPE_ENTITY_CUTOUT_SHADER)
						.setTextureState(new RenderStateShard.TextureStateShard(texture, false, false))
						.setLightmapState(LIGHTMAP)
						.setOverlayState(OVERLAY)
						.createCompositeState(true));
	}

	private static final RenderType DEFAULT_OUTLINE_SOLID =
			getOutlineSolid(AllSpecialTextures.BLANK.getLocation());

	public static RenderType getOutlineSolid() {
		return DEFAULT_OUTLINE_SOLID;
	}

	public static RenderType getOutlineTranslucent(ResourceLocation texture, boolean cull) {
		return RenderType.create(createLayerName("outline_translucent" + (cull ? "_cull" : "")),
				DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 256, true, true, RenderType.CompositeState.builder()
						.setShaderState(RENDERTYPE_ENTITY_TRANSLUCENT_SHADER)
						.setTextureState(new RenderStateShard.TextureStateShard(texture, false, false))
						.setTransparencyState(TRANSLUCENT_TRANSPARENCY)
						.setCullState(cull ? CULL : NO_CULL)
						.setLightmapState(LIGHTMAP)
						.setOverlayState(OVERLAY)
						.setWriteMaskState(RenderStateShard.COLOR_WRITE)
						.createCompositeState(true));
	}

	public static RenderType getGlowingSolid(ResourceLocation texture) {
		return RenderType.create(createLayerName("glowing_solid"), DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 256,
				true, false, RenderType.CompositeState.builder()
						.setShaderState(NEW_ENTITY_SHADER)
						.setTextureState(new RenderStateShard.TextureStateShard(texture, false, false))
						.setLightmapState(LIGHTMAP)
						.setOverlayState(OVERLAY)
						.createCompositeState(true));
	}

	private static final RenderType GLOWING_SOLID_DEFAULT = getGlowingSolid(InventoryMenu.BLOCK_ATLAS);

	public static RenderType getGlowingSolid() {
		return GLOWING_SOLID_DEFAULT;
	}

	public static RenderType getGlowingTranslucent(ResourceLocation texture) {
		return RenderType.create(createLayerName("glowing_translucent"), DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS,
				256, true, true, RenderType.CompositeState.builder()
						.setShaderState(NEW_ENTITY_SHADER)
						.setTextureState(new RenderStateShard.TextureStateShard(texture, false, false))
						.setTransparencyState(TRANSLUCENT_TRANSPARENCY)
						.setCullState(NO_CULL)
						.setLightmapState(LIGHTMAP)
						.setOverlayState(OVERLAY)
						.createCompositeState(true));
	}

	private static final RenderType GLOWING_TRANSLUCENT_DEFAULT = getGlowingTranslucent(InventoryMenu.BLOCK_ATLAS);

	public static RenderType getGlowingTranslucent() {
		return GLOWING_TRANSLUCENT_DEFAULT;
	}

	private static final RenderType ITEM_PARTIAL_SOLID =
			RenderType.create(createLayerName("item_partial_solid"), DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 256, true,
					false, RenderType.CompositeState.builder()
							.setShaderState(RENDERTYPE_ENTITY_SOLID_SHADER)
							.setTextureState(BLOCK_SHEET)
							.setTransparencyState(NO_TRANSPARENCY)
							.setLightmapState(LIGHTMAP)
							.setOverlayState(OVERLAY)
							.createCompositeState(true));

	public static RenderType getItemPartialSolid() {
		return ITEM_PARTIAL_SOLID;
	}

	private static final RenderType ITEM_PARTIAL_TRANSLUCENT = RenderType.create(createLayerName("item_partial_translucent"),
			DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 256, true, true, RenderType.CompositeState.builder()
					.setShaderState(RENDERTYPE_ENTITY_TRANSLUCENT_SHADER)
					.setTextureState(BLOCK_SHEET)
					.setTransparencyState(TRANSLUCENT_TRANSPARENCY)
					.setLightmapState(LIGHTMAP)
					.setOverlayState(OVERLAY)
					.createCompositeState(true));

	public static RenderType getItemPartialTranslucent() {
		return ITEM_PARTIAL_TRANSLUCENT;
	}

	private static final RenderType FLUID = RenderType.create(createLayerName("fluid"),
			DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 256, true, true, RenderType.CompositeState.builder()
					.setShaderState(RENDERTYPE_ENTITY_TRANSLUCENT_SHADER)
					.setTextureState(BLOCK_SHEET_MIPPED)
					.setTransparencyState(TRANSLUCENT_TRANSPARENCY)
					.setLightmapState(LIGHTMAP)
					.setOverlayState(OVERLAY)
					.createCompositeState(true));

	public static RenderType getFluid() {
		return FLUID;
	}

	protected static class NoCullState extends RenderStateShard.CullStateShard {
		public NoCullState() {
			super(false);
		}

		@Override
		public void setupRenderState() {
			RenderSystem.disableCull();
		}
	}

	private static String createLayerName(String name) {
		return TheMightyArchitect.ID + ":" + name;
	}

	// Mmm gimme those protected fields
	public RenderTypes() {
		super(null, null, null);
	}
}
