
public class HullRescueEpisode extends Episode {
	ship rescue;
	ship user;
	
	IslandManager im;
	ShipManager sm;
	
	int rescue_segx, rescue_segy;
	WorldThing start_place;
	double victory_timer;
	double time_out;
	
	public void Init(MissionData mdin, UserInput uin, IslandManager immin[][], ShipManager smmin[][]) {

		super.Init(mdin, uin, immin, smmin);
		title = "ABANDONDEND SHIP";
		description = "RENDEZVOUS WITH ABANDONED SHIP AND ESCORT IT BACK TO PORT AS SALVAGE";
		bonus = 10;
		state = ON_DECK;
		start_place = new WorldThing();
	}		
	public boolean CanStart() {
		user = md.FindShip(md.userid);
		if (user==null)
			return false;
		
		// only discover wrecks when you are h
		if ((md.userfleet!=null) && (md.userfleet.count() > 1))
			return false;

		int try_count = 0;
		
		im = md.GetShipIM(user);
		sm = md.GetShipSM(user);
		
		PathData pd;
		boolean start_found = false;
		
		
		while ((!start_found) && (try_count < 100)) {
			start_place.xpos = im.offsetx+ Math.random()*md.world_width*0.8d- md.world_width*0.4d;
			start_place.ypos = im.offsety+ Math.random()*md.world_height*0.8d-md.world_height*0.4d;
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
		
		rescue_segx = md.GetSegmentX(start_place.xpos);
		rescue_segy = md.GetSegmentY(start_place.ypos);
		
		return start_found;
	}
	public int Update(double t) {
		im = imm[rescue_segx][rescue_segy];
		sm = smm[rescue_segx][rescue_segy];
		user = md.FindShip(md.userid);
		if (user==null) {
			state = CLEAN_UP_STATE;
		}
		
		switch (state) {
		case SETUP_STATE:
			int sid = sm.AddShip(start_place);
			rescue = sm.GetShip(sid);
			HullData nhd = new HullData();
			nhd.BuildHull(nhd);
			rescue.BuildShip(nhd);

			//rescue.BuildShip();
			rescue.AttachAI(sm, im, md);
			rescue.SetTeam(md.PLAYER_TEAM, md);
			rescue.ai.StartWait(50, 5000d);
			
			for (int i=0; i < rescue.health-20;i++) {
				rescue.TakeDamage(1, null);
			}
			ui.target_mode = ui.TARGET_OBJECTIVE;
			ui.arrow_string = "SALVAGE";
			ui.dist_string = " ";
			mission_message = true;
			ui.objective_status= UserInput.OBJECTIVE_INACTIVE;
			ui.TurnOnTicker("XXX FLEET INTELLIGENCE HAS DETECTED AN ABANDONED SHIP XXX RENDEZVOUS WITH THE ABANDONED SHIP AND SEIZE IT FOR SALVAGE XXX");
			time_out = 90000d/ md.time_factor;
			state = MESSAGE_STATE;
			break;
		case MESSAGE_STATE:
			if (rescue.type == ship.stype.SHIP) {
				if (ui.target_mode!=ui.TARGET_PORT) {
					ui.SetTargetXY(rescue.xpos, rescue.ypos);
					ui.arrow_string = "SALVAGE";
					ui.dist_string = " ";
					
				}
				if (user.DistanceSq(rescue) < 20000) {
					rescue.ai.RemoveTaskType(AIControl.TASK_WAIT);
					rescue.ai.StartFollow(rescue, user, 0, 0);
					if (md.userfleet==null) {
						// md.userfleet= new 
						md.userfleet = new fleet();
						md.userfleet.AddShip(rescue);
					} else {
						md.userfleet.AddShip(rescue);
					}
					ui.TurnOnTicker("XXX TAKE THE ABANDONED SHIP TO PORT FOR SALVAGE XXX THE CREW WILL FOLLOW YOUR SHIP XXX");
					ui.dist_string = " ";
					
					ui.target_mode= ui.TARGET_PORT;
					ui.click_timer=0.0d;
					ui.target_switch=1;
					time_out = 100d;
					state = WAIT_FOR_PLAYER_STATE;
				}
				
				time_out-=t;
				if (time_out < 0) {
					sm.DeleteShip(rescue.id);
					state = CLEAN_UP_STATE;
					victory_timer = 30000d/md.time_factor;
				}

			} else
			{
				ui.TurnOnTicker("XXX TARGET HAS BEEN LOST AT SEA XXX THE ABANDONED SHIP WAS LOST AT SEA XXX");
				ui.dist_string = " ";
				victory_timer = 0d;
				state = CLEAN_UP_STATE;
			}
			break;
		case WAIT_FOR_PLAYER_STATE:
			time_out-=t;
			if ((ui.at_target) && (time_out < 95d)){
				md.AddMoney(md.PLAYER_TEAM, bonus);
				completed = true;
				bonus = 0;
				state= CLEAN_UP_STATE;	
				ui.TurnOffTicker();
				ui.target_mode= ui.TARGET_NOTHING;
				// the player could finish without actually entering the harbor
			}
			if (rescue.type!=ship.stype.SHIP)
			{
				ui.TurnOnTicker("XXX TARGET HAS BEEN LOST AT SEA XXX THE ABANDONED SHIP WAS LOST AT SEA XXX");
				ui.dist_string = " ";
				victory_timer = 0d;
				state = CLEAN_UP_STATE;
			}
			break;
		
		case CLEAN_UP_STATE:
			if (ui.target_mode == ui.TARGET_OBJECTIVE) {
				ui.target_mode = ui.TARGET_NOTHING;
				if ((rescue!=null) && (rescue.type==ship.stype.SHIP)) {
					sm.DeleteShip(rescue.id);
				}
			}
			victory_timer+=t;
			if (victory_timer> 150d) {
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
