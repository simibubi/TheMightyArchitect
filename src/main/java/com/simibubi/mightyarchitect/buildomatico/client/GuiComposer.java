package com.simibubi.mightyarchitect.buildomatico.client;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import com.google.common.collect.Lists;
import com.simibubi.mightyarchitect.buildomatico.model.groundPlan.Cuboid;
import com.simibubi.mightyarchitect.buildomatico.model.sketch.DesignLayer;
import com.simibubi.mightyarchitect.buildomatico.model.sketch.DesignTheme;
import com.simibubi.mightyarchitect.gui.GuiResources;
import com.simibubi.mightyarchitect.gui.widgets.DynamicLabel;
import com.simibubi.mightyarchitect.gui.widgets.ScrollArea;
import com.simibubi.mightyarchitect.gui.widgets.ScrollArea.ICancelableScrollAction;
import com.simibubi.mightyarchitect.gui.widgets.ScrollArea.IScrollAction;
import com.simibubi.mightyarchitect.gui.widgets.SimiButton;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

public class GuiComposer extends GuiScreen {
	
	private static final int BUTTON_ADD_LAYER = 0;
	private List<GuiComposerPartial> partials;
	private int xSize, ySize;
	private int xTopLeft, yTopLeft;
	private int topLayer;
	private Cuboid anchor;
	
	public GuiComposer(Cuboid anchor) {
		mc = Minecraft.getMinecraft();
		this.anchor = anchor;
	}

	private void init(Cuboid anchor) {
		Cuboid c = anchor;
		partials = new ArrayList<>();
		buttonList.clear();
		topLayer = 0;
		while (c != null) {
			partials.add(new GuiComposerPartial(this, c, topLayer));
			c = c.getCuboidAbove();
			topLayer++;
		}
		topLayer--;
		xSize = 256;
		ySize = 58 + (topLayer) * 52 + 20;
		xTopLeft = (this.width - this.xSize) / 2;
        yTopLeft = (this.height - this.ySize) / 2;
	}
	
	@Override
	public void initGui() {
		init(anchor);
		if (topLayer < 4)
			addButton(new SimiButton(BUTTON_ADD_LAYER, xTopLeft + 2, yTopLeft, GuiResources.ICON_ADD));
        
        for (int layer = 0; layer < partials.size(); layer++) {
			GuiComposerPartial partial = partials.get(layer);
			int offset = (partials.size() - layer - 1) * 52 + 20;
			partial.initGui(offset);
		}
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		this.drawDefaultBackground();
		
		for (int layer = 0; layer < partials.size(); layer++) {
			GuiComposerPartial partial = partials.get(layer);
			int offset = (partials.size() - layer - 1) * 52 + 20;
			partial.drawScreen(offset, mouseX, mouseY, partialTicks);
		}
		
		super.drawScreen(mouseX, mouseY, partialTicks);
		
		for (GuiComposerPartial partial : partials) {
			for (ScrollArea area : partial.scrollAreas) {
				area.draw(this, mouseX, mouseY);
			}
		}
		
	}
	
	public void updateAllPositioningLabels() {
		for (GuiComposerPartial partial : partials)
			partial.updatePositioningLabels();
	}
	
	class GuiComposerPartial {
		private List<ScrollArea> scrollAreas;
		private Cuboid cuboid;
		private GuiComposer parent;
		private int layer;
		
		private Vector<DynamicLabel> labelPosition;
		private Vector<DynamicLabel> labelSize;
		private DynamicLabel style;
		private DynamicLabel styleGroup;
		private DynamicLabel paletteGroup;
		
		public GuiComposerPartial(GuiComposer parent, Cuboid cuboid, int layer) {
			scrollAreas = new ArrayList<>();
			this.cuboid = cuboid;
			this.layer = layer;
			this.parent = parent;
			labelPosition = new Vector<>(3);
			labelSize = new Vector<>(3);
		}
		
		public void initGui(int yOffset) {
			scrollAreas.clear();
			int x = parent.xTopLeft;
			int y = parent.yTopLeft + yOffset;
			
			style = new DynamicLabel(x + 78, y + 15);
			styleGroup = new DynamicLabel(x + 131, y + 35);
			paletteGroup = new DynamicLabel(x + 92, y + 35);
			
			initPosAndSizeFields(x, y);
			
			initStyleAndStyleGroupFields(x, y);
		}

		private void initStyleAndStyleGroupFields(int x, int y) {
			DesignTheme theme = GroundPlannerClient.getInstance().getGroundPlan().getTheme();
			List<DesignLayer> layers = theme.getLayers(); 
			List<String> styleOptions = new ArrayList<>();
			layers.forEach(layer -> { styleOptions.add(layer.getDisplayName()); });
			
			ScrollArea styleScrollArea = new ScrollArea(styleOptions, new IScrollAction() {
				@Override
				public void onScroll(int position) {
					cuboid.designLayer = theme.getLayers().get(position);
					style.text = styleOptions.get(position);
				}
			});
			styleScrollArea.setBounds(x + 75, y + 12, 70, 14);
			styleScrollArea.setTitle("Build style");
			styleScrollArea.setState(layers.indexOf(cuboid.designLayer));
			style.text = cuboid.designLayer.getDisplayName();
			scrollAreas.add(styleScrollArea);
			
			List<String> paletteOptions = Lists.newArrayList("Primary", "Secondary");
			ScrollArea palleteScrollArea = new ScrollArea(paletteOptions, new IScrollAction() {
				@Override
				public void onScroll(int position) {
					cuboid.setSecondary(position == 1);
					paletteGroup.text = cuboid.isSecondary()? "2nd" : "1st";
				}
			});
			palleteScrollArea.setBounds(x + 87, y + 32, 30, 14);
			palleteScrollArea.setTitle("Palette used");
			palleteScrollArea.setState(cuboid.isSecondary()? 1 : 0);
			paletteGroup.text = cuboid.isSecondary()? "2nd" : "1st";
			scrollAreas.add(palleteScrollArea);
			
			ScrollArea styleGroupScrollArea = new ScrollArea(0, 5, new IScrollAction() {
				@Override
				public void onScroll(int position) {
					char groupChar = (position < 4)? (char) (position + 'A') : 'U';
					cuboid.styleGroup = groupChar;
					styleGroup.text = "" + groupChar;
				}
			});
			styleGroupScrollArea.setBounds(x + 123, y + 32, 22, 14);
			styleGroupScrollArea.setTitle("Style group");
			styleGroupScrollArea.setState((cuboid.styleGroup >= 'A' && cuboid.styleGroup <= 'D')? cuboid.styleGroup - 'A' : 5);
			styleGroup.text = "" + ((styleGroupScrollArea.getState() < 4)? (char) (styleGroupScrollArea.getState() + 'A') : 'U');
			scrollAreas.add(styleGroupScrollArea);
		}

		private void initPosAndSizeFields(int x, int y) {
			labelPosition.addElement(new DynamicLabel(x + 179, y + 15));
			labelPosition.addElement(new DynamicLabel(x + 203, y + 15));
			labelPosition.addElement(new DynamicLabel(x + 227, y + 15));

			labelSize.addElement(new DynamicLabel(x + 179, y + 35));
			labelSize.addElement(new DynamicLabel(x + 203, y + 35));
			labelSize.addElement(new DynamicLabel(x + 227, y + 35));
			
			Vector<Integer> pos = new Vector<>(3);
			pos.addElement(cuboid.x);
			pos.addElement(cuboid.y);
			pos.addElement(cuboid.z);
			
			Vector<Integer> size = new Vector<>(3);
			size.addElement(cuboid.width);
			size.addElement(cuboid.height);
			size.addElement(cuboid.length);
			
			for (int i = 0; i < 3; i++) {
				final int coordinate = i;
				ScrollArea scrollArea = new ScrollArea(pos.elementAt(i) -50, pos.elementAt(i) +50, new IScrollAction() {
					@Override
					public void onScroll(int position) {
						int diff;
						switch (coordinate) {
						case 0:
							diff = position - cuboid.x;
							cuboid.x = position;
							for (Cuboid c = cuboid.getCuboidAbove(); c != null; c = c.getCuboidAbove())
								c.x += diff;
							break;
						case 1:
							diff = position - cuboid.y;
							cuboid.y = position;
							for (Cuboid c = cuboid.getCuboidAbove(); c != null; c = c.getCuboidAbove())
								c.y += diff;
							break;
						case 2:
							diff = position - cuboid.z;
							cuboid.z = position;
							for (Cuboid c = cuboid.getCuboidAbove(); c != null; c = c.getCuboidAbove())
								c.z += diff;
							break;
						}
						parent.updateAllPositioningLabels();
					}
				});
				scrollArea.setBounds(x + 176 + 24 * i,y + 12, 22, 14);
				scrollArea.setState(pos.elementAt(i));
				scrollArea.setTitle("Change Position");
				scrollArea.setNumeric(true);
				labelPosition.elementAt(i).text = pos.elementAt(i).toString();
				scrollAreas.add(scrollArea);
			}
			
			for (int i = 0; i < 3; i++) {
				final int coordinate = i;
				final boolean vertical = coordinate == 1;
				ScrollArea scrollArea = new ScrollArea(vertical? 3 : 2, vertical? 11 : 20, new ICancelableScrollAction() {
					@Override
					public void onScroll(int position) {
						int diff;
						switch (coordinate) {
						case 0:
							diff = (position * 2 + 1) - cuboid.width;
							cuboid.width = position * 2 + 1;
							cuboid.x += diff / -2;
							for (Cuboid c = cuboid.getCuboidAbove(); c != null; c = c.getCuboidAbove()) {
								c.width += diff;
								c.x += diff / -2;
							}
							break;
						case 1:
							diff = position - cuboid.height;
							cuboid.height = position;
							for (Cuboid c = cuboid.getCuboidAbove(); c != null; c = c.getCuboidAbove()) {
								c.y += diff;
							}
							break;
						case 2:
							diff = (position * 2 + 1) - cuboid.length;
							cuboid.length = position * 2 + 1;
							cuboid.z += diff / -2;
							for (Cuboid c = cuboid.getCuboidAbove(); c != null; c = c.getCuboidAbove()) {
								c.length += diff;
								c.z += diff / -2;
							}
							break;
						}
						parent.updateAllPositioningLabels();
					}

					@Override
					public boolean canScroll(int position) {
						switch (coordinate) {
							case 0:
								if (Math.min(position * 2 + 1, cuboid.length) > 15)
									return false;
								break;
							case 2:
								if (Math.min(cuboid.width, position * 2 + 1) > 15)
									return false;								
								break;
						}
						return true;
					}
				});
				scrollArea.setBounds(x + 176 + 24 * i, y + 32, 22, 14);
				scrollArea.setTitle("Change Size");
				scrollArea.setState(i == 1 ? size.elementAt(i) : (size.elementAt(i) - 1) / 2);
				scrollArea.setNumeric(true);
				labelSize.elementAt(i).text = size.elementAt(i).toString();
				scrollAreas.add(scrollArea);
			}
			
			
		}
		
		public void updatePositioningLabels() {
			Vector<Integer> pos = new Vector<>(3);
			pos.addElement(cuboid.x);
			pos.addElement(cuboid.y);
			pos.addElement(cuboid.z);
			
			Vector<Integer> size = new Vector<>(3);
			size.addElement(cuboid.width);
			size.addElement(cuboid.height);
			size.addElement(cuboid.length);
			
			for (int i = 0; i < 3; i++) {
				labelPosition.elementAt(i).text = pos.elementAt(i).toString();
				labelSize.elementAt(i).text = size.elementAt(i).toString();
			}
		}
		
		public void drawScreen(int yOffset, int mouseX, int mouseY, float partialTicks) {
			int x = parent.xTopLeft;
			int y = parent.yTopLeft + yOffset;
			GuiResources.COMPOSER.draw(parent, x, y);
			
			drawString(fontRenderer, "" + (layer + 1), x + 9, y + 11, 0x4B5F9E);
			
			fontRenderer.drawString("Type", x + 32, y + 15, 0x3B4152, false);
			fontRenderer.drawString("Style", x + 32, y + 35, 0x3B4152, false);
			
			style.draw(parent);
			styleGroup.draw(parent);
			paletteGroup.draw(parent);
			for (int i = 0; i < 3; i++) {
				labelPosition.elementAt(i).draw(parent);
				labelSize.elementAt(i).draw(parent);
			}
		}
		
		
	}
	
	@Override
	protected void actionPerformed(GuiButton button) throws IOException {
		super.actionPerformed(button);
		if (button.id == BUTTON_ADD_LAYER) {
			Cuboid c = anchor;
			while (c.getCuboidAbove() != null) 
				c = c.getCuboidAbove();
			GroundPlannerClient.getInstance().getGroundPlan().add(c.stack(), topLayer + 1);
			initGui();
			
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
		for (GuiComposerPartial partial : partials) {
			for (ScrollArea area : partial.scrollAreas) {
				area.tryScroll(mouseX, mouseY, scrollAmount);
			}
		}
		
	}
	
	@Override
	public void handleMouseInput() throws IOException {
		super.handleMouseInput();
		
		int i = Mouse.getEventX() * this.width / this.mc.displayWidth;
		int j = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1;
		
		int scroll = Mouse.getEventDWheel();
		if (scroll != 0) {
			for (GuiComposerPartial partial : partials) {
				for (ScrollArea area : partial.scrollAreas) {
					area.tryScroll(i, j, (int) (scroll / -120f));
				}
			}
		}
	}
	

}
