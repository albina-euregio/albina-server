// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.model.enumerations;

import eu.albina.model.converter.EnumSetToStringConverter;

public enum GrainShape {
	PP, MM, DF, RG, FC, DH, SH, MF, IF, PPco, PPnd, PPpl, PPsd, PPir, PPgp, PPhl, PPip, PPrm, MMrp, MMci, DFdc, DFbk, RGsr, RGlr, RGwp, RGxf, FCso, FCsf, FCxr, DHcp, DHpr, DHch, DHla, DHxr, SHsu, SHcv, SHxr, MFcl, MFpc, MFsl, MFcr, IFil, IFic, IFbi, IFrc, IFsc;

	@jakarta.persistence.Converter
	public static class Converter extends EnumSetToStringConverter<GrainShape> {

		@Override
		protected Class<GrainShape> getEnumClass() {
			return GrainShape.class;
		}
	}
}
