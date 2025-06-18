package eu.albina.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import eu.albina.model.enumerations.Aspect;

public class AspectsTest {

	@Test
	void testSortAspects_EmptySet() {
		Set<Aspect> aspects = EnumSet.noneOf(Aspect.class);
		List<Aspect> sorted = Aspect.sortAspects(aspects);
		assertTrue(sorted.isEmpty());
	}

	@Test
	void testSortAspects_SingleAspect() {
		Set<Aspect> aspects = EnumSet.of(Aspect.N);
		List<Aspect> sorted = Aspect.sortAspects(aspects);
		assertEquals(List.of(Aspect.N), sorted);
	}

	@Test
	void testSortAspects_TwoAdjacentAspects() {
		Set<Aspect> aspects = EnumSet.of(Aspect.N, Aspect.NE);
		List<Aspect> sorted = Aspect.sortAspects(aspects);
		assertEquals(List.of(Aspect.N, Aspect.NE), sorted);
	}

	@Test
	void testSortAspects_ThreeAdjacentAspects() {
		Set<Aspect> aspects = EnumSet.of(Aspect.N, Aspect.NE, Aspect.E);
		List<Aspect> sorted = Aspect.sortAspects(aspects);
		assertEquals(List.of(Aspect.N, Aspect.NE, Aspect.E), sorted);
	}

	@Test
	void testSortAspects_ThreeAdjacentAspects2() {
		Set<Aspect> aspects = EnumSet.of(Aspect.NW, Aspect.N, Aspect.NE);
		List<Aspect> sorted = Aspect.sortAspects(aspects);
		assertEquals(List.of(Aspect.NW, Aspect.N, Aspect.NE), sorted);
	}

	@Test
	void testSortAspects_FourOrMoreAspects() {
		Set<Aspect> aspects = EnumSet.of(Aspect.N, Aspect.NE, Aspect.E, Aspect.SE);
		List<Aspect> sorted = Aspect.sortAspects(aspects);
		// Should return [N, E, SE] (middleAspect is E)
		assertEquals(List.of(Aspect.N, Aspect.E, Aspect.SE), sorted);
	}
}
