package lifegame;
import java.util.ArrayList;

public class BoardModel {
	private int cols;
	private int rows;
	private int time;
	private int startTime;
	private int maxhistory;
	private boolean[][] cells;
	private boolean[][][] history;
	private boolean[][] nextstate;
	private ArrayList<BoardListener> listeners;
	private int previousX;
	private int previousY;
	
	public BoardModel(int c, int r) {
		cols = c;
		rows = r;
		time = 0;
		startTime = 0;
		maxhistory = 33;
		cells = new boolean[rows][cols];
		history = new boolean[maxhistory][rows][cols];
		nextstate = new boolean[rows][cols];
		listeners = new ArrayList<BoardListener>();

		for(int i = 0; i < cols; i++) {
			for(int j = 0; j < rows; j++) {
				history[0][j][i] = false;
			}
		}
	}
	
	public int getCols() {
		return cols;
	}
	public int getRows() {
		return rows;
	}
	public boolean getCells(int j,int i) {
		return cells[j][i];
	}

	public void setPattern(ArrayList<Integer> x,ArrayList<Integer> y) {
		for(int i = 0; i < cols; i++) {
			for(int j = 0; j < rows; j++) {
				cells[j][i] = false;
			}
		}
		for(int i = 0; i < x.size(); i++) {
			cells[x.get(i)][y.get(i)] = true;
		}
	}	
	public int getPreviousX() {
		return previousX;
	}
	public int getPreviousY() {
		return previousY;
	}
	public void changeCellState(int x, int y) {
		previousX = x;
		previousY = y;
		if(cells[x][y] == true) {
			cells[x][y] = false;
		}
		else if(cells[x][y] == false) {
			cells[x][y] = true;
		}
		fireUpdate();
		saveHistory();
	}

	private void fireUpdate() {
		for(BoardListener listener: listeners) {
			listener.updated(this);
		}
	}

	private boolean checkAlive(int i, int j) {
		int startI = 0,endI = 3;
		int startJ = 0,endJ = 3;
		int num = 0;

		if(i == 0) {
			startJ = 1;
		}
		else if(i == cols-1) {
			endJ = 2;
		}
		if(j == 0) {
			startI = 1;
		}
		else if(j == rows-1) {
			endI = 2;
		}

		for (int k = startI; k < endI; k++) {
			for (int l = startJ; l < endJ; l++) {
				if (cells[j+k-1][i+l-1] == true) {
					num++;
				}
			}
		}

		if (cells[j][i] == true) {
			num--;
		}

		if ((cells[j][i] == false && num == 3) || (cells[j][i] == true && (num == 2 || num == 3))) {
			return true;
		}
		return false;
	}

	public void saveHistory() {
		time++;
		startTime = 0;
		if(time >= maxhistory) {
			time = maxhistory-1;
			for(int i = 0; i < cols; i++) {
				for(int j = 0; j < rows; j++) {
					for(int k = 1; k < maxhistory; k++) {
						history[k-1][j][i] = history[k][j][i];
					}
				}
			}
		}

		for(int i = 0 ; i < cols; i++) {
			for(int j = 0; j < rows; j++) {
				history[time][j][i] = cells[j][i];
			}
		}
	}
	public void next() {
		for (int i = 0; i < cols; i++) {
			for (int j = 0; j < rows; j++) {
				nextstate[j][i] = checkAlive(i,j);
			}
		}

		saveHistory();

		for (int i = 0; i < cols; i++) {
			for (int j = 0; j < rows; j++) {
				cells[j][i] = nextstate[j][i];
			}
		}

	}

	public void undo() {
		time--;
		startTime++;
		for (int i = 0; i < cols; i++) {
			for (int j = 0; j < rows; j++) {
				cells[j][i] = history[time][j][i];
			}
		}
	}

	public boolean isUndorable() {
		if(startTime > 32 || time == 0) {
			return false;
		}
		return true;
	}
}