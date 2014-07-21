
public class FreighterProtectEpisode extends Episode {
	ship obj_ship;
	ship attacker;
	double sleep_timer;
	double attack_delay=0;
	PathData fpd;
	Path fpath;
	island target_port;
	
	
	
	public void Init(MissionData mdin, UserInput uin, IslandManager immin[][], ShipManager smmin[][]) {

		super.Init(mdin, uin, immin, smmin);
		
		state = ON_DECK;
		title = "ESCORT FREIGHTER";
		description = "RENDEVOUS WITH OUR FLEETS FFREIGHTER AND PROTECT IT AS IT HEADS TO PORT";
		bonus = 200; // should change with difficulty

	}
	
	public boolean CanStart() {
		// test to make sure there are two ports
		// you can place the freighter
		// you can make a path
		
		boolean canstart = true;
		ship user = md.FindShip(md.userid);
		if (user==null)
			return false;
		IslandManager im=imm[md.GetSegmentX(user.xpos)][md.GetSegmentY(user.ypos)];	
		ShipManager sm=	smm[md.GetSegmentX(user.xpos)][md.GetSegmentY(user.ypos)];
		
		fpd= new PathData();
		
		double place_dist=Math.min(md.world_width*2, 3000);
		

		if (im.PortCount() < 2)
			return false; 
		// build a freighter
		// stick it in the open place
		// keep a pointer to it

		
		for (double rad=0; rad < Math.PI*2d; rad=rad+0.5d) {
			double testx = user.xpos+Math.cos(rad)*place_dist;
			double testy = user.ypos+Math.sin(rad)*place_dist;
			fpd=im.GetClosestOpen(testx, testy);
			if (fpd!=null) {
				break;
			}
		}
		if (fpd==null)
			return false;  // we never found a point
		
		WorldThing start = new WorldThing();
		start.xpos=fpd.x;
		start.ypos=fpd.y;
		target_port=md.FindPort(im, start, md.FAR_DISTANCE, true);
		if (target_port==null)
			return false;
		fpath=im.GetPath(start.xpos, start.ypos, target_port.world_portx, target_port.world_porty);
		
		
		
		if (fpath==null) {
		//	port=md.FindPort(im, start, md.CLOSE_DISTANCE);	
		//	fpath=im.GetPath(obj_ship.xpos, obj_ship.ypos, port.world_portx, port.world_porty);
			// I should check to make sure there is a path when I put the ship down and place the 
			// ship along with it. 
			return false;
		}
		

		
		return true;
	}
	
	public int Update(double t) {
		ship user = md.FindShip(md.userid);
		if (user==null)
			return 0;
		
		IslandManager im= md.GetShipIM(user);  //imm[md.GetSegmentX(user.xpos)][md.GetSegmentY(user.ypos)];	
		ShipManager sm=	md.GetShipSM(user);
	
		double place_dist;

		switch (state) {
		case SETUP_STATE:
			// find an open space about 2 win_widths from the player
			ui.objective_status= UserInput.OBJECTIVE_INACTIVE;

			obj_ship = new ship(md.GetUniqieID(md.SHIP));

			HullData hdf =new HullData();
			hdf.BuildWideHull(24d,3, 1,1 );;
			obj_ship.BuildFreighter(hdf);

			// because it is mroe durable hopefully
			// obj_ship.BuildFreighter();
			obj_ship.SetTeam(md.PLAYER_TEAM, md);
			obj_ship.xpos=fpd.x;
			obj_ship.ypos=fpd.y;
			obj_ship.rotation= Math.atan2(fpd.x-fpath.xmat[1], fpd.y-fpath.ymat[1]) + Math.PI/2;
			
			sm.AddShip(obj_ship);
			obj_ship.AttachAI(sm, im, md);
			obj_ship.ai.controlid  = obj_ship.id;
			obj_ship.ai.StartWait(50, 5000d);  // priority =50, duration 5000
			obj_ship.SetTeam(md.PLAYER_TEAM, md);
			
			// add freight?  
			objective = obj_ship;
			state = MESSAGE_STATE;
			sleep_timer=0.0d;
			mission_message=true;
			String msg = "XXX FLEET ORDERS XXX  ESCORT FREIGHTER UFS BELLEPHON TO PORT " + target_port.name + " XXX";
			ui.TurnOnTicker(msg);
			md.can_feud=1;
			break;

		case MESSAGE_STATE:
			// say DEFEND FREIGHTER
			// make the interface point to the freighter
			
			
			// have this go on again off again, on a timer, in case the player wants
			// to ignore it
			ui.target_mode = ui.TARGET_OBJECTIVE;
			ui.SetTargetXY(objective.xpos, objective.ypos);
			ui.arrow_string= "PROTECT";
			ui.dist_string= " ";
			
			// when the distance between the player and the ship
			// is below win_width, make the ship go
			if ((user!=null) &&
					(user.DistanceSq(obj_ship) < (md.win_width*md.win_width/9))) {
				
				state= WAIT_FOR_PLAYER_STATE;  // this is all wrong in terms of
				// what state is next.  I wait for the player in this state

				// port is sometimes null
				obj_ship.ai.RemoveTaskType(AIControl.TASK_WAIT);
				obj_ship.ai.StartFollowPath(obj_ship, fpath, 50, AIControl.PATH_ONE_TIME);  
				
				attack_delay=0;
			}
				
			
			break;
		case WAIT_FOR_PLAYER_STATE:
			// wait until the player is close
			// spawn in a bunch enemies coming to get the freighter.  
			
			if ((attacker==null) && (attack_delay < 0.0d)) {
				
				place_dist=md.win_width*2;
				// if onpont +1 < mypath.points
				
				// morph point
				// morph ahead of the freighter	
				Path pth = obj_ship.ai.GetCurrentPath();
				int op = obj_ship.ai.GetOnPoint();
				
				int morph_point=0; // where you start the attacker
				
				double best_dist=0;
				for (int i=0; i < pth.numpoints; i++) {
					double d = (pth.xmat[i]-user.xpos)*(pth.xmat[i]-user.xpos)+ (pth.ymat[i]-user.ypos)*(pth.ymat[i]-user.ypos);
					if ((d> best_dist) && (best_dist< (md.win_width*md.win_height *5.0d))) {
						morph_point=i;
						best_dist=d;
					}
				}
				

				fpd.x=pth.xmat[morph_point];
				fpd.y=pth.ymat[morph_point];

				// build an attacker
				// stick it in the open place
				// keep a data();
				/*
				for (double rad=0; rad < Math.PI*2d; rad=rad+0.5d) {
					double testx = obj_ship.xpos+Math.cos(rad)*place_dist;
					double testy = obj_ship.ypos+Math.sin(rad)*place_dist;
					pn=im.GetClosestOpen(testx, testy);
					if ((pn!=null) && 
							((pn.x-obj_ship.xpos)*(pn.x-obj_ship.xpos)+(pn.y-obj_ship.ypos)*(pn.y-obj_ship.ypos) > 10000)) {
						break;
					}
				}*/	
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
				attacker.SetTeam(2, md);
				md.relations[md.PLAYER_TEAM][2]=md.ENEMIES;			
				md.relations[2][md.PLAYER_TEAM]=md.ENEMIES;
				attacker.ai.StartAttackTask(attacker, obj_ship.id);

				attack_delay = 20000d/md.time_factor;  //delay reinforcements
	
			}
			else 
			{
				if ((attacker!=null) && (attacker.type!=ship.stype.SHIP)) {
					attack_delay=20000d/md.time_factor;
				
					attacker=null;
				} else { 
					attack_delay-=t;
				}
			}
			
			if (obj_ship.type==ship.stype.SHIP) {
				ui.SetTargetXY(objective.xpos, objective.ypos);
				ui.arrow_string= "PROTECT";
				ui.dist_string= " ";
			} else {
				if ((md.GotToPortSignal!=0) && (md.freighterid==obj_ship.id)) {
					// 
					ui.target_mode= ui.TARGET_NOTHING;
					ui.arrow_string= " ";
					ui.dist_string= " ";
					sleep_timer=0.0d;
					completed = true;
					ui.TurnOnTicker("XXX UFS BELLEPHON SAFELY IN PORT XXX CONGRATULATIONS CAPTAIN XXX");
					md.AddMoney(md.PLAYER_TEAM, bonus);
				}
				state = LAST_MESSAGE_STATE;
			}
			break;
		case REMINDER_STATE :
			break;
		case LAST_MESSAGE_STATE:
			md.can_feud=0;
			sleep_timer+=t;
			if (sleep_timer>20000/md.time_factor) {
				ui.TurnOffTicker();
				mission_message=false;
				ui.objective_status= UserInput.OBJECTIVE_ACTIVE;
				state=FINISHED_STATE;
			}
			break;
		case FINISHED_STATE:
			break;

		}


		return state;
	}
	
	
}
