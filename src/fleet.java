import java.util.ArrayList;
import java.util.List;


public class fleet {
	int team;
	
	List<ship> ships;  // = new ArrayList<ship>();

	
	java.util.Iterator<ship> index;
//	include a function to give a command to a fleet
	// and to reorder them in terms of who follow who

//	java.util.Iterator<PathData> it = open.iterator();
	public void MakeFollowChain(ship s, ShipManager sm, IslandManager im, MissionData md) {
		java.util.Iterator<ship> it = ships.iterator();
		int lead = 0;
		while (it.hasNext()) {
			if (it.next()==s)
				break;
			lead++;
		}		
		ship lead_ship = s;
		for (int i = 0; i < ships.size(); i++) {
			ship follow = ships.get((lead+i)%ships.size());  // modular so that you loop around the list
			if (follow!=s) {
				follow.AttachAI(sm, im, md);
				follow.ai.StartFollow(follow, lead_ship, 0, 0);
				lead_ship=follow;  // it's a chain
			}
		}
	}
	
	public ship GetTrailingShip(ship s) {
		ship trailer=null;
		for (int i=0; i < ships.size(); i++) {
			if (s==ships.get(i))
				trailer=ships.get((i+1)%ships.size());
		}
		if (trailer==null)
			trailer=ships.get(0);
		
		return trailer;
	}
	
	public int AddShip(ship s) {
		// check to see if I already have it
		if (!ships.contains(s))
			ships.add(s);
		return ships.size();
	}
	public void RemoveShip(int id) {
		java.util.Iterator<ship> it = ships.iterator();
		while (it.hasNext()) {
			ship testship = it.next();
			if (testship.id==id)
				it.remove();
		}	
	}
	
	public int count() {
		return ships.size();
	}
	
	public ship GetShip(int id){
		java.util.Iterator<ship> it = ships.iterator();
		while (it.hasNext()) {
			ship testship = it.next();
			if (testship.id==id)
				return testship;
		}
		return null;
	}
	public void StartShipList() {
		index=ships.iterator();
	}

	public boolean HasNextShip() {
		return index.hasNext();
	}
	public ship GetNextShip() {
		if (index.hasNext()) {
			return index.next();
		}
		else return null;
	}
	
	public void Empty() {
		ships.clear();
	}
	public fleet() {
		ships= new ArrayList<ship>();

	}
}
