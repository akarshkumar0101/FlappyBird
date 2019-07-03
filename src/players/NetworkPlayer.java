package players;

import flappy.FlappyGame;
import flappy.FlappyPlayer;
import flappy.Pipe;
import machinelearning.ne.neat.network.NeuralNetwork;

public class NetworkPlayer implements FlappyPlayer {

	private final NeuralNetwork network;

	public NetworkPlayer(NeuralNetwork network) {
		this.network = network;
	}

	@Override
	public boolean getInput(FlappyGame game) {
		double[] gameDataInput = extractGameInformation(game);
		network.calculate(gameDataInput);

		double out = network.getOutputNeurons().get(0).getActivation();

		return out > 0.5;
	}

	public double[] extractGameInformation(FlappyGame game) {
		Pipe pipe = game.getUpcomingPipe();

		double[] inputs = new double[3];

		inputs[0] = game.getPipePosition(pipe);
		inputs[1] = game.getPlayerPosition(this);
		inputs[2] = pipe.getCenterPosition();

		return inputs;
	}

}
