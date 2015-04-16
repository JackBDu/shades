/*
 * game.java
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

public class Game extends JPanel {
	int x = 0;
	int y = 0;

	private void moveBall() {
		x = x + 1;
		y = y + 1;
	}
	
	@Override
	public void paint(Graphics g) {
		this.setBackground(Color.BLACK);
		Graphics2D g2d = (Graphics2D) g;
		g2d.setColor(Color.RED);
		g2d.fillRect(x, y, 30, 30);
		g2d.drawRect(x, y, 30, 30);
	}
	
	public static void main(String[] args) throws InterruptedException {
		JFrame frame = new JFrame("Shades");
		Game game = new Game();
		frame.add(game);
		frame.setSize(600, 960);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setResizable(true);

		while (true) {
			game.moveBall();
			game.repaint();
			Thread.sleep(10);
		}
	}

}
