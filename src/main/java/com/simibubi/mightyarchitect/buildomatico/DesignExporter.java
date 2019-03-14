package com.simibubi.mightyarchitect.buildomatico;

import java.util.HashMap;
import java.util.Map;

import com.simibubi.mightyarchitect.block.AllBlocks;
import com.simibubi.mightyarchitect.buildomatico.helpful.FilesHelper;
import com.simibubi.mightyarchitect.buildomatico.model.sketch.DesignLayer;
import com.simibubi.mightyarchitect.buildomatico.model.sketch.DesignSlice.DesignSliceTrait;
import com.simibubi.mightyarchitect.buildomatico.model.sketch.DesignTheme;
import com.simibubi.mightyarchitect.buildomatico.model.sketch.DesignType;
import com.simibubi.mightyarchitect.buildomatico.model.sketch.Wall;

import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class DesignExporter {

	public static String exportDesign(World worldIn, BlockPos anchor, ItemStack exporter) {
		NBTTagCompound itemTag = exporter.getTagCompound();
		
		DesignTheme theme = DesignTheme.valueOf(itemTag.getString("Theme"));
		DesignType type = DesignType.valueOf(itemTag.getString("Type"));
		DesignLayer layer = DesignLayer.valueOf(itemTag.getString("Layer"));

		BlockPos layerDefAnchor = anchor;
		boolean found = false;
		for (int range = 1; range < 100 && !found; range++) {
			for (int i = 0; i < range; i++) {
				if (isMarker(worldIn, anchor.add(range, 0, i))) {
					layerDefAnchor = anchor.add(range, 0, i);
					found = true;
					break;
				} else if (isMarker(worldIn, anchor.add(i, 0, range))) {
					layerDefAnchor = anchor.add(i, 0, range);
					found = true;
					break;
				}
			}
		}
		
		if (found) {
			// Collect information
			int height = 0;
			for (BlockPos pos = layerDefAnchor; isMarker(worldIn, pos); pos = pos.up())
				height++;

			BlockPos size = layerDefAnchor.west().subtract(anchor.east()).add(1, height, 1);

			// Assemble nbt
			NBTTagCompound compound = new NBTTagCompound();
			compound.setTag("Size", NBTUtil.createPosTag(size));

			NBTTagList layers = new NBTTagList();

			Map<IBlockState, Palette> scanMap = new HashMap<>();
			PaletteDefinition.defaultPalette().getDefinition().forEach((palette, block) -> {
				scanMap.put(block, palette);
			});

			for (int y = 0; y < size.getY(); y++) {
				NBTTagCompound layerTag = new NBTTagCompound();
				DesignSliceTrait trait = DesignSliceTrait.values()[markerValueAt(worldIn, layerDefAnchor.up(y))];
				layerTag.setString("Trait", trait.name());
				
				StringBuilder data = new StringBuilder();
				for (int z = 0; z < size.getZ(); z++) {
					for (int x = 0; x < size.getX(); x++) {
						Palette block = scanMap.get(worldIn.getBlockState(anchor.east().add(x, y, z)));
						data.append(block != null? block.asChar() : ' ');
					}
					if (z < size.getZ() - 1)
						data.append(",");
				}
				layerTag.setString("Blocks", data.toString());
				layers.appendTag(layerTag);
			}

			compound.setTag("Layers", layers);
			
			// Additional data
			if (itemTag.hasKey("Additional")) {
				int data = itemTag.getInteger("Additional");
				switch (type) {
				case ROOF:
					compound.setInteger("Roofspan", data);
					break;
				case FLAT_ROOF:
					compound.setInteger("Margin", data);
					break;
				case WALL:
					compound.setString("ExpandBehaviour", Wall.ExpandBehaviour.values()[data].name());
					break;
				case TOWER:
					compound.setInteger("Radius", data);
					break;
				default:
					break;
				}
				
			}
			
			// Write nbt to file
			String basePath = "designs";
			FilesHelper.createFolderIfMissing(basePath);
			String themePath = basePath + "/" + theme.getFilePath();
			FilesHelper.createFolderIfMissing(themePath);
			String layerPath = themePath + "/" + layer.getFilePath();
			FilesHelper.createFolderIfMissing(layerPath);
			String typePath = layerPath + "/" + type.getFilePath();
			FilesHelper.createFolderIfMissing(typePath);
			
			String filename = FilesHelper.findFirstValidFilename("design", typePath, "json");
			String designPath = typePath + "/" + filename;
			FilesHelper.saveTagCompoundAsJson(compound, designPath);
			return designPath;
			//
		}
		return "";
	}

	private static boolean isMarker(World worldIn, BlockPos pos) {
		return worldIn.getBlockState(pos).getBlock() == AllBlocks.slice_marker;
	}

	private static int markerValueAt(World worldIn, BlockPos pos) {
		return AllBlocks.slice_marker.getMetaFromState(worldIn.getBlockState(pos));
	}

}
