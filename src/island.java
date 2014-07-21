import org.lwjgl.opengl.GL11;


public class island extends WorldThing {

	public enum iType {EMPTY, ISLAND};
	sketch isketch;
	sketch psketch; // port sketch
	iType type;
	RayText rt;
	String name;
	int has_port;
	PortData pd;
	boolean hasname;
	
	Path OrbitPath;
	
	double portx, porty;
	double world_portx, world_porty;  // in world coordinates
	
	public void draw(double scale, MissionData md) {
		isketch.drawrot( xpos,  ypos,  rotation, scale);
		if (has_port!=0) {
			psketch.ChangeColor(md.teamR(pd.team), md.teamG(pd.team), md.teamB(pd.team));
			rt.ChangeFontColor(md.teamR(pd.team), md.teamG(pd.team), md.teamB(pd.team));
			psketch.drawrot(xpos, ypos, rotation, scale);

		}
		if (hasname)	{
			
			double wid=rt.GetLength(name, scale)/2;
			double ht=rt.GetHeight(scale)/2;
			rt.DrawScaleText((int)(xpos- wid), (int)(ypos-ht), name, scale);  // should be string width // or rt shoudl have a center function
			
			double x=(xpos-wid)+rt.GetLength(name, scale)+20*scale;
			double y=(ypos-ht)+rt.GetLength(name, scale);
			
			DrawFlag(x,y);
		}


	}
	
	public void DrawFlag(double x, double y) {
		// set color
		// draw a triangle
	}
	public boolean Collide(sketch skt) {
		return isketch.Collide(skt);
	}
	
	public void SetName(String n) {
		name=n;
		hasname=true;
	}
	public void SetWorldPortPoints(double scale) {
		double x=portx*scale;
		double y=porty*scale;
		world_portx=x*Math.cos(rotation)-y*Math.sin(rotation)+xpos;
		world_porty=x*Math.sin(rotation)+y*Math.cos(rotation)+ypos;
	}
	
	island() {
		type=island.iType.EMPTY;
		rt = new RayText();
		OrbitPath= new Path();
		hasname=false;
	}
	island(double xin, double yin, double rin, String iname) {
		type=island.iType.ISLAND;
		xpos=xin;
		ypos=yin;
		rotation=rin;
		isketch=new sketch(iname);
		rt = new RayText();
		hasname=false;
		has_port=0;
		OrbitPath= new Path();
	}

}
