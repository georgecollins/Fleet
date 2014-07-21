import java.util.ArrayList;
import java.util.List;


public class FleetCommand {
	/*
	 *  This is the object that manages the players objectives
	 *  
	 *  
	 *  It sets a reticle target
	 *  It adds a message in the bottom ticker
	 *  It adds ships when the player has no opponents
	 *  sends in reinforcements
	 *  
	 *  Send a message when you start hostilities because you shot someone
	 *  Send a message when hostilities end
	 *  Send the player on missions
	 *  Organize groups to pick up crates
	 *  Organize attacks
	 */

	
	/* 
	 *  Have a place where you .. set up a freighter in the open ocean
	 *  Say "defend our freight blahtask
	 *  WHen you get close, start the frieghter on a path to a distant port
	 *  Send mixed ships to attack the whole way
	 *  When the attackers die (or end up too far away) send more
	 */
	int playersegx, playersegy;  // what segment is the player in?
	double playerx, playery;
	ship user;
	MissionData md;
	UserInput ui;
	ShipManager SMmat[][];
	IslandManager IMmat[][];
	Episode	CurrentEpisode;
	
	int episode_sequence;  // hack to vary the episodes
	
	int last_player_health;
	int last_player_ammo;
	
	int updatex, updatey;
	double sleep_timer;
	double episode_timer;
	double SOStimer; // how ling before you reply to SOS again
	
	// Objectives
	// The player has objectives
	//  The Fleet commander sets it up
	// goes through the states
	
	/*
	 *  Objectives
	 *  Go to port
	 *  Defend port
	 *  Attack fleet
	 *  Attack freighter
	 *  Defend Freighter
	 */
	
	int current_episode;
	int objective_state; // set up, in process, victory, defeat
	
	
	static final int FREE_OBJECTIVE = 0;
	static final int PORT_VISIT_EPISODE = 1;
	static final int DEFEND_FREIGHTER_EPISODE = 2;
	static final int DEFEND_PORT_OBJECTIVE = 3;
	static final int SINK_FREIGHTER_OBJECTIVE = 4;
	static final int SINK_FLEET_OBJECTIVE = 5;
	static final int PIRATE_OBJECTIVE = 6;
	static final int SINK_SHIP_EPISODE = 7;
	static final int FLEET_ATTACK_EPISODE = 8;
	static final int HULL_RESCUE_EPISODE = 9;
	static final int FISHING_EPISODE = 10;
	static final int CRATE_RACE_EPISODE = 11;
	static final int SINK_FREIGHTER_EPISODE = 12;
	static final int ENEMY_PORT_EPISODE = 13;
	
	static final int SETUP_STATE = 1;
	static final int MESSAGE_STATE = 2;
	static final int WAIT_FOR_PLAYER_STATE =3;
	static final int REMINDER_STATE  = 4;
	static final int FINISHED_STATE = 5;
			

	
	int port_count[][];
	List <island>port_island_list[][];

	int freighter_mission[][];  // have a freighter mission 
	
	static final int FREIGHTER_START = 0;
	static final int FREIGHTER_TRAVELING = 1;
	static final int FREIGHTER_DONE = 2;  // or sunk
	int freighter_id[][];
	

	FreighterProtectEpisode FPEpisode;
	PirateEpisode PEpisode;
	PortVisitEpisode PVEpisode;
	FleetAttackEpisode FAEpisodeEasy;
	FleetAttackEpisode FAEpisodeMedium;
	FleetAttackEpisode FAEpisodeHard;	
	SinkShipEpisode SSEpisodeEasy;
	SinkShipEpisode SSEpisodeMedium;
	SinkShipEpisode SSEpisodeHard;
	HullRescueEpisode HREpisode;
	FishingEpisode FEpisode;
	CrateRaceEpisode CREpisodeEasy;
	SinkFreighterEpisode SFEpisodeEasy;
	EnemyPortEpisode EPEpisode;

	
	public void SetUser(ship s) {
		user = s;
		if (s!=null) {
			playerx=s.xpos;
			playery=s.ypos;
		}
	}
	public void SetUserSegment(int segx, int segy) {
		playersegx=segx;
		playersegy=segy;
		if ((!ui.TickerShowing()) && (!MissionMessage()))
			ui.TurnOnTicker("XXX FLEET COMMAND MESSAGE XXX CAPTAIN SPENCER XXX DO YOUR HOMEWORK XXX REPEAT DO YOUR HOMEWORK XXX");
		
	}
	// is the player shooting
	public boolean CheckPlayerAmmo() {
		if (user!=null) {
			for (int count=0; count < user.component_count; count++) {
				if ((user.scomponent[count]!=null) && (user.scomponent[count].IsUsed()!=0) &&(user.scomponent[count].clip_count!=last_player_ammo)) {
					last_player_ammo=user.scomponent[count].clip_count;
					return true;
				}
			}
		}
		return false;
	}
	// is the health of the player changing 
	public boolean CheckPlayerHealth() {
		if (user!=null) {
			if (user.health!=last_player_health) {
				last_player_health= user.health;
				return true;
			}		
		}
		return false;
	}
	public boolean MissionMessage() {
		// I could return mission message
		return CurrentEpisode.GetMissionMessage();
	}
	
	public void Update(double t) {
		// update the target, update 
	
		if (((CheckPlayerAmmo()) || (CheckPlayerHealth())) && (!MissionMessage())) {
			sleep_timer=0.0d;
			ui.TurnOffTicker();
		}
		else {
			if (!MissionMessage()){
				sleep_timer=sleep_timer+t;
				if ((sleep_timer> 150.d) && (!ui.TickerShowing())) {
					if (md.relations[md.PLAYER_TEAM][1]!=md.ENEMIES) {
						ui.TurnOnTicker("XXX FLEET COMMAND MESSAGE XXX PATROL THE AREA XXX");	
					} else
						ui.TurnOnTicker("XXX FLEET COMMAND MESSAGE XXX DESTROY ALL SHIPS XXX");	
					sleep_timer=0.0d;
				}
			}
		}

		// Point toward real actual ships
		switch (ui.target_mode) {
		case UserInput.TARGET_NOTHING:
			if (user!=null){ 
				ui.SetTargetXY(user.xpos, user.ypos);
				ui.past_mode=ui.TARGET_NOTHING;
			}
			break;
		case UserInput.TARGET_PORT:
			if (ui.target_switch>0) {
				if (ui.past_mode==ui.TARGET_PORT) {
					SwitchPort();
				} else
					FindNearestPort();
			} else
				ShowPortTarget();
			break;
		case UserInput.TARGET_ENEMY:
			FindNearestEnemy();
			ui.past_mode=ui.TARGET_NOTHING;
			break;
		}
		

	
		if (current_episode!= ENEMY_PORT_EPISODE) {
			// update it anyway!
			EPEpisode.Update(t);
		}
		
		// Pick Episode is the mission dispatcher
		// this is in case I need to do something every frame like draw a special map
		// update pirates or freighters
		switch (current_episode) {
			case FREE_OBJECTIVE:
				FreighterManager(updatex, updatey);
				updatex++;
				if (updatex > md.segment_width-1) {			
					updatex=0;
					updatey++;
					if (updatey > md.segment_height-1) {
						updatey=0;
					}
				}
				objective_state= SETUP_STATE;
				break;
			case PORT_VISIT_EPISODE:
			case DEFEND_FREIGHTER_EPISODE:
			case PIRATE_OBJECTIVE:	
			case SINK_SHIP_EPISODE:	
			case FLEET_ATTACK_EPISODE:
			case HULL_RESCUE_EPISODE:	
			case FISHING_EPISODE:	
			case CRATE_RACE_EPISODE:	
			case SINK_FREIGHTER_EPISODE:	
			case ENEMY_PORT_EPISODE:	
				CurrentEpisode.Update(t);
				if (CurrentEpisode.state== Episode.FINISHED_STATE) {
					current_episode=FREE_OBJECTIVE;
					episode_timer=0.0d;
				}
				break;


			case DEFEND_PORT_OBJECTIVE:
				break;
			case SINK_FREIGHTER_OBJECTIVE:
				break;
			case SINK_FLEET_OBJECTIVE:
				break;

		}

		current_episode= PickEpisode2(t);//DEFEND_FREIGHTER_OBJECTIVE;

		CheckSOS(t);
		CheckFleet();
		md.IncrementWarTimer(t);
		
	}
	
	
	public int PickEpisode2(double t) {
		if (current_episode!= FREE_OBJECTIVE)
			return current_episode;
	
		episode_timer+=t;
		
		if (episode_timer > 5000/md.time_factor) {  // i took out a zero to test this
			if (PEpisode.CanStart()) {
				CurrentEpisode=PEpisode;
				CurrentEpisode.Start();
				current_episode=PIRATE_OBJECTIVE;	
				episode_timer=0d;
				return current_episode;
			}	
		
			switch (episode_sequence) {
			case 0:
				if (PVEpisode.CanStart()) {
					PVEpisode.PutOnDeck();
				}
				if (FEpisode.CanStart()) {
					FEpisode.PutOnDeck();
				}
				if (SFEpisodeEasy.CanStart()) {
					SFEpisodeEasy.PutOnDeck();
				}
				if (SSEpisodeEasy.CanStart()) {
					SSEpisodeEasy.SetDifficulty(Episode.EASY);
					SSEpisodeEasy.title = "INTERCEPT ENEMY MERCHANTMAN";
					SSEpisodeEasy.PutOnDeck();
				}
				if (HREpisode.CanStart()) {
					HREpisode.PutOnDeck();
				}
				if (SSEpisodeMedium.CanStart()) {
					SSEpisodeMedium.SetDifficulty(Episode.MEDIUM);
					SSEpisodeMedium.title = "INTERCEPT ENEMY PATROL BOAT";
					SSEpisodeMedium.PutOnDeck();
				}
				if (FPEpisode.CanStart()) {
					FPEpisode.SetDifficulty(Episode.EASY);
					FPEpisode.PutOnDeck();
				}
				if (FAEpisodeEasy.CanStart()) {
					FAEpisodeEasy.SetDifficulty(Episode.EASY);
					FAEpisodeEasy.title = "INTERCEPT MERCHANTMAN FLEET";
					FAEpisodeEasy.PutOnDeck();
				}
				if (SSEpisodeHard.CanStart()) {
					SSEpisodeHard.SetDifficulty(Episode.HARD);
					SSEpisodeHard.title = "INTERCEPT ENEMY WARSHIP";
					SSEpisodeHard.PutOnDeck();
				}
				if (CREpisodeEasy.CanStart()) {
					CREpisodeEasy.PutOnDeck();
				}
				if (FAEpisodeMedium.CanStart()) {
					FAEpisodeMedium.SetDifficulty(Episode.MEDIUM);
					FAEpisodeMedium.title = "INTERCEPT MIXED FLEET";
					FAEpisodeMedium.PutOnDeck();
				}
				if (EPEpisode.CanStart()) {
					//it is always hard
					EPEpisode.PutOnDeck();
				}
				episode_sequence++;
				break;
				
			// how do I open up the objective	
			}
			
			if ((ui.objective_status== ui.OBJECTIVE_INACTIVE) && (md.episode_list.size() > 0)) {
				ui.objective_status = ui.OBJECTIVE_ACTIVE;
			}
		}
		
		if (PVEpisode.state== SETUP_STATE) {
			CurrentEpisode = PVEpisode;
			current_episode = PORT_VISIT_EPISODE;
		}
		
		if (FEpisode.state == SETUP_STATE) {
			CurrentEpisode = FEpisode;
			current_episode = FISHING_EPISODE;
		}
		if (SFEpisodeEasy.state == SETUP_STATE) {
			CurrentEpisode = SFEpisodeEasy;
			current_episode = SINK_FREIGHTER_EPISODE;
		}
		
		if (SSEpisodeEasy.state== SETUP_STATE) {
			CurrentEpisode = SSEpisodeEasy;
			current_episode = SINK_SHIP_EPISODE;
		}
		if (HREpisode.state == SETUP_STATE) {
			CurrentEpisode = HREpisode;
			current_episode = HULL_RESCUE_EPISODE;
		}
		if (SSEpisodeMedium.state== SETUP_STATE) {
			CurrentEpisode = SSEpisodeMedium;
			current_episode = SINK_SHIP_EPISODE;
		}
		if (FPEpisode.state == SETUP_STATE) {
			CurrentEpisode = FPEpisode;
			current_episode = DEFEND_FREIGHTER_EPISODE;
		}
		if (FAEpisodeEasy.state == SETUP_STATE) {
			CurrentEpisode = FAEpisodeEasy;
			current_episode = FLEET_ATTACK_EPISODE;
		}
		
		if (SSEpisodeHard.state == SETUP_STATE) {
			CurrentEpisode = SSEpisodeHard;
			current_episode = SINK_SHIP_EPISODE;
		}
		if (CREpisodeEasy.state == SETUP_STATE) {
			CurrentEpisode = CREpisodeEasy;
			current_episode = CRATE_RACE_EPISODE;
		}
		if (FAEpisodeMedium.state == SETUP_STATE) {
			CurrentEpisode = FAEpisodeMedium;
			current_episode = FLEET_ATTACK_EPISODE;
		}		
		if (EPEpisode.state == MESSAGE_STATE) {
			CurrentEpisode = EPEpisode;
			current_episode = ENEMY_PORT_EPISODE;
		}
		
		return current_episode;	
	}
	
	
	public int PickEpisode(double t) {

		
		if (current_episode!= FREE_OBJECTIVE)
			return current_episode;

		episode_timer+=t;
		
		if (episode_timer > 45000/md.time_factor) {  // i took out a zero to test this
			if (PEpisode.CanStart()) {
				CurrentEpisode=PEpisode;
				CurrentEpisode.Start();
				current_episode=PIRATE_OBJECTIVE;	
				episode_timer=0d;
				return current_episode;
			}
			
			switch (episode_sequence) {
				case 0:
					
					if (PVEpisode.CanStart()) {

						CurrentEpisode = PVEpisode;
						CurrentEpisode.Start();
					
						current_episode = PORT_VISIT_EPISODE;
						episode_sequence++;
						return current_episode;
					}
					
					break;
				case 1:	
					if ((SSEpisodeEasy.CanStart())) {
						CurrentEpisode = SSEpisodeEasy;
						CurrentEpisode.SetDifficulty(Episode.EASY);
						CurrentEpisode.Start();
						current_episode = SINK_SHIP_EPISODE;
						episode_sequence++;
						return current_episode;
					}
					break;
				case 2:
					if (HREpisode.CanStart()) {
						CurrentEpisode = HREpisode;
						CurrentEpisode.SetDifficulty(Episode.EASY);
						CurrentEpisode.Start();
						current_episode = HULL_RESCUE_EPISODE;
						episode_sequence++;
						return current_episode;
					}
					break;
				case 3:
					if ((SSEpisodeMedium.CanStart())) {
						CurrentEpisode = SSEpisodeMedium;
						CurrentEpisode.SetDifficulty(Episode.MEDIUM);
						CurrentEpisode.Start();
						current_episode = SINK_SHIP_EPISODE;
						episode_sequence++;
						return current_episode;
					}
					break;
				case 4:
					if ((FPEpisode.CanStart())) {

						CurrentEpisode=FPEpisode;
						CurrentEpisode.Start();
						CurrentEpisode.SetDifficulty(Episode.EASY);
						current_episode = DEFEND_FREIGHTER_EPISODE;
						episode_sequence++;
						return current_episode;
					}
					break;
				case 5:
					if ((SSEpisodeHard.CanStart())) {
						CurrentEpisode = SSEpisodeHard;
						CurrentEpisode.SetDifficulty(Episode.HARD);
						CurrentEpisode.Start();
						current_episode = SINK_SHIP_EPISODE;
						episode_sequence++;
						return current_episode;
					}
					break;
				case 6:
					if (HREpisode.CanStart()) {
						CurrentEpisode = HREpisode;
						CurrentEpisode.SetDifficulty(Episode.MEDIUM);
						CurrentEpisode.Start();
						current_episode = HULL_RESCUE_EPISODE;
						episode_sequence++;
						return current_episode;
					} else
						episode_sequence++;  // cause you can only do it with one ship
					break;
				case 7:
					if ((FAEpisodeEasy.CanStart())) {
						CurrentEpisode= FAEpisodeEasy;
						CurrentEpisode.SetDifficulty(Episode.EASY);
						CurrentEpisode.Start();
						current_episode=FLEET_ATTACK_EPISODE;
						episode_sequence=0;// just go around again
						return current_episode;
					}
					break;
			}



		}
		
/*

		if ((current_episode!=PIRATE_OBJECTIVE) && (current_episode!=FLEET_ATTACK_EPISODE)) {
			episode_timer+=t;
			if ((episode_timer> 25000/md.time_factor) && (PEpisode.CanStart())) {
				CurrentEpisode=PEpisode;
				CurrentEpisode.Start();
				current_episode=PIRATE_OBJECTIVE;
			}
			
			if ((episode_timer > 30000/md.time_factor) &&  (current_objective== FREE_OBJECTIVE) && (PVEpisode.CanStart())) {
				CurrentEpisode = PVEpisode;
				CurrentEpisode.Start();
				current_objective = PORT_VISIT_OBJECTIVE;
			}
// was 600000 below
			if (episode_timer> 60000/md.time_factor) {
				if ((current_objective==FREE_OBJECTIVE) && (FPEpisode.CanStart())) {
					current_objective=DEFEND_FREIGHTER_OBJECTIVE;
					CurrentEpisode=FPEpisode;
					CurrentEpisode.Start();
				} else
					episode_timer=0.0d;
			}
		} else
			episode_timer=0.0d;

		

		*/
		return current_episode;

	}
	
	void UpdateShips() {
		for (int segx=0;  segx < md.segment_width; segx++) 
			for (int segy=0; segy< md.segment_height; segy++)
				for (int count=0; count < SMmat[segx][segy].scount; count++) {
					ship s=SMmat[segx][segy].slist[count];  // so I don't have to keep typing it
					if ((s!=null) && (s.type==ship.stype.SHIP)) {
							// before you move it
						if (s.CheckPM(SMmat[segx][segy].pm)!=true) {
							s.SetPM(SMmat[segx][segy].pm); // this is a fail because of an error
						}
						
						md.MoveShip(s, segx, segy);  

					}
				}
	}
	
	void FreighterManager(int i, int j) {
		
		// this is where the game sticks
		//for (int i=0; i < md.segment_width; i++)
		//	for (int j=0; j < md.segment_width; j++) {
				
			
				if ((freighter_mission[i][j]==FREIGHTER_START) && (port_count[i][j]>1)) {
					FreighterPath(i, j);
				}
				if ((freighter_mission[i][j]==FREIGHTER_TRAVELING)  && (port_count[i][j]>1)) {
					CheckFreighter(i, j);
				}

	}
	

	public void FreighterPath(int xseg, int yseg) {
		IslandManager im= IMmat[xseg][yseg];
		double startx=0;
		double starty=0; 
		double endx= 0;
		double endy=0;
		int icount=0;
		
		
		if (port_island_list[xseg][yseg].size()<2)  {
		
			if (port_count[xseg][yseg]<2)
				return;  // there aren't two
			FillPortIslandList(xseg, yseg);
		}
		island first = port_island_list[xseg][yseg].get(0);
		island second = port_island_list[xseg][yseg].get(1);
		startx=first.world_portx;
		starty=first.world_porty;
		endx=second.world_portx;
		endy=second.world_porty;
		
		port_island_list[xseg][yseg].remove(0); // pop it off

		Path p=im.GetPath(startx, starty, endx, endy);

		// now add a freighter to follow the path
		//p.DrawPath();
		ShipManager sm= SMmat[xseg][yseg];
		if (p==null) {
			freighter_mission[xseg][yseg]=FREIGHTER_TRAVELING;
			return;
		}
			
		WorldThing wt=new WorldThing();
		wt.xpos=p.xmat[0]-im.offsetx;
		wt.ypos=p.ymat[0]-im.offsety;
		freighter_id[xseg][yseg]=sm.AddShip(wt);
		ship fship=sm.GetShip(freighter_id[xseg][yseg]);
		if (fship!=null) {
			fship.BuildFreighter();
			
			fship.AttachAI(SMmat[xseg][yseg], IMmat[xseg][yseg], md);
	
			fship.SetTeam((int)(Math.random()*(md.MAX_TEAMS-1)+1) , md);  // team was = (int)(Math.random()*(md.MAX_TEAMS-1)+1) 
			fship.ai.StartFollowPath(fship, p, 50 , AIControl.PATH_ONE_TIME);
		}
		freighter_mission[xseg][yseg]=FREIGHTER_TRAVELING;
		
	}
	
	public void CheckFreighter(int xseg, int yseg) {
		ShipManager sm= SMmat[xseg][yseg];  // where I am at
		
		// If the freighter is dead start again
		ship fs = sm.GetShip(freighter_id[xseg][yseg]);
		if ((fs==null) || (fs.type!=ship.stype.SHIP)) {
			freighter_mission[xseg][yseg]=FREIGHTER_START;
		}
		// If the freighter reached its home port, remove it and start again
		// NEED TO ADD
	}
	
	
	public void FindNearestPort() {

	
		// should be current port, not next
		
		md.MakePortList();

		ShowPortTarget();
		
		ui.target_switch=0;
		ui.past_mode= ui.TARGET_PORT;
	}
	
	public void SwitchPort() {
		md.GetNextPort();
		ui.target_switch=0;
	}
	
	public void ShowPortTarget() {
		island isl=md.GetCurrentPort();
		ui.SetTargetXY(isl.world_portx, isl.world_porty);
		ui.arrow_string= isl.name;
		
		if (user==null) {
				ui.dist_string = " ";  // figure later	
				return;
		} 
		double dist=user.DistanceSq(isl);
		ui.dist_string=String.valueOf((int) Math.sqrt(dist));
		
	}
	
	
	public void FindNearestEnemy() {
		if (user==null) return;
		int nearest =0;
		double nearx=user.xpos; 
		double neary=user.ypos;
		double near_dist=100000000* 100000000;

		
		for (int segx=0;  segx < md.segment_width; segx++) 
			for (int segy=0; segy< md.segment_height; segy++)
			{
				ShipManager sm= SMmat[segx][segy];  // to save typing
				for (int i=0; i < sm.scount; i++) 
					if ((sm.slist[i].team!=md.PLAYER_TEAM) && (sm.slist[i].type==ship.stype.SHIP)) {  // should be enemy to be figured out
						double dx=user.xpos-sm.slist[i].xpos;
						double dy=user.ypos-sm.slist[i].ypos;
						double dist= dx*dx+dy*dy;
						if (dist < near_dist) {
							near_dist=dist;
							nearx=sm.slist[i].xpos;
							neary=sm.slist[i].ypos;
							
						}
				}
			}
		
		ui.SetTargetXY(nearx, neary);
		ui.arrow_string= " ";
		ui.dist_string= " ";
	}

	
	public void CheckSOS(double t) {
		if ((md.SOSsignal!=0) && (SOStimer<0.0d) && (md.SOSteam!=md.PLAYER_TEAM)){
			GetSOS(md.SOSx, md.SOSy, md.SOSteam);
			md.ClearSOS();
			SOStimer=300.0d;
			
		} else
			SOStimer-=t;
	}
	
	public void CheckFleet() {
		md.userfleet.StartShipList();
		while (md.userfleet.HasNextShip()) {
			ship fship= md.userfleet.GetNextShip();
			if (fship.type==ship.stype.WRECK) {
				md.userfleet.RemoveShip(fship.id);
				break;
			}
		}
	}
	
	public void GetSOS(double x, double y, int team) {
		// make a rescue ship
		// send it where it is supposed to go 
		// pick a spawn point
		double spawnx =0;
		double spawny = 0;
		
		ship player = SMmat[playersegx][playersegy].GetShip(md.userid);
		
		
		double bestdist= 0;
		double bestspawnx=0;
		double bestspawny=0;
		double dist=0;
		for (int i=-1; i < 2; i++)
			for (int j=-1; j< 2; j++) 
			if ((i!=0) || (j!=0)){
				spawnx=x+1000*i;
				spawny=y+1000*j;
				
				// is it clear?
				// is it in bounds?  
				// if so break
				
				PathData pd=IMmat[playersegx][playersegy].GetClosestOpen(spawnx, spawny);

					
				
				if ((pd!=null) && (IMmat[playersegx][playersegy].InBounds(pd.x, pd.y))) {
					if (player!=null) {
						dist=(pd.x-player.xpos)*(pd.x-player.xpos)+(pd.y-player.ypos)*(pd.y-player.ypos);
					} else dist=1;
					if (dist> bestdist) {  // as far as possible from the player so he doesn't see it
						bestspawnx=pd.x;
						bestspawny=pd.y;
					}
				}
				
			}
		
		Path p=IMmat[playersegx][playersegy].GetPath(bestspawnx, bestspawny, x, y);
		if (p==null)
			return;
		
		
		
		WorldThing wt=new WorldThing();
		wt.xpos=spawnx-IMmat[playersegx][playersegy].offsetx;
		wt.ypos=spawny-IMmat[playersegx][playersegy].offsety;
		int rid=SMmat[playersegx][playersegy].AddShip(wt);
		ship rescue=SMmat[playersegx][playersegy].GetShip(rid);
		
		if (rescue!=null) {
			rescue.BuildShip();
			

			rescue.AttachAI(SMmat[playersegx][playersegy], IMmat[playersegx][playersegy], md);
	
			rescue.SetTeam(team, md);  // team was = (int)(Math.random()*(md.MAX_TEAMS-1)+1) 
			rescue.ai.StartFollowPath(rescue, p, 50, AIControl.PATH_ONE_TIME);
		}

		
			
	}
	private void FillPortIslandList(int xseg, int yseg) {
		for (int n=0; n < IMmat[xseg][yseg].island_count; n++)
			if (IMmat[xseg][yseg].islands[n].has_port==1) {
				// don't double add it if it is already there
				// this can get called when there is only one island left in the list
				// don't want to make it n+1 every time
				if (!port_island_list[xseg][yseg].contains(IMmat[xseg][yseg].islands[n]))
					port_island_list[xseg][yseg].add(IMmat[xseg][yseg].islands[n]);

			}

	}
	public void Init(ShipManager smm[][],IslandManager imm[][], UserInput uin, int user_segx, int user_segy  )  {
		playersegx=user_segx;
		playersegy=user_segy;
		SMmat=smm;
		IMmat=imm;
		ui=uin;

		
		port_island_list = new ArrayList[md.segment_width][md.segment_height];
		
		for (int i=0; i < md.segment_width; i++)
			for (int j=0; j < md.segment_width; j++) {
				port_island_list[i][j] = new ArrayList<island>();
				int count =0;
				// port count could be a function of Island Manager
				for (int n=0; n < IMmat[i][j].island_count; n++)
					if (IMmat[i][j].islands[n].has_port==1) {
						port_island_list[i][j].add(IMmat[i][j].islands[n]);
						count++;
					}
				port_count[i][j]=count;
				
			}
				
		FPEpisode.Init(md, ui, IMmat, SMmat);
		PEpisode.Init(md, ui, IMmat, SMmat);
		PVEpisode.Init(md, ui, IMmat, SMmat);
		FAEpisodeEasy.Init(md, ui, IMmat, SMmat);
		FAEpisodeMedium.Init(md, ui, IMmat, SMmat);
		SSEpisodeEasy.Init(md, ui, IMmat, SMmat);
		SSEpisodeMedium.Init(md, ui, IMmat, SMmat);
		SSEpisodeHard.Init(md, ui, IMmat, SMmat);
		HREpisode.Init(md, ui, IMmat, SMmat);
		FEpisode.Init(md, ui, IMmat, SMmat);
		CREpisodeEasy.Init(md, ui, IMmat, SMmat);
		SFEpisodeEasy.Init(md, ui, IMmat, SMmat);
		EPEpisode.Init(md, ui, IMmat, SMmat);
		CurrentEpisode = FPEpisode;
		
	}
	
	

	
	FleetCommand(MissionData mdin) {
		md=mdin;
		user=null;
		sleep_timer=0;
		freighter_mission = new int[md.segment_width][md.segment_height];
		freighter_id = new int[md.segment_width][md.segment_height];
		port_count = new int[md.segment_width][md.segment_height];	
		episode_timer=0;
		SOStimer=0;
		updatex=0;
		updatey=0;
		FPEpisode = new FreighterProtectEpisode();
		PEpisode = new PirateEpisode();
		PVEpisode = new PortVisitEpisode();
		FAEpisodeEasy = new FleetAttackEpisode();
		SSEpisodeEasy = new SinkShipEpisode();
		SSEpisodeMedium = new SinkShipEpisode();
		SSEpisodeHard = new SinkShipEpisode();
		HREpisode = new HullRescueEpisode();
		FAEpisodeEasy = new FleetAttackEpisode();
		FAEpisodeMedium = new FleetAttackEpisode();
		FEpisode = new FishingEpisode();
		CREpisodeEasy = new CrateRaceEpisode();
		SFEpisodeEasy = new SinkFreighterEpisode();
		EPEpisode = new EnemyPortEpisode();
		
		episode_sequence = 0;

	}
}
