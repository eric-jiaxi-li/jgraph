/*
 * ExprNode.java
 * 
 * Store user-selected equations in LinkedList format.
 * Store the JTextField itself as well as the "Add below" 
 * and "Delete this equation" buttons.
 * 
 * Nodes are deleted based on the value of this.deleted
 * when the app is refreshed. 
 * 
 * Plotting function would be O(n) regardless of whether
 * a LinkedList or array is used, since we have to iterate
 * through all equations, so storing the data in this way
 * does not affect plotting performance
 */

import javax.swing.*;

import java.awt.*;
import java.awt.event.*;

import java.util.*;
import java.io.*;

import net.objecthunter.exp4j.*;

public class ExprNode {
	JTextField field;
	JButton addButton;
	JButton delButton;
	JButton tableButton;
	JPanel tablePanel;
	
	static int numExprs;
	
	ExprNode prev;
	ExprNode next;
	
	// Table variables
	double xMin = Integer.MIN_VALUE;
	double xMax = Integer.MAX_VALUE;
	int nPoints = 0;
	int nPointsDefault = 10;
	String defaultFileName = "table";
	String defaultFolderName = "tables";
	
	public ExprNode(ExprNode prevParam, JTextField fieldParam, ExprNode nextParam) {
		prev = prevParam;
		field = fieldParam;
		next = nextParam;
		
		/*
		 * When modifying this, make sure to modify the initial
		 * node adding in Main.instance.java main method!
		 */
		
		addButton = new JButton("+");
		addButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				addNode();
			}
		});
		
		delButton = new JButton("-");
		delButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				delNode();
			}
		});
		
		tableButton = new JButton("Table");
		tableButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				Main.instance.regenAll();
				
				// Make sure expression is legal
				try {
					new ExpressionBuilder(field.getText()).variables("x").build();
				}
				catch(IllegalArgumentException e) {
					JOptionPane.showMessageDialog(null, "Invalid expression.");
					return;
				}
				
				tablePanel = new JPanel();
				tablePanel.setLayout(new FlowLayout(FlowLayout.CENTER));
				
				// If table has never been opened, set it to the graph's xMin/xMax and have 10 points
				if(xMin == Integer.MIN_VALUE) {
					xMin = Main.instance.g.xMin;
					xMax = Main.instance.g.xMax;
					nPoints = nPointsDefault;
				}
				
				JLabel xMinInputLabel = new JLabel("Table X Min");
				JTextField xMinInput = new JTextField(Double.toString(xMin));
				JLabel xMaxInputLabel = new JLabel("Table X Max");
				JTextField xMaxInput = new JTextField(Double.toString(xMax));
				JLabel nPointsInputLabel = new JLabel("Number of points");
				JTextField nPointsInput = new JTextField(Integer.toString(nPoints));
				JButton download = new JButton("Download table");
				
				tablePanel.add(xMinInputLabel);
				tablePanel.add(xMinInput);
				tablePanel.add(xMaxInputLabel);
				tablePanel.add(xMaxInput);
				tablePanel.add(nPointsInputLabel);
				tablePanel.add(nPointsInput);
				tablePanel.add(download);
				
				download.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						// Check user's settings for validity
						double xMinNew, xMaxNew;
						int nPointsNew;
						try {
							xMinNew = Double.parseDouble(xMinInput.getText());
							xMaxNew = Double.parseDouble(xMaxInput.getText());
							nPointsNew = Integer.parseInt(nPointsInput.getText());
						}
						catch (NumberFormatException e2) {
							System.out.println("Number format exception");
							JOptionPane.showMessageDialog(null, "Invalid settings. Your previous settings remain.");
							return;
						}
						if(!(xMaxNew > xMinNew && nPointsNew > 0)) {
							System.out.println("Range error");
							JOptionPane.showMessageDialog(null, "Invalid settings. Your previous settings remain.");
							return;
						}
						xMin = xMinNew;
						xMax = xMaxNew;
						nPoints = nPointsNew;
						
						// File output
						double[][] table = genTable();
						
						try {
							// Get HashSet of files in output folder
							File[] fileList = new File(defaultFolderName).listFiles();
							String[] fileNameList = new String[fileList.length];
							for(int j = 0; j < fileList.length; j++) {
								fileNameList[j] = fileList[j].getName();
							}
							HashSet<String> fileNameSet = new HashSet<String>(Arrays.asList(fileNameList));
							
							// Determine output file name
							int fileNumber = 1;
							while(fileNameSet.contains(defaultFileName + fileNumber + ".csv")) {
								fileNumber++;
							}
							String fileName = defaultFolderName + "/" + defaultFileName + fileNumber + ".csv";
							
							FileWriter csv = new FileWriter(new File(fileName));
							csv.write(field.getText() + ", \n");
							csv.write("x, f(x), \n");
							for(int j = 0; j < nPoints; j++) {
								if(table[j][1] != Double.MIN_VALUE) {
									csv.write(table[j][0] + ", " + table[j][1] + ", \n");
								}
								else {
									csv.write(table[j][0] + ", " + "NA" + ", \n");
								}
							}
							
							csv.close();
							
							JOptionPane.showMessageDialog(null, "Output successfully generated at " + fileName + ".");
						} 
						catch(IOException e2) {
							// Auto-generated catch block
							e2.printStackTrace();
							return;
						}
					}
				});
				
				JOptionPane.showMessageDialog(null, tablePanel);
			}
		});
	}
	
	/*
	 * Using the already-validated inputs, generate and return the table
	 */
	public double[][] genTable() {
		Expression expr = new ExpressionBuilder(field.getText()).variables("x").build();
		double[][] table = new double[nPoints][2]; 
		
		double x = xMin;
		double xStep = (xMax - xMin) / nPoints;
		for(int j = 0; j < nPoints; j++) {
			table[j][0] = x;
			try {
				table[j][1] = expr.setVariable("x", x).evaluate();
			}
			catch(ArithmeticException e) {
				table[j][1] = Double.MIN_VALUE; // Mark as invalid
			}
			x += xStep;
		}
		
		return table;
	}
	
	public void addNode() {
		if(numExprs < Main.instance.s.maxExprs) {
			numExprs++;
			
			JTextField exprInput = new JTextField(Main.instance.s.fieldSize);
			exprInput.addKeyListener(new KeyAdapter() {
				public void keyPressed(KeyEvent e) {
					if(e.getKeyCode() == KeyEvent.VK_ENTER) {
						Main.instance.regenAll();
						exprInput.requestFocus();
					}
				}
			});
			ExprNode newExprNode = new ExprNode(this, exprInput, next);
			if(next != null) {
				next.prev = newExprNode;
			}
			next = newExprNode;
			
			Main.instance.regenAll();
			next.field.requestFocus();
		}
		else {
			JOptionPane.showMessageDialog(null, "Maximum number of expressions reached.");
		}
	}
	
	public void delNode() {
		if(numExprs >= 2) {
			numExprs--;
			
			if(prev == null) {
				Main.instance.s.head = Main.instance.s.head.next;
				Main.instance.s.head.prev = null;
				Main.instance.s.head.field.requestFocus(); 
			}
			else {
				if(prev != null) {
					prev.next = next;
				}
				if(next != null) {
					next.prev = prev;
				}
				prev.field.requestFocus();
			}
			
			Main.instance.regenAll();
		}
		else {
			JOptionPane.showMessageDialog(null, "Cannot delete the last expression.");
		}
	}
}