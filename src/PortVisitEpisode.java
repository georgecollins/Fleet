
public class PortVisitEpisode extends Episode {
	
	
	island goal_port;
	
	public void Init(MissionData mdin, UserInput uin, IslandManager immin[][], ShipManager smmin[][]) {
		super.Init(mdin, uin, immin, smmin);
		completed=false;
		title = "PORT VISIT";
		description = "CAPTURE THE NEAREST PORT BY FOLLOWING THE ARROW TO ANCHOR ICON";
		bonus = 25;
		state = ON_DECK;
	}
	
	public boolean CanStart() {
		// For now lets always have this mission
		/*
		if (ui.target_mode== ui.TARGET_PORT)
			return false;
		if (has_happened== true)
			return false;
		*/
		if (completed) 
			bonus = 0;
		return true;
	}
	public int Update(double t) {
		switch (state) {
		case SETUP_STATE:
				ui.target_mode= ui.TARGET_PORT;
				ui.click_timer=0.0d;
				ui.target_switch=1;
				state= MESSAGE_STATE;
				ui.objective_status= UserInput.OBJECTIVE_INACTIVE;
				break;
		case MESSAGE_STATE:
			// it takes a frame for arrow string to fill
			String s = "XXX CAPTURE THE PORT AT " + ui.arrow_string + " BY ENTERING THE HARBOR XXX";
			mission_message=true;
			ui.TurnOnTicker(s);
			state = WAIT_FOR_PLAYER_STATE;
			break;
		case WAIT_FOR_PLAYER_STATE:
			ship shp= md.FindShip(md.userid);
			if (shp== null)
				state=FINISHED_STATE;
			if (ui.at_target) {
				state= CLEAN_UP_STATE;	
				completed=true;
				ui.TurnOffTicker();
				ui.target_mode= ui.TARGET_NOTHING;
				// the player could finish without actually entering the harbor
			}
			break;
		case CLEAN_UP_STATE:
			mission_message = false;
			ui.objective_status= UserInput.OBJECTIVE_ACTIVE;
			state = FINISHED_STATE;
			md.AddMoney(md.PLAYER_TEAM, bonus);
			bonus = 0;  // you only get it once
			break;
		case FINISHED_STATE:


			break;
			
		}
		return 0;
	}
}
