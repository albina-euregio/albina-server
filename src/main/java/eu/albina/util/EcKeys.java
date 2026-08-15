// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.util;

import java.math.BigInteger;
import java.security.AlgorithmParameters;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.ECParameterSpec;
import java.security.spec.ECPoint;
import java.security.spec.ECPublicKeySpec;
import java.util.Arrays;

/**
 * EC public-key plumbing shared between WebAuthn ({@link eu.albina.webauthn.CoseKey}) and Web
 * Push ({@code ch.rasc.webpush.CryptoService}): both need to turn raw elliptic-curve point
 * coordinates into a JDK {@link ECPublicKey} (and back), independent of any third-party crypto
 * library.
 */
public final class EcKeys {

	private EcKeys() {
	}

	public static byte[] concat(byte[]... arrays) {
		int totalLength = 0;
		for (byte[] array : arrays) {
			totalLength += array.length;
		}
		byte[] result = new byte[totalLength];
		int currentIndex = 0;
		for (byte[] array : arrays) {
			System.arraycopy(array, 0, result, currentIndex, array.length);
			currentIndex += array.length;
		}
		return result;
	}

	public static ECPublicKey ecPublicKey(String curveName, BigInteger x, BigInteger y) {
		try {
			AlgorithmParameters params = AlgorithmParameters.getInstance("EC");
			params.init(new ECGenParameterSpec(curveName));
			ECParameterSpec ecParameterSpec = params.getParameterSpec(ECParameterSpec.class);
			ECPoint point = new ECPoint(x, y);
			KeyFactory keyFactory = KeyFactory.getInstance("EC");
			return (ECPublicKey) keyFactory.generatePublic(new ECPublicKeySpec(point, ecParameterSpec));
		} catch (GeneralSecurityException e) {
			throw new IllegalArgumentException("Invalid EC public key", e);
		}
	}

	/**
	 * Parses a SEC1 uncompressed point ({@code 0x04 || X || Y}) for the given curve, e.g. the
	 * {@code p256dh} key of a Web Push subscription.
	 */
	public static ECPublicKey ecPublicKeyFromUncompressedPoint(String curveName, byte[] uncompressedPoint) {
		if (uncompressedPoint.length < 3 || uncompressedPoint[0] != 0x04 || (uncompressedPoint.length - 1) % 2 != 0) {
			throw new IllegalArgumentException("Not a SEC1 uncompressed EC point");
		}
		int coordSize = (uncompressedPoint.length - 1) / 2;
		byte[] x = Arrays.copyOfRange(uncompressedPoint, 1, 1 + coordSize);
		byte[] y = Arrays.copyOfRange(uncompressedPoint, 1 + coordSize, 1 + 2 * coordSize);
		return ecPublicKey(curveName, new BigInteger(1, x), new BigInteger(1, y));
	}

	/**
	 * Encodes an EC public key as a SEC1 uncompressed point ({@code 0x04 || X || Y}), each
	 * coordinate left-padded with zeroes to the curve's field size.
	 */
	public static byte[] uncompressedPoint(ECPublicKey publicKey) {
		int fieldSizeBytes = (publicKey.getParams().getCurve().getField().getFieldSize() + 7) / 8;
		byte[] result = new byte[1 + 2 * fieldSizeBytes];
		result[0] = 0x04;
		copyFixedLength(publicKey.getW().getAffineX(), result, 1, fieldSizeBytes);
		copyFixedLength(publicKey.getW().getAffineY(), result, 1 + fieldSizeBytes, fieldSizeBytes);
		return result;
	}

	private static void copyFixedLength(BigInteger value, byte[] dest, int offset, int length) {
		byte[] bytes = value.toByteArray();
		if (bytes.length >= length) {
			// drop a leading sign byte (or any leading zero padding), keep the low-order `length` bytes
			System.arraycopy(bytes, bytes.length - length, dest, offset, length);
		} else {
			System.arraycopy(bytes, 0, dest, offset + length - bytes.length, bytes.length);
		}
	}
}
