package com.simibubi.mightyarchitect.control.design;

import com.simibubi.mightyarchitect.control.helpful.DesignHelper;

import net.minecraft.client.Minecraft;
import net.minecraft.util.text.TextComponentString;

public class ThemeValidator {

	private static final int MIN_RADIUS = 2;
	private static final int MAX_RADIUS = 5;
	private static final int MIN_HEIGHT = 1;
	private static final int MAX_HEIGHT = 10;

	public static void check(DesignTheme theme) {
		status("Validating the " + theme.getDisplayName() + " theme...");
		
		for (DesignLayer layer : theme.getLayers()) {
			for (DesignType type : theme.getTypes()) {

				DesignQuery query = new DesignQuery(theme, layer, type);
				if (!exists(query))
					continue;

				switch (type) {
				case CORNER:
					for (int height = MIN_HEIGHT; height <= MAX_HEIGHT; height++) {
						DesignQuery cornerQuery = new DesignQuery(theme, layer, type).withHeight(height);
						if (!exists(cornerQuery))
							alert("No " + layer.getDisplayName() + " " + type.getDisplayName()
									+ " has a height of " + height + "m.");
					}
					break;
				case NONE:
					alert("Found design with no type in layer " + layer.getDisplayName() + "!");
					break;
				case ROOF:
				case FLAT_ROOF:
					for (int span = 5; span <= 15; span += 2) {
						DesignQuery roofQuery = new DesignQuery(theme, layer, type).withWidth(span);
						if (!exists(roofQuery))
							alert("No " + layer.getDisplayName() + " " + type.getDisplayName()
									+ " has a span of " + span + "m.");
					}
					break;
				case TOWER:
					for (int radius = MIN_RADIUS; radius <= MAX_RADIUS; radius++) {
						for (int height = MIN_HEIGHT; height <= MAX_HEIGHT; height++) {
							DesignQuery towerQuery = new DesignQuery(theme, layer, type).withWidth(radius * 2 + 1)
									.withHeight(height);
							if (!exists(towerQuery))
								alert("No " + layer.getDisplayName() + " " + type.getDisplayName()
										+ " has radius " + radius + "m and a height of " + height
										+ "m.");
						}
					}
					break;
				case TOWER_FLAT_ROOF:
				case TOWER_ROOF:
					for (int radius = MIN_RADIUS; radius <= MAX_RADIUS; radius++) {
						DesignQuery towerQuery = new DesignQuery(theme, layer, type).withWidth(radius * 2 + 1);
						if (!exists(towerQuery))
							alert("No " + layer.getDisplayName() + " " + type.getDisplayName()
									+ " has a radius of " + radius + "m.");
					}
					break;
				case WALL:
					for (int width = 3; width <= 15; width += 2) {
						for (int height = MIN_HEIGHT; height <= MAX_HEIGHT; height++) {
							DesignQuery wallQuery = new DesignQuery(theme, layer, type).withHeight(height).withWidth(width);
							if (!exists(wallQuery))
								alert("No " + layer.getDisplayName() + " " + type.getDisplayName()
										+ " spans " + width + "x" + height + "m.");
						}
					}
					break;
				default:
					break;

				}

			}
		}
		
		status("Done checking!");

	}

	private static boolean exists(DesignQuery query) {
		return DesignHelper.pickRandom(query) != null;
	}

	private static void status(String message) {
		Minecraft.getMinecraft().player.sendMessage(new TextComponentString("-> " + message));
	}

	private static void alert(String message) {
		Minecraft.getMinecraft().player.sendMessage(new TextComponentString("§c!> " + message));
	}

}
