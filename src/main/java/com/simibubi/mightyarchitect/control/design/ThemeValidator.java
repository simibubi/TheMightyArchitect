package com.simibubi.mightyarchitect.control.design;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import com.simibubi.mightyarchitect.foundation.utility.DesignHelper;
import com.simibubi.mightyarchitect.foundation.utility.Lang;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;

public class ThemeValidator {

	static List<Component> complaints;

	public static void check(DesignTheme theme) {
		LocalPlayer player = Minecraft.getInstance().player;
		for (int i = 0; i < 3; i++)
			Lang.text("")
				.sendChat(player);
		Lang.text(ChatFormatting.AQUA + "--> Validation on " + ChatFormatting.BLUE + ChatFormatting.BOLD
			+ theme.getDisplayName() + ChatFormatting.AQUA + " <--")
			.sendChat(player);
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
			Lang.text("The Following Designs are missing:")
				.style(ChatFormatting.GOLD)
				.sendChat(player);
			for (Component text : complaints)
				player.displayClientMessage(text, false);
			Lang.text("Try and add these missing designs or exclude their type from your theme.")
				.style(ChatFormatting.GOLD)
				.sendChat(player);
			return;
		}

		Lang.text("For prior traits no missing designs have been found.")
			.style(ChatFormatting.GREEN)
			.sendChat(player);
	}

	private static boolean exists(DesignQuery query) {
		return DesignHelper.pickRandom(query.withoutFallback(), new Random()) != null;
	}

	private static void alert(String message) {
		complaints.add(Lang.text("-> " + ChatFormatting.RED + message)
			.component());
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
