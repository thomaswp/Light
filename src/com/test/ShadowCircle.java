package com.test;

import org.newdawn.slick.geom.Circle;
import org.newdawn.slick.geom.Vector2f;

@SuppressWarnings("serial")
public class ShadowCircle extends Circle implements ShadowShape {

	Vector2f[] verticies = new Vector2f[2];
	
	public ShadowCircle(float x, float y, float radius) {
		super(x, y, radius);
	}

	@Override
	public Vector2f[] getVertices(Vector2f light) {
		Vector2f toLight = light.copy();
		float x = getCenterX(), y = getCenterY();
		toLight.x -= x; toLight.y -= y;
		toLight.add(90);
		toLight.normalise();
		toLight.scale(radius);
		verticies[0] = toLight.copy();
		verticies[0].x += x; verticies[0].y += y;
		verticies[1] = toLight.scale(-1);
		verticies[1].x += x; verticies[1].y += y;
		return verticies;
	}

	@Override
	public Vector2f getIntersection(Vector2f start, Vector2f dir,
			Vector2f ignore) {
		for (int i = 0; i < 2; i++) {
			if (verticies[i] == ignore) return null;
		}
		float x = getCenterX(), y = getCenterY();
		float ox = start.x - x, oy = start.y - y;
		float a = dir.x * dir.x + dir.y * dir.y;
		float b = 2 * (ox * dir.x + oy * dir.y);
		float c = ox * ox + oy * oy - radius * radius;
		float disc = b * b - 4 * a * c;
		if (disc < 0) return null;
		float t0 = (-b - (float)Math.sqrt(disc)) / (2 * a);
		float t1 = (-b + (float)Math.sqrt(disc)) / (2 * a);
		float t;
		if (t0 < 0) {
			if (t1 < 0) return null;
			t = t1;
		} else {
			t = t0;
		}
		return dir.copy().scale(t).add(start);
	}

	@Override
	public boolean contains(Vector2f pos) {
		return contains(pos.x, pos.y);
	}

}
