// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.util;

import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.properties.VerticalAlignment;
import com.itextpdf.layout.renderer.IRenderer;

public class RoundedCornersCell extends Cell {
	public RoundedCornersCell() {
		super();
		setBorder(Border.NO_BORDER);
		setMargin(0);
	}

	public RoundedCornersCell(int rowspan, int colspan) {
		super(rowspan, colspan);
		setBorder(Border.NO_BORDER);
		setVerticalAlignment(VerticalAlignment.MIDDLE);
		setMarginLeft(20);
	}

	@Override
	protected IRenderer makeNewRenderer() {
		return new RoundedCornersCellRenderer(this);
	}
}
