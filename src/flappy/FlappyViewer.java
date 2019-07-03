package flappy;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.JComponent;

import players.HumanFlappyPlayer;
import thread.Threading;

public class FlappyViewer extends JComponent {
	private static final long serialVersionUID = -4506860996185249456L;

	private FlappyGame game;

	public FlappyViewer(FlappyGame game) {
		this.game = game;

		this.setFocusable(true);
		this.requestFocus();
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		// paint birds

		g.drawImage(background, 0, 0, getWidth(), getHeight(), null);

		synchronized (game) {

			for (Pipe pipe : game.getPipes()) {
				float yposTop = pipe.getTopPosition();
				float yposBot = pipe.getBotPosition();
				float xpos = game.getPipePosition(pipe);
				float width = pipe.getWidth();

				yposTop = 1 - yposTop;
				yposBot = 1 - yposBot;

				int w = (int) (width * this.getWidth());
				int ytop = (int) (yposTop * getHeight());
				int ybot = (int) (yposBot * getHeight());
				int x = (int) (xpos * getWidth());

				g.setColor(Color.DARK_GRAY);
				g.fillRect(x, 0, w, ytop);
				g.fillRect(x, ybot, w, getHeight() - ybot);

				int pipeHeight = 600;

				// g.fillRect(x, 0, w, ytop);
				g.drawImage(topPipeImage, x, ytop - pipeHeight, w, pipeHeight, null);
				g.drawImage(botPipeImage, x, ybot, w, pipeHeight, null);
			}
			for (FlappyPlayer player : game.getPlayers()) {
				float ypos = game.getPlayerPosition(player);
				float xpos = FlappyGame.PLAYER_START_POSITION_X;

				ypos = 1 - ypos;

				int rad = 16;
				int y = (int) (ypos * getHeight());
				int x = (int) (xpos * getWidth());
				if (game.playerIsAlive(player)) {
					g.setColor(Color.ORANGE);
				} else {
					g.setColor(Color.RED);
				}
				g.fillOval(x - rad, y - rad, 2 * rad, 2 * rad);

				rad += 3;
				g.drawImage(birdImage, x - rad, y - rad, 2 * rad, 2 * rad, null);
			}

			if (game.isRunning()) {
				g.setColor(Color.GREEN);
			} else {
				g.setColor(Color.RED);
			}
			((Graphics2D) g).setStroke(new BasicStroke(5.0f));
			g.drawRect(0, 0, getWidth(), getHeight());
		}

	}

	public Thread startRepaintingThread(int FPS) {
		Thread guiThread = new Thread(new Runnable() {
			@Override
			public void run() {
				while (true) {
					FlappyViewer.this.repaint();
					Threading.sleep(1000 / FPS);
				}
			}
		});
		guiThread.setDaemon(true);
		guiThread.start();
		return guiThread;
	}

	public void addHumanPlayer(HumanFlappyPlayer player) {
		this.addKeyListener(player);
	}

	public void removeHumanPlayer(HumanFlappyPlayer player) {
		this.removeKeyListener(player);
	}

	static Image background = null;
	static BufferedImage botPipeImage = null;
	static BufferedImage topPipeImage = null;
	static BufferedImage birdImage = null;
	static {
		URL backgroundURL = FlappyViewer.class.getResource("/background_hd.png");
		URL pipeURL = FlappyViewer.class.getResource("/botpipe2a.png");
		URL birdURL = FlappyViewer.class.getResource("/birda.png");
		try {
			background = ImageIO.read(backgroundURL);
			botPipeImage = ImageIO.read(pipeURL);

			AffineTransform tx = AffineTransform.getScaleInstance(-1, -1);
			tx.translate(-botPipeImage.getWidth(null), -botPipeImage.getHeight(null));
			AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
			topPipeImage = op.filter(botPipeImage, null);

			birdImage = ImageIO.read(birdURL);

		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
