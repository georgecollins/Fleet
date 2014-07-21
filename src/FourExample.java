
// based on http://lwjgl.org/wiki/index.php?title=LWJGL_Basics_1_%28The_Display%29
import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;


public class FourExample {

	/**
	 * @param args
	 */
	
	static UserInput ui;
	static ProjectileManager PM;
	static ShipManager SM;
	static IslandManager IM;
	static MessagePump mp;
	
	// static ship fleets[];
	
	static double width, height;
	static int win_width, win_height;
	static double wavetime;
	

	static int screen_state;
	
	
	public static final int MAX_TEAMS=6;
	
	public static final int MISSION_SCREEN=1;
	public static final int BUILD_SCREEN=2;
	public static final int SIM_SCREEN=3;
	public static final int PAUSE_SCREEN=5;
	public static final int END_SCREEN=6;
	public static final int FORCE_QUIT = 7;
	
	
	public static int DEBUG_MODE=0;  // "debug" arguement changes this
	
	public void OpenWindow() {
		try {

			if (DEBUG_MODE==0) {
				
				DisplayMode targetDisplayMode= Display.getDesktopDisplayMode();
				win_width= targetDisplayMode.getWidth();
				win_height=targetDisplayMode.getHeight();
				width=win_width;
				height=win_height;
				Display.setFullscreen(true);
				Display.setDisplayMode(targetDisplayMode);
				Display.create();
				
			} else {
				DisplayMode targetDisplayMode= new DisplayMode(1280,800);
				Display.setDisplayMode(targetDisplayMode);
				Display.setFullscreen(true);
				Display.create();

			}

		} catch (LWJGLException e) {
			e.printStackTrace();
			System.exit(0);
		}
	
	}


	

	public void BigMissionLaunch() {
		int display_stage=0;
		MissionData md2=new MissionData();
		
	
		md2.ship_number=11;
		md2.advanced_ship_number =4;
		md2.island_number=9;
		md2.win_width=win_width;
		md2.win_height=win_height;
		md2.width= width;
		md2.height= height;
		md2.default_width= width;
		md2.default_height= height;
		md2.zoom_width = 14;
		md2.zoom_height = 14;
		md2.world_width=(int)(IslandManager.MAX_MAP*IslandManager.MAP_SCALE);
		md2.world_height=(int)(IslandManager.MAX_MAP*IslandManager.MAP_SCALE);

		// DEBUGGING
		if (DEBUG_MODE==1) 
			md2.debug_mode = 1;
		
	/*
		md2.segment_width= 5;  // double border
		md2.segment_height= 5; // double border		
		md2.startx=2;
		md2.starty=2;

		*/
		// 1x1 world
/*
		md2.segment_width = 1;
		md2.segment_height =1;
		md2.startx=0;
		md2.starty=0;
		md2.time_factor = 100d; 
*/
	/*
		md2.segment_width= 2;  // double border
		md2.segment_height= 2; // double border		
		md2.startx=0;
		md2.starty=0;
	*/
		
		md2.segment_width = 3;
		md2.segment_height =3;
		md2.startx=1;
		md2.starty=1;
		md2.time_factor = 100d;
		
		md2.wavetime=0.0d;
		md2.end_condition=MissionData.PLAYER_DIES_CONDITION;
		
		for (int xcount=0; xcount < md2.segment_width; xcount++) 
			for (int ycount=0; ycount < md2.segment_height; ycount++) {
					// 0 = border
				if ((xcount==0) || (ycount==0) || (xcount==md2.segment_width-1) || (ycount==md2.segment_height-1))	{
					md2.ship_mat[xcount][ycount]=0;
					md2.advanced_ship_mat[xcount][ycount]=0;
					md2.island_mat[xcount][ycount]= 0;// md2.island_number;
					md2.pickup_mat[xcount][ycount]= 0;  // ? what should this be?
				
				} else
			
				{
					if ((xcount==md2.startx) && (ycount==md2.starty)) {  // safe center
						md2.ship_mat[xcount][ycount]=  10;//6;//20;
						md2.advanced_ship_mat[xcount][ycount]=0;
						md2.island_mat[xcount][ycount]= 20;//8; //25;// md2.island_number;
						md2.pickup_mat[xcount][ycount]= 10;  //5;//10;  // ? what should this be?
						md2.fish_mat[xcount][ycount] = 25;
						
					} else {
						md2.ship_mat[xcount][ycount]=md2.ship_number;
						md2.advanced_ship_mat[xcount][ycount]=md2.advanced_ship_number;
						md2.island_mat[xcount][ycount]=md2.island_number;
						md2.pickup_mat[xcount][ycount]= 5;  // ? what should this be?
						
					}
				}			
			}
		
		for (int count=0; count <  MissionData.MAX_TEAMS; count++) {
			md2.money[count]=100;  // make it easier
			if (DEBUG_MODE==1) 
				md2.money[count]=1000;
			md2.tons_sunk[count]=0;
		}
		
		BigMission msnb=new BigMission(md2);		
		HullData hdt = new HullData();
		hdt.BuildHull(hdt);
		
		RayText rt = new RayText();
		msnb.SetUpDisplay();		
	
		boolean open=true;
		display_stage=0;
		int retval = 0;
		while (open) // should be mission end case
		{

			retval = msnb.DisplayLoop();
			if (msnb.game_state== msnb.QUIT_GAME)
				retval= FORCE_QUIT;
			if ((Display.isCloseRequested()) || (retval== FORCE_QUIT)) {
				Display.destroy();	
				open=false;
			}
		}
	
	}


	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		MissionData md=new MissionData();
		FourExample fe = new FourExample();
		
		try {
			String one = args[0];
			if (one.contentEquals("debug")) {
				
				DEBUG_MODE=1;
			}
		} catch (ArrayIndexOutOfBoundsException e) {
		//	System.out.println("ArrayIndexOutOfBoundsException catched");
		}

		
		//DisplayMode dm=Display.getDisplayMode();

		width= 1280;//1024; //dm.getWidth(); //1024; //1024+512; //1024+512;
		height= 800; //768;  //dm.getHeight(); //768; //768+384;  // 768+384;'
		
		
		win_width = 1280; //1024; // dm.getWidth();// 1024; //1024;
		win_height = 800; //768;  // dm.getHeight();// 768; //768;
		wavetime=0.0d;
		

		fe.OpenWindow();
		screen_state=SIM_SCREEN;
	//	fe.MissionSelect();
			
		fe.BigMissionLaunch();
			
	
	
		
	}



}
