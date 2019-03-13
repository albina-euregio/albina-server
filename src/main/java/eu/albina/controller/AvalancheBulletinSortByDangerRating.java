package eu.albina.controller;

import java.util.Comparator;

import eu.albina.model.AvalancheBulletin;

public class AvalancheBulletinSortByDangerRating implements Comparator<AvalancheBulletin> {
	public int compare(AvalancheBulletin a, AvalancheBulletin b) {
		return b.getHighestDangerRatingDouble().compareTo(a.getHighestDangerRatingDouble());
	}
}