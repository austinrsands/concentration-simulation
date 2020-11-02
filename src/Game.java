import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/**
 * Represents the game of concentration, a card matching game.
 * 
 * There are 4 game modes: single player, player vs player, player vs. bot, and
 * bot vs bot simulation. The game starts by placing a set of cards face down on
 * a playing field. During each turn, two cards are flipped face up. If they
 * match, then they are removed from the field and the player gets another turn.
 * If they do not match, then they are placed face down again and it becomes the
 * next player's turn. The goal of the game is to match all of the cards in the
 * least number of moves. In bot vs bot simulation mode, a series of games
 * between two bots is played out, and the spreads of the matches are reported.
 * 
 * @author Austin Sands
 */
public class Game {

	// the players
	private Player[] players = {};

	// reference to player with current turn
	private Player currentPlayer;

	// index of current player in players array;
	private int currentPlayerIndex;

	// the total number of matches
	private int numMatches = 0;

	// the total number of moves
	private int numMoves = 0;

	// whether the board is simulating and should stop console output
	private boolean simulating;

	// number of simulations to run
	private int numSimulations = 15000;

	// the number of completed simulations
	private int completedSimulations;

	// records the the count of the spreads (difference in matches between players)
	// for the simulation
	private HashMap<Integer, Integer> spreads = new HashMap<Integer, Integer>();

	// the instance of the board
	private Board board;

	// scanner used for getting user input
	private Scanner scanner = new Scanner(System.in);

	// Messages to user, common among all instances of the class
	private static final String WELCOME_MESSAGE = "Welcome to the Game of Concentration!";
	private static final String EXIT_KEYWORD = "quit";
	private static final String INPUT_INSTRUCTIONS = "Enter the row-column pair of two cards to flip, or type \""
			+ EXIT_KEYWORD + "\" to end the game.";
	private static String INPUT_EXAMPLE = "Example: \"(0, 0) (0, 1)\" or \"0 0 0 1\"";
	private static final String INPUT_SUGGESTION = "Please choose two different, available cards as an ordered pair.";
	private static final String MATCH_MADE_MESSAGE = "made a match! This pair will be removed.";
	private static final String SINGLE_PLAYER_NO_MATCH_MESSAGE = "No match found. Please try again or type \""
			+ EXIT_KEYWORD + "\" to exit.";
	private static final String MULTIPLAYER_NO_MATCH_MESSAGE = "No match found. Switching turns...";
	private static final String GAME_OVER_MESSAGE = "Game Over.";
	private static final String GAME_WON_MESSAGE = "won the game!";
	private static final String AFFIRMATIVE_RESPONSE = "yes";
	private static final String NEGATIVE_RESPONSE = "no";
	private static final String REPLAY_PROMPT = "Would you like to play again? \"" + AFFIRMATIVE_RESPONSE + "\" or \""
			+ NEGATIVE_RESPONSE + "\"";
	private static final String ANSWER_PROMPT = "Anwser: ";
	private static final String ALREADY_PAIRED_MESSAGE = "One or more of the given cards has already been paired.";
	private static final String OUT_OF_BOUNDS_MESSAGE = "The given positions aren't on the board.";
	private static final String DUPLICATE_CARD_MESSAGE = "The given positions must be different.";
	private static final String INVALID_INPUT_MESSAGE = "Invalid input!";
	private static final String INVALID_MODE_MESSAGE = "Invalid Mode!";
	private static final String INVALID_SIMULATIONS_MESSAGE = "Invalid number of games!";
	private static final String MODE_MESSAGE = "Please enter the number of the mode you would like to select:";
	private static final String MODE_PROMPT = "Mode: ";
	private static final String MODE_LIST_MESSAGE = "1 - Single Player\n2 - Player vs Player\n3 - Player vs Bot\n4 - Bot vs Bot (Simulation)";
	private static final String SIMULATIONS_MESSAGE = "Please enter the number of games to simulate";
	private static final String SIMULATIONS_PROMPT = "Number of games: ";

	// regular expression for getting safe input, matches integers
	private static final String INPUT_PATTERN = "[0-9][0-9]*";
	private Pattern inputPattern = Pattern.compile(INPUT_PATTERN);
	private Matcher inputMatcher;

	// regular expression for getting mode number
	private static final String MODE_PATTERN = "[1-4]";
	private Pattern modePattern = Pattern.compile(MODE_PATTERN);
	private Matcher modeMatcher;

	// regular expression for getting number of simulations
	private static final String SIMULATIONS_PATTERN = "[1-9][0-9]*";
	private static final int MAX_SIMULATIONS = 1000000;
	private static final int SIMULATIONS_DISPLAY_INCREMENT = 10000;
	private Pattern simulationsPattern = Pattern.compile(SIMULATIONS_PATTERN);
	private Matcher simulationsMatcher;

	// the number of values to be inputed
	private static final int INPUT_NUMS_SIZE = 4;

	// whether a match has been found
	private boolean matchFound;

	// whether a move is valid
	private boolean validMove;

	/**
	 * Constructs a Game with the given columns, rows, and names.
	 * 
	 * @param columns number of columns
	 * @param rows    number of rows
	 * @param names   names to appear on cards
	 */
	public Game(int columns, int rows, String[] names) {
		board = new Board(columns, rows, names);
	}

	/**
	 * Constructs a Game with the given columns, rows, and names.
	 * 
	 * @param columns number of columns
	 * @param rows    number of rows
	 * @param names   names to appear on cards
	 */
	public Game(int columns, int rows) {
		board = new Board(columns, rows);
	}

	/**
	 * Constructs a Game with the given names.
	 * 
	 * @param names possible names to appear on the cards
	 */
	public Game(String[] names) {
		board = new Board(names);
	}

	/**
	 * Constructs a Game with default properties.
	 */
	public Game() {
		board = new Board();
	}

	/**
	 * Begins the matching game.
	 */
	public void start() {
		System.out.println(WELCOME_MESSAGE);
		board.setup();
		selectGameMode();
		resetGameState();
		enterGameLoop();
	}

	/*
	 * Resets the number of moves, matches, and whether match is found
	 */
	private void resetGameState() {
		for (Player player : players) {
			player.reset();
		}
		matchFound = false;
		numMatches = 0;
		numMoves = 0;
		currentPlayerIndex = 0;
		currentPlayer = players[0];
	}

	/*
	 * Returns valid game mode from user
	 */
	private int getGameMode() {
		boolean modeIsSelected = false;
		while (!modeIsSelected) {
			System.out.println(MODE_MESSAGE);
			System.out.println(MODE_LIST_MESSAGE);
			System.out.print(MODE_PROMPT);
			String modeInput = scanner.nextLine();

			// if user types the exit keyword
			if (modeInput.toLowerCase().equals(EXIT_KEYWORD.toLowerCase())) {
				exitGame();
				// break out of the loop
				break;
			}

			modeMatcher = modePattern.matcher(modeInput);
			if (modeMatcher.find()) {
				// If number cannot be parsed, it will throw a NumberFormatException
				try {
					return Integer.parseInt(modeMatcher.group());
				} catch (NumberFormatException e) {
					System.out.println(INVALID_MODE_MESSAGE);
				}
			} else {
				System.out.println(INVALID_MODE_MESSAGE);
			}
		}
		// should never get to this point, but just in case
		return -1;
	}

	/*
	 * Selects the correct game mode
	 */
	private void selectGameMode() {
		int mode = getGameMode();
		switch (mode) {
		case 1:
			// mode 1: single player
			players = new Player[] { new Player("Player 1 (You)", true) };
			simulating = false;
			System.out.println("Single Player Mode Selected");
			break;
		case 2:
			// mode 2: human vs human
			players = new Player[] { new Player("Player 1", true), new Player("Player 2", true) };
			simulating = false;
			System.out.println("Player vs Player Mode Selected");
			break;
		case 3:
			// mode 3: human vs bot
			players = new Player[] { new Player("Player 1 (You)", true), new Player("Bot 1", false) };
			simulating = false;
			System.out.println("Player vs Bot Mode Selected");
			break;
		case 4:
			// mode 4:
			players = new Player[] { new Player("Bot 1", false), new Player("Bot 2", false) };
			simulating = true;
			completedSimulations = 0;
			spreads.clear();
			System.out.println("Bot vs Bot (Simulation) Mode Selected");
			numSimulations = getNumSimulations();
			break;
		default:
			// exit the program
			exitGame();
		}
	}

	/*
	 * Gets the number of simulations to run from user
	 */
	private int getNumSimulations() {
		boolean inputAccepted = false;
		while (!inputAccepted) {
			System.out.printf("%s between 1 and %d.\n", SIMULATIONS_MESSAGE, MAX_SIMULATIONS);
			System.out.print(SIMULATIONS_PROMPT);
			String simulationsInput = scanner.nextLine();

			// if user types the exit keyword
			if (simulationsInput.toLowerCase().equals(EXIT_KEYWORD.toLowerCase())) {
				exitGame();
				// break out of the loop
				break;
			}

			simulationsMatcher = simulationsPattern.matcher(simulationsInput);
			if (simulationsMatcher.find()) {
				// If number cannot be parsed, it will throw a NumberFormatException
				try {
					int num = Integer.parseInt(simulationsMatcher.group());
					if (num <= MAX_SIMULATIONS) {
						return num;
					} else {
						System.out.println(INVALID_SIMULATIONS_MESSAGE);
					}
				} catch (NumberFormatException e) {
					System.out.println(INVALID_SIMULATIONS_MESSAGE);
				}
			} else {
				System.out.println(INVALID_SIMULATIONS_MESSAGE);
			}
		}
		// should never get to this point, but just in case
		return 1;
	}

	/*
	 * Runs throughout lifetime of game, gets input from user
	 */
	private void enterGameLoop() {
		// if in simulation mode, repeat the game loop
		if (simulating) {
			runSimulations();
		} else {
			// loop while game is not over
			while (numMatches != board.getPossibleMatches()) {

				// change turns, if necessary
				if (!matchFound && numMoves != 0) {
					switchTurns();
				}

				// display the board
				System.out.println(board);
				// Tell user if they made match or need instruction
				printMatchMessage();

				if (currentPlayer.isHuman()) {
					// get input
					handleInput();
				} else {
					flipCards(board.getRandomCards());
				}
			}
		}
		// game is over
		endGame(true);
	}

	/*
	 * Runs repeated simulations of the game with two bots and records the spread of
	 * their matches
	 */
	private void runSimulations() {
		System.out.println("Simulating...");
		// int displayIncrement = numSimulations / 10000;
		for (int i = 0; i < numSimulations; i++) {
			if (i > 0 && i % SIMULATIONS_DISPLAY_INCREMENT == 0) {
				System.out.printf("Simulated %d/%d Games\n", i, numSimulations);
			}
			// loop while game is not over
			while (numMatches != board.getPossibleMatches()) {

				// change turns, if necessary
				if (!matchFound && numMoves != 0) {
					switchTurns();
				}
				if (matchFound) {
					matchFound = false;
				}
				flipCards(board.getRandomCards());
			}
			// single simulation is complete, record the win
			int spread = players[0].getMatches() - players[1].getMatches();
			int spreadCount = spreads.containsKey(spread) ? spreads.get(spread) : 0;
			spreads.put(spread, spreadCount + 1);

			// restart game for next simulation
			board.setup();
			resetGameState();
			completedSimulations++;
		}
		if (numSimulations == 1) {
			System.out.printf("Simulated %s Game\n\n", completedSimulations);
		} else {
			System.out.printf("Simulated %s Games\n\n", completedSimulations);
		}
	}

	/*
	 * switches turns to the next player
	 */
	private void switchTurns() {
		if (++currentPlayerIndex >= players.length) {
			currentPlayerIndex = 0;
		}
		currentPlayer = players[currentPlayerIndex];
	}

	/*
	 * Prompts the user for input and flips cards accordingly
	 */
	private void handleInput() {
		validMove = false;

		// Prompt the user for input until it is valid
		while (!validMove) {
			System.out.print(currentPlayer.getName() + ": ");

			// get input
			String input = scanner.nextLine();

			// if user quits, end the game
			if (input.toLowerCase().equals(EXIT_KEYWORD.toLowerCase())) {
				// prepare the board and quit the game
				board.hideCards();
				board.update();
				endGame(false);

				// break out of the loop
				break;

			} else {
				// create array to hold input values
				int[] sanitizedInput = getSanitizedInput(input);

				// flip the cards with the given input, if possible
				flipCards(sanitizedInput);
			}
		}
	}

	/*
	 * Attempts to return array of sanitized input, returns null if input could not
	 * be sanitized.
	 */
	private int[] getSanitizedInput(String input) {
		int[] inputNums = new int[INPUT_NUMS_SIZE];
		// prepare input for pattern matching
		inputMatcher = inputPattern.matcher(input);

		// try to get as many numbers as needed
		for (int i = 0; i < inputNums.length; i++) {
			// if a number is found, then try to parse it and add it to inputNums
			if (inputMatcher.find()) {
				// If number cannot be parsed, it will throw a NumberFormatException
				try {
					inputNums[i] = Integer.parseInt(inputMatcher.group());
				} catch (NumberFormatException e) {
					return null;
				}
			} else {
				return null;
			}
		}
		return inputNums;
	}

	/*
	 * Determines if the given input numbers correspond to valid and flippable
	 * cards.
	 */
	private boolean inputIsValid(int[] inputNums) {
		if (inputNums == null || inputNums.length != INPUT_NUMS_SIZE) {
			// input cannot be sanitized
			printInputError(INVALID_INPUT_MESSAGE);
			return false;
		} else {
			Card card1 = board.getCard(inputNums[0], inputNums[1]);
			Card card2 = board.getCard(inputNums[2], inputNums[3]);

			// if cards aren't the same
			if (inputNums[0] == inputNums[2] && inputNums[1] == inputNums[3]) {
				// at least one card is already paired
				printInputError(DUPLICATE_CARD_MESSAGE);
				return false;
			} else if (card1 == null || card2 == null) {
				// positions not on board
				printInputError(OUT_OF_BOUNDS_MESSAGE);
				return false;
			} else if (card1.isPaired() || card2.isPaired()) {
				// positions are the same
				printInputError(ALREADY_PAIRED_MESSAGE);
				return false;
			}
		}
		return true;
	}

	/*
	 * Flips the given cards, if possible
	 */
	private void flipCards(int[] inputNums) {
		if (inputIsValid(inputNums)) {
			Card card1 = board.getCard(inputNums[0], inputNums[1]);
			Card card2 = board.getCard(inputNums[2], inputNums[3]);

			// hide previously flipped cards
			board.hideCards();

			// flip given cards
			card1.setFlipped(true);
			card2.setFlipped(true);

			if (!simulating) {
				board.update();

				// display message
				System.out.printf("\n%s flipping cards at (%d, %d) and (%d, %d)...\n", currentPlayer.getName(),
						inputNums[0], inputNums[1], inputNums[2], inputNums[3]);
			}

			// if cards match
			if (card1.equals(card2)) {
				// pair cards
				card1.setPaired(true);
				card2.setPaired(true);
				// increment matches
				numMatches++;
				currentPlayer.addMatch();
				matchFound = true;
			}

			// increment moves
			numMoves++;
			currentPlayer.addMove();
			validMove = true;
		}
	}

	/*
	 * Prints an input error with the specified messsage
	 */
	private void printInputError(String message) {
		System.out.printf("\n%s\n%s\n%s\n\n", message, INPUT_SUGGESTION, INPUT_EXAMPLE);
	}

	/*
	 * Tells user if match was made or shows instructions
	 */
	private void printMatchMessage() {
		// tell user if they made a match or not
		if (matchFound) {
			System.out.println(currentPlayer.getName() + " " + MATCH_MADE_MESSAGE);
			matchFound = false;
		} else if (numMoves > 0) {
			System.out.println(players.length == 1 ? SINGLE_PLAYER_NO_MATCH_MESSAGE : MULTIPLAYER_NO_MATCH_MESSAGE);
		}

		// print instructions on how to play game
		if (numMoves == 0 && currentPlayer.isHuman()) {
			System.out.printf("%s\n%s\n", INPUT_INSTRUCTIONS, INPUT_EXAMPLE);
		}
	}

	/*
	 * Returns the average of the spreads
	 */
	private double getAverageOfSpreads() {
		double sum = 0;
		for (int spread : spreads.keySet()) {
			sum += spread * spreads.get(spread);
		}
		return sum / numSimulations;
	}

	/*
	 * Returns the standard deviation of the spreads
	 */
	private double getStandardDeviationOfSpreads() {
		Integer[] spreadsArray = spreads.keySet().toArray(new Integer[spreads.keySet().size()]);
		Integer[] countsArray = spreads.values().toArray(new Integer[spreads.values().size()]);
		double mean = getAverageOfSpreads();
		double squaredDeviationSum = 0;
		for (int i = 0; i < spreadsArray.length; i++) {
			squaredDeviationSum += countsArray[i] * Math.pow(spreadsArray[i] - mean, 2);
		}
		return Math.sqrt(squaredDeviationSum / numSimulations);
	}

	/*
	 * Displays data about the spreads of the simulation
	 */
	private void displaySimulationStatistics() {
		System.out.println("Spread, Count");

		// get array of spreads and sort it
		Integer[] spreadKeys = spreads.keySet().toArray(new Integer[spreads.keySet().size()]);
		Arrays.sort(spreadKeys);

		// print out the spreads with their counts in ascending order
		for (int spread : spreadKeys) {
			System.out.printf("%d, %d\n", spread, spreads.get(spread));
		}
		System.out.printf("\nAverage Spread: %f\n", getAverageOfSpreads());
		System.out.printf("Standard Deviation: %f\n\n", getStandardDeviationOfSpreads());
	}

	/*
	 * Displays data about the players
	 */
	void displayPlayerStatistics(boolean gameCompleted) {
		if (gameCompleted) {
			int maxMatches = -1;
			// Find winning players
			ArrayList<Player> winningPlayers = new ArrayList<Player>();
			for (Player player : players) {
				if (player.getMatches() > maxMatches) {
					winningPlayers.clear();
					winningPlayers.add(player);
					maxMatches = player.getMatches();
				} else if (player.getMatches() == maxMatches) {
					winningPlayers.add(player);
				}
			}
			// if there is one winner
			if (winningPlayers.size() == 1) {
				System.out.println(
						gameCompleted ? winningPlayers.get(0).getName() + " " + GAME_WON_MESSAGE : GAME_OVER_MESSAGE);
			} else {
				// if there is a tie
				System.out.print("There is a tie between ");
				Player lastPlayer = winningPlayers.remove(winningPlayers.size() - 1);
				if (winningPlayers.size() > 1) {
					for (Player player : winningPlayers) {
						System.out.print(player.getName() + ", ");
					}
				} else {
					// if only two players tied, forget the comma
					System.out.print(winningPlayers.get(0).getName() + " ");
				}
				System.out.printf("and %s.\n", lastPlayer.getName());
			}
		}
		// display data about player's moves
		for (Player player : players) {
			// grammar check
			String moveString = player.getMoves() == 1 ? "move" : "moves";
			String matchString = player.getMatches() == 1 ? "match" : "matches";

			// print ending message
			System.out.printf("%s made %d %s and %d %s.\n", player.getName(), player.getMoves(), moveString,
					player.getMatches(), matchString);
		}
	}

	/*
	 * Called after game is won or quit. Prompts the user to play again
	 */
	private void endGame(boolean gameCompleted) {

		// if in simulation, print out simulation Statistics
		if (simulating) {
			displaySimulationStatistics();
		} else {
			// display the board one last time
			System.out.println(board);
			displayPlayerStatistics(gameCompleted);
		}

		// ask to play again. If yes, restart game. If no, quit.
		if (gameCompleted) {
			askForReplay();
		} else {
			// player chose to quit, exit the program
			exitGame();
		}
	}

	/*
	 * Asks the user to play again or quit
	 */
	private void askForReplay() {
		System.out.println(REPLAY_PROMPT);
		String answer = "";
		while (!(answer.equals(AFFIRMATIVE_RESPONSE.toLowerCase()) || answer.equals(NEGATIVE_RESPONSE.toLowerCase()))) {
			System.out.print(ANSWER_PROMPT);
			answer = scanner.nextLine().toLowerCase();
		}

		// If yes, start again
		if (answer.equals(AFFIRMATIVE_RESPONSE)) {
			start();
		} else {
			// exit the program
			exitGame();
		}
	}

	/*
	 * exits the game
	 */
	private void exitGame() {
		scanner.close();
		System.exit(0);
	}
}
