package main;

import flappy.FlappyGame;
import flappy.FlappyViewer;
import players.HumanFlappyPlayer;
import players.PerfectAIPlayer;
import thread.Threading;
import ui.FrameWrapper;

public class Main {

	public static final FlappyGame game = new FlappyGame();
	public static final FlappyViewer viewer = new FlappyViewer(game);
	public static final FrameWrapper<FlappyViewer> frame = new FrameWrapper<>("Flappy", 1000, 600, true, false);

	public static void main(String... args) {

		HumanFlappyPlayer humanPlayer = new HumanFlappyPlayer();
		viewer.addHumanPlayer(humanPlayer);

		game.addPlayer(humanPlayer);
		game.addPlayer(new PerfectAIPlayer());

		frame.setComponent(viewer);
		frame.setVisible(true);

		game.setGameFPS(40);
		viewer.startRepaintingThread(40);

		while (true) {
			game.runGame(true, 2);

			Threading.sleep(1000);
			game.hardReset(false);
		}
	}
}
