package com.simibubi.mightyarchitect.gui;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import com.simibubi.mightyarchitect.gui.widgets.DynamicLabel;
import com.simibubi.mightyarchitect.gui.widgets.ScrollArea;
import com.simibubi.mightyarchitect.gui.widgets.ScrollArea.IScrollAction;
import com.simibubi.mightyarchitect.item.ItemWandSymmetry;
import com.simibubi.mightyarchitect.networking.PacketItemNBT;
import com.simibubi.mightyarchitect.networking.PacketSender;
import com.simibubi.mightyarchitect.symmetry.SymmetryAxis;
import com.simibubi.mightyarchitect.symmetry.SymmetryCrossPlane;
import com.simibubi.mightyarchitect.symmetry.SymmetryElement;
import com.simibubi.mightyarchitect.symmetry.SymmetryEmptySlot;
import com.simibubi.mightyarchitect.symmetry.SymmetryPlane;
import com.simibubi.mightyarchitect.symmetry.SymmetryPoint;
import com.simibubi.mightyarchitect.symmetry.SymmetryTriplePlane;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;

public class GuiWandSymmetry extends GuiScreen{

	public ItemStack wand;
	public List<SymmetryElement> elements;
	private List<ScrollArea> scrollAreas;
	private int animationProgress;
	private GuiElementAnimation wandY;
	private int xSize, ySize;
	private EntityPlayer player;
	private LinkedList<DynamicLabel> labels;
	
	public GuiWandSymmetry(EntityPlayer player) {
		super();
		wand = player.getHeldItemMainhand();
		elements = ItemWandSymmetry.getMirrors(wand);
		scrollAreas = new LinkedList<>();
		labels = new LinkedList<>();
		this.xSize = 306;//256
		this.ySize = 175;
		this.player = player;
	}
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		this.drawDefaultBackground();
		this.drawBackgroundLayer(mouseX, mouseY, partialTicks);
		super.drawScreen(mouseX, mouseY, partialTicks);
		
		drawElement(16, 0, elements.get(0));
		drawElement(91, 0, elements.get(1));
		drawElement(166, 0, elements.get(2));
		
		for (ScrollArea area : scrollAreas) area.draw(this, mouseX, mouseY); 
		
        RenderHelper.disableStandardItemLighting();

        GlStateManager.pushMatrix();
		GlStateManager.translate((this.width - this.xSize) / 2 + 320, wandY.jumpInFromBelow(), 100);
		GlStateManager.rotate(-30, .4f, 0, -.2f);
		GlStateManager.rotate(90 + 0.2f * animationProgress, 0, 1, 0);
		GlStateManager.scale(300, -300, 300);
		itemRender.renderItem(wand, TransformType.GROUND);
		GlStateManager.popMatrix();
		animationProgress++;
		
		RenderHelper.enableStandardItemLighting();
		
	}
	
	private void drawElement(int x, int y, SymmetryElement element) {
		int i = (this.width - this.xSize) / 2 + x;
        int j = (this.height - this.ySize) / 2 + y;
		if (element instanceof SymmetryEmptySlot) {
			GuiResources.SYMMETRY_EMPTY.draw(this, i, j);
		} else {
			GuiResources.SYMMETRY_ELEMENT.draw(this, i, j);
			for (DynamicLabel label : labels) label.draw(this);
			
			GlStateManager.pushMatrix();
			mc.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
			GlStateManager.enableBlend();
			
			BufferBuilder buffer = Tessellator.getInstance().getBuffer();
			buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
			GlStateManager.translate(i + 24, j + 55, 20);
			GlStateManager.rotate(-22.5f, .3f, 1f, 0f);
			GlStateManager.scale(32, -32, 32);
			GlStateManager.shadeModel(GL11.GL_SMOOTH);
			mc.getBlockRendererDispatcher().renderBlock(element.getModel(), new BlockPos(0,0,0), mc.world, buffer);
			
			Tessellator.getInstance().draw();
			GlStateManager.popMatrix();
		}
	}
	
	private void drawBackgroundLayer(int mouseX, int mouseY, float partialTicks) {
		int i = (this.width - this.xSize) / 2;
        int j = (this.height - this.ySize) / 2;
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        
        GuiResources.SYMMETRY_WAND.draw(this, i, j);
	}

	@Override
	public void initGui() {
		super.initGui();
		int i = (this.width - this.xSize) / 2;
        int j = (this.height - this.ySize) / 2;
        scrollAreas.clear();
		animationProgress = 0;
		wandY = new GuiElementAnimation(height + 100, (this.height - this.ySize) / 2 + 200);

		int x = i + 16;
		for (int index = 0; index < elements.size(); index++) {
			SymmetryElement element = elements.get(index);
			final int replace_index = index;
			if (!(element instanceof SymmetryEmptySlot)) {
				
				
				ScrollArea image = new ScrollArea(SymmetryElement.TOOLTIP_ELEMENTS, new IScrollAction() {
					@Override public void onScroll(int position) {
						switch (position) {
						case 0:
							elements.set(replace_index, new SymmetryPlane(element.getPosition()));
							break;
						case 1: 
							elements.set(replace_index, new SymmetryAxis(element.getPosition()));
							break;
						case 2: 
							elements.set(replace_index, new SymmetryCrossPlane(element.getPosition()));
							break;
						case 3: 
							elements.set(replace_index, new SymmetryTriplePlane(element.getPosition()));
							break;
						case 4: 
							elements.set(replace_index, new SymmetryPoint(element.getPosition()));
							break;
						}
						labels.get(replace_index).text = elements.get(replace_index).getOrientation().getName();
					}
				});
				image.setBounds(x + 4, j + 20, 59, 43);
				image.setState(((element instanceof SymmetryPlane) ? 0
						: (element instanceof SymmetryAxis) ? 1
								: (element instanceof SymmetryCrossPlane) ? 2
										: (element instanceof SymmetryTriplePlane) ? 3 : 4));
				image.setTitle("Type of Symmetrical Operation");
				scrollAreas.add(image);
				
				ScrollArea rotation = new ScrollArea(new IScrollAction() {
					@Override public void onScroll(int position) {
						elements.get(replace_index).rotate(position > 0);
						labels.get(replace_index).text = elements.get(replace_index).getOrientation().getName();
				}});
				rotation.setBounds(x + 4, j + 65, 59, 26);
				rotation.setTitle("Cycle Orientation");
				scrollAreas.add(rotation);
				
				DynamicLabel label = new DynamicLabel(x + 16, j + 73);
				label.text = element.getOrientation().getName();
				labels.add(label);
				
				
			}
			x += 75;
		}
	}
	
	@Override
	public boolean doesGuiPauseGame() {
		return false;
	}
	
	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		super.mouseClicked(mouseX, mouseY, mouseButton);
		
		int scrollAmount = ((mouseButton == 0)? -1 : 1) * ((Keyboard.isKeyDown(Keyboard.KEY_LSHIFT))? 5 : 1);
		for (ScrollArea area : scrollAreas) {
			area.tryScroll(mouseX, mouseY, scrollAmount);
		}
		
	}

	@Override
	public void handleMouseInput() throws IOException {
		super.handleMouseInput();
		
		int i = Mouse.getEventX() * this.width / this.mc.displayWidth;
		int j = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1;
		
		int scroll = Mouse.getEventDWheel();
		if (scroll != 0) {
			for (ScrollArea area : scrollAreas) {
				area.tryScroll(i, j, (int) (scroll / -120f));
			}
		}
	}
	
	@Override
	public void onGuiClosed() {
		super.onGuiClosed();
		NBTTagList elements = new NBTTagList();
		
		for (SymmetryElement element : this.elements) 
			elements.appendTag(element.writeToNbt());
		
		wand.getTagCompound().setTag(ItemWandSymmetry.$SYMMETRY_ELEMS, elements);
		PacketSender.INSTANCE.sendToServer(new PacketItemNBT(wand));
		player.setHeldItem(EnumHand.MAIN_HAND, wand);
	}
	
}
