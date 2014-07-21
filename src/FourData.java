

import java.io.File;
import java.io.IOException;


public class FourData {

	public class component {
		float colorR, colorG, colorB;
		sketch csketch;
		String component_name;
		component() {
			csketch=new sketch();
			component_name="unitialized";
		}
		public void getmsg(int gmsg) {
			// game tells the component to do something?
			// do I need message data?
		}
	}
	// the slots for what goes into a ship
	public class slot {
		
	}
	public class ship {
		float direction;
		float x, y;
		
		
		public void Move(float x, float y) {
			
		}
		public void Rotate(float x, float y) {
			
		}
	}

	public FourData()  {
	
	System.out.println("Four Data Constuctor");
		File fd= new File("fourdata.dat");
		try {
			fd.createNewFile();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  // gets created
	}
	public void amethod() {
		System.out.println("called a method");		
	}
	
}
