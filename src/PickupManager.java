
public class PickupManager extends ObjectManager {
	pickup pickups[];
	int pkcount;
	int collision_test_count; // we don't need to do this every frame, so keep track
	IslandManager im;
	
	
	static public int MAX_PICKUPS = 200;  // per segment
	
	pickup AddPickup(WorldThing wt, int pkin) {
		
		
		pickup.pktype pkt=pickup.pktype.REPAIR;

		switch (pkin) {
		case 0: 
			pkt=pickup.pktype.REPAIR;
			break;
		case 1:
			pkt=pickup.pktype.BANNAS;
	//		temp=pkin.crate[0];
			break;
		case 2:
			pkt=pickup.pktype.ORE;
	//		temp=pkin.crate[1];
			break;
		case 3: 
			pkt=pickup.pktype.TECH;		
	//		temp=pkin.crate[2];
			break;
			
		case 4:
			pkt=pickup.pktype.FISH;
			break;
		}
		
		
		if (pkcount > 0)
			for (int loop=0; loop< pkcount; loop++) {
			  if ((pickups[loop]==null)  || (pickups[loop].type==pickup.pktype.EMPTY)) {
					pickups[loop]= new pickup();
					pickups[loop].SetID(md.GetUniqieID(md.PICKUP));
					pickups[loop].type=pkt;
					pickups[loop].xpos=wt.xpos;
					pickups[loop].ypos=wt.ypos;
					pickups[loop].rotation=wt.rotation;
					/*
					if (pkin>0) // change the crate sketch
						pickups[loop].psketch=pickups[loop].crate[pkin-1];
						*/
					pickups[loop].SetPickupType(pkin);
					return pickups[loop];  // i created the pickup
			  }
			}
		
		if (pkcount < MAX_PICKUPS - 1) {
				
			pickups[pkcount]= new pickup();
			pickups[pkcount].SetID(md.GetUniqieID(md.PICKUP));			
			pickups[pkcount].type=pkt;
			pickups[pkcount].xpos=wt.xpos;
			pickups[pkcount].ypos=wt.ypos;
			pickups[pkcount].rotation=wt.rotation;
			/*
			if (pkin>0) // change the crate sketch
				pickups[pkcount].psketch=pickups[pkcount].crate[pkin-1];
				*/
			pickups[pkcount].SetPickupType(pkin);
			pkcount++;
			return pickups[pkcount-1];
		}
		else return null;
		
	}
	void ShipCollide(ship s) {
		
		for (int loop=0; loop < pkcount; loop++) 
		  if ((pickups[loop].type!=pickup.pktype.EMPTY) && (s.type==ship.stype.SHIP))
				// see if I collide with any pickup
		  	if (pickups[loop].Collide(s.hd.hull)) {
		  		if (pickups[loop].type==pickup.pktype.REPAIR) {	
			  	// if so 
				// remove me
			  		pickups[loop].type=pickup.pktype.EMPTY;
				// set ship health to max health
			  		s.SetHealth(s.GetMaxHealth());
				// repair hull
			  		s.RepairHull();
		  		}
		  		if (pickups[loop].type == pickup.pktype.FISH) {
					// remove me
			  		pickups[loop].type=pickup.pktype.EMPTY;			
			  		md.AddMoney(s.team, 5);
		  		}
		 	
		  		if ((pickups[loop].type==pickup.pktype.BANNAS) || 
		  			(pickups[loop].type==pickup.pktype.ORE) ||
		  			(pickups[loop].type==pickup.pktype.TECH)) 
		  			for (int i=0; i < s.component_count; i++) {
		  				if ((s.scomponent[i].cd.type==ComponentData.ctype.CARGO) &&
		  					(s.scomponent[i].cd.holding==0)) {
		  					if (pickups[loop].type==pickup.pktype.BANNAS) {
		  						s.scomponent[i].cd.holding=1;
		  						s.scomponent[i].BuildCrate(1);
		  					}
		  					if (pickups[loop].type==pickup.pktype.ORE) {
		  						s.scomponent[i].cd.holding=2;
		  						s.scomponent[i].BuildCrate(2);
		  					}
		  					if (pickups[loop].type==pickup.pktype.TECH) {
		  						s.scomponent[i].cd.holding=3;
		  						s.scomponent[i].BuildCrate(3);
		  					}
	  						
		  				
		  			  		pickups[loop].type=pickup.pktype.EMPTY;
		  					
		  					
		  					
		  				}
		  			
		  			
		  		}
		  		
		}
	}
	void Draw() {
		for (int count=0; count < pkcount; count++)
			if ((pickups[count]!=null) && (pickups[count].type!=pickup.pktype.EMPTY)) {
				pickups[count].Draw();
			}
		
	}
	
	public boolean InBounds(double x, double y) {
		if (((x-im.offsetx) >  md.world_width/2) || 
			 ((x-im.offsetx) < -md.world_width/2) ||
			 ((y-im.offsety) > md.world_height/2) ||
			 ((y-im.offsety) < -md.world_height/2))	{
			return false;
		} 
		return true;
	}
	
	void Update(double t) {
		for (int count=0; count < pkcount; count++)
			if (pickups[count].type!=pickup.pktype.EMPTY) {
				if (im.TestCollision(pickups[count].psketch)!=0) {
					pickups[count].xvel=-pickups[count].xvel;
					pickups[count].yvel=-pickups[count].yvel;
					pickups[count].xacc=0d;
					pickups[count].yacc=0d;
				}
				pickups[count].Update(im, t);
				
				// repel from other boxes
				if (pickups[collision_test_count].type!=pickup.pktype.EMPTY) {
					double dist=pickups[count].xpos*pickups[collision_test_count].xpos+pickups[count].ypos*pickups[collision_test_count].ypos;
					if (dist > 0.1d) {   // to prevent divide by zero, near divide by zero
						dist=dist/2d;  // makes the force stronger
						pickups[count].xacc+= (pickups[count].xpos-pickups[collision_test_count].xpos)/dist;
						pickups[count].yacc+= (pickups[count].ypos-pickups[collision_test_count].ypos)/dist;					
					}
				}
				if (!InBounds(pickups[count].xpos, pickups[count].ypos)) {
					pickups[count].type=pickup.pktype.EMPTY;
				}
			}	
		
		collision_test_count++;
		if ((collision_test_count == pkcount) || (collision_test_count > pkcount)) 
			collision_test_count=0;
		
		

		

	}
	
	int FishCount() {
		int fc = 0;
		for (int count=0; count < pkcount; count++)
			if (pickups[count].type!=pickup.pktype.EMPTY) 
				if (pickups[count].type== pickup.pktype.FISH)
					fc++;
		return fc;
	}
	
	void SetIslandManager(IslandManager imin) {
		im=imin;
		
	}
	
	PickupManager(MissionData mdin) {
		pickups = new pickup[MAX_PICKUPS];
		pkcount=0;

		collision_test_count=0;
		md=mdin;

	}
}
