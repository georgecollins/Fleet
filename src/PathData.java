
public class PathData {


	/**
	 *   node of the path data that is saved in an array in the IslandManager
	 *   
	 *   is this node clear?  What directions can I go?
	 *   
	 *   maybe this should extend world thing?
	 *   
	 */
	int clear;
	int passable[];
	PathData neighbors[];
	IslandManager im;
	double x;
	double y;
	
	double bestdist;
	int bestdir;
	PathData bestfrom;  // best path data I came from
	
	static final int PASSABLE = 1;
	static final int IMPASSABLE = 2;
	
	static final int MAX_DIRECTIONS = 8;
	static final int NORTH_EAST = 0;
	static final int NORTH = 1;
	static final int NORTH_WEST =2;
	static final int EAST = 3;
	static final int WEST = 4;
	static final int SOUTH_EAST = 5;
	static final int SOUTH = 6;
	static final int SOUTH_WEST = 7;
	
	
	public void IsClear() {
		// start from two before my x, two before my y
		// check the map in MAP_SCALE increments
		// if EVERYTHING is clear then you can call this 
		// node 'clear'
		clear = PASSABLE;
		for (int i=-im.PATH_WIDTH/2; i < im.PATH_WIDTH+1; i++)
			for (int j=-im.PATH_WIDTH/2; j < im.PATH_WIDTH+1; j++) {
				if (im.MapValue(x+i*im.MAP_SCALE, y + j * im.MAP_SCALE)!=0)
					clear=IMPASSABLE; // was impassable, as a test
			}
	}
	// can I go in the direction -1 0 1 x/y
	public int ClearDirection(int dirx, int diry) { 
		double x2 = x + dirx * im.MAP_SCALE*im.PATH_WIDTH;
		double y2 = y + diry * im.MAP_SCALE*im.PATH_WIDTH;
	
		// the line test sometimes seems to be wrong, but the map
		// array test does not
		// Is this because the world position of islands aren't set up correctly?
		// Or there is a bug in LineCollides?  
		// It needs to be checked
		
		
		if (im.LineCollides(x, y,x2, y2)) 
			return IMPASSABLE;
		
		return PASSABLE;
	}
	
	public double distsq(PathData pn) {
		return (x-pn.x)*(x-pn.x)+(y-pn.y)*(y-pn.y);
		
	}
	public void SetXY(double xin, double yin) {
		x=xin;
		y=yin;
	}
	
	
	
	// I need a constructor 
	PathData() {
			
		
	}
	
	PathData(IslandManager imin) {
		im = imin;
		passable = new int[MAX_DIRECTIONS];
		neighbors =new PathData[MAX_DIRECTIONS];
		for (int i=0; i < MAX_DIRECTIONS; i++) {
			passable[i] = PASSABLE;
			neighbors[i]=null;
		}
		clear = IMPASSABLE;
	}

}
