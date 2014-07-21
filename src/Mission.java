import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;


public class Mission {
	/*
	 *  Include
	 *  pointer to sm / pm
	 *  Fill up the mission
	 *  Run it
	 *  End it when it is over
	 */
	
	ShipManager SM;
	ProjectileManager PM;
	IslandManager IM;
	UserInput ui;
	MissionData md;

	ProjectileManager PMmat[] = new ProjectileManager[25];
	ShipManager SMmat[] = new ShipManager[25];
	IslandManager IMmat[]= new IslandManager[25];
	
	
	

	int zoomid , pauseid;
	
	int userid;
	long ticks;
	long last_tick;
	
	// if you pause, you can set the last sim time
	public void SetLastTick(int newt) {
		last_tick=newt;
	}
	
	public int TestEndCase() {
		userid=SM.GetPlayerID();
		ship s= SM.GetShip(userid);
		switch (md.end_condition) {
			case MissionData.PLAYER_DIES_CONDITION:
				if (s==null) { 
					return 1;
				}
				else {
					if ((s.type != ship.stype.SHIP) && (s.type!=ship.stype.WRECK))
						return 2;
				}
 				break;		
				
		}
		// what if all the enemy are dead?
		for (int count=0; count < SM.scount; count++) {
			if ((SM.slist[count].type==ship.stype.SHIP) && (SM.slist[count].team !=0))
				return 0;  // some enemy alive
		}
		
		return 1;
		
	}
	
	void Init() {

		FillIslandManager();
		FillShipManager();
		
	}
	
	
	
	public void DrawWater(double x, double y, double t) {
		/*
		 * This isn't really water, it's just something in the background to show
		 * relative motion
		 */
		
		md.wavetime=md.wavetime+t;
		
		int w=((int) md.width);
		int h=((int) md.height);
		int startx=(int) (x-w/2);
		int starty=(int) (y-h/2);
		
		x=x-x%40;
		y=y-y%40;
		
		GL11.glBegin(GL11.GL_LINES);
		GL11.glColor3f(0.5f, 0.5f, 1.0f);
		GL11.glLineWidth(1.0f);


		for (int loopx=(int) x-w/2; loopx < x+w; loopx=loopx+40)
			for (int loopy=(int) y-h/2; loopy < y+h; loopy=loopy+40) {

				float u=((float) 3f*((float) Math.sin(((double)loopy)/400f+md.wavetime/10)));
				float v=((float) 3f*((float)Math.cos(((double)loopx)/400f+md.wavetime/10)));

				if (IM.MapValue((double)loopx, (double) loopy)==0) {
					GL11.glColor3f(0.5f, 0.5f, 1.0f);
				}
				else
				{
					if (IM.MapValue((double)loopx, (double) loopy)==2) {
						GL11.glColor3f(1.0f, 0.0f, 0.0f);
					}
					else
						GL11.glColor3f(0.0f, 1.0f, 0.0f);
				}
					GL11.glVertex3d(((float)loopx)-u, ((float)loopy)-v, 0f);				
					GL11.glVertex3d(((float)loopx+3)+u, ((float)loopy+3)+v, 0f);
				
			}
		
		GL11.glEnd();				
	}
	
	public void DrawWater2(double x, double y, double t) {
		/*
		 * This isn't really water, it's just something in the background to show
		 * relative motion
		 */
		
		md.wavetime=md.wavetime+t;
		
		int w=((int) md.width);
		int h=((int) md.height);
		int startx=(int) (x-w/2);
		int starty=(int) (y-h/2);
		
		x=x-x%40;
		y=y-y%40;
		
		GL11.glBegin(GL11.GL_LINES);
		GL11.glColor3f(0.5f, 0.5f, 1.0f);
		GL11.glLineWidth(1.0f);
		int oddcount=0;
		for (int loopx=(int) x-w/2; loopx < x+w; loopx=loopx+40)
			for (int loopy=(int) y-h/2; loopy < y+h; loopy=loopy+40) {
				/*
				float u=((float) 3f*((float) Math.sin(((double)loopy)/400f+md.wavetime/10)));
				float v=((float) 3f*((float)Math.cos(((double)loopx)/400f+md.wavetime/10)));
				*/
				
				float u=0;
				float v=0;
				float d=0;
				oddcount++;
				if (IM.MapValue((double)loopx, (double) loopy)==0) {

					d=((float) (0.8* ((int) (md.wavetime+oddcount))%50));
	
				}
				if (oddcount>10)
					oddcount=0;
				
				u=((float) IM.GetCurrentX((double) loopx, (double) loopy)*d);
				v=((float) IM.GetCurrentY((double) loopx, (double) loopy)*d);

				if (IM.MapValue((double)loopx, (double) loopy)==0) {
					GL11.glColor3f(0.25f, 0.25f, 0.5f);
				}
				else
				{
					if (IM.MapValue((double)loopx, (double) loopy)==2) {
						GL11.glColor3f(0.5f, 0.0f, 0.25f);
					}
					else
						GL11.glColor3f(0.0f, 0.5f, 0.0f);
				}
					GL11.glVertex3d(((float)loopx)+u, ((float)loopy)+v, 0f);				
					GL11.glVertex3d(((float)loopx)+u+3, ((float)loopy)+v, 0f);
				
			}
		
		GL11.glEnd();				
	}

	
	public void FillShipManager() {
		
		WorldThing wt;
		int id;
		ship s;
		wt=new WorldThing(0.0d, 0.0d, Math.PI);
		
	
		SM.SetPM(PM);
		
		for (int loop = 0; loop < md.ship_number; loop++) {
			boolean blocked;
			do {
				blocked=false;
				wt.xpos=Math.random()*md.width*2-md.width;
				wt.ypos=Math.random()*md.height*2-md.height;
				for (int i=-1; i < 2; i++)
					for (int j=-1; j<2; j++)
						if (IM.MapValue(wt.xpos+ i*50d, wt.ypos+j*50d)!=0)
							blocked=true;
				// check to see if another ship is in range
				if (!blocked)
					blocked=SM.AnyShipAt(wt);
			} while(blocked);  // can't be two close to the center sqrt(20000)= 141 distance
			wt.rotation=Math.random()*Math.PI*2;


			id=SM.AddShip(wt);
			s=SM.GetShip(id);
			if (s != null) {
				if (loop==0) {
					s.load("destroyer.txt");
				}
				else {
					
					if (loop == 1) {
						s.load("destroyer2.txt");
						// s.load("testboat.txt");
					} else {
					
					if (loop <15 ) {
						s.load("ptboat.txt");
					}
					else s.load("destroyer2.txt");
					}
				}

				s.SetSpeed(1d);

				s.SetTeam(2, md); // something else
				s.AttachAI(SM, IM, md);
				if (loop==0) {
					// make user ship
					userid=id;
					s.SetTeam(0, md);

					
				}
			}
		}
	}	
	
	public void FillIslandManager() {
		for (int count=0; count < md.island_number; count++) {

			IM.PlaceRandomIsland(Math.random()*50d+25d);  //  random size map, but it can't be too small.  
		}

		IM.FillMap2();

		// Added for fun
		for (int i=0; i < 25; i++) {
			IMmat[i]=new IslandManager(md);
			for (int count=0; count < md.island_number; count++) {

				IMmat[i].PlaceRandomIsland(Math.random()*50d+25d);  //  random size map, but it can't be too small.  
			}

			IMmat[i].FillMap2();
		}
		
		
	}
	
	
	void SetUpDisplay(){
		ui.UpdateCursor();
		
		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glLoadIdentity();
		GL11.glOrtho(0, md.win_width, 0, md.win_height, 1, -1);
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		
		ui.SetScale(md.win_width, md.win_height, md.width, md.height);
		
		ticks=1;
		last_tick = System.currentTimeMillis();	
	}
	
	public int DisplayLoop() {
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT); 
		
		// set the color of the quad (R,G,B,A)
		GL11.glColor3f(0.5f,0.5f,1.0f);

		// ui.pollInput(win_width, win_height);
		double time = ticks / 100d;
		double centerx=0; 
		double centery=0;
		PM.Simulate(time);
		SM.Simulate(time, IM, 0, 0);
		IM.Update(SM, PM, userid, time);
		ship s=SM.GetShip(userid);
		if (s!=null) {
			centerx=s.xpos;
			centery=s.ypos;
			GL11.glMatrixMode(GL11.GL_PROJECTION);
			GL11.glLoadIdentity();
			GL11.glOrtho(s.xpos-md.width/2, md.width+s.xpos-md.width/2, s.ypos-md.height/2, md.height+s.ypos-md.height/2, 1, -1);	
			GL11.glMatrixMode(GL11.GL_MODELVIEW);
		}
			
	
		SM.HandleUI(ui, userid, time);
		
		// draw the background
		ship su=SM.GetShip(userid);
		if (su!=null) {

			DrawWater2(su.xpos, su.ypos, time);
		}
		
		
		WorldThing min=new WorldThing();
		WorldThing max=new WorldThing();
		
		min.xpos = centerx - md.win_width*2d/3d;
		min.ypos = centery - md.win_height*2d/3d;
		max.xpos = centerx + md.win_width*2d/3d;
		max.ypos = centery + md.win_height*2d/3d;
		
		IM.Draw();
		SM.Draw(min, max);
		PM.Draw(centerx, centery, 0);
		ui.Draw(centerx, centery);
		
		
		ui.pollInput(time, md.win_width, md.win_height, md.width, md.height);

		Display.update();
		
		
		ticks=System.currentTimeMillis()-last_tick;
		if (ticks>500)
			ticks=100;
		last_tick=System.currentTimeMillis();
		return FourExample.SIM_SCREEN;
	}
	

	
	Mission(MissionData mdin) {
		md=mdin;

		ui = new UserInput(md);
		PM = new ProjectileManager(md);
		SM = new ShipManager(md);
		IM = new IslandManager(md);

		ticks=1;
		
		


	}

}
