package com.test;

import net.java.games.input.Version;

import org.newdawn.slick.geom.Polygon;
import org.newdawn.slick.geom.Vector2f;

@SuppressWarnings("serial")
public class ShadowPolygon extends Polygon implements ShadowShape{

	Vector2f[] verticies;
	
	public ShadowPolygon(float[] verticies) {
		super(verticies);
	}
	
	@Override
	public Vector2f[] getVertices(Vector2f light) {
		float[] verts = getPoints();
		if (verticies == null || verticies.length != verts.length / 2) {
			verticies = new Vector2f[verts.length / 2];
		}
		for (int i = 0; i < verticies.length; i++) {
			if (verticies[i] == null) verticies[i] = new Vector2f();
			verticies[i].set(verts[i * 2], verts[i * 2 + 1]);
		}
		return verticies;
	}

	@Override
	public Vector2f getIntersection(Vector2f start, Vector2f dir,
			Vector2f ignore) {
		getVertices(null);
		Vector2f closest = null;
		for (int i = 0; i < verticies.length; i++) {
			Vector2f p0 = verticies[i];
			Vector2f p1 = verticies[(i + 1) % verticies.length];
			
			if (p0 == ignore || p1 == ignore) continue;
			
			Vector2f intersect = Game.intersect(start, dir, p0, p1);
			if (intersect == null) continue;
//			for (int j = 0; j < verticies.length; j++) {
//				if (intersect.equals(verticies[j])) {
//					int before = (j - 1 + 4) % 4;
//					int after = (j + 1) % 4;
//					if (verticies[before] == ignore || verticies[after] == ignore) {
//						continue;
//					}
//				}
//			}
			
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
