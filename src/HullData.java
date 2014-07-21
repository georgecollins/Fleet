import org.lwjgl.opengl.GL11;


public class HullData {
	/*
	 * This contains the hull information of a ship, 
	 * not the components that sit on top of it,
	 * the flag or the engine.  Or the physics statas
	 */
	
	sketch hull;
	static final int MAX_COMPONENTS =12;
	static int MAX_PROPS=5;
	
	int hull_type;
	static final int THIN_HULL = 1;
	static final int WIDE_HULL = 2;
	static final int DIAMOND_HULL = 3;
	static final int SUB_HULL = 4;  // dives underwater like a torpedo 
	static final int BARGE_HULL = 5; // doesn't move
	
	double hardpointx[];
	double hardpointy[];
	double facing[];  // radian facing of the hardpoint
	int sweep[];  // sweep type : fwd, bwrd, fleft, fright, r, l, lbwrd, rbwrd
	
	double width;  // currently this is a proxy for size

	double flagx;  // flag bridge
	double flagy;
	float bridgex;
	float bridgey;
	float bwidth;  // related to the bridge width / length
	float blength;
	
	double prow_length;  // this being longer makes the ship top speed faster
	double stern_length;  
	double full_length;
	/*
	 * 
	 * 		|
	 * 		|
	 * 		| <-waist_start
	 * 		|\
	 * 		| \  V- waist_width
	 * 		+------------+  <- waist_end
	 * 					 |
	 * 					 |  <- waist_length
	 * 					 |
	 * 		+------------+			
	 * 					/
	 * 				   /		<- waist taper
	 * 
	 *  From this I can figure out the last diagonal based on total length
	 *  
	 */
	
	float waist_start;
	float waist_end;
	float waist_width;
	float waist_length;
	float waist_taper;  // 
	
	double max_speed;
	int weight;
	double max_weight;
	int max_health;
	// Max Speed
	
	int cost;
	
	
	
	// where do I put prop points?  
	int prop_count;
	double propx[], propy[];

	
	int numhardpoints;
	
	public double CalcMaxWeight() {
		max_weight=numhardpoints*75;  // this will get more complicated
		return max_weight;
	}
	
	public int CalcCost() {
		switch (hull_type) {
		case THIN_HULL:
				cost=numhardpoints*90;
				break;
		case WIDE_HULL:
				cost=numhardpoints*75;
				break;
		case DIAMOND_HULL:
			cost=numhardpoints*100;
			break;
		case BARGE_HULL:
			cost = numhardpoints*50;
			break;
				
		}
		return cost;
	}
	
	public int CalcMaxHitpoints() {
		max_health=0;
		switch (hull_type) {
		case THIN_HULL:
				max_health=numhardpoints*((int) (width*0.65d));
				break;
		case WIDE_HULL:
				max_health=numhardpoints*((int) (width*0.85d));
				break;
		case DIAMOND_HULL:
			max_health=numhardpoints*((int) (width*0.75d));
			break;
		case BARGE_HULL:
			max_health = numhardpoints*((int) (width));
			break;
			
		}
		return max_health;
	}
	public double CalcMaxSpeed() {
		max_speed=0;
		switch (hull_type) {
		case THIN_HULL:
				max_speed=(34-width/2);  //36 - width/2  then 40 was 55- width
				break;
		case WIDE_HULL:
				max_speed=(27-width/2); //31 - width /2 34 was 45- width
				break;
		case DIAMOND_HULL:
				max_speed=(30-width/2);  //33 - width/ 2  37 was 50 - width
				break;
		case BARGE_HULL:
			max_speed = 0;
			break;
		}
		return max_speed;
	}	
	
	public double GetTurnRate()  {
		double turn_rate=0.01d;
		// later take engines into account
		switch (hull_type) {
		case THIN_HULL:
				turn_rate= 0.24d-numhardpoints*0.035d; // up to 6
				break;
		case WIDE_HULL:
				turn_rate= 0.33d-numhardpoints*0.03d;
				break;
		case DIAMOND_HULL:
				turn_rate=0.27d-numhardpoints*0.02d;
				break;
		case BARGE_HULL:
			turn_rate = 0;
			break;
		}	
		return turn_rate;
	}
	
	public void DerivedQualities() {
		CalcMaxSpeed();
		CalcMaxHitpoints();
		CalcMaxWeight();
		
	}
	
	public HullData BuildHull(HullData hd) {

		width= Math.random()*10d+18d;
		int point_count= (int) (Math.random()*2d+Math.random()*3d)+2; // should be 6-2 points
		int front_points = (int) (Math.random()*2d+Math.random()*3d);  // should be 0-4
		
		if (front_points > point_count-1) front_points=point_count-1;
		
		if (Math.random()<0.7) {
			ThinHull(width, point_count, front_points);
		}
		else {
			if (front_points==4) front_points=3;  // 4 is too much for a diamond hull
			int back_points= (int) (Math.random()*3);  //0-2
			int mid_points=(int) (2*(1+Math.random()*2));  // was *2
			point_count=front_points+back_points+mid_points;
			int bridge_location= (int) (Math.random()*point_count);

			DiamondHull(width, point_count, front_points, back_points, bridge_location);  // should be 2, 4 (double) ,1 // 7,2, 1
		}
		return hd;
	}
	
	public HullData BuildSmallHull(HullData hdin) {
		// build a hull that's half size or less
		double rnd = Math.random();
		width= Math.random()*10d+14d;  // a little smaller
		
		int point_count= (int) (Math.random()*3d)+2; // should be 2-4 points
		int front_points = (int) (Math.random()*3d);  // should be 0-2
		
		if (front_points > point_count-1) front_points=point_count-1;
		
		if (rnd < 0.5) {
			// thin hull
			ThinHull(width, point_count, front_points);
		}
		else 
		{
			if (rnd < 0.8) {
				// wide hull
				point_count=(int) (Math.random()*3d+1);  // 1-3, shorter then the thing hull 2-4 points
				int bridge_point = (int) (Math.random()*point_count);  
				int bridge_side= (int) Math.random()*2;  // 0-1  which side is the bridge on
				BuildWideHull(width, point_count, bridge_point, bridge_side);
			
			}
			else
			{
				// diamond hull
				
				if (front_points>2) front_points=2;  // 2 is too much for a diamond hull
				int back_points= (int) (Math.random()*2);  //0-2
				int mid_points=(int) (2);  // was *2
				point_count=front_points+back_points+mid_points;
				int bridge_location= (int) (Math.random()*point_count);

				DiamondHull(width, point_count, front_points, back_points, bridge_location);  // should be 2, 4 (double) ,1 // 7,2, 1

			}
		}
		CopyTo(hdin);  // put what I just made into hdin
		return hdin;
	}
	
	HullData BuildWideHull(HullData hd) {

		width= Math.random()*10d+18d;  // it is 2 x width
		int point_count= (int) (Math.random()*2d+Math.random()*2d)+1; // should be 1-3 points
		int bridge_point = (int) (Math.random()*point_count);  
		int bridge_side= (int) Math.random()*2;  // 0-1  which side is the bridge on
		BuildWideHull(width, point_count, bridge_point, bridge_side);
		return hd;
	
	}
	void BuildWideHull(double wd, int pc, int bpoint, int bside) {	
	//	if (front_points > point_count-1) front_points=point_count-1;
		hull_type= WIDE_HULL;
		width=wd;
		int point_count = pc;
		int bridge_point =bpoint;
		int bridge_side = bside;
		// draw hull
		// length = num points * width 1.2d;
		
		double length= point_count * width*1.5d;
		
		// prow 
		prow_length= (width*(Math.random()+0.7d));  
		// stern length
		double stern_length=width*(3d*Math.random()-1d)/4d;
		full_length=length+prow_length+Math.max(0, stern_length);
		
		hull.xmat[0]=0;
		hull.ymat[0]= (float)(length/2d+prow_length);

		
		hull.numpoints=24;  // should be more ? 
		hull.xmat[1]=(float)( -width);  // it's wide now
		hull.ymat[1]=(float) (length/2d);
		
		// \
		hull.xmat[2]=0;
		hull.ymat[2]= (float)(length/2d+prow_length);
		hull.xmat[3]=(float) (width); // it's wide now
		hull.ymat[3]= (float)(length/2d);
		
		// right side
		hull.xmat[4]=(float) (width);
		hull.ymat[4]= (float)(length/2d);		
		hull.xmat[5]=(float) (width);
		hull.ymat[5]= (float)(0);	
		
		hull.xmat[6]=(float) (width);
		hull.ymat[6]= (float)(0);			
		hull.xmat[7]=(float) (width);
		hull.ymat[7]= (float)(-length/2d);		
	
		
		// left side
		hull.xmat[8]=(float) (-width);
		hull.ymat[8]= (float)(length/2d);	
		hull.xmat[9]=(float) (-width);
		hull.ymat[9]= (float)(0);		
		
		hull.xmat[10]=(float) (-width);
		hull.ymat[10]= (float)(0);		
	
		hull.xmat[11]=(float) (-width);
		hull.ymat[11]= (float)(-length/2d);		
		
		// stern
		
		hull.xmat[12]=(float) (width);
		hull.ymat[12]= (float)(-length/2d);		
		hull.xmat[13]=(float) 0;
		hull.ymat[13]= (float)(-length/2d - stern_length);		
		
		hull.xmat[14]=(float) 0;
		hull.ymat[14]= (float)(-length/2d -stern_length);		
		hull.xmat[15]=(float) (-width);
		hull.ymat[15]= (float)(-length/2d);		
	
	
		double spacing = width*1.3d;
		double startpoint = length/2d-spacing/2d;
		numhardpoints=0;
		for (int side = 0; side < 2; side++ )
			for (int count=0; count < point_count; count++) {
				
				if ((count==bridge_point) && (side==bridge_side)) {
					flagx = -width/2+ width*side;			
					flagy = startpoint - count*spacing;
				} else
				{

					hardpointx[numhardpoints] = -width/2+ width*side; // + /- 1/2 width		
					hardpointy[numhardpoints] = startpoint - count*spacing;


					if (count == 0) { // front
						if (side==0) {  // left
							sweep[numhardpoints]=5;  // front left corner

							
						} else
							sweep[numhardpoints]=6; // front right corner
					}
					if (count== point_count-1)  { // back
						if (side==0) { // left
							sweep[numhardpoints]=7;  // back left						
						}
						else
							sweep[numhardpoints]=8;
					}
					if ((count!=0) && (count!=point_count-1)) {  // not front or back == sides
						if (side==0) {
							sweep[numhardpoints]=3;  // left side
							
						} else
							sweep[numhardpoints]=4;  // right side					
						}
						numhardpoints++;
					}
		
			
			}			
	

		// Draw flag bridge
		bwidth= (float) (width*0.6d);
		blength = (float) (width*0.8d);
		bridgex= (float) (-width/2d + 0.2d * width+flagx);
		bridgey= (float) (flagy-width/2);  //(startpoint -((double) (front_points)) * spacing)-bwidth/2 ;
	
		// -  bottom
		hull.xmat[16] = bridgex+((float) (width*0.1d));
		hull.ymat[16] = bridgey;
		hull.xmat[17] = bridgex+bwidth-((float)(width*0.1d));
		hull.ymat[17] = bridgey;
		
		// right |
		hull.xmat[18] = bridgex +bwidth-((float) (width*0.1d));
		hull.ymat[18] = bridgey;
		hull.xmat[19] = bridgex + bwidth;
		hull.ymat[19] = bridgey + blength;
		
		// bottom
		hull.xmat[20] = bridgex + bwidth;
		hull.ymat[20] = bridgey + blength;
		hull.xmat[21] = bridgex;
		hull.ymat[21] = bridgey + blength;
		// left |
		hull.xmat[22] = bridgex;
		hull.ymat[22] = bridgey + blength;
		hull.xmat[23] = bridgex+((float) (width*0.1d));
		hull.ymat[23] = bridgey;

		// add propellers
		
		prop_count=2;
		propx[0]=-width/3d;
		propy[0]=-length/2d-stern_length/3d;
		propx[1]=width/3d;
		propy[1]=-length/2d-stern_length/3d;
		
		// do I need this?
		//hd=this;
		
		weight=(int) (width*numhardpoints);
		
		DerivedQualities();
		for (int count=0; count<hull.numpoints/2; count++) {
			hull.rcolor[count]=1.0f;
			hull.gcolor[count]=1.0f;
			hull.bcolor[count]=1.0f;
			
		}
	}

	void ThinHull(double iwidth, int point_count, int front_points) {
		// draw hull
		// length = num points * width 1.2d;
		hull_type= THIN_HULL;
		width = iwidth;
		
		double length= point_count * width*1.3d;

		// prow 
		prow_length= (width*(Math.random()+0.5d));  
		// stern length
		double stern_length=width*(3d*Math.random()-1d)/4d;
		full_length=length+prow_length+Math.max(0, stern_length);
		
		hull.numpoints=24;
		
		// /
		hull.xmat[0]=0;
		hull.ymat[0]= (float)(length/2d+prow_length);
		
		hull.xmat[1]=(float)( -width/2d);
		hull.ymat[1]=(float) (length/2d);
		
		// \
		hull.xmat[2]=0;
		hull.ymat[2]= (float)(length/2d+prow_length);
		hull.xmat[3]=(float) (width/2d);
		hull.ymat[3]= (float)(length/2d);
		
		// right side
		hull.xmat[4]=(float) (width/2d);
		hull.ymat[4]= (float)(length/2d);		
		hull.xmat[5]=(float) (width/2d);
		hull.ymat[5]= (float)(0);		
		hull.xmat[6]=(float) (width/2d);
		hull.ymat[6]= (float)(0);		
		
		hull.xmat[7]=(float) (width/2d);
		hull.ymat[7]= (float)(-length/2d);		
	
		
		// left side
		hull.xmat[8]=(float) (-width/2d);
		hull.ymat[8]= (float)(length/2d);	
		hull.xmat[9]=(float) (-width/2d);
		hull.ymat[9]= (float)(0);		
		
		hull.xmat[10]=(float) (-width/2d);
		hull.ymat[10]= (float)(0);		
	
		hull.xmat[11]=(float) (-width/2d);
		hull.ymat[11]= (float)(-length/2d);		
		
		// stern
		
		hull.xmat[12]=(float) (width/2d);
		hull.ymat[12]= (float)(-length/2d);		
		hull.xmat[13]=(float) 0;
		hull.ymat[13]= (float)(-length/2d - stern_length);		
		
		hull.xmat[14]=(float) 0;
		hull.ymat[14]= (float)(-length/2d -stern_length);		
		hull.xmat[15]=(float) (-width/2);
		hull.ymat[15]= (float)(-length/2d);			

		
		double spacing = width*1.2d;
		double startpoint = length/2d-spacing/2d;
		numhardpoints=0;
		for (int count=0; count < point_count; count++) {
			if (count< front_points) {
				hardpointx[numhardpoints] = 0d;			
				hardpointy[numhardpoints] = startpoint - count*spacing;
				sweep[numhardpoints]=1;  // = 1 front facing
				numhardpoints++;
			}
			// flag bridge
			if (count==front_points)  {
				flagx = 0d;			
				flagy = startpoint - count*spacing;
			}
			// rear sweep
			if (count> front_points) {
				hardpointx[numhardpoints] = 0d;			
				hardpointy[numhardpoints] = startpoint - count*spacing;
				sweep[numhardpoints]=2;  // =2 rear facing 
				numhardpoints++;
			}			
		}
		
		// Draw flag bridge
		bwidth= (float) (width*0.6d);
		blength = (float) (width*0.8d);
		bridgex= (float) (flagx-width/2d + 0.2d * width);
		bridgey= (float) (flagy-width/2);  //(startpoint -((double) (front_points)) * spacing)-bwidth/2 ;
	
		// -  bottom
		hull.xmat[16] = bridgex+((float) (width*0.1d));
		hull.ymat[16] = bridgey;
		hull.xmat[17] = bridgex+bwidth-((float)(width*0.1d));
		hull.ymat[17] = bridgey;
		
		// right |
		hull.xmat[18] = bridgex +bwidth-((float) (width*0.1d));
		hull.ymat[18] = bridgey;
		hull.xmat[19] = bridgex + bwidth;
		hull.ymat[19] = bridgey + blength;
		
		// bottom
		hull.xmat[20] = bridgex + bwidth;
		hull.ymat[20] = bridgey + blength;
		hull.xmat[21] = bridgex;
		hull.ymat[21] = bridgey + blength;
		// left |
		hull.xmat[22] = bridgex;
		hull.ymat[22] = bridgey + blength;
		hull.xmat[23] = bridgex+((float) (width*0.1d));
		hull.ymat[23] = bridgey;

		
		// add propellers
		
		prop_count=2;
		propx[0]=-width/3d;
		propy[0]=-length/2d-stern_length/3d;
		propx[1]=width/3d;
		propy[1]=-length/2d-stern_length/3d;
		
		// do I need this?
		//hd=this;
		
		weight=(int) (width*numhardpoints);
		
		DerivedQualities();
		for (int count=0; count<hull.numpoints/2; count++) {
			hull.rcolor[count]=1.0f;
			hull.gcolor[count]=1.0f;
			hull.bcolor[count]=1.0f;
			
		}	
	
	}
	
	void DiamondHull(double width, int point_count, int front_points, int back_points, int bridge_point) {
		// draw hull
		// length = num points * width 1.2d;
		hull_type= DIAMOND_HULL;
		
		int mid_points=point_count-front_points-back_points;  // should be an even number
		
		double length= (front_points+back_points+mid_points/2) * width*1.3d;
		
		// prow 
		prow_length= (width*(Math.random()+0.5d));  
		// stern length
		double stern_length=width*(3d*Math.random()-1d)/4d;
		full_length=length+prow_length+Math.max(0, stern_length);
		/*
		 * 
		 * 		|
		 * 		|
		 * 		| <-waist_start
		 * 		|\
		 * 		| \  V- waist_width
		 * 		+------------+  <- waist_end
		 * 					 |
		 * 					 |  <- waist_length
		 * 					 |
		 * 		+------------+			
		 * 					/
		 * 				   /		<- waist taper
		 * 
		 *  From this I can figure out the last diagonal based on total length
		 *  
		 */
	
		waist_start= (float) (length/2d- front_points*width*0.65d);
		waist_end = (float) (waist_start-width*0.65d);
		waist_width = (float) (width*1.1d);
		waist_length= (float) (waist_end-width*1.3d*(mid_points/2d));
		waist_taper = (float) (waist_length-width*0.65d);

		hull.numpoints=36;  // + 12 
		
		// /
		hull.xmat[0]=0;
		hull.ymat[0]= (float)(length/2d+prow_length);
		
		hull.xmat[1]=(float)( -width/2d);
		hull.ymat[1]=(float) (length/2d);
		
		// \
		hull.xmat[2]=0;
		hull.ymat[2]= (float)(length/2d+prow_length);
		hull.xmat[3]=(float) (width/2d);
		hull.ymat[3]= (float)(length/2d);
		
		// right side
		hull.xmat[4]=(float) (width/2d);
		hull.ymat[4]= (float)(length/2d);		
		hull.xmat[5]=(float) (((width/2d)+waist_width)/2d);
		hull.ymat[5]= waist_start;
		
		hull.xmat[6] = (float) (((width/2d)+waist_width)/2d);
		hull.ymat[6] = waist_start;
		hull.xmat[7] = waist_width;
		hull.ymat[7] = waist_end;
		
		hull.xmat[8] = waist_width;
		hull.ymat[8] = waist_end;
		hull.xmat[9] = waist_width;
		hull.ymat[9] = waist_length;
		
		hull.xmat[10] = waist_width;
		hull.ymat[10] = waist_length;
		hull.xmat[11] = (float) (((width/2d)+waist_width)/2d);
		hull.ymat[11] = waist_taper;
		
		
		hull.xmat[12]=(float) (((width/2d)+waist_width)/2d);
		hull.ymat[12]= waist_taper;		
		hull.xmat[13]=(float) (width/2d);
		hull.ymat[13]= (float)(-length/2d);		
	// ta da-- the right side.
	
		
		// left side
		hull.xmat[14]=(float) (-width/2d);
		hull.ymat[14]= (float)(length/2d);	
		hull.xmat[15]=(float) (-((width/2d)+waist_width)/2d);
		hull.ymat[15]= waist_start;
	
		
		
		hull.xmat[16] = (float) (-((width/2d)+waist_width)/2d);
		hull.ymat[16] = waist_start;
		hull.xmat[17] = -waist_width;
		hull.ymat[17] = waist_end;
		
		hull.xmat[18] = -waist_width;
		hull.ymat[18] = waist_end;
		hull.xmat[19] = -waist_width;
		hull.ymat[19] = waist_length;
		
		hull.xmat[20] = -waist_width;
		hull.ymat[20] = waist_length;
		hull.xmat[21] = (float) (-((width/2d)+waist_width)/2d);
		hull.ymat[21] = waist_taper;
		
		hull.xmat[22]=(float) (-((width/2d)+waist_width)/2d);
		hull.ymat[22]= waist_taper;		
		hull.xmat[23]=(float) (-width/2d);
		hull.ymat[23]= (float)(-length/2d);		

// stern
		
		hull.xmat[24]=(float) (width/2d);
		hull.ymat[24]= (float)(-length/2d);		
		hull.xmat[25]=(float) 0;
		hull.ymat[25]= (float)(-length/2d - stern_length);		
		
		hull.xmat[26]=(float) 0;
		hull.ymat[26]= (float)(-length/2d -stern_length);		
		hull.xmat[27]=(float) (-width/2);
		hull.ymat[27]= (float)(-length/2d);			
		
		// keep this part the same for now.  
		double spacing = width*1.1d;  // was 1.2
		double startpoint = length/2d-spacing/2d;
		numhardpoints=0;
		
		boolean nobridge=true;
		
		for (int count=0; count < front_points; count++) {
			if (count< front_points) {
				if ((nobridge) && (numhardpoints==bridge_point)) {
					flagx = 0d;			
					flagy = startpoint - count*spacing;
					nobridge=false;
				} else
				{
					hardpointx[numhardpoints] = 0d;			
					hardpointy[numhardpoints] = startpoint - count*spacing;
					sweep[numhardpoints]=1;  // = 1 front facing
					numhardpoints++;
				}
			}


		}
			
			// now do the midpoints in a square

			
		for (int side = 0; side < 2; side++ )
			for (int count=0; count < mid_points/2; count++) {
			
				if ((nobridge) && (numhardpoints==bridge_point)) {
					flagx = -width/2+ width*side; ;			
					flagy = startpoint - (count*spacing)-(front_points*spacing);
					nobridge=false;
				} else				
				{
					hardpointx[numhardpoints] = -width/2+ width*side; // + /- 1/2 width		
					hardpointy[numhardpoints] = startpoint - (count*spacing)-(front_points*spacing);


					if (count == 0) { // front
						if (side==0) {  // left
							sweep[numhardpoints]=5;  // front left corner

								
						} else
								sweep[numhardpoints]=6; // front right corner
						}
						if (count== point_count-1)  { // back
							if (side==0) { // left
								sweep[numhardpoints]=7;  // back left						
							}
							else
								sweep[numhardpoints]=8;
						}
						if ((count!=0) && (count!=point_count-1)) {  // not front or back == sides
							if (side==0) {
								sweep[numhardpoints]=3;  // left side
								
							} else
								sweep[numhardpoints]=4;  // right side					
					}
						numhardpoints++;
				}
			}
			
				
			
			// rear sweep
			if (back_points>0)
			for (int count=0; count < back_points; count++)
			{
				if ((nobridge) && (numhardpoints==bridge_point)) {
					flagx = 0d;			
					flagy = startpoint - count*spacing - front_points*spacing-mid_points*spacing/2d;
					nobridge=false;
				} else
				{
					hardpointx[numhardpoints] = 0d;			
					hardpointy[numhardpoints] = startpoint - count*spacing - front_points*spacing-mid_points*spacing/2d;
					sweep[numhardpoints]=2;  // =2 rear facing 
					numhardpoints++;
				}
			}			
	
		
		// Draw flag bridge
		bwidth= (float) (width*0.6d);
		blength = (float) (width*0.8d);
		bridgex= (float) (flagx-width/2d + 0.2d * width);
		bridgey= (float) (flagy-width/2);  //(startpoint -((double) (front_points)) * spacing)-bwidth/2 ;
	
		// -  bottom
		hull.xmat[28] = bridgex+((float) (width*0.1d));
		hull.ymat[28] = bridgey;
		hull.xmat[29] = bridgex+bwidth-((float)(width*0.1d));
		hull.ymat[29] = bridgey;
		
		// right |
		hull.xmat[30] = bridgex +bwidth-((float) (width*0.1d));
		hull.ymat[30] = bridgey;
		hull.xmat[31] = bridgex + bwidth;
		hull.ymat[31] = bridgey + blength;
		
		// bottom
		hull.xmat[32] = bridgex + bwidth;
		hull.ymat[32] = bridgey + blength;
		hull.xmat[33] = bridgex;
		hull.ymat[33] = bridgey + blength;
		// left |
		hull.xmat[34] = bridgex;
		hull.ymat[34] = bridgey + blength;
		hull.xmat[35] = bridgex+((float) (width*0.1d));
		hull.ymat[35] = bridgey;

		
		// add propellers
		
		prop_count=2;
		propx[0]=-width/3d;
		propy[0]=-length/2d-stern_length/3d;
		propx[1]=width/3d;
		propy[1]=-length/2d-stern_length/3d;
		
		// do I need this?
		//hd=this;
		
		weight=(int) (width*numhardpoints);
		
		DerivedQualities();
		for (int count=0; count<hull.numpoints/2; count++) {
			hull.rcolor[count]=1.0f;
			hull.gcolor[count]=1.0f;
			hull.bcolor[count]=1.0f;
			
		}	

	}
 		
	
	public void Barge(int numpts, float widthin) {
		hull_type = BARGE_HULL;

		width = widthin;
		numhardpoints = numpts;
		if ((numhardpoints < 1)  || (numhardpoints> 4))
			numhardpoints = 1;
		
		
		/*
		 * FRONT
		 *   +  one hard point
		 *   
		 *   ++ two hard points
		 *   
		 *   +++ three hardpoints
		 *   
		 *   ++ Four harpoints
		 *   ++
		 */
		switch (numhardpoints)	{
		case 1:
			hull.CreateRectangle(-widthin/2, -widthin/2, widthin, widthin);
			hardpointx[0]=0;
			hardpointy[0]=0;
			sweep[0] = 1;
			break;
		case 2:
			hull.CreateRectangle(-widthin, -widthin, widthin*2, widthin);
			for (int i= 0; i < 2; i++) {
				hardpointx[i]= -widthin*(0.5f)+ i*widthin;
				hardpointy[i]=0;
				sweep[i] = 1;
			}
			break;
		case 3:
			hull.CreateRectangle(-widthin*3/2, -widthin/2, widthin*2, widthin);
			for (int i= 0; i < 2; i++) {
				hardpointx[i]= -width*(1.5f)+ i*width;
				hardpointy[i]=0;
				sweep[i] = 1;
			}
		case 4:	
			hull.CreateRectangle(-widthin, -widthin, widthin*2, widthin*2);
			for (int i=0; i < 2; i++)
				for (int j=0; j < 2; j++) {
					hardpointx[i+j]= -width*(0.5f)+ i*width;
					hardpointy[i+j]=-width*(0.5f) + j* width;
					if (j==0) {
						sweep[i+j] = 1;
					}
					else
						sweep[i+j] = 2;
				}
		}
		
		// no bridge, no propellers
		weight=(int) (width*numhardpoints);
		
		DerivedQualities();
		for (int count=0; count<hull.numpoints/2; count++) {
			hull.rcolor[count]=1.0f;
			hull.gcolor[count]=1.0f;
			hull.bcolor[count]=1.0f;
			
		}				
	}
		
	
	public double GetAngle(int arc_type, double relative_angle) {

		while ((relative_angle> Math.PI) || (relative_angle< -Math.PI)) {
			if (relative_angle > Math.PI) relative_angle-=2*Math.PI;
			if (relative_angle < -Math.PI) relative_angle+=2*Math.PI;
		}	
		switch (arc_type) {
		case 0:  // fixed gun
		//	relative_angle=min_arc;
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

		return relative_angle;
	}	
	

	public void ShowHull(double u, double v, double r, double s) {
		// Show hull with hardpoints, sweeps
		hull.drawrot(u, v, r, s);

		for (int count=0; count < numhardpoints; count++) {
			// draw a dot where the hardpoint is based
			float x=(float)(hardpointx[count]*Math.cos(r) - hardpointy[count]*Math.sin(r)*s+u);
			float y=(float)(hardpointx[count]*Math.sin(r) + (hardpointy[count]-1d*s)*Math.cos(r)*s+v);

			GL11.glBegin(GL11.GL_LINES);
			GL11.glColor3f(0.25f, 0.25f, 0.0f);
			GL11.glLineWidth(2.0f);	
			GL11.glVertex3d(x, y, 0f);	
			y=(float)(hardpointx[count]*Math.sin(r) + (hardpointy[count]+2d*s)*Math.cos(r)*s+v);			
			GL11.glVertex3d(x, y, 0f);	
			// draw the sweep
			GL11.glEnd();
			GL11.glBegin(GL11.GL_LINES);
			GL11.glColor3f(0.25f, 0.25f, 0.0f);
			GL11.glLineWidth(1.0f);
			x=(float)(hardpointx[count]*Math.cos(r) - hardpointy[count]*Math.sin(r)*s+u);
			y=(float)(hardpointx[count]*Math.sin(r) + hardpointy[count]*Math.cos(r)*s+v);	
			float x1, y1, x2, y2;
			double sweep1, sweep2;
			double ang1, ang2;
			for (int arc=0; arc < 15; arc++) {
				sweep1=(arc-8d)/8d*Math.PI;//+Math.PI/2;
			//	if (sweep_ang > Math.PI) sweep_ang=sweep_ang-2*Math.PI;
				ang1=GetAngle(sweep[count], sweep1);
				x1=x+(float)(Math.cos(sweep1+Math.PI/2) * width/3d);
				y1=y+(float)(Math.sin(sweep1+Math.PI/2) * width/3d);
				sweep2=(arc-7d)/8d*Math.PI;//+Math.PI/2;
			//	if (sweep_ang > Math.PI) sweep_ang=sweep_ang-2*Math.PI;
				ang2=GetAngle(sweep[count], sweep2) ;
				x2=x+(float)(Math.cos(sweep2+Math.PI/2) * width/3d);
				y2=y+(float)(Math.sin(sweep2+Math.PI/2) * width/3d);
				if ((ang1==sweep1) && (ang2==sweep2)) {
					GL11.glVertex3d(x1, y1, 0f);
					GL11.glVertex3d(x2, y2, 0f);
				}
			}
			GL11.glEnd();
		}

	}
	
	
	public HullData CopyTo(HullData hd) {
		hull.CopyTo(hd.hull);
		hd.hull_type = hull_type;
		
		for (int count = 0; count < MAX_COMPONENTS; count++) {
			hd.hardpointx[count] = hardpointx[count];
			hd.hardpointy[count]= hardpointy[count];
			hd.facing[count] =  facing[count];  // radian facing of the hardpoint
			hd.sweep[count] = sweep[count];  // sweep type : fwd, bwrd, fleft, fright, r, l, lbwrd, rbwrd
		}
		hd.width = width;  // currently this is a proxy for size

		hd.flagx = flagx;  // flag bridge
		hd.flagy = flagy;
		hd.bridgex = bridgex;
		hd.bridgey = bridgey;
		hd.bwidth = bwidth;  // related to the bridge width / length
		hd.blength = blength;
		
		hd.prow_length = prow_length;  // this being longer makes the ship top speed faster
		hd.stern_length = stern_length;  
		hd.full_length = full_length;
		
		hd.waist_start = waist_start;
		hd.waist_end = waist_end;
		hd.waist_width = waist_width;
		hd.waist_length = waist_length;
		hd.waist_taper = waist_taper;  // 
		
		hd.max_speed = max_speed;
		hd.weight = weight;
		hd.max_weight = max_weight;
		hd.max_health = max_health;
		// Max Speed
		
		hd.cost = cost;
		
		
		
		// where do I put prop points?  
		hd.prop_count = prop_count;
		for (int count = 0; count < MAX_PROPS; count++) {
		 hd.propx[count] = propx[count];
		 hd.propy[count] = propy[count];

		}
		
		hd.numhardpoints = numhardpoints;
		return hd;
	}
	
	HullData() {
		hardpointx = new double[MAX_COMPONENTS];
		hardpointy = new double[MAX_COMPONENTS];
		facing = new double[MAX_COMPONENTS];
		sweep = new int[MAX_COMPONENTS];

		propx = new double[MAX_PROPS]; 
		propy = new double[MAX_PROPS];
		hull=new sketch();
	}
	

}
