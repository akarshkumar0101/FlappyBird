package flappy;

public class Pipe {

	private float botPosition;
	private float topPosition;
	private float width;

	public Pipe(float botPosition, float topPosition, float width) {
		this.botPosition = botPosition;
		this.topPosition = topPosition;
		this.width = width;
	}

	public float getBotPosition() {
		return botPosition;
	}

	public void setBotPosition(float botPosition) {
		this.botPosition = botPosition;
	}

	public float getTopPosition() {
		return topPosition;
	}

	public void setTopPosition(float topPosition) {
		this.topPosition = topPosition;
	}

	public float getCenterPosition() {
		return (botPosition + topPosition) / 2;
	}

	public float getWidth() {
		return width;
	}

	public void setWidth(float width) {
		this.width = width;
	}
}
