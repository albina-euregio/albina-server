// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.webauthn;

import java.util.Arrays;

/**
 * Parsed {@code authenticatorData} (WebAuthn §6.1) — a fixed binary structure, not CBOR, except
 * for the embedded COSE_Key which is present only during registration (attested credential data).
 */
public record AuthenticatorData(
	byte[] rpIdHash,
	boolean userPresent,
	boolean userVerified,
	long signCount,
	byte[] aaguid,
	byte[] credentialId,
	CoseKey credentialPublicKey,
	/** Raw CBOR bytes of {@link #credentialPublicKey}, stored verbatim as the passkey's opaque key record. */
	byte[] credentialPublicKeyRaw
) {

	private static final int FLAG_USER_PRESENT = 0x01;
	private static final int FLAG_USER_VERIFIED = 0x04;
	private static final int FLAG_ATTESTED_CREDENTIAL_DATA = 0x40;

	public static AuthenticatorData parse(byte[] data) {
		if (data.length < 37) {
			throw new IllegalArgumentException("authenticatorData too short");
		}
		byte[] rpIdHash = Arrays.copyOfRange(data, 0, 32);
		int flags = data[32] & 0xff;
		long signCount = ((long) (data[33] & 0xff) << 24)
			| ((data[34] & 0xff) << 16)
			| ((data[35] & 0xff) << 8)
			| (data[36] & 0xff);

		if ((flags & FLAG_ATTESTED_CREDENTIAL_DATA) == 0) {
			return new AuthenticatorData(rpIdHash, (flags & FLAG_USER_PRESENT) != 0,
				(flags & FLAG_USER_VERIFIED) != 0, signCount, null, null, null, null);
		}

		int pos = 37;
		byte[] aaguid = Arrays.copyOfRange(data, pos, pos + 16);
		pos += 16;
		int credentialIdLength = ((data[pos] & 0xff) << 8) | (data[pos + 1] & 0xff);
		pos += 2;
		byte[] credentialId = Arrays.copyOfRange(data, pos, pos + credentialIdLength);
		pos += credentialIdLength;

		Cbor.Result coseKeyItem = Cbor.decodeWithLength(data, pos);
		byte[] credentialPublicKeyRaw = Arrays.copyOfRange(data, pos, coseKeyItem.endOffset());
		CoseKey credentialPublicKey = CoseKey.parse(Cbor.asMap(coseKeyItem.value()));
		// any bytes after the COSE key (extensions) are intentionally ignored: we request none

		return new AuthenticatorData(rpIdHash, (flags & FLAG_USER_PRESENT) != 0,
			(flags & FLAG_USER_VERIFIED) != 0, signCount, aaguid, credentialId,
			credentialPublicKey, credentialPublicKeyRaw);
	}
}
