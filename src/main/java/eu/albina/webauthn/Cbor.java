// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.webauthn;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Minimal CBOR (RFC 8949) decoder for the definite-length subset used by WebAuthn attestation
 * objects and COSE keys. Deliberately not a general-purpose CBOR library (no encoder, no
 * indefinite-length items, no BigNum/tag semantics) to avoid pulling in a third-party dependency
 * for what CTAP2 authenticators actually emit.
 *
 * <p>Decodes into plain JDK types: {@code Long} for integers, {@code byte[]} for byte strings,
 * {@code String} for text strings, {@code List<Object>} for arrays, {@code Map<Object, Object>}
 * for maps (COSE keys use integer labels as map keys).
 *
 * <p>Immutable: a {@code Cbor} value is just a cursor (buffer + read position) over the input;
 * every read returns a {@link Result} pairing the decoded value with the position right after it,
 * rather than mutating any state.
 */
public record Cbor(byte[] data, int pos) {

	public record Result(Object value, int endOffset) {
	}

	public static Object decode(byte[] data) {
		return decode(data, 0);
	}

	public static Object decode(byte[] data, int offset) {
		return decodeWithLength(data, offset).value();
	}

	/** Decodes a single CBOR item starting at {@code offset}, also reporting where it ended. */
	public static Result decodeWithLength(byte[] data, int offset) {
		return new Cbor(data, offset).readItem();
	}

	@SuppressWarnings("unchecked")
	public static Map<Object, Object> asMap(Object value) {
		if (!(value instanceof Map)) {
			throw new IllegalArgumentException(
				"Expected a CBOR map, got: " + (value == null ? "null" : value.getClass()));
		}
		return (Map<Object, Object>) value;
	}

	private int byteAt(int p) {
		if (p < 0 || p >= data.length) {
			throw new IllegalArgumentException("Unexpected end of CBOR data");
		}
		return data[p] & 0xff;
	}

	private byte[] slice(int from, int length) {
		if (length < 0 || from + length > data.length) {
			throw new IllegalArgumentException("Unexpected end of CBOR data");
		}
		byte[] result = new byte[length];
		System.arraycopy(data, from, result, 0, length);
		return result;
	}

	private record Unsigned(long value, int endOffset) {
	}

	private Unsigned readUnsigned(int p, int additionalInfo) {
		if (additionalInfo < 24) {
			return new Unsigned(additionalInfo, p);
		}
		int numBytes = switch (additionalInfo) {
			case 24 -> 1;
			case 25 -> 2;
			case 26 -> 4;
			case 27 -> 8;
			default -> throw new IllegalArgumentException("Unsupported CBOR additional info: " + additionalInfo);
		};
		long value = 0;
		int cur = p;
		for (int i = 0; i < numBytes; i++) {
			value = (value << 8) | byteAt(cur);
			cur++;
		}
		return new Unsigned(value, cur);
	}

	private Result readItem() {
		return readItemAt(pos);
	}

	private Result readItemAt(int p) {
		int initial = byteAt(p);
		int majorType = initial >> 5;
		int additionalInfo = initial & 0x1f;
		if (additionalInfo == 31) {
			throw new IllegalArgumentException("Indefinite-length CBOR items are not supported");
		}
		int afterHeader = p + 1;
		return switch (majorType) {
			case 0 -> {
				Unsigned u = readUnsigned(afterHeader, additionalInfo);
				yield new Result(u.value(), u.endOffset());
			}
			case 1 -> {
				Unsigned u = readUnsigned(afterHeader, additionalInfo);
				yield new Result(-1L - u.value(), u.endOffset());
			}
			case 2 -> {
				Unsigned u = readUnsigned(afterHeader, additionalInfo);
				int length = (int) u.value();
				yield new Result(slice(u.endOffset(), length), u.endOffset() + length);
			}
			case 3 -> {
				Unsigned u = readUnsigned(afterHeader, additionalInfo);
				int length = (int) u.value();
				String text = new String(slice(u.endOffset(), length), StandardCharsets.UTF_8);
				yield new Result(text, u.endOffset() + length);
			}
			case 4 -> {
				Unsigned u = readUnsigned(afterHeader, additionalInfo);
				List<Object> list = new ArrayList<>();
				int cur = u.endOffset();
				for (long i = 0; i < u.value(); i++) {
					Result item = readItemAt(cur);
					list.add(item.value());
					cur = item.endOffset();
				}
				yield new Result(list, cur);
			}
			case 5 -> {
				Unsigned u = readUnsigned(afterHeader, additionalInfo);
				Map<Object, Object> map = new LinkedHashMap<>();
				int cur = u.endOffset();
				for (long i = 0; i < u.value(); i++) {
					Result key = readItemAt(cur);
					Result value = readItemAt(key.endOffset());
					map.put(key.value(), value.value());
					cur = value.endOffset();
				}
				yield new Result(map, cur);
			}
			case 6 -> {
				// tag number, irrelevant to WebAuthn/COSE parsing: skip it, decode the tagged item
				Unsigned u = readUnsigned(afterHeader, additionalInfo);
				yield readItemAt(u.endOffset());
			}
			case 7 -> switch (additionalInfo) {
				case 20 -> new Result(Boolean.FALSE, afterHeader);
				case 21 -> new Result(Boolean.TRUE, afterHeader);
				case 22, 23 -> new Result(null, afterHeader);
				// simple values/floats: not needed by WebAuthn, but still consume their bytes
				default -> {
					Unsigned u = readUnsigned(afterHeader, additionalInfo);
					yield new Result(u.value(), u.endOffset());
				}
			};
			default -> throw new IllegalArgumentException("Unsupported CBOR major type: " + majorType);
		};
	}
}
