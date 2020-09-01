package tsdb.util.gui;

import java.awt.geom.Path2D;
import java.util.Collection;
import java.util.List;

import tsdb.util.gui.TimeSeriesDiagram.RawPoint;

/*
Derived from: https://www.stkent.com/2015/07/03/building-smooth-paths-using-bezier-curves.html
The MIT License (MIT)
Copyright (c) 2015-2020 Stuart Kent
Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

public class PolyBezierPathUtil {

	/**
	 * Computes a Poly-Bezier curve passing through a given list of knots.
	 * The curve will be twice-differentiable everywhere and satisfy natural
	 * boundary conditions at both ends.
	 *
	 * @param knots a list of knots
	 * @return      a Path2D representing the twice-differentiable curve
	 *              passing through all the given knots
	 */
	public static void computePath2DThroughKnots(List<RawPoint> knots, Path2D path) {
		throwExceptionIfInputIsInvalid(knots);

		final RawPoint firstKnot = knots.get(0);
		path.moveTo(firstKnot.x, firstKnot.y);

		/*
		 * variable representing the number of Bezier curves we will join
		 * together
		 */
		final int n = knots.size() - 1;

		if (n == 1) {
			final RawPoint lastKnot = knots.get(1);
			path.lineTo(lastKnot.x, lastKnot.y);
		} else {
			final RawPoint[] controlPoints = computeControlPoints(n, knots);

			for (int i = 0; i < n; i++) {
				final RawPoint targetKnot = knots.get(i + 1);
				appendCurveToPath2D(path, controlPoints[i], controlPoints[n + i], targetKnot);
			}
		}
	}

	private static RawPoint[] computeControlPoints(int n, List<RawPoint> knots) {
		final RawPoint[] result = new RawPoint[2 * n];

		final RawPoint[] target = constructTargetVector(n, knots);
		final Float[] lowerDiag = constructLowerDiagonalVector(n - 1);
		final Float[] mainDiag = constructMainDiagonalVector(n);
		final Float[] upperDiag = constructUpperDiagonalVector(n - 1);

		final RawPoint[] newTarget = new RawPoint[n];
		final Float[] newUpperDiag = new Float[n - 1];

		// forward sweep for control points c_i,0:
		newUpperDiag[0] = upperDiag[0] / mainDiag[0];
		newTarget[0] = target[0].scale(1 / mainDiag[0]);

		for (int i = 1; i < n - 1; i++) {
			newUpperDiag[i] = upperDiag[i] / (mainDiag[i] - lowerDiag[i - 1] * newUpperDiag[i - 1]);
		}

		for (int i = 1; i < n; i++) {
			final float targetScale = 1 / (mainDiag[i] - lowerDiag[i - 1] * newUpperDiag[i - 1]);

			newTarget[i] = (target[i].minus(newTarget[i - 1].scale(lowerDiag[i - 1]))).scale(targetScale);
		}

		// backward sweep for control points c_i,0:
		result[n - 1] = newTarget[n - 1];

		for (int i = n - 2; i >= 0; i--) {
			result[i] = newTarget[i].minus(newUpperDiag[i], result[i + 1]);
		}

		// calculate remaining control points c_i,1 directly:
		for (int i = 0; i < n - 1; i++) {
			result[n + i] = knots.get(i + 1).scale(2).minus(result[i + 1]);
		}

		result[2 * n - 1] = knots.get(n).plus(result[n - 1]).scale(0.5f);

		return result;
	}

	private static RawPoint[] constructTargetVector(int n, List<RawPoint> knots) {
		final RawPoint[] result = new RawPoint[n];

		result[0] = knots.get(0).plus(2, knots.get(1));

		for (int i = 1; i < n - 1; i++) {
			result[i] = (knots.get(i).scale(2).plus(knots.get(i + 1))).scale(2);
		}

		result[result.length - 1] = knots.get(n - 1).scale(8).plus(knots.get(n));

		return result;
	}

	private static Float[] constructLowerDiagonalVector(int length) {
		final Float[] result = new Float[length];

		for (int i = 0; i < result.length - 1; i++) {
			result[i] = 1f;
		}

		result[result.length - 1] = 2f;

		return result;
	}

	private static Float[] constructMainDiagonalVector(int n) {
		final Float[] result = new Float[n];

		result[0] = 2f;

		for (int i = 1; i < result.length - 1; i++) {
			result[i] = 4f;
		}

		result[result.length - 1] = 7f;

		return result;
	}

	private static Float[] constructUpperDiagonalVector(int length) {
		final Float[] result = new Float[length];

		for (int i = 0; i < result.length; i++) {
			result[i] = 1f;
		}

		return result;
	}

	private static void appendCurveToPath2D(Path2D Path2D, RawPoint control1, RawPoint control2, RawPoint targetKnot) {
		Path2D.curveTo(control1.x, control1.y, control2.x, control2.y, targetKnot.x, targetKnot.y);
	}

	private static void throwExceptionIfInputIsInvalid(Collection<RawPoint> knots) {
		if (knots.size() < 2) {
			throw new IllegalArgumentException("Collection must contain at least two knots");
		}
	}
}