package lifegame;

import java.awt.Graphics;
import javax.swing.JPanel;

public class BoardView extends JPanel {
	
	private static final long serialVersionUID = 1L;
	private int cols, rows;
	private boolean cells[][];
	
	public BoardView(BoardModel model) {
		cols = model.getCols();
		rows = model.getRows();
		cells = new  boolean[rows][cols];
	}
	
	public void checkCellState(BoardModel model) {
		for(int i = 0; i < cols; i++) {
			for(int j = 0; j < rows; j++) {
				cells[j][i] = model.getCells(j,i);
			}
		}
	}

	public void paint(Graphics g) {
		super.paint(g); 
		
		double width = this.getWidth();
		double height = this.getHeight();
		double smallLength = Math.min(width,height);
		int cellLength = (int)smallLength/cols;
		
		for (int i = 1; i < cols+1; i++) {
			g.drawLine(cellLength*i, 0, cellLength*i, cellLength*cols);
		}
		for(int i = 1; i < rows+1; i++) {
			g.drawLine(0,cellLength*i, cellLength*rows,cellLength*i);
		}
		
		for(int i = 0; i < cols; i++) {
			for(int j = 0; j < rows; j++) {
				if(cells[j][i] == true) {
					g.fillRect(cellLength*j, cellLength*i, cellLength, cellLength);
				}
			}
		}
	}

}
