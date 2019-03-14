package com.simibubi.mightyarchitect.buildomatico;

import java.util.List;

import com.simibubi.mightyarchitect.buildomatico.StyleGroupManager.StyleGroupDesignProvider;
import com.simibubi.mightyarchitect.buildomatico.helpful.DesignHelper;
import com.simibubi.mightyarchitect.buildomatico.model.groundPlan.Cuboid;
import com.simibubi.mightyarchitect.buildomatico.model.groundPlan.GroundPlan;
import com.simibubi.mightyarchitect.buildomatico.model.sketch.Design.DesignInstance;
import com.simibubi.mightyarchitect.buildomatico.model.sketch.DesignLayer;
import com.simibubi.mightyarchitect.buildomatico.model.sketch.DesignTheme;
import com.simibubi.mightyarchitect.buildomatico.model.sketch.Sketch;

import net.minecraft.util.math.BlockPos;

public class StandardDesignPicker implements IPickDesigns {

	private DesignTheme theme;
	
	public Sketch assembleSketch(GroundPlan groundPlan) {
		Sketch sketch = pickDesigns(groundPlan);
		sketch.setContext(groundPlan.getContext());
		return sketch;
	}

	private Sketch pickDesigns(GroundPlan groundPlan) {
		Sketch sketch = new Sketch();
		StyleGroupManager styleGroupManager = new StyleGroupManager();
		
		for (int layer = 0; layer < groundPlan.layerCount; layer++) {
			for (Cuboid c : groundPlan.getCuboidsOnLayer(layer)) {
				
				BlockPos origin = c.getOrigin();
				List<DesignInstance> designList = c.isSecondary() ? sketch.secondary : sketch.primary;
				StyleGroupDesignProvider styleGroup = styleGroupManager.getStyleGroup(c.styleGroup);

				BlockPos size = c.getSize();
				DesignHelper.addCuboid(styleGroup, designList, theme, c.designLayer, origin, size);
				
				if (!c.isTop())
					continue;
				
				switch (c.roofType) {
				case ROOF:
					if (c.width == c.length) {
						DesignHelper.addNormalCrossRoof(styleGroup, designList, theme, DesignLayer.Independent, origin.up(c.height), size);
					} else {
						DesignHelper.addNormalRoof(styleGroup, designList, theme, DesignLayer.Independent, origin.up(c.height), size);
					}
					break;
					
				case FLAT_ROOF:
					DesignHelper.addFlatRoof(styleGroup, designList, theme, DesignLayer.Independent, origin.up(c.height), size);
					break;
					
				default:
					break;
				}
			}
		}

		sketch.interior = groundPlan.getRoomSpaceCuboids();
		return sketch;
	}

	@Override
	public void setTheme(DesignTheme theme) {
		this.theme = theme;
	}

}
