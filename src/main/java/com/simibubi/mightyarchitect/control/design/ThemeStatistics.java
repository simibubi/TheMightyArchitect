package com.simibubi.mightyarchitect.control.design;

import java.util.Random;

import com.simibubi.mightyarchitect.control.compose.Room;
import com.simibubi.mightyarchitect.control.helpful.DesignHelper;

import net.minecraft.client.Minecraft;
import net.minecraft.util.text.StringTextComponent;

public class ThemeStatistics {

	// Limits for the design Exporter
	public static final int MIN_TOWER_RADIUS = 1, MAX_TOWER_RADIUS = 15;
	public static final int MIN_ROOF_SPAN = 3, MAX_ROOF_SPAN = 35;
	public static final int MIN_MARGIN = 0, MAX_MARGIN = 15;

	// Limits for the composer
	public static final int MAX_FLOORS = 25;

	public int MinRoomLength = 3;
	public int MaxRoomLength = 97;

	public int MinFlatRoof = 3;
	public int MinGableRoof = 3;
	public int MaxGableRoof = 15;

	public boolean hasGables = true;
	public boolean hasFlatRoof = true;

	public int MinTowerRadius = 1;
	public int MaxConicalRoofRadius = 5;
	public int MaxTowerRadius = 5;

	public boolean hasTowers = true;
	public boolean hasFlatTowerRoof = true;
	public boolean hasConicalRoof = true;

	public static ThemeStatistics evaluate(DesignTheme theme) {
		ThemeStatistics stats = new ThemeStatistics();

		if (!theme.getTypes().contains(DesignType.TOWER_FLAT_ROOF))
			stats.hasFlatTowerRoof = false;
		if (!theme.getTypes().contains(DesignType.TOWER_ROOF))
			stats.hasConicalRoof = false;

		if (theme.getTypes().contains(DesignType.TOWER)) {
			// Determine min and max radius
			stats.MinTowerRadius = stats.MaxRoomLength;
			stats.MaxTowerRadius = 0;
			stats.MaxConicalRoofRadius = 0;
			stats.hasTowers = false;

			for (DesignLayer layer : theme.getRoomLayers()) {
				DesignQuery towerQuery = new DesignQuery(theme, layer, DesignType.TOWER);
				DesignQuery conicalRoofQuery = new DesignQuery(theme, DesignLayer.Roofing, DesignType.TOWER_ROOF);

				if (!designExists(towerQuery)) {
					break;
				}

				stats.hasTowers = true;

				for (int radius = 1; radius <= stats.MaxRoomLength; radius += 1) {
					if (designExists(towerQuery.withWidth(radius * 2 + 1))) {
						stats.MinTowerRadius = Math.min(radius, stats.MinTowerRadius);
						break;
					}
				}

				for (int radius = stats.MaxRoomLength; radius >= stats.MinTowerRadius; radius -= 1) {
					if (designExists(towerQuery.withWidth(radius * 2 + 1))) {
						stats.MaxTowerRadius = Math.max(radius, stats.MaxTowerRadius);
						break;
					}
				}

				for (int radius = stats.MaxTowerRadius; radius >= stats.MinTowerRadius; radius -= 1) {
					if (designExists(conicalRoofQuery.withWidth(radius * 2 + 1))) {
						stats.MaxConicalRoofRadius = Math.max(radius, stats.MaxConicalRoofRadius);
						break;
					}
				}
			}
		} else {
			stats.hasTowers = false;
		}

		if (theme.getTypes().contains(DesignType.ROOF)) {

			// Determine min and max gable span
			DesignQuery roofQuery = new DesignQuery(theme, DesignLayer.Roofing, DesignType.ROOF);

			if (!designExists(roofQuery)) {
				stats.hasGables = false;
			}

			for (int roofSpan = 3; roofSpan <= 15; roofSpan += 2) {
				if (designExists(roofQuery.withWidth(roofSpan))) {
					stats.MinGableRoof = roofSpan;
					break;
				}
			}

			for (int roofSpan = stats.MaxRoomLength; roofSpan >= stats.MinGableRoof; roofSpan -= 2) {
				if (designExists(roofQuery.withWidth(roofSpan))) {
					stats.MaxGableRoof = roofSpan;
					break;
				}
			}
		} else {
			stats.hasGables = false;
			stats.MinGableRoof = stats.MaxRoomLength;
		}

		if (theme.getTypes().contains(DesignType.FLAT_ROOF)) {
			DesignQuery roofQuery = new DesignQuery(theme, DesignLayer.Roofing, DesignType.FLAT_ROOF);

			if (!designExists(roofQuery)) {
				stats.hasFlatRoof = false;
			}

			for (int roofSpan = 3; roofSpan <= stats.MaxRoomLength; roofSpan += 2) {
				if (designExists(roofQuery.withWidth(roofSpan))) {
					stats.MinFlatRoof = roofSpan;
					break;
				}
			}
		} else {
			stats.hasFlatRoof = false;
			stats.MinFlatRoof = stats.MaxRoomLength;
		}

		if (stats.hasFlatRoof || stats.hasGables)
			stats.MinRoomLength = Math.min(stats.MinGableRoof, stats.MinFlatRoof);

		return stats;
	}

	protected static boolean designExists(DesignQuery query) {
		return DesignHelper.pickRandom(query.withoutFallback(), new Random()) != null;
	}

	public void sendToPlayer() {
		chat("Room size: " + MinRoomLength + " to " + (MaxRoomLength == 97 ? "Infinity" : MaxRoomLength));

		if (hasFlatRoof)
			chat("Smallest Flat Roof Spans: " + MinFlatRoof);
		else
			chat("No Flat Roofing");

		if (hasGables)
			chat("Gable Roofs span: " + MinGableRoof + " to " + MaxGableRoof);
		else
			chat("No Gable Roofing");

		if (hasTowers) {
			chat("Tower radii: " + MinTowerRadius + " to " + MaxTowerRadius);

			if (hasFlatTowerRoof)
				chat("Has Flat Tower Roofing");
			else
				chat("No Flat Tower Roofing");

			if (hasConicalRoof)
				chat("Largest Conical Roof radius: " + MaxConicalRoofRadius);
			else
				chat("No Conical Roofing");

		} else {
			chat("No Towers");
		}
	}

	private void chat(String message) {
		Minecraft.getInstance().player.sendMessage(new StringTextComponent(message));
	}

	public DesignType fallbackRoof(Room room, boolean tower) {
		DesignType desired = room.roofType;
		
		if (!tower && room.quadFacadeRoof && room.width != room.length)
			room.quadFacadeRoof = false;
		
		if (!tower && desired == DesignType.ROOF) {
			if (hasGables && Math.min(room.width, room.length) <= MaxGableRoof
					&& Math.min(room.width, room.length) >= MinGableRoof)
				return desired;
			if (hasFlatRoof && Math.min(room.width, room.length) >= MinFlatRoof)
				return DesignType.FLAT_ROOF;
			return DesignType.NONE;
		}

		if (!tower && desired == DesignType.FLAT_ROOF) {
			if (hasFlatRoof)
				return desired;
			return DesignType.NONE;
		}

		if (tower && desired == DesignType.ROOF) {
			if (hasConicalRoof && room.width <= MaxConicalRoofRadius * 2 + 1)
				return desired;
			if (hasFlatTowerRoof)
				return DesignType.FLAT_ROOF;
			return DesignType.NONE;
		}

		if (tower && desired == DesignType.FLAT_ROOF) {
			if (hasFlatTowerRoof)
				return desired;
			return DesignType.NONE;
		}

		return DesignType.NONE;
	}

}
