package tsdb.util.gui;

import java.awt.geom.Path2D;
import java.awt.geom.Path2D.Float;
import java.util.ArrayList;

import tsdb.util.gui.TimeSeriesDiagram.RawPoint;

//Derived from : https://en.wikipedia.org/wiki/Centripetal_Catmull%E2%80%93Rom_spline#Code_example_in_Unity_C#
public class Spline {


	public static ArrayList<RawPoint> catmulRom(RawPoint p0, RawPoint p1, RawPoint p2, RawPoint p3, int pointCount) {
		float t0 = 0.0f;
		float t1 = GetT(t0, p0, p1);
		float t2 = GetT(t1, p1, p2);
		float t3 = GetT(t2, p2, p3);

		ArrayList<RawPoint> newPoints = new ArrayList<RawPoint>(pointCount);
		for (float t = t1; t < t2; t += (t2-t1)/pointCount) {			
			RawPoint a1 = p0.scale((t1-t)/(t1-t0)).plus(p1.scale((t-t0)/(t1-t0)));
			RawPoint a2 = p1.scale((t2-t)/(t2-t1)).plus(p2.scale((t-t1)/(t2-t1)));
			RawPoint a3 = p2.scale((t3-t)/(t3-t2)).plus(p3.scale((t-t2)/(t3-t2)));

			RawPoint b1 = a1.scale((t2-t)/(t2-t0)).plus(a2.scale((t-t0)/(t2-t0)));
			RawPoint b2 = a2.scale((t3-t)/(t3-t1)).plus(a3.scale((t-t1)/(t3-t1)));

			RawPoint c = b1.scale((t2-t)/(t2-t1)).plus(b2.scale((t-t1)/(t2-t1)));

			newPoints.add(c);
		}
		return newPoints;
	}

	public static Float catmulRom(RawPoint p0, RawPoint p1, RawPoint p2, RawPoint p3) {
		float t0 = 0.0f;
		float t1 = GetT(t0, p0, p1);
		float t2 = GetT(t1, p1, p2);
		float t3 = GetT(t2, p2, p3);

		// derived from https://stackoverflow.com/questions/30748316/catmull-rom-interpolation-on-svg-paths#

		float c1 = (t2-t1)/(t2-t0);
		float c2 = (t1-t0)/(t2-t0);
		float d1 = (t3-t2)/(t3-t1);
		float d2 = (t2-t1)/(t3-t1);

		float m1x = (t2-t1)*(c1*(p1.x-p0.x)/(t1-t0) + c2*(p2.x-p1.x)/(t2-t1));
		float m1y = (t2-t1)*(c1*(p1.y-p0.y)/(t1-t0) + c2*(p2.y-p1.y)/(t2-t1));
		float m2x = (t2-t1)*(d1*(p2.x-p1.x)/(t2-t1) + d2*(p3.x-p2.x)/(t3-t2));
		float m2y = (t2-t1)*(d1*(p2.y-p1.y)/(t2-t1) + d2*(p3.y-p2.y)/(t3-t2));

		float q0x = p1.x;
		float q0y = p1.y;
		float q1x = p1.x + m1x / 3;
		float q1y = p1.y + m1y / 3;
		float q2x = p2.x - m2x / 3;
		float q2y = p2.y - m2y / 3;
		float q3x = p2.x;		
		float q3y = p2.y;
		
		Float path = new Path2D.Float();
		path.moveTo(q0x, q0y);
		path.curveTo(q1x, q1y, q2x, q2y, q3x, q3y);
		return path;
	}

	private static float GetT(float t, RawPoint p0, RawPoint p1) {
		// Parametric constant: 0.0 for the uniform spline, 0.5 for the centripetal spline, 1.0 for the chordal spline
		float alpha = 0.5f;

		float a = (float) (Math.pow((p1.x-p0.x), 2.0f) + Math.pow((p1.y-p0.y), 2.0f));
		float b = (float) Math.pow(a, alpha * 0.5f);

		return (b + t);
	}

}
