import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;


public class PauseScreen {
	// this is the screen that happens when you pause the game
	MissionData md;
	UserInput ui;
	MessagePump mp;

	int doneid;
	public void SetUpDisplay() {
		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glLoadIdentity();
		GL11.glOrtho(0, md.win_width, 0, md.win_height,  1, -1);
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		
		ui.SetScale(md.win_width, md.win_height, md.width, md.height);
		
		
	}
	
	public int DisplayLoop()  {

		
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT); 
		ui.pollInput(1, md.win_width, md.win_height, md.win_width, md.win_height);

		ui.Draw(md.width/2, md.height/2);
		
		Display.update();
		
		return FourExample.PAUSE_SCREEN;
	}
	
	PauseScreen(MissionData mdin) {
		md=mdin;
	
		ui = new UserInput(md);		
		ui.SetScale(md.win_width, md.win_height, md.win_width, md.win_height);

	}

}
