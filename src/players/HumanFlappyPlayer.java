package players;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import flappy.FlappyGame;
import flappy.FlappyPlayer;

public class HumanFlappyPlayer implements FlappyPlayer, KeyListener {

	private boolean didJump;

	public HumanFlappyPlayer() {
		didJump = false;
	}

	@Override
	public boolean getInput(FlappyGame game) {
		synchronized (this) {
			if (didJump) {
				didJump = false;
				return true;
			}
			return false;
		}
	}

	@Override
	public void keyTyped(KeyEvent e) {
	}

	@Override
	public void keyPressed(KeyEvent e) {
		synchronized (this) {
			didJump = true;
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
	}

}
