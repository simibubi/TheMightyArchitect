package com.simibubi.mightyarchitect.gui.widgets;

public class GuiElementAnimation {
	public float y;
	public float targetY;
	private float velY;
	public GuiElementAnimation(float startY, float targetY) {
		y = startY;
		this.targetY = targetY;
		velY = 0;
	}
	public float jumpInFromBelow() {
		// target not reached?
		if (y != targetY || velY != 0) {
			// downwards momentum and target passed -> lock to target
			y += velY;
			if (velY > 0 && y > targetY) {
				y = targetY;
				velY = 0;
			// above target -> fall down
			} else if (y < targetY) {
				velY += 3;
			// below target -> move up
			} else {
				velY = -20;
			}
		}
		return y;
	}
}