import org.lwjgl.opengl.GL11;


public class GameScreen {
	/*
	 * This is an object that has the common functions of
	 * Mission (really mission screen)
	 * Pause Screen
	 * Mission Select
	 * Build Screen - will be choose ship at first
	 */
	MissionData md;
	UserInput ui;
	MessagePump mp;
	
	public void SetUpDisplay() {
		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glLoadIdentity();
		GL11.glOrtho(0, md.win_width, 0, md.win_height,  1, -1);
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		
		ui.SetScale(md.win_width, md.win_height, md.width, md.height);
		
		
	}
	
	// Display Loop??
	GameScreen() {
		
	}
	GameScreen(MissionData mdin) {
		md=mdin;
		mp= new MessagePump(md); // why make a new one?
		ui = new UserInput(md);		// why make a new one?
		// the scale may not always be the same
		ui.SetScale(md.win_width, md.win_height, md.win_width, md.win_height);

	}
}
