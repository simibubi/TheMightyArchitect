package com.simibubi.mightyarchitect.control.design;

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

}
