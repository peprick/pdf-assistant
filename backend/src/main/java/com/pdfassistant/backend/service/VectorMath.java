package com.pdfassistant.backend.service;

public final class VectorMath {

	private VectorMath() {
	}

	public static double cosineSimilarity(double[] left, double[] right) {
		int length = Math.min(left.length, right.length);
		if (length == 0) {
			return 0.0;
		}

		double dot = 0.0;
		double leftNorm = 0.0;
		double rightNorm = 0.0;
		for (int i = 0; i < length; i++) {
			dot += left[i] * right[i];
			leftNorm += left[i] * left[i];
			rightNorm += right[i] * right[i];
		}
		if (leftNorm == 0.0 || rightNorm == 0.0) {
			return 0.0;
		}
		return dot / (Math.sqrt(leftNorm) * Math.sqrt(rightNorm));
	}
}
