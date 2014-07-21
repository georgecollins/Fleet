
public class SinkShipEpisode extends Episode {
	// create a ship	
	// point to it
	// tell the user to sink it
	// give it variable difficulty
	
	// have the ship go to a port to take it over
	
	ship user;  // find the user from anywhere
	
	int enemy_segx, enemy_segy;
	ship enemy;
	double victory_timer;
	double time_out;
	
	WorldThing start_place;
	island goal_port;	
	IslandManager im;
	ShipManager sm;
	
	public void Init(MissionData mdin, UserInput uin, IslandManager immin[][], ShipManager smmin[][]) {

		super.Init(mdin, uin, immin, smmin);
		
		state = ON_DECK;
		title = "SHIP INTERCEPT";
		description = "FOLLOW NAVIGATION ARROW TO INTERCEPT ENEMY SHIP";
		bonus = 50; // should change with difficulty
		start_place = new WorldThing();
	}
	public boolean CanStart() {
		
		// should check to see if there is a neutral port to head for
		
		user = md.FindShip(md.userid);
		if (user == null)
			return false;
		// this should be a function in episode = SafeSpawn
		int try_count = 0;
		im = md.GetShipIM(user);
		sm = md.GetShipSM(user);
		
		PathData pd;
		boolean start_found = false;
		
		while ((!start_found) && (try_count < 100)) {
			start_place.xpos = im.offsetx+ Math.random()*md.world_width*0.7d- md.world_width*0.35d;
			start_place.ypos = im.offsety+ Math.random()*md.world_height*0.7d-md.world_height*0.35d;
			pd = im.GetClosestOpen(start_place.xpos, start_place.ypos);
			try_count++;
			if (pd!=null) {
				start_place.xpos = pd.x;
				start_place.ypos = pd.y;
				if (start_place.DistanceSq(user) > md.win_width* md.win_height*2)   // needs to be distant
					start_found = true;
			}
		}
		
		start_place.rotation = Math.random()*Math.PI*2d;
		
		md.relations[md.PLAYER_TEAM][2]=md.ENEMIES;			
		md.relations[2][md.PLAYER_TEAM]=md.ENEMIES;

		// if this works you can start the Episode
		enemy_segx = md.GetSegmentX(start_place.xpos);
		enemy_segy = md.GetSegmentY(start_place.ypos);
		
		return start_found;
	}
	
	public int Update(double t) {
		im = imm[enemy_segx][enemy_segy];
		sm = smm[enemy_segx][enemy_segy];
		
		user = md.FindShip(md.userid);	// in case it changees
		if (user==null)
			return state;
		
		switch (state) {
		case SETUP_STATE:
			ui.objective_status= UserInput.OBJECTIVE_INACTIVE;
			md.can_feud=1;
			start_place = FindStart(im, md.win_width*md.win_height, 3000000, user, 0.9d);
			int sid = sm.AddShip(start_place);
			enemy = sm.GetShip(sid);
			switch (difficulty) {
				case EASY:
					HullData shd = new HullData();
					shd.BuildSmallHull(shd);
					//shd.Barge(1, 40f);					enemy.BuildMixedShip(shd);
					enemy.BuildMixedShip(shd);
					break;
				case MEDIUM:
					enemy.BuildMixedShip();
					break;
				case HARD: 	
					enemy.BuildShip();
					break;
			}
			enemy.AttachAI(sm, im, md);
			enemy.SetTeam(md.RED_TEAM, md);
			enemy.ai.StartWait(50, 5000d);
			ui.target_mode = ui.TARGET_OBJECTIVE;
			ui.arrow_string = "INTERCEPT";
			ui.dist_string = " ";
			mission_message = true;
			ui.TurnOnTicker("XXX INTERCEPT ENEMY SHIP AND ENGAGE XXX");
			time_out = 80000d/md.time_factor;
			state = MESSAGE_STATE;
			break;
		case MESSAGE_STATE:
			if (enemy.type==ship.stype.SHIP) {
				
				if (ui.target_mode!=ui.TARGET_PORT) {
					ui.SetTargetXY(enemy.xpos, enemy.ypos);
					ui.arrow_string = "INTERCEPT";
					ui.dist_string = " ";
					// else count down objective to they qui
				}
				if (user.DistanceSq(enemy) < md.win_width*md.win_height) {
					enemy.ai.StartAttackTask(enemy, user.id);
					enemy.ai.RemoveTaskType(AIControl.TASK_WAIT);  // don't go back to waiting if the attack is interrupted
					state = WAIT_FOR_PLAYER_STATE;
				}
				
				time_out-=t;
				if (time_out < 0) {
					sm.DeleteShip(enemy.id);
					state = CLEAN_UP_STATE;
					victory_timer = 50000d/md.time_factor;
				}
			}
			else 
			{
				state= CLEAN_UP_STATE;
				victory_timer = 50000d/md.time_factor;
			}
			
			break;
		case WAIT_FOR_PLAYER_STATE:
			if (enemy.type == ship.stype.SHIP) {
				if (ui.target_mode!=ui.TARGET_PORT) {
					ui.SetTargetXY(enemy.xpos, enemy.ypos);
					ui.arrow_string = "INTERCEPT";
					ui.dist_string = " ";
					// else count down objective to they quit
				}
			} else {
				ui.TurnOnTicker("XXX TARGET DESTROYED XXX REPEAT TARGET DESTROYED XXX");
				victory_timer=0;
				md.AddMoney(md.PLAYER_TEAM, bonus);
				state = CLEAN_UP_STATE;
			}
			break;
		case CLEAN_UP_STATE:
			completed = true;
			md.can_feud = 0;
			ui.target_mode = ui.TARGET_NOTHING;
			ui.arrow_string = " ";
			state = LAST_WAIT_STATE;
			break;
		case LAST_WAIT_STATE:
			victory_timer+=t;
			if (victory_timer > 15000d/md.time_factor) {
				ui.TurnOffTicker();
				mission_message = false;
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
