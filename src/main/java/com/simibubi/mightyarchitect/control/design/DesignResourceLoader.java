package com.simibubi.mightyarchitect.control.design;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.simibubi.mightyarchitect.TheMightyArchitect;
import com.simibubi.mightyarchitect.control.design.partials.Design;
import com.simibubi.mightyarchitect.control.helpful.FilesHelper;

import net.minecraft.nbt.NBTTagCompound;

public class DesignResourceLoader {

	private static final String BASE_PATH = "designs";

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
		
		String folderPath = "themes";
		String themePath = folderPath + "/" + theme.getFilePath();
		
		if (!Files.isDirectory(Paths.get(themePath)))
			return designMap;
		
		theme.getLayers().forEach(layer -> {

			final HashMap<DesignType, Set<Design>> typeMap = new HashMap<>();
			theme.getTypes().forEach(type -> {

				String path = folderPath + "/" + theme.getFilePath() + "/" + layer.getFilePath() + "/"
						+ type.getFilePath();
				typeMap.put(type, importExternalDesigns(theme, layer, type, path));

			});
			designMap.put(layer, typeMap);

		});
		
		return designMap;
	}

	private static Set<Design> importDesigns(DesignTheme theme, DesignLayer layer, DesignType type, String folderPath) {
		final Set<Design> designs = new HashSet<>();

		int index = 0;
		while (index < 2048) {
			final String path = folderPath + "/design" + ((index == 0) ? "" : "_" + index) + ".json";
			if (TheMightyArchitect.class.getClassLoader().getResource(path) == null)
				break;
			final NBTTagCompound designTag = FilesHelper.loadJsonResourceAsNBT(path);
			designs.add(type.getDesign().fromNBT(designTag));
			index++;
		}

		return designs;
	}
	
	private static Set<Design> importExternalDesigns(DesignTheme theme, DesignLayer layer, DesignType type, String folderPath) {
		final Set<Design> designs = new HashSet<>();
		
		if (!Files.exists(Paths.get(folderPath)))
			return designs;
		
		try {
			DirectoryStream<Path> newDirectoryStream = Files.newDirectoryStream(Paths.get(folderPath));
			for (Path path : newDirectoryStream) {
				final NBTTagCompound designTag = FilesHelper.loadJsonAsNBT(path.toString());
				designs.add(type.getDesign().fromNBT(designTag));			
			}
			newDirectoryStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return designs;
	}

}
