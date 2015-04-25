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
	boolean 				debugging		= true;				// for debugging
	int 					numberOfColumns	= 4;				// default value for number of columns of blocks
	int 					numberOfRows	= 11;				// default value for number of rows of blocks
	Board					thisBoard		= this;				// for later reference
	boolean					isDead;								// stores whether or not the current game is dead
	boolean					isDisappearing; 					// stores whether or not one row is disappearing
	boolean					isMerging;							// stores whether or not one block is merging
	boolean					isPaused;							// stores whether or not the game is paused
	int						maxSleepTime;						// stores max thread sleep time
	int						minSleepTime;						// stores max thread sleep time
	int 					sleepTime;							// stores current sleep time // will be decreased when speed up
	Info					info;								// stores the info when playing
	Block[][]				blocks;								// stores the static blocks
	DroppableBlock[][]		droppableBlocks;					// stores the blocks that can only drop
	int[]					numbersOfStacks;					// stores the the number of blocks stacked for each column
	int						height, width;						// stores the width and height of the Board
	MovableBlock			movableBlock, nextMovableBlock;		// stores the current and next block that player can control
	DisappearableBlock[][]	disappearableBlock;					// stores the blocks that can only disappear
	
	public Board(int w, int h) {
		KeyListener listener = new MyKeyListener();
		this.addKeyListener(listener);
		this.width	= w;
		this.height	= h;
		this.setBackground(new Color(255, 255, 255));
		this.setFocusable(true);
		this.reset();
	}

	private void reset() {
		this.isDead				= false;
		this.maxSleepTime		= 10;
		this.minSleepTime		= 2;
		this.sleepTime			= this.maxSleepTime;
		this.isPaused			= false;
		this.isDisappearing		= false;
		this.isMerging			= false;
		this.sleepTime			= this.maxSleepTime;
		this.info				= new Info();
		this.blocks				= new Block[this.numberOfColumns][this.numberOfRows+1];
		this.droppableBlocks 	= new DroppableBlock[this.numberOfColumns][this.numberOfRows-1];
		this.disappearableBlock	= new DisappearableBlock[this.numberOfColumns][this.numberOfRows];
		this.numbersOfStacks 	= new int[this.numberOfColumns];
		this.movableBlock		= new MovableBlock();
		this.nextMovableBlock	= new MovableBlock();
		this.movableBlock.setVisible(true);
		this.movableBlock.setTransformable(true);
		for (int c = 0; c < this.numberOfColumns; c++) {
			this.numbersOfStacks[c] = 0;
			for (int r = 0; r < this.numberOfRows + 1; r++) {
				int x = c * this.movableBlock.width;
				int y = r * this.movableBlock.height + this.height % this.numberOfRows;
				this.blocks[c][r] = new Block(x, y);
				if (r < this.numberOfRows) {
					if (r < this.numberOfRows - 1) {
						this.droppableBlocks[c][r] = new DroppableBlock(x, y);
					}
					this.disappearableBlock[c][r] = new DisappearableBlock(x, y);
				}
			}
		}
	} // reset() ends
	
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
			int keyCode = e.getKeyCode();
			if (thisBoard.movableBlock.canDrop && !thisBoard.movableBlock.canMerge) {
				if (keyCode == 37) {
					thisBoard.movableBlock.moveLeft();
				} else if (keyCode == 39) {
					thisBoard.movableBlock.moveRight();
				} else if (keyCode < 58 && keyCode > 47) { // number key move to
					if (keyCode == 48) { // 0 key
						thisBoard.movableBlock.moveTo(9);
					} else {
						thisBoard.movableBlock.moveTo(keyCode - 49);
					}
					
				}
			}
			if (keyCode == 32) {
				thisBoard.isPaused = !thisBoard.isPaused;
				if (debugging) {
					System.out.println(thisBoard.isPaused);
				}
			} else if (keyCode == 40 || keyCode == 192) {
				if (thisBoard.sleepTime == 2) {
					thisBoard.sleepTime = 1;
				} else if (thisBoard.sleepTime != 1) {
					thisBoard.sleepTime = 2;
				}
			}
			if (debugging) {
				System.out.println(KeyEvent.getKeyText(keyCode)+"("+keyCode+")"+" pressed");
			}
		}
		@Override
		public void keyTyped(KeyEvent e) {
		}

		public void keyReleased(KeyEvent e) {

		}
	} // MyKeyListener ends

	public void handleDisappear() {
		int		disappearingRow	= -1;			// stores the row to disappear
		int[]	stacks 			= Arrays.copyOf(this.numbersOfStacks, this.numbersOfStacks.length);	// make copy of the array so as to sort
		Arrays.sort(stacks);					// sort the array in order to get minimun value
		int		stackMin		= stacks[0];	// stores the minimum number of blocks stacked in one column
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
			thisBoard.info.score += 10;
			for (int c = 0; c < this.numberOfColumns; c++) {
				Color color = thisBoard.blocks[c][disappearingRow].color;
				thisBoard.blocks[c][disappearingRow].setVisible(false);
				thisBoard.disappearableBlock[c][disappearingRow].setVisible(true);
				thisBoard.disappearableBlock[c][disappearingRow].setColor(color);
				thisBoard.numbersOfStacks[c] = thisBoard.numberOfRows - disappearingRow - 1;
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
			thisBoard.isDisappearing = false;
		}
	} // handleDisappear() ends

	// paint the whole board
	public void paint(Graphics g) {
		super.paint(g);
		Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		for (int c = 0; c < this.numberOfColumns; c++) {
			for (int r = 0; r < this.numberOfRows; r++) {
				this.blocks[c][r].paint(g2d);
				if (r < this.numberOfRows - 1) {
					this.droppableBlocks[c][r].paint(g2d);
				}
				this.disappearableBlock[c][r].paint(g2d);
			}
		}
		this.movableBlock.paint(g2d);
		this.nextMovableBlock.paint(g2d);
		this.info.paint(g2d);
	} // paint() ends

	// update the status
	public void update() {
		if (this.isDead) {
			this.reset();
		}
		this.movableBlock.update();
		this.nextMovableBlock.update();
		this.info.update();
		for (int c = 0; c < this.numberOfColumns; c++) {
			for (int r = this.numberOfRows - 1; r >= 0; r--) {
				if (r < this.numberOfRows - 1) {
 					this.droppableBlocks[c][r].update();
 				}
				this.disappearableBlock[c][r].update();
			}
		}
	} // update() ends
	
	// main function for the board
	public static void main(String[] args) throws InterruptedException {
		JFrame frame	= new JFrame("Shades");
		Board board		= new Board(300, 480);

		// initialize the frame
		frame.setSize(300, 502);
		frame.add(board);
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
	} // main() ends

	// the block class
	private class MovableBlock extends Block {
		public int		tempX;	// x coordinate of the block
		public int		tempY;	// y coordinate of the block
		public int		tempWidth;
		public int		tempHeight;
		public boolean	canDrop			= false;
		public boolean	canTransform	= false;
		public boolean	canMerge		= false;
		public int		mergeTime		= this.height;
		public int		mergeTimer		= -1;
		public int		transformTime	= 100;
		public int		transformTimer	= this.transformTime;
		public int		appearTime		= 100;
		public int		appearTimer		= this.appearTime;

		// contructor that sets (0, 0) as default coordinates
		public MovableBlock() {
			Random rand	= new Random();
			int n		= rand.nextInt(thisBoard.numberOfColumns);;
			this.x		= this.width * n;
			this.y		= - 9 * this.height / 10;
			int m		= this.color.getRed() - rand.nextInt(4) * 43;
			this.color	= new Color(m, m, m, 0);
		}

		public void update() {
			if (this.appearTimer >= 0) {
				this.color = new Color(this.color.getRed(), this.color.getGreen(), this.color.getBlue(), (this.appearTime-this.appearTimer)*255/this.appearTime);
				if (this.visible) {
					this.appearTimer--;
				}
			}
			if (this.transformTimer >= 0) {
				this.tempWidth	= thisBoard.width - ((thisBoard.numberOfColumns - 1) * this.width - this.transformTimer * (thisBoard.numberOfColumns - 1) * this.width / this.transformTime);
				this.tempHeight	= this.height;
				this.tempX		= this.x - this.x * this.transformTimer / this.transformTime;
				this.tempY		= this.y;
				if (!thisBoard.isDisappearing && !thisBoard.isMerging && this.canTransform) {
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
					thisBoard.sleepTime = thisBoard.info.levelSleepTime;
					int row = this.y / this.height;
					if (this.y < 0) {
						row = -1;
					}
					if (this.color.getRed() > 58 && row+1 < thisBoard.numberOfRows && 1 == this.compareTo(thisBoard.blocks[column][row+1])) {
						this.canMerge			= true;
						this.tempX				= this.x;
						this.tempY				= this.y;
						this.mergeTimer			= this.mergeTime;
						thisBoard.info.score	= thisBoard.info.score + 4;
						thisBoard.numbersOfStacks[column]--;
						if (debugging) {
							System.out.println("Merging block("+this.y/this.height+", "+this.x/this.width+")");
						}
					} else {
						this.canDrop = false;
						this.checkDie();
						if (row >= 0) {
							thisBoard.info.score	= thisBoard.info.score + 2;
							thisBoard.blocks[column][row].setColor(this.color);
							thisBoard.blocks[column][row].setVisible(true);
							System.out.println("shouldStop"+thisBoard.blocks[column][row].y);
							thisBoard.numbersOfStacks[column]++;
							thisBoard.nextMovableBlock.setTransformable(true);
							thisBoard.movableBlock		= thisBoard.nextMovableBlock;
							thisBoard.nextMovableBlock	= new MovableBlock();
							thisBoard.isDisappearing	= true;
							thisBoard.handleDisappear();
						}
					}
					if (debugging) {
						System.out.println("Score: "+thisBoard.info.score);
						System.out.println("Block at ("+column+", "+row+")\ncolor: "+this.color+"\nvisible: "+this.visible);
						System.out.println("Stacks: ("+thisBoard.numbersOfStacks[0]+", "+thisBoard.numbersOfStacks[1]+", "+thisBoard.numbersOfStacks[2]+", "+thisBoard.numbersOfStacks[3]+")");
					}	
				}
			}
		} // update() ends

		// paint the block
		public void paint(Graphics2D g2d) {
			if (this.visible) {
				g2d.setColor(this.color);
				if (this.transformTimer >= 0) {
					g2d.fillRect(this.tempX, this.tempY, this.tempWidth, this.tempHeight);
				} else {
					g2d.fillRect(this.x, this.y, this.width, this.height);
				}
				// if (debugging) {
				// 	System.out.println("painting block (color: "+this.color+"; pos: ("+this.x+", "+this.y+"); size: "+this.tempWidth+", "+this.tempHeight+")");
				// }
			}
		}

		public void drop() {
			this.y++;
		}

		public void merge() {
			if (this.mergeTimer > 0) {
				int row = this.tempY/this.height+1;
				if (this.tempY < 0) {
					row = 0;
				}
				int column = this.tempX/this.width;
				if (this.mergeTimer == 1) {
					thisBoard.blocks[column][row].setVisible(false);
					thisBoard.isMerging 	= false;
				}
				this.color = new Color(this.color.getRed()-1, this.color.getGreen()-1, this.color.getBlue()-1, 255);
				this.mergeTimer--;
				thisBoard.blocks[column][row].setColor(new Color(this.color.getRed()-1, this.color.getGreen()-1, this.color.getBlue()-1));
			} else {
				System.out.println("stop");
				this.canMerge		= false;
				thisBoard.isMerging = false;
			}
		} // merge() ends

		public void checkDie() {
			thisBoard.isDead = this.y <= 0 ? true : false;
			if (thisBoard.isDead) {
				thisBoard.isPaused = true;
			}
		}

		private void moveLeft() {
			if (!thisBoard.isPaused && this.x > 0 && (thisBoard.numbersOfStacks[this.x/this.width-1] + 1)*this.height < (thisBoard.numberOfRows*this.height-this.y)) {
				this.x -= this.width;
				if (debugging) {
					System.out.println("Block moved left");
				}
			}
		}

		private void moveRight() {
			if (!thisBoard.isPaused && this.x < thisBoard.width - this.width && (thisBoard.numbersOfStacks[this.x/this.width+1] + 1)*this.height < (thisBoard.numberOfRows*this.height-this.y)) {
				this.x += this.width;
				if (debugging) {
					System.out.println("Block moved right");
				}
			}
		}

		private void moveTo(int n) {
			if (!thisBoard.isPaused && n >= 0 && n < thisBoard.numberOfColumns && thisBoard.numbersOfStacks[n] + 1 < (thisBoard.numberOfRows-this.y/this.height)) {
				int c = this.x/this.width;
				if (c < n) {
					for (int i = c; i < n; i++) {
						this.moveRight();
					}
				} else if (c > n) {
					for (int i = c; i > n; i--) {
						this.moveLeft();
					}
				}
				if (debugging) {
					System.out.println("Block moved to column "+n);
				}
			}
		} // moveTo() ends	

		private void setTransformable(boolean b) {
			this.canTransform = b;
		}
	} // MoveableBlock ends

	/*
	 * DroopableBlock is used to show the dropping
	 * animation for exisiting static blocks after
	 * some row disappears
	 */
	private class DroppableBlock extends MovableBlock {
		public int bounceTime	= 10;
		public int bounceTimer	= bounceTime;

		public DroppableBlock(int x, int y) {
			this.x				= x;		// assigning the x coordinate of the droppable block
			this.y				= y;		// assigning the y coordinate of the droppable block
			this.tempX			= this.x;	// stores the temporary x which is used to demostrate the animation // stay unchanged actually
			this.tempY			= this.y;	// stores the temporary y which is used to demostrate the animation
			this.transformTimer	= -1;		// it doesn't need to transform as MovableBlock does, so the transformTimer is set to -1
			this.canDrop		= false;	// the droppable blocks not dropping till it needs to be visible for animation
			this.color			= thisBoard.blocks[this.x/this.width][this.y/this.height].color;	// set this color to the corresponding static block color
		}  // DroppableBlock() ends

		public void update() {
			if (this.bounceTimer > 0 && this.canDrop) { 
				this.y--;
				this.y--;
				this.bounceTimer--;
			} else if (this.canDrop) {
				System.out.println("dropping "+this.y);
				this.drop();
				if (this.canMerge) {
					thisBoard.sleepTime = thisBoard.info.levelSleepTime;
					this.merge();
				}
				int column = this.x / this.width;
				if (this.y == thisBoard.height - this.height * (thisBoard.numbersOfStacks[column] + 1)) {
					thisBoard.sleepTime = thisBoard.info.levelSleepTime;
					int row = this.y / this.height;
					if (this.color.getRed() > 58 && row+1 < thisBoard.numberOfRows && 1 == this.compareTo(thisBoard.blocks[column][row+1])) {
						this.tempX				= this.x;
						this.tempY				= this.y;
						this.canMerge			= true;
						this.mergeTimer			= this.mergeTime;
						thisBoard.isMerging		= true;
						if (debugging) {
							System.out.println("Merging block("+this.y/this.height+", "+this.x/this.width+")");
						}
						thisBoard.numbersOfStacks[column]--;
					} else {
						this.canDrop 				= false;
						thisBoard.numbersOfStacks[column]++;
						thisBoard.blocks[column][row].setColor(this.color);
						thisBoard.blocks[column][row].setVisible(true);
						this.setVisible(false);
						this.x = this.tempX;
						this.y = this.tempY;
						this.bounceTimer = this.bounceTime;
						thisBoard.handleDisappear();
					}
					if (debugging) {
						System.out.println("Stacks: ("+thisBoard.numbersOfStacks[0]+", "+thisBoard.numbersOfStacks[1]+", "+thisBoard.numbersOfStacks[2]+", "+thisBoard.numbersOfStacks[3]+")");
					}
				}
			}
		} // update() ends

		public void setDroppable(boolean b) {
			this.canDrop = b;
		}
	} // DroppableBlock ends

	private class DisappearableBlock extends Block {
		public int normHeight		= this.height;
		public int bounceTime 		= 10;
		public int bounceTimer 		= bounceTime;
		public int normY;

		public DisappearableBlock(int x, int y) {
			this.x 				= x;
			this.y 				= y;
			this.normY			= this.y;
		}

		public void update() {
			if (this.visible) {
				System.out.println("("+this.x+", "+this.y+") THIS IS VISIBLE!");
				this.disappear();
			}
		}

		public void disappear() {
			if (this.bounceTimer > 0) {
				this.height++;
				this.height++;
				this.y--;
				this.y--;
				this.bounceTimer--;
			} else if (this.height > 1) {
				thisBoard.sleepTime = 2;
				this.height--;
				this.y++;
			} else {
				this.height					= this.normHeight;
				this.y						= this.normY;
				this.bounceTimer			= this.bounceTime;
				this.visible				= false;
				thisBoard.isDisappearing	= false;
			}
		}

	}

	private class Block implements Comparable<Block> {
		public int		x, y;														// stores x and y coordinates of the block
		public int		width	= thisBoard.width/(thisBoard.getNumberOfColumns());	// stores width of the block, the number floored down
		public int		height	= thisBoard.height/(thisBoard.getNumberOfRows());	// stores height of the block, the number floored down
		public Color	color	= new Color(230, 230, 230, 255);							// stores color of the block
		public boolean	visible	= false;											// stores whether or not the block is visible

		public Block() {
		}

		public Block(int x, int y) {
			this.x = x;
			this.y = y;
		}
		
		// paint the block
		public void paint(Graphics2D g2d) {
			if (this.visible) {
				g2d.setColor(this.color);
				g2d.fillRect(this.x, this.y, this.width, this.height);
			}
		}

		public void setVisible(boolean b) {
			this.visible = b;
		}

		public void setColor(Color color) {
			this.color = color;
		}

		public boolean getVisible() {
			return this.visible;
		}

		public int compareTo(Block block) {
			if (this.color.getRed() == block.color.getRed() && this.color.getGreen() == block.color.getGreen() && this.color.getBlue() == block.color.getBlue()) {
				return 1;
			}
			return 0;
		}
	} // Block ends

	public class Info {
		public	int	score			= 0;						// stores the score that player earns
		public	int level			= 1;						// stores the current level of the game
		public	int levelSleepTime	= thisBoard.maxSleepTime;	// stores the sleep time for current level 
		private int	x				= thisBoard.width / 2;		// stores the x coordinate of the score that is displayed
		private int	y				= thisBoard.height / 15;	// stores the y coordinate of the score that is displayed
		private int fontSize		= thisBoard.height / 20;	// stores the font size of the score that is displayed


		public Info() {
		}

		public void update() {
			this.level			= this.score / 480 + 1;
			int sleepTimeToSet	= thisBoard.maxSleepTime - this.level / thisBoard.maxSleepTime;
			if (sleepTimeToSet <= thisBoard.maxSleepTime || sleepTimeToSet >= thisBoard.minSleepTime) {
				this.levelSleepTime = sleepTimeToSet;
			}
		} // update() ends
		
		// paint the block
		private void paint(Graphics2D g2d) {
			String scoreString = Integer.toString(this.score);
			String levelString = "Level "+Integer.toString(this.level);
			g2d.setFont(new Font("Arial", Font.PLAIN, this.fontSize));
			g2d.setColor(new Color(0, 0, 0));
			FontMetrics fm = g2d.getFontMetrics();
		    this.x = (thisBoard.width - fm.stringWidth(scoreString)) / 2;
			g2d.drawString(scoreString, this.x, this.y);
			g2d.setFont(new Font("Arial", Font.PLAIN, this.fontSize * 4 / 5));
			g2d.drawString(levelString, this.y/2, this.y);
		} 	// paint() ends
	} // Info ends
} // Board ends
