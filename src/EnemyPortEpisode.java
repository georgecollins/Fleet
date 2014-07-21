
public class EnemyPortEpisode extends Episode {
	
	// this episode should be going as long as an enemy port exists
	int islid;
	island enemyisl;
	String islname;
	ship mine_layer;
	ship barge1, barge2, chase1, chase2;  // find them and replace them
	ship user;
	
	ship warship[];
	
	int enemy_segx, enemy_segy;
	
	double time_out;
	IslandManager im;
	ShipManager sm;	
	
	WorldThing anchor1, anchor2;  // anchor points for the mission
	WorldThing spawnpt;
	
	int MAX_WARSHIPS = 10;
	
	public void Init(MissionData mdin, UserInput uin, IslandManager immin[][], ShipManager smmin[][]) {
		super.Init(mdin, uin, immin, smmin);
		title = "ENEMY PORT ATTACK";
		
		for (int xseg = 0; xseg < md.segment_width; xseg++)
			for (int yseg = 0; yseg < md.segment_height; yseg++)
				for (int i=0; i < imm[xseg][yseg].island_count; i++)
					if ((imm[xseg][yseg].islands[i].has_port > 0) && (imm[xseg][yseg].islands[i].pd.team == md.RED_TEAM)) {
						enemyisl = imm[xseg][yseg].islands[i];
						islname= imm[xseg][yseg].islands[i].name;
						islid = imm[xseg][yseg].islands[i].id;
						enemy_segx= xseg;
						enemy_segy= yseg;
						anchor1.xpos = enemyisl.isketch.worldx[36];
						anchor1.ypos = enemyisl.isketch.worldy[36];
						anchor1.rotation = enemyisl.rotation - Math.PI/2;
						
						anchor2.xpos = enemyisl.isketch.worldx[0];
						anchor2.ypos = enemyisl.isketch.worldy[0];	
						anchor2.rotation = enemyisl.rotation - Math.PI/2;
						
						spawnpt.midpoint(anchor1, anchor2);
						break;
					}
						

		description = "ATTACK THE ENEMY PORT AT " + islname;

		// this episode starts in setup-- it is always set up
		state = SETUP_STATE;
	}
	
	public boolean CanStart() {
		if (enemyisl==null)
			return false;
		if (enemyisl.pd.team!= md.RED_TEAM)
			return false;
		
		return true;
	}
	
	public void PutOnDeck() {
		// tee up the mission to start in the episode list
		ui.objective_status=UserInput.OBJECTIVE_ACTIVE; 
		md.episode_list.add(this);
	
	}
	
	public void Start() {
		ui.objective_status = UserInput.OBJECTIVE_ACTIVE;
		ui.target_mode = ui.TARGET_OBJECTIVE;
		ui.SetTargetXY(enemyisl.world_portx, enemyisl.world_porty);
		ui.arrow_string = "CAPTURE";
		ui.dist_string = " ";
		mission_message = true;
		ui.TurnOnTicker("XXX ENTER ENEMY PORT TO CAPTURE " + islname + " FROM THE RED FLEET XXX");
		time_out = 160000d/md.time_factor;

		md.can_feud = 1;
		state= MESSAGE_STATE;  // for this mission it really starts with setyp

	}
	
	public void MaintainDefense() {

		double dist = user.DistanceSq(enemyisl);
		if (dist < md.win_width*md.win_height*4d) {
			// if the player is close spawn some ships to attack
			if ((mine_layer!=null) && (mine_layer.type== ship.stype.SHIP)) {
				if (mine_layer.ai.GetCurrentTask()!= AIControl.TASK_MINE_LAY)
					mine_layer.ai.StartMinelaying(10, 10000, mine_layer);
			}
			
			if ((chase1== null) || (chase1.type != ship.stype.SHIP)) {
				int chase1id = sm.AddShip(spawnpt);
				chase1= sm.GetShip(chase1id);
				HullData shd = new HullData();
				shd.BuildSmallHull(shd);
				chase1.BuildAttackShip(shd);
				chase1.AttachAI(sm, im, md);
				chase1.SetTeam(md.RED_TEAM, md);
				chase1.ai.StartAttackTask(chase1, user.id);

				for (int i = 0; i < MAX_WARSHIPS; i++) 
					if ((warship[i]==null) || (warship[i].type != ship.stype.SHIP)) 
				{
					WorldThing place = new WorldThing();
					place.xpos = enemyisl.OrbitPath.xmat[i*2]; 
					place.ypos= enemyisl.OrbitPath.ymat[i*2];
					int id = sm.AddShip(place);
					warship[i] = sm.GetShip(id);
					warship[i].BuildShip();
					warship[i].SetTeam(md.RED_TEAM, md);
					warship[i].AttachAI(sm, im, md);
					warship[i].ai.StartWait(50, 60d);
				}

			}

			// have the mine layer lay mines
		} else
		{

			// if the player isn't close, respawn the two barges if they aren;'t there. 
			if ((mine_layer==null) || (mine_layer.type != ship.stype.SHIP)) {
				int mineid = sm.AddShip(spawnpt);
				mine_layer = sm.GetShip(mineid);
				HullData shd = new HullData();
				shd.BuildSmallHull(shd);
				mine_layer.BuildShip(shd);
				for (int i = 0; i < mine_layer.hd.numhardpoints; i++) {
					ComponentData cd = new ComponentData();
					cd.BuildMineComponent();
					component comp = new component();
					comp.cd= cd;
					mine_layer.AttachComponent(comp, i);
				}
				mine_layer.AttachAI(sm, im, md);
				mine_layer.SetTeam(md.RED_TEAM, md);
				mine_layer.ai.StartWait(50, 50000d);			
			}
		}		
	}
	
	
	public int Update(double t) {
		im = imm[enemy_segx][enemy_segy];
		sm = smm[enemy_segx][enemy_segy];
		
		user = md.FindShip(md.userid);	// in case it changes
		if (user==null)
			return state;
				
		double dist;
		switch (state) {
		case SETUP_STATE:
			MaintainDefense();		
			break;
		case MESSAGE_STATE:
			// this is where the mission starts
			md.can_feud = 1;
			MaintainDefense();	
			// but also highlight the objective
			
			// if the port goes away -- do victory
			if (enemyisl.pd.team!= md.RED_TEAM) {
				state = WAIT_FOR_PLAYER_STATE;
			}
			break;
		case WAIT_FOR_PLAYER_STATE:

			completed = true;
			md.can_feud = 0;
			ui.target_mode = ui.TARGET_NOTHING;
			ui.arrow_string = " ";
			state = LAST_WAIT_STATE;
			ui.TurnOffTicker();
			mission_message = false;
			ui.objective_status= UserInput.OBJECTIVE_ACTIVE;
			state = FINISHED_STATE;
			break;
		}
		return 0;
	}
	
	EnemyPortEpisode() {
		super();
		mine_layer=null;
		barge1 = null;
		barge2 = null;
		chase1 = null;
		chase2 = null;
		anchor1 = new WorldThing();
		anchor2 = new WorldThing();
		spawnpt = new WorldThing();
		
		warship = new ship[10];
				
	}

}
