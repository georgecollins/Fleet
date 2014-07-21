
public class Episode {
	int state;
	int difficulty;
	IslandManager imm[][];
	ShipManager smm[][];
	MissionData md;
	UserInput ui;
	WorldThing objective;
	
	boolean mission_message;
	
	String title;
	String description;
	int bonus;
	boolean completed; // have I ever done this mission
	
	static final int SETUP_STATE = 1;
	static final int MESSAGE_STATE = 2;
	static final int WAIT_FOR_PLAYER_STATE =3;
	static final int REMINDER_STATE  = 4;
	static final int LAST_MESSAGE_STATE = 5;
	static final int CLEAN_UP_STATE = 6;
	static final int LAST_WAIT_STATE = 7;
	static final int FINISHED_STATE = 8;
	
	static final int CHASE_PIRATE_STATE = 9;
	
	static final int ON_DECK = 12;  // ready to go but needs to be activated by the player
	
	static final int EASY = 1;
	static final int MEDIUM = 2;
	static final int HARD = 3;
	
	// can start is called potentially many times
	// when the objective 
	
	// when there are missions "on deck"
	// the missions icon appears
	// when I click on that icon 
	

	public void Init(MissionData mdin, UserInput uin, IslandManager immin[][], ShipManager smmin[][]) {
		md= mdin;
		imm= immin;
		smm= smmin;
		ui=uin;
		state = ON_DECK;
	}
	
	public String GetTitle() {
		if (title!=null)
			return title;
		return " ";
	}
	public String GetDescription() {
		if (description!=null)
			return description;
		return " ";
	}
	
	public boolean CanStart() {
		return true;  // should be overided by each mission
	}
	
	public void PutOnDeck() {
		// tee up the mission to start in the episode list
		ui.objective_status=UserInput.OBJECTIVE_ACTIVE; 
		md.episode_list.add(this);
	}
	// find a point between max and min dist from ref
	// that is open to put ships on
	public WorldThing FindStart(IslandManager im, double min_dist, double max_dist, WorldThing ref, double center_ratio ) {
		WorldThing startpt = new WorldThing();
		
		boolean criteria = false;
		int tries = 0;
		
		while (criteria == false) {
			tries++;
			startpt.xpos = im.offsetx + md.world_width*Math.random()*center_ratio - md.world_width/2* center_ratio;
			startpt.ypos = im.offsety + md.world_height*Math.random()*center_ratio - md.world_height/2*center_ratio;
			PathData pd= im.GetClosestOpen(startpt.xpos, startpt.ypos);
			if (pd != null) {
				startpt.xpos = pd.x; // really it is so stupid PathData is not derived from World Thing
				startpt.ypos = pd.y;
				if ((startpt.DistanceSq(ref) < max_dist) && (startpt.DistanceSq(ref) > min_dist))
					criteria = true;
			}
			if (tries == 1000) {
				startpt.xpos=im.offsetx;
				startpt.ypos=im.offsety;
				criteria = true;
				System.out.println("Find Start Point Failed");
			}
		}
		return startpt;
	}
	
	public WorldThing TwoPointStart(IslandManager im, double min_dist1, double max_dist1, WorldThing ref1, double max_dist2, double min_dist2, WorldThing ref2) {
		WorldThing startpt = new WorldThing();
		WorldThing retpt = new WorldThing();
		boolean criteria = false;
		int tries = 0;
		double d1, d2;
		PathData pd;
		while (criteria == false) {
			tries++;
			startpt.xpos = im.offsetx + md.world_width*Math.random() - md.world_width/2;
			startpt.ypos = im.offsety + md.world_height*Math.random() - md.world_height/2;
			d1 = startpt.DistanceSq(ref1);
			d2 = startpt.DistanceSq(ref2);					
			if ((d1 > min_dist1) && (d1 < max_dist1) && (d2 > min_dist2) && (d2 < max_dist2)) 	{
				if (im.PathMapValue(startpt) == PathData.PASSABLE)
					criteria = true;
			}
			if (tries == 1000) {
				startpt.xpos=im.offsetx;
				startpt.ypos=im.offsety;
				criteria = true;
				System.out.println("Find Start Point Failed");
			}
		}
		return startpt;
	}
	
	public void SetDifficulty(int diff) {
		difficulty = diff;
	}
	public void Start() {
	//	ui.objective_status = UserInput.OBJECTIVE_ACTIVE;
		state= SETUP_STATE;
	}
	
	public int Update(double t) {
		return 0;
	}
	
	public boolean GetMissionMessage() {
		return mission_message;
	}
	
	Episode() {
		completed = false;
		state = ON_DECK;
		difficulty = EASY;
	}

}
