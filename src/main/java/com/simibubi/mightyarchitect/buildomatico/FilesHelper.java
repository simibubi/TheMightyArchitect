package com.simibubi.mightyarchitect.buildomatico;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import com.simibubi.mightyarchitect.TheMightyArchitect;

public class FilesHelper {

	public static void createFolderIfMissing(String name) {
		if (!Files.isDirectory(Paths.get(name))) {
			try {
				Files.createDirectory(Paths.get(name));
			} catch (IOException e) {
				TheMightyArchitect.logger.warn("Could not create Folder: " + name);
			}
		}
	}

	public static String findFirstValidFilename(String name, String folderPath, String extension) {
		int index = 0;
		String filename;
		String filepath;
		do {
			filename = name.toLowerCase().replace(' ', '_') + ((index == 0)? "" : "_" + index) + "." + extension;
			index++;
			filepath = folderPath + "/" + filename;
		} while (Files.exists(Paths.get(filepath)));
		return filename;
	}
	
}
