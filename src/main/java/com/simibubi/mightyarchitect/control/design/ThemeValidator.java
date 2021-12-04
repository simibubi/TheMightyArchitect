package com.simibubi.mightyarchitect.control.design;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import com.simibubi.mightyarchitect.foundation.utility.DesignHelper;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

public class ThemeValidator {

	static List<StringTextComponent> complaints;

	public static void check(DesignTheme theme) {
		ClientPlayerEntity player = Minecraft.getInstance().player;
		for (int i = 0; i < 3; i++)
			player.displayClientMessage(new StringTextComponent(" "), false);
		player.displayClientMessage(new StringTextComponent(TextFormatting.AQUA + "--> Validation on "
			+ TextFormatting.BLUE + TextFormatting.BOLD + theme.getDisplayName() + TextFormatting.AQUA + " <--"),
			false);
		theme.clearDesigns();
		ThemeStatistics stats = theme.getStatistics();
		stats.sendToPlayer();
		complaints = new LinkedList<>();

		for (DesignLayer layer : theme.getLayers()) {
			for (DesignType type : theme.getTypes()) {

				if (theme.getRoomLayers()
					.contains(layer)
					&& DesignType.roofTypes()
						.contains(type))
					continue;
				if (!theme.getRoomLayers()
					.contains(layer)
					&& !DesignType.roofTypes()
						.contains(type))
					continue;

				DesignQuery query = new DesignQuery(theme, layer, type);
				if (!exists(query)) {
					if (type == DesignType.FACADE)
						continue;
					if (type == DesignType.CORNER && layer == DesignLayer.Open)
						continue;

					alert(layer.getDisplayName() + " " + type.getDisplayName() + " has no designs!");
					continue;
				}

				List<Integer> missingHeights = new ArrayList<>();

				switch (type) {
				case CORNER:
					missingHeights.clear();
					for (int height = 1; height <= theme.getMaxFloorHeight(); height++) {
						DesignQuery cornerQuery = new DesignQuery(theme, layer, type).withHeight(height);
						if (!exists(cornerQuery))
							missingHeights.add(height);
					}
					if (!missingHeights.isEmpty())
						alert(layer.getDisplayName() + " " + type.getDisplayName() + "s are missing heights "
							+ glue(missingHeights));
					break;
				case NONE:
					alert("Found design with no type in layer " + layer.getDisplayName() + "!");
					break;
				case ROOF:
				case FLAT_ROOF:
					for (int span = stats.MinGableRoof; span <= stats.MaxGableRoof; span += 2) {
						DesignQuery roofQuery = new DesignQuery(theme, layer, type).withWidth(span);
						if (!exists(roofQuery))
							alert("No " + type.getDisplayName() + " has a span of " + span + "m.");
					}
					break;
				case TOWER:
					for (int radius = stats.MinTowerRadius; radius <= stats.MaxTowerRadius; radius++) {
						DesignQuery withWidth = new DesignQuery(theme, layer, type).withWidth(radius * 2 + 1);

						if (!exists(withWidth)) {
							alert("No " + layer.getDisplayName() + " " + type.getDisplayName() + " has radius " + radius
								+ "m.");
							continue;
						}

						missingHeights.clear();
						for (int height = 1; height <= theme.getMaxFloorHeight(); height++) {
							DesignQuery towerQuery = withWidth.withHeight(height);
							if (!exists(towerQuery))
								missingHeights.add(height);
						}
						if (!missingHeights.isEmpty())
							alert(layer.getDisplayName() + " " + type.getDisplayName() + "s with radius " + radius
								+ " are missing heights " + glue(missingHeights));
					}
					break;
				case TOWER_FLAT_ROOF:
					for (int radius = stats.MinTowerRadius; radius <= stats.MaxTowerRadius; radius++) {
						DesignQuery towerQuery = new DesignQuery(theme, layer, type).withWidth(radius * 2 + 1);
						if (!exists(towerQuery))
							alert("No " + type.getDisplayName() + " has a radius of " + radius + "m.");
					}
					break;
				case TOWER_ROOF:
					for (int radius = stats.MinTowerRadius; radius <= stats.MaxConicalRoofRadius; radius++) {
						DesignQuery towerQuery = new DesignQuery(theme, layer, type).withWidth(radius * 2 + 1);
						if (!exists(towerQuery))
							alert("No " + type.getDisplayName() + " has a radius of " + radius + "m.");
					}
					break;
				case WALL:
					for (int width = stats.MinRoomLength - 2; width <= 15; width += 2) {
						DesignQuery wallQuery = new DesignQuery(theme, layer, type).withWidth(width);

						if (!exists(wallQuery)) {
							alert("No " + layer.getDisplayName() + " " + type.getDisplayName() + " spans " + width
								+ "m.");
							break;
						}

						missingHeights.clear();
						for (int height = 1; height <= theme.getMaxFloorHeight(); height++) {
							if (!exists(wallQuery.withHeight(height)))
								missingHeights.add(height);
						}
						if (!missingHeights.isEmpty())
							alert(layer.getDisplayName() + " " + type.getDisplayName() + "s which span " + width
								+ " are missing heights " + glue(missingHeights));
					}
					break;
				default:
					break;

				}

			}
		}

		if (complaints.size() > 0) {
			player.displayClientMessage(
				new StringTextComponent(TextFormatting.GOLD + "The Following Designs are missing:"), false);
			for (ITextComponent text : complaints) {
				player.displayClientMessage(text, false);
			}
			player.displayClientMessage(
				new StringTextComponent(
					TextFormatting.GOLD + "Try and add these missing designs or exclude their type from your theme."),
				false);

		} else {
			player.displayClientMessage(
				new StringTextComponent(TextFormatting.GREEN + "For prior traits no missing designs have been found."),
				false);
		}

	}

	private static boolean exists(DesignQuery query) {
		return DesignHelper.pickRandom(query.withoutFallback(), new Random()) != null;
	}

	private static void alert(String message) {
		complaints.add(new StringTextComponent("-> " + TextFormatting.RED + message));
	}

	private static String glue(List<Integer> heights) {
		if (heights.isEmpty())
			return null;
		String s = "";
		for (int h : heights)
			s += ", " + h;
		return s.substring(2);
	}

}
