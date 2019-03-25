package com.simibubi.mightyarchitect.control.design;

import java.nio.file.Files;
import java.nio.file.Paths;

import com.simibubi.mightyarchitect.TheMightyArchitect;
import com.simibubi.mightyarchitect.block.AllBlocks;
import com.simibubi.mightyarchitect.control.compose.Cuboid;
import com.simibubi.mightyarchitect.control.design.DesignSlice.DesignSliceTrait;
import com.simibubi.mightyarchitect.control.design.partials.Wall;
import com.simibubi.mightyarchitect.control.helpful.FilesHelper;
import com.simibubi.mightyarchitect.control.palette.BlockOrientation;
import com.simibubi.mightyarchitect.control.palette.Palette;
import com.simibubi.mightyarchitect.control.palette.PaletteDefinition;
import com.simibubi.mightyarchitect.control.phase.export.PhaseEditTheme;
import com.simibubi.mightyarchitect.networking.PacketPlaceSign;
import com.simibubi.mightyarchitect.networking.PacketSender;

import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class DesignExporter {

	public static PaletteDefinition scanningPalette = PaletteDefinition.defaultPalette();

	public static DesignTheme theme;
	public static DesignType type;
	public static DesignLayer layer;

	public static int designParameter;
	
	public static boolean changed = true;

	public static String exportDesign(World worldIn, BlockPos anchor) {
		BlockPos layerDefAnchor = anchor;
		boolean found = false;
		for (int range = 1; range < 100 && !found; range++) {
			for (int i = 0; i <= range; i++) {
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

		if (!found) {
			return "";
		}

		// Collect information
		int height = 0;
		int effectiveHeight = 0;
		for (BlockPos pos = layerDefAnchor; isMarker(worldIn, pos); pos = pos.up()) {
			height++;
			if (DesignSliceTrait.values()[markerValueAt(worldIn, pos)] != DesignSliceTrait.MaskAbove) 
				effectiveHeight++;
		}
		
		if (effectiveHeight != PhaseEditTheme.effectiveHeight) {
			PhaseEditTheme.effectiveHeight = effectiveHeight;
			changed = true;
		}

		BlockPos size = layerDefAnchor.west().subtract(anchor.east()).add(1, height, 1);

		boolean visualizing = PhaseEditTheme.isVisualizing();
		Cuboid bounds = new Cuboid(anchor.east(), size);
		boolean boundsChanged = visualizing && !PhaseEditTheme.selectedDesign.equals(bounds) || changed;
		
		changed = false;
		
		if (!visualizing || boundsChanged) {
			PhaseEditTheme.setVisualization(bounds);
			return "Design traits visualized, click again to confirm.";
		} 
		
		PhaseEditTheme.resetVisualization();
		
		// Assemble nbt
		NBTTagCompound compound = new NBTTagCompound();
		compound.setTag("Size", NBTUtil.createPosTag(size));

		NBTTagList layers = new NBTTagList();

		for (int y = 0; y < size.getY(); y++) {
			NBTTagCompound layerTag = new NBTTagCompound();
			DesignSliceTrait trait = DesignSliceTrait.values()[markerValueAt(worldIn, layerDefAnchor.up(y))];
			layerTag.setString("Trait", trait.name());

			StringBuilder data = new StringBuilder();
			for (int z = 0; z < size.getZ(); z++) {
				for (int x = 0; x < size.getX(); x++) {
					Palette block = scanningPalette.scan(worldIn.getBlockState(anchor.east().add(x, y, z)));
					data.append(block != null ? block.asChar() : ' ');
				}
				if (z < size.getZ() - 1)
					data.append(",");
			}
			layerTag.setString("Blocks", data.toString());

			StringBuilder orientationStrip = new StringBuilder();
			for (int z = 0; z < size.getZ(); z++) {
				for (int x = 0; x < size.getX(); x++) {
					BlockOrientation orientation = BlockOrientation
							.byState(worldIn.getBlockState(anchor.east().add(x, y, z)));
					orientationStrip.append(orientation.asChar());
				}
				if (z < size.getZ() - 1)
					orientationStrip.append(",");
			}
			layerTag.setString("Facing", orientationStrip.toString());

			layers.appendTag(layerTag);
		}

		compound.setTag("Layers", layers);

		// Additional data
		int data = designParameter;
		switch (type) {
		case ROOF:
			compound.setInteger("Roofspan", data);
			break;
		case FLAT_ROOF:
			compound.setInteger("Margin", data);
			break;
		case WALL:
			if (data == -1) 
				return "Revisit the Design settings.";
			compound.setString("ExpandBehaviour", Wall.ExpandBehaviour.values()[data].name());
			break;
		case TOWER_FLAT_ROOF:
		case TOWER_ROOF:
		case TOWER:
			compound.setInteger("Radius", data);
			break;
		default:
			break;
		}

		// Write nbt to file

		String basePath = "themes";
		FilesHelper.createFolderIfMissing(basePath);
		String themePath = basePath + "/" + theme.getFilePath();
		FilesHelper.createFolderIfMissing(themePath);
		String layerPath = themePath + "/" + layer.getFilePath();
		FilesHelper.createFolderIfMissing(layerPath);
		String typePath = layerPath + "/" + type.getFilePath();
		FilesHelper.createFolderIfMissing(typePath);

		String filename = "";
		String designPath = "";

		BlockPos signPos = anchor.up();
		if (worldIn.getBlockState(signPos).getBlock() == Blocks.STANDING_SIGN) {
			TileEntitySign sign = (TileEntitySign) worldIn.getTileEntity(signPos);
			filename = sign.signText[1].getUnformattedText();
			designPath = typePath + "/" + filename;

		} else {
			int index = 0;
			while (index < 2048) {
				filename = "design" + ((index == 0) ? "" : "_" + index) + ".json";
				designPath = typePath + "/" + filename;
				if (TheMightyArchitect.class.getClassLoader().getResource(designPath) == null
						&& !Files.exists(Paths.get(designPath)))
					break;
				index++;
			}
		}

		PacketSender.INSTANCE.sendToServer(new PacketPlaceSign(filename, signPos));
		FilesHelper.saveTagCompoundAsJson(compound, designPath);
		return designPath;
		//

	}
	
	public static void setTheme(DesignTheme theme) {
		DesignExporter.theme = theme;
		layer = DesignLayer.Regular;
		type = DesignType.WALL;
		changed = true;
	}

	public static DesignTheme getTheme() {
		return theme;
	}

	private static boolean isMarker(World worldIn, BlockPos pos) {
		return worldIn.getBlockState(pos).getBlock() == AllBlocks.slice_marker;
	}

	private static int markerValueAt(World worldIn, BlockPos pos) {
		return AllBlocks.slice_marker.getMetaFromState(worldIn.getBlockState(pos));
	}

}
