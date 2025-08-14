// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.model.enumerations;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import eu.albina.util.XMLResourceBundleControl;

public enum Aspect {
	N, NE, E, SE, S, SW, W, NW;

	public String toString(Locale locale) {
		return ResourceBundle.getBundle("i18n.Aspect", locale, new XMLResourceBundleControl())
				.getString(name());
	}

	public static Aspect fromString(String text) {
		if (text != null) {
			return Arrays.stream(Aspect.values()).filter(type -> text.equalsIgnoreCase(type.toString())).findFirst().orElse(null);
		}
		return null;
	}

	public String toCaamlString() {
		return "AspectRange_" + this.toLowerCaseString();
	}

	public String toString() {
		switch (this) {
		case N:
			return "N";
		case NE:
			return "NE";
		case E:
			return "E";
		case SE:
			return "SE";
		case S:
			return "S";
		case SW:
			return "SW";
		case W:
			return "W";
		case NW:
			return "NW";

		default:
			return null;
		}
	}

	public String toLowerCaseString() {
		switch (this) {
		case N:
			return "n";
		case NE:
			return "ne";
		case E:
			return "e";
		case SE:
			return "se";
		case S:
			return "s";
		case SW:
			return "sw";
		case W:
			return "w";
		case NW:
			return "nw";

		default:
			return null;
		}
	}

	public static String getSymbolPath(Set<Aspect> aspects, boolean grayscale) {
		if (aspects == null || aspects.isEmpty()) {
			return "aspects/color/empty.png";
		}
		int bitmask = aspects.stream().mapToInt(Aspect::bitmask).reduce(0b00000000, (a, b) -> a | b);
		if (grayscale)
			return "aspects/grey/" + Integer.valueOf(bitmask).toString() + ".png";
		else
			return "aspects/color/" + Integer.valueOf(bitmask).toString() + ".png";
	}

	private static int bitmask(Aspect aspect) {
		switch (aspect) {
			case N:
				return 0b10000000;
			case NE:
				return 0b01000000;
			case E:
				return 0b00100000;
			case SE:
				return 0b00010000;
			case S:
				return 0b00001000;
			case SW:
				return 0b00000100;
			case W:
				return 0b00000010;
			case NW:
				return 0b00000001;
			default:
				return 0;
		}
	}

	public static List<Aspect> sortAspects(Set<Aspect> aspects) {
		List<Aspect> order = List.of(Aspect.N, Aspect.NE, Aspect.E, Aspect.SE, Aspect.S, Aspect.SW, Aspect.W, Aspect.NW);
		List<Aspect> sorted = Stream.concat(order.stream(), order.stream())
			.dropWhile(aspects::contains)
			.dropWhile(Predicate.not(aspects::contains))
			.takeWhile(aspects::contains)
			.collect(Collectors.toList());
		if (sorted.size() < 4) {
			return sorted;
		}
		Aspect middleAspect = Stream.of(Aspect.N, Aspect.S, Aspect.W, Aspect.E).filter(sorted.subList(1, sorted.size() - 1)::contains).findFirst().orElseThrow();
		return List.of(sorted.get(0), middleAspect, sorted.get(sorted.size() - 1));
	}
}
