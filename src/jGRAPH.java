
/*
 * jGRAPH.java
 * 
 * Main window (JFrame) for jGRAPH application
 */

import javax.swing.*;

import java.awt.*;
import java.awt.event.*;

@SuppressWarnings("serial")
public class jGRAPH extends JFrame {	
	// Dimensions of the window
	int width = 700;
	int height = 700;
	
	Sidebar s;
	Graph g;
	
	public jGRAPH() {
		
		s = new Sidebar();
		g = new Graph();
		
		setTitle("Catboy's jGRAPH");
		setLayout(new BorderLayout());
		g.setPreferredSize(new Dimension(width, height));
		
		/*
		 * Expression input
		 */
		
		/*
		 * When modifying this, make sure to modify the buttons
		 * in ExprNode.java!
		 */
		JTextField exprInput = new JTextField(s.fieldSize);
		exprInput.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				if(e.getKeyCode() == KeyEvent.VK_ENTER) {
					regenAll();
					exprInput.requestFocus();
				}
			}
		});
		s.head = new ExprNode(null, exprInput, null);
		ExprNode.numExprs = 1;
		
		add(g, BorderLayout.CENTER);
		add(s, BorderLayout.WEST);
		regenS(); // Without this, sidebar doesn't show
		
		pack(); // Make the JFrame encompass the graph's dimensions
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setResizable(false);
		setVisible(true);
	}
	
	public void regenAll() {
		regenS();
		regenG();
	}
	
	public void regenS() {
		s.regen();
		
		// May not be necessary, but included just in case
		revalidate(); 
		repaint();
	}
	
	public void regenG() {
		g.repaint();
		
		// May not be necessary, but included just in case
		revalidate();
		repaint();
	}

}