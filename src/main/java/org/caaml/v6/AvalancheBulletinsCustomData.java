package org.caaml.v6;

public class AvalancheBulletinsCustomData {
	public final AvalancheBulletinsCustomData.ALBINA ALBINA;

	public AvalancheBulletinsCustomData(AvalancheBulletinsCustomData.ALBINA ALBINA) {
		this.ALBINA = ALBINA;
	}

	public static class ALBINA {
		public final String generalHeadline;

		public ALBINA(String generalHeadline) {
			this.generalHeadline = generalHeadline;
		}
	}
}
