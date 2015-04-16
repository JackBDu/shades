/*
 * Board.java
 *
 * Created on April 14, 2015
 *
 * Copyright(c) {2015} Jack B. Du (Jiadong Du) All Rights Reserved.
 *
 */

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;

/*
 * @ version 0.0.1
 * @ author Jack B. Du (Jiadong Du)
 */

public class Board extends JPanel {
	boolean debugging = true;	// for debugging
	int numberOfColumns = 4;	// number of columns of blocks
	int numberOfRows    = 11;	// number of rows of blocks
	Board thisBoard = this;		// used for later reference
	Block block;
	public Board() {
		KeyListener listener = new MyKeyListener();
		addKeyListener(listener);
		setFocusable(true);
	}
	
	// return the number of columns of blocks 
	private int getNumberOfColumns() {
		if (debugging) {
			System.out.println("returning numberOfColumns");
		}
		return numberOfColumns;
	}

	// return the number of rows of blocks 
	private int getNumberOfRows() {
		if (debugging) {
			System.out.println("returning numberOfRows");
		}
		return numberOfRows;
	}

	// set the number of rows of blocks
	private void setNumberOfRows(int n) {
		if (debugging) {
			System.out.println("setting numberOfRows");
		}
		this.numberOfRows = n;
	}

	// set the number of columns of blocks
	private void setNumberOfColumns(int n) {
		if (debugging) {
			System.out.println("setting numberOfColumns");
		}
		this.numberOfColumns = n;
	}

	// the block class
	private class Block {
		private int x;	// x coordinate of the block
		private int y;	// y coordinate of the block
		private int width  = thisBoard.getWidth() /thisBoard.getNumberOfColumns();	// width of the block
		private int height = thisBoard.getHeight()/thisBoard.getNumberOfRows();		// height of the block
		Color color = Color.RED;	// color of the block
		private boolean canDrop = true;

		// contructor that sets (0, 0) as default coordinates
		public Block() {
			this.x = 0;
			this.y = 0;
		}

		// constructor that sets specified coordinates
		public Block(int x, int y) {
			this.x = x;
			this.y = y;
		}

		private void update() {
			if (this.canDrop) {
				this.drop();
				if (this.y == thisBoard.getHeight() - this.height) {
					this.canDrop = false;
					System.out.println("Stop!");
				}
			}
		}


		private void drop() {
			this.y++;
		}

		private void moveLeft() {
			if (debugging) {
				System.out.println("block moving left");
			}
			if (this.x > 0) {
				this.x -= this.width;
			}
		}

		private void moveRight() {
			if (debugging) {
				System.out.println("block moving right");
			}
			if (this.x < thisBoard.getWidth() - this.width) {
				this.x += this.width;
			}
		}
		
		// paint the block
		private void paint(Graphics2D g2d) {
			if (debugging) {
				System.out.println("painting block (color: "+this.color+"; pos: ("+this.x+", "+this.y+"); size: "+this.width+", "+this.height+")");
			}
			g2d.setColor(this.color);
			g2d.fillRect(this.x, this.y, this.width, this.height);
		}

	}

	public class MyKeyListener implements KeyListener {
		public void keyPressed(KeyEvent e) {
			if (KeyEvent.getKeyText(e.getKeyCode()) == "Left") {
				thisBoard.block.moveLeft();
			}
			if (KeyEvent.getKeyText(e.getKeyCode()) == "Right") {
				thisBoard.block.moveRight();
			}
		}
		@Override
		public void keyTyped(KeyEvent e) {
		}

		public void keyReleased(KeyEvent e) {

		}
	}


	// paint the whole board
	@Override
	public void paint(Graphics g) {
		if (debugging) {
			System.out.println("painting");
		}
		super.paint(g);
		this.setBackground(Color.BLACK);
		Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		this.block.paint(g2d);
	}

	// update the status
	public void update() {
		if (debugging) {
			this.block = new Block(0, 0);
			debugging = false;
		}
		this.block.update();
	}
	
	// main function for the board
	public static void main(String[] args) throws InterruptedException {
		JFrame frame = new JFrame("Shades");
		Board board = new Board();

		// initialize the frame
		frame.add(board);
		frame.setSize(300, 480);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setResizable(false);

		// initialize the board
		//board.setSize(frame.getWidth(), frame.getHeight());
		board.setNumberOfRows(11);
		board.setNumberOfColumns(4);

		while (true) {
			board.update();
			board.repaint();
			Thread.sleep(10);
		}
	}

}
