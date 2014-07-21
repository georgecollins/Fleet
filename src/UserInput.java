import java.nio.IntBuffer;

import org.lwjgl.LWJGLException;
import org.lwjgl.input.Cursor;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;


/*
 * This should get the user's keyboard input and interpret it as navigation for a ship.  
 */

public class UserInput {
	private float throttle;
	private float steering;
	private double aimarc;
	public double trigx, trigy;
	public double scale;  // scale size to draw interface
	double screen_width;
	double screen_height;
	
	int aim_mode;  // 0 = mouse mode, 1 = buttom mode
	int quit;  // when I get a quit message from the use
	
	static final int MOUSE_AIM_MODE=0;
	static final int BUTTON_AIM_MODE=1;
	
	static final int OBJECTIVE_INACTIVE = 0;
	static final int OBJECTIVE_ACTIVE = 1;
	
	double money_display_time;  // how long the "Money = " display stays on
	boolean timer_display;
	RayText timertxt;
	boolean timer_freeze;
	int time; //integer for time count down in seconds
	double time_fraction;  // how much has passed up until 1 sec
	
	private int firebutton;
	int switch_ships;
	

	boolean target_pointer;
	double targetx;
	double targety;
	sketch arrow;
	double pointer_wave;
	boolean at_target;
	
	RayText arrowtxt;
	String arrow_string = " ";
	String dist_string = " ";
	
	sketch port_icon;
	sketch target_icon;
	sketch objective_icon;
	sketch map_icon;
	
	int target_mode;
	int past_mode;
	static final int TARGET_NOTHING = 0;
	static final int TARGET_PORT = 1;
	static final int TARGET_ENEMY = 2;
	static final int TARGET_OBJECTIVE = 3 ;
	int target_switch;  
	
	int objective_status;  // can I show an objective?
	int objective_toggle;  // when I click to see objectives
	
	RayText ticker;
	double tickx, ticky;
	boolean ticker_on;
	int tickstat;  // ticker status 0 = off, or fully scrolled, 1= showing
	String ticker_txt;

	MissionData md;
	boolean map_mode;
	double map_check;
	
	double centerx, centery;
	
	
	RayText moneytxt;
	double money_timer;
	
	RayText maptxt;
	
	double click_timer;
	double port_time_out;  // delay on accepting clicks because we just left the port interface
							// for example, I should not interpret the click on the corner for 'continue'
							// as switch ships or fire
	
	public final int MAX_BUTTONS = 10;  // for now there can be 10
	Cursor cb;
	
	public boolean TurnLeft() {
		if (Keyboard.isKeyDown(Keyboard.KEY_A)) {
			return true;
		}
		else return false;	}
	public boolean TurnRight() {
		if (Keyboard.isKeyDown(Keyboard.KEY_D)) {
		return true;
		} else return false;
	}
	public boolean GoForward() {
		if (Keyboard.isKeyDown(Keyboard.KEY_SPACE)) {
			return true;
		}
		else return false;
	}
	public boolean Forward() {
		if (Keyboard.isKeyDown(Keyboard.KEY_W)) {
			return true;
		}
		return false;
	}
	public boolean Backward() {
		if (Keyboard.isKeyDown(Keyboard.KEY_S)) {
			return true;
		}
		return false;
	}
	
	public boolean ShowMap() {

		return map_mode;
	}
	
	public boolean Reload() {
		if (Mouse.isButtonDown(1)) {
			return true;
		}
		return false;
	}
	
	
	public float GetThrottle() {
		return throttle;
	}
	
	public float GetAim() {
		return (float) aimarc;
	}
	
	public int FireButton() {
		return firebutton;
	}
	
	public void SetScale(double win_width, double win_height, double width, double height) {
		double worldx=win_width/width ;
		double worldy = win_height/height;
		scale= 10/(worldx+worldy)/2d;
		screen_width=width;
		screen_height=height;
		
		ticky=-screen_height/2+50;
		tickx=-screen_width/2;
	}
	
	
	public void SetTargetXY(double x, double y) {
		targetx=x;
		targety=y;
	}
	public void DrawTargetPointer(double centerx, double centery) {
		double mag;
		double dirx, diry;
		float arrowx, arroy;
		
		//if ((Math.abs(centerx-targetx)< screen_width) || (Math.abs(centery-targety)< screen_height)) 
		
		mag=Math.sqrt((centerx-targetx)*(centerx-targetx)+(centery-targety)*(centery-targety));
		if (mag> 200.0f)	{
			at_target=false;
			dirx=(centerx-targetx)/mag;
			diry=(centery-targety)/mag;
			double rot=Math.atan2(diry, dirx)+Math.PI;
			arrow.drawrot(centerx+Math.cos(rot)*240f, centery+Math.sin(rot)*240f, rot, 1.0d, (1+Math.cos(pointer_wave))/2d);
			arrow.drawrot(centerx+Math.cos(rot)*220f, centery+Math.sin(rot)*220f, rot, 1.0d, (1+Math.cos(pointer_wave+Math.PI/4d))/2d);			
			arrow.drawrot(centerx+Math.cos(rot)*200f, centery+Math.sin(rot)*200f, rot, 1.0d, (1+Math.cos(pointer_wave+Math.PI*2d/4d))/2d);
			arrow.drawrot(centerx+Math.cos(rot)*180f, centery+Math.sin(rot)*180f, rot, 1.0d, (1+Math.cos(pointer_wave+Math.PI*3d/3d))/2d);
			
			if (arrow_string!=null)
				arrowtxt.DrawScaleText((int) (centerx+Math.cos(rot)*220f), (int) (centery+Math.sin(rot)*220f), arrow_string, 0.5d);
			arrowtxt.DrawScaleText((int)(centerx+Math.cos(rot)*220f), (int) ((centery+Math.sin(rot)*220f)- 20), dist_string, 0.5d);
		}		
		else {
			at_target = true;
		}
		
	}
	
	public void DrawIcons(double centerx, double centery) {
//		port_icon.drawat((float)(centerx-md.win_width/2+50), (float) (centery+md.win_height/2-50));
		port_icon.drawrot((float)(centerx-md.win_width/2+25), (float) (centery+md.win_height/2-25), -Math.PI/2, 2.0d);
		target_icon.drawat((float)(centerx-md.win_width/2+25), (float) (centery-md.win_height/2+25));
		map_icon.drawat((float)(centerx + md.win_width/2-56), (float )(centery-md.win_height/2+25));
	}
	
	public void DrawObjectiveIcon(double centerx, double centery) {
		if (objective_status == OBJECTIVE_ACTIVE)
			objective_icon.drawat((float)(centerx + md.win_width/2-25),  (float) (centery+md.win_height/2-25) ) ;
	}
	
	public void SetPortTimeOut() {
		port_time_out= 2.0d;
	}
	
	
	public void TurnOnTicker(String new_text) {
		ticker_txt=new_text;
		ticker_on=true;
		tickstat=1;
		tickx=-1000000;  // a big number will cause the updater to rest its position	
	}
	
	public void TurnOffTicker() {
		tickstat=0;
		ticker_on = false;
	}
	
	public boolean TickerShowing() {
		if ((!ticker_on) || (tickstat==1))
			return false;
		return true;
	}
	
	public void DrawTicker(double centerx, double centery) {
		if (ticker_on)
		ticker.DrawFadeText((int) (tickx+centerx), (int) (ticky + centery),  ticker_txt, centerx-screen_width/2+screen_width/4, centerx+screen_width/2-screen_width/4); 
	}
	
	public  void SetTime(int timein) {
		time = timein;
	}
	
	
	public void TurnOnTime() {
		timer_freeze= false;
		timer_display = true;
	}
	
	public void FreezeTimer() {
		timer_freeze = true;
	}
	public void DrawTimer() {
		int minutes = time/60;
		int seconds = time%60;
		String mid =":";
		if (seconds < 10) {
			mid = ":0";
		}
		timertxt.ChangeFontColor(0.788f, 0.824f, 0.07f);
		timertxt.DrawText((int) (centerx -150), (int) (centery+md.win_height/2-40), String.valueOf(minutes) + mid + String.valueOf(seconds));
	}
	
	public void TurnOffTime() {
		timer_display = false;
		
	}
	public void Draw(double cx, double cy) {
		// draw buttons and controls, but relative to screen not the world.  
		centerx=cx;
		centery=cy;
		
		double x, y;
		if (money_timer < money_display_time) { // later fade in / out
			moneytxt.DrawText((int) (centerx +md.win_width/2-150), (int) (centery+md.win_height/2-40), "MONEY "+ String.valueOf(md.money[md.PLAYER_TEAM]));
		} else 
		{
			DrawObjectiveIcon(centerx, centery);
		}
		if (timer_display) {
			DrawTimer();
		}
		

		if (target_pointer) 
			DrawTargetPointer(centerx, centery);

		if (!ShowMap()) {
			DrawIcons(centerx, centery);
			DrawTicker(centerx, centery);
		} else
		{
			// draw the map explanation UI
			maptxt.DrawScaleText((int)(md.win_width*md.zoom_width*(-0.2d)), (int)(md.win_height*md.zoom_height*(-0.45d)), "CLICK ON AN ISLAND TO TARGET YOUR NAVIGATION", md.zoom_width);
		}

	}
	
	
	public void pollInput(double t, double win_width, double win_height, double width, double height) {	
		int x = Mouse.getX();
		int y = Mouse.getY();
		boolean button_press=false;
		firebutton =0;
		if (aim_mode==MOUSE_AIM_MODE) {
			aimarc=Math.atan2(((double) (x-win_width/2)), (double) (y-win_height/2));
		}

		
		if (Keyboard.isKeyDown(Keyboard.KEY_RIGHT)) {
			aimarc=aimarc+Math.PI/6*t*md.time_factor*0.01d;
			if (aimarc > Math.PI) aimarc-=2*Math.PI;
			aim_mode=BUTTON_AIM_MODE;

		}
		if (Keyboard.isKeyDown(Keyboard.KEY_LEFT)) {
			aimarc=aimarc-Math.PI/6*t*md.time_factor*0.01d;
			if (aimarc < -Math.PI) aimarc+=2*Math.PI;
			aim_mode=BUTTON_AIM_MODE;

		}
		
		trigx = Math.cos(aimarc);
		trigy = Math.sin(aimarc);
		
		if ((Keyboard.isKeyDown(Keyboard.KEY_UP)) || (Keyboard.isKeyDown(Keyboard.KEY_SPACE))) {
			firebutton=1;
			aim_mode=BUTTON_AIM_MODE;
		}
		port_time_out-=t;  // time out on mouse clicks because we just left the port interface
		if ((Mouse.isButtonDown(0)) && (port_time_out < 0)) {
			aim_mode=MOUSE_AIM_MODE;  
			
			if (map_mode == true) {
				if (map_check < 0d) {
					map_mode=false;  // no shooting in map mode
					// check to see if the player clicked on an island
					island isl = md.ClickOnPort(x,y, centerx, centery);
					if (isl!=null) {
						target_pointer=true;
						SetTargetXY(isl.xpos, isl.ypos);
						arrow_string=isl.name;
						target_mode= TARGET_PORT;
						md.MakeOneIslandList(isl);
					}
				}
			}


				if ((x < 70) && (y > md.win_height-70)) {
					button_press=true;
					if (click_timer> 3.0d) {
						click_timer=0.0d;
	
						target_mode= TARGET_PORT;
						target_switch=1;
	
					}


				}

		if (target_mode!= TARGET_OBJECTIVE) {		
				if ((x < 70) && (y < 70)) {
					button_press= true;
					if (click_timer > 300.0d/ md.time_factor) {
						if (target_mode== TARGET_ENEMY) {
							target_mode= TARGET_NOTHING;
							target_switch=1;
						} else {
							target_mode= TARGET_ENEMY;
							target_switch=1;
						}
						click_timer=0.0d;
					}
				}
				
				// also, if I don't have an objective targeted, I can target
				if ((x > md.win_width-70) && (y > md.win_height- 70)
						&& (money_timer > money_display_time)
						&& (objective_status == OBJECTIVE_ACTIVE)) {
					button_press= true;
					objective_toggle = 1;
				//	int xx = 0;
				//	xx= 1/xx;
				}
				
			}
				


				if ((y< 70) && (x> md.win_width-70)) {
					button_press= true;
					if (click_timer > 200d/md.time_factor) {
						map_check=1000.0d/md.time_factor;
						
						// should this toggle?  
						if (map_mode==false) {
							map_mode=true;
						}
				//		else
				//			map_mode=false;
	
						click_timer=0.0d;
					}

				}
					
			if (!button_press)  // but the mouse is down..	
				firebutton=1;
		}
		map_check=map_check-t;
		click_timer+=t;	
		
		if ((Keyboard.isKeyDown(Keyboard.KEY_TAB)) && (click_timer > 200.0d/md.time_factor)) {
			switch_ships = 1;
			click_timer=0.0d;
		
		}
		
		if (Keyboard.isKeyDown(Keyboard.KEY_ESCAPE)) {
			quit=1;  // you quit
		}
		if ((Keyboard.isKeyDown(Keyboard.KEY_M)) && (map_check<0.0d)) {
			map_check=500.0d/md.time_factor;
			if (map_mode==false) {
				map_mode=true;
			}
			else
				map_mode=false;
				
		}
		if ((Keyboard.isKeyDown(Keyboard.KEY_N)) && (map_check<0.0d) && (md.debug_mode >  0)) {
			map_check=500.0d/md.time_factor;
			if (md.show_ai==0) {
				md.show_ai=1;
			}
			else
				md.show_ai=0;
				
		}
		if ((Keyboard.isKeyDown(Keyboard.KEY_P)) && (click_timer > 200.0d/md.time_factor)
				/*&& (target_mode!= TARGET_OBJECTIVE)*/)  {
			target_mode= TARGET_PORT;
			click_timer=0.0d;
			target_switch=1;
		}
		if ((Keyboard.isKeyDown(Keyboard.KEY_E))&& (click_timer > 200.0d/md.time_factor)
				&& (target_mode != TARGET_OBJECTIVE))  {
			target_mode= TARGET_ENEMY;
			click_timer=0.0d;
			target_switch=1;
		}
		if ((Keyboard.isKeyDown(Keyboard.KEY_X)) && (click_timer > 200.0d/md.time_factor)) {
			target_mode= TARGET_NOTHING;
			click_timer=0.0d;
			target_switch=1;
		}

		
		// ticker
		tickx=tickx-md.time_factor*0.1d*t;
		if ((ticker_txt!=null) && (tickx < -width/2- ticker.GetLength(ticker_txt, 1.0d))) {
			tickx=width/2;
			tickstat=0;
		}
		// arrow
		pointer_wave+=t/2d;
		
		
		money_timer+=t;
		// money timer on
		if (md.turn_on_money==1) {
			if (money_timer> money_display_time)  // in other words its off
				money_timer=0.0d;  //its on
			md.turn_on_money=0;
		}	

	
		// timer
		if (!timer_freeze) 
			time_fraction+=t/10;
		if (time_fraction > 1.0d) {
			time_fraction-=1.0d;
			time-=1;
			if (time < 1)
				time=0;
		}
		

	}
	

	
	public void UpdateCursor() {
		IntBuffer ib = IntBuffer.allocate(10*10* 4);
		
		Mouse.getNativeCursor();
		
		try {
			Mouse.setNativeCursor(null);
			//cb = new Cursor(10, 10, 5, 5, 1, ib, null);
		}
		catch (LWJGLException e) {
		}	
	}
	public UserInput(MissionData mdin) {

		md=mdin;
		
		throttle=0.0f;
		steering=0.0f;



		map_mode=false;
		
		aim_mode=MOUSE_AIM_MODE;
		arrow=new sketch();

		switch_ships=0;
		
		arrow.numpoints=4;
		arrow.xmat[0]=-20f;
		arrow.ymat[0]=0f;
		arrow.xmat[1]=0f;
		arrow.ymat[1]=0f;
		
		arrow.xmat[0]=0f;
		arrow.ymat[0]=0f;
		arrow.xmat[1]=-10f;
		arrow.ymat[1]=-10f;
		arrow.xmat[2]=0f;
		arrow.ymat[2]=0f;
		arrow.xmat[3]=-10f;
		arrow.ymat[3]=10f;

		for (int i=0; i < arrow.numpoints/2; i++) {
			arrow.bcolor[i]=0.45f;
			arrow.rcolor[i]=0.75f;
			arrow.gcolor[i]=0.95f;
		}
	
		click_timer= 0.0d;
		port_icon= new sketch();

		port_icon.numpoints=10;
		
		port_icon.xmat[0]= - 5f;
		port_icon.ymat[0]= 0;
		port_icon.xmat[1]= 10f;
		port_icon.ymat[1]=0;
		
		port_icon.xmat[2]=0;
		port_icon.ymat[2]=-5f;
		port_icon.xmat[3]=0;
		port_icon.ymat[3]=5f;
		
		port_icon.xmat[4]=10f;
		port_icon.ymat[4]=- 3f;
		port_icon.xmat[5]=10f;
		port_icon.ymat[5]=3f;
		
		port_icon.xmat[6]=10f;
		port_icon.ymat[6]=-3f;
		port_icon.xmat[7]=7f;
		port_icon.ymat[7]=-5f;
		
		port_icon.xmat[8]=10f;
		port_icon.ymat[8]=3f;
		port_icon.xmat[9]=7f;
		port_icon.ymat[9]=5f;
		
		for (int count=0; count< port_icon.numpoints/2; count++) {
			port_icon.rcolor[count]=0.7f;
			port_icon.gcolor[count]=0.7f;
			port_icon.bcolor[count]=0.5f;
		}
			
		

		
		
		target_icon = new sketch();
		target_icon.numpoints=38;
		
		for (int i=0; i < 9; i++) {
			target_icon.xmat[i*2]=(float) (6d*Math.cos((double)(i/4d*Math.PI)));
			target_icon.ymat[i*2]=(float) (6d*Math.sin((double)(i/4d*Math.PI)));
			target_icon.xmat[i*2+1]=(float) (6d*Math.cos((double)((i+1)/4d*Math.PI)));
			target_icon.ymat[i*2+1]=(float) (6d*Math.sin((double)((i+1)/4d*Math.PI)));	
			
			target_icon.xmat[18+i*2]=(float) (12d*Math.cos((double)(i/4d*Math.PI)));
			target_icon.ymat[18+i*2]=(float) (12d*Math.sin((double)(i/4d*Math.PI)));
			target_icon.xmat[18+i*2+1]=(float) (12d*Math.cos((double)((i+1)/4d*Math.PI)));
			target_icon.ymat[18+i*2+1]=(float) (12d*Math.sin((double)((i+1)/4d*Math.PI)));	
		}
		target_icon.xmat[37]=(float) (0);
		target_icon.ymat[37]=(float) (-1);
		target_icon.xmat[38]=(float) (0);
		target_icon.ymat[38]=(float) (1);	
		
		for (int i=0; i < target_icon.numpoints/2; i++) {
			target_icon.bcolor[i]=0.0f;
			target_icon.rcolor[i]=0.9f;
			target_icon.gcolor[i]=0.0f;
		}		
		
		map_icon = new sketch();
		map_icon.CreateRectangle(0, 0, 48, 32);
		map_icon.xmat[8] = 24;
		map_icon.ymat[8] = 8;
		map_icon.xmat[9] = 24;
		map_icon.ymat[9] = 24;
		
		map_icon.xmat[10] = 24;
		map_icon.ymat[10] = 24;
		map_icon.xmat[11] = 18;
		map_icon.ymat[11] = 18;
		
		map_icon.xmat[12] = 24;
		map_icon.ymat[12] = 24;
		map_icon.xmat[13] = 30;
		map_icon.ymat[13] = 18;
		
		map_icon.numpoints = 14;
		
		map_icon.ChangeColor(0.2f, 0.2f, 0.8f);
		
		
		objective_icon = new sketch();
		objective_icon.numpoints = 6;
		
		objective_icon.xmat[0] = -10;
		objective_icon.ymat[0] = 20;
		objective_icon.xmat[1] = -10;
		objective_icon.ymat[1] = -20;

		objective_icon.xmat[2] = -10;
		objective_icon.ymat[2] = 20;		
		objective_icon.xmat[3] = 10;
		objective_icon.ymat[3] = 10;
		
		objective_icon.xmat[4] = 10;
		objective_icon.ymat[4] = 10;
		objective_icon.xmat[5] = -10;
		objective_icon.ymat[5] = 0;

		for (int i=0; i < objective_icon.numpoints/2; i++) {
			objective_icon.bcolor[i]=0.8f;
			objective_icon.rcolor[i]=0.8f;
			objective_icon.gcolor[i]=0.8f;
		}		
				
		
		objective_status = OBJECTIVE_INACTIVE;
		
		ticker=new RayText();
		tickstat=0;
		quit =0;
		
		money_display_time = 5000d/md.time_factor;
		moneytxt=new RayText();
		moneytxt.ChangeFontColor(0.2f, 0.8f, 0.2f);
		
		arrowtxt= new RayText();
		target_pointer = false;
		//arrow_string = " ";
		
		maptxt= new RayText();
		timertxt = new RayText();
		timer_display = false;
		timer_freeze = false;
		SetTime(300);
		time_fraction = 0.0d;
		
	}
}
