
/*
 * Graph.java
 */

import javax.swing.*;

import java.awt.*;
import java.awt.geom.*;

import net.objecthunter.exp4j.*;

@SuppressWarnings("serial")
public class Graph extends JPanel {
	
	// x and y ranges of graph
	double xMin = -5;
	double xMax = 5;
	double yMin = -5;
	double yMax = 5;
	double zoomMult = 0.10; // Dimensions x (1+multiplier) for zoom out, x 1/(1+multiplier) for zoom in
	
	// Size of tick marks and grid lines
	// For now, they will always be controlled by the same input
	double xTickStep = 1;
	double yTickStep = 1;
	
	int numPoints = 10_000;
	double slopeChangeLimit = 50; // Detect when the graph has a jump and do not connect those points
	
	// Colors
	Color[] colorPalette = new Color[]{
			// https://docs.oracle.com/javase/7/docs/api/java/awt/Color.html
			// List of possible graph colors (light ones only)
			Color.RED,
			Color.CYAN, 
			Color.GREEN, 
			Color.MAGENTA,
			Color.ORANGE,
			Color.PINK,
			Color.YELLOW
	};
	Color gridColor = Color.LIGHT_GRAY;
	Color axesColor = Color.BLACK;
	Color invalidExprColor = Color.WHITE;
	int colorIndex = 0;
	
	public void paintComponent(Graphics g) {
		Graphics2D g2d = (Graphics2D) g;
		
		// Dimensions of just the graph, not the whole frame
		double graphW = Main.instance.width;
		double graphH = Main.instance.height;
		
		/*
		 * Draw grid lines
		 */
		g2d.setColor(gridColor);
		for(double xTick = Math.ceil(xMin / xTickStep); xTick <= xMax; xTick += xTickStep) {
			g2d.draw(new Line2D.Double(toWin(xTick, yMin).x, toWin(xTick, yMin).y, toWin(xTick, yMax).x, toWin(xTick, yMax).y));
		}
		for(double yTick = Math.ceil(yMin / yTickStep); yTick <= yMax; yTick += yTickStep) {
			g2d.draw(new Line2D.Double(toWin(xMin, yTick).x, toWin(xMin, yTick).y, toWin(xMax, yTick).x, toWin(xMax, yTick).y));
		}
		
		
		/*
		 * Function plotting loop
		 */
		Expression expr;
		ExprNode ptr = Main.instance.s.head;
		while(ptr != null) {
//			if(ptr.deleted == true) { // Doesn't seem necessary, leaving commented just in case
//				ptr = ptr.next;
//				continue;
//			}
			// Leave invalid expressions uncolored
			try {
				expr = new ExpressionBuilder(ptr.field.getText()).variables("x").build();
				if(ptr.field.getBackground().equals(invalidExprColor)) {
					ptr.field.setBackground(colorPalette[colorIndex % colorPalette.length]);
					colorIndex++;
				}
				else {
					ptr.field.setBackground(ptr.field.getBackground());
				}
				
				// Default interval at which f(x) is evaluated when graphing
				// Will be adjusted based on how steep the graph is, in order
				// to prevent the graph from appearing "dotted"
				double xStep = toGraph(graphW, graphH).x / numPoints; 
				
				g2d.setColor(ptr.field.getBackground());
				double x = xMin;
				double xPrev = x - xStep;
				double slopePrev = 0;
				
				while(x < xMax) {
					try {
						double winXPrev = toWin(xPrev, expr.setVariable("x", xPrev).evaluate()).x;
						double winYPrev = toWin(xPrev, expr.setVariable("x", xPrev).evaluate()).y;
						double winX = toWin(x, expr.setVariable("x", x).evaluate()).x;
						double winY = toWin(x, expr.setVariable("x", x).evaluate()).y;
						
						double slope = (winY - winYPrev) / (winX - winXPrev);
						
						if(Math.abs(slope - slopePrev) < slopeChangeLimit) {
							g2d.draw(new Line2D.Double(winXPrev, winYPrev, winX, winY));
						}
						
						slopePrev = slope;
					}
					
					// In case the x value is not in the domain, skip this value
					catch(ArithmeticException e) {}
					
					xPrev = x;
					x += xStep;
				}
			}
			catch(IllegalArgumentException e) { // Protect against nonsense inputs
				ptr.field.setBackground(invalidExprColor);
			}
			
			ptr = ptr.next;
		}
		
		/*
		 * Draw x and y axes
		 */
		g2d.setColor(axesColor);
		g2d.draw(new Line2D.Double(toWin(0, yMin).x, toWin(0, yMin).y, toWin(0, yMax).x, toWin(0, yMax).y));
		g2d.draw(new Line2D.Double(toWin(xMin, 0).x, toWin(xMin, 0).y, toWin(xMax, 0).x, toWin(xMax, 0).y));
		
	}
	
	
	/*
	 * Transform the x and y coordinates on the graph to the 
	 * x and y coordinates of the location of the point in the
	 * window.
	 * Remember to scale up the graph x and y to the window x y.
	 */
	public static Coord toWin(double x, double y) {
		// Dimensions of just the graph, not the whole frame
		double graphW = Main.instance.width;
		double graphH = Main.instance.height;
		
		// Set origin of axes
		double winOriginX = graphW / 2 - (Main.instance.g.xMin + Main.instance.g.xMax) / 2 * graphW / (Main.instance.g.xMax - Main.instance.g.xMin);
		double winOriginY = graphH / 2 + (Main.instance.g.yMin + Main.instance.g.yMax) / 2 * graphH / (Main.instance.g.yMax - Main.instance.g.yMin);
		
		// Calculate point location
		double winX = winOriginX + x * graphW / (Main.instance.g.xMax - Main.instance.g.xMin);
		double winY = winOriginY - y * graphH / (Main.instance.g.yMax - Main.instance.g.yMin);
		
		return new Coord(winX, winY);
	}
	
	
	/*
	 * Reverse the process of toWin.
	 */
	public static Coord toGraph(double winX, double winY) {
		// Dimensions of just the graph, not the whole frame
		double graphW = Main.instance.width;
		double graphH = Main.instance.height;
		
		// Set origin of axes
		double winOriginX = graphW / 2 - (Main.instance.g.xMin + Main.instance.g.xMax) / 2 * graphW / (Main.instance.g.xMax - Main.instance.g.xMin);
		double winOriginY = graphH / 2 + (Main.instance.g.yMin + Main.instance.g.yMax) / 2 * graphH / (Main.instance.g.yMax - Main.instance.g.yMin);
		
		// Calculate point location
		double x = (winX - winOriginX) * (Main.instance.g.xMax - Main.instance.g.xMin) / graphW;
		double y = (winY - winOriginY) * (Main.instance.g.yMax - Main.instance.g.yMin) / graphH;
		
		return new Coord(x, y);
		
	}
}