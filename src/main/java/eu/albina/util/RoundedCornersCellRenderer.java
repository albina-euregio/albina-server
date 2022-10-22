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

import com.itextpdf.kernel.colors.Color;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.layout.renderer.CellRenderer;
import com.itextpdf.layout.renderer.DrawContext;
import com.itextpdf.layout.renderer.IRenderer;

public class RoundedCornersCellRenderer extends CellRenderer {

	public static final Color greyDarkColor = new DeviceRgb(85, 95, 96);

	public RoundedCornersCellRenderer(Cell modelElement) {
		super(modelElement);
	}

	@Override
	public void drawBorder(DrawContext drawContext) {
		Rectangle occupiedAreaBBox = getOccupiedAreaBBox();
		Rectangle rectangle = applyMargins(occupiedAreaBBox, getMargins(), false);
		PdfCanvas canvas = drawContext.getCanvas();
		canvas.roundRectangle(rectangle.getX() - 5, rectangle.getY() + 2, rectangle.getWidth() + 10,
				rectangle.getHeight() - 4, 8).setColor(greyDarkColor, false).setLineWidth(0.5f).stroke();
		super.drawBorder(drawContext);
	}

	@Override
	public IRenderer getNextRenderer() {
		return new RoundedCornersCellRenderer((Cell) getModelElement());
	}

	@Override
	protected Rectangle applyMargins(Rectangle rect, UnitValue[] margins, boolean reverse) {
		return rect.applyMargins(margins[0].getValue(), margins[1].getValue(), margins[2].getValue(), margins[3].getValue(), reverse);
	}
}
