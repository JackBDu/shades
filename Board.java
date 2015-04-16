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
import java.util.*;

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
	Block[][] blocks = new Block[this.numberOfColumns][this.numberOfRows];
	public Board() {
		this.setBackground(Color.BLACK);
		KeyListener listener = new MyKeyListener();
		addKeyListener(listener);
		setFocusable(true);
		this.block = new Block();
		for (int c = 0; c < this.numberOfColumns; c++) {
			for (int r = 0; r < this.numberOfRows; r++) {
				this.blocks[c][r] = new Block();
			}
		}
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
		private boolean canDrop = false;
		private int transformTime = 100;
		private int transformTimer = transformTime;

		// contructor that sets (0, 0) as default coordinates
		public Block() {
			Random rand = new Random();
			int n = rand.nextInt(thisBoard.numberOfColumns);
			this.x = this.width * n;
			this.y = - 9 * this.height / 10;
		}

		public Block(int x, int y) {
			this.transformTimer = 0;
			this.x = x;
			this.y = y;
		}

		private void update() {
			if (this.canDrop) {
				this.drop();
				if (this.y == thisBoard.getHeight() - this.height) {
					this.canDrop = false;
					thisBoard.block = new Block();
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
			int width, height, x, y;
			g2d.setColor(this.color);
			if (this.transformTimer > 0) {
				width = thisBoard.getWidth() - ((thisBoard.numberOfColumns - 1) * this.width - this.transformTimer * (thisBoard.numberOfColumns - 1) * this.width / this.transformTime);
				height = this.height;
				x = this.x - this.x * this.transformTimer / this.transformTime;
				y = this.y;
				this.transformTimer--;
				System.out.println(this.transformTimer);
			} else {
				this.canDrop = true;
				width = this.width;
				height = this.height;
				x = this.x;
				y = this.y;
			}
			if (debugging) {
				System.out.println("painting block (color: "+this.color+"; pos: ("+x+", "+y+"); size: "+width+", "+height+")");
			}
			g2d.fillRect(x, y, width, height);
		}

	}

	public class MyKeyListener implements KeyListener {
		public void keyPressed(KeyEvent e) {
			if (KeyEvent.getKeyText(e.getKeyCode()) == "Left") {
				thisBoard.block.moveLeft();
			} else if (KeyEvent.getKeyText(e.getKeyCode()) == "Right") {
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
		Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		/*
		for (int c = 0; c < this.numberOfColumns; c++) {
			for (int r = 0; r < this.numberOfRows; r++) {
				this.blocks[c][r].paint(g2d);
			}
		}
		*/
		this.block.paint(g2d);
	}

	// update the status
	public void update() {
		this.block.update();
		for (int c = 0; c < this.numberOfColumns; c++) {
			for (int r = 0; r < this.numberOfRows; r++) {
				this.blocks[c][r].update();
			}
		}
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
			Thread.sleep(5);
		}
	}

}
