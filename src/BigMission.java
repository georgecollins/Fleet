import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;


public class BigMission {
	// this is like mission, but bigger, with 25 areas
	
	ProjectileManager PMmat[][];
	ShipManager SMmat[][]; 
	IslandManager IMmat[][];
	PickupManager PKmat[][];
	FleetCommand fc;
	UserInput ui;
	MissionData md;

	int user_segmentx;
	int user_segmenty;
	long ticks;
	long last_tick;	
	
	int game_state;
	static final int GAME_STATE_INTRO = 0;
	static final int GAME_STATE_LOAD = 1;
	static final int GAME_STATE_SEA = 2;
	static final int GAME_STATE_PORT = 3;
	static final int GAME_STATE_OBJECTIVE = 4;
	static final int GAME_STATE_END_SCREEN = 5;
	
	static final int QUIT_GAME = 6;
	static final int RESUME_GAME = 7;
	
	
	// load screen data
	int loadx, loady;
	ship load_screen_ship;
		
	PortDisplay port_disp;
	ObjectiveDisplay obj_disp;

	island port_island;


	double port_shut_down_timer;

	int last_dock;  // actually sort of game data
	int namecount=0;	

	
	
	// I should move this function to MissionData md
	public double GetOffsetX(int segment) {
		double offset= (segment-((int)md.segment_width/2))*md.world_width;
		return offset;
	}
	
	public double GetOffsetY(int segment) {
		double offset= (segment-((int)md.segment_height/2))*md.world_height;
		return offset;
	}
	
	
	void IntroScreen() {
		sketch wbox, abox, sbox, dbox;
		sketch mouse;
		RayText rt=new RayText();
		rt.DrawScaleText(100, (int) (md.win_height*0.9f), "FLAT FLEET PROTOTYPE", 3.0d);
		
		float box_width = md.win_width/20f;
		float box_spacing = md.win_width/18f;
		int gutter = (int) (box_spacing/4);
		int keydispx = md.win_width/10;
		int keydispy= (int)(md.win_height*0.5f);
		
		rt.DrawScaleText(keydispx+(int)(box_spacing), (int) (keydispy+ box_spacing*2+gutter), "CONTROLS", 2.0d);
		wbox = new sketch();
		wbox.CreateRectangle(keydispx + box_spacing, keydispy, box_width, box_width);
		wbox.drawit();
		
		abox = new sketch();
		abox.CreateRectangle(keydispx+ box_spacing*2, keydispy + box_spacing, box_width, box_width);
		abox.drawit();
		rt.DrawScaleText((int)(keydispx+ box_spacing*2+gutter), (int)(keydispy + box_spacing+gutter),"W", 2.0d);
		
		wbox = new sketch();
		wbox.CreateRectangle(keydispx + box_spacing, keydispy, box_width, box_width);
		wbox.drawit();
		rt.DrawScaleText((int)(keydispx+ box_spacing+ gutter), (int)(keydispy +gutter),"A", 2.0d);

		sbox = new sketch();
		sbox.CreateRectangle(keydispx + box_spacing*2, keydispy, box_width, box_width);
		sbox.drawit();
		rt.DrawScaleText((int)(keydispx+ box_spacing*2+ gutter), (int)(keydispy +gutter),"S", 2.0d);
		
		dbox = new sketch();
		dbox.CreateRectangle(keydispx + box_spacing*3, keydispy, box_width, box_width);
		dbox.drawit();
		rt.DrawScaleText((int)(keydispx+ box_spacing*3+ gutter), (int)(keydispy +gutter),"D", 2.0d);

		float sx = 60f;
		float sy = 90f;
		mouse = new sketch();
		
		int mousex = (int)(md.win_width*7f/10f);
		int mousey = (int)(md.win_height*0.6f);
		
		mouse.numpoints = 44;
		for (int count=0; count< 40; count=count+2) {
			// fill fill
			mouse.xmat[count] = (float) (Math.cos(((double)(count/2)+1)/10d*Math.PI) * sx + mousex);
			mouse.ymat[count] = (float) (Math.sin(((double)(count/2)+1)/10d*Math.PI) * sy + mousey);	
			mouse.xmat[count+1] = (float) (Math.cos(((double)(count/2)+2)/10d*Math.PI) * sx + mousex);
			mouse.ymat[count+1] = (float) (Math.sin(((double)(count/2)+2)/10d*Math.PI) * sy + mousey);
			mouse.rcolor[count/2] = 1.0f;
			mouse.gcolor[count/2] = 1.0f;
			mouse.bcolor[count/2] = 1.0f;
		}
		int point1 = 9;
		int point2 = 19; 
		mouse.xmat[40] = (float) (Math.cos(((double) point1 + 1)/10d*Math.PI) * sx + mousex);
		mouse.ymat[40] = (float) (Math.sin(((double)point1 + 1)/10d*Math.PI) * sy + mousey);
		mouse.xmat[41] = (float) (Math.cos(((double) point2 + 1)/10d*Math.PI) * sx + mousex);
		mouse.ymat[41] = 	(float) (Math.sin(((double) point2 + 1)/10d*Math.PI) * sy + mousey);	
		mouse.rcolor[20] = 1.0f;
		mouse.gcolor[20] = 1.0f;
		mouse.bcolor[20] = 1.0f;	
		mouse.xmat[42] = (float) mousex;
		mouse.ymat[42] = (float) (sy + mousey);
		mouse.xmat[43] = (float) mousex;
		mouse.ymat[43] = 	(float) mousey ;	
	
		mouse.rcolor[21] = 1.0f;
		mouse.gcolor[21] = 1.0f;
		mouse.bcolor[21] = 1.0f;	

		mouse.drawit();
		
		rt.DrawText((int)(mousex-sx*2), (int)(mousey+sy*1), "FIRE");
		rt.DrawText((int)(mousex+sx), (int)(mousey+sy*1), "RELOAD");	
		

		int texty = (int) (md.win_height/3);
		int lineheight  =rt.GetHeight(1.5d);
		rt.ChangeFontColor(md.teamR(0), md.teamG(0), md.teamB(0));
		rt.DrawScaleText(keydispx, texty, "ORANGE FLAG IS YOUR TEAM", 1.5d);
		rt.ChangeFontColor(md.teamR(1), md.teamG(1), md.teamB(1));
		rt.DrawScaleText(keydispx, texty- lineheight, "GREEN FLAG IS NEUTRAL BUT THEY CAN BE PROVOKED", 1.5d);
		rt.ChangeFontColor(md.teamR(2), md.teamG(2), md.teamB(2));
		rt.DrawScaleText(keydispx, texty - lineheight*2, "RED FLAG IS THE ENEMY FLEET", 1.5d);
		rt.ChangeFontColor(md.teamR(3), md.teamG(3), md.teamB(3));
		rt.DrawScaleText(keydispx, texty - lineheight*3, "BLUE FLAG IS THE ENEMY PIRATE FLEET", 1.5d);

		if ((Keyboard.isKeyDown(Keyboard.KEY_SPACE)) 
				|| (Keyboard.isKeyDown(Keyboard.KEY_RETURN)) 
				|| (Keyboard.isKeyDown(Keyboard.KEY_ESCAPE)) 
				|| (Keyboard.isKeyDown(Keyboard.KEY_W))
				|| (Keyboard.isKeyDown(Keyboard.KEY_UP))
				|| (Mouse.isButtonDown(0))) {
			game_state = GAME_STATE_LOAD;
		}
		
		
		Display.update();	
	}
	
	int GameEndScreen() {
		RayText rt;
		rt = new RayText();

		
		double quitx, quity, quit_width;
		double resumex, resumey, resume_width;
		
		quit_width= md.win_width/15;
		resume_width = md.win_width/15;
		
		quitx= md.win_width*4d/5d;
		quity= md.win_height/3d;
		
		resumex = md.win_width/5d;
		resumey = quity;
		
		sketch quitbtn = new sketch();
		quitbtn.CreateCircle((float)quitx, (float)quity, (float)quit_width);
		quitbtn.ChangeColor(1.0f, 0.1f, 0.1f);
		rt.ChangeFontColor(1.0f, 0.1f, 0.1f);
		rt.DrawScaleText((int) (quitx-rt.GetLength("QUIT", 1.5d)/2d), (int) (quity- rt.GetHeight(1.5d)/2d), "QUIT", 1.5d);
		sketch resumebtn = new sketch();

		resumebtn.CreateCircle((float) resumex, (float) resumey, (float) resume_width);
		resumebtn.ChangeColor(0.1f, 1.0f, 0.1f);
		rt.ChangeFontColor(0.1f, 1.0f, 0.1f);
		rt.DrawScaleText((int) (resumex-rt.GetLength("RESUME", 1.5d)/2d), (int) (resumey - rt.GetHeight(1.5d)/2), "RESUME", 1.5d);

		
		rt.ChangeFontColor(0.9f, 0.9f, 0.9f);
		quitbtn.drawit();
		resumebtn.drawit();
		
		int x = md.win_width/10;
		int y = (int) (md.win_height *9f/10f);	
		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glLoadIdentity();
		GL11.glOrtho(0, md.win_width, 0, md.win_height, 1, -1);
		GL11.glMatrixMode(GL11.GL_MODELVIEW);

		
		rt.DrawScaleText(x, y, "THANK YOU FOR TRYING THIS PROTOTYPE", 1.5d);
		rt.DrawScaleText(x, (int) (y- rt.GetHeight(2.0d)), "PLEASE SEND ANY IDEAS/ SUGGESTIONS/ COMMENTS", 1.5d);
		rt.DrawScaleText(x, (int) (y - 2* rt.GetHeight(2.0d)),"TO GEORGECOLLINS AT GMAIL DOT COM", 1.5d );
		
		rt.DrawScaleText(x, (int) (y-4*rt.GetHeight(2.0d)), 
				"TOTAL MONEY EARNED "+ String.valueOf(md.total_money[md.PLAYER_TEAM])+ "    TOTAL TONS SUNK " + String.valueOf(md.tons_sunk[md.PLAYER_TEAM]), 1.5d);
		if (Mouse.isButtonDown(0)) {
			
			int mx= Mouse.getX();
			int my= Mouse.getY();
			
			if ((mx-quitx)*(mx-quitx) + (my-quity)*(my-quity) < quit_width*quit_width)
				return QUIT_GAME;
			
			if ((mx - resumex)*(mx- resumex) + (my-resumey)*(my-resumey) < resume_width*resume_width)
				return GAME_STATE_SEA;
			
			// another click could be RESUME_GAME
		//	end game
		}
	
		Display.update();		
		return GAME_STATE_END_SCREEN;
	}
	
	void Init(int userx, int usery) {
		// the draw part
		RayText rt=new RayText();
		rt.DrawText(100, 100, String.valueOf(loadx));
		
		load_screen_ship.SetPosition(md.win_width/2, md.win_height/2, 0);
		load_screen_ship.DrawIt();
		
		rt.DrawText((int)(md.win_width/2d)-5, (int)(md.win_height/2d+ load_screen_ship.hd.width*load_screen_ship.hd.numhardpoints+40),"W");
		rt.DrawText((int)(md.win_width/2d)-5, (int)(md.win_height/2d- load_screen_ship.hd.width*load_screen_ship.hd.numhardpoints-40),"S");
		rt.DrawText((int)(md.win_width/2d -20-load_screen_ship.hd.width), (int) (md.win_height/2d), "A");
		rt.DrawText((int)(md.win_width/2d +10+ load_screen_ship.hd.width), (int) (md.win_height/2d), "D");
		
		
		rt.DrawText((int) (md.win_width-200), (int) (md.win_height-md.win_height/8), "M   SHOW MAP");
		rt.DrawText((int) (md.win_width-200), (int) (md.win_height-md.win_height/8- 30), "P   FIND PORT");	
		rt.DrawText((int) (md.win_width-200), (int) (md.win_height-md.win_height/8- 60), "E   FIND ENEMY");

		if (md.island_mat[loadx][loady] > 0) {
			while ((IMmat[loadx][loady]==null) || (IMmat[loadx][loady].PortCount() < 6)) {  // could be a in port
				FillIslandManager(loadx, loady);
			}
		} else
			FillIslandManager(loadx, loady);
		FillPickupManager(loadx, loady);
		FillShipManager(loadx, loady, userx, usery);  // this is now the most time consuming


		
		loadx=loadx+1;
		if (loadx==md.segment_width) {
			loadx=0;
			loady=loady+1;
			if (loady==md.segment_height) {
				FillParticleAdjacent();

				md.SetupWorld(IMmat, SMmat, PMmat, PKmat);
				// set the far port to team RED
				SetUpEnemyPort();
				fc.Init(SMmat, IMmat, ui, userx, usery);
				game_state=GAME_STATE_SEA;
			}
		}

		Display.update();	
		
	}

	public void DrawWater3(double x, double y, double t, int sectorx, int sectory ){
		// this is a test version to figure out why it isn't working
	

		GL11.glBegin(GL11.GL_LINES);
		GL11.glColor3f(0.5f, 0.5f, 1.0f);
		GL11.glLineWidth(1.0f);

		int w=((int) md.width);
		int h=((int) md.height);
		int startx=(int) (x-w/2);
		int starty=(int) (y-h/2);
		
		x=x-x%50;
		y=y-y%50;
		
		
		int step = (int)IMmat[user_segmentx][user_segmenty].MAP_SCALE;
		int oddcount=0;
		float u=1;
		float v=1;
		float d;
		for (int loopx=(int) x-w/2; loopx < x+w; loopx=loopx+step) {
			for (int loopy=(int) y-h/2; loopy < y+h; loopy=loopy+step) {
		//		System.out.print(IMmat[user_segmentx][user_segmenty].MapValue(loopx, loopy) + " ");
				oddcount++;
				if (oddcount>10)
					oddcount=0;		
				if (IMmat[user_segmentx][user_segmenty].MapValue((double)loopx, (double) loopy)==0) {
					d=((float) (0.8* ((int) (md.wavetime+oddcount))%50));
					u=((float) IMmat[user_segmentx][user_segmenty].GetCurrentX((double) loopx, (double) loopy)*d);
					v=((float) IMmat[user_segmentx][user_segmenty].GetCurrentY((double) loopx, (double) loopy)*d);
					GL11.glColor3f(0.5f, 0.5f, 1.0f);
				}
				else
				{
					// no motion
					u=0;
					v=0;
					if (IMmat[user_segmentx][user_segmenty].MapValue((double)loopx, (double) loopy)==2) {
						GL11.glColor3f(1.0f, 0.0f, 0.0f);
					}
					else
						GL11.glColor3f(0.0f, 1.0f, 0.0f);
				}
				GL11.glVertex3d(((float)loopx)+u, ((float)loopy)+v, 0f);				
				GL11.glVertex3d(((float)loopx+1)+u, ((float)loopy+1)+v, 0f);
				
			
			}
	//		System.out.println(" ");
		}
		

	}

	public void FillShipManager(int xcount, int ycount, int sectorx, int sectory) {
		
		WorldThing wt;
		int id;
		ship s;
		wt=new WorldThing(0.0d, 0.0d, Math.PI);

				PMmat[xcount][ycount]= new ProjectileManager(md);  // initialize the instance with a constructor
				PMmat[xcount][ycount].offsetx=GetOffsetX(xcount);
				PMmat[xcount][ycount].offsety=GetOffsetY(ycount);
				SMmat[xcount][ycount]= new ShipManager(md);		// initialize the instance with a contstructor
				SMmat[xcount][ycount].SetOffset(md, xcount, ycount);

				SMmat[xcount][ycount].md=md;
				SMmat[xcount][ycount].SetPM(PMmat[xcount][ycount]);  
				SMmat[xcount][ycount].SetPK(PKmat[xcount][ycount]);
		
				double xrange = md.world_width*0.8d;
				double yrange = md.world_height*0.8d;
				for (int loop = 0; loop < md.ship_mat[xcount][ycount]; loop++) {
					boolean blocked;
					do {
						blocked=false;
						
						wt.xpos=Math.random()*xrange - xrange/2;
						wt.ypos=Math.random()*yrange - yrange/2;
						for (int i=-2; i < 3; i++)
							for (int j=-2; j<3; j++)
								if (IMmat[xcount][ycount].MapValue(wt.xpos+ i*50d+SMmat[xcount][ycount].offsetx , wt.ypos+j*50d+SMmat[xcount][ycount].offsety )!=0)
									blocked=true;
						// check to see if another ship is in range
						if (!blocked)
							blocked=SMmat[xcount][ycount].AnyShipAt(wt);
					} while(blocked);  // can't be two close to the center sqrt(20000)= 141 distance
					wt.rotation=Math.random()*Math.PI*2;


					id=SMmat[xcount][ycount].AddShip(wt);
					s=SMmat[xcount][ycount].GetShip(id);
					// this should be a lot more complicated an in a function
					// some segments have no ships.  others lots differnt teams, different mixes
					double width;
					if (s != null) {
						if ((loop==0)  && (xcount==sectorx) && (ycount==sectory)) {
						//	s.load("destroyer.txt");
							HullData hd = new HullData();
							width = Math.random()*8+17;
							hd.ThinHull(width, 3, 1);
							s.BuildShip(hd);
							s.StartingComponents();
							// add this ships component to my starting components
							md.AddTeamComponent(s.scomponent[0].cd, md.PLAYER_TEAM);
							
						}
						else {
					
							if (loop < 3) {
								width = Math.random()*10+15;
								HullData hd = new HullData();
								hd.ThinHull(width, 3, 2);
								s.BuildShip(hd);
								s.StartingComponents();

								//s.load("destroyer2.txt");
								// s.load("testboat.txt");
							} else {
					
		
								s.BuildShip();
					
					
							
							}
						}

						s.SetTeam(1, md); //((int) (Math.random()*3+1), md); // something else
						s.AttachAI(SMmat[xcount][ycount], IMmat[xcount][ycount], md);
						if ((loop==0)  && (xcount==sectorx) && (ycount==sectory)) {  // this is where the user is
							// make user ship

							md.userid=id;
							user_segmentx=sectorx;
							user_segmenty=sectory;
							s.SetTeam(0, md);

					
						}
					}
				}
				
				
				// add freighters to ports

				for (int icount=0; icount< IMmat[xcount][ycount].island_count; icount++) {
					if (IMmat[xcount][ycount].islands[icount].has_port!=0) {
						int fteam = 1; //(int) (Math.random()*3+1);
						island isl=IMmat[xcount][ycount].islands[icount];  // to save typing
						wt.xpos=isl.OrbitPath.xmat[0]-SMmat[xcount][ycount].offsetx;
						wt.ypos=isl.OrbitPath.ymat[0]-SMmat[xcount][ycount].offsety;
						id=SMmat[xcount][ycount].AddShip(wt);
						s=SMmat[xcount][ycount].GetShip(id);
						HullData hdf = new HullData();
						hdf.BuildSmallHull(hdf);
						s.BuildFreighter(hdf);
						SMmat[xcount][ycount].PlaceOnPath(s, isl.OrbitPath);

						s.SetTeam(fteam, md); // something else
						s.AttachAI(SMmat[xcount][ycount], IMmat[xcount][ycount], md);			
				

						s.ai.StartFollowPath(s,isl.OrbitPath, 50, AIControl.PATH_CIRCULAR);
						// build escort s2
						wt.xpos=isl.OrbitPath.xmat[isl.OrbitPath.numpoints-1]-SMmat[xcount][ycount].offsetx;
						wt.ypos=isl.OrbitPath.ymat[isl.OrbitPath.numpoints-1]-SMmat[xcount][ycount].offsety;
						wt.rotation = Math.atan2(wt.xpos-isl.OrbitPath.xmat[0],wt.ypos-isl.OrbitPath.ymat[0]) + Math.PI;
						id=SMmat[xcount][ycount].AddShip(wt);
						ship s2=SMmat[xcount][ycount].GetShip(id);
						HullData hdm = new HullData();
						hdm.BuildSmallHull(hdm);
						s2.BuildMixedShip(hdm);
						s2.SetTeam(fteam, md); // something else
						s2.AttachAI(SMmat[xcount][ycount], IMmat[xcount][ycount], md);		
						s2.ai.StartFollow(s2, s, -50, 0);
					
						
					}
				}


	}	
	
	
	public void  FillParticleAdjacent() {
	// for each particle manager assign left, right, top, bottom
		int xmat, ymat;
	
		for (int xcount=0; xcount<md.segment_width; xcount++)
			for (int ycount=0; ycount<md.segment_height; ycount++) {

			xmat=xcount+1;
			ymat=ycount;
			if (xmat> md.segment_width-1) xmat=0;
			PMmat[xcount][ycount].right=PMmat[xmat][ymat];
			
			xmat=xcount-1;
			if (xmat < 0) xmat=md.segment_width-1;
			PMmat[xcount][ycount].left=PMmat[xmat][ymat];
			
			xmat=xcount;
			ymat=ycount+1;
			if (ymat > md.segment_height-1) ymat=0;
			PMmat[xcount][ycount].top=PMmat[xmat][ymat];
			
			ymat=ycount-1;
			if (ymat < 0) ymat=md.segment_height-1;
			PMmat[xcount][ycount].bottom=PMmat[xmat][ymat];

		}
	}
	
	public void FillIslandManager(int xcount, int ycount) {
		// this needs to be redone
		// island count should be segment specific

				IMmat[xcount][ycount] = new IslandManager(md);
				IMmat[xcount][ycount].SetOffset(md, xcount, ycount);
				//IMmat[xcount][ycount].offsetx=GetOffsetX(xcount);
				//IMmat[xcount][ycount].offsety=GetOffsetY(ycount);
				
				for (int count=0; count < md.island_mat[xcount][ycount]; count++) {

					double size=Math.random()*100d+Math.random()*100d+20d;
					int id= IMmat[xcount][ycount].PlaceRandomIsland(size);  //  random size map, but it can't be too small. 
					if  (size > IslandManager.PORT_SIZE)  {

						island bigisland= IMmat[xcount][ycount].GetIsland(id);

										
						if (bigisland!=null) {
							

							
							switch (namecount) {
								case 0 : bigisland.SetName("GOA");
									break;
								case 1 : bigisland.SetName("APU");
									break;
								case 2 : bigisland.SetName("MANKEE");
									break;
								case 3 : bigisland.SetName("AROOT");
									break;
								case 4 : bigisland.SetName("DONETA"); 
									break;
								case 5 : bigisland.SetName("BANIKE");
									break;
								case 6 : bigisland.SetName("DOREE");
									break;
								case 7 : bigisland.SetName("NUBONO");
									break;
								case 8 : bigisland.SetName("BALISH");
									break;
								case 9 : bigisland.SetName("LEEDO"); 								
									break;
								case 10 : bigisland.SetName("GOA");
								break;
							case 11 : bigisland.SetName("RAPATU");
								break;
							case  12: bigisland.SetName("GWEST");
								break;
							case 13 : bigisland.SetName("PONETA");
								break;
							case 14 : bigisland.SetName("TWONKLE"); 
								break;
							case 15 : bigisland.SetName("VWALEE");
								break;
							case 16 : bigisland.SetName("WUDOOF");
								break;
							case 17 : bigisland.SetName("RAFY");
								break;
							case 18 : bigisland.SetName("PEMOWA");
								break;
							case 19 : bigisland.SetName("ZYRNEN"); 
								break;
							case 20 : bigisland.SetName("FOSTEE");
								break;
							case 21: bigisland.SetName("SANPO");
								break;
							case 22: bigisland.SetName("DAW");
								break;
							case 23: bigisland.SetName("ZANO");
								break;
							}
								
							namecount++;
						}
					}
				}
			// fill the map data for currents, land, water and coast
			IMmat[xcount][ycount].FillMap2();
			// fill the path nodes for pathing searches
			IMmat[xcount][ycount].FillPathMap();

			// fill in paths AFTER you fill in the map
			for (int count=0; count < md.island_mat[xcount][ycount]; count++) {
				
				island bigisland= IMmat[xcount][ycount].islands[count];
				if (bigisland.has_port==1) {
					bigisland.OrbitPath= new Path();
					bigisland.OrbitPath.BuildPathAroundIsland(bigisland, IMmat[xcount][ycount]);
					bigisland.has_port=1;
			
					FillPort(bigisland, MissionData.NEUTRAL_TEAM);
				}
			}
		
	}
	// Fill PickupManager has to be called after IM
	void FillPickupManager(int xcount, int ycount) {

				PKmat[xcount][ycount]= new PickupManager(md);
				PKmat[xcount][ycount].SetIslandManager(IMmat[xcount][ycount]);
				// give it an offset
				PKmat[xcount][ycount].SetOffset(md, xcount, ycount);
				WorldThing wt= new WorldThing();
				for (int pcount=0; pcount< md.pickup_mat[xcount][ycount]; pcount++) {

					boolean placed= false;
					while (placed== false) {
						double xrange=md.world_width*0.6d;
						double yrange=md.world_height*0.6d;
						wt.xpos=Math.random()*xrange-xrange/2 + PKmat[xcount][ycount].offsetx;
						wt.ypos=Math.random()*yrange-yrange/2 + PKmat[xcount][ycount].offsety;
						wt.rotation=Math.random()*Math.PI*2;
						if (IMmat[xcount][ycount].MapValue(wt.xpos, wt.ypos)==0)
							placed = true;
					}
					PKmat[xcount][ycount].AddPickup(wt, 0); // 0 = repair
						
				}
				
				
				for (int pcount=0; pcount< md.pickup_mat[xcount][ycount]; pcount++) {
				
					boolean placed = false;
					while (placed== false) {
						double xrange=md.world_width*0.6d;
						double yrange=md.world_height*0.6d;
						wt.xpos=Math.random()*xrange-xrange/2 + PKmat[xcount][ycount].offsetx;
						wt.ypos=Math.random()*yrange-yrange/2 + PKmat[xcount][ycount].offsety;
						wt.rotation=Math.random()*Math.PI*2;
						if (IMmat[xcount][ycount].MapValue(wt.xpos, wt.ypos)==0)
							placed = true;
					}
					PKmat[xcount][ycount].AddPickup(wt,4);  // 4 = fish
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
	
	public void FillPort(island isl, int team) {
		if (isl.pd==null) {
			isl.pd= new PortData();
		}
		isl.pd.team=team;
	//	isl.pd.component_count=3;
		ComponentBuilder cb=new ComponentBuilder();
		isl.pd.component_count=PortData.MAX_COMPONENTS_IN_PORT;
		boolean has_torpedo=false;
		boolean has_mine = false;
		boolean has_engine = false;
		boolean has_repair = false;
		for (int i=0; i < isl.pd.component_count; i++) {
			isl.pd.components[i]=new ComponentData();
			if (i==0) {
				isl.pd.components[i].BuildCargoComponent();
			} else {
				ComponentData testcomp =new ComponentData();
				boolean ok=false;
				while (!ok) {
					ok=true;
					testcomp.BuildWeaponComponent();
					if ((has_mine) && (testcomp.type==ComponentData.ctype.MINE))
						ok=false;
					if ((has_torpedo) && (testcomp.type==ComponentData.ctype.TORPEDO))
						ok=false;
					if ((has_engine) && (testcomp.type==ComponentData.ctype.ENGINE))
						ok=false;
					if ((has_repair) && (testcomp.type== ComponentData.ctype.REPAIR))
						ok = false;
					
				}
				if (testcomp.type==ComponentData.ctype.MINE)
					has_mine=true;
				if (testcomp.type==ComponentData.ctype.TORPEDO)
					has_torpedo=true;
				if (testcomp.type==ComponentData.ctype.ENGINE)
					has_engine=true;
				if (testcomp.type == ComponentData.ctype.REPAIR)
					has_repair = true;
				
				isl.pd.components[i]=testcomp;

			}
				
		}
		isl.pd.hull_count=3;
		for (int i=0;i < isl.pd.hull_count; i++) {
			isl.pd.hulls[i] = new HullData();
			if (Math.random()< 0.6d) {
				isl.pd.hulls[i].BuildHull(isl.pd.hulls[i]);
			}
			else isl.pd.hulls[i].BuildWideHull(isl.pd.hulls[i]);
		}

		isl.pd.SetPrices(4);
			
	}
	
	void SetUpEnemyPort() {
		// find the player
		ship player = md.FindShip(md.userid);
		int psegx = md.GetSegmentX(player.xpos);
		int psegy = md.GetSegmentY(player.ypos);
		// get the farthes port
		island badisl = IMmat[psegx][psegy].FindFarthestPort(player);
		// set the team
		badisl.pd.team = md.RED_TEAM;
		// place barges at point 36 and 41 in world poitns
		
		badisl.SetWorldPortPoints(IMmat[psegx][psegy].SCALE_MULTIPLE);
		
		WorldThing b1 = new WorldThing();
		b1.xpos = badisl.isketch.worldx[36];
		b1.ypos = badisl.isketch.worldy[36];
		b1.rotation = badisl.rotation - Math.PI/2;
		int barge1id = SMmat[psegx][psegy].AddShip(b1);
		ship barge1 = SMmat[psegx][psegy].GetShip(barge1id);
		HullData bhd = new HullData();
		bhd.Barge(1, 30);
		barge1.BuildAttackShip(bhd);
		barge1.SetTeam(md.RED_TEAM, md);
		barge1.AttachAI(SMmat[psegx][psegy], IMmat[psegx][psegy], md);


		WorldThing b2 = new WorldThing();
		b2.xpos =badisl.isketch.worldx[0];
		b2.ypos = badisl.isketch.worldy[0];
		b2.rotation = badisl.rotation - Math.PI/2;

		int barge2id = SMmat[psegx][psegy].AddShip(b2);
		ship barge2 = SMmat[psegx][psegy].GetShip(barge2id);
		// I shouldn't have to declare a new Hull, somewhere data is not getting cloned
		HullData bhd2 = new HullData();
		bhd2.Barge(1, 30);
		
		barge2.BuildAttackShip(bhd2);
		barge2.SetTeam(md.RED_TEAM, md);
		barge2.AttachAI(SMmat[psegx][psegy], IMmat[psegx][psegy], md);
	

		// place mines
		// these won't last forever
		double mlx = b1.xpos- b2.xpos;
		double mly = b1.ypos - b2.ypos;
		double x, y;
		for (int i = 0; i < 10; i++) {
			x =b2.xpos + mlx*i/10f;
			y= b2.ypos + mly*i/10f;
			PMmat[psegx][psegy].AddMine(x, y, 0, 0, 0, barge1id, md.RED_TEAM);
			
		}
		
	}
	
	public boolean SetUpDockDisplay(int island_id, int team) {
//		port_island=IMmat[user_segmentx][user_segmenty].GetIsland(island_id);
		
		// find the island wherever we are
		for (int x=0; x<md.segment_width; x++)
			for (int y=0; y< md.segment_height;y++) {
				port_island=IMmat[x][y].GetIsland(island_id);
				if (port_island!=null) {
					user_segmentx=x;  // here we are
					user_segmenty=y;
					x=md.segment_width;
					y=md.segment_height;
				}
			}
		
		port_shut_down_timer=150.0d;		
		
		port_disp.SetupDisplay(md.userid, port_island,  SMmat[user_segmentx][user_segmenty] );
		
		if (md.money[md.PLAYER_TEAM] < port_disp.CheapestShip()) {
			return false;
		}
		
		return true;
	}
	
	public void PortDisplay(double time) {
		
		ship rship=port_disp.ShowDisplay(time);
		if (rship!=null)
			LeavePort(rship);
		

	}
	
	public void ObjectiveDisplay(double time) {
		if (obj_disp.ShowDisplay(time) != ObjectiveDisplay.NO_CHOICE_MADE) {
			// deal with the choice
			game_state = GAME_STATE_SEA;
		}
	}
	

	public void LeavePort(ship pick) {
		game_state=GAME_STATE_SEA;
		ui.SetPortTimeOut(); // don't mistake port mouse clicks for sea ones
		
		double startx= port_island.world_portx;
		double starty= port_island.world_porty;
		double mag =Math.sqrt((startx-port_island.xpos)*(startx-port_island.xpos)+
								(starty-port_island.ypos)*(starty-port_island.ypos));
		double unitx = (startx-port_island.xpos)/mag;
		double unity = (starty-port_island.ypos)/mag;
		
		double spacing = 150d;
		
		pick.xpos=startx + md.userfleet.count()*unitx*spacing;
		pick.ypos=starty + md.userfleet.count()*unity*spacing;
		// set pick rotation
		pick.rotation=port_island.rotation-Math.PI/2;  //Math.atan2(port_island.world_portx-port_island.xpos, port_island.world_porty-port_island.ypos);  // rotate ship out-> leaving
		
		SMmat[user_segmentx][user_segmenty].DeleteShip(md.userid);
		md.userfleet.StartShipList();
		while (md.userfleet.HasNextShip()) {
			ship spick=md.userfleet.GetNextShip();

			SMmat[user_segmentx][user_segmenty].DeleteShip(spick.id);  // if it is there already, get rid of it
		}
		int n=SMmat[user_segmentx][user_segmenty].AddShip(pick);
		if (n==0) {
			ui.quit=1;
			//int xx=0;
			//xx=1/xx;
		} 
		
		md.userid=pick.id;
		pick.AttachAI(SMmat[user_segmentx][user_segmenty], IMmat[user_segmentx][user_segmenty], md);
		SMmat[user_segmentx][user_segmenty].NewUserShipNotify(md.userid);
		ship s=SMmat[user_segmentx][user_segmenty].GetShip(md.userid);
		s.SetSpeed(0);
		for (int i=0; i < s.component_count; i++) {
			s.scomponent[i].SetAmmo();
		}
		// this shouldn't be required but  it's a test


		// sidekick
		int xstart= 20;
		int ystart = 20;
		int count=1;
		md.userfleet.StartShipList();
		while (md.userfleet.HasNextShip()) {
			ship spick=md.userfleet.GetNextShip();
			if (spick.id!=md.userid) { // if I was in the fleet and now I am the user, don't add me again
				
				spick.rotation=port_island.rotation-Math.PI/2;  // rotate ship out-> leaving
				//spick.xpos=pick.xpos+xstart*Math.cos(spick.rotation);
				//spick.ypos=pick.ypos+ystart*Math.sin(spick.rotation);
				spick.xpos=startx + (md.userfleet.count()-count)*unitx*spacing;
				spick.ypos=starty + (md.userfleet.count()-count)*unity*spacing;


				
				int sidekick=SMmat[user_segmentx][user_segmenty].AddShip(spick);	
				if (sidekick!=0) {
					ship sk=SMmat[user_segmentx][user_segmenty].GetShip(sidekick);
					int follow_dist = (int)((5+ s.hd.full_length/2 + sk.hd.full_length/2)*-1);

					sk.AttachAI(SMmat[user_segmentx][user_segmenty], IMmat[user_segmentx][user_segmenty], md);
					sk.SetTeam(md.PLAYER_TEAM, md);
					if (md.userfleet.count()==1) {
						sk.ai.StartFleetFollow(sk,s, 10,follow_dist, 0);  // me = sk, s= target, 10 is priority
					}
					else {
						if (count==1){
							sk.ai.StartFleetFollow(sk,s, 10,follow_dist, 75); 
						} else
							sk.ai.StartFleetFollow(sk,s, 10,follow_dist, -75); 		
					}
					sk.SetSpeed(0);
					for (int i=0; i < s.component_count; i++) {
						if (sk.scomponent[i]!=null)
							sk.scomponent[i].SetAmmo();
					}
					s=sk; // follow this one now
				}
				count++;

			}
		}
		md.userfleet.AddShip(pick);
		SetUpDisplay();
	}	
	public void SeaDisplay(double time) {
		// ui.pollInput(win_width, win_height);
		double centerx=0; 
		double centery=0;
		
		boolean map_mode=ui.ShowMap();
		
		int matx;
		int maty;

		int startx=0;
		int starty=0;
		int endx=1;
		int endy=1;
		ship s=SMmat[user_segmentx][user_segmenty].GetShip(md.userid);	
		
		/* 
		 *  This will have a problem it md.segment_height = 1
		 *  but md.segment_width != 1
		 */
		
		if ((s!=null) && (md.segment_width > 1)){
			if (s.xpos < SMmat[user_segmentx][user_segmenty].offsetx) {
				startx=-1;
				endx=1;
			} else {
				startx=0;
				endx=2;
			}
			
			if (s.ypos < SMmat[user_segmentx][user_segmenty].offsety){
				starty=-1;
				endy=1;	
			} else {
				starty=0;
				endy=2;
			}
				
		}
		// if my y is less then the offset of my segment then starty=-1, endy =0
		// else starty=0, endy=0

		if (map_mode==false) 
		for (int xcount=startx; xcount < endx; xcount++) 
			for (int ycount=starty; ycount < endy; ycount++) 
			{			
	
				matx = user_segmentx + xcount;
				if (matx > md.segment_width-1) matx=0;
				if (matx < 0) matx = md.segment_width-1;
				maty = user_segmenty + ycount;
				if (maty > md.segment_height-1) maty=0;
				if (maty < 0) maty = md.segment_height-1;

				SMmat[matx][maty].HandleUI(ui, md.userid, time);	

				PMmat[matx][maty].Simulate(time);

				SMmat[matx][maty].Simulate(time, IMmat[matx][maty], matx, maty);
				int dock=IMmat[matx][maty].Update(SMmat[matx][maty], PMmat[matx][maty], md.userid, time);

				PKmat[matx][maty].Update(time);
				if ((port_shut_down_timer<0.001d) && (dock!=0)) {
					last_dock=dock;

					// if dock is neutral, you have captured it
					island isl = IMmat[matx][maty].GetIsland(dock);
					if (isl!=null) {
					//	if (isl.pd.team== MissionData.NEUTRAL_TEAM)  // or evil red
							isl.pd.team = MissionData.PLAYER_TEAM;  
					}
					
					SetUpDockDisplay(dock,md.PLAYER_TEAM);  
					game_state=GAME_STATE_PORT;

				} else {
					port_shut_down_timer=port_shut_down_timer-time;
				}


				
			}	

		if (s!=null) {
			centerx=s.xpos;
			centery=s.ypos;
			fc.SetUser(s);

			GL11.glMatrixMode(GL11.GL_PROJECTION);
			GL11.glLoadIdentity();
			if (map_mode) {
				double mdw=md.width*md.zoom_width;
				double mdh=md.height*md.zoom_height;
				///GL11.glOrtho(s.xpos-mdw/2, mdw+s.xpos-mdw/2, s.ypos-mdh/2, mdh+s.ypos-mdh/2, 1, -1);	
				GL11.glOrtho(-mdw/2, mdw-mdw/2, -mdh/2, mdh-mdh/2, 1, -1);	
				s.DrawScaled(5.0d);
			} else {
 					GL11.glOrtho(s.xpos-md.width/2, md.width+s.xpos-md.width/2, s.ypos-md.height/2, md.height+s.ypos-md.height/2, 1, -1);	

			}
			// GL11.glOrtho(s.xpos-2048, 4096+s.xpos-2048, s.ypos-3072/2, 3072+s.ypos-3072/2, 1, -1);	
			GL11.glMatrixMode(GL11.GL_MODELVIEW);



		} else {
	//		System.out.println("GetShip(" +md.userid+") return null");
	//		System.out.println("segment x " + user_segmentx + " y " + user_segmenty);
			if ((md.userfleet.count()>0)) {
				md.userfleet.StartShipList();
				s=md.userfleet.GetNextShip();
				md.userfleet.RemoveShip(s.id);
				md.userid=s.id;
			} 
			else {
				if (last_dock!=0) {
					
					// if you can't buy a ship quit
					
					if (SetUpDockDisplay(last_dock, 0)) {
						game_state=GAME_STATE_PORT;
					} else
						game_state=GAME_STATE_END_SCREEN;
				}
			}
				
		}
		
		
		// draw the background


		if ((s!=null) && (!map_mode)) {

			DrawWater3(s.xpos, s.ypos, time, user_segmentx, user_segmenty);  // was 2
		}
		

		for (int xcount=startx; xcount < endx; xcount++) 
			for (int ycount=starty; ycount < endy; ycount++) 

			{
				matx = user_segmentx + xcount;
				if (matx > md.segment_width-1) matx=0;
				if (matx < 0) matx = md.segment_width-1;
				maty = user_segmenty + ycount;
				if (maty > md.segment_height-1) maty=0;
				if (maty < 0) maty = md.segment_height-1;

				WorldThing min=new WorldThing();
				WorldThing max=new WorldThing();
				
				min.xpos = centerx - md.win_width*2d/3d;
				min.ypos = centery - md.win_height*2d/3d;
				max.xpos = centerx + md.win_width*2d/3d;
				max.ypos = centery + md.win_height*2d/3d;
				
				
				IMmat[matx][maty].Draw();
				SMmat[matx][maty].Draw(min, max);
				PMmat[matx][maty].Draw(centerx, centery, 0);
				if (!map_mode) PKmat[matx][maty].Draw();
			}
		
		if (map_mode) {
			ui.Draw(0, 0);
		}	else
			ui.Draw(centerx, centery);

		ui.pollInput(time, md.win_width, md.win_height, md.width, md.height);
		//System.out.print("XXX FC Update XXX");	
		fc.Update(time);  

		//System.out.print("Display update");
		Display.update();
		
		/*
		 *  I need to move the ship and the segment when the user goes out of bounds
		 */
		
		if (s!=null) {
			int new_segx = user_segmentx;
			int new_segy = user_segmenty;
			if (s.xpos > IMmat[user_segmentx][user_segmenty].offsetx + md.world_width/2) 
				new_segx+=1;
			if (s.xpos < IMmat[user_segmentx][user_segmenty].offsetx - md.world_width/2)
				new_segx-=1;
			if (s.ypos > IMmat[user_segmentx][user_segmenty].offsety + md.world_width/2)
				new_segy+=1;
			if (s.ypos < IMmat[user_segmentx][user_segmenty].offsety - md.world_width/2)
				new_segy-=1;
			// wrap ship around
			if (new_segx== md.segment_width) {
				new_segx=0;
				s.xpos=s.xpos-md.world_width*md.segment_width;
			}
			if (new_segx < 0) {
				new_segx = md.segment_width - 1;
				s.xpos=s.xpos+md.world_width*md.segment_width;
			}
			if (new_segy== md.segment_height) {
				new_segy=0;
				s.ypos=s.ypos-md.world_height*md.segment_height;
			}
			if (new_segy < 0) {
				new_segy = md.segment_height - 1;
				s.ypos=s.ypos+md.world_height*md.segment_height;
			}
			
			if ((new_segx!=user_segmentx) || (new_segy!=user_segmenty)) {
				SMmat[new_segx][new_segy].AddShip(s);
				SMmat[user_segmentx][user_segmenty].DeleteShip(md.userid);
				user_segmentx=new_segx;
				user_segmenty=new_segy;
				fc.SetUserSegment(new_segx, new_segy);
				
				// I need to do the same thing for md.userfleet
				// move them all over here
			}
		
			
			if (ui.switch_ships!=0) { 
				if (md.userfleet.count()>0) {
					md.userfleet.StartShipList();
					/*
					ship nxts= md.userfleet.GetNextShip();
					while (nxts==s) {
						nxts=md.userfleet.GetNextShip();
					}
					*/
					ship nxts = md.userfleet.GetTrailingShip(s);
					
					
					md.userid=nxts.id;
//					md.userfleet.RemoveShip(md.userid);
					
					int old_segmentx=user_segmentx;
					int old_segmenty=user_segmenty;
					
					user_segmentx=md.GetSegmentX(nxts.xpos); 
					user_segmenty=md.GetSegmentY(nxts.ypos);
					fc.SetUserSegment(user_segmentx, user_segmenty);			
					/*
					md.userfleet.StartShipList();
					ship last=nxts;  // follow first
					if (md.userfleet.count() > 0) {
						while (md.userfleet.HasNextShip()) last=md.userfleet.GetNextShip();
					}
					*/
					md.userfleet.AddShip(s);  // this doesn't' seem to double add
					// I should just go down the list and have people follow in order
					/*
					s.AttachAI(SMmat[old_segmentx][old_segmenty], IMmat[old_segmentx][old_segmenty], md);
					s.ai.StartFollow(s, last, 0, -50);
					*/
					s=nxts;
					md.userfleet.MakeFollowChain(s, SMmat[old_segmentx][old_segmenty], IMmat[old_segmentx][old_segmenty], md);
					
				}
				ui.switch_ships=0;
				
			}
			
		}
		

		
		
	}
	
	public int DisplayLoop() {
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT); 
		
		// set the color of the quad (R,G,B,A)
		GL11.glColor3f(0.5f,0.5f,1.0f);

		double time = ticks / md.time_factor;  // 400d;

		
		switch (game_state) {
			case GAME_STATE_INTRO:
				IntroScreen();
				break;
			case GAME_STATE_LOAD:
				Init(md.startx, md.starty);	
				break;
			case GAME_STATE_SEA:
				SeaDisplay(time);
				break;
			case GAME_STATE_PORT:
				PortDisplay(time);
				break;		
			case GAME_STATE_OBJECTIVE:
				ObjectiveDisplay(time); 
				break;
			case GAME_STATE_END_SCREEN:
				game_state=GameEndScreen();
				if (game_state==QUIT_GAME) {
					return FourExample.FORCE_QUIT;
				}
				break;
			
		}
		
		ticks=System.currentTimeMillis()-last_tick;  // 30 fps lock
		while (ticks < 16) {
			ticks=System.currentTimeMillis()-last_tick;
		}
		
		if (ticks>500)
			ticks=100;
		last_tick=System.currentTimeMillis();	

		if (ui.quit>0) {
			//return FourExample.FORCE_QUIT;
			game_state = GAME_STATE_END_SCREEN;
			ui.quit=0;
		}
			
		if (ui.objective_toggle > 0) {
			if (game_state == GAME_STATE_SEA) {
				obj_disp.Init();  // puts me on page 0
				game_state = GAME_STATE_OBJECTIVE;
			} else
				game_state = GAME_STATE_SEA;
			ui.objective_toggle = 0;	
			
		}
		return FourExample.SIM_SCREEN;
	}

	
	
	
	BigMission(MissionData mdin) {
		
		md=mdin;
		ui = new UserInput(md);
		fc = new FleetCommand(md);
		ui.target_pointer=true;
		ui.targetx=0f;
		ui.targety=0f;
		PMmat = new ProjectileManager[md.segment_width][md.segment_height];
		SMmat = new ShipManager[md.segment_width][md.segment_height];
		IMmat = new IslandManager[md.segment_width][md.segment_height];
		PKmat = new PickupManager[md.segment_width][md.segment_height];
		game_state= GAME_STATE_INTRO;  //GAME_STATE_LOAD;
		port_shut_down_timer=0;
		loadx=0;
		loady=0;
		last_dock=0;
		load_screen_ship=new ship(0);
		load_screen_ship.BuildShip();
			
		port_disp=new PortDisplay(ui, md);
		obj_disp = new ObjectiveDisplay(ui, md);

	}

}
