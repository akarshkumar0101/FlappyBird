package flappy;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import math.AKMath;
import math.AKRandom;
import thread.Threading;

public class FlappyGame {

	public static final float PLAYER_START_POSITION_X = 0.1f;
	public static final float PLAYER_START_POSITION_Y = 0.5f;
	public static final float PLAYER_START_VELOCITY_X = 0.0f;
	public static final float PLAYER_START_VELOCITY_Y = 0.0f;

	public static final float GRAVITY = -3.0f;

	public static final float PIPE_SPEED = 0.30f;

	public static final float EDGE_POSITION = 1.05f;
	public static final float NEXT_EDGE_POSITION = 2.04f;
	public static final float PIPE_SPACING = 0.5f;

	public static final float PIPE_WIDTH = 0.05f;
	public static final float PIPE_BOUNDRY_TOP = 0.95f;
	public static final float PIPE_BOUNDRY_BOT = 0.05f;

	public static final float PIPE_GAP = 0.2f;

	public static final float GROUND_Y = 0.01f;
	public static final float SKY_Y = 0.99f;

	public static final float PLAYER_JUMP_VELOCITY = 0.5f;

	// WHOLE enviroment is from [0,1] x and [0,1] y

	public double gameSpeed = 1.0;

	private int FPS;

	private boolean isRunning;

	private final Map<FlappyPlayer, PlayerData> players;
	// pipes x positions;
	private final Map<Pipe, Float> pipePositions;

	private float distanceTraveled;

	public FlappyGame() {

		players = new HashMap<>();
		pipePositions = new HashMap<>();

		FPS = 60;

		hardReset(true);
	}

	public void hardReset(boolean deletePlayers) {
		isRunning = false;

		if (deletePlayers) {
			players.clear();
		} else {
			for (FlappyPlayer player : players.keySet()) {
				players.put(player, new PlayerData(player));
			}
		}

		pipePositions.clear();
		distanceTraveled = 0.0f;
		
		rand = new Random(0);
	}

	public void setGameSpeed(double gameSpeed) {
		this.gameSpeed = gameSpeed;
	}

	public boolean isRunning() {
		return isRunning;
	}

	public void addPlayer(FlappyPlayer player) {
		if (isRunning) {
			throw new RuntimeException("Game already started buddy");
		}
		players.put(player, new PlayerData(player));
	}

	public void setGameFPS(int FPS) {
		this.FPS = FPS;
	}

	public boolean someoneIsAlive() {
		for (FlappyPlayer player : players.keySet()) {
			PlayerData data = players.get(player);
			if (data.isAlive) {
				return true;
			}
		}
		return false;
	}

	public void runGame(boolean gui) {
		runGame(gui, Float.MAX_VALUE);
	}

	public void runGame(boolean gui, float distanceTimeout) {
		this.isRunning = true;

//		for (float pipePosition = EDGE_POSITION; pipePosition <= NEXT_EDGE_POSITION; pipePosition += PIPE_SPACING) {
//			generatePipeAtPosition(pipePosition);
//		}
		generatePipeAtEdge();

		long last = System.currentTimeMillis();
		while (someoneIsAlive()) {
			long now = System.currentTimeMillis();
			if (gui) {
				syncTick((double) (now - last) / 1000.0);
			} else {
				tick((double) (now - last) / 1000.0);
			}
			last = now;

			Threading.sleep(1000 / FPS);

			if (distanceTraveled >= distanceTimeout) {
				// killAll();
				break;
			}
		}

		isRunning = false;
	}

	public void killAll() {
		for (PlayerData playerData : players.values()) {
			if (playerData.isAlive) {
				playerData.die();
			}
		}
	}

	public void syncTick(double dt) {
		synchronized (this) {
			tick(dt);
		}
	}

	public void tick(double dt) {
		dt *= gameSpeed;
		Pipe upcomingPipe = getUpcomingPipe();

		// upate players
		for (FlappyPlayer player : players.keySet()) {
			PlayerData data = players.get(player);

			data.y += data.vely * dt;
			data.vely += GRAVITY * dt;

			if (data.isAlive && player.getInput(this)) {
				// data.vely+=1;
				data.vely = PLAYER_JUMP_VELOCITY;
			}

			data.y = Math.max(data.y, GROUND_Y);
		}

		// update pipes
		boolean shouldAddNewPipe = false;
		Iterator<Map.Entry<Pipe, Float>> pipeItr = pipePositions.entrySet().iterator();

		while (pipeItr.hasNext()) {
			Pipe pipe = pipeItr.next().getKey();
			float pos = pipePositions.get(pipe);
			float prevPos = pos;
			pos -= PIPE_SPEED * dt;
			pipePositions.put(pipe, pos);

			if (pipe == upcomingPipe) {
				float posBack = pos + pipe.getWidth(), prevPosBack = prevPos + pipe.getWidth();
				if (posBack < PLAYER_START_POSITION_X && prevPosBack >= PLAYER_START_POSITION_X) {
					// increment scores for alive players
					for (PlayerData playerData : players.values()) {
						if (playerData.isAlive) {
							playerData.incrementScore();
						}
					}
				}
			}

			if (pos < -0.05f) {
				pipeItr.remove();
			}

			if (prevPos > EDGE_POSITION - PIPE_SPACING && pos <= EDGE_POSITION - PIPE_SPACING) {
				shouldAddNewPipe = true;
			}
		}
		if (shouldAddNewPipe) {
			generatePipeAtEdge();
		}

		double dx = PIPE_SPEED * dt;
		distanceTraveled += dx;

		// update deaths
		for (FlappyPlayer player : players.keySet()) {
			PlayerData playerData = players.get(player);
			if (playerData.isAlive) {

				if (playerData.y <= GROUND_Y) {
					playerData.die();
				} else if (playerData.y >= SKY_Y) {
					playerData.die();
				} else {

					for (Pipe pipe : pipePositions.keySet()) {

						float pipePosX = pipePositions.get(pipe);
						if (playerData.x >= pipePosX && playerData.x <= pipePosX + pipe.getWidth()) {
							if (playerData.y < pipe.getBotPosition() || playerData.y > pipe.getTopPosition()) {
								playerData.die();
							}
						}
					}
				}

				playerData.distanceTraveled += dx;
			}
		}

	}

	public Pipe getUpcomingPipe() {
		Pipe pipe = null;
		float pipepos = Float.MAX_VALUE;
		for (Pipe p : pipePositions.keySet()) {
			float pos = pipePositions.get(p) + p.getWidth();
			if (pos >= PLAYER_START_POSITION_X && pos < pipepos) {
				pipe = p;
				pipepos = pos;
			}
		}

		return pipe;
	}

	private void generatePipeAtEdge() {
		generatePipeAtPosition(EDGE_POSITION);
	}

	public Random rand = new Random(0);

	private void generatePipeAtPosition(float position) {

		double p = rand.nextDouble();
		
		float botPosition = (float) AKRandom.randomNumber(PIPE_BOUNDRY_BOT, PIPE_BOUNDRY_TOP - PIPE_GAP);
		botPosition = 0.40f;
		botPosition = (float) AKMath.scale(p, 0, 1, PIPE_BOUNDRY_BOT, PIPE_BOUNDRY_TOP - PIPE_GAP);

		Pipe pipe = new Pipe(botPosition, botPosition + PIPE_GAP, PIPE_WIDTH);

		this.pipePositions.put(pipe, position);
	}

	/* private */ class PlayerData {
		FlappyPlayer player;

		float x;
		float y;
		float velx;
		float vely;

		boolean isAlive;

		int score;

		float distanceTraveled;

		public PlayerData(FlappyPlayer player) {
			this.player = player;
			x = PLAYER_START_POSITION_X;
			y = PLAYER_START_POSITION_Y;
			velx = PLAYER_START_VELOCITY_X;
			vely = PLAYER_START_VELOCITY_Y;

			isAlive = true;

			score = 0;
		}

		public void incrementScore() {
			score++;
		}

		public void die() {
			isAlive = false;
			player.didDie();
		}

	}

	public Set<FlappyPlayer> getPlayers() {
		return players.keySet();
	}

	public float getPlayerPosition(FlappyPlayer player) {
		return players.get(player).y;
	}

	public float getPlayerVelocity(FlappyPlayer player) {
		return players.get(player).vely;
	}

	public boolean playerIsAlive(FlappyPlayer player) {
		return players.get(player).isAlive;
	}

	public Set<Pipe> getPipes() {
		return pipePositions.keySet();
	}

	public float getPipePosition(Pipe pipe) {
		return pipePositions.get(pipe);
	}

	public float getDistanceTraveledCurrently() {
		return distanceTraveled;
	}

	public float getDistanceTraveledForPlayer(FlappyPlayer player) {
		return players.get(player).distanceTraveled;
	}

	public int scoreForPlayer(FlappyPlayer player) {
		return players.get(player).score;

	}

}
