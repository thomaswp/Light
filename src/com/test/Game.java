package com.test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import org.lwjgl.Sys;
import org.lwjgl.input.Cursor;
import org.newdawn.slick.AppGameContainer;
import org.newdawn.slick.BasicGame;
import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.Input;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.geom.Circle;
import org.newdawn.slick.geom.GeomUtil;
import org.newdawn.slick.geom.Polygon;
import org.newdawn.slick.geom.Rectangle;
import org.newdawn.slick.geom.Shape;
import org.newdawn.slick.geom.Transform;
import org.newdawn.slick.geom.Vector2f;
import org.newdawn.slick.util.pathfinding.navmesh.Space;


public class Game extends BasicGame{

	List<ShadowRect> rects = new ArrayList<ShadowRect>();
	List<Vector2f> lightPoints = new ArrayList<Vector2f>();
	ShadowPolygon shadowPoly;
	ShadowCircle shadowCircle;
	ArrayList<ShadowShape> allShapes;
	ArrayList<ShadowShape> visibleShapes = new ArrayList<ShadowShape>();
	
	ShadowRect bounds;
	
	Polygon lightPolygon;
	Circle lightCircle;
	
	Circle indicator;
	
	public Game() {
		super("Game Test");
	}

	@Override
	public void init(GameContainer game) throws SlickException {
		game.setTargetFrameRate(60);
		
		rects.add(new ShadowRect(200, 150, 100, 100));
		rects.add(new ShadowRect(400, 350, 100, 100));
		rects.add(new ShadowRect(705, 95, 100, 100));
		rects.add(new ShadowRect(600, 200, 100, 100));
		rects.add(new ShadowRect(50, 300, 200, 25));
		//there's a problem if shapes overlap, so let's avoid it
		rects.add(new ShadowRect(50, 325.01f, 25, 149.98f));
		rects.add(new ShadowRect(50, 475, 200, 25));
		lightCircle = new Circle(200, 400, 30);
		bounds = new ShadowRect(10, 10, game.getWidth() - 20, game.getHeight() - 20);
		indicator = new Circle(0, 0, 10);
		shadowPoly = new ShadowPolygon(new float[] {600, 400, 700, 450, 625, 450, 550, 500} );
		shadowCircle = new ShadowCircle(400, 100, 50);
		lightPolygon = new Polygon();
		lightPoints = new ArrayList<Vector2f>();
		

		//array of all shadowable shapes
		allShapes = new ArrayList<ShadowShape>();
		allShapes.addAll(rects);
		allShapes.add(bounds);
		allShapes.add(shadowPoly);
		allShapes.add(shadowCircle);
	}

	
	@Override
	public void render(GameContainer game, Graphics g) throws SlickException {
		
		
		g.setColor(Color.lightGray);
		g.fill(lightPolygon);
		
		for (ShadowShape shape : allShapes) {
			if (shape == bounds) continue; //don't draw the bounds like the other shapes
			if (visibleShapes.contains(shape)) {
				g.setColor(Color.blue);
			} else {
				g.setColor(new Color(20, 20, 20));
			}
			g.fill((Shape)shape);
		}
		
		//sight limiter
		Circle lightRad = new Circle(lightCircle.getCenterX(), lightCircle.getCenterY(), 400);
		Shape[] unlit = bounds.subtract(lightRad);
		g.setColor(Color.black);
		for (Shape ul : unlit) {
			g.fill(ul);	
		}

		g.setColor(Color.lightGray);
		g.draw(bounds);
		
		//to debug verticies
//		for (int i = 0; i < lightPoints.size(); i++) {
//			Vector2f lp = lightPoints.get(i);
//			int r = i * 255 / lightPoints.size();
//			g.setColor(new Color(r, (i % 2) * 255 , 255 - r));
//			indicator.setCenterX(lp.x);
//			indicator.setCenterY(lp.y);
//			g.fill(indicator);
//		}

		g.setColor(Color.yellow);
		g.fill(lightCircle);
	}

	@Override
	public void update(GameContainer game, int dt) throws SlickException {
		//light is at the mouse
		lightCircle.setCenterX(game.getInput().getMouseX());
		lightCircle.setCenterY(game.getInput().getMouseY());
		final Vector2f light = new Vector2f(lightCircle.getCenterX() + 0.5f, lightCircle.getCenterY() + 0.5f);
		
		visibleShapes.clear();
		
		//keep a map of which vectors come from which shapes
		HashMap<Vector2f, ShadowShape> vertexMap = new HashMap<Vector2f, ShadowShape>();
		
		List<Vector2f> vertices = new ArrayList<Vector2f>();
		for (ShadowShape shape : allShapes) {
			for (Vector2f vertex : shape.getVertices(light)) {
				vertices.add(vertex);
				vertexMap.put(vertex, shape);
			}
			
		}

		//Sort the vertices in order of angle (so the light polygon has correctly
		//ordered vertices
		Collections.sort(vertices, new Comparator<Vector2f>() {
			@Override
			public int compare(Vector2f o1, Vector2f o2) {
				Vector2f dis1 = o1.copy().sub(light);
				Vector2f dis2 = o2.copy().sub(light);
				double t1 = dis1.getTheta();
				double t2 = dis2.getTheta();
				return (int) Math.signum(t1 - t2);
			}
		});

		Vector2f toVertex = new Vector2f();
		lightPoints = new ArrayList<Vector2f>();
		List<Integer> swappables = new ArrayList<Integer>();
		
		//for each vertex...
		for (Vector2f vert : vertices) {
			Vector2f blocking = null;
			ShadowShape blockingShape = null;
			ShadowShape myShape = vertexMap.get(vert);
			
			//create a vector from the light to the vertex
			toVertex.set(vert);
			toVertex.sub(light);
			
			//look through all the shapes
			for (ShadowShape shape : allShapes) {
				//get the closest intersection blocking this
				//vertex from the light (ignoring any thing containing this vertex)
				Vector2f intersect = shape.getIntersection(light, toVertex, vert);
				if (intersect == null) continue;
				//blocking is the closest intersection
				if (blocking == null || intersect.distance(light) < blocking.distance(light)) {
					blocking = intersect;
					blockingShape = shape;
				}
			}
			
			if (blocking == null) {
				//If nothing is blocking this vertex from the light, add it to the
				//light polygon
				lightPoints.add(vert);
			} else if (blocking.distance(light) > vert.distance(light)) {
				//If something is "blocking" it, but is farther away,
				//we cast a shadow, so add this vertex
				lightPoints.add(vert);
				//If I cast a shadow on another shape (and I'm not inside a shape)
				if (blockingShape != myShape && !myShape.contains(light)) {
					//add the shadow point
					lightPoints.add(blocking);
					vertexMap.put(blocking, blockingShape);
					//we may need to swap the order of these two verticies later
					swappables.add(lightPoints.size() - 2);
				}
			}
		}
		
		//list the visible shapes
		for (Vector2f vertex : lightPoints) {
			visibleShapes.add(vertexMap.get(vertex));
		}
		
		//try swapping the co-linear verticies to make the light polygon correct 
		for (int i = 0; i < 4; i++) {
			for (int s0 : swappables) {
				int s1 = s0 + 1;
				int before = (s0 - 1 + lightPoints.size()) % lightPoints.size();
				int after = (s1 + 1) % lightPoints.size();
				
				ShadowShape beforeShape = vertexMap.get(lightPoints.get(before)),
						afterShape = vertexMap.get(lightPoints.get(after)),
						s0Shape = vertexMap.get(lightPoints.get(s0)),
						s1Shape = vertexMap.get(lightPoints.get(s1));
				
				if (s0Shape == afterShape || s1Shape == beforeShape) { 
					Collections.swap(lightPoints, s0, s1);
				}
			}
		}
		
		
		float[] polys = new float[lightPoints.size() * 2];
		int index = 0;
		for (Vector2f lp : lightPoints) {
			polys[index++] = lp.x;
			polys[index++] = lp.y;
		}
		
		lightPolygon = new Polygon(polys);
		
	}
	
	/**
	 * Intersection between a ray and a line segment, or null if none exists.
	 * 
	 * @param start Start of the ray
	 * @param dir Direction of the ray
	 * @param p0 P0 of the line segment
	 * @param p1 P1 of the line segmnet
	 * @return
	 */
	public static Vector2f intersect(Vector2f start, Vector2f dir, Vector2f p0, Vector2f p1) {
		Vector2f p = start, r = dir, q = p0, s = p1.copy().sub(p0);
		float cross = cross(r, s);
		if (cross == 0) {
			return null;
		}
		Vector2f qmp = q.copy().sub(p);
		float u = cross(qmp, r) / cross;
		if (u < 0 || u > 1) return null;
		float t = cross(qmp, s) / cross;
		if (t < 0) return null;
		return s.scale(u).add(q);
	}
	
	/** Gives the magnitude of a cross product **/
	private static float cross(Vector2f a, Vector2f b) {
		return a.x * b.y - a.y * b.x;
	}
	
	public static void main(String[] args)
            throws SlickException {
         AppGameContainer app =
            new AppGameContainer(new Game());
  
         app.setDisplayMode(1000, 600, false);
         app.start();
    }
}
