
public class FishingEpisode extends Episode {
	
	// spawn a bunch of fish somewhere
	// put up a timer
	// dare the player to get 20 fish in the amount of time

	// first do it without a time
	
	ship user;
	int fish_time;
	int fish_count;
	int enemy_segx;
	int enemy_segy;
	
	pickup fish[];

	WorldThing start_place; // fishing grounds
	
	IslandManager im;
	ShipManager sm;
	double timer;
	
	static final int MAX_FISH = 20;
	
	public void Init(MissionData mdin, UserInput uin, IslandManager immin[][], ShipManager smmin[][]) {
		super.Init(mdin,  uin, immin, smmin);
		state = ON_DECK;
		title = "GO FISHING";
		description = "CATCH TWENTY GOLDFISH BEFORE TIME RUNS OUT";
		bonus = 50; // if you get every fish in time
		fish_count = MAX_FISH;
		fish_time = 100; // every time the time goes dow 
		
		fish = new pickup[MAX_FISH];
	}
	
	public boolean CanStart() {
		user = md.FindShip(md.userid);	// in case it changees
		if (user==null)
			return false;
		return true;
	}
	
	public void Start() {
	//	ui.objective_status = UserInput.OBJECTIVE_ACTIVE;
		super.Start();
		fish_time -= 10;  // fish timer goes down every time
		enemy_segx = md.GetSegmentX(user.xpos);
		enemy_segy = md.GetSegmentY(user.ypos);

	}
	
	public int Update(double t) {
		im = imm[enemy_segx][enemy_segy];
		sm = smm[enemy_segx][enemy_segy];
		
		user = md.FindShip(md.userid);	// in case it changees
		if (user==null)
			return state;
		
		WorldThing fishpt = new WorldThing();
		
		switch (state) {
		case SETUP_STATE:
			ui.objective_status= UserInput.OBJECTIVE_INACTIVE;
			start_place = FindStart(im, md.win_width*md.win_height*2.0d, 6000000, user, 0.3d);
		//	start_place = FindStart(im, 10000, 200000, user);
			
			int i = 0;
			for (int school = 0; school < 4; school++) {
				// add a pickup fish
				start_place.CopyTo(fishpt);
				fishpt.xpos+= Math.random()*200d-100d;
				fishpt.ypos+= Math.random()*200d - 100d;
				for (int j=0; j< fish_count /4; j++) {
					fishpt.xpos+= Math.random() * 40d -20d;
					fishpt.ypos+= Math.random() *40d - 20d;		
					fish[i]= md.PKmat[enemy_segx][enemy_segy].AddPickup(fishpt, 4);
					i++;
				}


			}
			mission_message = true;
			ui.TurnOnTicker("XXX A SCHOOL OF FISH HAS BEEN SIGHTED XXX");
			ui.time = 100;
			ui.TurnOnTime();
			state = MESSAGE_STATE;
			break;
		case MESSAGE_STATE:
			ui.target_mode = ui.TARGET_OBJECTIVE;
			ui.arrow_string = "FISH";  // turn this off when close
			ui.dist_string = " ";  // better safe than sorry
			ui.SetTargetXY(start_place.xpos, start_place.ypos);
			if (start_place.DistanceSq(user) < md.win_width*md.win_height * 0.5d) {
				state= WAIT_FOR_PLAYER_STATE;
				ui.TurnOnTicker("XXX YOU ARE LOCATED AT THE FISHING GROUNDS XXX");
				ui.target_mode = ui.TARGET_NOTHING;
				ui.arrow_string = "FISH";
			}
			break;
		case WAIT_FOR_PLAYER_STATE:
			ui.target_mode = ui.TARGET_NOTHING;
			ui.arrow_string = " ";
			// check to see if fish[] are still alive,
			// respawning could be a problem
			boolean fish_alive = false;
			for (int count=0; count < fish_count; count++) {
				if (fish[count]!=null)
					if (fish[count].type==pickup.pktype.EMPTY) {
						fish[count]= null;  // so I don't get confused if it is reused
					} else
						fish_alive = true;
			}
			
			if (!fish_alive) {
				md.AddMoney(md.PLAYER_TEAM, bonus);
				bonus = 0; // you have done this 
				completed = true;
				state = LAST_MESSAGE_STATE;
				timer = 20000/md.time_factor;
				ui.FreezeTimer();
				ui.TurnOnTicker("XXX GOOD FISHING CAPTAIN XXX ALL SCHOOLS CAUGHT IN OUR NETS XXX");
			} else {
				// if time is running out, point the player where to go
				if (ui.time < 31) {
					for (int count = 0; count < fish_count; count++) 
						if (fish[count]!=null){
							ui.target_mode = ui.TARGET_OBJECTIVE;
							ui.arrow_string = "FISH";  // turn this off when close
							ui.SetTargetXY(fish[count].xpos, fish[count].ypos);						
						}
				}
			}
				
			break;
		case LAST_MESSAGE_STATE:
			timer=timer-t;
			if (timer < 0) {
				ui.objective_status= UserInput.OBJECTIVE_ACTIVE;
				mission_message = false;
				ui.TurnOffTicker();
				ui.TurnOffTime();
				state = FINISHED_STATE;			
			}
			break;
		case FINISHED_STATE:	
			break;
		}
		
		return 0;
	}
}
