package tetris;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.Semaphore;

import javafx.animation.AnimationTimer;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;

/**
 * Holds the game logic and performs operations on tetriminos.
 * Defines all input logic. (onclick, key, etc.)
 * Calls `Tetris` methods to update UI.
 * @author John Meyering
 *
 */
public class Controller {
	
	Random rand;
	Semaphore dataAccess; // used by update and handlers
	
	// data that is set by handlers and consumed by update
	int numRotates; 
	int xChange;
	boolean hardDrop;
	Set<Integer> breakRows;
	
	String gameState;
	public static Driver driver;
	Block currentBlock;
	
	boolean softDrop; // when true, we drop twice as fast
	double lowerTimeSoft; // time per lowering block by 1 unit in softDrop
	double fallingTimer; // updated when in "Falling" state
	double lowerTimeNormal; // time per lowering block by 1 unit
	
	double breakTimer; // updated when in the "Break Handler" state
	double breakTime;
	boolean breakShown;
	
	public Controller() {
		rand = new Random();
		
		gameState = "Waiting";
		
		dataAccess = new Semaphore(1);
		
		Tetris.startMusic();
	}
	
	/**
	 * Get the environment ready so we can play.
	 * (Setup the driver, setup event handlers, etc.)
	 */
	public void start() {
		// Driver
		driver = new Driver();
		
		// New Game
		Tetris.newGame.setOnMouseClicked((MouseEvent event) -> {
			startGame();
		});
		
		// Leaderboard
		Tetris.leaderboard.setOnMouseClicked((MouseEvent event) -> {
			Tetris.toggleLeaderboard();
		});
		
		// Keyboard Controls
		Tetris.scene.setOnKeyPressed( (KeyEvent event) -> {
			try { dataAccess.acquire(); } catch(Exception e) { System.out.println(e.getMessage());}
			
			switch(event.getCode()) {
			case W:
				++numRotates;
				break;
			case S:
				softDrop = true;
				break;
			case A:
				--xChange;
				break;
			case D:
				++xChange;
				break;
			case Q:
				startGame();
				break;
			case SPACE:
				hardDrop = true;
				break;
			case E:
				Tetris.toggleLeaderboard();
			}
			
			dataAccess.release();
		});
		Tetris.scene.setOnKeyReleased( (KeyEvent event) -> {
			try { dataAccess.acquire(); } catch(Exception e) { System.out.println(e.getMessage());}
			if(event.getCode() == KeyCode.S) {
				softDrop = false;
			}
			dataAccess.release();
		});
	}
	
	/**
	 * Clear the space and initialize variables
	 */
	public void startGame() {
		Tetris.clearPlayArea();
		Tetris.gameStarted = true;
		
		gameState = "Waiting";
		softDrop = false;
		lowerTimeNormal = 1.2; // 1.2 seconds per lower as a baseline (updates on break) (if I decide to)
		lowerTimeSoft = lowerTimeNormal / 8;
		breakTime = 0.2; // This probably shouldn't get faster (would mess with satisfaction)
		numRotates = 0;
		xChange = 0;
		hardDrop = false;
		driver.start();
	}
	
	/**
	 * Take one step into the future!
	 */
	public void update(double deltaT) {
		// Keep the music going!
		if(!Tetris.musicClip.isPlaying()) {
			Tetris.startMusic();
		}
		
		try { dataAccess.acquire(); } catch(Exception e) {
			System.out.println(e.getMessage() + " from dataAccess.acquire()");
		}
		
		// Set the time limit based on user input
		double lowerTimeChosen = 0;
		if(softDrop) {
			lowerTimeChosen = lowerTimeSoft;
		}
		else {
			lowerTimeChosen = lowerTimeNormal;
		}
		
		if(gameState.equals("Waiting")) {
			startNewBlock();
			if(currentBlock.canLower()) {
				gameState = "Falling";
			}
			else {
				/* If we just spawned a block but can't lower it,
				   then the game must be over. */
				gameState = "Game Over";
				int finalScore = Integer.parseInt(Tetris.score.getText());
				saveScore(finalScore);
			}
			
			fallingTimer = 0;
		}
		else if(gameState.equals("Falling")) {
			fallingTimer += deltaT;
			
			// input handling
			consumeInputs();
			
			if(fallingTimer > lowerTimeChosen) {
				fallingTimer -= lowerTimeChosen;
				
				if(currentBlock.canLower()) {
					currentBlock.lower();
				}
				else {
					gameState = "Break Check";
				}
			}
		}
		else if(gameState.equals("Break Check")) {
			/* Every line for a break, if there is one, increment
			 * the brink counter and check again. */
			breakCheck();
			gameState = "Break Handler";
			breakTimer = 0;
			breakShown = false;
		}
		else if(gameState.equals("Break Handler")) {
			if(breakRows.isEmpty()) {
				// There weren't any breaks, move on
				gameState = "Waiting";
			}
			else {
				// There were breaks, we need to display them
				breakTimer += deltaT;
				if(breakTimer < breakTime) {
					if(!breakShown) {
						for(Integer y: breakRows) {
							Tetris.breakRow(y);
						}
						int score = determineScore();
						int oldScore = Integer.parseInt(Tetris.score.getText());
						Tetris.setScore(oldScore + score);
						breakShown = true;
					}
				}
				else {
					// Move on if enough time has passed
					// i.e. drop the tiles that were above the breaks
					// and check for more breaks
					for(Integer y: breakRows) {
						Tetris.resetRow(y);
					}
					List<Integer> breakRowsList = new ArrayList<Integer>(breakRows);
					Collections.sort(breakRowsList);
					for(Integer y: breakRowsList) {
						// Drop items above each row, one row at a time
						Tetris.dropHangingRows(y, 1);
					}
					
					gameState = "Break Check";
				}
			}
			
		}
		
		dataAccess.release();
	}
	
	/**
	 * Make a random new block and display it in the starting position.
	 */
	public void startNewBlock() {
		// https://stackoverflow.com/questions/1972392/pick-a-random-value-from-an-enum
		int pick = rand.nextInt(BlockType.values().length);
		currentBlock = new Block(BlockType.values()[pick]);
		currentBlock.paintTiles();
	}
	
	/**
	 * Perform operations designated by user inputs.
	 * Zeroes out input variables.
	 */
	public void consumeInputs() {
		while(numRotates > 0) {
			currentBlock.rotate();
			--numRotates;
		}
		if(xChange > 0) {
			while(xChange > 0) {
				currentBlock.moveRight();
				--xChange;
			}
		}
		else if(xChange < 0) {
			while(xChange < 0) {
				currentBlock.moveLeft();
				++xChange;
			}
		}
		
		if(hardDrop) {
			currentBlock.hardDrop();
			fallingTimer = lowerTimeNormal - 0.35;
			hardDrop = false;
		}
	}
	
	/**
	 *  Checks for breaks and sets the breakRows set.
	 */
	public void breakCheck() {
		/* Go through each row 
		 * if the row is entirely taken, then we need to break it*/
		breakRows = new HashSet<Integer>();
		
		for(int y=0; y < 20; ++y) {
			// Check if the row needs to be broken
			boolean rowNeedsBreak = true;
			for(int x=0; x < 10; ++x) {
				if(!Tetris.tiles[x][y].isTaken()) {
					rowNeedsBreak = false;
					break;
				}
			}
			// If it does, then remember that
			if(rowNeedsBreak) {
				breakRows.add(y);
			}
		}
	}
	
	/**
	 * Scores the user's broken rows
	 */
	public int determineScore() {
		int score = 0;
		// https://tetris.wiki/Scoring
		if(breakRows.size() == 1) {
			// Single
			score = 100;
		}
		else if(breakRows.size() == 2) {
			// Double
			score = 300;
		}
		else if(breakRows.size() == 3) {
			// Triple
			score = 500;
		}
		else if(breakRows.size() == 4) {
			// Tetris
			score = 800;
		}
		return score;
	}
	
	/**
	 * Saves the user's score in scores.txt
	 */
	public void saveScore(int score) {
		LocalDateTime time = LocalDateTime.now();
		
		String year = Integer.toString(time.getYear());
		String month = Integer.toString(time.getMonthValue());
		String day = Integer.toString(time.getDayOfMonth());
		
		String hour = Integer.toString(((time.getHour() % 12) + 1));
		String minute = Integer.toString(time.getMinute());
		if(hour.length() == 1) {
			hour = "0" + hour;
		}
		if(minute.length() == 1) {
			minute = "0" + minute;
		}
		
		String xm = (time.getHour() / 12) < 1 ? "AM" : "PM";
		
		String date = year + "-" + month + "-" + day + "::" + hour + ":" + minute + xm;
		String string = date + " " + score + "\n";
		try {
			BufferedWriter out = new BufferedWriter(
					new FileWriter("scores.txt", true));
			out.write(string);
			out.close();
			
		} catch(Exception e) {
			System.out.println(e.getMessage() + " occured in saveScore()");
		}
	}
	
	/**
	 * Does what you think it does
	 * @author John Meyering
	 *
	 */
	public class Driver extends AnimationTimer {
		long lasttime;
		boolean firsttime = true;
		
		@Override
		public void handle(long now) {
			if( firsttime ) { lasttime = now; firsttime = false; }
			else {
				double deltat = (now-lasttime) * 1.0e-9;
				lasttime = now;
				update(deltat);
			}
		}
	}
}
