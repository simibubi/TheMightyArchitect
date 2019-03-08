package com.simibubi.mightyarchitect.buildomatico.model.sketch;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.simibubi.mightyarchitect.buildomatico.Palette;
import com.simibubi.mightyarchitect.buildomatico.PaletteDefinition;
import com.simibubi.mightyarchitect.buildomatico.model.groundPlan.Cuboid;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;

public abstract class Design {

	public enum Type {
		FACADE, WALL_REPEAT, WALL_REPEAT_OVERRIDE, WALL, TRIM, TOWER, FEATURE, CORNER, PILLAR, ROOF;
	}

	public enum Style {
		HEAVY, LIGHT, OPEN, ANY;
	}

	public Vec3i size;
	public Layer[] layers;
	public Set<Integer> heights;
	public int defaultHeight;
	public int defaultWidth;
	public Style style;
	public int yShift;

	public Design(List<String> definition) {
		String[] keyWords = definition.get(0).split(" ");
		String[] dimensions = keyWords[1].split("x");
		size = new Vec3i(Integer.parseInt(dimensions[0]), Integer.parseInt(dimensions[1]),
				Integer.parseInt(dimensions[2]));

		style = Style.valueOf(keyWords[3]);
		layers = new Layer[size.getY()];
		defaultWidth = size.getX();
		heights = new HashSet<>();
		heights.add(0);
		yShift = 0;
		defaultHeight = 0;
		for (int layerIndex = 0; layerIndex < layers.length; layerIndex++) {
			layers[layerIndex] = new Layer(size, definition.get(layerIndex * 2 + 1),
					definition.get(layerIndex * 2 + 2));
			adjustHeight(layers[layerIndex].type);
		}
	}

	private void adjustHeight(Layer.Type type) {
		Set<Integer> newHeights = new HashSet<>();
		for (Integer integer : heights) {
			switch (type) {
			case CLONE:
				newHeights.add(integer + 1);
				newHeights.add(integer + 2);
				newHeights.add(integer + 3);
				newHeights.add(integer + 4);
				break;
			case CLONE_ONCE:
				newHeights.add(integer + 1);
				newHeights.add(integer + 2);
				break;
			case NOT_SUPPORTIVE:
				newHeights.add(integer);
				break;
			case OPTIONAL:
				newHeights.add(integer);
				newHeights.add(integer + 1);
				break;
			case MASK_BELOW:
				newHeights.add(integer);
				break;
			case STANDARD:
				newHeights.add(integer + 1);
				break;
			default:
				break;
			}

		}
		if (type == Layer.Type.MASK_BELOW)
			yShift--;
		if (type != Layer.Type.NOT_SUPPORTIVE && type != Layer.Type.MASK_BELOW)
			defaultHeight++;
		heights = newHeights;
	}

	public static Design fromDefinition(List<String> definition) {
		String[] keyWords = definition.get(0).split(" ");
		if (keyWords[0].equals("DESIGN")) {
			switch (Type.valueOf(keyWords[2])) {
			case FACADE:
				return new Facade(definition);
			case WALL:
			case WALL_REPEAT:
			case WALL_REPEAT_OVERRIDE:
				return new Wall(definition);
			case CORNER:
				return new Corner(definition);
			case TRIM:
				return new Trim(definition);
			case TOWER:
				return new Tower(definition);
			case FEATURE:
				return new Feature(definition);
			case PILLAR:
				return new Pillar(definition);
			case ROOF:
				return new Roof(definition);

			default:
				return null;
			}
		}
		return null;
	}

	public abstract Type getType();

	public void getBlocks(DesignInstance instance, Map<BlockPos, PaletteBlockInfo> blocks) {
		getBlocksShifted(instance, blocks, BlockPos.ORIGIN);
	}
	
	protected void getBlocksShifted(DesignInstance instance, Map<BlockPos, PaletteBlockInfo> blocks, BlockPos localShift) {
		BlockPos position = instance.localAnchor;
		BlockPos totalShift = localShift.add(0, yShift, 0);
		List<Layer> toPrint = selectPrintedLayers(instance);

		for (int y = 0; y < toPrint.size(); y++) {
			Layer layer = toPrint.get(y);
			for (int x = 0; x < size.getX(); x++) {
				for (int z = 0; z < size.getZ(); z++) {
					Palette key = layer.blocks[z][x];
					if (key == null)
						continue;
					BlockPos pos = rotateAroundZero(new BlockPos(x, y, z).add(totalShift), instance.rotationY)
							.add(position);
					putBlock(blocks, pos, key, EnumFacing.fromAngle(instance.rotationY));
				}
			}
		}
	}

	protected Cuboid getBounds(DesignInstance instance) {
		return new Cuboid(instance.localAnchor, rotateAroundZero(new BlockPos(instance.width, instance.height, instance.depth), instance.rotationY));
	}

	@Deprecated
	protected void putBlock(World world, PaletteDefinition palette, Palette key, BlockPos pos) {
		IBlockState state = palette.get(key);
		IBlockState existing = world.getBlockState(pos);
		if (key == Palette.CLEAR || (existing.getBlock() != palette.clear().getBlock() && !existing.isFullCube()))
			world.setBlockState(pos, state);
	}
	
	protected void putBlock(Map<BlockPos, PaletteBlockInfo> blocks, BlockPos pos, Palette palette, EnumFacing facing) {
		if (!blocks.containsKey(pos) || !blocks.get(pos).palette.isPrefferedOver(palette)) {
			blocks.put(pos, new PaletteBlockInfo(palette, facing));
		}
	}

	protected List<Layer> selectPrintedLayers(DesignInstance instance) {
		List<Layer> toPrint = new LinkedList<>();
		int currentHeight = defaultHeight;
		for (Layer layer : layers) {
			switch (layer.type) {
			case NOT_SUPPORTIVE:
			case MASK_BELOW:
			case STANDARD:
				toPrint.add(layer);
				break;
			case OPTIONAL:
				if (currentHeight > instance.height)
					currentHeight--;
				else
					toPrint.add(layer);
				break;
			case CLONE_ONCE:
				toPrint.add(layer);
				if (currentHeight < instance.height) {
					currentHeight++;
					toPrint.add(layer);
				}
				break;
			case CLONE:
				toPrint.add(layer);
				for (int i = 0; i < 3 && currentHeight < instance.height; i++) {
					currentHeight++;
					toPrint.add(layer);
				}
				break;
			default:
				break;
			}
		}
		return toPrint;
	}

	public String toString() {
		String heights = "HEIGHTS ";
		for (Integer integer : this.heights) {
			heights += integer + " ";
		}
		return String.format("%s %s DESIGN", style.name(), getType().name()) + "\n" + heights;
	}

	public boolean fitsHorizontally(int width) {
		return this.defaultWidth == width;
	}

	public boolean fitsVertically(int height) {
		return heights.contains(Integer.valueOf(height));
	}

	public BlockPos rotateAroundZero(BlockPos in, int rotation) {
		return rotateAround(in, rotation, new BlockPos(0, 0, 0));
	}

	public BlockPos rotateAround(BlockPos in, int rotation, BlockPos origin) {
		BlockPos local = in.subtract(origin);
		int x = (rotation == 180) ? -local.getX()
				: (rotation == 90) ? -local.getZ() : (rotation == -90) ? local.getZ() : local.getX();
		int z = (rotation == 180) ? -local.getZ()
				: (rotation == 90) ? local.getX() : (rotation == -90) ? -local.getX() : local.getZ();
		BlockPos rotated = new BlockPos(x, local.getY(), z);
		return rotated.add(origin);
	}

	public DesignInstance create(BlockPos anchor, int rotation, int height) {
		return create(anchor, rotation, size.getX(), height);
	}

	public DesignInstance create(BlockPos anchor, int rotation, int width, int height) {
		return new DesignInstance(this, anchor, rotation, width, height, 0);
	}

	public class DesignInstance {

		BlockPos localAnchor;
		int rotationY, rotationZ;
		int width, height, depth;
		Design template;
		boolean hasGlass;

		public DesignInstance(Design template, BlockPos anchor, int rotation, int width, int height, int depth) {
			this.template = template;
			this.localAnchor = anchor;
			this.height = height;
			this.rotationY = rotation;
			this.width = width;
			this.depth = depth;
		}

		public void getBlocks(Map<BlockPos, PaletteBlockInfo> blocks) {
			template.getBlocks(this, blocks);
		}


	}


}
