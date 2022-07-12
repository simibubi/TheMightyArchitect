package com.simibubi.mightyarchitect.control.design;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.simibubi.mightyarchitect.TheMightyArchitect;
import com.simibubi.mightyarchitect.control.design.partials.Design;
import com.simibubi.mightyarchitect.foundation.utility.FilesHelper;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;

public class DesignResourceLoader {

	private static final String BASE_PATH = "themes";

	public static Map<DesignLayer, Map<DesignType, Set<Design>>> loadDesignsForTheme(DesignTheme theme) {
		if (theme.isImported())
			return loadExternalDesignsForTheme(theme);

		final Map<DesignLayer, Map<DesignType, Set<Design>>> designMap = new HashMap<>();
		theme.getLayers().forEach(layer -> {

			final HashMap<DesignType, Set<Design>> typeMap = new HashMap<>();
			theme.getTypes().forEach(type -> {

				String path = BASE_PATH + "/" + theme.getFilePath() + "/" + layer.getFilePath() + "/"
						+ type.getFilePath();
				typeMap.put(type, importDesigns(theme, layer, type, path));

			});
			designMap.put(layer, typeMap);

		});

		designMap.putAll(loadExternalDesignsForTheme(theme)); // extensions

		return designMap;
	}

	public static Map<DesignLayer, Map<DesignType, Set<Design>>> loadExternalDesignsForTheme(DesignTheme theme) {
		final Map<DesignLayer, Map<DesignType, Set<Design>>> designMap = new HashMap<>();
		boolean isFile = theme.getFilePath().endsWith(".theme") || theme.getFilePath().endsWith(".json");
		final Map<DesignLayer, Map<DesignType, Set<CompoundTag>>> compoundMap = isFile ? loadThemeFromThemeFile(theme) : loadThemeFromFolder(theme);

		theme.getLayers().forEach(layer -> {
			if (!compoundMap.containsKey(layer))
				return;

			final HashMap<DesignType, Set<Design>> typeMap = new HashMap<>();
			theme.getTypes().forEach(type -> {
				if (!compoundMap.get(layer).containsKey(type))
					return;

				Set<Design> designs = new HashSet<>();
				compoundMap.get(layer).get(type).forEach(compound -> designs.add(type.getDesign().fromNBT(compound)));
				typeMap.put(type, designs);
			});
			designMap.put(layer, typeMap);
		});
		return designMap;
	}

	private static Map<DesignLayer, Map<DesignType, Set<CompoundTag>>> loadThemeFromThemeFile(DesignTheme theme) {
		final Map<DesignLayer, Map<DesignType, Set<CompoundTag>>> compoundMap = new HashMap<>();

		CompoundTag importedThemeFile = new CompoundTag();
		
		if (theme.getFilePath().endsWith(".theme")) {
			try {
				InputStream inputStream = Files.newInputStream(Paths.get("themes/" + theme.getFilePath()),
						StandardOpenOption.READ);
				importedThemeFile = NbtIo.readCompressed(inputStream);
				inputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}			
		} else {
			importedThemeFile = FilesHelper.loadJsonAsNBT("themes/" + theme.getFilePath());
		}
		
		final CompoundTag themeFile = importedThemeFile;

		if (themeFile.contains("Designs")) {
			theme.getLayers().forEach(layer -> {

				final HashMap<DesignType, Set<CompoundTag>> typeMap = new HashMap<>();
				theme.getTypes().forEach(type -> {

					Set<CompoundTag> designs = new HashSet<>();
					CompoundTag tagLayers = themeFile.getCompound("Designs");
					if (tagLayers.contains(layer.name())) {
						CompoundTag tagTypes = tagLayers.getCompound(layer.name());
						if (tagTypes.contains(type.name())) {
							ListTag tagDesigns = tagTypes.getList(type.name(), 10);
							tagDesigns.forEach(tag -> designs.add((CompoundTag) tag));
						}
					}
					typeMap.put(type, designs);
				});
				compoundMap.put(layer, typeMap);

			});
		}

		return compoundMap;
	}

	public static Map<DesignLayer, Map<DesignType, Set<CompoundTag>>> loadThemeFromFolder(DesignTheme theme) {
		final Map<DesignLayer, Map<DesignType, Set<CompoundTag>>> compoundMap = new HashMap<>();

		String folderPath = "themes";
		String themePath = folderPath + "/" + theme.getFilePath();

		if (!Files.exists(Paths.get(themePath)) && !Files.isDirectory(Paths.get(themePath)))
			return compoundMap;

		theme.getLayers().forEach(layer -> {

			final HashMap<DesignType, Set<CompoundTag>> typeMap = new HashMap<>();
			theme.getTypes().forEach(type -> {

				String path = folderPath + "/" + theme.getFilePath() + "/" + layer.getFilePath() + "/"
						+ type.getFilePath();
				typeMap.put(type, importExternalDesigns(theme, layer, type, path));

			});
			compoundMap.put(layer, typeMap);

		});

		return compoundMap;
	}

	private static Set<Design> importDesigns(DesignTheme theme, DesignLayer layer, DesignType type, String folderPath) {
		final Set<Design> designs = new HashSet<>();

		int index = 0;
		while (index < 2048) {
			final String path = folderPath + "/design" + ((index == 0) ? "" : "_" + index) + ".json";
			if (TheMightyArchitect.class.getClassLoader().getResource(path) == null)
				break;
			final CompoundTag designTag = FilesHelper.loadJsonResourceAsNBT(path);
			designs.add(type.getDesign().fromNBT(designTag));
			index++;
		}

		return designs;
	}

	private static Set<CompoundTag> importExternalDesigns(DesignTheme theme, DesignLayer layer, DesignType type,
			String folderPath) {
		final Set<CompoundTag> designs = new HashSet<>();

		if (!Files.exists(Paths.get(folderPath)))
			return designs;

		try {
			DirectoryStream<Path> newDirectoryStream = Files.newDirectoryStream(Paths.get(folderPath));
			for (Path path : newDirectoryStream) {
				final CompoundTag designTag = FilesHelper.loadJsonAsNBT(path.toString());
				designs.add(designTag);
			}
			newDirectoryStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return designs;
	}

}
