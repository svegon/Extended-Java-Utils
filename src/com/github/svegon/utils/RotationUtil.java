/*
 * Copyright (c) 2021-2021 Svegon and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package com.github.svegon.utils;

import com.github.svegon.utils.math.MathUtil;
import com.github.svegon.utils.vector.VectorUtil;

public final class RotationUtil {
	private RotationUtil() {
		throw new AssertionError();
	}

	public static double[] getNeededRotations(double[] viewer, double[] vec) {
		double unaccountedVectorLength = VectorUtil.distance(viewer, vec);
		final int end = viewer.length - 1;
		final double[] result = new double[end];

		for (int i = 0; i < end; i++) {
			double diff = viewer[i] - vec[i];
			result[i] = Math.asin(diff / unaccountedVectorLength);
			unaccountedVectorLength = Math.sqrt(MathUtil.squared(unaccountedVectorLength) - MathUtil.squared(diff));
		}

		return result;
	}
}
