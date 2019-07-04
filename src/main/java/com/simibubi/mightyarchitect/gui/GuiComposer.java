package com.simibubi.mightyarchitect.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.GlStateManager;
import com.simibubi.mightyarchitect.control.ArchitectManager;
import com.simibubi.mightyarchitect.control.compose.CylinderStack;
import com.simibubi.mightyarchitect.control.compose.Room;
import com.simibubi.mightyarchitect.control.compose.Stack;
import com.simibubi.mightyarchitect.control.compose.planner.SelectionTool;
import com.simibubi.mightyarchitect.control.design.DesignLayer;
import com.simibubi.mightyarchitect.control.design.DesignTheme;
import com.simibubi.mightyarchitect.control.design.DesignType;
import com.simibubi.mightyarchitect.control.design.ThemeStatistics;
import com.simibubi.mightyarchitect.gui.widgets.DynamicLabel;
import com.simibubi.mightyarchitect.gui.widgets.GuiIndicator;
import com.simibubi.mightyarchitect.gui.widgets.GuiIndicator.State;
import com.simibubi.mightyarchitect.gui.widgets.ScrollArea;
import com.simibubi.mightyarchitect.gui.widgets.ScrollArea.ICancelableScrollAction;
import com.simibubi.mightyarchitect.gui.widgets.ScrollArea.IScrollAction;
import com.simibubi.mightyarchitect.gui.widgets.ScrollBar;
import com.simibubi.mightyarchitect.gui.widgets.SimiButton;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.util.text.StringTextComponent;

public class GuiComposer extends Screen {

	private static final int BUTTON_ADD_LAYER = 0;
	private static final int BUTTON_NORMAL_ROOF = 1;
	private static final int BUTTON_FLAT_ROOF = 2;
	private static final int BUTTON_NO_ROOF = 3;
	private static final int BUTTON_REMOVE_LAYER = 4;

	private List<GuiComposerPartial> partials;
	private int xSize, ySize;
	private int xTopLeft, yTopLeft;
	private Stack stack;

	private SimiButton buttonNormalRoof;
	private SimiButton buttonFlatRoof;
	private SimiButton buttonNoRoof;
	private GuiIndicator indicatorNormalRoof;
	private GuiIndicator indicatorFlatRoof;
	private GuiIndicator indicatorNoRoof;

	private ScrollBar scrollBar;

	private DesignTheme theme;
	private ThemeStatistics stats;
	private boolean tower;

	private float alpha;

	public GuiComposer(Stack stack) {
		super(new StringTextComponent("Composer"));
		this.stack = stack;
		theme = ArchitectManager.getModel().getGroundPlan().theme;
		stats = theme.getStatistics();
		tower = stack instanceof CylinderStack;
		alpha = 1;
	}

	private void init(Stack stack) {
		partials = new ArrayList<>();
		buttons.clear();

		List<Room> rooms = stack.getRooms();
		stack.forEach(room -> {
			partials.add(new GuiComposerPartial(this, room, rooms.indexOf(room)));
		});

		xSize = 256;
		ySize = 58 + (stack.floors() - 1) * 52 + 20;
		xTopLeft = (this.width - this.xSize) / 2;
		yTopLeft = (this.height - this.ySize) / 2;

		scrollBar = new ScrollBar(xTopLeft + xSize + 20, ySize);

		int x = xTopLeft + 3 * 20;
		if (tower && stats.hasConicalRoof || !tower && stats.hasGables) {
			buttonNormalRoof = new SimiButton(BUTTON_NORMAL_ROOF, x, yTopLeft,
					tower ? GuiResources.ICON_TOWER_ROOF : GuiResources.ICON_NORMAL_ROOF);
			indicatorNormalRoof = new GuiIndicator(x, yTopLeft - 5, "");
			buttons.add(buttonNormalRoof);
			x += 20;
		}

		if (tower && stats.hasFlatTowerRoof || !tower && stats.hasFlatRoof) {
			buttonFlatRoof = new SimiButton(BUTTON_FLAT_ROOF, x, yTopLeft,
					tower ? GuiResources.ICON_TOWER_FLAT_ROOF : GuiResources.ICON_FLAT_ROOF);
			indicatorFlatRoof = new GuiIndicator(x, yTopLeft - 5, "");
			buttons.add(buttonFlatRoof);
			x += 20;
		}

		buttonNoRoof = new SimiButton(BUTTON_NO_ROOF, x, yTopLeft,
				tower ? GuiResources.ICON_TOWER_NO_ROOF : GuiResources.ICON_NO_ROOF);
		indicatorNoRoof = new GuiIndicator(x, yTopLeft - 5, "");

		buttons.add(buttonNoRoof);

		swapRoofTypeIfNecessary();
		indicate(stack.highest().roofType == DesignType.ROOF ? indicatorNormalRoof
				: stack.highest().roofType == DesignType.FLAT_ROOF ? indicatorFlatRoof : indicatorNoRoof);

	}

	private boolean normalRoofPossible() {
		return Math.min(stack.highest().width,
				stack.highest().length) <= (tower ? stats.MaxConicalRoofRadius * 2 + 1 : stats.MaxGableRoof);
	}

	private void swapRoofTypeIfNecessary() {
		if (buttonNormalRoof == null)
			return;
		buttonNormalRoof.active = normalRoofPossible();

		if (indicatorNormalRoof.state == State.OFF)
			return;

		indicate(indicatorNormalRoof);
		if (normalRoofPossible()) {
			return;
		}

		if (buttonFlatRoof != null) {
			stack.highest().roofType = DesignType.FLAT_ROOF;
			indicate(indicatorFlatRoof);
			return;
		}

		stack.highest().roofType = DesignType.NONE;
		indicate(indicatorNoRoof);
	}

	@Override
	public void init() {
		init(stack);
		if (stack.floors() <= ThemeStatistics.MAX_FLOORS)
			addButton(new SimiButton(BUTTON_ADD_LAYER, xTopLeft + 2, yTopLeft, GuiResources.ICON_ADD));
		addButton(new SimiButton(BUTTON_REMOVE_LAYER, xTopLeft + 22, yTopLeft, GuiResources.ICON_TRASH));

		for (int layer = 0; layer < partials.size(); layer++) {
			GuiComposerPartial partial = partials.get(layer);
			int offset = (partials.size() - layer - 1) * 52 + 20;
			partial.init(offset);
		}
	}

	@Override
	public void render(int mouseX, int mouseY, float partialTicks) {
		this.renderBackground();

		if (alpha < 1f)
			alpha += (1 - alpha) * .2f;

		mouseY -= scrollBar.getYShift();
		GlStateManager.pushMatrix();
		GlStateManager.translated(0, scrollBar.getYShift(), 0);

		for (int layer = 0; layer < partials.size(); layer++) {
			GuiComposerPartial partial = partials.get(layer);
			int offset = (partials.size() - layer - 1) * 52 + 20;
			partial.drawScreen(offset, mouseX, mouseY, partialTicks);
		}

		super.render(mouseX, mouseY, partialTicks);

		if (indicatorNormalRoof != null)
			indicatorNormalRoof.render(minecraft, mouseX, mouseY);
		if (indicatorFlatRoof != null)
			indicatorFlatRoof.render(minecraft, mouseX, mouseY);
		indicatorNoRoof.render(minecraft, mouseX, mouseY);

		GlStateManager.popMatrix();

		scrollBar.render(this);

		GlStateManager.pushMatrix();
		GlStateManager.translated(0, scrollBar.getYShift(), 0);

		SelectionTool.selectedRoom = null;
		for (int layer = 0; layer < partials.size(); layer++) {
			GuiComposerPartial partial = partials.get(layer);
			int offset = (partials.size() - layer - 1) * 52 + 20;
			
			if (partial.isHovered(offset, mouseX, mouseY))
				SelectionTool.selectedRoom = partial.cuboid;
			
			for (ScrollArea area : partial.scrollAreas) {
				if ((partial.scrollAreaPosition.contains(area) || partial.scrollAreaSize.contains(area)) && area.isHovered(mouseX, mouseY)) {
					alpha += (-alpha) * .8f;
				}
				area.draw(this, mouseX, mouseY);
			}
		}

		GlStateManager.popMatrix();
	}

	public void updateAllPositioningLabels() {
		for (GuiComposerPartial partial : partials)
			partial.updatePositioningLabels();
	}
	
	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
		Widget clicked = null;
		for (Widget button : buttons) {
			if (button.isMouseOver(mouseX, mouseY))
				clicked = button;
		}
		
		if (clicked != null && clicked instanceof SimiButton)
			actionPerformed((SimiButton) clicked);
		
		mouseY = (int) (mouseY - scrollBar.getYShift());
		super.mouseClicked(mouseX, mouseY, mouseButton);

		int scrollAmount = ((mouseButton == 0) ? -1 : 1) * ((Keyboard.isKeyDown(Keyboard.LSHIFT)) ? 5 : 1);
		for (GuiComposerPartial partial : partials) {
			for (ScrollArea area : partial.scrollAreas) {
				area.tryScroll(mouseX, mouseY, scrollAmount);
			}
		}
		
		return super.mouseClicked(mouseX, mouseY, mouseButton);
	}

	class GuiComposerPartial {
		private List<ScrollArea> scrollAreas;
		private Room cuboid;
		private GuiComposer parent;
		private int layer;

		private Vector<ScrollArea> scrollAreaPosition;
		private Vector<ScrollArea> scrollAreaSize;

		private Vector<DynamicLabel> labelPosition;
		private Vector<DynamicLabel> labelSize;
		private DynamicLabel style;
		private DynamicLabel styleGroup;
		private DynamicLabel paletteGroup;

		public GuiComposerPartial(GuiComposer parent, Room cuboid, int layer) {
			scrollAreas = new ArrayList<>();
			this.cuboid = cuboid;
			this.layer = layer;
			this.parent = parent;
			labelPosition = new Vector<>(3);
			labelSize = new Vector<>(3);
			scrollAreaPosition = new Vector<>(3);
			scrollAreaSize = new Vector<>(3);
		}

		public void init(int yOffset) {
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
			List<DesignLayer> layers = theme.getRoomLayers();
			List<String> styleOptions = new ArrayList<>();
			layers.forEach(layer -> {
				styleOptions.add(layer.getDisplayName());
			});

			ScrollArea styleScrollArea = new ScrollArea(styleOptions, new IScrollAction() {
				@Override
				public void onScroll(int position) {
					cuboid.designLayer = theme.getRoomLayers().get(position);
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
					cuboid.secondaryPalette = position == 1;
					paletteGroup.text = cuboid.secondaryPalette ? "2nd" : "1st";
				}
			});
			palleteScrollArea.setBounds(x + 87, y + 32, 30, 14);
			palleteScrollArea.setTitle("Palette used");
			palleteScrollArea.setState(cuboid.secondaryPalette ? 1 : 0);
			paletteGroup.text = cuboid.secondaryPalette ? "2nd" : "1st";
			scrollAreas.add(palleteScrollArea);

			ScrollArea styleGroupScrollArea = new ScrollArea(0, 5, new IScrollAction() {
				@Override
				public void onScroll(int position) {
					char groupChar = (position < 4) ? (char) (position + 'A') : 'U';
					cuboid.styleGroup = groupChar;
					styleGroup.text = "" + groupChar;
				}
			});
			styleGroupScrollArea.setBounds(x + 123, y + 32, 22, 14);
			styleGroupScrollArea.setTitle("Style group");
			styleGroupScrollArea
					.setState((cuboid.styleGroup >= 'A' && cuboid.styleGroup <= 'D') ? cuboid.styleGroup - 'A' : 5);
			styleGroup.text = ""
					+ ((styleGroupScrollArea.getState() < 4) ? (char) (styleGroupScrollArea.getState() + 'A') : 'U');
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

			scrollAreaPosition = new Vector<>(3);
			scrollAreaSize = new Vector<>(3);

			final int X = 0, Y = 1, Z = 2;

			for (int i = 0; i < 3; i++) {
				final int coordinate = i;
				ScrollArea scrollArea = new ScrollArea(pos.elementAt(i) - 50, pos.elementAt(i) + 50,
						new IScrollAction() {
							@Override
							public void onScroll(int position) {
								int diff;
								switch (coordinate) {

								case X:
									diff = position - cuboid.x;
									stack.forRoomAndEachAbove(cuboid, room -> room.x += diff);
									break;

								case Y:
									diff = position - cuboid.y;
									stack.forRoomAndEachAbove(cuboid, room -> room.y += diff);
									break;

								case Z:
									diff = position - cuboid.z;
									stack.forRoomAndEachAbove(cuboid, room -> room.z += diff);
									break;

								}
								parent.updateAllPositioningLabels();
							}
						});
				scrollArea.setBounds(x + 176 + 24 * i, y + 12, 22, 14);
				scrollArea.setState(pos.elementAt(i));
				scrollArea.setTitle("Move Rooms");

				float angle = minecraft.player.rotationYawHead % 360;
				if (angle < -180)
					angle += 360;
				if (angle > 180)
					angle -= 360;

				boolean numeric = (coordinate == x) ? (angle < 0) : (Math.abs(angle) < 90);

				scrollArea.setNumeric(numeric || coordinate == Y);

				labelPosition.elementAt(i).text = pos.elementAt(i).toString();
				scrollAreas.add(scrollArea);
				scrollAreaPosition.addElement(scrollArea);
			}

			for (int i = 0; i < 3; i++) {
				final int coordinate = i;
				final boolean vertical = coordinate == Y;
				int min = vertical ? 1 : (stats.MinRoomLength - 1) / 2;
				int max = vertical ? theme.getMaxFloorHeight() + 1 : (stats.MaxRoomLength - 1) / 2;
				ScrollArea scrollArea = new ScrollArea(min, max, new ICancelableScrollAction() {
					@Override
					public void onScroll(int position) {
						int diff;
						switch (coordinate) {

						case X:
							diff = (position * 2 + 1) - cuboid.width;
							stack.forRoomAndEachAbove(cuboid, room -> {
								if (Math.min(room.width + diff, room.length) > stack.getMaxFacadeWidth())
									return;
								if (Math.min(room.width + diff, room.length) < stack.getMinWidth())
									return;
								if (stack instanceof CylinderStack && room.width + diff > stack.getMaxFacadeWidth())
									return;
								if (stack instanceof CylinderStack && room.width + diff < stack.getMinWidth())
									return;

								room.width += diff;
								room.x += diff / -2;

								if (stack instanceof CylinderStack) {
									room.length += diff;
									room.z += diff / -2;
								}
							});
							swapRoofTypeIfNecessary();
							break;

						case Y:
							diff = position - cuboid.height;
							cuboid.height += diff;
							stack.forEachAbove(cuboid, room -> {
								room.y += diff;
							});
							break;

						case Z:
							diff = (position * 2 + 1) - cuboid.length;
							stack.forRoomAndEachAbove(cuboid, room -> {
								if (Math.min(room.width, room.length + diff) > stack.getMaxFacadeWidth())
									return;
								if (Math.min(room.width, room.length + diff) < stack.getMinWidth())
									return;
								if (stack instanceof CylinderStack && room.width + diff > stack.getMaxFacadeWidth())
									return;
								if (stack instanceof CylinderStack && room.width + diff < stack.getMinWidth())
									return;

								room.length += diff;
								room.z += diff / -2;

								if (stack instanceof CylinderStack) {
									room.width += diff;
									room.x += diff / -2;
								}
							});
							swapRoofTypeIfNecessary();
							break;

						}
						parent.updateAllPositioningLabels();
					}

					@Override
					public boolean canScroll(int position) {
						switch (coordinate) {
						case 0:
							if (Math.min(position * 2 + 1, cuboid.length) > stack.getMaxFacadeWidth())
								return false;
							break;
						case 2:
							if (Math.min(cuboid.width, position * 2 + 1) > stack.getMaxFacadeWidth())
								return false;
							break;
						}
						return true;
					}
				});
				scrollArea.setBounds(x + 176 + 24 * i, y + 32, 22, 14);
				scrollArea.setTitle("Resize Rooms");
				scrollArea.setState(i == 1 ? size.elementAt(i) : (size.elementAt(i) - 1) / 2);
				scrollArea.setNumeric(true);
				labelSize.elementAt(i).text = size.elementAt(i).toString();
				scrollAreas.add(scrollArea);
				scrollAreaSize.addElement(scrollArea);
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
				scrollAreaPosition.elementAt(i).setState(pos.elementAt(i));
				scrollAreaSize.elementAt(i).setState(i == 1 ? size.elementAt(i) : (size.elementAt(i) - 1) / 2);
			}
		}

		public void drawScreen(int yOffset, int mouseX, int mouseY, float partialTicks) {
			int x = parent.xTopLeft;
			int y = parent.yTopLeft + yOffset;

			GlStateManager.enableBlend();
			GlStateManager.color4f(1, 1, 1, parent.alpha);
			GuiResources.COMPOSER.draw(parent, x, y);
			GlStateManager.disableBlend();

			drawCenteredString(font, "" + (layer + 1), x + 13, y + 15, 0xCCDDFF);

			font.drawString("Type", x + 32, y + 15, GuiResources.FONT_COLOR);
			font.drawString("Style", x + 32, y + 35, GuiResources.FONT_COLOR);

			style.draw(parent);
			styleGroup.draw(parent);
			paletteGroup.draw(parent);

			for (int i = 0; i < 3; i++) {
				labelPosition.elementAt(i).draw(parent);
				labelSize.elementAt(i).draw(parent);
			}
		}

		public boolean isHovered(int yOffset, int mouseX, int mouseY) {
			int x = parent.xTopLeft;
			int y = parent.yTopLeft + yOffset;
			int width = GuiResources.COMPOSER.width;
			int height = GuiResources.COMPOSER.height;

			return (!(mouseX < x || mouseX > x + width || mouseY < y || mouseY > y + height));
		}

	}

	protected void actionPerformed(SimiButton button) {
		switch (button.id) {
		case BUTTON_ADD_LAYER:
			stack.increase();
			init();
			return;

		case BUTTON_REMOVE_LAYER:
			if (Keyboard.isKeyDown(Keyboard.LSHIFT) || stack.floors() == 1) {
				ArchitectManager.getModel().getGroundPlan().remove(stack);
				minecraft.displayGuiScreen(null);
				return;
			}
			stack.decrease();
			init();
			return;

		case BUTTON_NORMAL_ROOF:
			stack.highest().roofType = DesignType.ROOF;
			indicate(indicatorNormalRoof);
			return;

		case BUTTON_FLAT_ROOF:
			stack.highest().roofType = DesignType.FLAT_ROOF;
			indicate(indicatorFlatRoof);
			return;

		case BUTTON_NO_ROOF:
			stack.highest().roofType = DesignType.NONE;
			indicate(indicatorNoRoof);
			return;

		}
	}

	private void indicate(GuiIndicator indicator) {
		if (indicatorNormalRoof != null)
			indicatorNormalRoof.state = indicator == indicatorNormalRoof ? State.ON : State.OFF;
		if (indicatorFlatRoof != null)
			indicatorFlatRoof.state = indicator == indicatorFlatRoof ? State.ON : State.OFF;
		indicatorNoRoof.state = indicator == indicatorNoRoof ? State.ON : State.OFF;
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	@Override
	public void removed() {
		SelectionTool.selectedRoom = null;
		super.removed();
	}
	
	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double scroll) {
		
		float jBeforeShift = (float) (this.height - mouseY * this.height / this.minecraft.mainWindow.getHeight() - 1);
		int j = (int) (jBeforeShift - scrollBar.getYShift());
		
		if (scroll != 0) {
			scrollBar.tryScroll((int) mouseX, (int) jBeforeShift, (int) (scroll / -120f));
			for (GuiComposerPartial partial : partials) {
				for (ScrollArea area : partial.scrollAreas) {
					area.tryScroll((int) mouseX, j, (int) (scroll / -120f));
				}
			}
		}
		
		return super.mouseScrolled(mouseX, mouseY, scroll);
	}
	
}
