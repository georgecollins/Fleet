
public class SinkFreighterEpisode extends Episode {
	// create a freighter close to player
	// tell the player he has to sink it
	// send the frieghter to a far port
	// add defenders
	
	ship user;
	ship freighter;
	
	island target_port;
	
	int enemy_segx, enemy_segy;
	double time_out;
	double end_timer;
	
	WorldThing start_place;
	island goal_port;
	IslandManager im;
	ShipManager sm;
	
	public void Init(MissionData mdin, UserInput uin, IslandManager immin[][], ShipManager smmin[][]) {
		
		super.Init(mdin, uin, immin, smmin);
		
		state = ON_DECK;
		title = "SINK FREIGHTER";
		description = "SINK RED FLEET FREIGHTER XXX COLLECT CRATES IN YOUR CARGO HOLD FOR EXTRA FUNDS";
		bonus = 25;
		start_place = new WorldThing();
			
	}

	public boolean CanStart() {
		
		// At some point I might want to check for cargo
		return true;
	}
	
	public int Update(double t){
		user = md.FindShip(md.userid);	// in case it changees
		if (user==null)
			return state;
		
		enemy_segx = md.GetSegmentX(user.xpos);
		enemy_segy = md.GetSegmentY(user.ypos);
		
		im = imm[enemy_segx][enemy_segy];
		sm = smm[enemy_segx][enemy_segy];
		
		switch (state) {
		case SETUP_STATE:
			ui.objective_status = UserInput.OBJECTIVE_INACTIVE;			
			md.can_feud=1;
			start_place = FindStart(im, md.win_width*md.win_height, 30000000, user, 0.8d);
			int sid = sm.AddShip(start_place);
			freighter= sm.GetShip(sid);
			HullData shd = new HullData();
			shd.BuildSmallHull(shd);
			freighter.BuildFreighter(shd);
			freighter.AttachAI(sm,  im, md);
			freighter.SetTeam(md.RED_TEAM, md);
			
			// now find the far port
			//target_port = im.FindClosestPort(start_place);
			target_port = im.FindFarthestPort(start_place);
			Path freighter_path = im.GetPath(start_place.xpos, start_place.ypos, target_port.world_portx, target_port.world_porty);
			if (freighter_path!=null) {
				freighter.ai.StartFollowPath(freighter, freighter_path, 30, AIControl.PATH_ONE_TIME);
			}
			ui.target_mode = ui.TARGET_OBJECTIVE;
			ui.arrow_string = "INTERCEPT";
			ui.dist_string = " ";
			mission_message = true;
			ui.TurnOnTicker("XXX INTERCEPT ENEMY FRIEGHTER AND ENGAGE XXX");
			time_out = 80000d/md.time_factor;
			state = MESSAGE_STATE;
			break;
		case MESSAGE_STATE:
			if (im.AtPort(freighter)!=null) {
				// freighter escaped
				ui.TurnOnTicker("XXX FREIGHTER ESCAPED TO PORT XXX YOU FAILED XXX");
				end_timer = 15000d/md.time_factor;
				ui.target_mode = ui.TARGET_NOTHING;
				ui.arrow_string = " ";
				state = LAST_WAIT_STATE;
				break;
			}
			if (freighter.type==ship.stype.SHIP) {
				if (ui.target_mode!=ui.TARGET_PORT) {
					ui.SetTargetXY(freighter.xpos, freighter.ypos);
					ui.arrow_string = "INTERCEPT";
					ui.dist_string = " ";
					// else count down objective to they qui
				}

			}
			else {
				completed = true;
				ui.TurnOnTicker("XXX TARGET DESTROYED XXX");
				md.money[md.PLAYER_TEAM]+=bonus;
				ui.target_mode = ui.TARGET_NOTHING;
				ui.arrow_string = " ";
				state = LAST_WAIT_STATE;
				end_timer = 15000/md.time_factor;
				// do messages or something
				// look for a free crate
				// send a green ship to pick it up
			}
			time_out-=t;
			if (time_out < 0d) {
				state = CLEAN_UP_STATE;
				ui.target_mode = ui.TARGET_NOTHING;
				ui.arrow_string = " ";
			}
			break;
			
		case LAST_WAIT_STATE:
			end_timer-=t;
			if (end_timer < 0d )
				state = CLEAN_UP_STATE;
			break;
		case CLEAN_UP_STATE:
			md.can_feud = 0;
			ui.TurnOffTicker();
			state = FINISHED_STATE;
			mission_message = false;
			break;
		case FINISHED_STATE:
			break;
			
		}
		
		return 0;
	}
	
}
