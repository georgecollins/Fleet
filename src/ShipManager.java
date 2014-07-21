import org.lwjgl.opengl.GL11;



public class ShipManager extends ObjectManager {
	private static int MAX_SHIPS=200;
	ship slist[];
	ProjectileManager pm;
	PickupManager pk;
	
	double restock_timer;

	int scount;
	// replace with ship data?
	public int AddShip(WorldThing wt, String fname) {
		if (scount > 0 ) {
			for (int loop=0; loop < scount; loop++) {
				if (slist[loop].type==ship.stype.EMPTY)	{
					slist[loop].type= ship.stype.SHIP;
					slist[loop].xpos = wt.xpos +offsetx;
					slist[loop].ypos = wt.ypos +offsety;
					slist[loop].rotation = wt.rotation;
					slist[loop].hd.hull.load(fname);
					slist[loop].id = md.GetUniqieID(md.SHIP);

							
					return slist[loop].id;  // we added the ship
				}
			}
		}
		// no free ship so we create one.  
		if (scount < MAX_SHIPS-1) {

			slist[scount] = new ship( md.GetUniqieID(md.SHIP), fname);
			//slist[scount].type= ship.sType.SHIP;
			slist[scount].xpos = wt.xpos + offsetx;
			slist[scount].ypos = wt.ypos + offsety;
			slist[scount].rotation = wt.rotation;

			scount++;
			return slist[scount-1].id;
		}
		
		
		return 0;
	}
	
	public int AddShip(WorldThing wt) {
		if (scount > 0 ) {
			for (int loop=0; loop < scount; loop++) {
				if (slist[loop].type==ship.stype.EMPTY)	{
					slist[loop].type= ship.stype.SHIP;
					slist[loop].xpos = wt.xpos + offsetx;
					slist[loop].ypos = wt.ypos + offsety;
					slist[loop].rotation = wt.rotation;
					// slist[loop].hull.load(fname);
					slist[loop].id = md.GetUniqieID(md.SHIP);

							
					return slist[loop].id;  // we added the ship
				}
			}
		}
		// no free ship so we create one.  
		if (scount < MAX_SHIPS-1) {

			slist[scount] = new ship( md.GetUniqieID(md.SHIP));
			//slist[scount].type= ship.sType.SHIP;
			slist[scount].xpos = wt.xpos + offsetx;
			slist[scount].ypos = wt.ypos + offsety;
			slist[scount].rotation = wt.rotation;

			scount++;
			return slist[scount-1].id;
		}
		
		
		return 0;
	}
	public void DeleteShip(int id) {
		for (int count=0; count < scount; count++) {
			if (slist[count].id == id) { 
				slist[count]=new ship(md.GetUniqieID(md.SHIP));
				slist[count].type=ship.stype.EMPTY;
			}

		}
	}
	
	private void PasteShipInSlot(int slot, ship s) {
		// we have to really copy everything.. components, physics, etc.  
		
		slist[slot]=s;
	}
	
	public int AddShip(ship s) {
		// add the ship s, presumably from another ship manager
		if (scount > 0 ) {
			for (int loop=0; loop < scount; loop++) {
				if ((slist[loop].type==ship.stype.EMPTY) || (slist[loop].id==s.id))	{
					slist[loop] = s;
					//System.out.println("Inserted ship into slot" + loop);
					//System.out.println("ship id " + slist[loop].id);
					return slist[loop].id;
				}
			}
		}
				// no free ship so we create one.  
		if (scount < MAX_SHIPS-1) {

			slist[scount] = s;
			//System.out.println("Added ship at slot" + scount);
			//System.out.println("ship id " + slist[scount].id);
			scount++;
			return slist[scount-1].id;
		}
		return 0;
	
	}
	
	public void PlaceOnPath(ship s, Path p) {
		s.xpos=p.xmat[0];
		s.ypos=p.ymat[0];
		s.rotation=Math.atan2((s.xpos-p.xmat[1]), (s.ypos-p.ymat[1]))+Math.PI;
	}
	
	public boolean AnyShipAt(WorldThing wt) {
		for (int count=0; count < scount; count++) {
			if (slist[count].type==ship.stype.SHIP) {
				if (slist[count].hd.hull.InBounds((float) wt.xpos, (float) wt.ypos)) 
					return true;
			}
		}
		return false;
	}
	
	// check if it is null before you use it.  
	public ship GetShip(int shipid) {
		for (int count=0; count < scount; count++) {
			if (slist[count].id == shipid) return slist[count];
		}
		return null;
	}
	
	public int GetPlayerID() {
		for (int count=0; count < scount; count++) {
			if ((slist[count].team == 0) && (slist[count].type==ship.stype.SHIP)) return slist[count].id;
		}	
		return 0;
	}
	void NewUserShipNotify(int userid) {
		// Do I need to do this?  

	}
	public void Draw(WorldThing min, WorldThing max) {
		for (int loop=0; loop < scount; loop++) {
			if ((slist[loop].type == ship.stype.SHIP) || (slist[loop].type == ship.stype.WRECK)) {
				if (((slist[loop].xpos > min.xpos) && (slist[loop].xpos < max.xpos) && (slist[loop].ypos > min.ypos) && (slist[loop].ypos < max.ypos)) || 
						(slist[loop].team==md.PLAYER_TEAM)) {
				// check range 
					slist[loop].DrawIt();
				} else
					slist[loop].SetWorldPoints();
		
			}
		}
		
	}
	public int TestCollision(sketch skt) {
		for (int loop=0; loop< scount; loop++) 
			if (slist[loop].type==ship.stype.SHIP) {
				if (slist[loop].Collide(skt))
					return slist[loop].id;
			}
		return 0;
	}
	public int HandleIslandCollision(island isl, int userid) {
		int port_of_call=0;
		for (int loop=0; loop< scount; loop++) 
			if (slist[loop].type==ship.stype.SHIP) {
				if (slist[loop].Collide(isl.isketch))
					slist[loop].HandleCollision(isl.id);
				
				
				// I need to handle this better, so I know what to do if other ships enter
				if ((slist[loop].id==userid) && (isl.has_port==1)) {   // 0 = user team
					double dist=((slist[loop].xpos-isl.world_portx)* 
							(slist[loop].xpos-isl.world_portx)+
							(slist[loop].ypos-isl.world_porty) *(slist[loop].ypos-isl.world_porty));
					//System.out.println(isl.name+" "+dist + " "+isl.id);
					if (dist< 2000d){
						port_of_call=isl.id;
					}
				}
			}	
		return port_of_call;
	}
	
	public boolean CheckFriendlyFire(WorldThing fp1, WorldThing fp2, int fireid, int team) {
		// is there something between the line wp1 to wp2 that is on team but not fire id;
		// InCollisionBox(maxx, maxy, minx, miny
		
		for (int loop=0; loop < scount; loop++)
			if ((slist[loop].type == ship.stype.SHIP) && (slist[loop].team== team) && (slist[loop].id!=fireid)) {
	

			
				if (slist[loop].dhull.InCollisionBox(fp1.xpos, fp1.ypos, fp2.xpos, fp2.ypos))
					return true;
				
				/*
				if (slist[loop].dhull.LineCollide(fp1.xpos, fp1.ypos, fp2.xpos, fp2.ypos))
					return true;
					*/
			}
		return false;
	}
	public void Simulate(double t, IslandManager im, int segx, int segy) {
		for (int loop=0; loop < scount; loop++) {
			if ((slist[loop].type == ship.stype.SHIP) || (slist[loop].type == ship.stype.WRECK)) {
				ship cship=GetShip(slist[loop].collided);  // get a pointer to the ship I collided with
				slist[loop].Simulate((float) t, im, cship);
				slist[loop].ClearCollision();  // we test for collison below.  

				md.MoveShip(slist[loop], segx, segy);
				slist[loop].CheckPM(pm);  // just to test
				
				// return the projectile
				projectile phit = pm.Collide(slist[loop].hd.hull, slist[loop].id, slist[loop].team);
				
				if (phit != null)  {
					// notify the ai
					slist[loop].ai.NotifyDamage(phit);
					int tons=slist[loop].GetTotalWeight();  // i get rewarded for the weight at sinking??
					int hteam=slist[loop].team;
					//take the projectile's damage
					if (slist[loop].TakeDamage(phit.pd.damage,pk) < 1) {  // or it could be the damage from phit
					// this should be via the message system
					
						// send the message that someone has died
						im.md.SendSunkMessage(slist[loop],phit.pd.owner, phit.pd.team);
						
					}
				}
				
				// now check to see if I collide with any other ship.  				
				for (int tcount=0; tcount<scount; tcount++) {
					if ((tcount!=loop) && (slist[tcount].type==ship.stype.SHIP)) {
						// later I may want to collide with islands and wwrecks
						if (slist[loop].Collide(slist[tcount]))  // pass the ship ID so you don't collide with your owner
						{
							slist[loop].HandleCollision(slist[tcount].id, slist[tcount]);
							// fleets[loop].TakeDamage(1);  // this kills quickly
							// System.out.println("Collision" + tcount + " -> " + loop);
						}
					}
				}

				
				// test collide with pickups
				pk.ShipCollide(slist[loop]);
				
				if ((slist[loop].xpos > offsetx+md.world_width/2) || (slist[loop].ypos > offsety+md.world_height) 
						|| (slist[loop].xpos < offsetx-md.world_width/2) || (slist[loop].ypos < offsety-md.world_height)) {
					int xx=0;
					xx++; // = 1/xx;
				}
				
				
			}		
			
		}
	
		
		
		restock_timer+=t;
		WorldThing wt = new WorldThing();
		if (restock_timer> 10000/md.time_factor) {
			if (pk.FishCount() < 10) {
				int fish = 10- pk.FishCount();
				
				boolean placed = false;
				while (placed== false) {
					double xrange=md.world_width*0.6d;
					double yrange=md.world_height*0.6d;
					wt.xpos=Math.random()*xrange-xrange/2 + pk.offsetx;
					wt.ypos=Math.random()*yrange-yrange/2 + pk.offsety;
					wt.rotation=Math.random()*Math.PI*2;
					if (im.MapValue(wt.xpos, wt.ypos)==0)
						placed = true;
				}
				pk.AddPickup(wt,4);  // 4 = fish
					
			}
				
		}
		
	}

	public void HandleUI(UserInput ui, int userid, double t) {
		for (int loop=0; loop < scount; loop++) {
			if (slist[loop].type == ship.stype.SHIP) {
				if (slist[loop].id==userid) {
					slist[loop].Input(ui, t);
				}
				else {
					// do the attached ai
					slist[loop].ai.Update(t);
					if (slist[loop].trigger==1) {
						for (int i=0; i < slist[loop].component_count; i++) {
							if (slist[loop].scomponent[i].IsUsed()!=0) {
					
								slist[loop].scomponent[i].FireButtonDown(slist[loop].xvel, slist[loop].yvel, Math.cos(slist[loop].gunaim1), Math.sin(slist[loop].gunaim1), 0);
							}
						
						}
					}
				}
			}
		}
	}
	
	public void SetPM(ProjectileManager pmin) {
		pm=pmin;
	}
	public void SetPK(PickupManager pkin) {
		pk=pkin;
	}
	
	// find the closest ship except for one, so that I don't have to return myself (the exception could be zero)
	public int FindClosestShip(WorldThing wt, int exceptid) {
		int bestid = 0;
		double bestdist=10000.0f*10000.0f;
		for (int loop = 0; loop < scount; loop++) {
			if ((slist[loop].type == ship.stype.SHIP) && (slist[loop].id != exceptid)) {
				double newdist=slist[loop].DistanceSq(wt);
				if (newdist< bestdist) {
					bestid=slist[loop].id;
					bestdist=newdist;
				}
			}	
		}
		return bestid;
		
	}
	// find the closest ship except for one, so that I don't have to return myself (the exception could be zero)
	public int FindClosestEnemyShip(WorldThing wt, int exceptid, int myteam) {
		int bestid = 0;
		double bestdist=10000.0f*10000.0f;
		for (int loop = 0; loop < scount; loop++) {
			if ((slist[loop]!=null) && (slist[loop].type == ship.stype.SHIP) && (slist[loop].id != exceptid) && 
					(md.relations[slist[loop].team][myteam]==md.ENEMIES)) {
				double newdist=slist[loop].DistanceSq(wt);
				if (newdist< bestdist) {
					bestid=slist[loop].id;
					bestdist=newdist;
				}
			}	
		}
		return bestid;
		
	}
		
	ShipManager(MissionData mdin) {
		scount=0;
		slist = new ship[MAX_SHIPS];

		restock_timer = 0d;
		md = mdin;
		
	}
	
}
