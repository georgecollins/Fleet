import org.lwjgl.opengl.GL11;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Scanner;


public class sketch {
	public static int MAX_POINTS=44;
	int numpoints;
	float rcolor[] = new float[MAX_POINTS/2];
	float gcolor[] = new float[MAX_POINTS/2];
	float bcolor[] = new float[MAX_POINTS/2];
	float xmat[]= new float[MAX_POINTS];
	float ymat[]= new float[MAX_POINTS];
	float width[] = new float[MAX_POINTS/2];
	// world coordinates
	double worldx[]= new double[MAX_POINTS];
	double worldy[]= new double[MAX_POINTS];
	
	float maxx, maxy, minx, miny;  // the bounding box

	public void AddPoint(int pt, float x, float y) {
		xmat[pt]=x;
		ymat[pt]=y;
	}
	public float getX(int pt) {
		return xmat[pt];
	}
	public float getY(int pt) {
		return ymat[pt];
	}
	public void ChangeColor(float r,float g, float b) {
		for (int i=0; i < numpoints/2; i++) {
			rcolor[i]=r;
			gcolor[i]=g;
			bcolor[i]=b;
		}
	}
	public float AddLine(float a, float b, float c, float d) {
		AddPoint(numpoints, a, b);
		AddPoint(numpoints+1, c, d);
		numpoints+=2;
		return a;
	}

	
	public void move(float u, float v) {
		for (int count=0;count<numpoints; count++) {
			xmat[count]+=u;
			ymat[count]+=v;
		}
			
	}
	
	private float SetColorAndWidth(int count, float last_width, float f) {
		if (count%2==0) {
			GL11.glColor3f(rcolor[count/2]*f, gcolor[count/2]*f,bcolor[count/2]*f);
			if ((width[count/2]>1f) && (width[count/2]!=last_width)) {
				GL11.glEnd();
				last_width=width[count/2];
				GL11.glLineWidth(width[count/2]);
				GL11.glBegin(GL11.GL_LINES);
			}
			else
				if ((width[count/2]<1.1f) && (last_width!=1.0f)) {
					GL11.glEnd();
					GL11.glLineWidth(1.0f);
					GL11.glBegin(GL11.GL_LINES);
					last_width=1.0f;
				}
		}
		return last_width;
	}
	
	public void drawit() {

		float last_width=1.0f;
		GL11.glLineWidth(1.0f);
		GL11.glBegin(GL11.GL_LINES);
		for (int count=0;count < numpoints;count++) {
			last_width=SetColorAndWidth(count, last_width, 1.0f);
			
			GL11.glVertex3f(getX(count), getY(count), 0f);
		}
		GL11.glEnd();
	}
	public void drawat(float u, float v) {
		float last_width=1.0f;
		GL11.glLineWidth(1.0f);
		GL11.glBegin(GL11.GL_LINES);
		for (int count=0;count<numpoints; count++) {
			last_width=SetColorAndWidth(count, last_width, 1.0f);
		
			GL11.glVertex3f(getX(count)+u, getY(count)+v, 0f);
		}
		GL11.glEnd();
	}

	public void drawrot(float u, float v, float r) {
		maxx=-100000.0f;
		maxy=-100000.0f;
		minx=100000.0f;
		miny=100000.0f;
		
		float last_width=1.0f;
		GL11.glLineWidth(1.0f);
		GL11.glBegin(GL11.GL_LINES);
			

	
		
		for (int count=0;count<numpoints; count++) {
			last_width=SetColorAndWidth(count, last_width, 1.0f);
			worldx[count]=getX(count)*Math.cos(r)-getY(count)*Math.sin(r)+u;
			worldy[count]=getX(count)*Math.sin(r)+getY(count)*Math.cos(r)+v;
			GL11.glVertex3d(worldx[count], worldy[count] , 0f);
			UpdateBounds((float) worldx[count], (float) worldy[count]);
		
		}
		
		// draw the bounding box
				GL11.glColor3f(0.75f, 0.75f, 0.0f);
				GL11.glVertex3d(maxx, maxy,0f);
				GL11.glVertex3d(minx, maxy,0f);
				GL11.glVertex3d(minx, maxy,0f);
				GL11.glVertex3d(minx, miny,0f);
				GL11.glVertex3d(minx, miny, 0f);
				GL11.glVertex3d(maxx, miny, 0f);
				GL11.glVertex3d(maxx, miny, 0f);
				GL11.glVertex3d(maxx, maxy, 0f);
				
		
		GL11.glEnd();		
	}

	public void drawrot(double u, double v, double r) {
		maxx=-100000.0f;
		maxy=-100000.0f;
		minx=100000.0f;
		miny=100000.0f;
		float last_width=1.0f;
		GL11.glLineWidth(1.0f);
		GL11.glBegin(GL11.GL_LINES);
			

		double cs = Math.cos(r);
		double sn = Math.sin(r);
		
		for (int count=0;count<numpoints; count++) {
			last_width=SetColorAndWidth(count, last_width, 1.0f);
			worldx[count]=getX(count)*cs-getY(count)*sn+u;
			worldy[count]=getX(count)*sn+getY(count)*cs+v;
			GL11.glVertex3d(worldx[count], worldy[count] , 0f);
			UpdateBounds((float) worldx[count], (float) worldy[count]);
		
		}
		

		
		GL11.glEnd();		
	}
	// s is scale
	public void drawrot(double u, double v, double r, double s) {
		maxx=-100000.0f;
		maxy=-100000.0f;
		minx=100000.0f;
		miny=100000.0f;
		float last_width=1.0f;
		GL11.glLineWidth(1.0f);
		GL11.glBegin(GL11.GL_LINES);
			
		double cs = Math.cos(r);
		double sn = Math.sin(r);

	
		
		for (int count=0;count<numpoints; count++) {
			last_width=SetColorAndWidth(count, last_width, 1.0f);
			worldx[count]=(getX(count)*cs-getY(count)*sn)*s+u;
			worldy[count]=(getX(count)*sn+getY(count)*cs)*s+v;
			GL11.glVertex3d(worldx[count], worldy[count] , 0f);
			UpdateBounds((float) worldx[count], (float) worldy[count]);
		
		}
		

		
		GL11.glEnd();		
	}
	
	public void drawrot(double u, double v, double r, double s, double f) {
		maxx=-100000.0f;
		maxy=-100000.0f;
		minx=100000.0f;
		miny=100000.0f;
		float last_width=1.0f;
		GL11.glLineWidth(1.0f);
		GL11.glBegin(GL11.GL_LINES);
			

		double cs = Math.cos(r);
		double sn = Math.sin(r);

		
		for (int count=0;count<numpoints; count++) {
			last_width=SetColorAndWidth(count, last_width, (float) f);
			worldx[count]=(getX(count)*cs-getY(count)*sn)*s+u;
			worldy[count]=(getX(count)*sn+getY(count)*cs)*s+v;
			GL11.glVertex3d(worldx[count], worldy[count] , 0f);
			UpdateBounds((float) worldx[count], (float) worldy[count]);
		
		}
		

		
		GL11.glEnd();		
	}
	/*
	 *   SetWorldPoints
	 *   This function lets you set the world coordinates for a sketch you 
	 *   don't draw, like an AI projection of where you will be in the future. 
	 *   Getting the world points of an undrawn sketch means you can test for collision.  
	 */

	public void SetWorldPoints(double u, double v, double r) {
		maxx=-100000.0f;
		maxy=-100000.0f;
		minx=100000.0f;
		miny=100000.0f;		
		
		double cs = Math.cos(r);
		double sn = Math.sin(r);

		for (int count=0;count<numpoints; count++) {
			worldx[count]=getX(count)*cs-getY(count)*sn+u;
			worldy[count]=getX(count)*sn+getY(count)*cs+v;
			UpdateBounds((float) worldx[count], (float) worldy[count]);
		
		}
	}
	public void SetWorldPoints(double u, double v, double r, double s) {
			maxx=-100000.0f;
			maxy=-100000.0f;
			minx=100000.0f;
			miny=100000.0f;		
			
			double cs = Math.cos(r);
			double sn = Math.sin(r);

			for (int count=0;count<numpoints; count++) {
				worldx[count]=(getX(count)*cs-getY(count)*sn)*s+u;
				worldy[count]=(getX(count)*sn+getY(count)*cs)*s+v;
				UpdateBounds((float) worldx[count], (float) worldy[count]);
			
			}

		
		}
	
	// update the minimum and maximum
	private void UpdateBounds(float x, float y) {
		if (x > maxx) maxx=x;
		if (y > maxy) maxy=y;
		if (x < minx) minx=x;
		if (y < miny) miny=y;
	}
	
	public boolean InBounds(float x, float y) {
		if ((x < maxx) && (x > minx) && (y < maxy) && (y > miny)) {
			return true;
		} else return false;
	}
	
	public void Erase() {
		numpoints=0;
		for (int count=0;count<MAX_POINTS; count++) {
			xmat[count]=0.0f;
			ymat[count]=0.0f;
		}
	}
	
	public void load(String sname) {
		// read until you find the right string.  
		try {
			Scanner sdata= new Scanner(new FileInputStream(sname));
			sdata.useDelimiter("\\s,\\s");   // has to end with space and then comma
			String testname=sdata.next();  // the name of the sketch, which I am not saving
	//		System.out.println(testname);
			numpoints=sdata.nextInt();  // number of points  -- should I do this by lines?
	//		System.out.print(numpoints);
	//		System.out.println("I opened up the sketch file");
			for (int count= 0; count < numpoints; count=count+2) {
			//	System.out.println(count);
				
				rcolor[count/2]=sdata.nextFloat();
		//		System.out.println(rcolor[count/2]);
				gcolor[count/2]=sdata.nextFloat();
		//		System.out.println(gcolor[count/2]);
				bcolor[count/2]=sdata.nextFloat();
		//		System.out.println(bcolor[count/2]);

				xmat[count]=sdata.nextFloat();
				ymat[count]=sdata.nextFloat();
				xmat[count+1]=sdata.nextFloat();
				ymat[count+1]=sdata.nextFloat();
				
				
				UpdateBounds(xmat[count], ymat[count]);
				UpdateBounds(xmat[count+1], ymat[count+1]);
				
			}
			sdata.close();
				
		} catch (FileNotFoundException fe) {
			
		}
	}
	
	public void CreateRectangle(float x, float y, float width, float height) {
		xmat[0]=x;
		ymat[0]=y;
		xmat[1]=x+width;
		ymat[1]=y;
		
		xmat[2]=x+width;
		ymat[2]=y;
		xmat[3]=x+width;
		ymat[3]=y+height;
		
		xmat[4]=x+width;
		ymat[4]=y+height;
		xmat[5]=x;
		ymat[5]=y+height;
		
		xmat[6]=x;
		ymat[6]=y+height;
		xmat[7]=x;
		ymat[7]=y;
		
		numpoints=8;
		for (int i=0; i < numpoints/2; i++) {
			rcolor[i]=1.0f;
			gcolor[i]=1.0f;
			bcolor[i]=1.0f;
		}
	}
	
	public void CreateCircle(float x, float y, float radi) {
		int numlines = 12;
		if (radi > 10f)
			numlines = 16;  // more round
		
		for (int i = 0; i < numlines; i++) {
			xmat[i*2]= x + (float)Math.cos(Math.PI*(i-1)/(numlines/2))*radi;
			ymat[i*2]= y + (float)Math.sin(Math.PI*(i-1)/(numlines/2))*radi;
			xmat[i*2+1] = x + (float)Math.cos(Math.PI*(i)/(numlines/2))*radi;
			ymat[i*2+1] = y + (float)Math.sin(Math.PI*(i)/(numlines/2))*radi;


			rcolor[i] = 1.0f;
			gcolor[i] = 1.0f;
			bcolor[i] = 1.0f;
			if (radi > 10f)
				width[i] = 2.0f;
		}
		
		numpoints = numlines*2;

	}
	
	public void AddGoTriangle(float x, float y, float radi) {
		int n = numpoints;
		xmat[n] = x+ (-0.866f * radi);
		ymat[n]	= y+ radi;
		xmat[n+1] = x+(-0.866f * radi);
		ymat[n+1] = y-radi;
		
		xmat[n+2] = x+(-0.866f * radi);
		ymat[n+2] = y-radi;
		xmat[n+3] = x+(0.866f * radi);
		ymat[n+3] = y;
		
		xmat[n+4] = x+(0.866f * radi);
		ymat[n+4] = y;
		xmat[n+5] =  x+(-0.866f * radi);
		ymat[n+5] = y+radi;		
		
		for (int i= numpoints/2; i < numpoints/2 +3;i++) {
			rcolor[i] = 0.1f;
			gcolor[i] = 0.8f;
			bcolor[i] = 0.1f;
			
		}
		
		numpoints+=6;
				
	}
	
	// UTILLITIES
	
	public boolean InCollisionBox(sketch sketch2) {
		if (!(maxy < sketch2.miny  || miny > sketch2.maxy || maxx  < sketch2.minx  || minx > sketch2.maxx) )
			return true;
		return false;
	}
	public boolean InCollisionBox(sketch sketch2, double buffer) {
		if (!(maxy+buffer < sketch2.miny  || miny-buffer > sketch2.maxy || maxx+buffer  < sketch2.minx  || minx-buffer > sketch2.maxx) )
			return true;
		return false;
	}
	
	public boolean InCollisionBox(double mmx, double mmy, double mnx, double mny) {
		if (!(maxy < mny  || miny > mmy || maxx  < mnx  || minx > mmx) )
			return true;
		return false;	
	}
	
	public boolean LineCollide(double x3, double y3, double x4, double y4) {
		double maxx=Math.max(x3, x4);
		double maxy=Math.max(y3, y4);
		double minx=Math.min(x3, x4);
		double miny=Math.min(y3, y4);
		
		if (InCollisionBox(maxx, maxy, minx, miny)) {
			// if we are in each other bouds, check for a line collison
			boolean test =false;
			for (int count1=0; count1< numpoints; count1+=2) {
				double x1 = worldx[count1];
				double x2 = worldx[count1+1];			
				double y1 = worldy[count1];
				double y2 = worldy[count1+1];	

				if (Math.max(x1,  x2) < Math.min(x3, x4)) {
					test=false;
				}
				else {
						if ((x1==x2) || (x3==x4)) break;  // otherwise we divided by zero below
						double a1 = (y1-y2)/(x1-x2); // slope of f1(x)= a1*x+b1=y
						double a2 = (y3-y4)/(x3-x4);  // slope of f2(x)= a2*x+b2 = y	
						double b1 = y1 - a1 * x1;
						double b2 = y3 - a2 * x3;
						if (a1==a2)  {
							test=false;  // parallel lines  = slope
						} else {
							double xa = (b2-b1)/(a1-a2);
							if ((xa < Math.max(Math.min(x1,  x2), Math.min(x3, x4))) ||
									(xa > Math.min(Math.max(x1, x2), Math.max(x3, x4)))) {
								test=false;
							}
								else return true;
							
							}
							
					
							
						}			
				}
		}
		return false;
	
	}
	
	public boolean LineCollideEst(double x3, double y3, double x4, double y4) {
		double maxx=Math.max(x3, x4);
		double maxy=Math.max(y3, y4);
		double minx=Math.min(x3, x4);
		double miny=Math.min(y3, y4);
		if (InCollisionBox(maxx, maxy, minx, miny)) {
			return true;
		}
		return false;
	}
	// perhaps return world thing, to show slope that collided, rotation > 0  equals a collision
	public boolean Collide(sketch sketch2) {
		
		// i need to know there x y in world coordinates
		// http://stackoverflow.com/questions/3838329/how-can-i-check-if-two-segments-intersect

		// reset bounds based on worldx, world y

		
		//if (!(maxy < sketch2.miny  || miny > sketch2.maxy || maxx  < sketch2.minx  || minx > sketch2.maxx) )
		//	return true;

		
	//	System.out.println("this ship x"+ minx + " y "+ miny + " x " + maxx + " y " + maxy);
	//	System.out.println("other ship x"+ sketch2.minx + " y "+ sketch2.miny + " x " + sketch2.maxx + " y " + sketch2.maxy);
			// then check to see if the segments collide


		if (InCollisionBox(sketch2)) {

			for (int count1=0; count1< numpoints; count1+=2) 
				for (int count2=0; count2 < sketch2.numpoints; count2+=2) {
					boolean test=true;
					double x1 = worldx[count1];
					double x2 = worldx[count1+1];
					double x3 = sketch2.worldx[count2];
					double x4 = sketch2.worldx[count2+1];
					
					double y1 = worldy[count1];
					double y2 = worldy[count1+1];
					double y3 = sketch2.worldy[count2];
					double y4 = sketch2.worldy[count2+1];
				
					if (Math.max(x1,  x2) < Math.min(x3, x4)) {
						test=false;
					}
					else {
						if ((x1==x2) || (x3==x4)) break;  // otherwise we divided by zero below
						double a1 = (y1-y2)/(x1-x2); // slope of f1(x)= a1*x+b1=y
						double a2 = (y3-y4)/(x3-x4);  // slope of f2(x)= a2*x+b2 = y
						double b1 = y1 - a1 * x1;
						double b2 = y3 - a2 * x3;
						if (a1==a2)  {
							test=false;  // paralel lines  = slope
						} else {
							double xa = (b2-b1)/(a1-a2);
							if ((xa < Math.max(Math.min(x1,  x2), Math.min(x3, x4))) ||
									(xa > Math.min(Math.max(x1, x2), Math.max(x3, x4)))) {
							  test=false;
							}
							else return true;
							
						}
							
					
							
					}
						
			}
		}
		
		
		return false;
	}
	
	public void CopyTo(sketch skt) {
		//public static int MAX_POINTS=44;
		skt.numpoints = numpoints;
		
		for (int i = 0; i < MAX_POINTS/2; i++) {
			skt.rcolor[i] = rcolor[i];
			skt.gcolor[i] = gcolor[i];
			skt.bcolor[i] = bcolor[i];
			skt.width[i] = width[i];
		}
		// world coordinates
		
		for (int i=0; i < MAX_POINTS; i++) {
			skt.xmat[i]= xmat[i];
			skt.ymat[i]= ymat[i];
		
			skt.worldx[i]= worldx[i];
			skt.worldy[i]= worldy[i];	
		
		}
		
		skt.maxx = maxx;
		skt.maxy = maxy;
		skt.minx = minx;
		skt.miny = miny;
	}
	public sketch() {


		numpoints=0;
		
		maxx=-1000.0f;
		maxy=-1000.0f;
		minx=1000.0f;
		miny=1000.0f;
	

	}
	public sketch(String filename) {
		load(filename);
	}
}
