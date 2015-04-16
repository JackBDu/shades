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
	int height;
	int width;
	Board thisBoard = this;		// used for later reference
	MovableBlock movableBlock, nextMovableBlock;
	Block[][] blocks = new Block[this.numberOfColumns][this.numberOfRows];
	int[] numbersOfStacks = new int[this.numberOfColumns];
	public Board(int w, int h) {
		this.width = w;
		this.height = h;
		this.setBackground(Color.BLACK);
		KeyListener listener = new MyKeyListener();
		this.addKeyListener(listener);
		this.setFocusable(true);
		this.movableBlock = new MovableBlock();
		this.movableBlock.setVisible(true);
		this.nextMovableBlock = new MovableBlock();
		for (int c = 0; c < this.numberOfColumns; c++) {
			numbersOfStacks[c] = 0;
			for (int r = 0; r < this.numberOfRows; r++) {
				int x = c * this.movableBlock.width;
				int y = r * this.movableBlock.height;
				this.blocks[c][r] = new Block(x, y);
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
	private class MovableBlock {
		private int tempX;	// x coordinate of the block
		private int tempY;	// y coordinate of the block
		private int tempWidth;
		private int tempHeight;
		private int x;	// x coordinate of the block
		private int y;	// y coordinate of the block
		private int width = thisBoard.width /thisBoard.getNumberOfColumns();	// width of the block
		private int height = thisBoard.height/thisBoard.getNumberOfRows();
		private Color color = Color.RED;	// color of the block
		private boolean canDrop = false;
		private int transformTime = 100;
		private int transformTimer = transformTime;
		private boolean visible = false;

		// contructor that sets (0, 0) as default coordinates
		public MovableBlock() {
			System.out.println(thisBoard.height);
			Random rand = new Random();
			int n = rand.nextInt(thisBoard.numberOfColumns);
			this.x = this.width * n;
			this.y = - 9 * this.height / 10;
		}

		private void update() {
			if (this.transformTimer >= 0) {
				this.tempWidth = thisBoard.width - ((thisBoard.numberOfColumns - 1) * this.width - this.transformTimer * (thisBoard.numberOfColumns - 1) * this.width / this.transformTime);
				this.tempHeight = this.height;
				this.tempX = this.x - this.x * this.transformTimer / this.transformTime;
				this.tempY = this.y;
				this.transformTimer--;
			} else {
				this.canDrop = true;
				thisBoard.nextMovableBlock.setVisible(true);
			}
			if (this.canDrop) {
				this.drop();
				int column = this.x / this.width;
				if (this.y == thisBoard.height - this.height * (numbersOfStacks[column] + 1)) {
					this.canDrop = false;
					int row = this.y / this.height;
					thisBoard.blocks[column][row].setVisible(true);
					numbersOfStacks[column]++;
					thisBoard.movableBlock = thisBoard.nextMovableBlock;
					thisBoard.nextMovableBlock = new MovableBlock();
				}
			}
		}

		private void setVisible(boolean b) {
			this.visible = b;
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
			if (this.x < thisBoard.width - this.width) {
				this.x += this.width;
			}
		}
		
		// paint the block
		private void paint(Graphics2D g2d) {
			if (this.visible) {
				g2d.setColor(this.color);
				if (this.transformTimer >= 0) {
					g2d.fillRect(this.tempX, this.tempY, this.tempWidth, this.tempHeight);
				} else {
					g2d.fillRect(this.x, this.y, this.width, this.height);
				}
				if (debugging) {
					System.out.println("painting block (color: "+this.color+"; pos: ("+x+", "+y+"); size: "+this.width+", "+this.height+")");
				}
			}
		}

	}

	private class Block {
		private int x;	// x coordinate of the block
		private int y;	// y coordinate of the block
		private int width  = thisBoard.width /thisBoard.getNumberOfColumns();	// width of the block
		private int height = thisBoard.height/thisBoard.getNumberOfRows();		// height of the block
		private boolean visible;
		Color color = Color.RED;	// color of the block

		public Block() {
		}

		public Block(int x, int y) {
			this.visible = false;
			this.x = x;
			this.y = y;
		}

		private void setVisible(boolean b) {
			this.visible = b;
		}
		
		// paint the block
		private void paint(Graphics2D g2d) {
			if (this.visible) {
				int width, height, x, y;
				width = this.width;
				height = this.height;
				x = this.x;
				y = this.y;
				g2d.fillRect(x, y, width, height);
			}
		}

	}

	public class MyKeyListener implements KeyListener {
		public void keyPressed(KeyEvent e) {
			if (thisBoard.movableBlock.canDrop) {
				if (KeyEvent.getKeyText(e.getKeyCode()) == "Left") {
					thisBoard.movableBlock.moveLeft();
				} else if (KeyEvent.getKeyText(e.getKeyCode()) == "Right") {
					thisBoard.movableBlock.moveRight();
				}
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
		
		this.movableBlock.paint(g2d);

		for (int c = 0; c < this.numberOfColumns; c++) {
			for (int r = 0; r < this.numberOfRows; r++) {
				this.blocks[c][r].paint(g2d);
			}
		}
		this.nextMovableBlock.paint(g2d);
	}

	// update the status
	public void update() {
		this.movableBlock.update();
	}
	
	// main function for the board
	public static void main(String[] args) throws InterruptedException {
		JFrame frame = new JFrame("Shades");
		Board board = new Board(300, 480);

		// initialize the frame
		frame.add(board);
		frame.setSize(300, 480);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setResizable(false);

		// initialize the board
		// board.setSize(frame.getWidth(), frame.getHeight());
		board.setNumberOfRows(11);
		board.setNumberOfColumns(4);

		while (true) {
			board.update();
			board.repaint();
			Thread.sleep(5);
		}
	}

}
