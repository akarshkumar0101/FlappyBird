package players;

import java.util.Set;

import flappy.FlappyGame;
import flappy.FlappyPlayer;
import flappy.Pipe;

public class PerfectAIPlayer implements FlappyPlayer {

	@Override
	public boolean getInput(FlappyGame game) {
		float py = game.getPlayerPosition(this);
		Set<Pipe> pipes = game.getPipes();
		float minPipePos = 100;
		Pipe closePipe = null;
		for (Pipe pipe : pipes) {
			if (game.getPipePosition(pipe) < minPipePos
					&& game.getPipePosition(pipe) >= FlappyGame.PLAYER_START_POSITION_X - .05) {
				minPipePos = game.getPipePosition(pipe);
				closePipe = pipe;
			}
		}
		if (py < (closePipe.getBotPosition() + closePipe.getTopPosition()) / 2) {
			return true;
		}

		return false;
	}
}
