



public class ProjectileManager extends ObjectManager {
	private static int MAX_PROJECTILES=400;
	projectile[] plist;
	sketch pbox;

	public ProjectileManager top, bottom, left, right;
	
	
	int pcount;
	

	public void InitProjectile(int count, ProjectileData.pType t,double x, double y,double xv, double yv, double rot, double time, int owner, int teamin) {


		plist[count].SetID(md.GetUniqieID(md.PROJECTILE));
		plist[count].pd.type=t;
		plist[count].pd.damage=1;  // default  
		plist[count].pd.dsketch=null;
		plist[count].xpos=x;
		plist[count].ypos=y;
		plist[count].pd.xvel=xv;
		plist[count].pd.yvel=yv;		
		plist[count].rotation=rot;
		plist[count].SetTimeLeft(time);
		plist[count].SetOwner(owner);
		plist[count].pd.team =teamin;

	}

	public void InitProjectile(int count, int id, ProjectileData pdi, double x, double y, double r) {
		plist[count].id=id;
		plist[count].xpos=x;
		plist[count].ypos=y;
		plist[count].rotation=r;
		plist[count].pd=pdi;
	}

	public boolean AddProjectile(double x, double y, double xv, double yv, double rot, int ownerid, int team) {

		// first check to see if I can reuse one
		if (pcount > 0)
			for (int loop=0; loop< pcount; loop++) {
			  if (plist[loop].pd.type==ProjectileData.pType.EMPTY) {
					InitProjectile(loop, ProjectileData.pType.BULLET, x, y, xv, yv, rot, 30f,  ownerid, team);				
					return true;  // i created the projectile
			  }
			}
		
		if (pcount < MAX_PROJECTILES - 1) {
					
			plist[pcount]= new projectile();
			InitProjectile(pcount, ProjectileData.pType.BULLET, x, y, xv, yv, rot, 30f,  ownerid, team);				
			pcount++;
			return true;
		}
		else { 
			//  this is a serious problem
			int xx=0;
			xx=1/xx;
			return false;
		}
	}
	public boolean AddTorpedo(double x, double y, double xv, double yv, double rot, int ownerid, int team) {
		// first check to see if I can reuse one
		if (pcount > 0)
			for (int loop=0; loop< pcount; loop++) {
			  if (plist[loop].pd.type==ProjectileData.pType.EMPTY) {
					plist[loop]= new projectile();
					InitProjectile(loop, ProjectileData.pType.TORPEDO, x, y, xv, yv, rot, 60d,  ownerid, team);				
					plist[loop].MakeTorpedo(25, team);
					return true;  // i created the projectile
			  }
			}
		
		if (pcount < MAX_PROJECTILES - 1) {
			
			
			plist[pcount]= new projectile();
			InitProjectile(pcount, ProjectileData.pType.TORPEDO, x, y, xv, yv, rot, 60d,  ownerid, team);				
			plist[pcount].MakeTorpedo(25, team);
			pcount++;
			return true;
		}
		else { 
			//  this is a serious problem
			int xx=0;
			xx=1/xx;
			return false;
		}	
	}
	
	
	
	public boolean AddMine(double x, double y, double xv, double yv, double rot, int ownerid, int team) {
		// first check to see if I can reuse one
		if (pcount > 0)
			for (int loop=0; loop< pcount; loop++) {
			  if (plist[loop].pd.type==ProjectileData.pType.EMPTY) {
					plist[loop]= new projectile();
					
					InitProjectile(loop, ProjectileData.pType.MINE, x, y, xv, yv, rot, 600d,  ownerid, team);				
					plist[loop].MakeMine(20, team);
					return true;  // i created the projectile
			  }
			}
		
		if (pcount < MAX_PROJECTILES - 1) {
			
			
			plist[pcount]= new projectile();
			InitProjectile(pcount, ProjectileData.pType.MINE, x, y, xv, yv, rot, 600d,  ownerid, team);				
			plist[pcount].MakeMine(20, team);
			pcount++;
			return true;
		}
		else { 
			//  this is a serious problem
			int xx=0;
			xx=1/xx;
			return false;
		}	
	}
	public int AddProjectile(projectile p) {
		// first check to see if I can reuse one
		if (pcount > 0)
			for (int loop=0; loop< pcount; loop++) {
			  if (plist[loop].pd.type==ProjectileData.pType.EMPTY) {
					InitProjectile(loop, p.id, p.pd, p.xpos, p.ypos, p.rotation);
					return p.id;
			  }
			}
		
		if (pcount < MAX_PROJECTILES - 1) {
		
			plist[pcount] = new projectile();
			InitProjectile(pcount, p.id, p.pd, p.xpos, p.ypos, p.rotation);
			pcount++;
			return p.id;	
		}
		
		
		return 0;
		
	}
	public void DeleteProjectile(int id) {
		for (int count=0; count < pcount; count++) {
			if (plist[count].id == id) { 
				plist[count].id=0;
				plist[count]=new projectile();
				plist[count].pd.type=ProjectileData.pType.EMPTY;
			}

		}
	}
	public void HandleIslandCollision(island isl) {
		for (int loop=0; loop< pcount; loop++) {
			if (plist[loop].pd.type==ProjectileData.pType.BULLET) {

				pbox.SetWorldPoints(plist[loop].xpos, plist[loop].ypos, plist[loop].rotation);
				if (pbox.Collide(isl.isketch))
					plist[loop].HandleCollision(isl.id);
			}	
			if (plist[loop].pd.type==ProjectileData.pType.TORPEDO) {
				if (plist[loop].pd.psketch.Collide(isl.isketch))
					plist[loop].HandleCollision(isl.id);
			}
		}
	}	
	
	// don't return id, return the damage
	public projectile Collide(sketch csketch, int id, int team) {
		for (int loop = 0; loop < pcount; loop++) {
			if ((plist[loop].GetOwner()!=id)){
				if (plist[loop].pd.type==ProjectileData.pType.BULLET){
					// test for collision.  
					if (!(plist[loop].ypos < csketch.miny  || plist[loop].ypos > csketch.maxy || plist[loop].xpos  < csketch.minx  || plist[loop].xpos > csketch.maxx) )
					{
						plist[loop].Explode();  // later an explosion  message? 
						//System.out.println("projectile collision ***" );
						return plist[loop];
					}
				}
				if (plist[loop].pd.type==ProjectileData.pType.TORPEDO){
					// test for collision.  
					if (plist[loop].pd.psketch.Collide(csketch))
					{
						plist[loop].Explode();  // later an explosion  message? 
						//System.out.println("projectile collision ***" );
						return plist[loop];
					}
				}		
				
				if (plist[loop].pd.type==ProjectileData.pType.MINE){
					// test for collision.  
					if ((plist[loop].pd.team!=team) && (plist[loop].pd.psketch.Collide(csketch)))
					{
						plist[loop].Explode();  // later an explosion  message? 
						//System.out.println("projectile collision ***" );
						return plist[loop];
					}
				}
			}
		}
		return null;	
	}
	
	public void Simulate(double t) {
		for (int loop = 0; loop < pcount; loop++) {
			plist[loop].Simulate(t);
			// if I am out of bounds, move me

			int pid=plist[loop].id;
			if (plist[loop].xpos> offsetx+md.world_width/2) {
				//copy it over to right
				right.AddProjectile(plist[loop]);
				DeleteProjectile(pid);
				
			}
			if (plist[loop].xpos< offsetx-md.world_width/2) {

				left.AddProjectile(plist[loop]);
				DeleteProjectile(pid);
				
			}	
			if (plist[loop].ypos> offsety+md.world_height/2) {

				top.AddProjectile(plist[loop]);
				DeleteProjectile(pid);
				
			}
			if (plist[loop].ypos< offsety-md.world_height/2) {

				bottom.AddProjectile(plist[loop]);
			//	bottom.AddProjectile(plist[loop].xpos, plist[loop].ypos, plist[loop].xvel, plist[loop].yvel, plist[loop].rotation, plist[loop].owner);
			
				DeleteProjectile(pid);
			}

		}
		
	}
	
	public void Draw(double camerax, double cameray, int team) {
		for (int loop = 0; loop < pcount; loop++) { 

			if (plist[loop].pd.type != ProjectileData.pType.EMPTY) {
				plist[loop].Draw(camerax, cameray, team);
			}

		}
	
	}
	
	ProjectileManager(MissionData mdin) {
		
		pcount = 0;
		plist=new projectile[MAX_PROJECTILES];
		pbox = new sketch();
		pbox.numpoints=8;
		pbox.xmat[0]=-5;
		pbox.ymat[0]=-5;
		pbox.xmat[1]=5;
		pbox.ymat[1]=5;
		pbox.xmat[2]=-5;
		pbox.ymat[2]=5;
		pbox.xmat[3]=5;
		pbox.ymat[3]=-5;
		pbox.xmat[4]=5;
		pbox.ymat[4]=5;
		pbox.xmat[5]=5;
		pbox.ymat[5]=-5;
		pbox.xmat[6]=-5;
		pbox.ymat[6]=5;
		pbox.xmat[7]=-5;
		pbox.ymat[7]=-5;
		
		
		
		md = mdin;
		
	}

}
