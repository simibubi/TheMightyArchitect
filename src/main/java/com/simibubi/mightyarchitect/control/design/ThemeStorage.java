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
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.simibubi.mightyarchitect.control.helpful.FilesHelper;
import com.simibubi.mightyarchitect.control.palette.PaletteDefinition;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTTagList;

public class ThemeStorage {

	public enum IncludedThemes {

		Medieval("medieval"), Fallback("fallback_theme"), Modern("modern"), TownHouse("town_house");

		public DesignTheme theme;
		public String themeFolder;

		private IncludedThemes(String themeFolder) {
			this.themeFolder = themeFolder;
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
		DesignTheme theme = new DesignTheme(name, Minecraft.getInstance().player.getName(),
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
		FilesHelper.saveTagCompoundAsJson(theme.getDefaultPalette().writeToNBT(new CompoundNBT()), palettePath);
	}

	public static String exportThemeFullyAsFile(DesignTheme theme, boolean compressed) {
		String folderPath = "themes/export";
		FilesHelper.createFolderIfMissing(folderPath);
		CompoundNBT massiveThemeTag = new CompoundNBT();

		massiveThemeTag.setTag("Theme", theme.asTagCompound());
		massiveThemeTag.setTag("Palette", theme.getDefaultPalette().writeToNBT(new CompoundNBT()));

		Map<DesignLayer, Map<DesignType, Set<CompoundNBT>>> designFiles = DesignResourceLoader
				.loadThemeFromFolder(theme);

		CompoundNBT layers = new CompoundNBT();
		for (DesignLayer layer : theme.getLayers()) {
			if (!designFiles.containsKey(layer))
				continue;

			CompoundNBT types = new CompoundNBT();
			for (DesignType type : theme.getTypes()) {
				if (!designFiles.get(layer).containsKey(type))
					continue;

				NBTTagList designs = new NBTTagList();
				for (CompoundNBT tag : designFiles.get(layer).get(type))
					designs.appendTag(tag);
				types.setTag(type.name(), designs);
			}
			layers.setTag(layer.name(), types);
		}
		massiveThemeTag.setTag("Designs", layers);

		if (compressed) {
			try {
				Path path = Paths.get(folderPath + "/" + theme.getFilePath() + ".theme");
				Files.deleteIfExists(path);
				OutputStream outputStream = Files.newOutputStream(
						path, StandardOpenOption.CREATE);
				CompressedStreamTools.writeCompressed(massiveThemeTag, outputStream);
				outputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}			
		} else {
			FilesHelper.saveTagCompoundAsJsonCompact(massiveThemeTag, folderPath + "/" + theme.getFilePath() + ".json");	
		}
		
		return theme.getFilePath() + (compressed? ".theme" : ".json");
	}

	public static DesignTheme importThemeFullyFromFile(String path) {
		return null;
	}

	private static DesignTheme loadInternalTheme(String themeFolder) {
		CompoundNBT themeCompound = FilesHelper.loadJsonResourceAsNBT("themes/" + themeFolder + "/theme.json");
		CompoundNBT paletteCompound = FilesHelper.loadJsonResourceAsNBT("themes/" + themeFolder + "/palette.json");
		DesignTheme theme = DesignTheme.fromNBT(themeCompound);
		theme.setFilePath(themeFolder);
		theme.setImported(false);
		theme.setDefaultPalette(PaletteDefinition.fromNBT(paletteCompound));
		return theme;
	}

	private static void importThemes() {
		importedThemes = new ArrayList<>();
		createdThemes = new ArrayList<>();
		String folderPath = "themes";
		if (Files.isDirectory(Paths.get(folderPath))) {

			try {
				DirectoryStream<Path> newDirectoryStream = Files.newDirectoryStream(Paths.get(folderPath));
				for (Path path : newDirectoryStream) {
					String themeFolder = path.getFileName().toString();

					CompoundNBT themeCompound;
					CompoundNBT paletteCompound;
					
					if (themeFolder.equals("export"))
						continue;

					if (themeFolder.endsWith(".theme") || themeFolder.endsWith(".json")) {
						CompoundNBT themeFile = new CompoundNBT();
						
						if (themeFolder.endsWith(".theme")) {
							try {
								InputStream inputStream = Files.newInputStream(
										Paths.get(folderPath + "/" + themeFolder),
										StandardOpenOption.READ);
								themeFile = CompressedStreamTools.readCompressed(inputStream);
								inputStream.close();
							} catch (IOException e) {
								e.printStackTrace();
							}							
						} else {
							themeFile = FilesHelper.loadJsonAsNBT("themes/" + themeFolder);
						}
						
						themeCompound = themeFile.getCompoundTag("Theme");
						paletteCompound = themeFile.getCompoundTag("Palette");						
					} else {
						themeCompound = FilesHelper.loadJsonAsNBT(folderPath + "/" + themeFolder + "/theme.json");
						paletteCompound = FilesHelper.loadJsonAsNBT(folderPath + "/" + themeFolder + "/palette.json");
					}
					
					if (themeCompound == null)
						continue;

					DesignTheme theme = DesignTheme.fromNBT(themeCompound);
					theme.setFilePath(themeFolder);
					theme.setImported(true);
					theme.setDefaultPalette(PaletteDefinition.fromNBT(paletteCompound));
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
}
