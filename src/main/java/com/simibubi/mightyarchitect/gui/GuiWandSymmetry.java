package com.simibubi.mightyarchitect.gui;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.platform.GlStateManager;
import com.simibubi.mightyarchitect.gui.widgets.DynamicLabel;
import com.simibubi.mightyarchitect.gui.widgets.OptionScrollArea;
import com.simibubi.mightyarchitect.gui.widgets.ScrollArea;
import com.simibubi.mightyarchitect.item.ItemWandSymmetry;
import com.simibubi.mightyarchitect.item.symmetry.SymmetryCrossPlane;
import com.simibubi.mightyarchitect.item.symmetry.SymmetryElement;
import com.simibubi.mightyarchitect.item.symmetry.SymmetryEmptySlot;
import com.simibubi.mightyarchitect.item.symmetry.SymmetryPlane;
import com.simibubi.mightyarchitect.item.symmetry.SymmetryTriplePlane;
import com.simibubi.mightyarchitect.networking.PacketNbt;
import com.simibubi.mightyarchitect.networking.Packets;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.fml.network.PacketDistributor;

public class GuiWandSymmetry extends AbstractSimiScreen {

	private ScrollArea areaType;
	private DynamicLabel labelType;
	private ScrollArea areaAlign;
	private DynamicLabel labelAlign;

	private SymmetryElement currentElement;
	private float animationProgress;
	private ItemStack wand;

	public GuiWandSymmetry(ItemStack wand) {
		super();

		currentElement = ItemWandSymmetry.getMirror(wand);
		if (currentElement instanceof SymmetryEmptySlot) {
			currentElement = new SymmetryPlane(Vec3d.ZERO);
		}
		this.wand = wand;
		animationProgress = 0;
	}

	@Override
	public void init() {
		super.init();
		this.setWindowSize(GuiResources.WAND_SYMMETRY.width + 50, GuiResources.WAND_SYMMETRY.height + 50);

		labelType = new DynamicLabel(topLeftX + 122, topLeftY + 15, "").colored(0xFFFFFFFF).withShadow();
		labelAlign = new DynamicLabel(topLeftX + 122, topLeftY + 35, "").colored(0xFFFFFFFF).withShadow();

		int state = currentElement instanceof SymmetryTriplePlane ? 2
				: currentElement instanceof SymmetryCrossPlane ? 1 : 0;
		areaType = new OptionScrollArea(topLeftX + 119, topLeftY + 12, 70, 14)
				.forOptions(SymmetryElement.TOOLTIP_ELEMENTS).titled("Type of Mirror").writingTo(labelType)
				.setState(state);

		areaType.calling(position -> {
			switch (position) {
			case 0:
				currentElement = new SymmetryPlane(currentElement.getPosition());
				break;
			case 1:
				currentElement = new SymmetryCrossPlane(currentElement.getPosition());
				break;
			case 2:
				currentElement = new SymmetryTriplePlane(currentElement.getPosition());
				break;
			default:
				break;
			}
			initAlign(currentElement);
		});

		widgets.clear();
		
		initAlign(currentElement);

		widgets.add(labelAlign);
		widgets.add(areaType);
		widgets.add(labelType);

	}

	private void initAlign(SymmetryElement element) {
		if (areaAlign != null) {
			widgets.remove(areaAlign);
		}

		areaAlign = new OptionScrollArea(topLeftX + 119, topLeftY + 32, 70, 14).forOptions(element.getAlignToolTips())
				.titled("Direction").writingTo(labelAlign).setState(element.getOrientationIndex())
				.calling(element::setOrientation);

		widgets.add(areaAlign);
	}

	@Override
	public void tick() {
		super.tick();
		animationProgress++;
	}

	@Override
	protected void renderWindow(int mouseX, int mouseY, float partialTicks) {
		GuiResources.WAND_SYMMETRY.draw(this, topLeftX, topLeftY);

		int x = topLeftX + 63;
		int y = topLeftY + 15;

		font.drawString("Symmetry", x, y, GuiResources.FONT_COLOR);
		font.drawString("Direction", x, y + 20, GuiResources.FONT_COLOR);

		minecraft.getTextureManager().bindTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE);
		GlStateManager.enableBlend();

		renderBlock();
		renderBlock();

		GlStateManager.pushLightingAttributes();
		RenderHelper.disableStandardItemLighting();
		GlStateManager.pushMatrix();
		GlStateManager.translated((this.width - this.sWidth) / 2 + 250, 250, 100);
		GlStateManager.rotatef(-30, .4f, 0, -.2f);
		GlStateManager.rotatef(90 + 0.2f * animationProgress, 0, 1, 0);
		GlStateManager.scaled(100, -100, 100);
		itemRenderer.renderItem(wand, itemRenderer.getModelWithOverrides(wand));
		GlStateManager.popMatrix();
		RenderHelper.enableStandardItemLighting();
		GlStateManager.popAttributes();
	}

	protected void renderBlock() {
		GlStateManager.pushMatrix();
		BufferBuilder buffer = Tessellator.getInstance().getBuffer();
		buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
		GlStateManager.translated(topLeftX + 15, topLeftY - 117, 20);
		GlStateManager.rotatef(-22.5f, .3f, 1f, 0f);
		GlStateManager.scaled(32, -32, 32);
		minecraft.getBlockRendererDispatcher().renderBlock(currentElement.getModel(), new BlockPos(0, -5, 0),
				minecraft.world, buffer, minecraft.world.rand, EmptyModelData.INSTANCE);

		Tessellator.getInstance().draw();
		GlStateManager.popMatrix();
	}

	@Override
	public void removed() {
		ItemStack heldItemMainhand = minecraft.player.getHeldItemMainhand();
		CompoundNBT compound = heldItemMainhand.getTag();
		compound.put(ItemWandSymmetry.$SYMMETRY, currentElement.writeToNbt());
		heldItemMainhand.setTag(compound);
		Packets.channel.send(PacketDistributor.SERVER.noArg(), new PacketNbt(heldItemMainhand));
		minecraft.player.setHeldItem(Hand.MAIN_HAND, heldItemMainhand);
		super.removed();
	}

}
