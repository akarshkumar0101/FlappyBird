package main;

import java.util.ArrayList;
import java.util.List;

import flappy.FlappyGame;
import flappy.FlappyViewer;
import machinelearning.ne.neat.NEAT;
import machinelearning.ne.neat.NEATStats;
import machinelearning.ne.neat.NEATTrainer;
import machinelearning.ne.neat.genome.BaseTemplate;
import machinelearning.ne.neat.genome.ConnectionGene;
import machinelearning.ne.neat.genome.Genome;
import machinelearning.ne.neat.network.NeuralNetwork;
import math.AKRandom;
import players.HumanFlappyPlayer;
import players.NetworkPlayer;
import thread.Threading;
import ui.FrameWrapper;

public class NEATMain {

	public static final FlappyGame game = new FlappyGame();
	public static final FlappyViewer viewer = new FlappyViewer(game);
	public static final FrameWrapper<FlappyViewer> frame = new FrameWrapper<>("Flappy", 1000, 600, true, false);

	public static final NEATFlappyTrainer trainer = new NEATFlappyTrainer();
	public static NEAT neat;

	public static void main(String... args) {

		HumanFlappyPlayer humanPlayer = new HumanFlappyPlayer();
		// viewer.addHumanPlayer(humanPlayer);
		// game.addPlayer(humanPlayer);

		// game.addPlayer(new PerfectAIPlayer());

		frame.setComponent(viewer);
		frame.setVisible(true);

		game.setGameFPS(40);
		viewer.startRepaintingThread(40);

		game.setGameSpeed(3.0f);
		neat = new NEAT(100, trainer);

		neat.currentInnovationNumber = 4;
		while (true) {
			// game.runGame(true, 2);

			// fitnessFunction(generateNGenomes(150));

			neat.runGeneration();

			Threading.sleep(100);
			// game.hardReset(false);
		}
	}

	public static List<Double> fitnessFunction(List<Genome> genos) {
		List<Double> fitnesses = new ArrayList<>(genos.size());
		List<NetworkPlayer> players = new ArrayList<>(genos.size());
		synchronized (game) {
			game.hardReset(true);

			for (Genome geno : genos) {
				NeuralNetwork network = new NeuralNetwork(geno);
				NetworkPlayer networkPlayer = new NetworkPlayer(network);

				players.add(networkPlayer);

				game.addPlayer(networkPlayer);
			}
		}

		game.runGame(true, 20);

		float maxScore = -1;
		for (int i = 0; i < players.size(); i++) {

			double fitness = 0;

			fitness += game.getDistanceTraveledForPlayer(players.get(i));

			fitnesses.add(fitness);

			maxScore = (float) Math.max(maxScore, game.scoreForPlayer(players.get(i)));
		}
		System.out.println(maxScore);

		return fitnesses;
	}

	public static List<Genome> generateNGenomes(int n) {
		List<Genome> genos = new ArrayList<>(n);
		for (int i = 0; i < n; i++) {
			genos.add(trainer.generateRandom(neat));
		}
		return genos;
	}

}

class NEATFlappyTrainer implements NEATTrainer {
	private final NEATStats neatStats = new NEATStats();

	@Override
	public double calculateFitness(Genome a, NEAT neat) {
		return 0;
	}

	@Override
	public List<Double> calculateFitness(List<Genome> genos, NEAT neat) {
		return NEATMain.fitnessFunction(genos);
	}

	@Override
	public Genome generateRandomGenome(NEAT neat) {
		Genome geno = new Genome(new BaseTemplate(true, 3, 1), 0);

		geno.getConnectionGenes().add(new ConnectionGene(0, 0, 4, AKRandom.randomNumber(-1, 1), true));

		geno.getConnectionGenes().add(new ConnectionGene(1, 1, 4, AKRandom.randomNumber(-1, 1), true));
		geno.getConnectionGenes().add(new ConnectionGene(2, 2, 4, AKRandom.randomNumber(-1, 1), true));
		geno.getConnectionGenes().add(new ConnectionGene(3, 3, 4, AKRandom.randomNumber(-1, 1), true));

		return geno;
	}

	@Override
	public NEATStats calculateStatsForGeneration(NEAT neat) {
		return neatStats;
	}

	{
		// similarity parameters for species calculation
		neatStats.c1 = 1.0;
		neatStats.c2 = 1.0;
		neatStats.c3 = 0.4;
		neatStats.deltaThreshold = 0.5;

		// general GA parameters
		neatStats.percentPopulationToKill = 0.3;
		neatStats.crossoverProbability = 1.0;

		// mutation parameters
		neatStats.weightShiftStrengh = 0.3;
		neatStats.weightRandomizeStrengh = 2.0;

		// mutation probabilities
		neatStats.mutationProbability = 0.2;

		neatStats.addConnectionProbability = 0.01;
		neatStats.addNodeProbability = 0.05;
		neatStats.weightShiftProbability = 0.02;
		neatStats.weightRandomizeProbability = 0.02;
		neatStats.toggleConnectionProbability = 0.0;

	}

}
