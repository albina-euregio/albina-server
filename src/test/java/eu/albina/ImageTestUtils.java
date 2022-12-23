package eu.albina;

import java.awt.Color;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public interface ImageTestUtils {

	/**
	 * Compares the reference image file with the actual images given as {@link BufferedImage}.
	 *
	 * @param message                 message for assertions
	 * @param reference               the reference image
	 * @param image                   the actual image
	 * @param thresholdPixels         maximum number of differing pixels
	 * @param thresholdTotalColorDiff maximum sum of color value differences
	 * @param diffImageConsumer       a consumer for a rendered image highlighting the differing pixels, may be null
	 */
	static void assertImageEquals(
		String message,
		BufferedImage reference, BufferedImage image,
		int thresholdPixels, int thresholdTotalColorDiff, Consumer<BufferedImage> diffImageConsumer) {

		// Adapted from ...
		// Source: https://github.com/openstreetmap/josm/blob/master/test/functional/org/openstreetmap/josm/gui/mappaint/MapCSSRendererTest.java
		// License: GNU General Public License v2 or later

		assertEquals(image.getWidth(), reference.getWidth(), String.format("Images %s width", message));
		assertEquals(image.getHeight(), reference.getHeight(), String.format("Images %s height", message));

		StringBuilder differences = new StringBuilder();
		ArrayList<Point> differencePoints = new ArrayList<>();
		int colorDiffSum = 0;

		for (int y = 0; y < reference.getHeight(); y++) {
			for (int x = 0; x < reference.getWidth(); x++) {
				int expected = reference.getRGB(x, y);
				int result = image.getRGB(x, y);
				int expectedAlpha = expected >> 24;
				boolean colorsAreSame = expectedAlpha == 0 ? result >> 24 == 0 : expected == result;
				if (!colorsAreSame) {
					Color expectedColor = new Color(expected, true);
					Color resultColor = new Color(result, true);
					int colorDiff = Math.abs(expectedColor.getRed() - resultColor.getRed())
						+ Math.abs(expectedColor.getGreen() - resultColor.getGreen())
						+ Math.abs(expectedColor.getBlue() - resultColor.getBlue());
					int alphaDiff = Math.abs(expectedColor.getAlpha() - resultColor.getAlpha());
					// Ignore small alpha differences due to Java versions, rendering libraries and so on
					if (alphaDiff <= 20) {
						alphaDiff = 0;
					}
					// Ignore small color differences for the same reasons, but also completely for almost-transparent pixels
					if (colorDiff <= 15 || resultColor.getAlpha() <= 20) {
						colorDiff = 0;
					}
					if (colorDiff + alphaDiff > 0) {
						differencePoints.add(new Point(x, y));
						if (differences.length() < 2000) {
							differences.append("\nDifference at ")
								.append(x)
								.append(",")
								.append(y)
								.append(": Expected ")
								.append(expectedColor)
								.append(" but got ")
								.append(resultColor)
								.append(" (color diff is ")
								.append(colorDiff)
								.append(", alpha diff is ")
								.append(alphaDiff)
								.append(")");
						}
					}
					colorDiffSum += colorDiff + alphaDiff;
				}
			}
		}

		if (differencePoints.size() <= thresholdPixels && colorDiffSum <= thresholdTotalColorDiff) {
			return;
		}

		// Add a nice image that highlights the differences:
		BufferedImage diffImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
		for (Point p : differencePoints) {
			diffImage.setRGB(p.x, p.y, 0xffff0000);
		}
		if (diffImageConsumer != null) {
			diffImageConsumer.accept(diffImage);
		}

		if (differencePoints.size() > thresholdPixels) {
			fail(String.format("Images %s differ at %d points, threshold is %d: %s", message,
				differencePoints.size(), thresholdPixels, differences));
		} else {
			fail(String.format("Images %s differ too much in color, value is %d, permitted threshold is %d: %s", message,
				colorDiffSum, thresholdTotalColorDiff, differences));
		}
	}
}
