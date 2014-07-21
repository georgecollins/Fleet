
public class DamageSketch extends sketch {
	void Damage() {
		// warp the model a bit
		for (int count=0; count < numpoints; count++) {
			if (Math.random()>0.8d) {
				xmat[count]= xmat[count]+(float)Math.random()*6-3f;
				ymat[count]= ymat[count]+(float)Math.random()*6-3f;
			}
		}
	}
	void BuildExplosion() {
		// set up the vectors for an explosion
		
		// find the center point
	}
	void SimulateExplosion(double speed, double t) {
		// take each line segment
		// figute out it's average distancefrom the center
		// normailze it.  
		// move both points in that vector diretion t times speed
		
		float avgx, avgy;
		for (int count=0; count < numpoints; count+=2) {
			avgx=xmat[count]+xmat[count+1];
			avgy=ymat[count]+ymat[count+1];
			double mag=Math.sqrt(avgx*avgx+avgy*avgy);
			xmat[count]=xmat[count]+(float) (avgx*speed*t/mag);
			ymat[count]=ymat[count]+(float) (avgy*speed*t/mag);		
			xmat[count+1]=xmat[count+1]+(float) (avgx*speed*t/mag);
			ymat[count+1]=ymat[count+1]+(float) (avgy*speed*t/mag);	

		}
//		ChangeColor((float)Math.min(1.0f, rcolor[0]+speed/t/20d),(float)Math.min(1.0f, gcolor[0]+speed/t/20d), (float)Math.min(1.0f, bcolor[0]+speed/t/20d)); 
		
		
	}
	
	
	DamageSketch(sketch cpy) {
		// just copy it in
		super();
		numpoints=cpy.numpoints;
		for (int count =0; count< numpoints/2; count++) {
			rcolor[count]=cpy.rcolor[count];
			gcolor[count]=cpy.gcolor[count];
			bcolor[count]=cpy.bcolor[count];
			width[count]=cpy.width[count];
					
		}
		
		for (int count = 0; count < numpoints; count++ ) {
			xmat[count]=cpy.xmat[count];
			ymat[count]=cpy.ymat[count];
		}
	}

	DamageSketch(String dfile) {
		super(dfile);
	}
}
