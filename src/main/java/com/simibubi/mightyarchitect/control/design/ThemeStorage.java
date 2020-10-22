package com.simibubi.mightyarchitect.control.design;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.simibubi.mightyarchitect.control.palette.PaletteDefinition;
import com.simibubi.mightyarchitect.foundation.utility.FilesHelper;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.ListNBT;

public class ThemeStorage {

	public enum IncludedThemes {

		Medieval("medieval", 3, 5),
		Fallback("fallback_theme", 3, 4),
		Modern("modern", 2, 4),
		TownHouse("town_house", 4, 5),
		Cattingham("cattingham_palace", 7, 2, 6);

		public DesignTheme theme;
		public String themeFolder;
		public List<Integer> heights;

		private IncludedThemes(String themeFolder, Integer... floorHeights) {
			this.themeFolder = themeFolder;
			this.heights = Arrays.asList(floorHeights);
		}
	}

	private static List<DesignTheme> importedThemes;
	private static List<DesignTheme> createdThemes;

	public static List<DesignTheme> getAllThemes() {
		List<DesignTheme> themes = new ArrayList<>(getIncluded());
		themes.addAll(getImported());
		return themes;
	}

	public static List<DesignTheme> getIncluded() {
		List<DesignTheme> included = new ArrayList<>();
		for (IncludedThemes theme : IncludedThemes.values()) {

			if (theme.theme == null)
				theme.theme = loadInternalTheme(theme.themeFolder).withHeightSequence(theme.heights);

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

	public static List<DesignTheme> getCreated() {
		if (createdThemes == null)
			importThemes();

		return createdThemes;
	}

	public static void reloadExternal() {
		importedThemes = null;
		createdThemes = null;
	}

	public static DesignTheme createTheme(String name) {
		if (name.isEmpty())
			name = "My Theme";
		DesignTheme theme = new DesignTheme(name, Minecraft.getInstance().player.getName()
			.getString());
		theme.setFilePath(FilesHelper.slug(name));
		theme.setImported(true);
		theme.setDefaultPalette(PaletteDefinition.defaultPalette());
		theme.setDefaultSecondaryPalette(PaletteDefinition.defaultPalette());
		return theme.withLayers(DesignLayer.Regular, DesignLayer.Roofing, DesignLayer.Foundation)
			.withTypes(DesignType.WALL, DesignType.CORNER, DesignType.ROOF, DesignType.FACADE, DesignType.FLAT_ROOF);
	}

	public static void exportTheme(DesignTheme theme) {
		String folderPath = "themes";
		FilesHelper.createFolderIfMissing(folderPath);

		String foldername = theme.getFilePath();
		FilesHelper.createFolderIfMissing(folderPath + "/" + foldername);

		String filepath = folderPath + "/" + foldername + "/theme.json";
		FilesHelper.saveTagCompoundAsJson(theme.asTagCompound(), filepath);

		String palettePath = folderPath + "/" + foldername + "/palette.json";
		FilesHelper.saveTagCompoundAsJson(theme.getDefaultPalette()
			.writeToNBT(new CompoundNBT()), palettePath);

		String palette2Path = folderPath + "/" + foldername + "/palette2.json";
		FilesHelper.saveTagCompoundAsJson(theme.getDefaultSecondaryPalette()
			.writeToNBT(new CompoundNBT()), palette2Path);
	}

	public static String exportThemeFullyAsFile(DesignTheme theme, boolean compressed) {
		String folderPath = "themes/export";
		FilesHelper.createFolderIfMissing(folderPath);
		CompoundNBT massiveThemeTag = new CompoundNBT();

		massiveThemeTag.put("Theme", theme.asTagCompound());
		massiveThemeTag.put("Palette", theme.getDefaultPalette()
			.writeToNBT(new CompoundNBT()));
		massiveThemeTag.put("SecondaryPalette", theme.getDefaultSecondaryPalette()
			.writeToNBT(new CompoundNBT()));

		Map<DesignLayer, Map<DesignType, Set<CompoundNBT>>> designFiles =
			DesignResourceLoader.loadThemeFromFolder(theme);

		CompoundNBT layers = new CompoundNBT();
		for (DesignLayer layer : theme.getLayers()) {
			if (!designFiles.containsKey(layer))
				continue;

			CompoundNBT types = new CompoundNBT();
			for (DesignType type : theme.getTypes()) {
				if (!designFiles.get(layer)
					.containsKey(type))
					continue;

				ListNBT designs = new ListNBT();
				for (CompoundNBT tag : designFiles.get(layer)
					.get(type))
					designs.add(tag);
				types.put(type.name(), designs);
			}
			layers.put(layer.name(), types);
		}
		massiveThemeTag.put("Designs", layers);

		if (compressed) {
			try {
				Path path = Paths.get(folderPath + "/" + theme.getFilePath() + ".theme");
				Files.deleteIfExists(path);
				OutputStream outputStream = Files.newOutputStream(path, StandardOpenOption.CREATE);
				CompressedStreamTools.writeCompressed(massiveThemeTag, outputStream);
				outputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			FilesHelper.saveTagCompoundAsJsonCompact(massiveThemeTag, folderPath + "/" + theme.getFilePath() + ".json");
		}

		return theme.getFilePath() + (compressed ? ".theme" : ".json");
	}

	public static DesignTheme importThemeFullyFromFile(String path) {
		return null;
	}

	private static DesignTheme loadInternalTheme(String themeFolder) {
		CompoundNBT themeCompound = FilesHelper.loadJsonResourceAsNBT("themes/" + themeFolder + "/theme.json");
		CompoundNBT paletteCompound = FilesHelper.loadJsonResourceAsNBT("themes/" + themeFolder + "/palette.json");
		CompoundNBT palette2Compound = FilesHelper.loadJsonResourceAsNBT("themes/" + themeFolder + "/palette2.json");
		DesignTheme theme = DesignTheme.fromNBT(themeCompound);
		theme.setFilePath(themeFolder);
		theme.setImported(false);
		theme.setDefaultPalette(PaletteDefinition.fromNBT(paletteCompound));
		theme.setDefaultSecondaryPalette(PaletteDefinition.fromNBT(palette2Compound));
		return theme;
	}

	private static void importThemes() {
		importedThemes = new ArrayList<>();
		createdThemes = new ArrayList<>();
		String folderPath = "themes";

		try {
			if (!Files.isDirectory(Paths.get(folderPath)))
				Files.createDirectory(Paths.get(folderPath));

			DirectoryStream<Path> newDirectoryStream = Files.newDirectoryStream(Paths.get(folderPath));
			for (Path path : newDirectoryStream) {
				String themeFolder = path.getFileName()
					.toString();

				CompoundNBT themeCompound;
				CompoundNBT paletteCompound;
				CompoundNBT secondaryPaletteCompound = null;

				if (themeFolder.equals("export"))
					continue;

				if (themeFolder.endsWith(".theme") || themeFolder.endsWith(".json")) {
					CompoundNBT themeFile = new CompoundNBT();

					if (themeFolder.endsWith(".theme")) {
						try {
							InputStream inputStream = Files.newInputStream(Paths.get(folderPath + "/" + themeFolder),
								StandardOpenOption.READ);
							themeFile = CompressedStreamTools.readCompressed(inputStream);
							inputStream.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					} else {
						themeFile = FilesHelper.loadJsonAsNBT("themes/" + themeFolder);
					}

					themeCompound = themeFile.getCompound("Theme");
					paletteCompound = themeFile.getCompound("Palette");
					if (themeFile.contains("SecondaryPalette"))
						secondaryPaletteCompound = themeFile.getCompound("SecondaryPalette");

				} else {
					themeCompound = FilesHelper.loadJsonAsNBT(folderPath + "/" + themeFolder + "/theme.json");
					paletteCompound = FilesHelper.loadJsonAsNBT(folderPath + "/" + themeFolder + "/palette.json");
					secondaryPaletteCompound =
						FilesHelper.loadJsonAsNBT(folderPath + "/" + themeFolder + "/palette2.json");
				}

				if (themeCompound == null)
					continue;

				DesignTheme theme = DesignTheme.fromNBT(themeCompound);
				theme.setFilePath(themeFolder);
				theme.setImported(true);
				theme.setDefaultPalette(PaletteDefinition.fromNBT(paletteCompound));

				if (secondaryPaletteCompound != null)
					theme.setDefaultSecondaryPalette(PaletteDefinition.fromNBT(secondaryPaletteCompound));
				else
					theme.setDefaultSecondaryPalette(theme.getDefaultPalette());

				importedThemes.add(theme);
				if (!themeFolder.endsWith(".theme") && !themeFolder.endsWith(".json"))
					createdThemes.add(theme);
			}
			newDirectoryStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
}
