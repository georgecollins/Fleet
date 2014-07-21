import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;

/**
 * 
 */

/**
 * @author George
 *
 */
public class ModScreen {
	int shipid;
	int win_height, win_width;
	
	sketch hull_icons[];
	
	public void Display() {
		// draw the interface, update based on the mouse
		int max_icons=3;
		hull_icons = new sketch[max_icons];
		for (int count=0; count < max_icons; count ++) {
			switch (count) {
				case 0: 
					hull_icons[count] = new sketch("hull.dat");
					break;
				case 1:	
					hull_icons[count] = new sketch("destoryer");
					break;
				case 3:
					hull_icons[count] = new sketch("destoryer2");
					break;
			}
		}
		
		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glLoadIdentity();
		GL11.glOrtho(0, win_width, 0, win_height, 1, -1);
		GL11.glMatrixMode(GL11.GL_MODELVIEW);

		while (!Display.isCloseRequested()) {
			GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT); 
			

			Display.update();
			
			if (Mouse.isButtonDown(0)) {
					int x = Mouse.getX();
					int y = Mouse.getY();
				}
		}
		Display.destroy();
	
	}
	public void Init(int id, int w, int h) {
		shipid=id;
		win_width = w;
		win_height = h;
		
	}
	
	ModScreen() {
	}
}
