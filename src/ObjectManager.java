
public class ObjectManager {

	public double offsetx;
	public double offsety;

	MissionData md;

	void SetOffset(MissionData md, int xseg, int yseg) {
		offsetx= (xseg-((int)md.segment_width/2))*md.world_width;
		offsety= (yseg-((int)md.segment_height/2))*md.world_height;
	}
}
