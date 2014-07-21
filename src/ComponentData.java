
		// the part of the component that is the permanent definition



public class ComponentData {
	final float CANNON_RED = 0.2f;
	final float CANNON_GREEN = 0.65f;
	final float CANNON_BLUE = 0.65f;

	static final int DUMBFIRE_AIM = 0;
	static final int WIREGUIDE_AIM = 1;
	static final int TARGET_AIM = 2;
	
	public enum ctype {GUN, FIXEDGUN, TORPEDO, MINE, SONAR, RADAR, REPAIR, ENGINE, NET, MISSILE, CARGO, EMPTY};
	ctype type;
	int guncount; // for guns(1-3 )
	int fire_rate;  // slow medium fast (0-2)
	int shot_speed; // slow medium fast (0-2)
	int clip_size;  // 1 - 8
	int shot_damage;  // 1-2-3
	
	int holding; // for cargo  
	int aim;  // does the compoent have an aiming mechanism
	
	float muzzlex[]; 
	float muzzley[]; 

	// timing hack
	double speed_factor = 0.8d;
	
	sketch csketch;
	DamageSketch cdsketch;
	sketch psketch;  // projectile sketch for torpedoes
	
	public void BuildCSketch() {
		// given the data, build the sketch 
		switch (type) {
		case TORPEDO:
			BuildTorpedoTubeSketch();
			BuildTorpedoSketch();
			break;
		case FIXEDGUN:
			BuildFixedGunSketch();
			break;
		case GUN:
			BuildGunSketch();
			break;
		case MINE:
			BuildMineSketch();
			break;
		case CARGO:
			BuildContainerSketch();
			break;
		case ENGINE:
			BuildEngineSketch(0);
			break;
		case REPAIR:
			BuildRepairSketch();
			break;
			
		}
	}
	public int GetWeight() {
		int wgt;
		wgt=20;  // default
		switch (type) {
		case GUN:
			wgt=(20+3*shot_speed+3*fire_rate)*guncount;// 20+ 0-12 * # of guns
			wgt=wgt+(shot_speed*fire_rate*3)*guncount; // +3, +6 + 12 per gun if you have both
			wgt=wgt + clip_size*2*shot_damage;
			break;
		
		case FIXEDGUN:	
			wgt=(12+3*shot_speed+3*fire_rate)*guncount;
			wgt=wgt+(shot_speed*fire_rate*3)*guncount; // +3, +6 + 12 per gun if you have both
			wgt=wgt + clip_size*2*shot_damage;
			break;
		case TORPEDO:
			wgt=50+ shot_speed*50;  
			break;
		case MINE:
			wgt = 100;
			break;
		case ENGINE:
			wgt = 100;
			break;
		case REPAIR:
			wgt = 25 + guncount*25+ fire_rate*25;
			break;
		}
		return wgt;
	}
	
	public int GetCost() {
		int cost= 0;
		switch (type) {
		case GUN:
			cost=20* guncount+10*shot_speed+10*fire_rate+5* clip_size;
			break;
		case FIXEDGUN:
			cost=10*guncount+5*shot_speed+5*fire_rate+3*clip_size;
			break;
		case TORPEDO:
			cost = 75 + shot_speed*25;
			break;
		case MINE:
			cost = 150;
			break;
		case CARGO:
			cost = 20;
			break;
		case ENGINE:
			cost = 200;
			break;
		case REPAIR:
			cost = 100 + guncount*guncount *50+ fire_rate*fire_rate*25;
			break;
		}
		
		return cost;
	}
	
	public double GetProjectileSpeed() {
		switch (type) {
		case GUN:
			return (30d+shot_speed*15)/speed_factor;

		case FIXEDGUN:
			return (35d+shot_speed*15)/speed_factor;
		
		case TORPEDO:
			return 25d+shot_speed*5;
		case MINE:

			break;
		case CARGO:

			break;
		case REPAIR:
			break;
		}	
		return 0d;
	}
	
	public double GetRefreshTime() {
	
		switch (type) {
		case GUN:
			return (8- fire_rate*2)*speed_factor;
		case FIXEDGUN:
			return (7- fire_rate*2)*speed_factor;	
		case TORPEDO:
			return 80d;
		case MINE:
			return 60d;
		case CARGO:
			break;
		}	
		
		return 0d;
	}
	
	public double GetReloadTime() {
		switch (type) {
		case GUN:
			return 20d/speed_factor;

		case FIXEDGUN:
			return 18d/speed_factor;
		
		case TORPEDO:
			return 80d/speed_factor;
		case MINE:
			return 60d/speed_factor;
		case CARGO:

			break;
		}	
		return 0d;
	}
	
	public void BuildTorpedoTubeSketch() {
		float radi=2f+(float) (Math.sqrt((double)GetWeight()/2d));  
		float xradi=radi;
		float yoffset=5f;		
		xradi=xradi/2f;  // if its torpedo then it's narrow

		csketch.numpoints=6;	
		// and forward

		csketch.xmat[0]=-xradi;
		csketch.ymat[0]=-radi+yoffset;
		csketch.xmat[1]=xradi;
		csketch.ymat[1]=-radi+yoffset;

		csketch.xmat[2]=xradi;
		csketch.ymat[2]=-radi+yoffset;
		csketch.xmat[3]=xradi;
		csketch.ymat[3]=radi+yoffset;
/*
		csketch.xmat[4]=xradi;
		csketch.ymat[4]=radi+yoffset;		
		csketch.xmat[5]=-xradi;
		csketch.ymat[5]=radi+yoffset;
*/
		csketch.xmat[4]=-xradi;
		csketch.ymat[4]=radi+yoffset;
		csketch.xmat[5]=-xradi;
		csketch.ymat[5]=-radi+yoffset;
		
		muzzlex[0]=0f;
		muzzley[0]=0f;
		
		for (int count=0; count < csketch.numpoints/2; count++) {		
			// set colors
			csketch.rcolor[count]=CANNON_RED;
			csketch.gcolor[count]=CANNON_GREEN;
			csketch.bcolor[count]=CANNON_BLUE;
		}
		
		cdsketch=new DamageSketch(csketch);		
	}
	
	public void BuildTorpedoSketch() {
		projectile torp = new projectile();
		psketch = torp.MakeTorpedo(20, 0);
	}
	
	public void BuildMineSketch() {
		csketch.numpoints=16;
		float xradi=8f;  // the container is 13, package is 11
		float yradi=6f;
		csketch.xmat[0]=-xradi;
		csketch.ymat[0]=yradi;
		csketch.xmat[1]=xradi;
		csketch.ymat[1]=yradi;
		
		csketch.xmat[2]=xradi;
		csketch.ymat[2]=yradi;
		csketch.xmat[3]=xradi;
		csketch.ymat[3]=-yradi;
		
		csketch.xmat[4]=xradi;
		csketch.ymat[4]=-yradi;
		csketch.xmat[5]=-xradi;
		csketch.ymat[5]=-yradi;
		
		csketch.xmat[6]=-xradi;
		csketch.ymat[6]=-yradi;
		csketch.xmat[7]=-xradi;
		csketch.ymat[7]=yradi;
		
		// now the diagonals
		csketch.xmat[8]=-xradi/4f;
		csketch.ymat[8]=0;
		csketch.xmat[9]=-xradi/4*2;
		csketch.ymat[9]=yradi-3;
		
		csketch.xmat[10]=-xradi/4f;
		csketch.ymat[10]=0;
		csketch.xmat[11]=-xradi/4*2;
		csketch.ymat[11]=-yradi+3;	
		
		csketch.xmat[12]=xradi/4f;
		csketch.ymat[12]=0;
		csketch.xmat[13]=xradi/4*2;
		csketch.ymat[13]=yradi-3;
		
		csketch.xmat[14]=xradi/4f;
		csketch.ymat[14]=0;
		csketch.xmat[15]=xradi/4*2;
		csketch.ymat[15]=-yradi+3;			
		for (int count=0; count < csketch.numpoints/2; count++) {
			csketch.rcolor[count]=CANNON_RED;
			csketch.gcolor[count]=CANNON_GREEN;
			csketch.bcolor[count]=CANNON_BLUE;
		}
	}
	
	public void BuildFixedGunSketch() {
		float radi=2f+(float) (Math.sqrt((double)GetWeight()/2d));  
		csketch.numpoints=8;	
		float xradi=radi;
		float yoffset=0f;	
		
		csketch.xmat[0]=-xradi;
		csketch.ymat[0]=-radi+yoffset;
		csketch.xmat[1]=xradi;
		csketch.ymat[1]=-radi+yoffset;
		
		csketch.xmat[2]=xradi;
		csketch.ymat[2]=-radi+yoffset;
		csketch.xmat[3]=xradi;
		csketch.ymat[3]=radi+yoffset;

		csketch.xmat[4]=xradi;
		csketch.ymat[4]=radi+yoffset;		
		csketch.xmat[5]=-xradi;
		csketch.ymat[5]=radi+yoffset;

		csketch.xmat[6]=-xradi;
		csketch.ymat[6]=radi+yoffset;
		csketch.xmat[7]=-xradi;
		csketch.ymat[7]=-radi+yoffset;
		
		for (int count=0; count < csketch.numpoints/2; count++) {		
			// set colors
			csketch.rcolor[count]=CANNON_RED;
			csketch.gcolor[count]=CANNON_GREEN;
			csketch.bcolor[count]=CANNON_BLUE;
		}
		int oldpoints=csketch.numpoints;
		float gunlength = radi*0.4f*(shot_speed+2);
		
		csketch.numpoints+=2*guncount;
		
		for (int count=0; count< guncount; count++) {
		// add color
			csketch.rcolor[oldpoints/2 + count]=CANNON_RED;  //0
			csketch.gcolor[oldpoints/2 + count]=CANNON_BLUE;  //0.75
			csketch.bcolor[oldpoints/2 + count]=CANNON_GREEN; // 0.75
			csketch.width[oldpoints/2+count]=(float) (1+shot_damage);  // widen the guns

			
			// draw gun
			if (guncount==1) {
				csketch.xmat[oldpoints + count*2]=0f;
				csketch.ymat[oldpoints + count*2]=radi;
				csketch.xmat[oldpoints+1+count*2]=0f;
				csketch.ymat[oldpoints+1+count*2]=radi+gunlength;
				muzzlex[count]=0f;
				muzzley[count]=radi+gunlength;
			} else
				if (guncount==2)
				{
					csketch.xmat[oldpoints + count*2]=-(radi/3f)+count*radi*2/3f;
					csketch.ymat[oldpoints + count*2]=radi;
					csketch.xmat[oldpoints+1 + count*2]=-(radi/3f)+count*radi*2/3f;
					csketch.ymat[oldpoints+1 + count*2]=radi+gunlength;	
					muzzlex[count]=-(radi/3f)+count*radi*2/3f;
					muzzley[count]=radi+gunlength;
				}
				else
				{
					csketch.xmat[oldpoints + count*2]=-(radi/2f)+count*radi/2f;
					csketch.ymat[oldpoints + count*2]= radi;
					csketch.xmat[oldpoints+1 + count*2]=-(radi/2f)+count*radi/2f;
					csketch.ymat[oldpoints+1 + count*2]= radi+gunlength;	
			
					muzzlex[count]=-(radi/2f)+count*radi/2f;
					muzzley[count]=radi+gunlength;
					
				}
		}
		
		cdsketch=new DamageSketch(csketch);
		

	}
	public void BuildGunSketch() {
		csketch.numpoints=16;	
		float radi=2f+(float) (Math.sqrt((double)GetWeight()/2d));  

		for (int count=0; count<16; count+=2) {
			int npoint=count/2;
			double dpoint=((double)npoint) -0.5d;  // so I get a flat top face
			csketch.xmat[count]=(float) Math.cos( dpoint/4d*Math.PI )*radi;
			csketch.ymat[count]=(float) Math.sin( dpoint/4d*Math.PI)*radi;
			csketch.xmat[count+1]=(float) Math.cos( (dpoint+1d)/4d*Math.PI)*radi;
			csketch.ymat[count+1]=(float) Math.sin( (dpoint+1d)/4d*Math.PI)*radi;
		
		}
		
		for (int count=0; count < csketch.numpoints/2; count++) {		
			// set colors
			csketch.rcolor[count]=CANNON_RED;
			csketch.gcolor[count]=CANNON_BLUE;
			csketch.bcolor[count]=CANNON_GREEN;//
		}
		
		

		int oldpoints=csketch.numpoints;
		float gunlength = radi*0.4f*(shot_speed+2);
		
		csketch.numpoints+=2*guncount;
		
		for (int count=0; count< guncount; count++) {
		// add color
			csketch.rcolor[oldpoints/2 + count]=CANNON_RED;  //0
			csketch.gcolor[oldpoints/2 + count]=CANNON_BLUE;  //0.75
			csketch.bcolor[oldpoints/2 + count]=CANNON_GREEN; // 0.75
			csketch.width[oldpoints/2+count]=(float) (1+shot_damage);  // widen the guns
		
		// draw gun
			if (guncount==1) {
				csketch.xmat[oldpoints + count*2]=0f;
				csketch.ymat[oldpoints + count*2]=radi;
				csketch.xmat[oldpoints+1+count*2]=0f;
				csketch.ymat[oldpoints+1+count*2]=radi+gunlength;
				muzzlex[count]=0f;
				muzzley[count]=radi+gunlength;
			} else
				if (guncount==2)
				{
					csketch.xmat[oldpoints + count*2]=-(radi/4f)+count*radi/2f;
					csketch.ymat[oldpoints + count*2]=radi;
					csketch.xmat[oldpoints+1 + count*2]=-(radi/4f)+count*radi/2f;
					csketch.ymat[oldpoints+1 + count*2]=radi+gunlength;	
					muzzlex[count]=-(radi/4f)+count*radi/2f;
					muzzley[count]=radi+gunlength;
				}
				else
				{
					csketch.xmat[oldpoints + count*2]=-(radi/3f)+count*radi/3f;
					csketch.ymat[oldpoints + count*2]=radi;
					csketch.xmat[oldpoints+1 + count*2]=-(radi/3f)+count*radi/3f;
					csketch.ymat[oldpoints+1 + count*2]=radi+gunlength;	
					muzzlex[count]=-(radi/3f)+count*radi/3f;
					muzzley[count]=radi+gunlength;
					
				}
			

			
		}

		cdsketch=new DamageSketch(csketch);
	
	}
	public void BuildContainerSketch() {
		csketch.numpoints=8;
		float radi=7f;  // the container is 13, package is 11
		csketch.xmat[0]=-radi;
		csketch.ymat[0]=radi;
		csketch.xmat[1]=radi;
		csketch.ymat[1]=radi;
		
		csketch.xmat[2]=radi;
		csketch.ymat[2]=radi;
		csketch.xmat[3]=radi;
		csketch.ymat[3]=-radi;
		
		csketch.xmat[4]=radi;
		csketch.ymat[4]=-radi;
		csketch.xmat[5]=-radi;
		csketch.ymat[5]=-radi;
		
		csketch.xmat[6]=-radi;
		csketch.ymat[6]=-radi;
		csketch.xmat[7]=-radi;
		csketch.ymat[7]=radi;
		
		for (int count=0; count < csketch.numpoints/2; count++) {
			csketch.rcolor[count]=0.25f;
			csketch.gcolor[count]=0.25f;
			csketch.bcolor[count]=0.35f;
			csketch.width[count]=1.0f;
		}
		
	}
	
	
	public void BuildEngineSketch(int n) {
		csketch.numpoints=20;

		float radi=8;
		for (int count=0; count<16; count+=2) {
			int npoint=count/2;
			double dpoint=((double)npoint) -0.5d;  // so I get a flat top face
			csketch.xmat[count]=(float) Math.cos( dpoint/4d*Math.PI )*radi;
			csketch.ymat[count]=(float) Math.sin( dpoint/4d*Math.PI)*radi;
			csketch.xmat[count+1]=(float) Math.cos( (dpoint+1d)/4d*Math.PI)*radi;
			csketch.ymat[count+1]=(float) Math.sin( (dpoint+1d)/4d*Math.PI)*radi;
		}
		
		csketch.xmat[16]=(float) Math.cos( n/4d*Math.PI )*radi;
		csketch.ymat[16]=(float) Math.sin( n/4d*Math.PI)*radi;
		csketch.xmat[17]=(float) Math.cos( (5d+n)/4d*Math.PI)*radi;
		csketch.ymat[17]=(float) Math.sin( (5d+n)/4d*Math.PI)*radi;

		csketch.xmat[18]=(float) Math.cos( (1+n)/4d*Math.PI )*radi;
		csketch.ymat[18]=(float) Math.sin( (1+n)/4d*Math.PI)*radi;
		csketch.xmat[19]=(float) Math.cos( (4d+n)/4d*Math.PI)*radi;
		csketch.ymat[19]=(float) Math.sin( (4d+n)/4d*Math.PI)*radi;
	
		// brown color
		/*
		124
		103
		63
		*/
		
		
		for (int count=0; count < csketch.numpoints/2; count++) {
			csketch.rcolor[count]=124f/255f;
			csketch.gcolor[count]=103f/255f;
			csketch.bcolor[count]=63f/255f;
			csketch.width[count]=1.0f;
		}
	
		
	}
	
	void BuildRepairSketch() {
		// wrench?
		/*
		csketch.numpoints = 16;
		
		csketch.xmat[0] = -5;
		csketch.ymat[0] = 1;
		csketch.xmat[1] = 5;
		csketch.ymat[1] = 1;
		
		csketch.xmat[2] = -5;
		csketch.ymat[2] = -1;
		csketch.xmat[3] = 5;
		csketch.ymat[3] = -1; 	
		
		csketch.xmat[4] = -5;
		csketch.ymat[4] = -3;
		csketch.xmat[5] = -5;
		csketch.ymat[5] = 3;
	
		csketch.xmat[6] = 5;
		csketch.ymat[6] = -3;
		csketch.xmat[7] = 5;
		csketch.ymat[7] = 3;	
		
		csketch.xmat[8]= -5;
		csketch.ymat[8]= -3;
		csketch.xmat[9]= -8;
		csketch.ymat[9]= -3;
		
		csketch.xmat[10]= -5;
		csketch.ymat[10]= 3;
		csketch.xmat[11]= -8;
		csketch.ymat[11]= 3;	
		
		csketch.xmat[12]= 5;
		csketch.ymat[12]= -3;
		csketch.xmat[13]= 8;
		csketch.ymat[13]= -3;	
		
		csketch.xmat[14]= 5;
		csketch.ymat[14]= 3;
		csketch.xmat[15]= 8;
		csketch.ymat[15]= 3;	
		
		*/
		
		// the base will be a box > based on the number of health added
		// the top is a crane, longer based on health
		
		// box
		
		csketch.numpoints = 20;
		
		float box_width = 4 + guncount*2;
		csketch.xmat[0] = -1;
		csketch.ymat[0] = 0;
		csketch.xmat[1] = -box_width/2f;
		csketch.ymat[1] = 0;
		
		csketch.xmat[2] = -box_width/2f;
		csketch.ymat[2] = 0;
		csketch.xmat[3] = -box_width/2f;
		csketch.ymat[3] = -box_width;
		
		csketch.xmat[4] = -box_width/2f;
		csketch.ymat[4] = -box_width;
		csketch.xmat[5] = box_width/2f;
		csketch.ymat[5] = -box_width;
		
		csketch.xmat[6] = box_width/2f;
		csketch.ymat[6] = -box_width;
		csketch.xmat[7] = box_width/2f;
		csketch.ymat[7] = 0;
	
		csketch.xmat[8] = box_width/2f;
		csketch.ymat[8] = 0;
		csketch.xmat[9] = 1;
		csketch.ymat[9] = 0;
		
		// arm
		float arm_length=5+ fire_rate;
		
		csketch.xmat[10]=-1;
		csketch.ymat[10]= 0;
		csketch.xmat[11]=-1;
		csketch.ymat[11]= arm_length;
		
		csketch.xmat[12]= 1;
		csketch.ymat[12]= 0;
		csketch.xmat[13]= 1;
		csketch.ymat[13]= arm_length;
		
		csketch.xmat[14]=-2;
		csketch.ymat[14]=arm_length;
		csketch.xmat[15]=2;
		csketch.ymat[15]=arm_length;
		
		csketch.xmat[16]=-2;
		csketch.ymat[16]=arm_length;
		csketch.xmat[17]=-2;
		csketch.ymat[17]=arm_length+3;
	
		csketch.xmat[18]=2;
		csketch.ymat[18]=arm_length;
		csketch.xmat[19]=2;
		csketch.ymat[19]=arm_length+3;

	
		for (int count=0; count < csketch.numpoints/2; count++) {
			csketch.rcolor[count]=124f/255f;
			csketch.gcolor[count]=103f/255f;
			csketch.bcolor[count]=63f/255f;
			csketch.width[count]=1.0f;
		}
	
	}

	void BuildRandomComponent() {
		int num=(int) (Math.random()*7d); // 0 to 6 only
		
		switch (num) {
			case 0: 
				type=ctype.GUN;
				guncount=(int) (Math.random()*3d)+1; // for guns(1-3 )
				fire_rate = (int) (Math.random()*3d);  // slow medium fast (0-2)
				shot_speed =(int) (Math.random()*3d); // slow medium fast (0-2)
				clip_size = (int) (Math.random()*5d)+1;  // 1 - 8
				shot_damage = 1;  // 1-2-3
				aim = DUMBFIRE_AIM;

				break;
			case 1:
				type=ctype.FIXEDGUN;
				guncount=(int) (Math.random()*3d)+1; // for guns(1-3 )
				fire_rate = (int) (Math.random()*3d);  // slow medium fast (0-2)
				shot_speed =(int) (Math.random()*3d); // slow medium fast (0-2)
				clip_size = (int) (Math.random()*5d)+1;  // 1 - 8
				shot_damage = 1;  // 1-2-3
				aim = DUMBFIRE_AIM;
				break;
			case 2:
				type=ctype.TORPEDO;
				guncount=(int) (Math.random()*3d)+1; // for guns(1-3 )
				fire_rate = (int) (Math.random()*3d);  // slow medium fast (0-2)
				shot_speed =(int) (Math.random()*3d); // slow medium fast (0-2)
				clip_size = (int) 1;  // 1 - 8
				shot_damage = 1;  // 1-2-3
				aim = DUMBFIRE_AIM;
			  break;
			  
			case 3:
				type=ctype.MINE;
				guncount=1;
				fire_rate = (int) (Math.random()*3d);  // slow medium fast (0-2)
				shot_speed =(int) (Math.random()*3d); // slow medium fast (0-2)
				clip_size = (int) (Math.random()*5d)+1;  // 1 - 8
				shot_damage = 1;  // 1-2-3
				aim = DUMBFIRE_AIM;
				break;		  
			case 4:
				type=ctype.CARGO;
				break;
			case 5:
				type=ctype.ENGINE;
				break;
			case 6:
				type = ctype.REPAIR;
				guncount=(int) (Math.random()*3d)+1; // for guns(1-3 )
				fire_rate = (int) (Math.random()*3d);  // slow medium fast (0-2)
				shot_speed =(int) (Math.random()*3d); // slow medium fast (0-2)
				clip_size = (int) 1;  // 1 - 8
				shot_damage = 1;  // 1-2-3				
				break;
		}
		
		BuildCSketch();
	}
	
	void BuildMineComponent() {
		type=ctype.MINE;
		guncount=1;
		fire_rate = (int) (Math.random()*3d);  // slow medium fast (0-2)
		shot_speed =(int) (Math.random()*3d); // slow medium fast (0-2)
		clip_size = (int) (Math.random()*5d)+1;  // 1 - 8
		shot_damage = 1;  // 1-2-3
		aim = DUMBFIRE_AIM;
		BuildCSketch();
	}
	
	void BuildWeaponComponent() {
		int num=(int)(Math.random()*6d); // 0 to 2 only
		
		switch (num) {
			case 0: 
				type=ctype.GUN;
				guncount=(int) (Math.random()*3d)+1; // for guns(1-3 )
				fire_rate = (int) (Math.random()*3d);  // slow medium fast (0-2)
				shot_speed =(int) (Math.random()*3d); // slow medium fast (0-2)
				clip_size = (int) (Math.random()*5d)+1;  // 1 - 8
				shot_damage = 1;  // 1-2-3
				aim = DUMBFIRE_AIM;

				break;
			case 1:
				type=ctype.FIXEDGUN;
				guncount=(int) (Math.random()*3d)+1; // for guns(1-3 )
				fire_rate = (int) (Math.random()*3d);  // slow medium fast (0-2)
				shot_speed =(int) (Math.random()*3d); // slow medium fast (0-2)
				clip_size = (int) (Math.random()*5d)+1;  // 1 - 8
				shot_damage = 1;  // 1-2-3
				aim = DUMBFIRE_AIM;
				break;
			case 2:
				type=ctype.TORPEDO;
				guncount=(int) (Math.random()*3d)+1; // for guns(1-3 )
				fire_rate = (int) (Math.random()*3d);  // slow medium fast (0-2)
				shot_speed =(int) (Math.random()*3d); // slow medium fast (0-2)
				clip_size = (int) 1;  // 1 - 8
				shot_damage = 1;  // 1-2-3
				aim = DUMBFIRE_AIM;
			  break;
			  
			case 3:
				type=ctype.MINE;
				guncount=1;
				fire_rate = (int) (Math.random()*3d);  // slow medium fast (0-2)
				shot_speed =(int) (Math.random()*3d); // slow medium fast (0-2)
				clip_size = (int) (Math.random()*5d)+1;  // 1 - 8
				shot_damage = 1;  // 1-2-3
				aim = DUMBFIRE_AIM;
				break;
			case 4:
				type= ctype.ENGINE;
				break;
				
			case 5: 
				type = ctype.REPAIR;
				guncount=(int) (Math.random()*3d)+1; // for guns(1-3 )
				fire_rate = (int) (Math.random()*3d);  // slow medium fast (0-2)
				shot_speed =(int) (Math.random()*3d); // slow medium fast (0-2)
				clip_size = (int) 1;  // 1 - 8
				shot_damage = 1;  // 1-2-3
				
				break;
			case 6:	
				type=ctype.CARGO;
				break;

		}
		
		BuildCSketch();
	}
	
	public void drawrot(WorldThing wt, double scale) {
		drawrot(wt.xpos, wt.ypos, wt.rotation, scale);
	}

	public void drawrot(double x, double y, double rotation, double scale) {
		csketch.drawrot(x, y, rotation, scale);
		if (type == ctype.TORPEDO) {
			psketch.drawrot(x, y, rotation, scale);
		}

	}

	void BuildCargoComponent() {
		type=ctype.CARGO;	
		BuildCSketch();
	}
	// this is not perfect equivelence, could cause bgs
	public boolean Equals(ComponentData cd) {
		if ((cd.type==type) &&
		(cd.guncount==guncount) &&  // for guns(1-3 )
		(cd.fire_rate==fire_rate) &&  // slow medium fast (0-2)
		 (cd.shot_speed== shot_speed) && // slow medium fast (0-2)
		 (cd.clip_size== clip_size) && // 1 - 8
		  (cd.shot_damage==shot_damage) )
		  return true;
		return false;
	}
	
	public void CopyTo(ComponentData cd) {
		
		cd.type=type;
		cd.guncount=guncount; // for guns(1-3 )
		cd.fire_rate=fire_rate;  // slow medium fast (0-2)
		cd.shot_speed= shot_speed; // slow medium fast (0-2)
		cd.clip_size=clip_size;  // 1 - 8
		cd.shot_damage=shot_damage;  // 1-2-3
		
		cd.holding=holding; // for cargo  
		cd.aim = aim;
		
		
		cd.muzzlex=new float[component.MAX_GUNS];
		cd.muzzley=new float[component.MAX_GUNS];
		for (int i=0; i< component.MAX_GUNS; i++) {
			cd.muzzlex[i]=muzzlex[i];
			cd.muzzley[i]=muzzley[i];
		}
		cd.csketch= new sketch();
		csketch.CopyTo(cd.csketch);
		cd.cdsketch= new DamageSketch(csketch);
		cd.cdsketch.CopyTo(csketch);
		psketch.CopyTo(cd.psketch);
	}
	
	ComponentData() {
		muzzlex=new float[component.MAX_GUNS];
		muzzley=new float[component.MAX_GUNS];
		csketch=new sketch();
		cdsketch=new DamageSketch(csketch);
		psketch = new sketch();
		type=ctype.EMPTY;
	}
}
