import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Scanner;

import org.lwjgl.opengl.GL11;



public class ship extends WorldThing {

	static int MAX_DAMAGE_STATES=5;
	
	String hullfile;
	HullData hd;
//	sketch	hull;
	DamageSketch dhull;
	sketch prop1, prop2, prop3;
	component[] scomponent;
	AIControl ai;

	int aim_mode;  // 1 = button
	int team;  // team 0 = player
	int trigger;

	public int collided;
	
	
	enum stype { EMPTY, SHIP, WRECK }  // later MONSTER!!
	stype type;
	
//	boolean alive;  // could be ship oor wreck

	double steer;	// + left or - right
	double throttle;  // + or -
	double accel;  // acceleration
	double speed;
	
	double xvel, yvel;
	
	WorldThing accelv;  // do I even need this
	
	WorldThing velv;
	double angm;
	
	// double max_speed;  // max_speed is now calculated
	double gunaim1;

	int prop_state[];  // 0 to 4	
	double prop_timer[];
	double max_prop_timer;

	float flagR, flagG, flagB;
	

	
	int health;
	//int max_health;
	int damage_state_amt[];
//	int control;  // 1= forward 2 = backward
	double mountx[];
	double mounty[];
	
	double wreck_timer;
	double max_wreck_timer;
	
	double repair_timer;
	
	double show_damage_timer;
	

	int component_count;
	
	boolean magic_move;
	WorldThing mmove;  // when the AI magically moves me
	
	public void SetAccel(double a) {
		accel = a;
	}
	
	public void SetSpeed(double s) {
		speed=s;
	}


	public void SetTeam(int t, MissionData md) {
		team=t;
		for (int count=0; count < component_count; count++) {
			scomponent[count].team=t;
		}
		flagR = md.teamR(t);
		flagG = md.teamG(t);
		flagB = md.teamB(t);
		
		// try changing the colors of the bridge
		
		switch (hd.hull_type) {
		case HullData.THIN_HULL: 
		case HullData.WIDE_HULL:	
			dhull.rcolor[8] = flagR;
			dhull.gcolor[8] = flagG;
			dhull.bcolor[8] = flagB;
			dhull.rcolor[9] = flagR;
			dhull.gcolor[9] = flagG;
			dhull.bcolor[9] = flagB;
			dhull.rcolor[10] = flagR;
			dhull.gcolor[10] = flagG;
			dhull.bcolor[10] = flagB;
			dhull.rcolor[11] = flagR;
			dhull.gcolor[11] = flagG;
			dhull.bcolor[11] = flagB;

			hd.hull.rcolor[8] = flagR;
			hd.hull.gcolor[8] = flagG;
			hd.hull.bcolor[8] = flagB;
			hd.hull.rcolor[9] = flagR;
			hd.hull.gcolor[9] = flagG;
			hd.hull.bcolor[9] = flagB;
			hd.hull.rcolor[10] = flagR;
			hd.hull.gcolor[10] = flagG;
			hd.hull.bcolor[10] = flagB;
			hd.hull.rcolor[11] = flagR;
			hd.hull.gcolor[11] = flagG;
			hd.hull.bcolor[11] = flagB;
			//8, 9 , 10 ,11
			break;

		case HullData.DIAMOND_HULL:
			dhull.rcolor[14] = flagR;
			dhull.gcolor[14] = flagG;
			dhull.bcolor[14] = flagB;
			dhull.rcolor[15] = flagR;
			dhull.gcolor[15] = flagG;
			dhull.bcolor[15] = flagB;
			dhull.rcolor[16] = flagR;
			dhull.gcolor[16] = flagG;
			dhull.bcolor[16] = flagB;
			dhull.rcolor[17] = flagR;
			dhull.gcolor[17] = flagG;
			dhull.bcolor[17] = flagB;

			hd.hull.rcolor[14] = flagR;
			hd.hull.gcolor[14] = flagG;
			hd.hull.bcolor[14] = flagB;
			hd.hull.rcolor[15] = flagR;
			hd.hull.gcolor[15] = flagG;
			hd.hull.bcolor[15] = flagB;
			hd.hull.rcolor[16] = flagR;
			hd.hull.gcolor[16] = flagG;
			hd.hull.bcolor[16] = flagB;
			hd.hull.rcolor[17] = flagR;
			hd.hull.gcolor[17] = flagG;
			hd.hull.bcolor[17] = flagB;			
			break;
		}
	}
	public int GetTeam()  {
		return team;
	}
	
	public int GetCargoCount() {
		int count =0;
		for (int i=0; i < component_count; i++) {
			if ((scomponent[i]!=null) && (scomponent[i].IsUsed()!=0) && (scomponent[i].cd.type== ComponentData.ctype.CARGO) )
				count++;
		}
		return count;
	}
	public boolean HasCargoLoaded() {
		for (int i=0; i < component_count; i++) {

			if ((scomponent[i].IsUsed()!=0) && (scomponent[i].cd.type==ComponentData.ctype.CARGO)
				&& (scomponent[i].cd.holding!=0)) 
					return true;
		}
		return false;
	}
	public int GetTotalWeight() {
		int wgt=hd.weight;
		for (int i=0; i < component_count; i++) {
			if ((scomponent[i]!=null) && (scomponent[i].IsUsed()!=0))
				wgt+=scomponent[i].cd.GetWeight();
		}
		return wgt;
	}
	
	public int MaxWeight() {
		return (int) (hd.max_weight+EngineCount()*200);
	}
	public int GetScrapValue() {
		return GetTotalWeight()/2;
	}
	
	public double GetMaxSpeed() {
		double spd;
		double max_speed=hd.max_speed+hd.max_speed*0.2d*EngineCount();
		// speed is hull speed * max(1, (max_weight-weight)/max_weight)
		if (hd.max_weight==0) {
			spd=hd.max_speed;
		}  else
			spd=max_speed+ max_speed*Math.min(0d, ((double) (MaxWeight()-GetTotalWeight()))/((double)MaxWeight()));
		return spd;
	}
	public double GetCurrentTurnRate(double speed) {
		if (speed>0) {
			return hd.GetTurnRate()*(2*speed/GetMaxSpeed() +1d)/3d;  // 1/3-> to full turn, depending on speed/ max_speed
		} else
			return hd.GetTurnRate()*0.25d;
	}
	public void SetStartHealth(int h) {
		//max_health=h;
		health=h;
	}
	
	public void SetHealth(int h) {
		health=h;
	}
	
	
	public int GetMaxHealth() {
		
		return hd.max_health + RepairCount()*10; // 10 health
	}
	
	public int EngineCount() {
		int c=0;
		for (int i=0; i < hd.numhardpoints; i++) 
			if ((scomponent[i]!=null) && (scomponent[i].cd.type==ComponentData.ctype.ENGINE))
				c++;
		return c;
	}
	
	public int RepairCount() {
		// how many repair improve health
		int r = 0;
		for (int i=0; i < hd.numhardpoints; i++) 
			if ((scomponent[i]!=null) && (scomponent[i].cd.type==ComponentData.ctype.REPAIR))
				r+= scomponent[i].cd.guncount;
		return r;
	}
	
	public int GetRepairRate() {
		int r = 0;
		for (int i=0; i < hd.numhardpoints; i++) 
			if ((scomponent[i]!=null) && (scomponent[i].cd.type==ComponentData.ctype.REPAIR))
				r+= scomponent[i].cd.fire_rate;
		return r;
	}

	
	public int TakeDamage(int d,PickupManager pk) {  //pass null as the pickup manager if you don't want pickups
		health=health-d;
		
		show_damage_timer=8.0d;

		for (int dcount=0; dcount<MAX_DAMAGE_STATES; dcount++) {
			if (health==damage_state_amt[dcount]) {
					dhull.Damage();
					if (dcount < 3) {  /// if down to 60% I may lose components
						for (int count=0; count < component_count; count++) {
							if ((scomponent[count].IsUsed()>0)  && (Math.random()<0.15d)) {
								//scomponent[count].used=0;
								scomponent[count].damaged=1;
								
								/// if the damaged component is cargo, spit out a pickup
								if ((pk!=null) && (scomponent[count].cd.type== ComponentData.ctype.CARGO)) {
									WorldThing wt=new WorldThing();
									wt.xpos=scomponent[count].xpos;
									wt.ypos=scomponent[count].ypos;
									wt.rotation=rotation;
									if (scomponent[count].cd.holding!=0)   // don't add health for every empty compartment
										pk.AddPickup(wt, scomponent[count].cd.holding);
								}
							}					
						}
					}
			}
		}
		

		
		if ((health< 1) && (type==stype.SHIP)) {
			
			type = stype.WRECK;
			wreck_timer=max_wreck_timer;
			
			// cut loose all remaining cargo
			int any_cargo=0;
			
			for (int count=0; count < component_count; count++) {

					
					/// if the damaged component is cargo, spit out a pickup
					if ((pk!=null) && (scomponent[count].damaged==0) && (scomponent[count].cd.type== ComponentData.ctype.CARGO)) {
						WorldThing wt=new WorldThing();
						wt.xpos=scomponent[count].xpos;
						wt.ypos=scomponent[count].ypos;
						wt.rotation=rotation;
						if (scomponent[count].cd.holding!=0)   // don't add health for every empty compartment
							pk.AddPickup(wt, scomponent[count].cd.holding);
						any_cargo=1;
					}
					scomponent[count]= null;  // don't leave the component hanging around
							
			}
			if (any_cargo==0) {
				if (Math.random() <0.6d) {
					WorldThing wt=new WorldThing();
					wt.xpos=xpos;
					wt.ypos=ypos;
					wt.rotation=rotation;
					
					if (pk!=null) {
						pk.AddPickup(wt, 0);  // repair
					} else
						System.out.println("No pickup manager attacher to ship");
				}
			}
		}
		//System.out.println("Health " + health);
		return health;
	}
	
	public void ShowDamageBar() {
		// eventually this should"
		// fade in quickly,
		// show the damage changing (from old damage to current)
		// fade out
		
		// for now we will just show the damage.  
		
		
		float bracket_width = (float)-hd.width*1.2f;
		if (hd.hull_type== hd.WIDE_HULL)
			bracket_width=bracket_width*1.7f;
		
		sketch bracket= new sketch();
		
		bracket.xmat[0]=bracket_width;
		bracket.ymat[0]=-25;
		bracket.xmat[1]=bracket_width;
		bracket.ymat[1]=25;
		
		bracket.xmat[2]=bracket_width;
		bracket.ymat[2]=-26;		
		
		bracket.xmat[3]=bracket_width-4;
		bracket.ymat[3]=-26;	
		
		bracket.xmat[4]=bracket_width;
		bracket.ymat[4]=26;		
		bracket.xmat[5]=bracket_width-4;
		bracket.ymat[5]=26;		
	
		for (int i = 0; i < 3; i++) {
			bracket.bcolor[i]=0.75f;
			bracket.rcolor[i]=0.75f;
			bracket.gcolor[i]=0.75f;
		}
		bracket.numpoints=6;
		
		
		bracket.drawrot(xpos,ypos, rotation);
		// the tow points of the health bar
		WorldThing base_point= new WorldThing();
		WorldThing health_tip = new WorldThing();
		
		base_point.xpos=bracket_width-3f;
		base_point.ypos=-25;
		
		health_tip.xpos=bracket_width-3f;
		health_tip.ypos=-25f + ((float)health)/((float)GetMaxHealth())*50f;
		
		
		GL11.glLineWidth(2.0f);
		GL11.glBegin(GL11.GL_LINES);
		
		if (((float)health)/((float)GetMaxHealth())> 0.5f) {
			GL11.glColor3f(0.0f, 0.75f, 0.0f);
		} else
			GL11.glColor3f(0.75f, 0.0f, 0.0f);
		// bracket
		GL11.glVertex3f((float)(base_point.rotX(rotation)+xpos), (float) (base_point.rotY(rotation)+ypos), 0);
		GL11.glVertex3f((float)(health_tip.rotX(rotation)+xpos), (float) (health_tip.rotY(rotation)+ypos), 0);
		
//			GL11.glVertex3d(slist[loop].ai.mypath.xmat[i], slist[loop].ai.mypath.ymat[i], 0.0d);
		
		GL11.glEnd();
		

	}
	
	public void RepairHull() { 
		// sometimes there is no hullfile
		show_damage_timer=8d;
		dhull= new DamageSketch(hd.hull);// new DamageSketch(hullfile);
	}

	
	public void AttachAI(ShipManager sm, IslandManager im,  MissionData md) {

		ai=new AIControl(AIControl.aiType.SHIPAI, sm, im, md, id);

		for (int i=0; i < component_count; i++) 
			if (scomponent[i]!=null) {
				scomponent[i].SetPM(sm.pm);
			}
		ai.GetStrategy(this);
		ai.Setup(sm, im);  // cuz it resets the hull
	}
	
	public boolean CheckPM(ProjectileManager pm) {
		for (int i=0; i < component_count; i++) 
			if ((scomponent[i]!=null)  && 
				(scomponent[i].PM!=pm)) {
				// hack that I am doing to see if it fixes bugs
				scomponent[i].PM = pm;
			}

			
		return true;
	}
	
	public void SetPM(ProjectileManager pm) {
		
		// this is wrong because pm should not be stored in components
		for (int i=0; i < component_count; i++) 
			if (scomponent[i]!=null) 
				scomponent[i].PM=pm;
				
	}
	
	public void Simulate(double t, IslandManager im, ship cship) {

		
		show_damage_timer-=t;
		

		if (type==stype.SHIP) 
		{
			rotation=rotation+steer*t*GetCurrentTurnRate(speed);
			speed=speed+throttle*accel*t;
			/*
			if (speed>0)
			speed=speed-speed/(max_speed*max_speed*t);
			 */
			if (speed> GetMaxSpeed()) speed=GetMaxSpeed();
			if (speed< GetMaxSpeed()*(-0.5d)) speed=GetMaxSpeed()*(-0.5d);

		
			xvel=Math.cos((rotation+Math.PI/2))*speed;
			yvel=Math.sin((rotation+Math.PI/2))*speed;

			xpos=xpos+xvel*t+im.GetCurrentX(xpos, ypos)*t;
			ypos=ypos+yvel*t+im.GetCurrentY(xpos, ypos)*t;


				if ((cship!=null) && (cship.type!=ship.stype.EMPTY)) {
					double u=xpos-cship.xpos;
					double v=ypos-cship.ypos;
					double mag=Math.sqrt(u*u + v*v);
					if (mag<0.1d) mag=0.1d;
					if (mag > 0.1) {
						xpos=xpos+u/mag*10d*t;
						ypos=ypos+v/mag*10d*t;
					}
					
				}


			
			if (gunaim1>Math.PI*2) {
				gunaim1=0.0f;
			}
		

			for (int i=0; i < component_count; i++) 
				if (scomponent[i].IsUsed()!=0) {
					scomponent[i].Simulate(t);
				}
			
			// proptimer & state
			
			for (int count=0; count<hd.prop_count; count++) {
				prop_timer[count]=prop_timer[count]+t*Math.abs(speed/GetMaxSpeed());  // could be more turn specific
				if (prop_timer[count] > max_prop_timer) {
					prop_timer[count]=0;
					prop_state[count]++;
					if (prop_state[count]>3) {
						prop_state[count]=0;
					}
				}
			}
			
			
			// damage control
			if (health < GetMaxHealth()*0.8d) {
				repair_timer+=t;
				if (repair_timer > 20d) {
					int fix = GetRepairRate();
					health+=fix;
					repair_timer=0d;
					show_damage_timer=4.0d;
				
				}
			} else
				repair_timer=0.0d;
			
			
		}
		else
		{
		
			if (type==stype.WRECK) {
				wreck_timer=wreck_timer-t;
				if (wreck_timer<0.1d) {
					type=stype.EMPTY;
					id=0; // don't let AI be confused
				}
			}
		}
		
		if (magic_move) {
			mmove.CopyTo(this);
			magic_move=false;  //don't leave the magic move on
		}
	}
	
	public void Simulate2(double t, IslandManager im, ship cship) {
		double current_turn_rate;
		
		show_damage_timer-=t;

		if (type==stype.SHIP) 
		{
			angm+= steer* GetCurrentTurnRate(speed)* t;// will converge to steer in 1 m
			rotation = rotation + angm;
		}
	}
	
	public void ClearCollision() {
		// clear the information on a collision
		collided=0;
	}
	
	public void HandleCollision(int id) {
		// deal a collision
		// eventually this should have more info, like what I collided with
		// what direction, damage etc.  

		speed=0;
		steer=0;
		collided=id;
		
	}
	
	public void HandleCollision(int id, ship s) {
		
		// do two point collision
		

		steer = 0;
		collided = id;
		//speed = 0;
		
		// normalize the two xvel
	
		/*
		double mass_ratio = ((double)GetTotalWeight()) / ((double) (s.GetTotalWeight()));

		xvel= xvel*mass_ratio+(s.xvel*1/mass_ratio);
		yvel = yvel*mass_ratio+(s.yvel*1/mass_ratio);
		speed = Math.sqrt(xvel*xvel+yvel*yvel);
		*/
	}
 	
	public void SetPosition(float x, float y, float rot) {
		xpos=x;
		ypos=y;
		rotation=rot;
	}
	
	public void SetPosition(WorldThing wt) {
		xpos = wt.xpos;
		ypos = wt.ypos;
		rotation = wt.rotation;
	}
	
	public void DrawFlag(double x, double y, double r, double s) {
		// pick the color, draw the flag
		float fposx= (float) (x+ (hd.flagx*Math.cos(r) -(hd.flagy-3)*Math.sin(r))*s);
		float fposy= (float) (y+ (hd.flagx*Math.sin(r)+(hd.flagy-3)*Math.cos(r))*s);
		
		
		GL11.glLineWidth(3.0f);
		GL11.glBegin(GL11.GL_LINES);
		
		
		GL11.glColor3f(flagR, flagG, flagB);	

		WorldThing fp1 = new WorldThing();
		
		if (speed> GetMaxSpeed()/10) {
			if (speed> GetMaxSpeed()/2) {
				fp1.xpos=(hd.flagx+Math.random()*5-2)*s;
			}
			else
				fp1.xpos=(hd.flagx+Math.random()*3-1)*s;
		}
		else
			fp1.xpos=hd.flagx;
		fp1.ypos=hd.flagy;
		
		GL11.glVertex3d(fp1.rotX(r)+x, fp1.rotY(r)+y,0f);	
		
		WorldThing fp2= new WorldThing();

		fp2.xpos=0d;
		fp2.ypos=10d*s;		
		

		
		GL11.glVertex3d(fp2.rotX(r)+fposx, fp2.rotY(r)+fposy, 0);
		GL11.glEnd();

	}
	
	private void DrawComponent(double s) {
		
		if (hd.hull_type== HullData.BARGE_HULL) {
			int nn=0;
			nn++;
			
		}
		for (int i=0; i < component_count; i++) {
			WorldThing cworld = new WorldThing();
			//cworld.xpos=mountx[i];
			//cworld.ypos=mounty[i];
			cworld.xpos=hd.hardpointx[i];
			cworld.ypos=hd.hardpointy[i];
			double cworldx= cworld.rotX(rotation)*s;
			double cworldy= cworld.rotY(rotation)*s;
			
			if ((scomponent[i]!=null) && (scomponent[i].IsUsed()!=0)) {
				scomponent[i].xpos=cworldx+xpos; 		// update the components x and y
				scomponent[i].ypos=cworldy+ypos;  
				if (team==0) {

				
				  scomponent[i].SetRotation(-gunaim1, rotation, aim_mode);
				  scomponent[i].drawrot(xpos+cworldx, ypos + cworldy, scomponent[i].GetRotation(), s);
				}  else {

					scomponent[i].SetRotation(-gunaim1, rotation, aim_mode);
					scomponent[i].drawrot(xpos + cworldx, ypos + cworldy, scomponent[i].GetRotation(), s);					
				}
			} else {
				WorldThing marker = new WorldThing();
				marker.xpos=hd.hardpointx[i];
				marker.ypos=hd.hardpointy[i]+2;

				GL11.glBegin(GL11.GL_LINES);
				GL11.glColor3f(1.0f, 1.0f, 1.0f);
				GL11.glVertex3f((float) (cworldx+xpos), (float) (cworldy+ypos), 0f);
				GL11.glVertex3f((float)(marker.rotX(rotation)*s+xpos), (float)(marker.rotY(rotation)*s+ypos), 0f);
				GL11.glEnd();

					
			}

		}	
	}
	
	public void DrawIt() {

		if (hd.hull_type== HullData.BARGE_HULL) {
			int nn=0;
			nn++;
		}
		if (type==stype.SHIP)
		{
			dhull.drawrot(xpos, ypos, rotation);
			hd.hull.SetWorldPoints(xpos, ypos, rotation);
			for (int count=0; count < hd.prop_count; count++) {
				double pworldx=hd.propx[count]*Math.cos(rotation)-hd.propy[count]*Math.sin(rotation);
				double pworldy=hd.propx[count]*Math.sin(rotation)+hd.propy[count]*Math.cos(rotation);

				switch (prop_state[count]) {
				case 0:
					prop1.drawrot(pworldx+xpos, pworldy+ypos, rotation);
					break;
				case 1:
					prop2.drawrot(pworldx+xpos, pworldy+ypos, rotation);
					break;
				case 2:
					prop3.drawrot(pworldx+xpos, pworldy+ypos, rotation);
					break;
				case 3:
					prop2.drawrot(pworldx+xpos, pworldy+ypos, rotation);
					break;
				}
			}

			DrawComponent(1.0d);
			DrawFlag(xpos, ypos, rotation, 1.0d);
			if (show_damage_timer> 0) 
				ShowDamageBar();
		}
		else
		{
			dhull.drawrot(xpos, ypos, rotation, wreck_timer/max_wreck_timer, wreck_timer/max_wreck_timer);
		}
		



	}
	
	public void SetWorldPoints() {
		hd.hull.SetWorldPoints(xpos, ypos, rotation);
	}
	
	public void DrawScaled(double s) {

		if (type==stype.SHIP)
		{
			dhull.drawrot(xpos, ypos, rotation, s);
			hd.hull.SetWorldPoints(xpos, ypos, rotation, s);
			for (int count=0; count < hd.prop_count; count++) {
				double pworldx=(hd.propx[count]*Math.cos(rotation)-hd.propy[count]*Math.sin(rotation))*s;
				double pworldy=(hd.propx[count]*Math.sin(rotation)+hd.propy[count]*Math.cos(rotation))*s;

				switch (prop_state[count]) {
				case 0:
					prop1.drawrot(pworldx+xpos, pworldy+ypos, rotation, s);
					break;
				case 1:
					prop2.drawrot(pworldx+xpos, pworldy+ypos, rotation, s);
					break;
				case 2:
					prop3.drawrot(pworldx+xpos, pworldy+ypos, rotation, s);
					break;
				case 3:
					prop2.drawrot(pworldx+xpos, pworldy+ypos, rotation, s);
					break;
				}
			}
			DrawComponent(s);
		}
		/*
		else
		{
			dhull.drawrot(xpos, ypos, rotation, wreck_timer/max_wreck_timer, wreck_timer/max_wreck_timer);
		}
		*/

		DrawFlag(xpos, ypos, rotation, s);
	
	
	}
	
	boolean ComponentBounds(int i, double s, int x, int y ){
		// is x,y in the bounds of component i at the scale of s
		double cworldx=(mountx[i]*Math.cos(rotation)-mounty[i]*Math.sin(rotation))*s;
		double cworldy=(mountx[i]*Math.sin(rotation)+mounty[i]*Math.cos(rotation))*s;
		
		if ((x < cworldx+hd.width/2*s) && (x > cworldx-hd.width/2*s) &&
			(y < cworldy+hd.width/2*s) && (y > cworldy-hd.width/2*s)) {

			return true;
		}
		return false;
	}
	
	public boolean Collide(sketch skt) {
		return hd.hull.Collide(skt);
	}
	
	public boolean Collide(ship tship) {


		return hd.hull.Collide(tship.hd.hull);
	}
	
	public void Input(UserInput u, double t) {
		double pvx, pvy;  // projectile velocity
		
		
		
		if (team==0) {
			steer=0;
			throttle=0;		// process input
			if (u.TurnLeft()) {
				
				steer=1f;
				//rotation=rotation+0.14f*t;
			}
			if (u.TurnRight()) {
				steer=-1f;
				//rotation=rotation-0.14f*t;
				
			}
			if (u.Forward()) {
				throttle=1d;
				//speed=speed + accel*t;
				//System.out.println("Accel " + accel + " speed " + speed + " Max Speed " + max_speed + " time " + t);
			}
			if (u.Backward()) {
				throttle=-1d;
				// speed= speed-accel*t;
			}

			if (u.FireButton()==1) {
				trigger= 1;
				for (int i=0; i < component_count; i++) {
					if (scomponent[i].IsUsed()!=0) {

						 scomponent[i].FireButtonDown(xvel, yvel, u.trigx, u.trigy, u.aim_mode);
					}
					
				}
			} else
				trigger = 0;
			if (u.Reload()) {
				for (int i=0; i < component_count; i++) {
					if (scomponent[i].IsUsed()!=0)
						scomponent[i].Reload();
				}
			}
			if (rotation > Math.PI) rotation-=2*Math.PI;
			if (rotation < -Math.PI) rotation+=2*Math.PI;
			gunaim1=u.GetAim();
			aim_mode=u.aim_mode;
			
		}


		
	}
	
	// 
	// Non spaghetti version of BuildShip
	//
	public void BuildShip(int hullstyle, int hpcount, int bridgeposition, component cmat[]) {
		ComponentBuilder cb=new ComponentBuilder();  // do I still need this?  
		
		switch (hullstyle) {
		case 0:
			hd.BuildHull(hd);// hpcount, bridge position
			break;
		case 1:	
			hd.BuildWideHull(hd);
		case 2:
			// build diamond hull
			break;
		}	
	}
	public void BuildShip() {
		ComponentBuilder cb=new ComponentBuilder();
		
		if (Math.random() < 0.7) {
			hd.BuildHull(hd);
		}
		else hd.BuildWideHull(hd);
		dhull=new DamageSketch(hd.hull);  // this should copy in the values.
		//max_health = GetMaxHealth();// hd.CalcMaxHitpoints(); // max health
		health = GetMaxHealth();//max_health;
		int dam_counter=GetMaxHealth()/MAX_DAMAGE_STATES;
		damage_state_amt = new int[MAX_DAMAGE_STATES];
		for (int count=0; count < MAX_DAMAGE_STATES; count++) 
			damage_state_amt[count]=dam_counter*(count+1);
		
		//hd.max_speed = 25-hd.numhardpoints*3d;  // max speed
		accel= 3d-hd.width/15d;

		
		component_count=hd.numhardpoints;  // number of components
		float radius = (float) (hd.width/3d+Math.random()*hd.width/4d);
		int gun_per_turret = 1;
		if (radius > 9f) {
			gun_per_turret=2;
			if (radius > 12f) gun_per_turret=3;
		}	
		
		
		boolean has_mine = false;
		
		for (int cread=0; cread < component_count; cread++) {
			scomponent[cread]= new component();		
			if (Math.random()*10d < 5d) { // sweep cannon
				if ((Math.random() < 0.8d) || (has_mine)) {
					scomponent[cread] = cb.BuildCannon(scomponent[cread], radius, gun_per_turret);
					scomponent[cread].arc_type=hd.sweep[cread];
					scomponent[cread].SetComponentType( ComponentData.ctype.GUN);
				} else
				{
					scomponent[cread] = cb.BuildFixedCannon(scomponent[cread], (radius *0.6f), 0);
					scomponent[cread].cd.BuildMineSketch();
					scomponent[cread].arc_type=0;  // fixed
 					scomponent[cread].SetComponentType( ComponentData.ctype.MINE);
 					has_mine=true;
	
				}
			}
			else {
				// fixed cannon or torpedo 
				// if I am at the front facing forward, or the back facing backward I am a torpedo, 50% of the time
				if (((cread==0) && (hd.sweep[cread]==1) && (Math.random()>0.5d)) ||
						((cread==component_count-1) && (hd.sweep[cread]==2) && (Math.random()>0.5d))) {
					scomponent[cread] = cb.BuildFixedCannon(scomponent[cread], (radius *0.6f), 0);
					if ((Math.random() < 0.7d) || (has_mine)) {
						scomponent[cread].SetComponentType(ComponentData.ctype.TORPEDO);
					} else {
						scomponent[cread].cd.BuildMineSketch();
						scomponent[cread].SetComponentType(ComponentData.ctype.MINE);
						has_mine=true;
					}

				} else {
					scomponent[cread] = cb.BuildFixedCannon(scomponent[cread], (radius *0.6f), gun_per_turret);
					scomponent[cread].SetComponentType( ComponentData.ctype.FIXEDGUN);
		
				}

				scomponent[cread].arc_type=0;
				if ((hd.sweep[cread]==1) || (hd.sweep[cread]==5) || (hd.sweep[cread]==6)) {
					scomponent[cread].min_arc=0;
				}
				else  { 
					
					if ((hd.sweep[cread]==7) || (hd.sweep[cread]==8) || (hd.sweep[cread]==2)) {
						scomponent[cread].min_arc=Math.PI;
					}
					if (hd.sweep[cread]==3)
						scomponent[cread].min_arc=Math.PI/2;
					if (hd.sweep[cread]==4)
						scomponent[cread].min_arc=-Math.PI/2;
				
				}
			//	scomponent[cread].rotation=scomponent[cread].min_arc;
				

			}
			mountx[cread]=hd.hardpointx[cread];
			mounty[cread]=hd.hardpointy[cread];

			scomponent[cread].SetOwnerID(id, team);
		}
		
	

		dhull=new DamageSketch(hd.hull);  // this should copy in the values.
		
	}
	
	public void BuildFreighter() {
		if (Math.random()<0.6d) {
			hd.BuildHull(hd);
		} else
			hd.BuildWideHull(hd);
		BuildFreighter(hd);
		
	}
	
	public void BuildFreighter(HullData hdin) {
		hdin.CopyTo(hd);
		ComponentBuilder cb=new ComponentBuilder();

		dhull=new DamageSketch(hd.hull);  // this should copy in the values.
	//	max_health = hd.CalcMaxHitpoints(); 
		health=GetMaxHealth();
		int dam_counter=GetMaxHealth()/MAX_DAMAGE_STATES;
		damage_state_amt = new int[MAX_DAMAGE_STATES];
		for (int count=0; count < MAX_DAMAGE_STATES; count++) 
			damage_state_amt[count]=dam_counter*(count+1);
		
		//hd.max_speed = 20-hd.numhardpoints*3d;  // max speed
		accel= 2d-hd.width/15d;
	
		
		component_count=hd.numhardpoints;  // number of components
		for (int cread=0; cread < component_count; cread++) {
			scomponent[cread]= new component();		
			if ((cread > 4) && (cread%2==1)) {
				// if i am too big I can't be dlos

					scomponent[cread].cd.type=ComponentData.ctype.ENGINE;
					scomponent[cread].cd.BuildEngineSketch(0);

				} else
				{
					scomponent[cread].cd.type=ComponentData.ctype.CARGO;
					scomponent[cread].cd.BuildContainerSketch();
					scomponent[cread].cd.holding=(int) (Math.random()*3d)+1;
					scomponent[cread].BuildCrate(scomponent[cread].cd.holding);
				
				}

			scomponent[cread].SetOwnerID(id, team);
	
			mountx[cread]=hd.hardpointx[cread];
			mounty[cread]=hd.hardpointy[cread];
		
		}
		


		dhull=new DamageSketch(hd.hull);  // this should copy in the values.
		
	}
	
	void FillMixedWeapons() {	
		ComponentBuilder cb=new ComponentBuilder();
		int component_count=hd.numhardpoints;  // number of components
		float radius = (float) (hd.width/3d+Math.random()*hd.width/4d);
		int gun_per_turret = 1;
		if (radius > 9f) {
			gun_per_turret=2;
			if (radius > 12f) gun_per_turret=3;
		}	
		
		boolean has_weapon = false;
		while (!has_weapon) {
		
				for (int cread=0; cread < component_count; cread++) {
					scomponent[cread]= new component();		
					if (Math.random()*10d < 5d) { // sweep cannon
						
						if (Math.random()*10d <7d ) {
							scomponent[cread] = cb.BuildCannon(scomponent[cread], radius, gun_per_turret);
							scomponent[cread].arc_type=hd.sweep[cread];
							scomponent[cread].SetComponentType( ComponentData.ctype.GUN);
							has_weapon = true;
						} else
						{
							scomponent[cread].cd.type=ComponentData.ctype.CARGO;
							scomponent[cread].cd.BuildContainerSketch();
						}
					}
					else {
						// fixed cannon or torpedo 
						// if I am at the front facing forward, or the back facing backward I am a torpedo, 50% of the time
						if (((cread==0) && (hd.sweep[cread]==1) && (Math.random()>0.4d)) ||
								((cread==component_count-1) && (hd.sweep[cread]==2) && (Math.random()>0.5d))) {
							has_weapon = true;
							scomponent[cread] = cb.BuildFixedCannon(scomponent[cread], (radius *0.6f), 0);
							if (Math.random() < 0.6d) {
								scomponent[cread].SetComponentType(ComponentData.ctype.TORPEDO);
							} else {
								scomponent[cread].cd.BuildMineSketch();
								scomponent[cread].SetComponentType(ComponentData.ctype.MINE);
							}
						} else {
							if (Math.random()<0.5d) {
								scomponent[cread] = cb.BuildFixedCannon(scomponent[cread], (radius *0.6f), gun_per_turret);
								scomponent[cread].SetComponentType( ComponentData.ctype.FIXEDGUN);
								has_weapon=true;
							} else {
								scomponent[cread].cd.type=ComponentData.ctype.CARGO;
								scomponent[cread].cd.BuildContainerSketch();						
							}
								
				
						}
		
						scomponent[cread].arc_type=0;
						if ((hd.sweep[cread]==1) || (hd.sweep[cread]==5) || (hd.sweep[cread]==6)) {
							scomponent[cread].min_arc=0;
						}
						else  { 
							
							if ((hd.sweep[cread]==7) || (hd.sweep[cread]==8) || (hd.sweep[cread]==2)) {
								scomponent[cread].min_arc=Math.PI;
							}
							if (hd.sweep[cread]==3)
								scomponent[cread].min_arc=Math.PI/2;
							if (hd.sweep[cread]==4)
								scomponent[cread].min_arc=-Math.PI/2;
						
						}
						//scomponent[cread].rotation=scomponent[cread].min_arc;				
		
					}
					mountx[cread]=hd.hardpointx[cread];
					mounty[cread]=hd.hardpointy[cread];
		
					scomponent[cread].SetOwnerID(id, team);
				}
		}
	}

	
	
	
	public void BuildMixedShip() {

		if (Math.random()<0.8d) {
			hd.BuildHull(hd);
		} else
			hd.BuildWideHull(hd);		
		
		dhull=new DamageSketch(hd.hull);  // this should copy in the values.
	//	max_health = hd.CalcMaxHitpoints();  // max health
		health=GetMaxHealth();
		int dam_counter=GetMaxHealth()/MAX_DAMAGE_STATES;
		damage_state_amt = new int[MAX_DAMAGE_STATES];
		for (int count=0; count < MAX_DAMAGE_STATES; count++) 
			damage_state_amt[count]=dam_counter*(count+1);
		
		//hd.max_speed = 25-hd.numhardpoints*3d;  // max speed
		accel= 3d-hd.width/15d;
		
		component_count=hd.numhardpoints;  // number of components
		float radius = (float) (hd.width/3d+Math.random()*hd.width/4d);
		int gun_per_turret = 1;
		if (radius > 9f) {
			gun_per_turret=2;
			if (radius > 12f) gun_per_turret=3;
		}
		
		FillMixedWeapons();
		/*
		boolean has_weapon = false;
		while (!has_weapon) {
		
				for (int cread=0; cread < component_count; cread++) {
					scomponent[cread]= new component();		
					if (Math.random()*10d < 5d) { // sweep cannon
						
						if (Math.random()*10d <7d ) {
							scomponent[cread] = cb.BuildCannon(scomponent[cread], radius, gun_per_turret);
							scomponent[cread].arc_type=hd.sweep[cread];
							scomponent[cread].SetComponentType( ComponentData.ctype.GUN);
							has_weapon = true;
						} else
						{
							scomponent[cread].cd.type=ComponentData.ctype.CARGO;
							scomponent[cread].cd.BuildContainerSketch();
						}
					}
					else {
						// fixed cannon or torpedo 
						// if I am at the front facing forward, or the back facing backward I am a torpedo, 50% of the time
						if (((cread==0) && (hd.sweep[cread]==1) && (Math.random()>0.4d)) ||
								((cread==component_count-1) && (hd.sweep[cread]==2) && (Math.random()>0.5d))) {
							has_weapon = true;
							scomponent[cread] = cb.BuildFixedCannon(scomponent[cread], (radius *0.6f), 0);
							if (Math.random() < 0.6d) {
								scomponent[cread].SetComponentType(ComponentData.ctype.TORPEDO);
							} else {
								scomponent[cread].cd.BuildMineSketch();
								scomponent[cread].SetComponentType(ComponentData.ctype.MINE);
							}
						} else {
							if (Math.random()<0.5d) {
								scomponent[cread] = cb.BuildFixedCannon(scomponent[cread], (radius *0.6f), gun_per_turret);
								scomponent[cread].SetComponentType( ComponentData.ctype.FIXEDGUN);
								has_weapon=true;
							} else {
								scomponent[cread].cd.type=ComponentData.ctype.CARGO;
								scomponent[cread].cd.BuildContainerSketch();						
							}
								
				
						}
		
						scomponent[cread].arc_type=0;
						if ((hd.sweep[cread]==1) || (hd.sweep[cread]==5) || (hd.sweep[cread]==6)) {
							scomponent[cread].min_arc=0;
						}
						else  { 
							
							if ((hd.sweep[cread]==7) || (hd.sweep[cread]==8) || (hd.sweep[cread]==2)) {
								scomponent[cread].min_arc=Math.PI;
							}
							if (hd.sweep[cread]==3)
								scomponent[cread].min_arc=Math.PI/2;
							if (hd.sweep[cread]==4)
								scomponent[cread].min_arc=-Math.PI/2;
						
						}
						//scomponent[cread].rotation=scomponent[cread].min_arc;				
		
					}
					mountx[cread]=hd.hardpointx[cread];
					mounty[cread]=hd.hardpointy[cread];
		
					scomponent[cread].SetOwnerID(id, team);
				}
		}
		
		*/

		dhull=new DamageSketch(hd.hull);  // this should copy in the values.
		
	}
	
	public void BuildMixedShip(HullData hdin) {
		ComponentBuilder cb=new ComponentBuilder();
		hdin.CopyTo(hd);
		
		dhull=new DamageSketch(hd.hull);  // this should copy in the values.
	//	max_health = hd.CalcMaxHitpoints();  // max health
		health= GetMaxHealth();//max_health;
		int dam_counter= GetMaxHealth()/MAX_DAMAGE_STATES;
		damage_state_amt = new int[MAX_DAMAGE_STATES];
		for (int count=0; count < MAX_DAMAGE_STATES; count++) 
			damage_state_amt[count]=dam_counter*(count+1);
		
		//hd.max_speed = 25-hd.numhardpoints*3d;  // max speed
		accel= 3d-hd.width/15d;
		


		component_count=hd.numhardpoints;  // number of components
		float radius = (float) (hd.width/3d+Math.random()*hd.width/4d);
		int gun_per_turret = 1;
		if (radius > 9f) {
			gun_per_turret=2;
			if (radius > 12f) gun_per_turret=3;
		}	
		FillMixedWeapons();
		/*
		for (int cread=0; cread < component_count; cread++) {
			scomponent[cread]= new component();		
			if (Math.random()*10d < 5d) { // sweep cannon
				
				if (Math.random()*10d <7d ) {
					scomponent[cread] = cb.BuildCannon(scomponent[cread], radius, gun_per_turret);
					scomponent[cread].arc_type=hd.sweep[cread];
					scomponent[cread].SetComponentType( ComponentData.ctype.GUN);
				} else
				{
					scomponent[cread].cd.type=ComponentData.ctype.CARGO;
					scomponent[cread].cd.BuildContainerSketch();
				}
			}
			else {
				// fixed cannon or torpedo 
				// if I am at the front facing forward, or the back facing backward I am a torpedo, 50% of the time
				if (((cread==0) && (hd.sweep[cread]==1) && (Math.random()>0.4d)) ||
						((cread==component_count-1) && (hd.sweep[cread]==2) && (Math.random()>0.5d))) {
					scomponent[cread] = cb.BuildFixedCannon(scomponent[cread], (radius *0.6f), 0);
					if (Math.random() < 0.6d) {
						scomponent[cread].SetComponentType(ComponentData.ctype.TORPEDO);
					} else {
						scomponent[cread].cd.BuildMineSketch();
						scomponent[cread].SetComponentType(ComponentData.ctype.MINE);
					}
				} else {
					if (Math.random()<0.5d) {
						scomponent[cread] = cb.BuildFixedCannon(scomponent[cread], (radius *0.6f), gun_per_turret);
						scomponent[cread].SetComponentType( ComponentData.ctype.FIXEDGUN);
					} else {
						scomponent[cread].cd.type=ComponentData.ctype.CARGO;
						scomponent[cread].cd.BuildContainerSketch();						
					}
						
		
				}

				scomponent[cread].arc_type=0;
				if ((hd.sweep[cread]==1) || (hd.sweep[cread]==5) || (hd.sweep[cread]==6)) {
					scomponent[cread].min_arc=0;
				}
				else  { 
					
					if ((hd.sweep[cread]==7) || (hd.sweep[cread]==8) || (hd.sweep[cread]==2)) {
						scomponent[cread].min_arc=Math.PI;
					}
					if (hd.sweep[cread]==3)
						scomponent[cread].min_arc=Math.PI/2;
					if (hd.sweep[cread]==4)
						scomponent[cread].min_arc=-Math.PI/2;
				
				}
				//scomponent[cread].rotation=scomponent[cread].min_arc;				

			}
			mountx[cread]=hd.hardpointx[cread];
			mounty[cread]=hd.hardpointy[cread];

			scomponent[cread].SetOwnerID(id, team);
		}
		
		*/

		dhull=new DamageSketch(hd.hull);  // this should copy in the values.
		

	}
	public void BuildShip(HullData nhd) {
	//	ComponentBuilder cb=new ComponentBuilder();
		hd = new HullData();
		nhd.CopyTo(hd);

		dhull=new DamageSketch(hd.hull);  // this should copy in the values.
	//	max_health = hd.CalcMaxHitpoints(); // max health
		health=GetMaxHealth();
		int dam_counter=GetMaxHealth()/MAX_DAMAGE_STATES;
		damage_state_amt = new int[MAX_DAMAGE_STATES];
		for (int count=0; count < MAX_DAMAGE_STATES; count++) 
			damage_state_amt[count]=dam_counter*(count+1);
		
		//hd.max_speed = 25-hd.numhardpoints*3d;  // max speed
		accel= 3d-hd.width/15d;

		component_count=hd.numhardpoints;  // number of components

		
		for (int cread=0; cread < component_count; cread++) {
			mountx[cread]=hd.hardpointx[cread];
			mounty[cread]=hd.hardpointy[cread];
			scomponent[cread]=new component();
			scomponent[cread].used=0;

		}
		
	

		dhull=new DamageSketch(hd.hull);  // this should copy in the values.
		hd.DerivedQualities();  // fill out speed and so forth
	}
	
	public void BuildAttackShip(HullData nhd) {
		ComponentBuilder cb=new ComponentBuilder();
		hd=nhd;

		dhull=new DamageSketch(hd.hull);  // this should copy in the values.
		//max_health = hd.CalcMaxHitpoints(); // max health
		health=GetMaxHealth();
		int dam_counter = GetMaxHealth()/MAX_DAMAGE_STATES;
		damage_state_amt = new int[MAX_DAMAGE_STATES];
		for (int count=0; count < MAX_DAMAGE_STATES; count++) 
			damage_state_amt[count]=dam_counter*(count+1);
		
		//hd.max_speed = 25-hd.numhardpoints*3d;  // max speed
		accel= 3d-hd.width/15d;

		component_count=hd.numhardpoints;  // number of components
		float radius = (float) (hd.width/3d+Math.random()*hd.width/4d);
		int gun_per_turret = 1;
		if (radius > 9f) {
			gun_per_turret=2;
			if (radius > 12f) gun_per_turret=3;
		}	

		
		for (int cread=0; cread < component_count; cread++) {
			mountx[cread]=hd.hardpointx[cread];
			mounty[cread]=hd.hardpointy[cread];
			scomponent[cread]=new component();
			scomponent[cread] = cb.BuildCannon(scomponent[cread], radius, gun_per_turret);
			scomponent[cread].arc_type=hd.sweep[cread];
			scomponent[cread].SetComponentType( ComponentData.ctype.GUN);
			scomponent[cread].SetOwnerID(id, team);
			
		}
		
	

		dhull=new DamageSketch(hd.hull);  // this should copy in the values.
			
	}
	

	public void AttachComponent(component c, int hp) {
		int hpdeficit = GetMaxHealth() - health;
		c.used=1;
		scomponent[hp]=new component();
		scomponent[hp].CopyFrom(c);
		
		
		
		if (scomponent[hp].cd.type==ComponentData.ctype.GUN)
			scomponent[hp].arc_type=hd.sweep[hp];		
		mountx[hp]=hd.hardpointx[hp];
		mounty[hp]=hd.hardpointy[hp];

		scomponent[hp].SetOwnerID(id, team);
		
		



		// if fixed

		if (scomponent[hp].arc_type==0)  {

			if ((hd.sweep[hp]==1) || (hd.sweep[hp]==5) || (hd.sweep[hp]==6)) {
				scomponent[hp].min_arc=0;
			}
			else  { 
				
				if ((hd.sweep[hp]==7) || (hd.sweep[hp]==8) || (hd.sweep[hp]==2)) {
					scomponent[hp].min_arc=Math.PI;
				}
				if (hd.sweep[hp]==3)
					scomponent[hp].min_arc=Math.PI/2;
				if (hd.sweep[hp]==4)
					scomponent[hp].min_arc=-Math.PI/2;
			
			}
		
		
		}
		
		scomponent[hp].SetAmmo();
		
		if (scomponent[hp].cd.type == ComponentData.ctype.REPAIR) {
			health= GetMaxHealth()- hpdeficit;
		}
		
	}
	
	public void StartingComponents() {
		// for now standardize the components
		ComponentBuilder cb=new ComponentBuilder();
		
		float radius = (float) Math.random()*8f+5f;
		int gun_per_turret = 1;
		if (radius > 7f) {
			gun_per_turret=2;
			if (radius > 11f) gun_per_turret=3;
		}
		
		for (int cread=0; cread < component_count; cread++) {
			// load x & y

			//System.out.println(cname);
			//System.out.println("filler= " + filler);
			scomponent[cread]= new component();
			scomponent[cread]= 	cb.BuildCannon(scomponent[cread], radius, gun_per_turret);
			scomponent[cread].SetComponentType(ComponentData.ctype.GUN);
			mountx[cread]=hd.hardpointx[cread];
			mounty[cread]=hd.hardpointy[cread];

			scomponent[cread].arc_type=hd.sweep[cread];
			//scomsweep[cread]=scomponent[cread].arc_type;
			scomponent[cread].SetOwnerID(id, team);
			
			
		}
		hd.numhardpoints=component_count;
	}
	
	
	// this should no longer work
	// due to changes in the files
	public void load(String shipfile) {
		
		// ptboat.txt is the test file.  
		
		// String s;
		// int i = Integer.parseInt(s.trim);	
		ComponentBuilder cb=new ComponentBuilder();

		
		try {
			Scanner sdata= new Scanner(new FileInputStream(shipfile));	

			String hullname  = sdata.nextLine();
			hullfile=hullname;
			//System.out.println("hullname: " + hullname);
			
			hd.hull=new sketch(hullname);
			dhull=new DamageSketch(hullname);
			sdata.useDelimiter(",\\s");   // has to end with space and then comma

		//	max_health = sdata.nextInt();  // max health
		//	health=max_health;
			int dam_counter= GetMaxHealth()/MAX_DAMAGE_STATES;
			damage_state_amt = new int[MAX_DAMAGE_STATES];
			for (int count=0; count < MAX_DAMAGE_STATES; count++) {
				damage_state_amt[count]=dam_counter*(count+1);
			}
			hd.max_speed = sdata.nextInt();  // max speed
			accel= sdata.nextFloat();
			float turn_rate= sdata.nextFloat();  // I don't use this anymore
			
			component_count=sdata.nextInt();  // number of components
			hd.flagx=sdata.nextInt();
			hd.flagy=sdata.nextInt();
			
			hd.width=15d;  //  VERY ARBITRARY BECAUSE I DON'T LOAD IT
			hd.hull_type=HullData.THIN_HULL;
			
			hd.max_weight= component_count*75;  // AGAIN I DON'T LOAD IT BUT I NEED TO HAVE IT
			
			
			// for now standardize the components
			float radius = (float) Math.random()*8f+5f;
			int gun_per_turret = 1;
			if (radius > 7f) {
				gun_per_turret=2;
				if (radius > 11f) gun_per_turret=3;
			}
			
			for (int cread=0; cread < component_count; cread++) {
				// load x & y
				String filler = sdata.nextLine();
				String cname= sdata.nextLine();
				//System.out.println(cname);
				//System.out.println("filler= " + filler);
				scomponent[cread]= new component(cname);
				scomponent[cread]= 	cb.BuildCannon(scomponent[cread], radius, gun_per_turret);
				scomponent[cread].SetComponentType(ComponentData.ctype.GUN);
				mountx[cread]=sdata.nextInt();
				mounty[cread]=sdata.nextInt();
				hd.hardpointx[cread]=mountx[cread];
				hd.hardpointy[cread]=mounty[cread];
				scomponent[cread].arc_type=sdata.nextInt();
				hd.sweep[cread]=scomponent[cread].arc_type;
				scomponent[cread].SetOwnerID(id, team);
				
				
			}
			hd.numhardpoints=component_count;
			
			
			sdata.close();		
		}
		catch (FileNotFoundException fe) {
					
		}

	
	}
	
	

	
	public void LoadProps() {
		/*
		 *  For now I am just testing props, so write some code that adds props
		 */
		

		prop_timer = new double[hd.MAX_PROPS];
		prop_state = new int[hd.MAX_PROPS];  // 0 to 4		
		hd.prop_count=2;
		prop1 = new sketch();
		prop1.numpoints=12;
		prop1.xmat[0]=0;
		prop1.ymat[0]=-5;
		prop1.xmat[1]=0;
		prop1.ymat[1]=2;
		
		prop1.xmat[2]=-5;
		prop1.ymat[2]=-5;		
		prop1.xmat[3]=5;
		prop1.ymat[3]=-5;
		
		prop1.xmat[4]=-5;
		prop1.ymat[4]=-5;
		
		prop1.xmat[5]=-3;
		prop1.ymat[5]=-7;
		prop1.xmat[6]=-3;
		prop1.ymat[6]=-7;
		prop1.xmat[7]=0;
		prop1.ymat[7]=-5;
		prop1.xmat[8]=5;
		prop1.ymat[8]=-5;
		prop1.xmat[9]=3;
		prop1.ymat[9]=-3;
		prop1.xmat[10]=3;
		prop1.ymat[10]=-3;
		prop1.xmat[11]=0;
		prop1.ymat[11]=-5;

		prop2 = new sketch();
		prop2.numpoints=12;
		
		prop2.xmat[0]=0;
		prop2.ymat[0]=-5;
		prop2.xmat[1]=0;
		prop2.ymat[1]=2;
		
		prop2.xmat[2]=-3;
		prop2.ymat[2]=-5;		
		prop2.xmat[3]=3;
		prop2.ymat[3]=-5;
		
		prop2.xmat[4]=-3;
		prop2.ymat[4]=-5;
		
		prop2.xmat[5]=0;
		prop2.ymat[5]=-7;
		prop2.xmat[6]=0;
		prop2.ymat[6]=-7;
		prop2.xmat[7]=-3;
		prop2.ymat[7]=-5;
		
		
		prop2.xmat[8]=-3;
		prop2.ymat[8]=-5;
		prop2.xmat[9]=0;
		prop2.ymat[9]=-3;
		prop2.xmat[10]=0;
		prop2.ymat[10]=-3;
		prop2.xmat[11]=3;
		prop2.ymat[11]=-5;

		prop3 = new sketch();
		prop3.numpoints=12;
		prop3.xmat[0]=0;
		prop3.ymat[0]=-5;
		prop3.xmat[1]=0;
		prop3.ymat[1]=2;
		
		prop3.xmat[2]=-5;
		prop3.ymat[2]=-5;		
		prop3.xmat[3]=5;
		prop3.ymat[3]=-5;
		
		prop3.xmat[4]=-5;
		prop3.ymat[4]=-5;
		prop3.xmat[5]=-3;
		prop3.ymat[5]=-3;

		prop3.xmat[6]=-3;
		prop3.ymat[6]=-3;
		prop3.xmat[7]=0;
		prop3.ymat[7]=-5;
		
		prop3.xmat[8]=5;
		prop3.ymat[8]=-5;
		prop3.xmat[9]=3;
		prop3.ymat[9]=-7;
		prop3.xmat[10]=3;
		prop3.ymat[10]=-7;
		prop3.xmat[11]=0;
		prop3.ymat[11]=-5;
		
				
		hd.propx[0] = 8;
		hd.propy[0] = -40;
		prop_timer[0]=0.0d;
	
		hd.propx[1] = -8;
		hd.propy[1] = -40;
		prop_timer[1]=0.5d;

		max_prop_timer=1.0d;

		
		for (int count=0; count < 12; count++)
		{
			prop1.bcolor[count]=1.0f;
			prop1.gcolor[count]=1.0f;
			prop1.rcolor[count]=1.0f;
			prop2.bcolor[count]=1.0f;
			prop2.gcolor[count]=1.0f;
			prop2.rcolor[count]=1.0f;
			prop3.bcolor[count]=1.0f;
			prop3.gcolor[count]=1.0f;
			prop3.rcolor[count]=1.0f;
			
		}
		
	}
	
	
	public ship(int newid) {
		id=newid;
		type= stype.SHIP;
		hd= new HullData();
		hd.max_speed=10d;
		steer=0d;
		throttle=0d;

		component_count=0;
		
		scomponent= new component[HullData.MAX_COMPONENTS];
		mountx=new double[HullData.MAX_COMPONENTS];	
		mounty=new double[HullData.MAX_COMPONENTS];
		
		wreck_timer=0.0d;
		max_wreck_timer=15.0d;

			
		//component_count=2;
		gunaim1=0.0f;
		trigger=0;
		LoadProps();
		
	
	}
	public ship(int newid, String hullf) {
		id=newid;

		type= stype.SHIP;
		hd= new HullData();
		hd.max_speed=10d;;
		steer=0d;
		throttle=0d;

			
		
		scomponent= new component[HullData.MAX_COMPONENTS];
		mountx=new double[HullData.MAX_COMPONENTS];
		mounty=new double[HullData.MAX_COMPONENTS];
		wreck_timer=0.0d;	
		max_wreck_timer=15.0d;
		
		repair_timer=0.0d;
		
		magic_move=false;
		mmove= new WorldThing();



		hd.hull=new sketch(hullf);
		dhull= new DamageSketch(hullf);
		hullfile = hullf;
		gunaim1=0.0f;
		trigger=0;
		LoadProps();
		
	//	System.out.println("Ship made with filename");	
	}


}
