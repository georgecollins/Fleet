import java.util.ArrayList;
import java.util.List;

public class MissionData {
	
	/*
	 * This is something that holds the data of a specific mission. 
	 * 
	 * how many ships, 
	 * how many islands
	 * how many teams
	 * what is the relationship between teams
	 * what is the end condition.  
	 */
	public int number_teams;
	public int ship_number;   // later [];
	public int advanced_ship_number;  // more dangerous, skilled ships
	public int island_number;
	public int world_width;  // actually this is a segment
	public int world_height;
	 
	public int segment_width;
	public int segment_height;
	public int turn_on_money;  // a flag to tell the ui to turn on money display
	
	public int startx;  // the sector that the player starts in x, y
	public int starty;
	
	public  int show_ai = 0;  // you can change this
	public int debug_mode = 0;
	
	public static final int MAX_SEGMENT_WIDTH = 8;
	public static final int MAX_SEGMENT_HEIGHT = 8;

	
	public static final int CLOSE_DISTANCE = 0;  // for preferences of search
	public static final int FAR_DISTANCE = 1;

	
	// this is segment by segment data for the number of ships, islands and pickups
	public int ship_mat[][];
	public int advanced_ship_mat[][];
	public int island_mat[][];
	public int pickup_mat[][];
	public int fish_mat[][];
	
	public double wavetime;
	public int win_width, win_height;	
	public double width, height;
	public double default_width, default_height;
	public double zoom_width, zoom_height;
	
	
	public double time_factor;  // how much do I throttle simulation?  100-400
	
	
	List <Episode>episode_list;
	static final int MAX_EPISODES = 32;
	
	public int end_condition;
	
	
	public final static int MAX_TEAMS=6;
	public final static int PLAYER_TEAM = 0;
	public final static int GREEN_TEAM = 1;// possibly friendly
	public final static int RED_TEAM = 2;  // never friendly
	public final static int PIRATE_TEAM = 3;
	public final static int NEUTRAL_TEAM = 4;
	
	public int userid;
	
	public int money[]; 
	public int total_money[];
	public int tons_sunk[];
	public int relations[][];  // friend or foe
	public double war_timer[];
	
	public int team_component_count[];
	public ComponentData team_components[];
	
	public final static int MAX_TEAM_COMPONENTS  = 6;  // with scroll this could be more

	public int can_feud = 0; // ignore starting wars during missions
	
	
	
	public final static int NEUTRAL=0;
	public final static int ENEMIES=1;
	public final static int ALLIES= 2;  // will actively help you.
	
	
	float team_colorR[];
	float team_colorG[];
	float team_colorB[];
	

	ShipManager SMmat[][];
	IslandManager IMmat[][];
	ProjectileManager PMmat[][];
	PickupManager PKmat[][];
	
	//game end conditions, should be flags
	public final static int PLAYER_DIES_CONDITION = 1;
//	public final static int TEAM_NEUTRAL = 0;
//	public final static int TEAM_ENEMY = 1;
//	public final static int TEAM_ALLY = 2;
			
	public static int PROJECTILE=0x10000;
	public static int SHIP=0x20000;
	public static int COMPONENT=0x30000;
	public static int ISLAND=0x40000;
	public static int PICKUP=0x50000;
	public static int BUTTON=0x60000;
	int id_count;
	
	
	int SOSsignal=0;
	double SOSx;
	double SOSy;
	int SOSteam;
	
	int GotToPortSignal = 0;
	int freighterid;
	int portid;
	
	fleet userfleet;  // the player's fleet
	
	public class portitem {
		island p;
		double dist;
		portitem() {
			
		}
	}
	List<portitem> portlist;
	int port_index;
	java.util.Iterator<portitem> port_it;
	
	int GetUniqieID(int objtype) {
		id_count++;
		return (objtype | id_count);
	}
	
	
	int GetSegmentX(double x) {
		double centerx=(world_width*segment_width)/2;
		return (int) ((x+centerx)/world_width);
	}
	
	int GetSegmentY(double y) {
		double centery=(world_height*segment_height)/2;
		return (int) ((y+centery)/world_height);
	}
	
	public void AddMoney(int team, int amount) {
		
		money[team]+=amount;
		total_money[team]+=amount;
		if (team== PLAYER_TEAM)
			turn_on_money=1;
	}
	
	public void AddTeamComponent(ComponentData comp, int team) {
		
		// check to see if it is identical
		for (int i=0; i < team_component_count[team]; i++) 
			if (team_components[i].Equals(comp))
				return;  // don't add it again
		if (team_component_count[team] < MAX_TEAM_COMPONENTS) {
			team_components[team_component_count[team]] = new ComponentData();
			comp.CopyTo(team_components[team_component_count[team]]);	
			team_component_count[team]++;
		} // otherwise ignore for now
		
		// some day this list will scroll
	}
	
	public void SendSunkMessage(ship s,int owner, int team) {
		// put this in a queue to read off later
		
		// don't give bounties for sinking freighters?? how?
		if ((team==PLAYER_TEAM) || (owner==userid)) {
			tons_sunk[PLAYER_TEAM]+=s.GetTotalWeight();
			AddMoney(PLAYER_TEAM, s.GetTotalWeight()/5);  // was team
		}
	}	
	
	public int Save() {
		return 0;
	}
	public int Load() {
		return 0;
	}

	public float teamR(int i)  {
		return team_colorR[i];
	}
	
	public float teamG(int i) {
		return team_colorG[i];
	}
	
	public float teamB(int i) {
		return team_colorB[i];
	}
	
	public void MakeEnemy(int myteam, int myenemy) {
		relations[myteam][myenemy]=ENEMIES;  // we are enemies now
		relations[myenemy][myteam]=ENEMIES;
	}
	
	void SendSOS(double x, double y, int team) {
		// absurdly crude
		SOSx=x;
		SOSy=y;
		SOSteam=team;
		SOSsignal=1;
	}
	
	void ClearSOS() {
		SOSsignal=0;
	}
	
	void SendGotToPort(int sid, int pid) {
		GotToPortSignal=1;
		freighterid = sid;;
		portid = pid;;
	
	}
	
	void ClearGotToPort() {
		GotToPortSignal=0;
	}
	
	void StartWarTimer(int team) {
		if ((team != PLAYER_TEAM) && (can_feud==0)) {
			war_timer[team]=240.0d;  // 60 only, it is not cumulative
			relations[team][PLAYER_TEAM]= ENEMIES;
			relations[PLAYER_TEAM][team] = ENEMIES;
		}
	}
	void IncrementWarTimer(double t) {
		for (int i = 0; i < RED_TEAM/*MAX_TEAMS*/; i++) {  // test to see if this is what causes teams to stop fighting
			if (war_timer[i] > 0d) {
				war_timer[i]-=t;
				if (war_timer[i]< 0.1d) {
					war_timer[i]=0.0d;
	
					relations[i][PLAYER_TEAM]=NEUTRAL;
					relations[PLAYER_TEAM][i]=NEUTRAL;
				
				}
			}
		}
	}
	
	
	public island ClickOnPort(int x, int y, double centerx, double centery) {
		float xworld = (float)(((x - win_width/2)*zoom_width+centerx));
		float yworld = (float)((y- win_height/2)*zoom_height+centery);
		for (int is=0; is < segment_width; is++)
			for (int js=0; js < segment_height; js++) 
				for (int loop=0; loop < IMmat[is][js].island_count; loop++) {
				//	float boundsx = (float)(xworld-IMmat[is][js].islands[loop].xpos);
				//	float boundsy = (float)(yworld-IMmat[is][js].islands[loop].ypos);
					if (IMmat[is][js].islands[loop].isketch.InBounds(xworld, yworld)) {
						return IMmat[is][js].islands[loop];
					}
				}
		return null;
	}
	
	// when you don't know for sure a ship is in a segment
	public ship FindShip(int targetid) {
		for (int segx=0;  segx < segment_width; segx++) 
			for (int segy=0; segy< segment_height; segy++)
				for (int count=0; count < SMmat[segx][segy].scount; count++) {
					ship s=SMmat[segx][segy].slist[count];  // so I don't have to keep typing it
					if ((s!=null) && (s.type==ship.stype.SHIP))
						if (s.id==targetid)
							return s;
				}
		return null;
	}
	// when you don't know for sure a ship is in a segment
	public pickup FindPickup(int targetid) {
		for (int segx=0;  segx < segment_width; segx++) 
			for (int segy=0; segy< segment_height; segy++)
				for (int count=0; count < PKmat[segx][segy].pkcount; count++) {
					pickup pk=PKmat[segx][segy].pickups[count];  // so I don't have to keep typing it
					if ((pk!=null) && (pk.type!=pickup.pktype.EMPTY)) 
						if (pk.id==targetid)
							return pk;
				}
		return null;
	}
	void MoveShip(ship s, int user_segmentx, int user_segmenty) {
		if ((s!=null) && (s.id!=userid)) {
			int new_segx = user_segmentx;
			int new_segy = user_segmenty;
			if (s.xpos > IMmat[user_segmentx][user_segmenty].offsetx + world_width/2) 
				new_segx+=1;
			if (s.xpos < IMmat[user_segmentx][user_segmenty].offsetx - world_width/2)
				new_segx-=1;
			if (s.ypos > IMmat[user_segmentx][user_segmenty].offsety + world_height/2)
				new_segy+=1;
			if (s.ypos < IMmat[user_segmentx][user_segmenty].offsety - world_height/2)
				new_segy-=1;
			// wrap ship around
			if (new_segx== segment_width) {
				new_segx=0;
				s.xpos=s.xpos-world_width*segment_width;
			}
			if (new_segx < 0) {
				new_segx = segment_width - 1;
				s.xpos=s.xpos+world_width*segment_width;
			}
			if (new_segy== segment_height) {
				new_segy=0;
				s.ypos=s.ypos-world_height*segment_height;
			}
			if (new_segy < 0) {
				new_segy = segment_height - 1;
				s.ypos=s.ypos+world_height*segment_height;
			}
			
			if ((new_segx!=user_segmentx) || (new_segy!=user_segmenty)) {
				int id=s.id;
				SMmat[new_segx][new_segy].AddShip(s);
				SMmat[user_segmentx][user_segmenty].DeleteShip(id);
				s.ai.Setup(SMmat[new_segx][new_segy], IMmat[new_segx][new_segy]);
				s.SetPM(PMmat[new_segx][new_segy]);
				
				// if it is the player..
				/*
				user_segmentx=new_segx;
				user_segmenty=new_segy;
				SetUserSegment(new_segx, new_segy);
				*/
				// I need to do the same thing for md.userfleet
				// move them all over here
			}
			
		}
	}
	
	
	void MakePortList() {
		portlist = new ArrayList<portitem>();
		ship s = FindShip(userid);
		if (s==null) return;
		

		
		for (int xs=0; xs< segment_width; xs++)
			for (int ys = 0; ys < segment_height; ys++)
				for (int count = 0; count < IMmat[xs][ys].island_count; count++) {
					if (IMmat[xs][ys].islands[count].has_port!=0) {
						portitem pi = new portitem(); // I have to new it each time or it gets overwritten
						pi.p=IMmat[xs][ys].islands[count];
						pi.dist = pi.p.DistanceSq(s); // (pi.p.xpos-s.xpos)*(pi.p.xpos-s.xpos)+(pi.p.ypos-s.ypos)*(pi.p.ypos-s.ypos);
						portlist.add(pi);
					}
						
			
		}
		
		// now sort
		boolean changed = true;
		portitem p1= new portitem();
		portitem p2= new portitem();
		while (changed) {
			changed= false;
			port_it = portlist.iterator();  // do I need thatline?
			for (int count = 0; count < portlist.size()-1; count++) {
				p1=portlist.get(count);
				p2=portlist.get(count+1);
				if (p1.dist> p2.dist) {
					changed= true;
					portlist.set(count, p2);
					portlist.set(count+1, p1);
				}
			}
		}
		// when no changes happen, it is in order
		port_it = portlist.iterator();
		port_index=0;
		
	}

	// make a list with one item
	void MakeOneIslandList(island isl) {
		portlist = new ArrayList<portitem>();
		portitem pi = new portitem(); // I have to new it each time or it gets overwritten
		pi.p=isl;
		portlist.add(pi);
		port_index=0;
	
	}
	
	island GetNextPort() {

		island isl=null;
		if (port_index < portlist.size()-1) {
			isl = portlist.get(port_index).p;
			port_index++;
		}
		else {
			port_index=0;
			isl=portlist.get(port_index).p;
		}
		return isl;
	}
	
	island GetCurrentPort() {
		return portlist.get(port_index).p;
	}
	
	
	island FindPort(IslandManager im, WorldThing wt, int dist_pref, boolean friendly) {
		island isl=null;
		// go thrugh find the best dist
		double best_dist;
		if (dist_pref==CLOSE_DISTANCE) {
			best_dist = segment_width*segment_width + segment_height*segment_height;
		} else {
			best_dist = 0;
		}
			
		for (int i=0; i < im.island_count; i++) 
			if ((im.islands[i].has_port > 0) && 
					((im.islands[i].pd.team!= RED_TEAM) || (!friendly))) {
			double dist= wt.DistanceSq(im.islands[i]);
			if (dist_pref==CLOSE_DISTANCE) {
				if (dist < best_dist) {
					best_dist=dist;
					isl = im.islands[i];
				}
				
			} else {
				if (dist > best_dist) {
					best_dist=dist;
					isl = im.islands[i];
				}
				
			}
				
		}
		
		return isl;
				
	}
	
	IslandManager GetShipIM(ship s) {
		int seg_x= GetSegmentX(s.xpos);
		int seg_y =GetSegmentY(s.ypos);
		if (seg_x > segment_width - 1 )
			seg_x= segment_width-1;
		if (seg_y > segment_height - 1)
			seg_y= segment_height-1;
		if (seg_x < 0) seg_x = 0;
		if (seg_y < 0) seg_y = 0;
		return IMmat[seg_x][seg_y];
	}
	ShipManager GetShipSM(ship s) {
		int seg_x= GetSegmentX(s.xpos);
		int seg_y =GetSegmentY(s.ypos);
		if (seg_x > segment_width - 1 )
			seg_x= segment_width-1;
		if (seg_y > segment_height - 1)
			seg_y= segment_height-1;
		if (seg_x < 0) seg_x = 0;
		if (seg_y < 0) seg_y = 0;
		return SMmat[seg_x][seg_y];
	}	
	void SetupWorld(IslandManager imm[][], ShipManager smm[][], ProjectileManager pmm[][], PickupManager pkm[][]) {
		IMmat=imm;
		SMmat=smm;
		PMmat=pmm;
		PKmat=pkm;
		for (int i=0; i < MAX_TEAMS; i++) {
			war_timer[i]=0.0d;
		}
			
	}	
	
	
	
	
	MissionData() {

		ship_mat = new int[MAX_SEGMENT_WIDTH][MAX_SEGMENT_HEIGHT];
		advanced_ship_mat = new int[MAX_SEGMENT_WIDTH][MAX_SEGMENT_HEIGHT];
		island_mat = new int[MAX_SEGMENT_WIDTH][MAX_SEGMENT_HEIGHT];
		pickup_mat = new int[MAX_SEGMENT_WIDTH][MAX_SEGMENT_HEIGHT];	
		fish_mat = new int[MAX_SEGMENT_WIDTH][MAX_SEGMENT_HEIGHT];
		money = new int[MAX_TEAMS];
		total_money = new int[MAX_TEAMS];
		tons_sunk = new int[MAX_TEAMS];
		relations= new int[MAX_TEAMS][MAX_TEAMS];
		war_timer = new double[MAX_TEAMS];
		team_component_count = new int [MAX_TEAMS];
		team_components = new ComponentData[MAX_TEAMS];
		
		id_count=0;
		
		team_colorR = new float[MAX_TEAMS];
		team_colorG = new float[MAX_TEAMS];
		team_colorB = new float[MAX_TEAMS];	
		//9.0f, 0.5f, 0.1f
				// team -0 Orange
		team_colorR[0] = 0.9f;
		team_colorG[0] = 0.5f;	
		team_colorB[0] = 0.1f;
		// team 1 green 0.0f, 0.5f, 0.0f
		team_colorR[1] = 0.0f;
		team_colorG[1] = 0.5f;	
		team_colorB[1] = 0.0f;

		// team 2 pink
		//1.0f, 0.5f, 0.8f
		team_colorR[2] = 1.0f;
		team_colorG[2] = 0.2f;
		team_colorB[2] = 0.3f;
		//0.0f, 0.45f, 0.9f
		// team 3 very blue
		team_colorR[3] = 0.0f;
		team_colorG[3] = 0.45f;
		team_colorB[3] = 0.9f;

		team_colorR[NEUTRAL_TEAM] = 1.0f;
		team_colorG[NEUTRAL_TEAM] = 1.0f;
		team_colorB[NEUTRAL_TEAM] =1.0f;
		userfleet= new fleet();
		episode_list = new ArrayList<Episode>();
	}
}
