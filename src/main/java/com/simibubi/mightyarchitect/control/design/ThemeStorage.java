package com.simibubi.mightyarchitect.control.design;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import com.simibubi.mightyarchitect.control.helpful.FilesHelper;
import com.simibubi.mightyarchitect.control.palette.PaletteDefinition;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTTagCompound;

public class ThemeStorage {

	public enum IncludedThemes {

		Medieval("medieval"), Fallback("fallback_theme");

		public DesignTheme theme;
		public String themeFolder;

		private IncludedThemes(String themeFolder) {
			this.themeFolder = themeFolder;
		}
	}

	private static List<DesignTheme> importedThemes;

	public static List<DesignTheme> getAllThemes() {
		List<DesignTheme> themes = new ArrayList<>(getIncluded());
		themes.addAll(getImported());
		return themes;
	}

	public static List<DesignTheme> getIncluded() {
		List<DesignTheme> included = new ArrayList<>();
		for (IncludedThemes theme : IncludedThemes.values()) {

			if (theme.theme == null)
				theme.theme = loadInternalTheme(theme.themeFolder);

			if (theme == IncludedThemes.Fallback)
				continue;

			included.add(theme.theme);
		}
		return included;
	}

	public static List<DesignTheme> getImported() {
		if (importedThemes == null)
			importThemes();

		return importedThemes;
	}

	public static void reloadExternal() {
		importedThemes = null;
	}

	public static DesignTheme createTheme(String name) {
		DesignTheme theme = new DesignTheme(name, Minecraft.getMinecraft().player.getName(),
				new StandardDesignPicker());
		theme.setFilePath(FilesHelper.slug(name));
		theme.setImported(true);
		theme.setDefaultPalette(PaletteDefinition.defaultPalette());
		return theme.withLayers(DesignLayer.Regular, DesignLayer.Roofing, DesignLayer.Foundation).withTypes(
				DesignType.WALL, DesignType.CORNER, DesignType.ROOF, DesignType.FACADE, DesignType.FLAT_ROOF);
	}

	public static void exportTheme(DesignTheme theme) {
		String folderPath = "themes";
		FilesHelper.createFolderIfMissing(folderPath);

		String foldername = theme.getFilePath();
		FilesHelper.createFolderIfMissing(folderPath + "/" + foldername);

		String filepath = folderPath + "/" + foldername + "/theme.json";
		FilesHelper.saveTagCompoundAsJson(theme.asTagCompound(), filepath);

		String palettePath = folderPath + "/" + foldername + "/palette.json";
		FilesHelper.saveTagCompoundAsJson(theme.getDefaultPalette().writeToNBT(new NBTTagCompound()), palettePath);
	}

	private static DesignTheme loadInternalTheme(String themeFolder) {
		NBTTagCompound themeCompound = FilesHelper.loadJsonResourceAsNBT("themes/" + themeFolder + "/theme.json");
		NBTTagCompound paletteCompound = FilesHelper.loadJsonResourceAsNBT("themes/" + themeFolder + "/palette.json");
		DesignTheme theme = DesignTheme.fromNBT(themeCompound);
		theme.setFilePath(themeFolder);
		theme.setImported(false);
		theme.setDefaultPalette(PaletteDefinition.fromNBT(paletteCompound));
		return theme;
	}

	private static void importThemes() {
		importedThemes = new ArrayList<>();
		String folderPath = "themes";
		if (Files.isDirectory(Paths.get(folderPath))) {

			try {
				DirectoryStream<Path> newDirectoryStream = Files.newDirectoryStream(Paths.get(folderPath));
				for (Path path : newDirectoryStream) {
					String themeFolder = path.getFileName().toString();

					NBTTagCompound themeCompound = FilesHelper
							.loadJsonAsNBT(folderPath + "/" + themeFolder + "/theme.json");
					NBTTagCompound paletteCompound = FilesHelper
							.loadJsonAsNBT(folderPath + "/" + themeFolder + "/palette.json");
					DesignTheme theme = DesignTheme.fromNBT(themeCompound);
					theme.setFilePath(themeFolder);
					theme.setImported(true);
					theme.setDefaultPalette(PaletteDefinition.fromNBT(paletteCompound));
					importedThemes.add(theme);
				}
				newDirectoryStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}

	}

}
