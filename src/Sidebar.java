
/*
 * Sidebar.java
 */

import javax.swing.*;

import java.awt.*;
import java.awt.event.*;

@SuppressWarnings("serial")
public class Sidebar extends JPanel {	
	ExprNode head; // see ExprNode.java for explanation of why we use LinkedList
	
	JButton settingsButton;
	JPanel settingsPanel;
	JButton zoomInButton;
	JButton zoomOutButton;
	
	int maxExprs = 20; 
	int fieldSize = 15; // in characters; interacts with width to put input field, "+", "-" on same line
	int width = 340;
	
	public Sidebar() {
		setLayout(new FlowLayout(FlowLayout.LEFT) {
			// https://stackoverflow.com/questions/29605734/how-to-set-the-maximum-width-of-a-flow-layouted-jpanel
			public Dimension preferredLayoutSize(Container target) {
                Dimension dim = super.preferredLayoutSize(target);
                dim.width = Math.min(width, dim.width);
                return dim;
            }
		}); 
		
		// Settings panel
		settingsButton = new JButton("Settings");
		settingsButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				
				// If you put these outside of the addActionListener() declaration, 
				// any invalid inputs will remain stored in the JTextFields (though
				// they won't be put into the variables themselves).
				Double.toString(Main.instance.g.xMax);
				settingsPanel = new JPanel();
				JLabel xMinInputLabel = new JLabel("Graph X Min");
				JTextField xMinInput = new JTextField(Double.toString(Main.instance.g.xMin));
				JLabel xMaxInputLabel = new JLabel("Graph X Max");
				JTextField xMaxInput = new JTextField(Double.toString(Main.instance.g.xMax));
				JLabel yMinInputLabel = new JLabel("Graph Y Min");
				JTextField yMinInput = new JTextField(Double.toString(Main.instance.g.yMin));
				JLabel yMaxInputLabel = new JLabel("Graph Y Max");
				JTextField yMaxInput = new JTextField(Double.toString(Main.instance.g.yMax));
//				JLabel tickStepInputLabel = new JLabel("Axis Tick Step");
//				JTextField tickStepInput = new JTextField(Double.toString(Main.instance.g.xTickStep));
				
				settingsPanel.add(xMinInputLabel);
				settingsPanel.add(xMinInput);
				settingsPanel.add(xMaxInputLabel);
				settingsPanel.add(xMaxInput);
				settingsPanel.add(yMinInputLabel);
				settingsPanel.add(yMinInput);
				settingsPanel.add(yMaxInputLabel);
				settingsPanel.add(yMaxInput);
//				settingsPanel.add(tickStepInputLabel);
//				settingsPanel.add(tickStepInput);
				
				JOptionPane.showMessageDialog(null, settingsPanel);
				double xMinNew, xMaxNew, yMinNew, yMaxNew; // , tickStepNew;
				
				try {
					xMinNew = Double.parseDouble(xMinInput.getText());
					xMaxNew = Double.parseDouble(xMaxInput.getText());
					yMinNew = Double.parseDouble(yMinInput.getText());
					yMaxNew = Double.parseDouble(yMaxInput.getText());
//					tickStepNew = Double.parseDouble(tickStepInput.getText());
					
					Main.instance.regenAll();
				}
				catch (NumberFormatException e) {
					JOptionPane.showMessageDialog(null, "Invalid settings. Your previous settings remain.");
					return;
				}
				
				// Use this version once tickStepInput works
//				if(!(xMaxNew > xMinNew && yMaxNew > yMinNew && tickStepNew > 0)) {
//					JOptionPane.showMessageDialog(null, "Invalid settings. Your previous settings remain.");
//					return;
//				}
				if(!(xMaxNew > xMinNew && yMaxNew > yMinNew)) {
					JOptionPane.showMessageDialog(null, "Invalid settings. Your previous settings remain.");
					return;
				}
				Main.instance.g.xMin = xMinNew;
				Main.instance.g.xMax = xMaxNew;
				Main.instance.g.yMin = yMinNew;
				Main.instance.g.yMax = yMaxNew;
//				Main.instance.g.xTickStep = tickStepNew;
//				Main.instance.g.yTickStep = tickStepNew;
				
				Main.instance.regenAll();
			}
		});
		
		zoomInButton = new JButton("Zoom +");
		zoomInButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// Zoom centered on current window center, NOT origin
				double graphCenterX = (Main.instance.g.xMin + Main.instance.g.xMax) / 2;
				double graphCenterY = (Main.instance.g.yMin + Main.instance.g.yMax) / 2;
				double graphW = Main.instance.g.xMax - Main.instance.g.xMin;
				double graphH = Main.instance.g.yMax - Main.instance.g.yMin;
				
				Main.instance.g.xMin = graphCenterX - graphW / 2 * (1 / (1 + Main.instance.g.zoomMult));
				Main.instance.g.xMax = graphCenterX + graphW / 2 * (1 / (1 + Main.instance.g.zoomMult));
				Main.instance.g.yMin = graphCenterY - graphH / 2 * (1 / (1 + Main.instance.g.zoomMult));
				Main.instance.g.yMax = graphCenterY + graphH / 2 * (1 / (1 + Main.instance.g.zoomMult));
				
				Main.instance.regenAll();
			}
		});
		
		zoomOutButton = new JButton("Zoom -");
		zoomOutButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// Zoom centered on current window center, NOT origin
				double graphCenterX = (Main.instance.g.xMin + Main.instance.g.xMax) / 2;
				double graphCenterY = (Main.instance.g.yMin + Main.instance.g.yMax) / 2;
				double graphW = Main.instance.g.xMax - Main.instance.g.xMin;
				double graphH = Main.instance.g.yMax - Main.instance.g.yMin;
				
				Main.instance.g.xMin = graphCenterX - graphW / 2 * (1 + Main.instance.g.zoomMult);
				Main.instance.g.xMax = graphCenterX + graphW / 2 * (1 + Main.instance.g.zoomMult);
				Main.instance.g.yMin = graphCenterY - graphH / 2 * (1 + Main.instance.g.zoomMult);
				Main.instance.g.yMax = graphCenterY + graphH / 2 * (1 + Main.instance.g.zoomMult);
				
				Main.instance.regenAll();
			}
		});
	}
	
	public void regen() {
		removeAll(); // Ensures sidebar will be updated when deleting inputs
		
		// Populate equation inputs
		ExprNode ptr = head;
		while(ptr != null) {
			add(ptr.field);
			add(ptr.addButton);
			add(ptr.delButton);
			add(ptr.tableButton);
			
			ptr = ptr.next;
		}
		
		add(settingsButton);
		add(zoomInButton);
		add(zoomOutButton);
		
//		// May not be necessary, but left just in case
//		revalidate();
//		repaint();
		
	}
	
}