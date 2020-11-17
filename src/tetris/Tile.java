package tetris;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

/**
 * The basic unit of the Play Area.
 * @author John Meyering
 *
 */
public class Tile extends StackPane {
	public static Random rand = new Random();
	
	Rectangle rect;
	int x; // <x,y> of the tile in the Tetris.tiles array
	int y; // 
	boolean isTaken;
	
	Color color;
	
	public Tile(int xVal, int yVal) {
		this.x = xVal;
		this.y = yVal;
		
		setMinHeight(30);
		setMinWidth(30);
		rect = new Rectangle(28, 28);
		rect.setFill(new Color(rand.nextDouble(), rand.nextDouble(), rand.nextDouble(), 1.0));
		getChildren().add(rect);
	}
	
	public void blackOut() {
		rect.setFill(Color.BLACK);
		color = Color.BLACK;
		isTaken = false;
	}
	public void whiteOut() {
		rect.setFill(Color.WHITE);
		color = Color.WHITE;
		isTaken = false;
	}
	public void setColor(Color color) {
		rect.setFill(color);
		this.color = color;
		isTaken = true;
	}
	public Color getColor() {
		return color;
	}
	
	public void paintNeighbors() {
		List<Tile> neibs = new ArrayList<>();
		neibs.add(getN());
		neibs.add(getS());
		neibs.add(getE());
		neibs.add(getW());
		neibs.add(getNE());
		neibs.add(getNW());
		neibs.add(getSE());
		neibs.add(getSW());
		
		for(Tile t: neibs) {
			t.setColor(Color.WHITE);
		}
	}
	
	// Functions for getting neighboring tiles
	// They either return a neighbor tile, or null (if none)
	// Straight directions
	public Tile getN() {
		Tile neighbor = null;
		
		int neibX = x;
		int neibY = aboveIndex();
		if(inRange(neibX, neibY)) {
			neighbor = Tetris.tiles[neibX][neibY];
		}
		
		return neighbor;
	}
	public Tile getS() {
		Tile neighbor = null;
		
		int neibX = x;
		int neibY = belowIndex();
		if(inRange(neibX, neibY)) {
			neighbor = Tetris.tiles[neibX][neibY];
		}
		
		return neighbor;
	}
	public Tile getE() {
		Tile neighbor = null;
		
		int neibX = rightIndex();
		int neibY = y;
		if(inRange(neibX, neibY)) {
			neighbor = Tetris.tiles[neibX][neibY];
		}
		
		return neighbor;
	}
	public Tile getW() {
		Tile neighbor = null;
		
		int neibX = leftIndex();
		int neibY = y;
		if(inRange(neibX, neibY)) {
			neighbor = Tetris.tiles[neibX][neibY];
		}
		
		return neighbor;
	}
	// Angled directions
	public Tile getNE() {
		Tile neighbor = null;

		int neibX = rightIndex();
		int neibY = aboveIndex();
		if(inRange(neibX, neibY)) {
			neighbor = Tetris.tiles[neibX][neibY];
		}
		
		return neighbor;
	}
	public Tile getNW() {
		Tile neighbor = null;

		int neibX = leftIndex();
		int neibY = aboveIndex();
		if(inRange(neibX, neibY)) {
			neighbor = Tetris.tiles[neibX][neibY];
		}
		
		return neighbor;
	}
	public Tile getSE() {
		Tile neighbor = null;

		int neibX = rightIndex();
		int neibY = belowIndex();
		if(inRange(neibX, neibY)) {
			neighbor = Tetris.tiles[neibX][neibY];
		}
		
		return neighbor;
	}
	public Tile getSW() {
		Tile neighbor = null;

		int neibX = leftIndex();
		int neibY = belowIndex();
		if(inRange(neibX, neibY)) {
			neighbor = Tetris.tiles[neibX][neibY];
		}
		
		return neighbor;
	}
	
	// Helpers for direction functions
	// Don't do much, but if you wanted to add in wrapping, then this would help
	public int leftIndex() {
		int left = x - 1;
		return left;
	}
	public int rightIndex() {
		int right = x + 1;
		return right;
	}
	public int aboveIndex() {
		int up = y - 1;
		return up;
	}
	public int belowIndex() {
		int down = y + 1;
		return down;
	}
	
	/**
	 * Return true if <x,y> is in the tile array
	 */
	public static boolean inRange(int x, int y) {
		if(x > -1 && x < 10 && y > -1 && y < 20) {
			return true;
		}
		else {
			return false;
		}
	}
	
	public boolean isTaken() {
		return isTaken;
	}
	
	/*
	 * Returns the tile that THIS tile would rotate to when
	 * rotating 90 degrees around the given pivot coordinates
	 */
	public Tile getRotationTargetTile(int pivotX, int pivotY) {
		Tile target = null;
		
		int xDiff = x - pivotX;
		int yDiff = y - pivotY;
		
		// swap x&y, negate x (refer to notebook if you must)
		int targetX = pivotX - yDiff;
		int targetY = pivotY + xDiff;
		
		if(inRange(targetX, targetY)) {
			target = Tetris.tiles[targetX][targetY];
		}
		
		return target;
	}
}
