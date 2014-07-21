import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;


public class ObjectiveDisplay {
	UserInput ui;
	MissionData md;
	
	sketch obj_button[];
	sketch down_button;
	sketch done_button;
	
	WorldThing btn_points[];
	
	RayText obj_text;
	int on_page;
	int page_lines;
	int num_lines;
	boolean more_btn;
	double resumex, resumey, resume_width;
	sketch resumebtn;
	
	int completed_count;  // how many has the player done
	
	double click_delay;
	
	static final int NO_CHOICE_MADE = 0;
	static final int CHOICE_MADE = 1;

	static final int SCROLL_DOWN = -1;
	static final int RESUME	 = -2;
	
	public int LinesPerScreen() {
		return (md.win_height-md.win_height/5)/obj_text.GetHeight(4.0);
	}
	
	public int FirstLine(int page) {
		return on_page*(LinesPerScreen()-1);
	}
	
	public int LastLine(int page) {
		int last = md.episode_list.size(); //- on_page*(LinesPerScreen()-1);
		return Math.min(last, on_page*(LinesPerScreen()-1)+LinesPerScreen()-1);
	}
	
	public float ButtonX() {
		return (float)(md.win_width *0.1);
	}
	
	public float ButtonY(int i, int on_page, RayText obj_text) {
		int y = (int) (md.win_height*0.9d - (num_lines/2)*obj_text.GetHeight(1.5d));
		return (float)( y-obj_text.GetHeight(4.0d)*(i-on_page*(LinesPerScreen()-1)) + obj_text.GetHeight(1.0d));
	}
	
	public int LineY(int i, int on_page, RayText obj_text, double scale) {
		return (int) (ButtonY(i, on_page, obj_text)-obj_text.GetHeight(scale)/2);
	}
	
	public void DrawDisplay() {
	
		obj_text.DrawScaleText(md.win_width/20, (int)(md.win_height*0.9d), "FLEET MISSIONS", 2.5d);
		
		String progress = "COMPLETED: " + String.valueOf(completed_count) + " / " + String.valueOf(md.episode_list.size());
		obj_text.DrawScaleText(md.win_width/2, (int)(md.win_height*0.9d), progress, 1.5d);
		
		num_lines = LinesPerScreen();
		
		page_lines = md.episode_list.size()-(num_lines-2)*on_page;
		more_btn = false;
		
		if (page_lines > num_lines-2) {
			// show the next page icon
			page_lines = num_lines-2;
		}

		int x= (int) (md.win_width*0.15);
		int y= (int) (md.win_height*0.9d - (num_lines/2) * obj_text.GetHeight(1.5d));
		int i = 0;
	//	for (i = (num_lines-2)*on_page; i < page_lines+(num_lines-2)*on_page; i++){	
		
		int first = FirstLine(on_page);
		int last = LastLine(on_page);
		
		if (md.episode_list.size() > last) 
			more_btn = true;
		
		for (i = FirstLine(on_page); i < LastLine(on_page); i++) {
			if (!md.episode_list.get(i).completed) {
				obj_text.ChangeFontColor(0.4f, 0.4f, 0.9f);
			} else
				obj_text.ChangeFontColor(0.2f, 0.2f, 0.5f);
			obj_text.DrawScaleText(x, LineY(i, on_page, obj_text, 1.5d), md.episode_list.get(i).GetTitle(), 1.5d);
			obj_text.ChangeFontColor(0.1f, 0.8f, 0.1f);


				
			String money = String.valueOf(md.episode_list.get(i).bonus);
			obj_text.DrawScaleText((int) (md.win_width*0.9f-obj_text.GetLength(money, 1.5d)), y, money, 1.5d);
			if (!md.episode_list.get(i).completed) {
				obj_text.ChangeFontColor(1f, 1f, 1f);
			} else
				obj_text.ChangeFontColor(0.5f, 0.5f, 0.5f);
			
			obj_text.DrawScaleText(x, y-obj_text.GetHeight(1.0d), md.episode_list.get(i).GetDescription(), 1.0d);
			// draw a little circle go thing
			
			obj_button[i].CreateCircle(ButtonX() , ButtonY(i, on_page, obj_text), 25f);
			obj_button[i].AddGoTriangle(ButtonX(), ButtonY(i, on_page, obj_text), 15f);

			if (md.episode_list.get(i).completed) 
				obj_button[i].ChangeColor(0.5f, 0.5f, 0.5f);
			obj_button[i].drawit();
			
			y=y-obj_text.GetHeight(4.0d);	
		}
		if (more_btn) {
			obj_text.ChangeFontColor(0.1f, 0.7f, 0.1f);
			obj_text.DrawScaleText(x, LineY(i, on_page, obj_text, 1.5d), "MORE MISSIONS", 1.5d);
			obj_text.ChangeFontColor(1f, 1f, 1f);
			obj_text.DrawScaleText(x, y-obj_text.GetHeight(1.0d), "CLICK HERE TO SEE MORE UNLOCKED MISSIONS", 1.0d);
		//	obj_button[i].CreateCircle((float)(md.win_width *0.1) ,(float)( y + obj_text.GetHeight(1.0d)), 25f);
		//	obj_button[i].AddGoTriangle((float)(md.win_width *0.1) ,(float)( y + obj_text.GetHeight(1.0d)), 15f);
			obj_button[i].CreateCircle(ButtonX() , ButtonY(i, on_page, obj_text), 25f);
			obj_button[i].AddGoTriangle(ButtonX(), ButtonY(i, on_page, obj_text), 15f);
			obj_button[i].drawit();

		}
		
		resumebtn.CreateCircle((float) resumex, (float) resumey, (float) resume_width);
		resumebtn.ChangeColor(0.1f, 1.0f, 0.1f);
		obj_text.ChangeFontColor(0.1f, 1.0f, 0.1f);
		obj_text.DrawScaleText((int) (resumex-obj_text.GetLength("RESUME", 1.0d)/2d), (int) (resumey - obj_text.GetHeight(1.0d)/2), "RESUME", 1.0d);
		resumebtn.drawit();
	}
	
	public int CheckDisplay() {
		
		//int n = md.episode_list.size();
		if ((click_delay < 0) && (Mouse.isButtonDown(0))) {
			click_delay = 5.0d;
			// if I click on a hull, add it	
			int x = Mouse.getX();
			int y = Mouse.getY();
			//  I need to make this a formula
			double testy = ButtonX();//md.win_height*0.9d - (num_lines/2) * obj_text.GetHeight(1.5d);
			double testx = (md.win_width *0.1);	
			int i = 0;
//			for (i = (num_lines-2)*on_page; i < page_lines+(num_lines-2)*on_page; i++) {
//			for (int i = 0; i < page_lines+1; i++) {
			for (i = FirstLine(on_page); i < LastLine(on_page); i++) {
				// for debugging
				testy=ButtonY(i, on_page, obj_text);
			//	double num = (x-testx)*(x-testx) + (y-testy)*(y-testy);
				if (((x-testx)*(x-testx) + (y-testy)*(y-testy)) < 625)
					return i;
				//testy=testy-obj_text.GetHeight(4.0d);	
			}
			if (more_btn) {
				testy=ButtonY(i, on_page, obj_text);
				if (((x-testx)*(x-testx) + (y-testy)*(y-testy)) < 625)
					return SCROLL_DOWN;
			}
			if ((x - resumex)*(x- resumex) + (y-resumey)*(y-resumey) < resume_width*resume_width)
				return RESUME;
			
		}
		
		return md.episode_list.size();
	}
	
	public int ShowDisplay(double time) {

		//mode = SHIP_CUSTOMIZER;
			

		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glLoadIdentity();
		GL11.glOrtho(0, md.win_width, 0, md.win_height, 1, -1);
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		

		DrawDisplay();
		int disp = CheckDisplay();
		
		if (disp == SCROLL_DOWN) { // md.episode_list.size()) {
			// you clicked on an episode
			// start the episode
			// close the display
			on_page++;
		} else {
			if (disp==RESUME) {
				return CHOICE_MADE;
			} else {

				if (disp < md.episode_list.size()) {
					md.episode_list.get(disp).Start();
					ui.objective_status = ui.OBJECTIVE_INACTIVE;  // I just chose
					return CHOICE_MADE;
				}
			}

		}


		click_delay=click_delay-time;
		Display.update();
		
		return NO_CHOICE_MADE;
	}
	
	public void Init() {
		completed_count = 0;
		for (int i=0; i < md.episode_list.size(); i++)
			if (md.episode_list.get(i).completed)
				completed_count++;

		resume_width = md.win_width/30d;
		resumex= md.win_width*14d/15d;
		resumey= md.win_height/15d;
		resumebtn = new sketch();
		
		on_page = 0;
	}
	
	ObjectiveDisplay(UserInput uin, MissionData mdin) {
		ui=uin;
		md=mdin;
		obj_text = new RayText();
		obj_button = new sketch[md.MAX_EPISODES];
		btn_points = new WorldThing[md.MAX_EPISODES];
		
		sketch done_button = new sketch();
		
		
		for (int i=0; i < md.MAX_EPISODES; i++)	{
			obj_button[i] = new sketch();
			btn_points[i] = new WorldThing();
		}

		on_page = 0;
	}
}
