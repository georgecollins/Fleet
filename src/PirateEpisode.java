

public class PirateEpisode extends Episode {
	// in this episode the player with cargo is attacked by pirates
	int challenge;  // easy = 0, hard =1, harder =2 
	
	ship attacker; // later make this an array.. for now just one
	double time_out;
	public boolean CanStart() {
		// if the player has a cargo or, the fleet has cargo
		
		ship user = md.FindShip(md.userid);
		if (user==null)
			return false;
		if (user.HasCargoLoaded())
			return true;
		md.userfleet.StartShipList();
		
		while (md.userfleet.HasNextShip()) {
			//port_island.pd.AddShip(md.userfleet.GetNextShip());
			ship s =md.userfleet.GetNextShip();
			// sometime s has cargo that is all null
			// it is type wreck - What do we do
			if (s.HasCargoLoaded())
				return true;
		}

		return false;
	}
	
	public int Update(double t) {
		ship user = md.FindShip(md.userid);
		if (user==null)
			return 0;
		IslandManager im=imm[md.GetSegmentX(user.xpos)][md.GetSegmentY(user.ypos)];	
		ShipManager sm=	smm[md.GetSegmentX(user.xpos)][md.GetSegmentY(user.ypos)];
	
		PathData fpd;
		
		switch (state) {
			case SETUP_STATE:
				// find a place by the user
				double place_dist=Math.sqrt(md.win_width*md.win_height*1.5d);
				double testx = user.xpos+Math.cos(user.rotation+Math.PI/2)*place_dist;  // or could be xvel
				double testy = user.ypos+Math.sin(user.rotation+Math.PI/2)*place_dist;  // yvel
				fpd=im.GetClosestOpen(testx, testy);
				if (fpd==null) {
					return SETUP_STATE;

				}
				if ((fpd.x-user.xpos)*(fpd.x-user.xpos)+(fpd.y-user.ypos)*(fpd.y-user.ypos) < md.win_width*md.win_height) {
					// not far enough away
					return SETUP_STATE;

				}
				
				attacker = new ship(md.GetUniqieID(md.SHIP));
				HullData hdf2 =new HullData();
				hdf2.BuildSmallHull(hdf2);
				attacker.BuildAttackShip(hdf2);  // need build ship small
				
				//attacker.BuildShip();

				attacker.xpos=fpd.x;
				attacker.ypos=fpd.y;
				
				sm.AddShip(attacker);
				attacker.AttachAI(sm, im, md);
				attacker.ai.controlid=attacker.id;  // scandalous that this can be wrong
				attacker.SetTeam(3, md);
				md.relations[md.PLAYER_TEAM][3]=md.ENEMIES;			
				md.relations[3][md.PLAYER_TEAM]=md.ENEMIES;
				
		
				attacker.ai.StartAttackTask(attacker, user.id);
				ui.objective_status= UserInput.OBJECTIVE_INACTIVE;			
				md.can_feud=1;
				mission_message=true;
				ui.TurnOnTicker("XXX BLUE FLEET PIRATES SIGHTED NEAR BY XXX");
				state=MESSAGE_STATE;
				break;
			case MESSAGE_STATE:
				// if my ship is sunk or the other guy is no longer carrying freight
				if ((attacker.type != ship.stype.SHIP) || (!CanStart())){
					time_out = 15000d/md.time_factor;
					state= CLEAN_UP_STATE;
					break;
				}
				if (attacker.DistanceSq(user) > md.win_width*md.win_width) {
					time_out = 15000d/md.time_factor;
					state=CLEAN_UP_STATE;
					break;
				}
				break;
			case CLEAN_UP_STATE:	
				time_out-=t;
				if (time_out < 0) {
					ui.objective_status= UserInput.OBJECTIVE_ACTIVE;
					mission_message= false;
					md.can_feud=0;
					state= FINISHED_STATE;
				}
				break;
			case FINISHED_STATE:

				break;
				
				
		}
		return state;
	}
}
