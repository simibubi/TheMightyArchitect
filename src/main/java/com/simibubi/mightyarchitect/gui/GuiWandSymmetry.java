package com.simibubi.mightyarchitect.gui;

import java.io.IOException;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

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
import com.simibubi.mightyarchitect.networking.PacketSender;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class GuiWandSymmetry extends GuiScreen {

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
		currentElement = ItemWandSymmetry.getMirror(wand);
		if (currentElement instanceof SymmetryEmptySlot) {
			currentElement = new SymmetryPlane(Vec3d.ZERO);
		}
		this.wand = wand;
		animationProgress = 0;
	}

	@Override
	public void initGui() {
		super.initGui();
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
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		drawDefaultBackground();
		
		GuiResources.WAND_SYMMETRY.draw(this, xTopLeft, yTopLeft);

		int x = xTopLeft + 63;
		int y = yTopLeft + 15;

		fontRenderer.drawString("Symmetry", x, y, GuiResources.FONT_COLOR, false);
		fontRenderer.drawString("Direction", x, y + 20, GuiResources.FONT_COLOR, false);
		labelType.draw(this);
		labelAlign.draw(this);
		areaType.draw(this, mouseX, mouseY);
		areaAlign.draw(this, mouseX, mouseY);

		super.drawScreen(mouseX, mouseY, partialTicks);
		animationProgress ++;

		mc.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
		GlStateManager.enableBlend();
		
		renderBlock();
		renderBlock();
		
		RenderHelper.disableStandardItemLighting();
        GlStateManager.pushMatrix();
		GlStateManager.translate((this.width - this.xSize) / 2 + 250, 300, 100);
		GlStateManager.rotate(-30, .4f, 0, -.2f);
		GlStateManager.rotate(90 + 0.2f * animationProgress, 0, 1, 0);
		GlStateManager.scale(300, -300, 300);
		itemRender.renderItem(wand, TransformType.GROUND);
		GlStateManager.popMatrix();
		RenderHelper.enableStandardItemLighting();
		
	}

	protected void renderBlock() {
		GlStateManager.pushMatrix();
		BufferBuilder buffer = Tessellator.getInstance().getBuffer();
		buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
		GlStateManager.translate(xTopLeft + 18, yTopLeft + 42, 20);
		GlStateManager.rotate(-22.5f, .3f, 1f, 0f);
		GlStateManager.scale(32, -32, 32);
		GlStateManager.shadeModel(GL11.GL_SMOOTH);
		mc.getBlockRendererDispatcher().renderBlock(currentElement.getModel(), new BlockPos(0,0,0), mc.world, buffer);
		
		Tessellator.getInstance().draw();
		GlStateManager.popMatrix();
	}

	@Override
	public void onGuiClosed() {
		ItemStack heldItemMainhand = mc.player.getHeldItemMainhand();
		NBTTagCompound compound = heldItemMainhand.getTagCompound();
		compound.setTag(ItemWandSymmetry.$SYMMETRY, currentElement.writeToNbt());
		heldItemMainhand.setTagCompound(compound);
		PacketSender.INSTANCE.sendToServer(new PacketNbt(heldItemMainhand));
		mc.player.setHeldItem(EnumHand.MAIN_HAND, heldItemMainhand);
		super.onGuiClosed();
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		super.mouseClicked(mouseX, mouseY, mouseButton);

		int scrollAmount = ((mouseButton == 0) ? -1 : 1) * ((Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) ? 5 : 1);
		areaAlign.tryScroll(mouseX, mouseY, scrollAmount);
		areaType.tryScroll(mouseX, mouseY, scrollAmount);

	}

	@Override
	public void handleMouseInput() throws IOException {
		super.handleMouseInput();

		int i = Mouse.getEventX() * this.width / this.mc.displayWidth;
		int j = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1;

		int scroll = Mouse.getEventDWheel();
		if (scroll != 0) {
			areaAlign.tryScroll(i, j, (int) (scroll / -120f));
			areaType.tryScroll(i, j, (int) (scroll / -120f));
		}
	}

}
