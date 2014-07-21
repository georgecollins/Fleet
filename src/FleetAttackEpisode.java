
public class FleetAttackEpisode extends Episode {
	// create a fleet in another sector that patrols in formation.  
	// when you get close, attack the player and his fleet.  
	
	// give it variable difficulty
	
	ship user;  // find the user from anywhere
	
	
	static final int MAX_ENEMIES = 5;
	int enemy_count;
	int enemy_segx, enemy_segy;
	ship enemies[];
	fleet enemy_fleet;
	double victory_timer;
	double time_out;
	
	WorldThing start_place;
	island goal_port;
	public void Init(MissionData mdin, UserInput uin, IslandManager immin[][], ShipManager smmin[][]) {

		super.Init(mdin, uin, immin, smmin);
		
		enemy_count = 3;
		enemies = new ship[MAX_ENEMIES];
		enemy_fleet = new fleet();
		start_place = new WorldThing();
		title = "INTERCEPT ENEMY FLEET";
		description = "FOLLOW NAVIGATION TO INTERCEPT A FLEET OF ENEMY VESSELS";
		bonus = 250;
	}
	public boolean CanStart() {
		// find a place you can safely set up ships
		// that is far from the player
		user = md.FindShip(md.userid);
		if (user==null)
			return false;
		
		/*
		if (md.userfleet==null)   // this is a mission for a fleet
				return false;
		
		
		if (md.userfleet.count()<2)
			return false;
		
		*/
		IslandManager im= md.GetShipIM(user);  //imm[md.GetSegmentX(user.xpos)][md.GetSegmentY(user.ypos)];	
		ShipManager sm=	md.GetShipSM(user);
		
		boolean start_found = false;
		PathData pd;
		// four corners
		for (int xo=-1; xo < 2; xo=xo+2) 
			for (int yo=-1; yo < 2; yo=yo+2) {
				start_place.xpos = sm.offsetx+0.4d*md.world_width*xo;
				start_place.ypos = sm.offsety+0.4d*md.world_height*yo;
				
				pd = im.GetClosestOpen(start_place.xpos, start_place.ypos);
				
				// close to a corner but far from the user
				if (pd!=null) {
					if (
							((pd.x-start_place.xpos)*(pd.x-start_place.xpos) + (pd.y-start_place.ypos)*(pd.y-start_place.ypos) < 200000) &&
							((pd.x-user.xpos)*(pd.x-user.xpos) + (pd.y-user.ypos) * (pd.y - user.ypos) > md.win_width*md.win_height))
						{
						start_found= true;
						start_place.xpos = pd.x;
						start_place.ypos = pd.y;
						xo = 2; // end the loops
						yo = 2;  
					}
				}
				
		}
		
		md.relations[md.PLAYER_TEAM][2]=md.ENEMIES;			
		md.relations[2][md.PLAYER_TEAM]=md.ENEMIES;

		// if this works you can start the Episode
		enemy_segx = md.GetSegmentX(start_place.xpos);
		enemy_segy = md.GetSegmentY(start_place.ypos);
		
		return start_found;
	}

	public int Update(double t) {

		IslandManager im=imm[enemy_segx][enemy_segy];	
		ShipManager sm=	smm[enemy_segx][enemy_segy];
		
		boolean something_alive;
		user = md.FindShip(md.userid);	// in case it changees
		if (user==null)
			return state;
		
		switch (state) {
		case SETUP_STATE:
			md.can_feud = 1;
			start_place = FindStart(im, md.win_width*md.win_height, md.win_width*md.win_height*5.0d, user, 0.9d);
			for (int i= 0; i < enemy_count;i++) {

				int sid=sm.AddShip(start_place);
				enemies[i]=sm.GetShip(sid);
				HullData nhd = new HullData();
				switch (difficulty) {
				case EASY:
					nhd.BuildSmallHull(nhd);
					enemies[i].BuildMixedShip(nhd);
					break;
				case MEDIUM:
					nhd.BuildSmallHull(nhd);
					enemies[i].BuildAttackShip(nhd);
					break;
				case HARD:
					enemies[i].BuildShip();
					break;
				}	
				enemies[i].AttachAI(sm, im, md);
				enemies[i].SetTeam(2, md);
				enemy_fleet.AddShip(enemies[i]);
				start_place.xpos+=80;
				start_place.ypos+=80;
				enemies[i].ai.StartWait(50, 1000d);  // priority 50, duration 10000
 			}
			
			ui.target_mode = ui.TARGET_OBJECTIVE;
			ui.arrow_string="FLEET";
			mission_message=true;
			ui.objective_status= UserInput.OBJECTIVE_INACTIVE;
			ui.TurnOnTicker("XXX INTERCEPT RED FLEET AND ENGAGE XXX DESTROY THE RED FLEET XXX");
			time_out =90000/md.time_factor;
			state= MESSAGE_STATE;
			break;
		
		case MESSAGE_STATE:
			time_out-=t;
			something_alive = false;
			for (int i=0; i < enemy_count; i++) 
				if (enemies[i].type==ship.stype.SHIP) {
					ui.SetTargetXY(enemies[i].xpos, enemies[i].ypos);
					something_alive = true;
					
					if (user.DistanceSq(enemies[i]) < md.win_width *md.win_height*3.0d) {
						state = WAIT_FOR_PLAYER_STATE;

					}
					
				}
			if (!something_alive) {
				state = CLEAN_UP_STATE;
				victory_timer = 10000d/md.time_factor;  // no victory message
			
			} else
				time_out-=t;
			
			break;
		case WAIT_FOR_PLAYER_STATE:
			enemies[0].ai.StartAttackTask(enemies[0],  user.id);
			for (int i=1; i < enemy_count; i++) 
				if (enemies[i].type==ship.stype.SHIP) {
					enemies[i].ai.StartAttackTask(enemies[i], user.id);
				//	enemies[i].ai.StartFollow(enemies[i], enemies[i-1], 0, 0);

				}
			
			for (int i = 0; i < enemy_count; i++) {
				enemies[i].ai.RemoveTaskType(AIControl.TASK_WAIT);  // no waiting
			}
				
			ui.TurnOffTicker();
			state = REMINDER_STATE;
			break;
		case REMINDER_STATE:
			something_alive = false;
			for (int i=0; i < enemy_count; i++) 
				if (enemies[i].type==ship.stype.SHIP) {
					if (ui.target_mode ==ui.TARGET_OBJECTIVE) {
						ui.SetTargetXY(enemies[i].xpos, enemies[i].ypos);
						ui.arrow_string="FLEET";
					}
					something_alive = true;
				} 
			time_out-=t;
			if (time_out < 0) {
				ui.TurnOnTicker("XXX ENEMY FLEET HAS WITHDRAWN XXX");
				victory_timer=0;
				state = CLEAN_UP_STATE;
				md.money[md.PLAYER_TEAM]+=0;	 // no bonus	
				
			}
			if (!something_alive) {
				ui.TurnOnTicker("XXX FLEET INTELLIGENCE REPORTS RED FLEET DESTROYED XXX REPEAT RED FLEET DESTORYED XXX");
				victory_timer=0;
				state = CLEAN_UP_STATE;
				completed= true;

				md.AddMoney(md.PLAYER_TEAM, bonus);
			}
			break;
		case CLEAN_UP_STATE:
			md.can_feud=0;
			ui.target_mode = UserInput.TARGET_NOTHING;
			state = LAST_WAIT_STATE;
			break;
		case LAST_WAIT_STATE:
			victory_timer=victory_timer+t;
			if (victory_timer > 30000.0d/md.time_factor) {
				ui.TurnOffTicker();
				mission_message=false;
				ui.objective_status= UserInput.OBJECTIVE_ACTIVE;
				state = FINISHED_STATE;
			}	
			break;
		case FINISHED_STATE:
			break;
			
		}
		return 0;
	}
}
