package tetris;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javafx.scene.paint.Color;

/**
 * The bookkeeping for tetriminos.
 * I refuse to type tetrimino again, so `Block` will do.
 * @author John Meyering
 *
 */
public class Block {
	Color color;
	BlockType blockType;
	List<Tile> tiles; // tile[0] is the base tile
	
	public Block(BlockType type) {
		blockType = type;
		defineColor();
		defineTiles();
	}
	
	/**
	 * Sets the color of the block
	 */
	public void defineColor() {
		switch(blockType) {
		case I:
			color = Color.CYAN;
			break;
		case O:
			color = Color.YELLOW;
			break;
		case T:
			color = Color.PURPLE;
			break;
		case S:
			color = Color.GREEN;
			break;
		case Z:
			color = Color.RED;
			break;
		case J:
			color = Color.BLUE;
			break;
		case L:
			color = Color.ORANGE;
			break;
		}
	}
	
	/**
	 * Sets the owned tiles of the block
	 */
	public void defineTiles() {
		tiles = new ArrayList<>();
		Tile baseTile = Tetris.tiles[4][0];
		tiles.add(baseTile);
		
		switch(blockType) {
		case I:
			tiles.add(Tetris.tiles[3][0]);
			tiles.add(Tetris.tiles[5][0]);
			tiles.add(Tetris.tiles[6][0]);
			break;
		case O:
			tiles.add(Tetris.tiles[4][1]);
			tiles.add(Tetris.tiles[5][0]);
			tiles.add(Tetris.tiles[5][1]);
			break;
		case T:
			tiles.add(Tetris.tiles[3][0]);
			tiles.add(Tetris.tiles[5][0]);
			tiles.add(Tetris.tiles[4][1]);
			break;
		case S:
			tiles.add(Tetris.tiles[3][1]);
			tiles.add(Tetris.tiles[4][1]);
			tiles.add(Tetris.tiles[5][0]);
			break;
		case Z:
			tiles.add(Tetris.tiles[3][0]);
			tiles.add(Tetris.tiles[4][1]);
			tiles.add(Tetris.tiles[5][1]);
			break;
		case J:
			tiles.add(Tetris.tiles[3][0]);
			tiles.add(Tetris.tiles[5][0]);
			tiles.add(Tetris.tiles[5][1]);
			break;
		case L:
			tiles.add(Tetris.tiles[3][0]);
			tiles.add(Tetris.tiles[3][1]);
			tiles.add(Tetris.tiles[5][0]);
			break;
		}
	}
	
	/**
	 * Changes member tiles to display the color of this block
	 */
	public void paintTiles() {
		for(Tile t: tiles) {
			t.setColor(color);
		}
	}
	
	/**
	 * Blackout all member tiles
	 */
	public void blackOut() {
		for(Tile t: tiles) {
			t.blackOut();
		}
	}
	
	/**
	 * Tells you if lowering the block is legal
	 */
	public boolean canLower() {
		boolean canLower = true;
		
		for(Tile t: tiles) {
			Tile lowerTile = t.getS();
			if(lowerTile == null) {
				canLower = false;
				break;
			}
			if(lowerTile.isTaken()) {
				/* it's OK if the lowerTile is taken, as long
				   as it's taken by THIS block */
				if(!tiles.contains(lowerTile)) {
					canLower = false;
					break;
				}
			}
		}
		
		return canLower;
	}
	
	/**
	 * Drops the block 1 increment
	 */
	public void lower() {
		blackOut();
		
		for(int i=0; i < 4; ++i) {
			// for each tile, swap it with the one beneath
			Tile current = tiles.get(i);
			Tile next = current.getS();
			tiles.set(i, next);
		}
		
		paintTiles();
	}
	
	/**
	 * Drops the block all the way down
	 */
	public void hardDrop() {
		while(canLower()) {
			lower();
		}
	}
	
	/**
	 * Rotate the block 90 degrees.
	 * If rotating 90 degrees isn't possible, rotate again.
	 */
	public void rotate() {
		// Only bother if the block isn't an O
		if(blockType != BlockType.O) {
			List<Tile> newTiles = new ArrayList<>();
			
			// Use the baseTile as a pivot
			Tile baseTile = tiles.get(0);
			newTiles.add(baseTile);
			
			for(int i=1; i < 4; ++i) {
				Tile curr = tiles.get(i);
				Tile targetTile = curr.getRotationTargetTile(baseTile.x, baseTile.y);
				if(targetTile != null) {
					if(!targetTile.isTaken()) {
						newTiles.add(targetTile);
					}
					else {
						// if the tile is taken by one of our blocks, it's still OK
						if(tiles.contains(targetTile)) {
							newTiles.add(targetTile);
						}
					}
				}
			}
			//if we can rotate all of the tiles, then do it
			if(newTiles.size() == 4) {
				blackOut();
				tiles = newTiles;
				paintTiles();
			}
		}
	}
	
	/**
	 * Move the block left if it is possible to do so
	 */
	public void moveLeft() {
		move("left");
	}
	
	/**
	 * Move the block right if it is possible to do so
	 */
	public void moveRight() {
		move("right");
	}
	private void move(String direction) {
		List<Tile> newTiles = new ArrayList<>();
		
		// Check to the `direction` of each of our current tiles
		for(Tile t: tiles) {
			Tile adjacentTile = null;
			if(direction.equals("left")) {
				adjacentTile = t.getW();
			}
			else {
				adjacentTile = t.getE();
			}
			
			if(adjacentTile != null) {
				if(!adjacentTile.isTaken()) {
					newTiles.add(adjacentTile);
				}
				else {
					// if the tile is taken by one of our blocks, it's ok
					if(tiles.contains(adjacentTile)) {
						newTiles.add(adjacentTile);
					}
				}
			}
		}
		// If our new block would be of size 4, then we move
		if(newTiles.size() == 4) {
			blackOut();
			tiles = newTiles;
			paintTiles();
		}
	}
	
	/**
	 *  Provides a set of the x coordinates belonging to this block
	 */
	public Set<Integer> getXValues() {
		Set<Integer> xValues = new HashSet<Integer>();
		
		for(Tile t: tiles) {
			xValues.add(t.x);
		}
		
		return xValues;
	}
}
