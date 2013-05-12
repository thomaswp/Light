package com.test;

import org.newdawn.slick.geom.Rectangle;
import org.newdawn.slick.geom.Vector2f;

@SuppressWarnings("serial")
public class ShadowRect extends Rectangle implements ShadowShape {

	Vector2f[] verticies = new Vector2f[4];
	
	public ShadowRect(float x, float y, float width, float height) {
		super(x, y, width, height);
		for (int i = 0; i < verticies.length; i++) {
			verticies[i] = new Vector2f();
		}
	}

	@Override
	public Vector2f[] getVertices(Vector2f light) {
		verticies[0].set(getMaxX(), getMaxY());
		verticies[1].set(getMinX(), getMaxY());
		verticies[2].set(getMinX(), getMinY());
		verticies[3].set(getMaxX(), getMinY());
		return verticies;
	}

	@Override
	public Vector2f getIntersection(Vector2f start, Vector2f dir, Vector2f ignore) {
		getVertices(null);
		Vector2f closest = null;
		for (int i = 0; i < 4; i++) {
			Vector2f p0 = verticies[i];
			Vector2f p1 = verticies[(i + 1) % 4];
			
			if (p0 == ignore || p1 == ignore) continue;
			
			Vector2f intersect = Game.intersect(start, dir, p0, p1);
			if (intersect == null) continue;
			
			if (closest == null || intersect.distance(start) < closest.distance(start)) {
				closest = intersect;
			}
		}
		return closest;
	}

	@Override
	public boolean contains(Vector2f pos) {
		return contains(pos.x, pos.y);
	}

}
