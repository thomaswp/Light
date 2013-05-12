package com.test;

import org.newdawn.slick.geom.Vector2f;

public interface ShadowShape {

	public Vector2f[] getVertices(Vector2f light);
	public Vector2f getIntersection(Vector2f start, Vector2f dir, Vector2f ignore);
	public boolean contains(Vector2f pos);

}
