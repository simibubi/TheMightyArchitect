package com.simibubi.mightyarchitect.control;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

import com.simibubi.mightyarchitect.control.design.DesignExporter;
import com.simibubi.mightyarchitect.control.design.DesignTheme;
import com.simibubi.mightyarchitect.control.design.ThemeStorage;
import com.simibubi.mightyarchitect.control.design.ThemeValidator;
import com.simibubi.mightyarchitect.control.phase.ArchitectPhases;
import com.simibubi.mightyarchitect.gui.ScreenHelper;
import com.simibubi.mightyarchitect.gui.TextInputPromptScreen;
import com.simibubi.mightyarchitect.gui.ThemeSettingsScreen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.Util;

public class ArchitectMenu {

	public static boolean handleMenuInput(char c) {
		switch (ArchitectManager.getPhase()) {

		case Composing:
			switch (c) {

			case 'f':
				ArchitectManager.design();
				return true;

			case 'u':
				ArchitectManager.unload();
				return true;
			}
			break;

		case CreatingPalette:
			switch (c) {

			case 'f':
				TextInputPromptScreen gui =
					new TextInputPromptScreen(result -> ArchitectManager.finishPalette(result), result -> {
					});
				gui.setButtonTextConfirm(I18n.format("mightyarchitect.menu.palette_save"));
				gui.setButtonTextAbort(I18n.format("mightyarchitect.menu.palette_cancel"));
				gui.setTitle(I18n.format("mightyarchitect.menu.palette_set_title"));
				ScreenHelper.open(gui);
				return false;

			case 'd':
				ArchitectManager.pickPalette();
				return true;

			case 'u':
				ArchitectManager.unload();
				return true;
			}
			break;

		case Empty:
			switch (c) {

			case 'c':
				ArchitectManager.unload();
				return true;

			case 'm':
				ArchitectManager.manageThemes();
				return false;

			case 'r':
				ThemeStorage.reloadExternal();
				ArchitectManager.status(I18n.format("mightyarchitect.menu.reloaded_themes"));
				ArchitectManager.enterPhase(ArchitectPhases.Empty);
				return false;

			default:
				int index = c - '1';
				ThemeStorage.reloadExternal();
				List<DesignTheme> themes = ThemeStorage.getAllThemes();
				if (index < themes.size() && index >= 0) {
					ArchitectManager.compose(themes.get(index));
					return true;
				}
			}
			break;

		case Previewing:
			boolean test = ArchitectManager.testRun;
			switch (c) {

			case 'c':
				ArchitectManager.pickPalette();
				return true;

			case 'e':
				ArchitectManager.compose();
				return true;

			case 's':
				if (test)
					return false;
				TextInputPromptScreen gui =
					new TextInputPromptScreen(result -> ArchitectManager.writeToFile(result), result -> {
					});
				gui.setButtonTextConfirm(I18n.format("mightyarchitect.menu.schematic_save"));
				gui.setButtonTextAbort(I18n.format("mightyarchitect.menu.schematic_cancel"));
				gui.setTitle(I18n.format("mightyarchitect.menu.schematic_set_title"));

				ScreenHelper.open(gui);
				return true;

			case 'p':
				if (test)
					return false;
				if (!Minecraft.getInstance().player.isCreative())
					return false;
				ArchitectManager.print();
				return true;

			case 'u':
				ArchitectManager.unload();
				return true;
			}
			break;

		case ManagingThemes:
			switch (c) {
			case 'n':
				ArchitectManager.createTheme();
				return true;
			case 't':
				ArchitectManager.enterPhase(ArchitectPhases.ListForEdit);
				return false;
			case 'o':
				Util.getOSType()
					.openFile(Paths.get("themes/")
						.toFile());
				return false;
			case 'c':
				ArchitectManager.unload();
				return true;
			}
			break;

		case Paused:
			switch (c) {
			case 'r':
				ArchitectManager.enterPhase(ArchitectPhases.Composing);
				return true;
			case 'd':
				ArchitectManager.unload();
				ArchitectManager.openMenu();
				return false;
			case 'c':
				return true;
			}
			break;

		case ListForEdit:
			switch (c) {

			case 'c':
				ArchitectManager.enterPhase(ArchitectPhases.ManagingThemes);
				return false;

			default:
				int index = c - '1';
				List<DesignTheme> themes = ThemeStorage.getCreated();
				if (index < themes.size() && index >= 0) {
					ArchitectManager.editTheme(themes.get(index));
					return false;
				}
			}
			break;

		case EditingThemes:
			switch (c) {

			case '1':
				ArchitectKits.ExporterToolkit();
				return true;

			case '2':
				ArchitectKits.FoundationToolkit();
				return true;

			case '3':
				ArchitectKits.RegularToolkit();
				return true;

			case '4':
				ArchitectKits.RoofingToolkit();
				return true;

			case 'd':
				ArchitectManager.pickScanPalette();
				return true;

			case 't':
				ScreenHelper.open(new ThemeSettingsScreen());
				return true;

			case 'e':
				String file = ThemeStorage.exportThemeFullyAsFile(DesignExporter.theme, true);
				ArchitectManager.status(I18n.format("mightyarchitect.menu.exported_theme", file));
				return false;

			case 'j':
				file = ThemeStorage.exportThemeFullyAsFile(DesignExporter.theme, false);
				ArchitectManager.status(I18n.format("mightyarchitect.menu.exported_theme", file));
				return false;

			case 'r':
				DesignExporter.theme.clearDesigns();
				ThemeStorage.exportTheme(DesignExporter.theme);
				ArchitectManager.testRun = true;
				ArchitectManager.compose(DesignExporter.theme);
				return true;

			case 'f':
				DesignExporter.theme.clearDesigns();
				ThemeStorage.exportTheme(DesignExporter.theme);
				ArchitectManager.unload();
				return true;

			case 'v':
				ThemeValidator.check(DesignExporter.theme);
				return true;
			}
			break;

		default:
			break;
		}
		return false;
	}

	public static KeyBindList getKeybinds() {
		KeyBindList keybinds = new KeyBindList();

		switch (ArchitectManager.getPhase()) {

		case Composing:
			keybinds.put("F", I18n.format("mightyarchitect.menu.keybinds.finish"));
			keybinds.lineBreak();
			keybinds.put("U", I18n.format("mightyarchitect.menu.keybinds.unload"));
			break;

		case CreatingPalette:
			keybinds.put("F", I18n.format("mightyarchitect.menu.keybinds.palette_save"));
			keybinds.put("D", I18n.format("mightyarchitect.menu.keybinds.palette_cancel"));
			keybinds.lineBreak();
			keybinds.put("U", I18n.format("mightyarchitect.menu.keybinds.unload"));
			break;

		case Empty:
			List<DesignTheme> allThemes = ThemeStorage.getAllThemes();
			for (DesignTheme theme : allThemes) {
				keybinds.put("" + (allThemes.indexOf(theme) + 1), theme.getDisplayName());
			}
			keybinds.lineBreak();
			keybinds.put("R", I18n.format("mightyarchitect.menu.keybinds.empty_reload"));
			keybinds.put("M", I18n.format("mightyarchitect.menu.keybinds.empty_manage_themes"));
			keybinds.put("C", I18n.format("mightyarchitect.menu.keybinds.cancel"));
			break;

		case Previewing:
			keybinds.put("E", I18n.format("mightyarchitect.menu.keybinds.previewing_edit"));
			keybinds.put("C", I18n.format("mightyarchitect.menu.keybinds.previewing_choose_palette"));
			keybinds.lineBreak();

			if (!ArchitectManager.testRun) {
				keybinds.put("S", I18n.format("mightyarchitect.menu.keybinds.previewing_save"));
				if (Minecraft.getInstance().player.isCreative())
					keybinds.put("P", I18n.format("mightyarchitect.menu.keybinds.previewing_print"));
				keybinds.lineBreak();
				keybinds.put("U", I18n.format("mightyarchitect.menu.keybinds.unload"));
			} else {
				keybinds.put("U", I18n.format("mightyarchitect.menu.keybinds.previewing_exit_test_run"));
			}

			break;

		case ManagingThemes:
			keybinds.put("N", I18n.format("mightyarchitect.menu.keybinds.themes_new"));
			keybinds.put("T", I18n.format("mightyarchitect.menu.keybinds.themes_edit"));
			keybinds.put("O", I18n.format("mightyarchitect.menu.keybinds.themes_open_folder"));
			keybinds.lineBreak();
			keybinds.put("C", I18n.format("mightyarchitect.menu.keybinds.cancel"));
			break;

		case Paused:
			keybinds.put("R", I18n.format("mightyarchitect.menu.keybinds.paused_recover"));
			keybinds.put("D", I18n.format("mightyarchitect.menu.keybinds.paused_discard"));
			keybinds.lineBreak();
			keybinds.put("C", I18n.format("mightyarchitect.menu.keybinds.paused_close"));
			break;

		case ListForEdit:
			allThemes = ThemeStorage.getCreated();
			for (DesignTheme theme : allThemes) {
				keybinds.put("" + (allThemes.indexOf(theme) + 1), theme.getDisplayName());
			}
			keybinds.lineBreak();
			keybinds.put("C", I18n.format("mightyarchitect.menu.keybinds.cancel"));
			break;

		case EditingThemes:
			keybinds.put("1", I18n.format("mightyarchitect.menu.keybinds.themes_equip_exporter_tools"));
			keybinds.put("2", I18n.format("mightyarchitect.menu.keybinds.themes_equip_foundation_tools"));
			keybinds.put("3", I18n.format("mightyarchitect.menu.keybinds.themes_equip_regular_blocks"));
			keybinds.put("4", I18n.format("mightyarchitect.menu.keybinds.themes_equip_roofing_blocks"));
			keybinds.lineBreak();
			keybinds.put("D", I18n.format("mightyarchitect.menu.keybinds.themes_default_palette"));
			keybinds.put("T", I18n.format("mightyarchitect.menu.keybinds.themes_settings"));
			keybinds.lineBreak();
			keybinds.put("V", I18n.format("mightyarchitect.menu.keybinds.themes_validate"));
			keybinds.put("R", I18n.format("mightyarchitect.menu.keybinds.themes_run_test"));
			keybinds.put("E", I18n.format("mightyarchitect.menu.keybinds.themes_export_compressed"));
			keybinds.put("J", I18n.format("mightyarchitect.menu.keybinds.themes_export_json"));
			keybinds.put("F", I18n.format("mightyarchitect.menu.keybinds.themes_finish"));
			break;
		default:
			break;
		}

		return keybinds;
	}

	public static class KeyBindList {
		private List<String> keys;
		private Map<String, String> descriptions;

		public KeyBindList() {
			keys = new ArrayList<>();
			descriptions = new HashMap<>();
		}

		public void put(String key, String description) {
			keys.add(key);
			descriptions.put(key, description);
		}

		public void lineBreak() {
			put("", "");
		}

		public void foreach(BiConsumer<String, String> biConsumer) {
			keys.forEach(key -> biConsumer.accept(key, descriptions.get(key)));
		}

		public float size() {
			float size = 0;
			for (String key : keys) {
				size += (key.isEmpty()) ? 0.5f : 1;
			}
			return size;
		}

		public List<String> getKeys() {
			return keys;
		}

		public String get(String key) {
			return descriptions.get(key);
		}

	}

}
