
public class Button extends WorldThing {
	int height, width;
	public int state;
	public int screenx, screeny;
	public int screen_width, screen_height;
	enum btype { EMPTY, PUSH, TOGGLE }
	btype type;
	boolean has_border;
	sketch bsketch;
	
	double rest_time;
	double last_time;
	
	public void Clicked(double t) {
		// do what you do to to the sate
		if (last_time > rest_time) {
			last_time=0.0d;
			switch (type) {
			case TOGGLE:
				if (state==0) {
					state=1;
				}
				else
					state=0;
				break;
			case PUSH:
				state=1;
				break;
			}
		}
			
		last_time=last_time+t;
	}
	
	public void NotClicked(double t) {
		// let it relapse
		last_time=last_time+t;
		if (type==btype.PUSH) {
			state=0;
		}
	}
	
	public boolean InBounds(int x, int y) {
	//	System.out.println("Click x " + x + " y " + y + " xpos " + xpos + " ypos " + ypos);
		if ((Math.abs(xpos-x)<20) &&  (Math.abs(ypos-y)<20)) {

			return true;
		}
		return false;
	}
	
	
	public boolean InScreenBounds(int x, int y) {
		if ((x < (screenx+screen_width/2)) && 
				(y < (screeny + screen_height)) && (x > (screenx - screen_width/2)) 
				&& (y > (screeny - screen_height/2)) )
			return true;
		
		return false;
	}
	public void Draw() {
		bsketch.drawat((float)xpos, (float)ypos);
		if (has_border) {
			// waaaaaawdadraw the border
		}
	}
	
	public void DrawAt(double x, double y, double s) {
		bsketch.drawrot((float)(xpos +x), (float)(ypos +y), 0d, s);
		if (has_border) {
			// draw the border
		}
	}
	
	Button() {
		type= btype.EMPTY;
		height= 0;
		width = 0;
		rest_time=0.5d;
		last_time=0.0d;		
	}
	Button(String filename) {
		bsketch= new sketch(filename);
		type=btype.TOGGLE;
		
		rest_time=1.0d;
		last_time=0.0d;
	}

}
