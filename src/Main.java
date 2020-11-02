/**
 * Driver class for the game of concentration
 *
 * @author Austin Sands
 */
public class Main {
	
	// The game instance
	private Game game;

	/**
	 * @param args command line arguments, not used for this project.
	 */
	public static void main(String[] args) {
		Main instance = new Main();
		instance.run();
	}
	
	public void run() {
		// Create a new instance of the game with default constructor
		game = new Game();

		// start the game
		game.start();
	}
}
