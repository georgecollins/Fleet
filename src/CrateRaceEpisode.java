
public class CrateRaceEpisode extends Episode {
	
	// put down a crate
	// race to it
	// who ever gets to it first has to take it back
	// you can sink the person taking it back or
	// get another crate
	
	ship user;
	ship pirate[];
	int crate_time;
	int crate_count;
	int enemy_segx;
	int enemy_segy;
	int target_ship;
	
	island flee_port;
	
	pickup crates[];
	
	WorldThing start_place;
	
	IslandManager im;
	ShipManager sm;
	
	double time_out;
	
	double timer;
	
	static final int MAX_CRATES = 1;
	
	public void Init(MissionData mdin, UserInput uin, IslandManager immin[][], ShipManager smmin[][]) {
		super.Init(mdin,  uin, immin, smmin);
		state = ON_DECK;
		title = "SMUGGLERS RUN";
		description = "GRAB CRATED BEFORE THE BLUE PIRATES GET THEM";
		bonus = 0; // if you get every fish in time
		crate_count = MAX_CRATES;
		crate_time = 100; // every time the time goes dow 
		
		pirate = new ship[MAX_CRATES];
		crates = new pickup[MAX_CRATES];
	}
	
	public boolean CanStart() {
		user = md.FindShip(md.userid);
		if (user == null)
			return false;
		return true;
	}
	
	public void Start() {
	//	ui.objective_status = UserInput.OBJECTIVE_ACTIVE;
		super.Start();
		crate_time -= 10;  // fish timer goes down every time
		enemy_segx = md.GetSegmentX(user.xpos);
		enemy_segy = md.GetSegmentY(user.ypos);

	}
	
	public int Update(double t) {
		im = imm[enemy_segx][enemy_segy];
		sm = smm[enemy_segx][enemy_segy];
		
		user = md.FindShip(md.userid);	// in case it changees
		if (user==null)
			return state;
		
		WorldThing cratept = new WorldThing();
		switch (state) {
		case SETUP_STATE:
			time_out = 48000/md.time_factor;
			double cratedist = md.win_width*md.win_height*2.0d;
			cratept = FindStart(im, cratedist*2, cratedist*4d, user, 0.4d);
			for (int i=0; i < MAX_CRATES; i++) {

				cratept.xpos+= Math.random()*60d-30d;
				cratept.ypos+= Math.random()*60d-30d;
				crates[i]= md.PKmat[enemy_segx][enemy_segy].AddPickup(cratept, (int)(Math.random()*3+1));
			
				WorldThing startpt = TwoPointStart(im, cratedist, cratedist*4.0d, crates[i], cratedist, cratedist * 20d, user); 
				int pship = sm.AddShip(startpt);
				pirate[i] = sm.GetShip(pship);
				HullData hd = new HullData();
				boolean cargo = false;
				while (!cargo) {
					hd.BuildSmallHull(hd);
					pirate[i].BuildMixedShip(hd);
					if (pirate[i].GetCargoCount() > 0)
						cargo = true;
				}
				pirate[i].SetTeam(md.PIRATE_TEAM, md);
				pirate[i].AttachAI(sm, im, md);
				pirate[i].ai.StartCrateGrab(10, 100d, crates[0]);
			}
			ui.objective_status= UserInput.OBJECTIVE_INACTIVE;
			mission_message = true;
			state= MESSAGE_STATE;

			break;
		case MESSAGE_STATE:
			time_out-=t;
			boolean some_exist = false;
			for (int i=0; i < MAX_CRATES; i++) 
				if (crates[i]!=null) {
					some_exist = true;
					ui.target_mode = ui.TARGET_OBJECTIVE;
					ui.SetTargetXY(crates[i].xpos, crates[i].ypos);
					if (crates[i].type == pickup.pktype.EMPTY) {
						crates[i]=null;
					} else {
					//	ui.SetTargetXY(pirate.xpos, pirate.ypos);

					}
			}
			for (int i=0; i < MAX_CRATES; i++) {
				if (pirate[i].HasCargoLoaded()) {
						state = CHASE_PIRATE_STATE;

						target_ship = i;
						flee_port = im.FindClosestPort(pirate[i]);
						Path ppath = im.GetPath(pirate[i].xpos, pirate[i].ypos, flee_port.world_portx, flee_port.world_porty);
						if (ppath!=null) {
							pirate[i].ai.StartFollowPath(pirate[i], ppath, 10, AIControl.PATH_ONE_TIME);
							pirate[i].ai.RemoveTaskType(AIControl.TASK_CRATE_GRAB);
						}
						break;
					}
			}
			
			if (!some_exist)
				state = LAST_MESSAGE_STATE;
		
			if (time_out < 0)
				state = LAST_MESSAGE_STATE;
			break;

		case CHASE_PIRATE_STATE:	
			time_out-=t;
			for (int i=0; i < MAX_CRATES; i++) {
				if (target_ship==i) {
					if (pirate[i].type == ship.stype.SHIP) {
						ui.SetTargetXY(pirate[i].xpos, pirate[i].ypos);	
					} else
					{
				//d	?? 
					// loop back if there are crates
					// but for now
					
					// also, is it a wreck? 
						state =LAST_MESSAGE_STATE;
					
					}
				}
				else {
					if ((pirate[i].HasCargoLoaded()) && (pirate[i].ai.GetCurrentTask()!= AIControl.TASK_PATH)) {
						flee_port = im.FindClosestPort(pirate[i]);
						Path ppath = im.GetPath(pirate[i].xpos, pirate[i].ypos, flee_port.world_portx, flee_port.world_porty);
						if (ppath!=null) {
							pirate[i].ai.StartFollowPath(pirate[i], ppath, 10, AIControl.PATH_ONE_TIME);
							pirate[i].ai.RemoveTaskType(AIControl.TASK_CRATE_GRAB);
						}

					}
				}
			}
			if (time_out < 0)
				state = LAST_MESSAGE_STATE;
			break;
		case WAIT_FOR_PLAYER_STATE:
			break;
		case LAST_MESSAGE_STATE:
			mission_message = false;
			ui.target_mode = ui.TARGET_NOTHING;
			ui.objective_status= UserInput.OBJECTIVE_ACTIVE;
			state = FINISHED_STATE;
			break;
		case FINISHED_STATE:
			break;
		}
	
		return 0;
	}
}
