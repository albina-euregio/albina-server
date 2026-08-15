// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.webauthn;

import eu.albina.util.EcKeys;

import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.RSAPublicKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Map;

/**
 * A COSE_Key (RFC 9053), decoded from CBOR, converted to a {@link PublicKey} the JDK's own
 * {@link java.security.Signature} can verify with — no third-party crypto library needed: EC and
 * RSA have always shipped in the JDK, and Ed25519/EdDSA has been native since JDK 15 (JEP 339).
 */
public record CoseKey(long kty, long alg, PublicKey publicKey) {

	private static final Map<Long, String> SIGNATURE_ALGORITHMS = Map.of(
		-7L, "SHA256withECDSA",  // ES256
		-35L, "SHA384withECDSA", // ES384
		-36L, "SHA512withECDSA", // ES512
		-257L, "SHA256withRSA",  // RS256
		-258L, "SHA384withRSA",  // RS384
		-259L, "SHA512withRSA",  // RS512
		-8L, "Ed25519"           // EdDSA
	);

	public boolean verifySignature(byte[] signedData, byte[] signature) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
		Signature verifier = Signature.getInstance(signatureAlgorithm());
		verifier.initVerify(publicKey());
		verifier.update(signedData);
		return verifier.verify(signature);
	}

	public String signatureAlgorithm() {
		String name = SIGNATURE_ALGORITHMS.get(alg);
		if (name == null) {
			throw new IllegalArgumentException("Unsupported COSE algorithm: " + alg);
		}
		return name;
	}

	public static CoseKey parse(Map<Object, Object> coseKey) {
		long kty = asLong(coseKey.get(1L));
		long alg = asLong(coseKey.get(3L));
		PublicKey publicKey = switch ((int) kty) {
			case 2 -> ecPublicKey(coseKey);  // EC2
			case 3 -> rsaPublicKey(coseKey); // RSA
			case 1 -> okpPublicKey(coseKey); // OKP (Ed25519/Ed448)
			default -> throw new IllegalArgumentException("Unsupported COSE key type: " + kty);
		};
		return new CoseKey(kty, alg, publicKey);
	}

	private static long asLong(Object value) {
		if (value instanceof Long l) return l;
		if (value instanceof Integer i) return i;
		throw new IllegalArgumentException("Expected a CBOR integer, got: " + value);
	}

	private static byte[] asBytes(Object value) {
		if (!(value instanceof byte[] bytes)) {
			throw new IllegalArgumentException("Expected a CBOR byte string, got: " + value);
		}
		return bytes;
	}

	private static PublicKey ecPublicKey(Map<Object, Object> coseKey) {
		long crv = asLong(coseKey.get(-1L));
		byte[] x = asBytes(coseKey.get(-2L));
		byte[] y = asBytes(coseKey.get(-3L));
		String curveName = switch ((int) crv) {
			case 1 -> "secp256r1"; // P-256
			case 2 -> "secp384r1"; // P-384
			case 3 -> "secp521r1"; // P-521
			default -> throw new IllegalArgumentException("Unsupported EC2 curve: " + crv);
		};
		return EcKeys.ecPublicKey(curveName, new BigInteger(1, x), new BigInteger(1, y));
	}

	private static PublicKey rsaPublicKey(Map<Object, Object> coseKey) {
		byte[] n = asBytes(coseKey.get(-1L));
		byte[] e = asBytes(coseKey.get(-2L));
		try {
			KeyFactory keyFactory = KeyFactory.getInstance("RSA");
			return keyFactory.generatePublic(new RSAPublicKeySpec(new BigInteger(1, n), new BigInteger(1, e)));
		} catch (GeneralSecurityException ex) {
			throw new IllegalArgumentException("Invalid RSA public key", ex);
		}
	}

	/**
	 * Ed25519's raw 32-byte public key, wrapped in the fixed X.509 SubjectPublicKeyInfo prefix
	 * (SEQUENCE { SEQUENCE { OID 1.3.101.112 } BIT STRING }) so the JDK's KeyFactory can import
	 * it without us having to reconstruct an {@code EdECPoint} by hand.
	 */
	private static final byte[] ED25519_X509_PREFIX = {
		0x30, 0x2a, 0x30, 0x05, 0x06, 0x03, 0x2b, 0x65, 0x70, 0x03, 0x21, 0x00
	};

	private static PublicKey okpPublicKey(Map<Object, Object> coseKey) {
		long crv = asLong(coseKey.get(-1L));
		if (crv != 6) { // 6 = Ed25519
			throw new IllegalArgumentException("Unsupported OKP curve: " + crv);
		}
		byte[] x = asBytes(coseKey.get(-2L));
		byte[] der = new byte[ED25519_X509_PREFIX.length + x.length];
		System.arraycopy(ED25519_X509_PREFIX, 0, der, 0, ED25519_X509_PREFIX.length);
		System.arraycopy(x, 0, der, ED25519_X509_PREFIX.length, x.length);
		try {
			KeyFactory keyFactory = KeyFactory.getInstance("Ed25519");
			return keyFactory.generatePublic(new X509EncodedKeySpec(der));
		} catch (GeneralSecurityException ex) {
			throw new IllegalArgumentException("Invalid Ed25519 public key", ex);
		}
	}
}
