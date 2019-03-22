package com.simibubi.mightyarchitect.control.design;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import com.simibubi.mightyarchitect.control.helpful.FilesHelper;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTTagCompound;

public class ThemeStorage {

	public enum IncludedThemes {
		Medieval(new DesignTheme("medieval", "Medieval", "simibubi", new StandardDesignPicker())
				.withLayers(DesignLayer.Foundation, DesignLayer.Regular, DesignLayer.Open, DesignLayer.Roofing)
				.withTypes(DesignType.WALL, DesignType.CORNER, DesignType.ROOF, DesignType.TOWER, DesignType.FACADE,
						DesignType.FLAT_ROOF, DesignType.TOWER_FLAT_ROOF, DesignType.TOWER_ROOF));

		public DesignTheme theme;

		private IncludedThemes(DesignTheme theme) {
			this.theme = theme;
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
		for (IncludedThemes theme : IncludedThemes.values())
			included.add(theme.theme);
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
		DesignTheme theme = new DesignTheme(FilesHelper.slug(name), name, Minecraft.getMinecraft().player.getName(),
				new StandardDesignPicker());
		return theme.withLayers(DesignLayer.Regular, DesignLayer.Roofing, DesignLayer.Foundation).withTypes(
				DesignType.WALL, DesignType.CORNER, DesignType.ROOF, DesignType.FACADE,
				DesignType.FLAT_ROOF);
	}

	public static void exportTheme(DesignTheme theme) {
		String folderPath = "themes";
		FilesHelper.createFolderIfMissing(folderPath);

		String foldername = theme.getFilePath();
		FilesHelper.createFolderIfMissing(folderPath + "/" + foldername);

		String filepath = folderPath + "/" + foldername + "/theme.json";
		FilesHelper.saveTagCompoundAsJson(theme.asTagCompound(), filepath);
	}

	private static void importThemes() {
		importedThemes = new ArrayList<>();
		String folderPath = "themes";
		if (Files.isDirectory(Paths.get(folderPath))) {

			try {
				DirectoryStream<Path> newDirectoryStream = Files.newDirectoryStream(Paths.get(folderPath));
				for (Path path : newDirectoryStream) {
					String string = path.getFileName().toString();
					
					NBTTagCompound compound = FilesHelper
							.loadJsonAsNBT(folderPath + "/" + string + "/theme.json");
					DesignTheme theme = DesignTheme.fromNBT(string, compound);
					importedThemes.add(theme);
				}
				newDirectoryStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			} 

		}

	}

}
