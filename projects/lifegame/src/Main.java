package lifegame;

import javax.swing.SwingUtilities;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.BorderLayout;

public class Main implements Runnable{
	private int rows = 32;
	private int cols = 32;

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Main());
	}

	public void run(){
		JFrame dialog = new JFrame();
		
		while(true) {
			try {
				String input = JOptionPane.showInputDialog(this,"セル数を入力(10以上)");
				if (input == null) {
					System.exit(0);
				}
				cols = Integer.parseInt(input);
				rows = cols;
				if(cols >= 10) {
					break;
				}
				JOptionPane.showMessageDialog(dialog, "9より小さい.再度入力してください.");
			}
			catch(NumberFormatException e){
				JOptionPane.showMessageDialog(dialog, "整数値を入力してください.");
			}
		}

		final BoardModel model = new BoardModel(cols, rows);

		JFrame frame = new JFrame("Lifegame");
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		JPanel base = new JPanel();
		frame.setContentPane(base);
		base.setPreferredSize(new Dimension(500, 500));
		frame.setMinimumSize(new Dimension(500, 500));

		base.setLayout(new BorderLayout());
		final BoardView view = new BoardView(model);
		base.add(view, BorderLayout.CENTER);


		JPanel buttonPanel = new JPanel();
		base.add(buttonPanel, BorderLayout.SOUTH);
		buttonPanel.setLayout(new FlowLayout());
		
		final JButton buttonNew = new JButton("New Game");
		final JButton buttonUndo = new JButton("undo");
		final JButton buttonNext = new JButton("next");
		buttonPanel.add(buttonNew);
		buttonPanel.add(buttonUndo);
		buttonPanel.add(buttonNext);
		
		view.checkCellState(model);
		buttonUndo.setEnabled(model.isUndorable());

		base.addMouseListener(new MouseListener(){
			public void mouseClicked(MouseEvent e) {}
			
			public void mousePressed(MouseEvent e) {
				double width = view.getWidth();
				double height = view.getHeight();
				double minLength = Math.min(width,height);
				int fillLength = (int)minLength/cols;
				int i = 0,j = 0;
				
				while(!(fillLength*i < e.getX() && e.getX() <fillLength*(i+1)) && i < cols+1) {
					i++;
				}
				while(!(fillLength*j < e.getY() && e.getY() <fillLength*(j+1)) && j < rows+1) {
					j++;
				}

				if(!(i == cols || j == rows)) {
					model.changeCellState(i, j);
					view.checkCellState(model);
					view.repaint();
					buttonUndo.setEnabled(model.isUndorable());
				}
			}

			
			public void mouseReleased(MouseEvent e) {}

			
			public void mouseEntered(MouseEvent e) {}

			
			public void mouseExited(MouseEvent e) {}
		});

		base.addMouseMotionListener(new MouseMotionListener() {
			
			public void mouseDragged(MouseEvent e) {
				double width = view.getWidth();
				double height = view.getHeight();
				double minLength = Math.min(width,height);
				int fillLength = (int)minLength/cols;
				int i = 0,j = 0;
				
				while(!(fillLength*i < e.getX() && e.getX() <fillLength*(i+1)) && i < cols) {
					i++;
				}
				while(!(fillLength*j < e.getY() && e.getY() <fillLength*(j+1)) && j < rows) {
					j++;
				}

				if((model.getPreviousY() != j || model.getPreviousX() != i )&& (!(i == cols || j == rows))) {
					model.changeCellState(i, j);
					view.checkCellState(model);
					view.repaint();
				}
			}

			
			public void mouseMoved(MouseEvent e) {}

		});

		class ButtonActionListener implements ActionListener {
			public void actionPerformed(ActionEvent e) {
				if(e.getSource() == buttonNext) {
					model.next();
				}
				else if(e.getSource() == buttonUndo) {
					model.undo();
				}
				else if(e.getSource() == buttonNew) {
					run();
				}
				view.checkCellState(model);
				view.repaint();
				buttonUndo.setEnabled(model.isUndorable());
			}
		}
		buttonNext.addActionListener(new ButtonActionListener());
		buttonUndo.addActionListener(new ButtonActionListener());
		buttonNew.addActionListener(new ButtonActionListener());

		frame.pack(); 
		frame.setVisible(true);
	}
}
