package org.avalanches.ais.controller;

import org.avalanches.ais.model.SnowProfile;

/**
 * This class holds all methods to classify a snow profile.
 * 
 * @author Norbert Lanzanasto
 *
 */
public class SnowProfileClassifier {

	public static int classify(SnowProfile profile) {
		rateLayers(profile);
		rateStabilityTest(profile);

		// rate temperature profile
		// rate ramm profile

		// TODO implement classify()
		// weight each factor

		return -1;
	}

	/**
	 * Rates the stability test.
	 * 
	 * @param profile
	 *            The snow profile to be rated.
	 * @return An integer between 0 and 100 rating the stability tests, -1 if no
	 *         tests are available.
	 */
	private static int rateStabilityTest(SnowProfile profile) {

		// TODO implement rateStabilityTest()

		return 0;
	}

	/**
	 * Rates the layering of the profile.
	 * 
	 * @param profile
	 *            The snow profile to be rated.
	 * @return An integer between 0 and 100 rating the layering, -1 if no tests
	 *         are available.
	 */
	private static int rateLayers(SnowProfile profile) {

		// TODO implement rateLayers()

		return 0;
	}
}
