package eu.albina.controller;

import java.util.Comparator;

import eu.albina.model.AvalancheBulletin;

public class AvalancheBulletinSortByValidity implements Comparator<AvalancheBulletin> {
	public int compare(AvalancheBulletin a, AvalancheBulletin b) {
		return a.getValidFrom().compareTo(b.getValidFrom());
	}
}