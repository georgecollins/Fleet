
import org.lwjgl.opengl.GL11;


public class Path {
	
	// this is a navigation aid for ships,
	// attached to islands so that ship don't have to search
	// for a way to get places
	static final int MAX_POINTS =100;  // was 50?
	int numpoints;
	int type;  // circle, to island
	double xmat[] = new double[MAX_POINTS];
	double ymat[]= new double[MAX_POINTS];
	
	
	public void BuildPathAroundIsland(island isl, IslandManager im) {
		// make a circle that is bigger then the islands radius
		// check to see that each path point is clear from objects
		// make sure you cna draw a line without island collison between the paths
		
		
		double std_radix=(isl.isketch.maxx-isl.isketch.minx)*0.7d;
		double std_radiy=(isl.isketch.maxy-isl.isketch.miny)*0.7d;
		
		for (int i=0; i < 30; i++) {
			double angle=(((double)i)/15d)*Math.PI;
			double s=1.0d;
			int tries=0;
			boolean blocked = true;
			double x=0;
			double y=0;;
			while (blocked) {
				blocked=false;
				x=isl.xpos+Math.cos(angle)*std_radix*s;
				y=isl.ypos+Math.sin(angle)*std_radiy*s;
				boolean line_collides=false;
				if (i>0) {
					line_collides=im.LineCollides(x,y, xmat[i-1], ymat[i-1]);
				} 
				if ((im.MapValue(x, y)> 0) ||(im.MapValue(x+im.MAP_SCALE,y) >0) || (im.MapValue(x-im.MAP_SCALE, y) > 0) ||
						(im.MapValue(x,y+ im.MAP_SCALE) > 0) || (im.MapValue(x, y- im.MAP_SCALE) >0)
						|| (line_collides)) {
					blocked = true;
					s=0.3d+tries*0.05d;
					tries++;
					if (tries> 40) {
							blocked= false;  // give up. 
							// the next best thing is the old point again
						 if (i>0) {
							 x=xmat[i-1];
							 y=ymat[i-1];
						 }
					}
				}
			}
			xmat[i]=x;
			ymat[i]=y;
			
		}
		numpoints=30;
	}
	
	
	public void DrawPath() {
		if (numpoints>0) {
			GL11.glBegin(GL11.GL_LINES);
			GL11.glColor3f(1.0f, 0.0f, 0.0f);
			for (int i=0; i< numpoints-1; i++) {
				GL11.glVertex3d(xmat[i], ymat[i], 0);
				GL11.glVertex3d(xmat[i+1], ymat[i+1], 0);				
			}
			GL11.glEnd();		
		}
	}

	public Path CopyTo(Path p) {

		p.numpoints = numpoints;
		p.type = type;  // circle, to island
		for (int i =0; i < MAX_POINTS; i++) {
			p.xmat[i] = xmat[i];
			p.ymat[i] = ymat[i];
		}	
		return p;
	}
	public Path() {
		
	}
}
