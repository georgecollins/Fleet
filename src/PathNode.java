
public class PathNode {

	/**
	 *   node of the path data that is saved in an array in the IslandManager
	 *   
	 *   is this node clear?  What directions can I go?
	 *   
	 */
	int clear;
	int passable[];
	static final int MAX_DIRECTIONS = 8;
	static final int NORTH_EAST = 0;
	static final int NORTH = 1;
	static final int NORTH_WEST =2;
	static final int EAST = 3;
	static final int WEST = 4;
	static final int SOUTH_EAST = 5;
	static final int SOUTH = 6;
	static final int SOUTH_WEST = 7;
	PathNode() {
		
	}

}
