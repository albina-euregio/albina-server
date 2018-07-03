package eu.albina.util;

import com.itextpdf.kernel.color.Color;
import com.itextpdf.kernel.color.DeviceRgb;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.layout.element.Cell;
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
		float[] margins = getMargins();
		Rectangle rectangle = applyMargins(occupiedAreaBBox, margins, false);
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
	protected Rectangle applyMargins(Rectangle rect, float[] margins, boolean reverse) {
		return rect.<Rectangle>applyMargins(margins[0], margins[1], margins[2], margins[3], reverse);
	}
}