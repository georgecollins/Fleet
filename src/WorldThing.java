
public class WorldThing {
	public double xpos,ypos, rotation;
	int		id;
	
	int visible[];  // visible, submerged (semi) invisible
	// delete visible?  Is it used? 
	
	public void SetID(int idin) {
		id=idin;
	}
	public double DistanceSq(WorldThing wt) {
		return (wt.xpos-xpos)*(wt.xpos-xpos)+(wt.ypos-ypos)*(wt.ypos-ypos);
	}
	
	public double rotX(double r) {
		return xpos*Math.cos(r)-ypos*Math.sin(r);

	}
	
	public double rotY(double r) {
		return xpos*Math.sin(r)+ypos*Math.cos(r);
	}
	
	public WorldThing midpoint(WorldThing p1, WorldThing p2) {
		xpos = (p1.xpos+p2.xpos)/2d;
		ypos = (p1.ypos+p2.ypos)/2d;
		rotation = (p1.rotation + p2.rotation)/2d;
		return this;
	}
	
	public WorldThing offset(WorldThing p1, WorldThing p2) {
		xpos = p1.xpos - p2.xpos;
		ypos = p1.ypos - p2.ypos;
		rotation = p1.rotation - p2.rotation;
		return this;
	}
	
	public WorldThing CopyTo(WorldThing wt) {
		wt.xpos=xpos;
		wt.ypos=ypos;
		wt.rotation = rotation;
		return wt;
	}
	
	WorldThing() {
		
	}
	WorldThing(double xin, double yin, double rin) {
		xpos=xin;
		ypos=yin;
		rotation=rin;
		visible = new int[FourExample.MAX_TEAMS];
	}

}
