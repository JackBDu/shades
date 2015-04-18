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
	boolean 			debugging		= true;	// for 
	boolean				isDisappearing	= false;
	boolean				isPaused		= false;
	int					levelSleepTime	= 5;
	int 				sleepTime		= levelSleepTime;
	int 				numberOfColumns	= 4;	// number of columns of blocks
	int 				numberOfRows	= 11;	// number of rows of blocks
	Board				thisBoard		= this;		// used for later reference
	Info				info;
	Block[][]			blocks;
	DroppableBlock[][]	droppableBlocks;
	int[]				numbersOfStacks;
	int					height, width;
	MovableBlock		movableBlock, nextMovableBlock;
	
	public Board(int w, int h) {
		KeyListener listener = new MyKeyListener();
		this.addKeyListener(listener);
		this.width	= w;
		this.height	= h;
		this.setBackground(new Color(150, 150, 50));
		this.setFocusable(true);
		this.reset();
	}

	private void reset() {
		this.info				= new Info();
		this.blocks				= new Block[this.numberOfColumns][this.numberOfRows];
		this.droppableBlocks 	= new DroppableBlock[this.numberOfColumns][this.numberOfRows-1];
		this.numbersOfStacks 	= new int[this.numberOfColumns];
		this.movableBlock		= new MovableBlock();
		this.nextMovableBlock	= new MovableBlock();
		this.movableBlock.setVisible(true);
		for (int c = 0; c < this.numberOfColumns; c++) {
			this.numbersOfStacks[c] = 0;
			for (int r = 0; r < this.numberOfRows; r++) {
				int x = c * this.movableBlock.width;
				int y = r * this.movableBlock.height + this.height % this.numberOfRows;
				this.blocks[c][r] = new Block(x, y);
				if (r < this.numberOfRows - 1) {
					this.droppableBlocks[c][r] = new DroppableBlock(x, y);
				}
			}
		}
	}
	
	// return the number of columns of blocks 
	private int getNumberOfColumns() {
		return numberOfColumns;
	}

	// return the number of rows of blocks 
	private int getNumberOfRows() {
		return numberOfRows;
	}

	// set the number of rows of blocks
	private void setNumberOfRows(int n) {
		this.numberOfRows = n;
		this.reset();
	}

	// set the number of columns of blocks
	private void setNumberOfColumns(int n) {
		this.numberOfColumns = n;
		this.reset();
	}

	public class MyKeyListener implements KeyListener {
		public void keyPressed(KeyEvent e) {
			if (thisBoard.movableBlock.canDrop && !thisBoard.movableBlock.canMerge) {
				if (KeyEvent.getKeyText(e.getKeyCode()) == "Left") {
					thisBoard.movableBlock.moveLeft();
				} else if (KeyEvent.getKeyText(e.getKeyCode()) == "Right") {
					thisBoard.movableBlock.moveRight();
				}
			}
			if (KeyEvent.getKeyText(e.getKeyCode()) == "Space") {
				thisBoard.isPaused = !thisBoard.isPaused;
				if (debugging) {
					System.out.println(KeyEvent.getKeyText(e.getKeyCode())+" pressed");
					System.out.println(thisBoard.isPaused);
				}
			} else if (KeyEvent.getKeyText(e.getKeyCode()) == "Down") {
				thisBoard.sleepTime = 1;
			}
		}
		@Override
		public void keyTyped(KeyEvent e) {
		}

		public void keyReleased(KeyEvent e) {

		}
	}

	public void handleDisappear() {
		int		disappearingRow	= -1;
		int[]	stacks 			= Arrays.copyOf(this.numbersOfStacks, this.numbersOfStacks.length);
		Arrays.sort(stacks);
		int		stackMin		= stacks[0];
		for (int r = this.numberOfRows - stackMin; r < this.numberOfRows; r++) {
			boolean shouldDisappear = true;
			for (int c = 1; c < this.numberOfColumns; c++) {
				if (this.blocks[c-1][r].compareTo(this.blocks[c][r]) == 0) {
					shouldDisappear = false;
				}
			}
			if (shouldDisappear) {
				disappearingRow = r;
				break;
			}
		}
		if (disappearingRow != -1) {
			for (int c = 0; c < this.numberOfColumns; c++) {
				thisBoard.blocks[c][disappearingRow].setVisible(false);
				thisBoard.numbersOfStacks[c]--;
			}
			for (int r = 0; r < disappearingRow; r++) {
				for (int c = 0; c < this.numberOfColumns; c++) {
					Block thisBlock = this.blocks[c][r];
					if (thisBlock.getVisible()) {
						thisBlock.setVisible(false);
						thisBoard.droppableBlocks[c][r].setVisible(true);
						thisBoard.droppableBlocks[c][r].setDroppable(true);
						thisBoard.droppableBlocks[c][r].setColor(thisBlock.color);
					}
				}
			}
		} else {
			this.isDisappearing = false;
		}
	}

	// paint the whole board
	public void paint(Graphics g) {
		super.paint(g);
		Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		
		this.movableBlock.paint(g2d);
		this.info.paint(g2d);
		for (int c = 0; c < this.numberOfColumns; c++) {
			for (int r = 0; r < this.numberOfRows; r++) {
				this.blocks[c][r].paint(g2d);
				if (r < this.numberOfRows - 1) {
					this.droppableBlocks[c][r].paint(g2d);
				}
			}
		}
		this.nextMovableBlock.paint(g2d);
	}

	// update the status
	public void update() {
		this.movableBlock.update();
		if (this.isDisappearing) {
			for (int c = 0; c < this.numberOfColumns; c++) {
				for (int r = 0; r < this.numberOfRows - 1; r++) {
					this.droppableBlocks[c][r].update();
				}
			}
		}
	}
	
	// main function for the board
	public static void main(String[] args) throws InterruptedException {
		JFrame frame	= new JFrame("Shades");
		Board board		= new Board(300, 480);

		// initialize the frame
		frame.add(board);
		frame.setSize(300, 502);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setResizable(false);

		// initialize the board
		// board.setSize(frame.getWidth(), frame.getHeight());
		board.setNumberOfRows(11);
		board.setNumberOfColumns(4);

		while(true) {
			if (!board.isPaused) {
				board.update();
			}
			board.repaint();
			Thread.sleep(board.sleepTime);
		}
	}

	// the block class
	private class MovableBlock extends Block {
		public int		tempX;	// x coordinate of the block
		public int		tempY;	// y coordinate of the block
		public int		tempWidth;
		public int		tempHeight;
		public boolean	canDrop			= false;
		public boolean	canMerge		= false;
		public int		mergeTime		= this.height;
		public int		mergeTimer		= -1;
		public int		transformTime	= 100;
		public int		transformTimer	= this.transformTime;

		// contructor that sets (0, 0) as default coordinates
		public MovableBlock() {
			Random rand	= new Random();
			int n		= rand.nextInt(thisBoard.numberOfColumns);
			this.x		= this.width * n;
			this.y		= - 9 * this.height / 10;
			int m		= this.color.getRed() - rand.nextInt(4) * 43;
			this.color	= new Color(m, m, m);
		}

		public void update() {
			if (this.transformTimer >= 0) {
				this.tempWidth	= thisBoard.width - ((thisBoard.numberOfColumns - 1) * this.width - this.transformTimer * (thisBoard.numberOfColumns - 1) * this.width / this.transformTime);
				this.tempHeight	= this.height;
				this.tempX		= this.x - this.x * this.transformTimer / this.transformTime;
				this.tempY		= this.y;
				if (!thisBoard.isDisappearing) {
					this.transformTimer--;
				}
			} else {
				this.canDrop	= true;
				thisBoard.nextMovableBlock.setVisible(true);
			}
			if (this.canDrop) {
				this.drop();
				if (this.canMerge) {
					this.merge();
				}
				int column = this.x / this.width;
				if (this.y == thisBoard.height - this.height * (thisBoard.numbersOfStacks[column] + 1)) {
					int row = this.y / this.height;
					if (this.color.getRed() > 58 && row+1 < thisBoard.numberOfRows && 1 == this.compareTo(thisBoard.blocks[column][row+1])) {
						thisBoard.blocks[column][row+1].setVisible(false);
						this.canMerge			= true;
						this.tempHeight			= this.height * 2;
						this.mergeTimer			= this.mergeTime;
						thisBoard.info.score	= thisBoard.info.score + 4;
						thisBoard.numbersOfStacks[column]--;
						if (debugging) {
							System.out.println("Merging block("+this.y/this.height+", "+this.x/this.width+")");
						}
					} else {
						this.canDrop = false;
						thisBoard.info.score	= thisBoard.info.score + 2;
						thisBoard.blocks[column][row].setColor(this.color);
						thisBoard.blocks[column][row].setVisible(true);
						System.out.println("shouldStop"+thisBoard.blocks[column][row].y);
						numbersOfStacks[column]++;
						thisBoard.movableBlock		= thisBoard.nextMovableBlock;
						thisBoard.nextMovableBlock	= new MovableBlock();
						thisBoard.isDisappearing	= true;
						thisBoard.sleepTime			= thisBoard.levelSleepTime;
						thisBoard.handleDisappear();
					}
					if (debugging) {
						System.out.println("Score: "+thisBoard.info.score);
						System.out.println("Stacks: ("+thisBoard.numbersOfStacks[0]+", "+thisBoard.numbersOfStacks[1]+", "+thisBoard.numbersOfStacks[2]+", "+thisBoard.numbersOfStacks[3]+")");
					}	
				}
			}
		}

		public void drop() {
			this.y++;
		}

		private void moveLeft() {
			if (!thisBoard.isPaused && this.x > 0 && thisBoard.numbersOfStacks[this.x/this.width-1] + 1 < (thisBoard.numberOfRows-this.y/this.height)) {
				this.x -= this.width;
				if (debugging) {
					System.out.println("Block moved left");
				}
			}
		}

		private void moveRight() {
			if (!thisBoard.isPaused && this.x < thisBoard.width - this.width && thisBoard.numbersOfStacks[this.x/this.width+1] + 1 < (thisBoard.numberOfRows-this.y/this.height)) {
				this.x += this.width;
				if (debugging) {
					System.out.println("Block moved right");
				}
			}
		}

		public void merge() {
			if (this.mergeTimer >= 0) {
				this.tempWidth	= this.width;
				this.tempHeight	= this.height + this.mergeTimer * this.height / this.mergeTime;
				this.tempX		= this.x;
				this.tempY		= this.y;
				this.color = new Color(this.color.getRed()-1, this.color.getGreen()-1, this.color.getBlue()-1);
				this.mergeTimer--;
			} else {
				System.out.println("stop");
				this.canMerge	= false;
			}
		}
		
		// paint the block
		public void paint(Graphics2D g2d) {
			if (this.visible) {
				g2d.setColor(this.color);
				if (this.transformTimer >= 0 || this.mergeTimer >= 0) {
					g2d.fillRect(this.tempX, this.tempY, this.tempWidth, this.tempHeight);
				} else {
					g2d.fillRect(this.x, this.y, this.width, this.height);
				}
				// if (debugging) {
				// 	System.out.println("painting block (color: "+this.color+"; pos: ("+x+", "+y+"); size: "+this.width+", "+this.height+")");
				// 	System.out.println(this.color.getRed());
				// }
			}
		}
	}

	private class DroppableBlock extends MovableBlock {

		public DroppableBlock(int x, int y) {
			this.x = x;
			this.y = y;
			this.tempX = this.x;
			this.tempY = this.y;
			this.transformTimer = -1;
			this.canDrop = false;
			this.color	= thisBoard.blocks[this.x/this.width][this.y/this.height].color;
		}

		public void setDroppable(boolean b) {
			this.canDrop = b;
		}

		public void update() {
			if (this.canDrop) {
				System.out.println("dropping "+this.y);
				this.drop();
				if (this.canMerge) {
					this.merge();
				}
				int column = this.x / this.width;
				if (this.y == this.tempY+this.height) {
					System.out.println("reach");
					int row = this.y / this.height;
					if (this.color.getRed() > 15 && row+1 < thisBoard.numberOfRows && 1 == this.compareTo(thisBoard.blocks[column][row+1])) {
						thisBoard.blocks[column][row+1].setVisible(false);
						this.tempHeight = 2 * this.height;
						this.canMerge	= true;
						this.tempHeight	= this.height * 2;
						this.mergeTimer	= this.mergeTime;
						if (debugging) {
							System.out.println("Merging block("+this.y/this.height+", "+this.x/this.width+")");
						}
						thisBoard.numbersOfStacks[column]--;
					} else {
						thisBoard.handleDisappear();
						this.canDrop 				= false;
						thisBoard.blocks[column][row].setColor(this.color);
						thisBoard.blocks[column][row].setVisible(true);
						this.setVisible(false);
						this.x = this.tempX;
						this.y = this.tempY;
					}
					if (debugging) {
						System.out.println("Stacks: ("+thisBoard.numbersOfStacks[0]+", "+thisBoard.numbersOfStacks[1]+", "+thisBoard.numbersOfStacks[2]+", "+thisBoard.numbersOfStacks[3]+")");
					}
				}
			}
		}
	}

	private class Block implements Comparable<Block> {
		public int		x, y;	// x coordinate of the block
		public int		width	= thisBoard.width/(thisBoard.getNumberOfColumns());	// width of the block
		public int		height	= thisBoard.height/(thisBoard.getNumberOfRows());
		public Color	color	= new Color(230, 230, 230);	// color of the block
		public boolean	visible	= false;

		public Block() {
		}

		public Block(int x, int y) {
			this.x = x;
			this.y = y;
		}

		public void setVisible(boolean b) {
			this.visible = b;
		}

		public boolean getVisible() {
			return this.visible;
		}
		
		// paint the block
		private void paint(Graphics2D g2d) {
			if (this.visible) {
				int width, height, x, y;
				width	= this.width;
				height	= this.height;
				x = this.x;
				y = this.y;
				g2d.setColor(this.color);
				g2d.fillRect(x, y, width, height);
			}
		}

		public void setColor(Color color) {
			this.color = color;
		}

		public int compareTo(Block block) {
			if (this.color.getRed() == block.color.getRed() && this.color.getGreen() == block.color.getGreen() && this.color.getBlue() == block.color.getBlue()) {
				return 1;
			}
			return 0;
		}
	}

	public class Info {
		public	int	score	= 0;
		private int	x		= 10;
		private int	y		= 20;

		public Info() {
		}
		
		// paint the block
		private void paint(Graphics2D g2d) {
			g2d.setColor(new Color(0, 0, 0));
			g2d.drawString(Integer.toString(this.score), this.x, this.y);
		}
	}
}
