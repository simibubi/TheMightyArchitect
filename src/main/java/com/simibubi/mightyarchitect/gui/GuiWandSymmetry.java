package com.simibubi.mightyarchitect.gui;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.platform.GlStateManager;
import com.simibubi.mightyarchitect.gui.widgets.DynamicLabel;
import com.simibubi.mightyarchitect.gui.widgets.ScrollArea;
import com.simibubi.mightyarchitect.gui.widgets.ScrollArea.IScrollAction;
import com.simibubi.mightyarchitect.item.ItemWandSymmetry;
import com.simibubi.mightyarchitect.item.symmetry.SymmetryCrossPlane;
import com.simibubi.mightyarchitect.item.symmetry.SymmetryElement;
import com.simibubi.mightyarchitect.item.symmetry.SymmetryEmptySlot;
import com.simibubi.mightyarchitect.item.symmetry.SymmetryPlane;
import com.simibubi.mightyarchitect.item.symmetry.SymmetryTriplePlane;
import com.simibubi.mightyarchitect.networking.PacketNbt;
import com.simibubi.mightyarchitect.networking.Packets;

import net.minecraft.client.gui.screen.Screen;
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
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.fml.network.PacketDistributor;

public class GuiWandSymmetry extends Screen {

	private int xSize, ySize;
	private int xTopLeft, yTopLeft;

	private ScrollArea areaType;
	private DynamicLabel labelType;
	private ScrollArea areaAlign;
	private DynamicLabel labelAlign;

	private SymmetryElement currentElement;
	private float animationProgress;
	private ItemStack wand;

	public GuiWandSymmetry(ItemStack wand) {
		super(new StringTextComponent("Symmetry Wand"));

		currentElement = ItemWandSymmetry.getMirror(wand);
		if (currentElement instanceof SymmetryEmptySlot) {
			currentElement = new SymmetryPlane(Vec3d.ZERO);
		}
		this.wand = wand;
		animationProgress = 0;
	}

	@Override
	public void init() {
		xSize = GuiResources.WAND_SYMMETRY.width + 50;
		ySize = GuiResources.WAND_SYMMETRY.height + 50;
		xTopLeft = (this.width - this.xSize) / 2;
		yTopLeft = (this.height - this.ySize) / 2;

		labelType = new DynamicLabel(xTopLeft + 122, yTopLeft + 15);
		labelAlign = new DynamicLabel(xTopLeft + 122, yTopLeft + 35);

		areaType = new ScrollArea(SymmetryElement.TOOLTIP_ELEMENTS, new IScrollAction() {
			@Override
			public void onScroll(int position) {
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
				labelType.text = SymmetryElement.TOOLTIP_ELEMENTS.get(position);
				initAlign(currentElement);
			}

		});
		areaType.setBounds(xTopLeft + 119, yTopLeft + 12, 70, 14);
		areaType.setState(currentElement instanceof SymmetryTriplePlane ? 2
				: currentElement instanceof SymmetryCrossPlane ? 1 : 0);
		areaType.setTitle("Type of Mirror");
		labelType.text = SymmetryElement.TOOLTIP_ELEMENTS.get(areaType.getState());
		initAlign(currentElement);

	}

	private void initAlign(SymmetryElement element) {
		areaAlign = new ScrollArea(element.getAlignToolTips(), new IScrollAction() {
			@Override
			public void onScroll(int position) {
				element.setOrientation(position);
				labelAlign.text = element.getAlignToolTips().get(position);
			}
		});
		areaAlign.setBounds(xTopLeft + 119, yTopLeft + 32, 70, 14);
		areaAlign.setState(element.getOrientationIndex());
		areaAlign.setTitle("Direction");
		labelAlign.text = element.getAlignToolTips().get(element.getOrientationIndex());
	}

	@Override
	public void render(int mouseX, int mouseY, float partialTicks) {
		renderBackground();

		GuiResources.WAND_SYMMETRY.draw(this, xTopLeft, yTopLeft);

		int x = xTopLeft + 63;
		int y = yTopLeft + 15;

		drawString(font, "Symmetry", x, y, GuiResources.FONT_COLOR);
		drawString(font, "Direction", x, y + 20, GuiResources.FONT_COLOR);
		labelType.draw(this);
		labelAlign.draw(this);
		areaType.draw(this, mouseX, mouseY);
		areaAlign.draw(this, mouseX, mouseY);

		super.render(mouseX, mouseY, partialTicks);
		animationProgress++;

		minecraft.getTextureManager().bindTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE);
		GlStateManager.enableBlend();

		renderBlock();
		renderBlock();

		RenderHelper.disableStandardItemLighting();
		GlStateManager.pushMatrix();
		GlStateManager.translated((this.width - this.xSize) / 2 + 250, 300, 100);
		GlStateManager.rotatef(-30, .4f, 0, -.2f);
		GlStateManager.rotatef(90 + 0.2f * animationProgress, 0, 1, 0);
		GlStateManager.scaled(300, -300, 300);
		itemRenderer.renderItem(wand, itemRenderer.getModelWithOverrides(wand));
		GlStateManager.popMatrix();
		RenderHelper.enableStandardItemLighting();

	}

	protected void renderBlock() {
		GlStateManager.pushMatrix();
		BufferBuilder buffer = Tessellator.getInstance().getBuffer();
		buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
		GlStateManager.translated(xTopLeft + 18, yTopLeft + 42, 20);
		GlStateManager.rotatef(-22.5f, .3f, 1f, 0f);
		GlStateManager.scaled(32, -32, 32);
		GlStateManager.shadeModel(GL11.GL_SMOOTH);
		minecraft.getBlockRendererDispatcher().renderBlock(currentElement.getModel(), new BlockPos(0, 0, 0),
				minecraft.world, buffer, minecraft.world.rand, EmptyModelData.INSTANCE);

		Tessellator.getInstance().draw();
		GlStateManager.popMatrix();
	}

	@Override
	public void onClose() {
		ItemStack heldItemMainhand = minecraft.player.getHeldItemMainhand();
		CompoundNBT compound = heldItemMainhand.getTag();
		compound.put(ItemWandSymmetry.$SYMMETRY, currentElement.writeToNbt());
		heldItemMainhand.setTag(compound);
		Packets.channel.send(PacketDistributor.SERVER.noArg(), new PacketNbt(heldItemMainhand));
		minecraft.player.setHeldItem(Hand.MAIN_HAND, heldItemMainhand);
		super.onClose();
	}

	@Override
	public boolean mouseScrolled(double x, double y, double scroll) {
		if (scroll != 0) {
			areaAlign.tryScroll(x, y, (int) (scroll / -120f));
			areaType.tryScroll(x, y, (int) (scroll / -120f));
		}

		return super.mouseScrolled(x, y, scroll);
	}

}
