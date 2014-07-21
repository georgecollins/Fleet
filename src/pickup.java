

public class pickup extends WorldThing {
	enum pktype { EMPTY, REPAIR, MONEY, TECH, ORE, BANNAS, OIL, FISH }
	pktype type;
	
	double xvel, yvel, xacc, yacc, rvel;
	double cycle_time; // for animations
	
	sketch psketch;
	sketch crate[];
	sketch fish;
	
	void SetPosition(WorldThing wt) {
		xpos=wt.xpos;
		ypos=wt.ypos;
		rotation=wt.rotation;
	}
	
	public boolean Collide(sketch skt) {
		return psketch.Collide(skt);
	}
	
	void Draw() {
		psketch.drawrot( xpos,  ypos,  rotation);
	}
	
	void Draw(double s) {
		psketch.drawrot(xpos, ypos, rotation, s);
	}
	
	
	// dumb function to turn enumeration into an integer.  
	public int GetPickupType() {
		switch (type) {
		case REPAIR: 
			return 0;

		case BANNAS:
			return 1;

		case ORE:
			return 2;
		case TECH: 
			return 3;
		case FISH:
			return 4;
		}	
		return 5; // will crash some arrays
	}
	
	void SetPickupType(int pkin) {
		switch (pkin) {
		case 0: 
			type=pickup.pktype.REPAIR;
			break;
		case 1:
			type=pickup.pktype.BANNAS;
			psketch=crate[0];
			break;
		case 2:
			type=pickup.pktype.ORE;
			psketch=crate[1];
			break;
		case 3: 
			type=pickup.pktype.TECH;		
			psketch=crate[2];
			break;
		case 4:	
			type=pickup.pktype.FISH;
			psketch=fish;
			break;
		}
	}
	
	void Update(IslandManager im, double t) {
		xacc=xacc+im.GetCurrentX(xpos, ypos)/10d*t;
		yacc=yacc+im.GetCurrentY(xpos, ypos)/10d*t;
		xvel+=xacc * t;
		if (xvel>2d) xvel=2d;
		if (xvel<-2d) xvel=-2d;
		yvel+=yacc * t;
		if (yvel>2d) yvel=2d;
		if (yvel<-2d) yvel=-2d;

		xpos+=xvel*t;
		ypos+=yvel*t;
		
		if (type == pickup.pktype.FISH) {
			cycle_time+=t;
			psketch.ymat[13] = (float) Math.sin(cycle_time*2f)*6f; // flap tail
			rotation= Math.atan2(xvel, yvel);
			if (xvel*yvel < 0)
				rotation= rotation + Math.PI;
		}
		else
			rotation=rotation+rvel*t/5d;
	}
	
	pickup() {
		
		cycle_time = 0f;
		psketch=new sketch();
		psketch.numpoints=8;
		float box_scale = 14;
		psketch.xmat[0]= (float)(box_scale/2);
		psketch.ymat[0]= (float)-(box_scale/2);
		psketch.xmat[1]= (float)(box_scale/2);
		psketch.ymat[1]= (float)(box_scale/2);
		
		psketch.xmat[2]= (float)(box_scale/2);
		psketch.ymat[2]= (float)(box_scale/2);
		psketch.xmat[3]= (float) -(box_scale/2);
		psketch.ymat[3]= (float) (box_scale/2);
		
		psketch.xmat[4] = (float) -(box_scale/2);
		psketch.ymat[4] = (float) (box_scale/2);
		psketch.xmat[5] = (float) -(box_scale/2);
		psketch.ymat[5] = (float) -(box_scale/2);
		
		psketch.xmat[6] = (float) -(box_scale/2);
		psketch.ymat[6] = (float) -(box_scale/2);
		psketch.xmat[7] = (float) (box_scale/2);
		psketch.ymat[7] = (float) -(box_scale/2);
		
		for (int count=0; count < psketch.numpoints/2; count++) {
			// white now but I think it shoudl be orange or purple.  
			psketch.rcolor[count] = 0.5f;
			psketch.gcolor[count] = 0.75f;
			psketch.bcolor[count] = 0.5f;
		}
		
		// add symbol
		psketch.numpoints= psketch.numpoints+4;
		psketch.xmat[8] = +0.0f;
		psketch.ymat[8] = (float) box_scale/2-3f;
		psketch.xmat[9] = +0.0f;
		psketch.ymat[9] = (float) -box_scale/2+ 3f;
		
		psketch.xmat[10] = (float) box_scale/2 - 3f;
		psketch.ymat[10] = +0.0f;
		psketch.xmat[11] = (float) -box_scale/2+3f;;
		psketch.ymat[11] = +0.0f;
		
		psketch.rcolor[4]=0.8f;
		psketch.gcolor[4]=0.0f;
		psketch.bcolor[4]=0.0f;
		psketch.rcolor[5]=0.8f;
		psketch.gcolor[5]=0.0f;
		psketch.bcolor[5]=0.0f;

	
		crate=new sketch[3];
		for (int i=0; i < 3; i++) {
			crate[i]= new sketch();
			crate[i].numpoints=12;
			float radi=5f;  // the container is 13, package is 11
			crate[i].xmat[0]=-radi;
			crate[i].ymat[0]=radi;
			crate[i].xmat[1]=radi;
			crate[i].ymat[1]=radi;
			
			crate[i].xmat[2]=radi;
			crate[i].ymat[2]=radi;
			crate[i].xmat[3]=radi;
			crate[i].ymat[3]=-radi;
			
			crate[i].xmat[4]=radi;
			crate[i].ymat[4]=-radi;
			crate[i].xmat[5]=-radi;
			crate[i].ymat[5]=-radi;
			
			crate[i].xmat[6]=-radi;
			crate[i].ymat[6]=-radi;
			crate[i].xmat[7]=-radi;
			crate[i].ymat[7]=radi;
			
			crate[i].xmat[8]=radi;
			crate[i].ymat[8]=-radi;
			crate[i].xmat[9]=-radi;
			crate[i].ymat[9]=radi;
			
			crate[i].xmat[10]=-radi;
			crate[i].ymat[10]=-radi;
			crate[i].xmat[11]=radi;
			crate[i].ymat[11]=radi;
			
			for (int count=0; count < crate[i].numpoints/2; count++) {
				switch (i) {
				// 0 = I'm not holding anything
				case 0: 
					crate[i].rcolor[count]=89f/255f;
					crate[i].gcolor[count]=89f/255f;
					crate[i].bcolor[count]=0.0f;
					break;
				case 1:
					crate[i].rcolor[count]=0.4f;
					crate[i].gcolor[count]=0.0f;
					crate[i].bcolor[count]=0.4f;
					break;
				case 2:
					crate[i].rcolor[count]=0.5f;
					crate[i].gcolor[count]=0.25f;
					crate[i].bcolor[count]=0.0f;
					break;
				}
				
			}
			
			
			fish = new sketch();
			fish.numpoints = 14;
	
			fish.xmat[0] =5;
			fish.ymat[0] = 0;
			fish.xmat[1] = 2;
			fish.ymat[1] = 3;
			
			fish.xmat[2] = 2;
			fish.ymat[2] = 3;
			fish.xmat[3] = -2;
			fish.ymat[3] =3;
			
			fish.xmat[4] = -2;
			fish.ymat[4]= 3;
			fish.xmat[5] = -7;
			fish.ymat[5] = 0;
	
			fish.xmat[6] = -7;
			fish.ymat[6] =0;
			fish.xmat[7] = -2;
			fish.ymat[7] = -3;
			
			fish.xmat[8] = -2;
			fish.ymat[8] = -3;
			fish.xmat[9] = 2;
			fish.ymat[9] =-3;
			
			fish.xmat[10] = 2;
			fish.ymat[10] = -3;
			fish.xmat[11] = 5;
			fish.ymat[11] =0;
			
			//default tail
			fish.xmat[12]= -7;
			fish.ymat[12]= 0;
			fish.xmat[13]= -14;
			fish.ymat[13]=0;
			
			for (int count=0; count < fish.numpoints/2; count++) {
				// golden, I hope
				fish.rcolor[count]=1.0f;
				fish.gcolor[count]=0.79f;
				fish.bcolor[count]=0.05f;

			}	

		}		
		
		
		
		xvel=0d;
		yvel=0d;
		rvel=Math.random()-0.5d;
		

			
	}

}
