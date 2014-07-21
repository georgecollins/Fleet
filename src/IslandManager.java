import java.util.Set;
import java.util.*;
import javax.swing.text.html.HTMLDocument.Iterator;

import org.lwjgl.opengl.GL11;

public class IslandManager extends ObjectManager {
	/*
	 * Island Manager manages an array of Islands
	 * Adds them,
	 * tests for collision,
	 * draws them.
	 */
	island 	islands[]; 
	sketch mbox;
	
	int island_count;


	
	int map[][];  // could be byte
	double currentx[][];
	double currenty[][];
	
	// pathing decompisition
	PathData path_map[][];
	
	
	static public int MAX_MAP = 250; //125;  // max & y =20
	static public double MAP_SCALE = 50.d;
	static public int PATH_WIDTH = 5;  // how many map nodes make a path node
	static public int MAX_ISLANDS = 40;
	static public int NO_ISLAND = -1;
	static public int PORT_SIZE= 135;
	
	static public float SCALE_MULTIPLE = 5f;  // to scale islands up and down uniformly
	
	
	
		public void FillMap2() {
		// add a vortex current
		for (int i=0; i< MAX_MAP; i++)
			for (int j=0; j<MAX_MAP;j++) {
				map[i][j]=0;
				double x= (i-(MAX_MAP/2))*MAP_SCALE + offsetx;
				double y= (j-(MAX_MAP/2))*MAP_SCALE + offsety;
				double outx=x-offsetx;
				double outy=y-offsety;
				double mag=Math.sqrt(outx*outx+outy*outy);
				if (mag < 0.001d) mag=0.001d;  // don't divide by zero
				currentx[i][j]=0;// (-1d)*outx/mag;
				currenty[i][j]=0;//(-1d)*outy/mag;				
			}	
		// go through the list of active islands
		// on the map array go from there start to there finish
		// check for collisions, mark them
		
		for (int count=0;count < island_count; count++) {
			islands[count].isketch.SetWorldPoints(islands[count].xpos, islands[count].ypos, islands[count].rotation, SCALE_MULTIPLE);
			int starti = (int) ((islands[count].isketch.minx- offsetx)/MAP_SCALE+MAX_MAP/2)-1;
			int endi = (int) ((islands[count].isketch.maxx- offsetx)/MAP_SCALE+MAX_MAP/2)+1;
			int startj = (int) ((islands[count].isketch.miny-offsety)/MAP_SCALE+MAX_MAP/2)-1;
			int endj = (int) ((islands[count].isketch.maxy-offsety)/ MAP_SCALE+MAX_MAP/2)+1;
			if (starti < 0) starti=0;  // I shoudl throw up some kind of warnign in any of the four cases below, something is wrong
			if (endi > MAX_MAP) endi=MAX_MAP;
			if (startj < 0) startj=0;
			if (endj > MAX_MAP) endj=MAX_MAP;

			for (int i=starti; i < endi; i++) 
				for (int j=startj; j< endj; j++){
					
					double x= (i-(MAX_MAP/2))*MAP_SCALE + offsetx;
					double y= (j-(MAX_MAP/2))*MAP_SCALE + offsety;
					mbox.SetWorldPoints(x, y, 0.0f);  //scale 
					if (islands[count].isketch.Collide(mbox)) {
						map[i][j]=2;
						double outx=x-islands[count].xpos;
						double outy=y-islands[count].ypos;
						double mag=Math.sqrt(outx*outx+outy*outy);
						if (mag < 0.1d) mag=0.1d;  // don't divide by zero
						for (int q=-1; q < 2; q++)
							for (int r=-1; r < 2; r++) 
							  if ((i+q > -1) && (i+q< MAX_MAP) && (j+r> -1) && (j+r < MAX_MAP)) {
									currentx[i+q][j+r]=outx/mag*3d;  // was 3d
									currenty[i+q][j+r]=outy/mag*3d;	 // ditto	
							}
						
					}	
			}
			
		}
			
		// new flood fill
		
		for (int count=0; count< island_count; count++) {
			int i = (int) ((islands[count].xpos-offsetx)/MAP_SCALE+MAX_MAP/2);
			int j = (int) ((islands[count].ypos- offsety)/MAP_SCALE+ MAX_MAP/2);
			map[i][j]=1;
			int starti = (int) ((islands[count].isketch.minx- offsetx)/MAP_SCALE+MAX_MAP/2)-1;
			int endi = (int) ((islands[count].isketch.maxx- offsetx)/MAP_SCALE+MAX_MAP/2)+1;
			int startj = (int) ((islands[count].isketch.miny-offsety)/MAP_SCALE+MAX_MAP/2)-1;
			int endj = (int) ((islands[count].isketch.maxy-offsety)/ MAP_SCALE+MAX_MAP/2)+1;
			if (starti < 0) starti=0;  // I shoudl throw up some kind of warnign in any of the four cases below, something is wrong
			if (endi > MAX_MAP) endi=MAX_MAP;
			if (startj < 0) startj=0;
			if (endj > MAX_MAP) endj=MAX_MAP;
			boolean changed=true;
			while (changed==true) {
				changed=false;
			
			for (int u=starti; u < endi; u++) 
				for (int v=startj; v< endj; v++){
						if ((u>1) && (v>1) && (u<MAX_MAP-1) && (v< MAX_MAP-1) && (map[u][v]==0)) {
							if (map[u-1][v]==1) {
								map[u][v]=1;
								changed=true;
							}
							if (map[u+1][v]==1) {
								map[u][v]=1;
								changed=true;
							}
							if (map[u][v-1]==1) {
								map[u][v]=1;
								changed=true;
							}
							if (map[u][v+1]==1) {
								map[u][v]=1;
								changed=true;
										
							}
								
							
						}
					}
			}
		}
	}
	
	
	public void FillPathMap() {
		// fill the path data array
		// init every node of the array
		// check to see if it is clear, and has a path north south east west
		// this is supposed to make the later A* faster
		for (int i=0; i < MAX_MAP/PATH_WIDTH; i++)
			for (int j=0; j < MAX_MAP/PATH_WIDTH; j++) {
				path_map[i][j] = new PathData(this);
				
				path_map[i][j].SetXY(offsetx+ (i-MAX_MAP/PATH_WIDTH/2) * MAP_SCALE*PATH_WIDTH, offsety + (j-MAX_MAP/PATH_WIDTH/2) * MAP_SCALE*PATH_WIDTH);
				path_map[i][j].IsClear();
				int d=0;
				for (int dx=-1; dx < 2; dx++)
					for (int dy=-1; dy < 2; dy++) {
						if (!((dx==0) && (dy==0))) {
							path_map[i][j].passable[d]=path_map[i][j].ClearDirection(dx, dy);
						}
					}
			}
		// this is a dumb way to have these pointers but for now I am doing it
		for (int i=0; i < MAX_MAP/PATH_WIDTH; i++)
			for (int j=0; j < MAX_MAP/PATH_WIDTH; j++) {
				int dcount = 0;
				for (int dirx=i-1; dirx < i+2; dirx++)
					for (int diry=j-1; diry< j+2; diry++) {
						if ((dirx!=i) || (diry!=j)) {
							if ((dirx>-1) && (dirx < MAX_MAP/PATH_WIDTH) && (diry>-1) && (diry < MAX_MAP/PATH_WIDTH))
								path_map[i][j].neighbors[dcount]=path_map[dirx][diry];
							dcount++;
						}
					}
			}
	
	}
	
	public boolean InBounds(double x, double y) {
		if (((x-offsetx) >  md.world_width/2) || 
			 ((x-offsetx) < -md.world_width/2) ||
			 ((y-offsety) > md.world_height/2) ||
			 ((y-offsety) < -md.world_height/2))	{
			return false;
		} 
		return true;
	}
	
	public int MapValue(double x, double y) {
		int i= (int)((x-offsetx)/MAP_SCALE+(MAX_MAP/2));
		int j= (int) ((y-offsety)/MAP_SCALE+(MAX_MAP/2));
		if ((i<0) || (i>MAX_MAP-1))
			return 0;
		if ((j<0) || (j>MAX_MAP-1))
			return 0;


		return map[i][j];
	}
	
	
	public int PathMapValue(WorldThing wt) {
	
		int i= (int) (wt.xpos-offsetx/MAP_SCALE*PATH_WIDTH + MAX_MAP/PATH_WIDTH/2);
		int j= (int) (wt.ypos-offsety/MAP_SCALE*PATH_WIDTH + MAX_MAP/PATH_WIDTH/2);
		return path_map[i][j].clear;
	}
	
	public double GetCurrentX(double x, double y) {
		int i= (int)((x-offsetx)/MAP_SCALE+(MAX_MAP/2));
		int j= (int) ((y-offsety)/MAP_SCALE+(MAX_MAP/2));
		if ((i<0) || (i>MAX_MAP-1))
			return 0;
		if ((j<0) || (j>MAX_MAP-1))
			return 0;


		return currentx[i][j];	
	}
	public double GetCurrentY(double x, double y) {
		int i= (int)((x-offsetx)/MAP_SCALE+(MAX_MAP/2));
		int j= (int) ((y-offsety)/MAP_SCALE+(MAX_MAP/2));
		if ((i<0) || (i>MAX_MAP-1))
			return 0;
		if ((j<0) || (j>MAX_MAP-1))
			return 0;

		return currenty[i][j];	
	}	
	
	public int NewIsland(double x, double y, double r, String fname) {
		
		// this assumes islands are never destroyed
		if (island_count< MAX_ISLANDS) {
			islands[island_count]=new island(x+offsetx, y+offsety, r, fname);
			islands[island_count].SetID(md.GetUniqieID(md.ISLAND));

			island_count++;
			return islands[island_count-1].id; 
		}
		else 
			return NO_ISLAND;
	}
	void MakePortSketch(island i) {
		// add a psketch for island i
		i.psketch = new sketch();
		i.psketch.numpoints=10;
		
		// to save typing
		float x = (float) i.portx;
		float y = (float) i.porty;
		i.psketch.xmat[0]= x- 5f;
		i.psketch.ymat[0]= y;
		i.psketch.xmat[1]= x +10f;
		i.psketch.ymat[1]=y;
		
		i.psketch.xmat[2]=x;
		i.psketch.ymat[2]=y-5f;
		i.psketch.xmat[3]=x;
		i.psketch.ymat[3]=y+5f;
		
		i.psketch.xmat[4]=x + 10f;
		i.psketch.ymat[4]=y - 3f;
		i.psketch.xmat[5]=x + 10f;
		i.psketch.ymat[5]=y + 3f;
		
		i.psketch.xmat[6]=x+ 10f;
		i.psketch.ymat[6]=y-3f;
		i.psketch.xmat[7]=x+ 7f;
		i.psketch.ymat[7]=y-5f;
		
		i.psketch.xmat[8]=x + 10f;
		i.psketch.ymat[8]=y + 3f;
		i.psketch.xmat[9]=x + 7f;
		i.psketch.ymat[9]=y + 5f;
		
		for (int count=0; count< i.psketch.numpoints/2; count++) {
			i.psketch.rcolor[count]=0.7f;
			i.psketch.gcolor[count]=0.7f;
			i.psketch.bcolor[count]=0.5f;
		}
		
	}
	
	/*
	beta = -angle * np.pi/180.0
		    sin_beta = np.sin(beta)
		    cos_beta = np.cos(beta)
		    alpha = np.radians(np.r_[0.:360.:1j*(360*k+1)])
		 
		    sin_alpha = np.sin(alpha)
		    cos_alpha = np.cos(alpha)
		    
		    pts[:, 0] = x + (a * cos_alpha * cos_beta - b * sin_alpha * sin_beta)
		    pts[:, 1] = y + (a * cos_alpha * sin_beta + b * sin_alpha * cos_beta)
	*/
	// hasport
	public int NewRandomIsland(double x, double y, double s, int has_port) {
		if (island_count < MAX_ISLANDS) {
			double sx=(0.5d+Math.random()/2d)*s;
			double sy=(0.5d+Math.random()/2d)*s;
			int first_point;
			// fiogure out eliptic ratio for sx, sy
			islands[island_count]=new island();
			islands[island_count].type = island.iType.ISLAND;
			islands[island_count].xpos = x+offsetx;
			islands[island_count].ypos = y+offsety;
			islands[island_count].isketch = new sketch();

			if (has_port==1) {
				islands[island_count].isketch.numpoints=42;
				islands[island_count].has_port=has_port;
			}
			else
				islands[island_count].isketch.numpoints=40;
				
			for (int count=0; count< 40; count=count+2) {
				// fill fill
				islands[island_count].isketch.xmat[count] = (float) (Math.cos(((double)(count/2)+1)/10d*Math.PI) * sx);
				islands[island_count].isketch.ymat[count] = (float) (Math.sin(((double)(count/2)+1)/10d*Math.PI) * sy);	
				islands[island_count].isketch.xmat[count+1] = (float) (Math.cos(((double)(count/2)+2)/10d*Math.PI) * sx);
				islands[island_count].isketch.ymat[count+1] = (float) (Math.sin(((double)(count/2)+2)/10d*Math.PI) * sy);				
			}
			
			
			if (has_port==1) {

				islands[island_count].isketch.xmat[36] = (float) (Math.cos(19d/10d*Math.PI) *sx);
				islands[island_count].isketch.ymat[36] = (float) (Math.sin(19d/10d*Math.PI) *sy);
				islands[island_count].isketch.xmat[37] = (float) (Math.cos(1.9d*Math.PI) *sx -sx/2d+3);
				islands[island_count].isketch.ymat[37] = (float) (Math.sin(1.9d*Math.PI) *sy);
				
				islands[island_count].isketch.xmat[38] = (float) (Math.cos(2.1d*Math.PI) *sx);
				islands[island_count].isketch.ymat[38] = (float) (Math.sin(2.1d*Math.PI) *sy);
				islands[island_count].isketch.xmat[39] = (float) (Math.cos(2.1d*Math.PI) *sx -sx/2d);
				islands[island_count].isketch.ymat[39] = (float) (Math.sin(2.1d*Math.PI) *sy);
				
				islands[island_count].isketch.xmat[40] = (float) (Math.cos(1.9d*Math.PI) *sx -sx/2d+3);
				islands[island_count].isketch.ymat[40] = (float) (Math.sin(1.9d*Math.PI) *sy);
				islands[island_count].isketch.xmat[41] = (float) (Math.cos(2.1d*Math.PI) *sx -sx/2d);
				islands[island_count].isketch.ymat[41] = (float) (Math.sin(2.1d*Math.PI) *sy);
	
				
				islands[island_count].portx = ((Math.cos(2.1d*Math.PI) *sx -sx/2d) + (Math.cos(1.9d*Math.PI) *sx))/2d;
				islands[island_count].porty = ((Math.sin(1.9d*Math.PI) *sy)+ (Math.sin(2.1d*Math.PI) *sy))/2d;				
				
				MakePortSketch(islands[island_count]);
				
				for (int count = 1; count < 35; count=count+2) {
					float u= (float) (Math.random()*s/3d-s/6d);
					float v= (float) (Math.random()*s/3d-s/6d);
					islands[island_count].isketch.xmat[count] +=u; 
					islands[island_count].isketch.ymat[count] +=v; 
					islands[island_count].isketch.xmat[count+1] += u; 
					islands[island_count].isketch.ymat[count+1] +=v; 
				
				}
			}
			else {
				for (int count = 1; count < 39; count=count+2) {
					float u= (float) (Math.random()*s/3d-s/6d);
					float v= (float) (Math.random()*s/3d-s/6d);
					islands[island_count].isketch.xmat[count] +=u; 
					islands[island_count].isketch.ymat[count] +=v; 
					islands[island_count].isketch.xmat[count+1] += u; 
					islands[island_count].isketch.ymat[count+1] +=v; 
				
				}
			}
			for (int count=0; count<  islands[island_count].isketch.numpoints/2; count++) {
				islands[island_count].isketch.rcolor[count]=0.75f;
				islands[island_count].isketch.gcolor[count]=0.75f;
				islands[island_count].isketch.bcolor[count]=0.0f;				
			}
			
			islands[island_count].rotation=Math.random()*2*Math.PI;
			
			
			islands[island_count].id = md.GetUniqieID(md.ISLAND);
			island_count++;
			return islands[island_count-1].id;
		}
		else
			return NO_ISLAND;
	}
	
	public int PlaceRandomIsland(double s) {
		// the x, y
		int port =0;
		if (s>PORT_SIZE) port =1;
		int id=NewRandomIsland(0, 0, s, port);
		// now test if the island is in a clear place
		
		island newisland=GetIsland(id);
		if (newisland==null)
			return 0;  // no island
		double x=0;
		double y=0;
		boolean blocked= true;
		while (blocked) {
			blocked = false;
			
			double xrange=md.world_width*0.7d;  // give it a buffer so it doesn't wrap segments was 0.8 in many worlds
			double yrange=md.world_height*0.7d;

			x = Math.random()*xrange-xrange/2 + offsetx;
			y = Math.random()*yrange-yrange/2 + offsety;
			
			newisland.isketch.SetWorldPoints(x, y, newisland.rotation, SCALE_MULTIPLE);
			for (int count = 0; count < island_count; count++) {
				islands[count].isketch.SetWorldPoints(islands[count].xpos, islands[count].ypos, islands[count].rotation, SCALE_MULTIPLE);
		
				if ((newisland.isketch.maxx> offsetx+md.world_width/2) || (newisland.isketch.minx <offsetx-md.world_width/2) || (newisland.isketch.maxy > offsety + md.world_height/2)  || (newisland.isketch.miny<offsety - md.world_height/2)) {
					blocked=true;
				} else
					// 100d is an extra collision buffer to allow shipping lanes.  
					if ((newisland.id!= islands[count].id) && (newisland.isketch.InCollisionBox(islands[count].isketch, 200d))) {		// was 100d buffer

						blocked= true;
					}
				
				
			}
			
		}
		newisland.xpos=x;
		newisland.ypos=y;
		newisland.SetWorldPortPoints(SCALE_MULTIPLE);  // set the port point in world coordinates
		return id;
	}
	
	public int TestCollision(sketch skt) {
		for (int loop=0; loop< island_count; loop++) {
				if (islands[loop].Collide(skt)) {
					//System.out.println("Island collision with id"+ islands[loop].id);
					return islands[loop].id;
				}
		}
		return 0;
	}
	
	public boolean LineCollides(double x1, double y1 , double x2, double y2) {
		for (int i=0; i < island_count; i++) {
			if (islands[i].isketch.LineCollide(x1, y1, x2, y2))
				return true;
		}
		return false;
	}
	
	public PathData GetClosestOpen(double x, double y) {
		double bestdist=100000*100000;
		PathData retval=null;
		for (int i=0; i < MAX_MAP/PATH_WIDTH; i++)
			for (int j=0; j< MAX_MAP/PATH_WIDTH; j++)
				if ((path_map[i][j].clear==PathData.PASSABLE) && (!LineCollides(x, y, path_map[i][j].x, path_map[i][j].y))) {
					double dist=(path_map[i][j].x-x)*(path_map[i][j].x-x)+(path_map[i][j].y-y)*(path_map[i][j].y-y);
					if (dist < bestdist) {
						bestdist=dist;
						retval=path_map[i][j];
					}
				}
	
		return retval;	
	}
	
	public void ClearPathCosts() {
		for (int i=0; i < MAX_MAP/PATH_WIDTH; i++) 
			for (int j=0; j < MAX_MAP/PATH_WIDTH; j++){
				path_map[i][j].bestdist=-1;
				path_map[i][j].bestdir=PathData.MAX_DIRECTIONS;
			 
		}
 	}
	
	public Path GetPath(double x1, double y1, double x2, double y2) {
		Path p = new Path();
		// Fail path-- if I can't make a path
		p.xmat[0]=x1;
		p.ymat[0]=y1;
		p.numpoints=1;
		
		ClearPathCosts();
		// start = get the closest passable node to x1, y1
		// end = get the closet passable node
		// fill out.. not very fase but we can do A* later
		// if we fill out and they don't connect ?? Fuck off?
		// When they do fill out make a path??
		List<PathData> open= new ArrayList<PathData>();
		List<PathData> closed = new ArrayList<PathData>();
		
		PathData start=GetClosestOpen(x2, y2);  // we actually search backwards
		if (start==null)  // no close open
			return p;  // fail path
		start.bestdist=0;
		PathData goal = GetClosestOpen(x1, y1);
		if (goal==null)  // no close open
			return p;  // fail path
	
	//	Set test = new Set();
		open.add(start);
		java.util.Iterator<PathData> it = open.iterator();
		
		
		
		int steps=0;
		
		PathData bestnode=null;
		double bestdist=100000;
		while ((bestnode!=goal) && (steps< 8*MAX_MAP)) {
			
			bestdist=100000*100000;
			it=open.iterator();
			while (it.hasNext()) {
				
				PathData anopen=it.next();
				//

				if ((anopen.bestdist>-1) && (anopen.bestdist < bestdist)) {
					bestnode=anopen;
					bestdist=anopen.bestdist;
				}
			}
			int jj=0;
			jj++;
			// now open all the nodes adjacent to the node
			for (int i=0; i< PathData.MAX_DIRECTIONS; i++) {
				if ((bestnode.neighbors[i]!=null) && (bestnode.neighbors[i].clear==PathData.PASSABLE)){
					if (bestnode.neighbors[i].bestdist==-1)
						open.add(bestnode.neighbors[i]);
					double d = Math.sqrt(bestnode.distsq(bestnode.neighbors[i])) +bestnode.bestdist;
					if ((d < bestnode.neighbors[i].bestdist) || (bestnode.neighbors[i].bestdist==-1)) {
						bestnode.neighbors[i].bestdist=d;
						bestnode.neighbors[i].bestdir=i;
						bestnode.neighbors[i].bestfrom=bestnode;
					}
				}

				
			}
			open.remove(bestnode);
			steps++;

		}
		 // now build a path in reverse from goal (which == bestnode)
		int count=0;
		p.xmat[count]=x1;
		p.ymat[count]=y1;
		count++;
		while ((bestnode!=start) && (count < 47)) {
			p.xmat[count]=bestnode.x;
			p.ymat[count]=bestnode.y;
			bestnode=bestnode.bestfrom;
			count++;
		}
		p.xmat[count]=start.x;
		p.ymat[count]=start.y;
		count++;
		p.xmat[count]=x2;
		p.ymat[count]=y2;
		count++;
		p.numpoints=count;
		if (steps== 8*MAX_MAP) p=null;
		return p;
	}
	
	
	public island AtPort(WorldThing wt) {
		// am I near a port, if so return the island I am very close to its port
		
		for (int i = 0; i < island_count; i++) 
			if (islands[i].has_port!=0) {
					//&& (islands[i].DistanceSq(wt) < 5000))
				if (((wt.xpos-islands[i].world_portx)*(wt.xpos-islands[i].world_portx)+
						(wt.ypos-islands[i].world_porty)*(wt.ypos-islands[i].world_porty))< 5000)
						return islands[i];
			}	
		return null;
	}
	
	public island FindClosestPort(WorldThing wt) {
		island isl = null;
		double bestdist = 199999999;
		for (int i = 0; i < island_count; i++) 
			if (islands[i].has_port!=0) {
				double dist = islands[i].DistanceSq(wt);
				if (dist < bestdist) {
					bestdist = dist;
					isl= islands[i];
				}
			}
		return isl;
	}
	
	public island FindFarthestPort(WorldThing wt) {
		island isl = null;
		double bestdist = 1;
		for (int i = 0; i < island_count; i++) 
			if (islands[i].has_port!=0) {
				double dist = islands[i].DistanceSq(wt);
				if (dist >  bestdist) {
					bestdist = dist;
					isl= islands[i];
				}
			}
		return isl;
	}
	public int Update(ShipManager sm, ProjectileManager pm, int userid, double t) {
		int port_of_call=0;
		for (int loop=0; loop < island_count; loop++) {
			int port=sm.HandleIslandCollision(islands[loop], userid);
			if (port!=0)
				port_of_call=port;
			pm.HandleIslandCollision(islands[loop]);
		}
		return port_of_call;
	}
	
	public void Draw() {
		for (int loop=0; loop<island_count; loop++) {
			// now draw something.
			islands[loop].draw(SCALE_MULTIPLE, md);
		}

	}
	
	
	public island GetIsland(int id) {
		for (int count=0; count < island_count; count++) {
			if (islands[count].id==id)
				return islands[count];
		}
		return null;
	}
	
	int PortCount() {
		// how many ports are here?
		int port_count=0;
		for (int count=0; count < island_count; count++) {
			if (islands[count].has_port>0) port_count++;
		}
		return port_count;
	}
	IslandManager(MissionData mdin) {
		md= mdin;
		island_count=0;
		islands=new island[MAX_ISLANDS];
		map=new int[MAX_MAP][MAX_MAP];
		
		path_map = new PathData[MAX_MAP/PATH_WIDTH][MAX_MAP/PATH_WIDTH];  // 32 by 32?
		
		currentx = new double[MAX_MAP][MAX_MAP];
		currenty = new double[MAX_MAP][MAX_MAP];
		
		mbox = new sketch();
		mbox.numpoints=14;
		float ms = (float) (MAP_SCALE/2 *1.2d);
		mbox.xmat[0]= -ms;
		mbox.ymat[0]= -ms;
		mbox.xmat[1]= ms;
		mbox.ymat[1]= ms;
		
		mbox.xmat[2]= -ms;
		mbox.ymat[2]= ms;
		mbox.xmat[3]= ms;
		mbox.ymat[3]= -ms;
		
		mbox.xmat[4] = ms;
		mbox.ymat[4] = ms;
		mbox.xmat[5] = ms;
		mbox.ymat[5] = -ms;

		mbox.xmat[6] = -ms;
		mbox.ymat[6] = ms;
		mbox.xmat[7] = -ms;
		mbox.ymat[7] =  -ms;
	
		mbox.xmat[8]= -ms;
		mbox.ymat[8]= -ms;
		mbox.xmat[9]= ms;
		mbox.ymat[9]= -ms;
		
		mbox.xmat[10]= ms;
		mbox.ymat[10]= -ms;
		mbox.xmat[11]= ms;
		mbox.ymat[11]= ms;
		
		mbox.xmat[12]= ms;
		mbox.ymat[12]= 0;
		mbox.xmat[13]= -ms;
		mbox.ymat[13]= 0;
		
		mbox.xmat[12]= 0;
		mbox.ymat[12]= ms;
		mbox.xmat[13]= 0;
		mbox.ymat[13]= -ms;


	}
	
}
