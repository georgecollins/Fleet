import org.lwjgl.opengl.GL11;


public class projectile extends WorldThing {

	
	ProjectileData pd;
	double dist;
	public void Draw(double camerax, double cameray, int team ) {
		switch (pd.type) {
			case BULLET:
			{
				GL11.glLineWidth(3.0f);
				GL11.glBegin(GL11.GL_LINES);
				GL11.glColor3f(1.0f, 0.0f, 0.0f);		
				GL11.glVertex3d(xpos, ypos,0f);
				//GL11.glVertex3d(xpos+xvel, ypos+yvel, 0f);
				GL11.glVertex3d(xpos + Math.cos(rotation)*5, ypos + Math.sin(rotation) *5, 0);
				GL11.glEnd();
				break;
			}
			case TORPEDO:
				// pd.psketch.drawrot(xpos, ypos, rotation-Math.PI/2d);
				dist=(xpos-camerax)*(xpos-camerax)+(ypos-cameray)*(ypos-cameray);
				if ((team!=pd.team) && (dist < 20000)) {
					pd.psketch.drawrot(xpos, ypos, rotation-Math.PI/2, 1.0d, 1.0d - (dist/20000));
				} else
					pd.psketch.drawrot(xpos, ypos, rotation-Math.PI/2d, 1.0d, 1.0d-pd.depth);
				break;
			case MINE:
				// pd.psketch.drawrot(xpos, ypos, rotation-Math.PI/2d);
				dist=(xpos-camerax)*(xpos-camerax)+(ypos-cameray)*(ypos-cameray);
				
	
				
				if ((team!=pd.team) && (dist < 20000)) {
					pd.psketch.drawrot(xpos, ypos, rotation-Math.PI/2, 1.0d, 1.0d - (dist/20000));
				} else {
					pd.psketch.drawrot(xpos, ypos, rotation-Math.PI/2d, 1.0d, 1.0d-pd.depth);
					if (team==pd.team) {
						// make a safety mark
						GL11.glLineWidth(2.0f);
						GL11.glBegin(GL11.GL_LINES);
						GL11.glColor3f(0.0f, 1.0f, 0.0f);		
						GL11.glVertex3d(xpos, ypos,0f);
						//GL11.glVertex3d(xpos+xvel, ypos+yvel, 0f);
						GL11.glVertex3d(xpos + Math.cos(rotation)*2, ypos + Math.sin(rotation) *2, 0);
						GL11.glEnd();

					}
				}
				
				break;
			case SPLASH:
			{
				GL11.glLineWidth(2.0f);
				GL11.glBegin(GL11.GL_LINES);
				GL11.glColor3f(0.5f, 0.5f, 0.9f);		
				GL11.glVertex3d(xpos, ypos,0f);
				GL11.glVertex3d(xpos+5, ypos, 0f);
				GL11.glVertex3d(xpos+5, ypos-5, 0f);
				GL11.glVertex3d(xpos+5, ypos, 0f);
				GL11.glEnd();
				break;
			}	
			case EXPLOSION:
			{
				GL11.glLineWidth(3.0f);
				GL11.glBegin(GL11.GL_LINES);
				GL11.glColor3f(1.0f, 1.0f, 0.0f);
				GL11.glVertex3d(xpos+Math.random()*6, ypos+Math.random()*6, 0.0f);
				GL11.glVertex3d(xpos+Math.random()*6, ypos+Math.random()*6, 0.0f);
				GL11.glVertex3d(xpos+Math.random()*6, ypos+Math.random()*6, 0.0f);	
				GL11.glEnd();
				
				if (pd.dsketch!=null)  {
					pd.dsketch.SimulateExplosion(4d, 5.0d-pd.lifetime);
					pd.dsketch.drawrot(xpos, ypos, rotation-Math.PI/2, 1.0d, 0.7d);
				}
					
				break;
			}
		}
	}
	public void SetOwner(int o) {
		pd.owner=o;
	}
	
	public int GetOwner() {
		return pd.owner;
	}
	
	public void SetTimeLeft(double t) {
		pd.lifetime=t;
		
	}
	
	public void Simulate(double t) {
		xpos=xpos+pd.xvel*t;
		ypos=ypos+pd.yvel*t;
		pd.lifetime=pd.lifetime-t;
		// these projectiles sink and become difficult to see--- but still deadly
		if ((pd.type==ProjectileData.pType.TORPEDO) || (pd.type==ProjectileData.pType.MINE)) {
			double max_depth=1.0d;
			if (pd.team==0) {
				max_depth=0.5d;
			}
			if  (pd.depth < max_depth) {
				
				if (pd.type==ProjectileData.pType.TORPEDO) {
					pd.depth=pd.depth+0.1d*t;  // sink
				}
				else 
					pd.depth=pd.depth+0.03d*t;  // the mine sinks more slowly
			}
		}
		// mines slow down quickly
		if (pd.type== ProjectileData.pType.MINE) {
			pd.xvel=pd.xvel-pd.xvel*0.08d*t;
			pd.yvel=pd.yvel-pd.yvel*0.08d*t;
			
			if (Math.abs(pd.xvel) < 0.2d) pd.xvel=0.0d;
			if (Math.abs(pd.yvel) < 0.2d) pd.yvel=0.0d;

		}
		
		if (pd.lifetime < 0.0f) {
			switch (pd.type) {
				case BULLET:
				{
					pd.type=ProjectileData.pType.SPLASH;
					pd.lifetime= 5.0f;
					pd.xvel=0.0f;
					pd.yvel=0.0f;
					break;
				}
				case TORPEDO:
				{
					
					pd.type=ProjectileData.pType.EMPTY;
				}
				case MINE:
				{
					
					pd.type=ProjectileData.pType.EMPTY;
				}
				case SPLASH:
				case EXPLOSION:	
				{

					pd.type=ProjectileData.pType.EMPTY;
				}
				
			}
		}
	}
	
	
	public boolean HandleCollision(int id) {
		pd.type=ProjectileData.pType.EMPTY;
		return true;
	}
	public void Explode() {
		pd.xvel=0;
		pd.yvel=0;
		pd.lifetime=5.0;
		pd.type=ProjectileData.pType.EXPLOSION;
	}

	public sketch MakeTorpedo(int dmg, int teamin) {
		// make this a torpedo with a propeller
		// 24 long 4 wide

		pd.damage=dmg;
		pd.psketch = new sketch();
		pd.team=teamin;
		
		pd.psketch.numpoints=12;
		//  / 
		pd.psketch.xmat[0]=0;
		pd.psketch.ymat[0]=12;
		pd.psketch.xmat[1]=-2;
		pd.psketch.ymat[1]=8;
		// | 
		pd.psketch.xmat[2]=-2;
		pd.psketch.ymat[2]=8;
		pd.psketch.xmat[3]=-2;
		pd.psketch.ymat[3]=-8;
		
		// \
		pd.psketch.xmat[4]=-2;
		pd.psketch.ymat[4]=-8;
		pd.psketch.xmat[5]=0;
		pd.psketch.ymat[5]=-14;
		
		// /
		pd.psketch.xmat[6]=0;
		pd.psketch.ymat[6]=-14;
		pd.psketch.xmat[7]=2;
		pd.psketch.ymat[7]=-8;
		// | 
		pd.psketch.xmat[8]=2;
		pd.psketch.ymat[8]=-8;
		pd.psketch.xmat[9]=2;
		pd.psketch.ymat[9]=8;
		// \
		pd.psketch.xmat[10]=2;
		pd.psketch.ymat[10]=8;
		pd.psketch.xmat[11]=0;
		pd.psketch.ymat[11]=12;
		
		for (int count=0; count < pd.psketch.numpoints/2; count++) {
			pd.psketch.rcolor[count]=0.8f;
			pd.psketch.gcolor[count]=0.5f;
			pd.psketch.bcolor[count]=0.0f;
		}
		
		pd.dsketch= new DamageSketch(pd.psketch);
		return pd.psketch;
	}
	
	public void MakeMine(int dmg, int teamin) {
		pd.damage=dmg;
		pd.psketch = new sketch();
		pd.team=teamin;
		
		pd.psketch.numpoints=16;
		
		pd.psketch.xmat[0]= -2;
		pd.psketch.ymat[0]= 2;
		pd.psketch.xmat[1]= 0;
		pd.psketch.ymat[1]= 10;
		
		pd.psketch.xmat[2]= 0;
		pd.psketch.ymat[2]= 10;
		pd.psketch.xmat[3]= 2;
		pd.psketch.ymat[3]= 2;
		
		pd.psketch.xmat[4]= 2;
		pd.psketch.ymat[4]= 2;
		pd.psketch.xmat[5]= 10;
		pd.psketch.ymat[5]= 0;
		
		pd.psketch.xmat[6]= 10;
		pd.psketch.ymat[6]= 0;
		pd.psketch.xmat[7]= 2;
		pd.psketch.ymat[7]= -2;
		
		pd.psketch.xmat[8]= 2;
		pd.psketch.ymat[8]= -2;
		pd.psketch.xmat[9]= 0;
		pd.psketch.ymat[9]= -10;
		
		pd.psketch.xmat[10]= 0;
		pd.psketch.ymat[10]= -10;
		pd.psketch.xmat[11]= -2;
		pd.psketch.ymat[11]= -2;
		
		pd.psketch.xmat[12]= -2;
		pd.psketch.ymat[12]= -2;
		pd.psketch.xmat[13]= -10;
		pd.psketch.ymat[13]= 0;
		
		pd.psketch.xmat[14]= -10;
		pd.psketch.ymat[14]= 0;
		pd.psketch.xmat[15]= -2;
		pd.psketch.ymat[15]= 2;

		for (int count=0; count < pd.psketch.numpoints/2; count++) {
			pd.psketch.rcolor[count]=0.8f;
			pd.psketch.gcolor[count]=0.5f;
			pd.psketch.bcolor[count]=0.0f;
		}
		
		pd.dsketch= new DamageSketch(pd.psketch);	
		
	
	}
	
	projectile() {
		super();
		pd=new ProjectileData();
		pd.type=ProjectileData.pType.EMPTY;
		pd.damage=1;
		pd.lifetime=0.0f;
		pd.depth=0.0d;
	}
	
	// add an owner
	projectile(float xpin, float ypin, float xvin, float yvin, String pfile) {
		super();
		xpos=xpin;
		ypos=ypin;
		pd.xvel=xvin;
		pd.yvel=yvin;
		pd.lifetime=20.0f;  /// default = 2sec
		pd.damage=1;
		pd.type=ProjectileData.pType.BULLET;
		pd.depth=0.0d;
		if (pfile!="EMPTY") {
			pd.psketch=new sketch(pfile);
		}
		
	}

}
