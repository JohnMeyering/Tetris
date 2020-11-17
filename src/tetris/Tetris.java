package tetris;

import java.io.File;
import java.io.FileInputStream;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.media.AudioClip;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

/**
 * Generates UI and provides methods for modifying UI.
 * @author John Meyering
 *
 */
public class Tetris extends Application {
	
	public static Pane root;
	public static Scene scene;
	public static AudioClip musicClip;
	
	public static Font font;
	public static Color red;
	public static Color blue;
	public static Color grey;
	
	public static Label score;
	public static Tile[][] tiles;
	public static CustomButton newGame;
	public static CustomButton leaderboard;
	
	GridPane playArea;
	public static Leaderboard lb;
	public static boolean gameStarted;
	
	public static void main(String[] args) {
		launch(args);
	}
	@Override
	public void start(Stage stage) {
		stage.setTitle("Tetris");
		root = new Pane();
		scene = new Scene(root, 600, 620);
		stage.setScene(scene);
		stage.show();
		
		stage.setResizable(false);
		stage.getIcons().add(new Image("file:icon.png"));
		
		setStyles();
		drawGUI();
		
		Controller controller = new Controller();
		controller.start();
	}
	
	public void setStyles() {
		root.setStyle("-fx-background-color: #751a9cff");
		Tetris.font = new Font("sans-serif", 32);
		Tetris.red = new Color(192.0/255, 0/255, 0/255, 1.0);
		Tetris.blue = new Color(27.0/255, 26.0/255, 156.0/255, 1);
		Tetris.grey = new Color(36.0/255, 28.0/255, 28.0/255, 1);
	}
	
	public void drawGUI() {
		lb = null;
		gameStarted = false;
		drawLogo();
		drawScoreArea();
		drawButtonArea();
		drawControlsArea();
		drawPlayArea();
	}
	
	public void drawLogo() {
		// https://www.tutorialspoint.com/javafx/javafx_images.htm   (htm not html, idk why)
		// Loading the file into a usable element (ImageView)
		FileInputStream inputStream = null;
		try {
			inputStream = new FileInputStream("logo.png");
		} catch(Exception e) {
			System.out.println(e.getMessage() + " from drawLogo()");
		}
		Image image = new Image(inputStream);
		ImageView imageView = new ImageView(image);
		
		// styling
		imageView.setX(330);
		imageView.setY(25);
		
		imageView.setFitHeight(83.252);
		imageView.setFitWidth(250);
		
		imageView.setPreserveRatio(true);
		
		root.getChildren().add(imageView);
	}
	
	/**
	 * Draws Background boxes for sub-areas (Not the application's background)
	 * @param width
	 * @param height
	 * @param x
	 * @param y
	 */
	public void drawBackground(double width, double height, double x, double y) {
		Rectangle rect = new Rectangle();
		rect.setWidth(width);
		rect.setHeight(height);
		rect.setFill( Tetris.grey );
		rect.setStroke(Color.WHITE);
		rect.setX(x);
		rect.setY(y);
		root.getChildren().add(rect);
	}
	
	/**
	 * Sets the `score` label object. Draws the scoring UI.
	 */
	public void drawScoreArea() {
		drawBackground(250, 105, 330, 133);
		
		// 'Score: '
		Label scoreLabel = new Label("Score:");
		scoreLabel.setFont(font);
		scoreLabel.setTextFill(Color.WHITE);
		scoreLabel.setLayoutX(345);
		scoreLabel.setLayoutY(160); // javafx is bad, -10 from inkscape y value
		root.getChildren().add(scoreLabel);
		
		// the score value
		score = new Label("0");
		score.setFont(font);
		score.setTextFill(Color.WHITE);
		score.setLayoutX(450);
		score.setLayoutY(160); // same as scoreLabel, javafx is bad with labels for some reason
		root.getChildren().add(score);
	}
	
	/**
	 *  Sets the `newGame` and `leaderboard` buttons.
	 *  Draws the clickable UI.
	 *  Does not setup onclicks.
	 */
	public void drawButtonArea() {
		drawBackground(250, 105, 330, 253);
		
		newGame = new CustomButton("New Game");
		newGame.setBG(368, 265, 177, 37);
		newGame.setLabel(370, 269);
		root.getChildren().add(newGame);
		
		leaderboard = new CustomButton("Leaderboard");
		leaderboard.setBG(350, 308, 210, 37);
		leaderboard.setLabel(358, 312);
		root.getChildren().add(leaderboard);
	}
	
	/**
	 * Draws the controls explanation UI.
	 */
	public void drawControlsArea() {
		drawBackground(250, 235, 330, 375);
		
		// Label
		Label title = new Label("CONTROLS");
		title.setFont(font);
		title.setTextFill(Color.WHITE);
		title.setLayoutX(375);
		title.setLayoutY(380); // javafx bad, -10 from inkscape y value
		root.getChildren().add(title);
		
		// Details
		Label controls = new Label(
				  "A: move left\n"
				+ "D: move right\n"
				+ "W: rotate right\n"
				+ "S: soft drop");
		controls.setTextAlignment(TextAlignment.CENTER);
		controls.setFont(font);
		controls.setTextFill(Color.WHITE);
		controls.setLayoutX(350);
		controls.setLayoutY(420); // javafx bad
		root.getChildren().add(controls);
	}
	
	/**
	 * Draws the play space UI.
	 */
	public void drawPlayArea() {
		playArea = new GridPane();
		playArea.setStyle("-fx-background-color: black"); // never actually seen
		playArea.setLayoutY(10);
		playArea.setLayoutX(10);
		playArea.setMinWidth(200);
		playArea.setMinHeight(200);
		root.getChildren().add(playArea);
		
		tiles = new Tile[10][20];
		
		for(int x=0; x < 10; ++x) {
			for(int y=0; y < 20; ++y) {
				Tile tile = new Tile(x, y);
				tiles[x][y] = tile;
				playArea.add(tile, x, y);
			}
		}
	}
	
	/**
	 * Blacks out all tiles in the Play Area
	 */
	public static void clearPlayArea() {
		for(int x=0; x < 10; ++x) {
			for(int y=0; y < 20; ++y) {
				tiles[x][y].blackOut();
			}
		}
	}
	
	/**
	 * Set the displayed score.
	 * @param newScore
	 */
	public static void setScore(int newScore) {	
		score.setText(Integer.toString(newScore));
	}
	
	/**
	 * Break the designated row.
	 */
	public static void breakRow(int y) {
		for(int x=0; x < 10; ++x) {
			tiles[x][y].whiteOut();
		}
	}
	
	/**
	 * Resets row y to black.
	 */
	public static void resetRow(int y) {
		for(int x=0; x < 10; ++x) {
			tiles[x][y].blackOut();
		}
	}
	
	/**
	 * Drop all rows above row y down by numRows
	 */
	public static void dropHangingRows(int yValue, int numRows) {
		// Start at the row above the specified row and work upwards
		for(int y=yValue-1; y > 0; --y) {
			for(int x=0; x < 10; ++x) {
				if(tiles[x][y].isTaken()) {
					Color tileColor = tiles[x][y].getColor();
					tiles[x][y].blackOut();
					tiles[x][y+numRows].setColor(tileColor);
				}
			}
		}
	}
	
	/**
	 * Makes a leaderboard if there isn't one-
	 * Destroys the leaderboard if there is one.
	 */
	public static void toggleLeaderboard() {
		if(lb == null) {
			if(gameStarted) {
				Controller.driver.stop();
			}
			lb = new Leaderboard();
			root.getChildren().add(lb);
		}
		else {
			root.getChildren().remove(lb);
			lb = null;
			if(gameStarted) {
				Controller.driver.firsttime = true;
				Controller.driver.start();
			}
			
		}
		
	}
	
	/**
	 * Starts the music!
	 */
	public static void startMusic() {
		// https://upload.wikimedia.org/wikipedia/commons/e/e5/Tetris_theme.ogg.mp3
		String filename = "Tetris_theme.mp3";
		
		File musicFile = new File(filename);
		String uriString = musicFile.toURI().toString();
		musicClip = new AudioClip(uriString);
		musicClip.setVolume(0.1);
		
		musicClip.play();
	}
}
