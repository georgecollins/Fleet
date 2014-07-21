
public class ComponentBuilder {
	
	// builds components like guns, torpedos, sonar radar etc.  
	

	final float CANNON_RED = 0.2f;
	final float CANNON_GREEN = 0.65f;
	final float CANNON_BLUE = 0.65f;
	
	
	public component BuildFixedCannon(component gun, float radi, int numguns) {
		gun.cd = new ComponentData();
		gun.cd.csketch.numpoints=8;	
		gun.cd.guncount=numguns;
		
		float xradi=radi;
		float yoffset=0f;
		if (numguns==0) {
			xradi=xradi/2f;  // if its torpedo then it's narrow
			// and forawrd
			yoffset=5f;
		}
		
		gun.cd.csketch.xmat[0]=-xradi;
		gun.cd.csketch.ymat[0]=-radi+yoffset;
		gun.cd.csketch.xmat[1]=xradi;
		gun.cd.csketch.ymat[1]=-radi+yoffset;
		
		gun.cd.csketch.xmat[2]=xradi;
		gun.cd.csketch.ymat[2]=-radi+yoffset;
		gun.cd.csketch.xmat[3]=xradi;
		gun.cd.csketch.ymat[3]=radi+yoffset;

		gun.cd.csketch.xmat[4]=xradi;
		gun.cd.csketch.ymat[4]=radi+yoffset;		
		gun.cd.csketch.xmat[5]=-xradi;
		gun.cd.csketch.ymat[5]=radi+yoffset;

		gun.cd.csketch.xmat[6]=-xradi;
		gun.cd.csketch.ymat[6]=radi+yoffset;
		gun.cd.csketch.xmat[7]=-xradi;
		gun.cd.csketch.ymat[7]=-radi+yoffset;
		
		for (int count=0; count < gun.cd.csketch.numpoints/2; count++) {		
			// set colors
			gun.cd.csketch.rcolor[count]=0.0f;
			gun.cd.csketch.gcolor[count]=0.75f;
			gun.cd.csketch.bcolor[count]=0.75f;
		}
		int oldpoints=gun.cd.csketch.numpoints;
		float gunlength = radi*0.75f+(3-numguns)*radi/2f;
		
		gun.cd.csketch.numpoints+=2*numguns;
		
		for (int count=0; count< numguns; count++) {
		// add color
			gun.cd.csketch.rcolor[oldpoints/2 + count]=CANNON_RED;  //0
			gun.cd.csketch.gcolor[oldpoints/2 + count]=CANNON_BLUE;  //0.75
			gun.cd.csketch.bcolor[oldpoints/2 + count]=CANNON_GREEN; // 0.75
			gun.cd.csketch.width[oldpoints/2+count]=2.0f;  // widen the guns
		
			if (numguns==0) { // torpedo
				gun.cd.muzzlex[0]=0;
				gun.cd.muzzley[0]=0;
			}
			
			// draw gun
			if (numguns==1) {
				gun.cd.csketch.xmat[oldpoints + count*2]=0f;
				gun.cd.csketch.ymat[oldpoints + count*2]=radi;
				gun.cd.csketch.xmat[oldpoints+1+count*2]=0f;
				gun.cd.csketch.ymat[oldpoints+1+count*2]=radi+gunlength;
				gun.cd.muzzlex[count]=0f;
				gun.cd.muzzley[count]=radi+gunlength;
			} else
				if (numguns==2)
				{
					gun.cd.csketch.xmat[oldpoints + count*2]=-(radi/3f)+count*radi*2/3f;
					gun.cd.csketch.ymat[oldpoints + count*2]=radi;
					gun.cd.csketch.xmat[oldpoints+1 + count*2]=-(radi/3f)+count*radi*2/3f;
					gun.cd.csketch.ymat[oldpoints+1 + count*2]=radi+gunlength;	
					gun.cd.muzzlex[count]=-(radi/3f)+count*radi*2/3f;
					gun.cd.muzzley[count]=radi+gunlength;
				}
				else
				{
					gun.cd.csketch.xmat[oldpoints + count*2]=-(radi/2f)+count*radi/2f;
					gun.cd.csketch.ymat[oldpoints + count*2]= radi;
					gun.cd.csketch.xmat[oldpoints+1 + count*2]=-(radi/2f)+count*radi/2f;
					gun.cd.csketch.ymat[oldpoints+1 + count*2]= radi+gunlength;	
			
					gun.cd.muzzlex[count]=-(radi/2f)+count*radi/2f;
					gun.cd.muzzley[count]=radi+gunlength;
					
				}
		}
		
		gun.cd.cdsketch=new DamageSketch(gun.cd.csketch);
		
			// add compensating data
		gun.cd.fire_rate =3- numguns; 	// how long I need to refresh

		gun.cd.clip_size = 8-numguns*2;
		gun.cd.shot_speed=3-numguns;  // = 0-2

		
		return gun;	
	}
	
	
	public component BuildCannon(component gun, float radi, int numguns) {
		gun.cd = new ComponentData();
		gun.cd.csketch.numpoints=16;	
		gun.cd.guncount=numguns;

		for (int count=0; count<16; count+=2) {
			int npoint=count/2;
			double dpoint=((double)npoint) -0.5d;  // so I get a flat top face
			gun.cd.csketch.xmat[count]=(float) Math.cos( dpoint/4d*Math.PI )*radi;
			gun.cd.csketch.ymat[count]=(float) Math.sin( dpoint/4d*Math.PI)*radi;
			gun.cd.csketch.xmat[count+1]=(float) Math.cos( (dpoint+1d)/4d*Math.PI)*radi;
			gun.cd.csketch.ymat[count+1]=(float) Math.sin( (dpoint+1d)/4d*Math.PI)*radi;
		
		}
		
		for (int count=0; count < gun.cd.csketch.numpoints/2; count++) {		
			// set colors
			gun.cd.csketch.rcolor[count]=CANNON_RED;
			gun.cd.csketch.gcolor[count]=CANNON_BLUE;
			gun.cd.csketch.bcolor[count]=CANNON_GREEN;//
		}
		
		

		int oldpoints=gun.cd.csketch.numpoints;
		float gunlength = radi*0.75f+(3-numguns)*radi/2f;
		
		gun.cd.csketch.numpoints+=2*numguns;
		
		for (int count=0; count< numguns; count++) {
		// add color
			gun.cd.csketch.rcolor[oldpoints/2 + count]=0.0f;
			gun.cd.csketch.gcolor[oldpoints/2 + count]=0.75f;
			gun.cd.csketch.bcolor[oldpoints/2 + count]=0.75f;
			gun.cd.csketch.width[oldpoints/2+count]=2.0f;  // widen the guns
		
		// draw gun
			if (numguns==1) {
				gun.cd.csketch.xmat[oldpoints + count*2]=0f;
				gun.cd.csketch.ymat[oldpoints + count*2]=radi;
				gun.cd.csketch.xmat[oldpoints+1+count*2]=0f;
				gun.cd.csketch.ymat[oldpoints+1+count*2]=radi+gunlength;
				gun.cd.muzzlex[count]=0f;
				gun.cd.muzzley[count]=radi+gunlength;
			} else
				if (numguns==2)
				{
					gun.cd.csketch.xmat[oldpoints + count*2]=-(radi/4f)+count*radi/2f;
					gun.cd.csketch.ymat[oldpoints + count*2]=radi;
					gun.cd.csketch.xmat[oldpoints+1 + count*2]=-(radi/4f)+count*radi/2f;
					gun.cd.csketch.ymat[oldpoints+1 + count*2]=radi+gunlength;	
					gun.cd.muzzlex[count]=-(radi/4f)+count*radi/2f;
					gun.cd.muzzley[count]=radi+gunlength;
				}
				else
				{
					gun.cd.csketch.xmat[oldpoints + count*2]=-(radi/3f)+count*radi/3f;
					gun.cd.csketch.ymat[oldpoints + count*2]=radi;
					gun.cd.csketch.xmat[oldpoints+1 + count*2]=-(radi/3f)+count*radi/3f;
					gun.cd.csketch.ymat[oldpoints+1 + count*2]=radi+gunlength;	
					gun.cd.muzzlex[count]=-(radi/3f)+count*radi/3f;
					gun.cd.muzzley[count]=radi+gunlength;
					
				}
			

			
		}
		gun.cd.cdsketch=new DamageSketch(gun.cd.csketch);

		
		// add compensenting data
		gun.cd.fire_rate =3- numguns; 
		gun.cd.clip_size = 8-numguns*2;
		gun.cd.shot_speed=3-numguns;
		
		return gun;

	}
	
	public void BuildContainer(component cargo) {
		cargo.cd = new ComponentData();
		cargo.cd.csketch.numpoints=8;
		float radi=6f;  // the container is 13, package is 11
		cargo.cd.csketch.xmat[0]=-radi;
		cargo.cd.csketch.ymat[0]=radi;
		cargo.cd.csketch.xmat[1]=radi;
		cargo.cd.csketch.ymat[1]=radi;
		
		cargo.cd.csketch.xmat[2]=radi;
		cargo.cd.csketch.ymat[2]=radi;
		cargo.cd.csketch.xmat[3]=radi;
		cargo.cd.csketch.ymat[3]=-radi;
		
		cargo.cd.csketch.xmat[4]=radi;
		cargo.cd.csketch.ymat[4]=-radi;
		cargo.cd.csketch.xmat[5]=-radi;
		cargo.cd.csketch.ymat[5]=-radi;
		
		cargo.cd.csketch.xmat[6]=-radi;
		cargo.cd.csketch.ymat[6]=-radi;
		cargo.cd.csketch.xmat[7]=-radi;
		cargo.cd.csketch.ymat[7]=radi;
		
		for (int count=0; count < cargo.cd.csketch.numpoints/2; count++) {
			cargo.cd.csketch.rcolor[count]=0.25f;
			cargo.cd.csketch.gcolor[count]=0.25f;
			cargo.cd.csketch.bcolor[count]=0.35f;
		}
		cargo.cd.cdsketch=new DamageSketch(cargo.cd.csketch);
		
	}
}


