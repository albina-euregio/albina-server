/*******************************************************************************
 * Copyright (C) 2019 Norbert Lanzanasto
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package eu.albina.util;

import com.itextpdf.layout.border.Border;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.property.VerticalAlignment;
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
