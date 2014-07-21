
public class PortData {
	
	// put all the information about this port into this structure
	// All ports are owned by islands, you don't need to point up
	// this lets the content of the port be permanent

	int team;
	static final int MAX_SHIPS_IN_PORT = 8;

	static final int MAX_HULLS_IN_PORT = 5;
	static final int MAX_COMPONENTS_IN_PORT = 6;
	int hull_count;
	int component_count;
	


	ComponentData[] components;
	HullData[] hulls;
	ship active_ship;
	int prices[];
	
	

	
	public void FillComponents() {
		component_count=3;
		int type_pick;
		for (int i=0; i< component_count; i++) {
			components[i]=new ComponentData();
		// randomly pick a type
			type_pick=(int)(Math.random()*3d);
			switch (type_pick) {
			case 0:
				components[i].type=ComponentData.ctype.GUN;
				components[i].guncount=(int)(Math.random()*3d)+1;
				components[i].fire_rate=(int)(Math.random()*3d);
				components[i].shot_speed=(int)(Math.random()*3d);
				components[i].clip_size=(int)(Math.random()*4d)+1;
				components[i].BuildGunSketch();
				break;
			case 1:
				components[i].type=ComponentData.ctype.FIXEDGUN;
				components[i].guncount=(int)(Math.random()*3d)+1;
				components[i].fire_rate=(int)(Math.random()*3d);
				components[i].shot_speed=(int)(Math.random()*3d);
				components[i].clip_size=(int)(Math.random()*4d)+1;
				components[i].BuildFixedGunSketch();
				break;			
			case 2:
				components[i].type=ComponentData.ctype.TORPEDO;
				components[i].guncount=(int)(Math.random()*3d)+1;
				components[i].fire_rate=(int)(Math.random()*3d);
				components[i].shot_speed=(int)(Math.random()*3d);
				components[i].clip_size=(int)(Math.random()*4d)+1;
				components[i].BuildTorpedoSketch();
				break;
			}
		
		// then fill in the values
		// then draw the sketch	
		}
		
	}
	

	public void SetPrices(int n) {
		prices=new int[n];
		for (int i=0; i < n; i++) {
			prices[i]=(int)(Math.random()*161)+40;
		}
	}
	
	PortData() {

		hulls = new HullData[MAX_HULLS_IN_PORT];
		components = new ComponentData[MAX_COMPONENTS_IN_PORT];
	}
}
