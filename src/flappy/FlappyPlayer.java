package flappy;

public interface FlappyPlayer {

	// true for jump, false for nothing
	public boolean getInput(FlappyGame game);

	public default void didDie() {
	}

}
