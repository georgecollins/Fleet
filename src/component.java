
public class component extends WorldThing {
	
	sketch ammo[];

	static final int SHOW_AMMO = 1;//  don't show ammo  do show ammo = 1
	ComponentData cd;
	
	int used;
	int damaged;
	int ownerid;
	int team;  // what team am I on
	int arc_type;

	double min_arc, max_arc;  // the arc I can be in .  
	double countdown_time;	// time to refresh
	double damage_time;  // how long it is damaged for
	

	double throb_time;
	int cycle;
	
	int clip_count;
	double clip_refresh;
	
	static final int MAX_AMMO=8;
	static final int MAX_GUNS= 3;



	sketch crate;
	
	ProjectileManager PM;
	
	/*
	 * 	I need to discriminate between the angle the user wants me to point
	 *  and the angle I am relative to my 
	 *  hull
	 */
	public void SetRotation(double goal, double hull_angle, int control_mode) {
		
		// does this fit into min and maxrotation??
		// also have to add 
		// System.out.println("Goal: " + goal + " hull " + hull_angle + " rotation " + rotation);
		double relative_angle= goal-hull_angle;
		while ((relative_angle> Math.PI) || (relative_angle< -Math.PI)) {
			if (relative_angle > Math.PI) relative_angle-=2*Math.PI;
			if (relative_angle < -Math.PI) relative_angle+=2*Math.PI;
		}
		if (control_mode==1) {   // button aim mode
			relative_angle=goal;
			
		}
		switch (arc_type) {
			case 0:  // fixed gun
				relative_angle=min_arc;
				break;			
			case 1:  // front facing
				if ((relative_angle < 0) && (relative_angle < -Math.PI*3/4)) {
					relative_angle= 0;//-Math.PI*3/4;
				}
				if ((relative_angle > 0) && (relative_angle > Math.PI*3/4)) {
					relative_angle = 0;// Math.PI*3/4;
				}
				break;
			case 2:  // rear facing
				if (((relative_angle) > -Math.PI*1/4) && (relative_angle< 0)) relative_angle = Math.PI;//-Math.PI*1/4;
				if (((relative_angle) > 0) && (relative_angle < Math.PI*1/4)) relative_angle = Math.PI; //Math.PI*1/4;
				break;
				
			case 3:  // left side facing		
				if ((relative_angle< 0) && (relative_angle> -Math.PI/2)) relative_angle=0d;
				if ((relative_angle<0) && (relative_angle< -Math.PI/2)) relative_angle=Math.PI;
				break;
			case 4: // right side facing
				if ((relative_angle> 0) && (relative_angle< Math.PI/2)) relative_angle=0d;
				if ((relative_angle>0) && (relative_angle> Math.PI/2)) relative_angle=-Math.PI;
				break;
			case 5: // Left front corner
				if (relative_angle<-Math.PI/4d) relative_angle=(-Math.PI/4d);
				if (relative_angle>Math.PI*3d/4d) relative_angle=Math.PI*3d/4d;
				break;
			case 6: // right front corner
				if (relative_angle > Math.PI/4d) relative_angle=Math.PI/4d;
				if (relative_angle < -Math.PI*3d/4d) relative_angle=(-Math.PI*3d/4d);
				break;
				
			case 7: // left rear corner
				if ((relative_angle < 0) && (relative_angle > -Math.PI*3d/4d)) relative_angle=(-Math.PI*3d/4d);
				if ((relative_angle > 0) && (relative_angle < Math.PI/4d)) relative_angle=Math.PI/4d;
				break;
			case 8:  // right rear corner
				if ((relative_angle < 0) && (relative_angle > -Math.PI/4d)) relative_angle = (-Math.PI/4d);
				if ((relative_angle > 0) && (relative_angle < Math.PI*3d/4d)) relative_angle = Math.PI*3d/4d;
				break;
			
		}		
		rotation=hull_angle+relative_angle;
	}
	public double GetRotation() {
		return rotation;
	}
	
	public void ShowAmmo(double xpos, double ypos, double r) {
		// I could have ammo sketches for every thing and then do ammo 1, ammo2 etc.  
		for (int count=0; count < (cd.clip_size - clip_count); count++ ) 
			if (ammo[count]!=null) {
				ammo[count].drawrot(xpos, ypos, r);	
			}
		
	}
	
	public void DrawIt() {
		
		if (cd.type!=ComponentData.ctype.EMPTY)
			cd.csketch.drawit();
		// skip drawing ammo for now

		if ((cd.type!=ComponentData.ctype.TORPEDO) && (SHOW_AMMO!=0) && (countdown_time< 0.1d)) {	
			if (clip_count > 0)
				ammo[0].drawit();
		}

	}
	
	public void Simulate(double t) {
		countdown_time-=t;
		
		if (clip_count==cd.clip_size) {
			clip_refresh=clip_refresh+t;
			if (clip_refresh > cd.GetReloadTime()) {
				clip_count=0;
				clip_refresh=0;
				countdown_time=0d;
			}
		}
		if (damaged!=0) {
			cd.cdsketch.SimulateExplosion(0.75d, damage_time);
			damage_time=damage_time+t;
			if (damage_time > 10.0d)
				used=0;
		}
		
		if (cd.type==ComponentData.ctype.ENGINE) {
			throb_time=throb_time+t;
			if (throb_time > 2d) {
				throb_time=0;
				cycle++;
				if (cycle > 3)
					cycle = 0;
				cd.BuildEngineSketch(cycle);
			}
			
		}
		
		// TEST
		if ((cd.type==ComponentData.ctype.TORPEDO) && (countdown_time<0.1f))
			clip_count=0;
	}
	public void Reload() {
		clip_count=cd.clip_size;
		// timer take care of the rest
	}
	
	public void FireButtonDown(double xvel, double yvel, double trigx, double trigy, int aim_type) {

		if ((cd.type==ComponentData.ctype.GUN) || (cd.type==ComponentData.ctype.FIXEDGUN))
		{
			if (clip_count < cd.clip_size) {
				if (countdown_time<0.1f) {
					double x=(cd.csketch.maxx+cd.csketch.minx)/2;
					double y=(cd.csketch.maxy+cd.csketch.miny)/2;
					double xv=Math.cos(rotation+Math.PI/2)*cd.GetProjectileSpeed();
					double yv=Math.sin(rotation+Math.PI/2)*cd.GetProjectileSpeed();//40;
				
					// i seem to be messing up x and y
					double u = Math.sin(rotation + Math.PI/2);
					double v = Math.cos(rotation + Math.PI/2);
					
					
					// are we more or less on target
					//System.out.println(" u " + u + " v " + v + " xtrig " + trigx + " ytrig " + trigy);
					
					//System.out.println(" xvel " + xvel + " yvel " + yvel + " xv " + xv + " yv " + yv);
					
					if (((u-trigx)*(u-trigx) < 0.10) && ((v-trigy) * (v-trigy) < 0.10) || (aim_type==1)) {
	
						for (int count=0; count< cd.guncount; count++) {
						
							double mx=cd.muzzlex[count]*Math.cos(rotation)-cd.muzzley[count]*Math.sin(rotation);
							double my=cd.muzzlex[count]*Math.sin(rotation)+cd.muzzley[count]*Math.cos(rotation);	
							
							PM.AddProjectile(x+mx, y+my, xv+xvel, yv+yvel, rotation+Math.PI/2, ownerid, team);
						}
						clip_count++;
						// System.out.println("Clip count " + clip_count);
					}
					else 
					{

					}
					countdown_time=cd.GetRefreshTime();
	
				}
			}
		}
		if  (cd.type==ComponentData.ctype.TORPEDO)
		{
			if (countdown_time<0.1f) {	
				countdown_time= cd.GetRefreshTime();
				
				double mx=cd.muzzlex[0]*Math.cos(rotation)-cd.muzzley[0]*Math.sin(rotation);
				double my=cd.muzzlex[0]*Math.sin(rotation)+cd.muzzley[0]*Math.cos(rotation);	
				
				double x=(cd.csketch.maxx + cd.csketch.minx)/2;
				double y=(cd.csketch.maxy + cd.csketch.miny)/2;
				double xv=Math.cos(rotation+Math.PI/2)*cd.GetProjectileSpeed();// should be torpedo speed
				double yv=Math.sin(rotation+Math.PI/2)*cd.GetProjectileSpeed();// should be torpedo speed
				PM.AddTorpedo(x+mx, y+my, xv, yv, rotation+Math.PI/2, ownerid, team);  // doesn't inherit velocity like a projectile
				clip_count++;

			}
		}
		if  (cd.type==ComponentData.ctype.MINE)
		{
			if (countdown_time<0.1f) {	
				countdown_time= cd.GetRefreshTime();
				
				double mx=cd.muzzlex[0]*Math.cos(rotation)-cd.muzzley[0]*Math.sin(rotation);
				double my=cd.muzzlex[0]*Math.sin(rotation)+cd.muzzley[0]*Math.cos(rotation);	
				
				double x=(cd.csketch.maxx + cd.csketch.minx)/2;
				double y=(cd.csketch.maxy + cd.csketch.miny)/2;
				
				double xv=Math.cos(rotation+Math.PI/2)*12+xvel;// should be torpedo speed
				double yv=Math.sin(rotation+Math.PI/2)*12+yvel;// should be torpedo speed
				
				PM.AddMine(x+mx, y+my, xv, yv, rotation+Math.PI/2, ownerid, team);  // doesn't inherit velocity like a projectile

			}
		}
	}
	public void drawrot(double xpos, double ypos, double r) {
		if (damaged==0) {
			cd.csketch.drawrot(xpos, ypos,  r);
			if ((crate!=null) && (cd.type==ComponentData.ctype.CARGO)) {
				crate.drawrot(xpos, ypos,r);
			}
		} else {
			cd.cdsketch.drawrot(xpos,ypos, r);
		}
			
		if (((cd.type==ComponentData.ctype.GUN) || (cd.type==ComponentData.ctype.FIXEDGUN) || 
				((cd.type==ComponentData.ctype.TORPEDO) && (countdown_time < 0.1))) && (SHOW_AMMO!=0))  
			ShowAmmo(xpos, ypos, r);
		
	}
	
	public void drawrot(double xpos, double ypos, double r, double s) {
		if (damaged==0) {
			cd.csketch.drawrot(xpos, ypos,  r, s);
			if ((crate!=null) && (cd.type==ComponentData.ctype.CARGO)) {
				crate.drawrot(xpos, ypos,r, s);
			}
		} else {

			if (cd==null) {
				int xx=0;
				xx=1/xx;
			}
			
			cd.cdsketch.drawrot(xpos,ypos, r, s);

		}
		if (((cd.type==ComponentData.ctype.GUN) || (cd.type==ComponentData.ctype.FIXEDGUN) || 
				((cd.type==ComponentData.ctype.TORPEDO) && (countdown_time < 0.1))) && (SHOW_AMMO!=0))  
			ShowAmmo(xpos, ypos, r);
	
		// if ((cd.type!=ComponentData.ctype.TORPEDO)&& (SHOW_AMMO!=0))  ShowAmmo(xpos, ypos, r);
	}

	public void draw_inactive(double xpos, double ypos, double r, double s) {
		cd.drawrot(xpos, ypos, r, s);
	}

	public int IsUsed() {
		return used;
	}
	
	
	
	public void CopyFrom(component c) {
		ammo=c.ammo;

		cd= new ComponentData();
		c.cd.CopyTo(cd);
		
		used=c.used;
		damaged=c.damaged;
		ownerid=c.ownerid;
		team=c.team;  // what team am I on
		arc_type=c.arc_type;

		min_arc= c.min_arc;
		max_arc= c.max_arc;  // the arc I can be in .  

		countdown_time = c.cd.GetRefreshTime();	// time to refresh
		damage_time= c.damage_time;  // how long it is damaged for
		

		clip_count= c.clip_count;

		clip_refresh=c.clip_refresh;
		

		crate = c.crate;
		
		PM= c.PM;
	}
	/*
	 * Set up the things about a component that don't change, like it's position and if it is used.  
	 */
	public void Init(float x, float y) {
		// this is never called.

		xpos=x;
		ypos=y;
		cd.csketch.Erase();
		used=0;
		rotation=min_arc;
		

	}
	public void SetArcType(int ain) {
		arc_type=ain;
	}
	public void SetComponentType(ComponentData.ctype ct) {
		cd.type=ct;
		SetAmmo();
		
	}

	public void SetPM(ProjectileManager pmin) {
		PM=pmin;
	}
	
	public void SetOwnerID(int id, int teamin) {
		ownerid=id;
		team=teamin;
	}
	
	public void Repair() {
		// fix this component
		used=1;
		damaged=0;
		damage_time=0;
		cd.cdsketch= new DamageSketch(cd.csketch);
	}

	public void SetAmmo() {
		float ammox=0;
		float ammoy=3;
		if ((cd.type==ComponentData.ctype.FIXEDGUN) || (cd.type==ComponentData.ctype.GUN)) {
			for (int count=0; count < MAX_AMMO; count++) {
				ammo[count]=new sketch();
				ammo[count].rcolor[0] = 0.75f;
				ammo[count].gcolor[0] = 0.0f;
				ammo[count].bcolor[0] = 0.0f;
				ammo[count].width[0] =1.0f;  // widen it up
				
	
				ammox=count*2- MAX_AMMO/4*2;
					/*
					if (count/2 > 0) {  // was count /2 > 0
						ammox=(count+1)/2*3;
					} else {
						ammox= -1* count/2*3;
						
					}
					*/
				if (count > MAX_AMMO/2-1) {
					ammoy=-6f;
					ammox=(count-count/2-1)*2- MAX_AMMO/4*2;
				}
			
				ammo[count].numpoints=2;
				ammo[count].xmat[0]= (float)ammox;
				ammo[count].ymat[0]= (float) ammoy-1;
				ammo[count].xmat[1]= (float) ammox;
				ammo[count].ymat[1]= (float) ammoy-3;
			
			}
		}
		if (cd.type==ComponentData.ctype.TORPEDO) {
			projectile temp = new projectile();
			temp.MakeTorpedo(20, 0);
			for (int count=0; count < MAX_AMMO; count++) {
				ammo[count]=new sketch();
				temp.pd.psketch.CopyTo(ammo[count]);
			}
		}
	}
	// so that both constructors fill the data the same way and
	// I can reset the data later if I want to
	private void FillData() {
		used=1;
		damaged=0;
		rotation=0f;

		countdown_time = 0.0f;	// trigger time
		damage_time = 0.0d;  // how log it is blowin up
		

		cd.clip_size = 3;
		clip_count = 0;
		clip_refresh =4d;	
		
		
		// make the ammo counters
		ammo = new sketch[MAX_AMMO];
		

		

	}
	
	public void RemoveCrate() {
		cd.holding=0;
		crate=null;
	}
	
	
	public void BuildCrate(int crate_type) {
		crate= new sketch();
		crate.numpoints=12;
		float radi=5f;  // the container is 13, package is 11
		crate.xmat[0]=-radi;
		crate.ymat[0]=radi;
		crate.xmat[1]=radi;
		crate.ymat[1]=radi;
		
		crate.xmat[2]=radi;
		crate.ymat[2]=radi;
		crate.xmat[3]=radi;
		crate.ymat[3]=-radi;
		
		crate.xmat[4]=radi;
		crate.ymat[4]=-radi;
		crate.xmat[5]=-radi;
		crate.ymat[5]=-radi;
		
		crate.xmat[6]=-radi;
		crate.ymat[6]=-radi;
		crate.xmat[7]=-radi;
		crate.ymat[7]=radi;
		
		crate.xmat[8]=radi;
		crate.ymat[8]=-radi;
		crate.xmat[9]=-radi;
		crate.ymat[9]=radi;
		
		crate.xmat[10]=-radi;
		crate.ymat[10]=-radi;
		crate.xmat[11]=radi;
		crate.ymat[11]=radi;
		
		for (int count=0; count < crate.numpoints/2; count++) {
			switch (crate_type) {
			// 0 = I'm not holding anything
			case 1: 
				crate.rcolor[count]=89f/255f;
				crate.gcolor[count]=89f/255f;
				crate.bcolor[count]=0.0f;
				break;
			case 2:
				crate.rcolor[count]=0.25f;
				crate.gcolor[count]=0.0f;
				crate.bcolor[count]=0.25f;
				break;
			case 3:
				crate.rcolor[count]=0.5f;
				crate.gcolor[count]=0.25f;
				crate.bcolor[count]=0.0f;
				break;
			}
			
		}
		
	}
	
	
	
	component() {
		cd = new ComponentData();
		cd.csketch=new sketch("turret.dat");
		cd.cdsketch=new DamageSketch(cd.csketch);
		cd.muzzlex = new float[MAX_GUNS];
		cd.muzzley = new float[MAX_GUNS];
		throb_time=0.0d;
		cycle=0;
		FillData();
	}
	
	component(String sname) {
		cd = new ComponentData();
		cd.csketch=new sketch(sname);
		cd.cdsketch=new DamageSketch(cd.csketch);
		cd.muzzlex = new float[MAX_GUNS];
		cd.muzzley = new float[MAX_GUNS];
		throb_time=0.0d;
		cycle=0;
		FillData();

	}

}
