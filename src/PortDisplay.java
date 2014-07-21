import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;


public class PortDisplay {

	UserInput ui;
	MissionData md;	
	ShipManager localsm;
	int playerid;
	
	double click_delay;
	
	// port data (move into something?)
	island port_island;
	RayText port_text;

	ship[] port_ships;
	int ship_count;
	HullData[] hulls;
	
	int hull_count;
	static final int max_port_ship_count = 10;
	int active_ship;
	
	int drag_state;  // 0 = nothing, 1 drag comp, 2 drag pickup
	int istate;  // info display state
	final int DRAG_NOTHING= 0;
	final int DRAG_COMPONENT = 1;
	final int DRAG_PICKUP=2;
	
	final int SHOW_NOTHING = 0;
	final int SHOW_COMPONENT = 1;
	final int SHOW_PICKUP = 2;
	
	component drag_comp;
	component icomp;  // inspection component
	boolean  icomp_paid_for;  // I don't have to pay for it to pick it up
	boolean ipickup_paid_for;

	pickup drag_pickup;
	pickup ipickup;  // inspection pickup
	boolean pickup_paid_for;
	int from_hardpoint;
	
	pickup price_pickup[];
	
	int mode;
	public static final int HULL_BUY_MODE  = 1;
	public static final int SHIP_CUSTOMIZER = 2;
	
	double sellx, selly, sell_width;
	double cboxx, cboxy, cbox_width, cbox_height;
	double iboxx, iboxy, ibox_width;  // inspector
	// repair box
	double repairx, repairy, repair_width, repair_height;
	sketch repair_box;
	
	double scrapx, scrapy, scrap_width, scrap_height;  // only show the scrap box if > 1 ships
	sketch scrap_box;  
	boolean can_scrap;  // can't if it is your only ship
	
	sketch comp_box;
	
	public void SetupDisplay(int userid, island pis, ShipManager sm){
		port_island=pis;
		
		hulls=port_island.pd.hulls;
		hull_count=port_island.pd.hull_count;
		
		localsm = sm;
		playerid=userid;
		
		port_text=new RayText();
		repair_box=new sketch();
		scrap_box = new sketch();
		comp_box = new sketch();
		
		drag_state= DRAG_NOTHING;
		click_delay = 0d;
		
		icomp=null;
		drag_comp=null;	
		mode= HULL_BUY_MODE;
		ship_count=0;
	
		
		ship s=localsm.GetShip(userid);
		
		
		if ((s!=null) && (md.userfleet.GetShip(userid)==null)) {
			port_ships[ship_count]=s;
			port_ships[ship_count].rotation=0;
			port_ships[ship_count].speed=0;
			ship_count++;
		}
		
		
		md.userfleet.StartShipList();
		
		while (md.userfleet.HasNextShip()) {
			//port_island.pd.AddShip(md.userfleet.GetNextShip());
			port_ships[ship_count]=md.userfleet.GetNextShip();
			localsm.DeleteShip(port_ships[ship_count].id);
			port_ships[ship_count].rotation=0;
			port_ships[ship_count].speed=0;
			ship_count++;
		}
		
		if (ship_count > 1) {
			can_scrap = true;
		} else can_scrap = false;

		playerid=userid;
		localsm=sm;
	}
	
	private void PositionShip(ship s, int count) {
		port_ships[count].xpos=md.win_width/10*(count+3);
		port_ships[count].ypos=md.win_height/5;

	}
	
	public void SetupModDisplay() {
		// find the island wherever we are


		for (int count=0; count<ship_count;count++ ) {
		
			if (port_ships[count].id!=playerid) {
			  PositionShip(port_ships[count], count);
			}
			else {
				active_ship=count;
				port_ships[active_ship].xpos=md.win_width/2;
				port_ships[active_ship].ypos=md.win_height/2;
			}
		}

			
		
		for (int i=0; i < 4; i++) {
			price_pickup[i]=new pickup();
			price_pickup[i].SetPickupType(i);
		}

		
		// organize them in a row, show their stats
		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glLoadIdentity();
		GL11.glOrtho(0, md.win_width, 0, md.win_height, 1, -1);
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		

		repairx = md.win_width/20;
		repairy =  md.win_height/3;// md.win_height/2;
		repair_width = port_text.GetLength("REPAIR",1.5d);
		
		repair_height = port_text.GetHeight(1.5d)+ port_text.GetHeight(1.0d);
		
	//	repair_box.CreateRectangle((float)repairx-2, (float)repairy-2, (float) (repair_width+4), (float) (repair_height+4));
		repair_box.CreateCircle((float)(repairx+repair_width/2), (float)(repairy+repair_height/2), (float) (repair_width*0.666));
	
		repair_box.ChangeColor(0.0f, 0.75f,0.0f);
		
		scrapx =  md.win_width *7f/10f;  //repairx;
		scrapy = repairy;  //-repair_height*3f;
		
		scrap_width = repair_width;
		scrap_height = repair_height;
		
		scrap_box.CreateCircle((float)(scrapx+scrap_width/2), (float)(scrapy+scrap_height/2), (float) (scrap_width*0.666));

	//	scrap_box.CreateRectangle((float)scrapx-2, (float)scrapy-2, (float) (scrap_width+4), (float) (scrap_height+4));
		scrap_box.ChangeColor(0.7f, 0.3f,0.3f);
	
		cboxx=Math.max(1, ComponentX(0)-40);
		cboxy=Math.max(1, ComponentY(0)+40);
		//cboxx=10;
		//cboxy=md.win_height-md.win_height/10;
		//cbox_width=port_island.pd.component_count*md.win_width/20;
		cbox_width = md.win_width/20*6 + 40;  
		cbox_height= ComponentY(0) - ComponentY(3);  // md.win_height/5;  //160;
		comp_box.CreateRectangle((float)cboxx, (float)cboxy, (float) (cbox_width), (float) (-cbox_height));
		
		mode = SHIP_CUSTOMIZER;
	}
	
	public void DrawDragObj(int x, int y) {
		// draw whatever I am dragging
		
		if (drag_state==DRAG_PICKUP) {
			drag_pickup.xpos=x;
			drag_pickup.ypos=y;
			drag_pickup.Draw(2.0d);
		}
		if (drag_state==DRAG_COMPONENT) {
			drag_comp.draw_inactive(x, y, 0, 2.0d); //drawrot(x, y, 0, 2.0d);
		}
	}
	
	public int ComponentSelect(int x, int y) {
		int selected= port_ships[active_ship].component_count;
		
		for (int i=0; i<port_ships[active_ship].component_count; i++)
			if (port_ships[active_ship].ComponentBounds(i, 2.0d, (x-md.win_width/2), (y-md.win_height/2))) {
				
				if (drag_state==DRAG_NOTHING) {

					if ((port_ships[active_ship].scomponent[i].cd.type== ComponentData.ctype.CARGO) && 
							(port_ships[active_ship].scomponent[i].cd.holding!=0)){
								
						drag_state=DRAG_PICKUP;
						// copy the thing
						drag_pickup= new pickup();
					//	drag_pickup.psketch=drag_pickup.crate[port_ships[active_ship].scomponent[i].cd.holding-1];
						drag_pickup.SetPickupType(port_ships[active_ship].scomponent[i].cd.holding);
						port_ships[active_ship].scomponent[i].RemoveCrate();    //cd.holding=0;
						pickup_paid_for=true;
					} else {
						if (port_ships[active_ship].scomponent[i].used!=0) {
							drag_state=DRAG_COMPONENT;
							drag_comp = new component();
							drag_comp.CopyFrom(port_ships[active_ship].scomponent[i]);
							port_ships[active_ship].scomponent[i].used=0;
							icomp_paid_for=true;
							from_hardpoint = i;
							
							istate= SHOW_COMPONENT;
							icomp= drag_comp;
						}
					}
	//				port_ships[active_ship].scomponent[i].damaged=1;
					break;
				}
				
				if ((drag_state==DRAG_COMPONENT) && 
						((drag_comp.cd.GetCost()-1< md.money[md.PLAYER_TEAM]) || (icomp_paid_for)))  // can I afford it?  
						{
					port_ships[active_ship].scomponent[i].used=1;
					// add the component to the team inventory

					if (!icomp_paid_for) {
						if (drag_comp.cd.type!= ComponentData.ctype.CARGO)
							md.AddTeamComponent(drag_comp.cd, md.PLAYER_TEAM);
						md.money[md.PLAYER_TEAM]-=drag_comp.cd.GetCost();  // pay for it
					}
					port_ships[active_ship].AttachComponent(drag_comp, i);
					//scomponent[i] = drag_comp;		
					drag_state=DRAG_NOTHING;
					istate= SHOW_NOTHING;
					icomp = null;
				}

				if (drag_state == DRAG_PICKUP) {
					int pt= drag_pickup.GetPickupType();
					if ((port_ships[active_ship].scomponent[i].cd.type== ComponentData.ctype.CARGO) 
							&& (port_ships[active_ship].scomponent[i].cd.holding==0)
							&& (md.money[md.PLAYER_TEAM] > port_island.pd.prices[pt]-1)) {
						drag_state = DRAG_NOTHING;
						port_ships[active_ship].scomponent[i].cd.holding = pt;
						port_ships[active_ship].scomponent[i].BuildCrate(pt);
						if (pickup_paid_for==false) {
							md.money[md.PLAYER_TEAM]-=port_island.pd.prices[pt];
							// economics hack
							port_island.pd.prices[pt]+=1;  // more I buy, prices go up
						}
						drag_pickup = null;
					}
					
				}
				
			}
		
		return selected;
	}
	
	public int ActiveShipInterface(int current, int x, int y) {

			if (y < md.win_height/4) {
				for (int i=0; i < ship_count; i++) {
					int ship_center = (int)(md.win_width/10d*(i+3));
					if ((x < (ship_center+md.win_width/20d)) && (x > ship_center-md.win_width/20d)) {
						current=i;
					}
					
				}
			}
		return current;
	}
	
	
	public void DrawActiveShipInfo() {
		RayText sinfo;
		sinfo=new RayText();
		ship aship=port_ships[active_ship];  //. to save typing
		double scale= 2.0d;
		int startx= (int) (md.win_width/2 + aship.hd.width *scale);  
		int starty= (int) (md.win_height/2 + aship.hd.full_length/2);  
		
		sinfo.DrawScaleText(startx, starty, "SPEED "+ String.valueOf((int) (aship.GetMaxSpeed())), 0.75d);
		starty=starty-30;
		sinfo.DrawScaleText(startx, starty, "HP " + String.valueOf(aship.health), 0.75d);
		starty=starty-30;
		sinfo.DrawScaleText(startx, starty, "CARGO " + String.valueOf(aship.GetCargoCount()), 0.75d);
		starty=starty-30;
		sinfo.DrawScaleText(startx, starty, "MAX WGT " + String.valueOf(aship.MaxWeight()), 0.75d);
		starty=starty-30;
		if (aship.GetTotalWeight() > aship.MaxWeight())
			sinfo.ChangeFontColor(0.75f, 0.0f, 0.0f);  // too much weight
		sinfo.DrawScaleText(startx, starty, "WEIGHT " + String.valueOf(aship.GetTotalWeight()), 0.75d);

		
		sinfo.ChangeFontColor(0.0f, 0.75f, 0.0f);
		if (aship.GetMaxHealth() > aship.health) {
			sinfo.DrawScaleText((int)repairx, (int)repairy+30, "REPAIR", 1.5d);
			sinfo.DrawScaleText((int)repairx+10, (int)repairy,	"COST " + String.valueOf(aship.GetMaxHealth()-aship.health), 1.0d);
			repair_box.drawit();
		}
		if (can_scrap) {
			sinfo.ChangeFontColor(0.7f, 0.3f, 0.3f);
			sinfo.DrawScaleText((int) scrapx, (int) scrapy+30, "SCRAP", 1.5d);
			sinfo.DrawScaleText((int) scrapx, (int)scrapy, "VALUE "+ String.valueOf(port_ships[active_ship].GetScrapValue()), 1.0d);
			scrap_box.drawit();
		}
		
	}
	
	public void DrawSellBox() {
		RayText sbtext= new RayText();
		GL11.glLineWidth(1.0f);
		GL11.glBegin(GL11.GL_LINES);
		GL11.glColor3f(0.0f, 0.8f, 0.0f);
		sell_width=md.win_width/10;
		sellx = md.win_width-sell_width-20;
		selly = md.win_height/2+sell_width;
		
		
		GL11.glVertex3d(sellx, selly, 0.0d);
		GL11.glVertex3d(sellx+sell_width, selly, 0.0d);
		GL11.glVertex3d(sellx+sell_width, selly, 0.0d);
		GL11.glVertex3d(sellx+sell_width, selly - sell_width, 0.0d);
		GL11.glVertex3d(sellx+sell_width, selly - sell_width, 0.0d);	
		GL11.glVertex3d(sellx, selly - sell_width, 0.0d);	
		GL11.glVertex3d(sellx, selly - sell_width, 0.0d);			
		GL11.glVertex3d(sellx, selly, 0.0d);	
		GL11.glEnd();
		sbtext.DrawText((int)sellx, (int)selly, "SELL");
		/*
		sbtext.DrawText((int)sellx, (int)(selly- sell_width-24), "PRICES");
		for (int i=1; i < 4; i++) {
			price_pickup[i].xpos= sellx;
			price_pickup[i].ypos=selly- sell_width- 24 -i*30;
			price_pickup[i].Draw(2.0d);
			sbtext.DrawText((int)(sellx+20), (int) (selly- sell_width-34- i*30), String.valueOf(port_island.pd.prices[i]));
			
		}
		*/
	}
	
	
	
	
	public boolean CheckSellBox( int x, int y) {
		if ((x-sellx > 0) && (x-sellx < sell_width) && (y - selly <  0)  && (y-selly > (sell_width*-1))) {
			if (drag_state==DRAG_PICKUP) {
				if (pickup_paid_for) {
					md.AddMoney(port_ships[active_ship].team, port_island.pd.prices[drag_pickup.GetPickupType()]);
					
					// economics hack
					// I sold some so demand goes down
					port_island.pd.prices[drag_pickup.GetPickupType()]-=1;  // price goes down
				}
				drag_state= DRAG_NOTHING;
				drag_pickup=null;
			}
			if ((drag_state==DRAG_COMPONENT) && (drag_comp!=null)) {
				drag_state= DRAG_NOTHING;
				if (icomp_paid_for)
					md.money[port_ships[active_ship].team]+=(int) (drag_comp.cd.GetCost()/2);
				drag_comp=null;
				
				istate= SHOW_NOTHING;
				icomp = null;
				
			}
			return true;
		}
		
		return false;
	}
	
	public void CheckRepairBox(int x, int y) {
		
		int repair_amount = port_ships[active_ship].GetMaxHealth()- port_ships[active_ship].health;
		if ((x > repairx) && (y>repairy) && (x< repairx+repair_width) && (y < repairy+ repair_height) &&
				(md.money[md.PLAYER_TEAM]> repair_amount-1)) {
			md.money[md.PLAYER_TEAM]-=repair_amount;
			port_ships[active_ship].RepairHull();
			port_ships[active_ship].health=port_ships[active_ship].GetMaxHealth();
		}
		if ((can_scrap) && 
				(x > scrapx) && (y>scrapy) && (x< scrapx+scrap_width) && (y < scrapy+ scrap_height) ) {
			md.money[md.PLAYER_TEAM]+=port_ships[active_ship].GetScrapValue();
			RemoveActiveShip();
			if (ship_count > 1) {
				can_scrap = true;
			} else 
				can_scrap = false;
			
		}
	}
	
	public int ComponentX(int n) {
		return (int) (md.win_width/20*(n+1));
	}
	
	public int ComponentY(int n) {
		return md.win_height-md.win_width/8- n*md.win_width/15;
	}
	
	public void DrawComponentBox() {

		RayText rt=new RayText();
		
		rt.DrawScaleText((int)cboxx, (int)(cboxy+ rt.GetHeight(0.75d)/2), "COMPONENTS", 0.75d);
		comp_box.drawit();
		
		double x,y;

		for (int count=0; count< port_island.pd.component_count; count++) {
			x = ComponentX(count);//md.win_width/20*(count+1);
			y= ComponentY(0); // md.win_height-md.win_width/10;
			port_island.pd.components[count].drawrot(x, y, 0, 2.0d);  //csketch.drawrot(x, y, 0,2.0d);
			rt.DrawScaleText((int)(x-5), (int) (y- 30), String.valueOf(port_island.pd.components[count].GetCost()), 0.75d);
		}
		
		for (int i = 0; i < md.team_component_count[md.PLAYER_TEAM]; i++) {
			x = ComponentX(i);//md.win_width/20*(count+1);
			y= ComponentY(1); // md.win_height-md.win_width/10;
			md.team_components[i].drawrot(x, y, 0, 2.0d);  // csketch.drawrot(x, y, 0,2.0d);
			rt.DrawScaleText((int)(x-5), (int) (y- 30), String.valueOf(md.team_components[i].GetCost()), 0.75d);
			
		}
		
		for (int i=1; i < 4; i++) {
			x = ComponentX(i-1);//md.win_width/20*i;  // this is not zero based
			y = ComponentY(2);//md.win_height-md.win_width/10 - md.win_width/20;
			price_pickup[i].xpos = x;
			price_pickup[i].ypos = y;
			price_pickup[i].Draw(2.0d);

			rt.DrawScaleText((int)(x-5), (int) (y- 30), String.valueOf(port_island.pd.prices[i]), 0.75d);
		}
		
				
	}
	
	public boolean CheckComponentBox(int x, int y) {
		int half_width=md.win_width/80;  // half the width of a component

		if ((x < cboxx) || (y >  cboxy) || (x> cboxx + cbox_width) || (y < cboxy - cbox_width))
			return false;
		
		if (drag_state!=DRAG_NOTHING)
			return true;
		

		

		for (int i = 0; i < port_island.pd.component_count; i++) {
			if (port_island.pd.components[i].csketch.InBounds(x, y)) {
				drag_state = DRAG_COMPONENT;
				drag_comp =new component();
			//	int i = (int) (Math.max(0, (x-half_width)-md.win_width/40)/(md.win_width/20));
				drag_comp.cd=port_island.pd.components[i];
				icomp_paid_for=false;		
				
				istate= SHOW_COMPONENT;
				icomp = drag_comp;
				
			}
				
		}
		
		for (int i = 0; i < md.team_component_count[md.PLAYER_TEAM]; i++) {
			double cx = ComponentX(i);
			double cy = ComponentY(1);
			md.team_components[i].cdsketch.SetWorldPoints(cx, cy, 0d);
			if (md.team_components[i].cdsketch.InBounds(x, y)) {
				drag_state = DRAG_COMPONENT;
				drag_comp = new component();
				drag_comp.cd = md.team_components[i];
				icomp_paid_for = false;		
				
				istate= SHOW_COMPONENT;
				icomp = drag_comp;
			}
		}
		
		for (int i=1; i < 4; i++) {
			// since this has been drawn I need to rest the position
			int px = ComponentX(i-1);//md.win_width/20*i;  // this is not zero based
			int py = ComponentY(2);//md.win_height-md.win_width/10 - md.win_width/20;
			price_pickup[i].psketch.SetWorldPoints(px, py, 0, 2.0d);
			if (price_pickup[i].psketch.InBounds(x, y)) {
				   drag_state = DRAG_PICKUP;
				   drag_pickup= new pickup();
				   drag_pickup.SetPickupType(i);
				   pickup_paid_for = false;
			}
		}
		
		return true;
		/*
		   if ((y< cboxy) && (y > cboxy-md.win_width/20)) {
				drag_state = DRAG_COMPONENT;
				drag_comp =new component();
				int i = (int) (Math.max(0, (x-half_width)-md.win_width/40)/(md.win_width/20));
				drag_comp.cd=port_island.pd.components[i];
				icomp_paid_for=false;
		   }
		   
		   if ((y < cboxy-md.win_width/20) && (y > cboxy - md.win_width/10)) {
			   int i = (int) (Math.max(0, (x-half_width)-md.win_width/40)/(md.win_width/20));
			   if (i < 3) {
				   drag_state = DRAG_PICKUP;
				   drag_pickup= new pickup();
				   drag_pickup.SetPickupType(i+1);
				   pickup_paid_for = false;
			   }
		   }
		*/
	}
	
	
	public void DrawComponentInspector() {
		// near top center box
		// if full draw component large scale
		RayText itext=new RayText();
		iboxx=md.win_width/2+md.win_width/10;
		iboxy=md.win_height-md.win_width/10;

		ibox_width=md.win_height/8;
		GL11.glLineWidth(1.0f);
		GL11.glBegin(GL11.GL_LINES);
		GL11.glColor3f(0.8f, 0.4f, 0.0f);	
		GL11.glVertex3d(iboxx, iboxy, 0.0d);
		GL11.glVertex3d(iboxx+ibox_width, iboxy, 0.0d);
		GL11.glVertex3d(iboxx+ibox_width, iboxy, 0.0d);
		GL11.glVertex3d(iboxx+ibox_width, iboxy - ibox_width, 0.0d);
		GL11.glVertex3d(iboxx+ibox_width, iboxy - ibox_width, 0.0d);	
		GL11.glVertex3d(iboxx, iboxy - ibox_width, 0.0d);	
		GL11.glVertex3d(iboxx, iboxy - ibox_width, 0.0d);			
		GL11.glVertex3d(iboxx, iboxy, 0.0d);	
		GL11.glEnd();	
		
		itext.DrawText((int)iboxx, (int)iboxy, "INSPECT");
		
		
		if (istate== SHOW_COMPONENT) {
			if (icomp!=null) {
				// icomp.DrawIt();
				icomp.draw_inactive(iboxx+ibox_width/2, iboxy-ibox_width/2, 0, 2.0d); //drawrot(iboxx+ibox_width/2, iboxy-ibox_width/2, 0, 2.0d);
				int x=(int)(iboxx+ibox_width+ 8);  // 8 is a buffer hack
				int y=(int)(iboxy);
				switch (icomp.cd.type) {
					case GUN: 
						itext.DrawScaleText(x, y, "GUN TURRET", 0.75d);
						y=y-30;
						itext.DrawScaleText(x, y, String.valueOf(icomp.cd.guncount)+ " GUN", 0.75d);
						y=y-30;
						itext.DrawScaleText(x, y, "SHOT SPEED "+ String.valueOf((int)(icomp.cd.GetProjectileSpeed())), 0.75d);
						y=y-30;
						itext.DrawScaleText(x, y, "SHOT DELAY "+String.valueOf((int)(icomp.cd.GetRefreshTime())), 0.75d);
						y=y-30;
						itext.DrawScaleText(x, y, "AMMO CLIP " + String.valueOf(icomp.cd.clip_size), 0.75d);
						y=y-30;
						itext.DrawScaleText(x, y, "WEIGHT "+ String.valueOf(icomp.cd.GetWeight()), 0.75d);
						y=y-30;
						if (icomp.cd.GetCost()> md.money[md.PLAYER_TEAM]) {// set color red}
							
						}
						itext.DrawScaleText(x, y, "COST " + String.valueOf(icomp.cd.GetCost()), 0.75d);	
						break;
					case FIXEDGUN:
						itext.DrawScaleText(x, y, "FIXED GUN", 0.75d);
						y=y-30;
						itext.DrawScaleText(x, y, String.valueOf(icomp.cd.guncount)+ " GUN", 0.75d);
						y=y-30;
						itext.DrawScaleText(x, y, "SHOT SPEED "+ String.valueOf((int)(icomp.cd.GetProjectileSpeed())), 0.75d);
						y=y-30;
						itext.DrawScaleText(x, y, "SHOT DELAY "+String.valueOf((int)(icomp.cd.GetRefreshTime())), 0.75d);
						y=y-30;
						itext.DrawScaleText(x, y, "AMMO CLIP " + String.valueOf(icomp.cd.clip_size), 0.75d);
						y=y-30;
						itext.DrawScaleText(x, y, "WEIGHT "+ String.valueOf(icomp.cd.GetWeight()), 0.75d);
						y=y-30;
						itext.DrawScaleText(x, y, "COST " + String.valueOf(icomp.cd.GetCost()), 0.75d);	
						
						break;
					case TORPEDO:
						itext.DrawScaleText(x, y, "TORPEDO TUBE", 0.75d);
						y=y-30;
						itext.DrawScaleText(x, y, "SHOT SPEED " + String.valueOf((int) (icomp.cd.GetProjectileSpeed())), 0.75d);
						y=y-30;
						itext.DrawScaleText(x, y, "WEIGHT "+ String.valueOf(icomp.cd.GetWeight()), 0.75d);
						y=y-30;
						itext.DrawScaleText(x, y, "COST " + String.valueOf(icomp.cd.GetCost()), 0.75d);
	
						break;
					case MINE:	
						itext.DrawScaleText(x, y, "MINE DROPPER", 0.75d);
						y=y-30;
						itext.DrawScaleText(x, y, "WEIGHT "+ String.valueOf(icomp.cd.GetWeight()), 0.75d);
						y=y-30;
						itext.DrawScaleText(x, y, "COST " + String.valueOf(icomp.cd.GetCost()), 0.75d);
						break;
					case CARGO:
						itext.DrawScaleText(x, y, "CARGO HOLDER", 0.75d);
						y=y-30;
						itext.DrawScaleText(x, y, "WEIGHT "+ String.valueOf(icomp.cd.GetWeight()), 0.75d);
						y=y-30;
						itext.DrawScaleText(x, y, "COST " + String.valueOf(icomp.cd.GetCost()), 0.75d);
					break;
					case ENGINE:
						itext.DrawScaleText(x, y, "ENGINE", 0.75d);
						y-=30;
						itext.DrawScaleText(x, y, "PLUS 20 PCT SPEED", 0.75d);
						y-=30;
						itext.DrawScaleText(x, y, "PLUS 25 PCT MAX WEIGHT", 0.75d);
						y=y-30;
						itext.DrawScaleText(x, y, "WEIGHT "+ String.valueOf(icomp.cd.GetWeight()), 0.75d);

					break;	
					case REPAIR:
						itext.DrawScaleText(x, y, "DAMAGE CONTROL", 0.75d);
						y-=30;
						itext.DrawScaleText(x, y, "PLUS "+ String.valueOf(icomp.cd.guncount*10) + " HEALTH", 0.75d);
						y-=30; 
						itext.DrawScaleText(x, y,"REPAIR RATE "+ String.valueOf(icomp.cd.fire_rate), 0.75d);
						y-=30;
						itext.DrawScaleText(x, y, "WEIGHT "+ String.valueOf(icomp.cd.GetWeight()), 0.75d);
						
				}
			}
		}
		if (istate==SHOW_PICKUP) {
			ipickup.xpos=iboxx+ibox_width/2;
			ipickup.ypos=iboxy-ibox_width/2;
			ipickup.rotation=0;
			ipickup.Draw(2.0d);
			switch (ipickup.type) {
			case REPAIR:
				break;
			case TECH:
				break;
			case ORE:
				break;
			case BANNAS:
				break;
			
			}
			
		}
	}
	public boolean CheckComponentInspector(int x, int y) {
		
		// if in bounds
		if ((x<iboxx) || (x>iboxx+ibox_width) || (y>iboxy) || (y<iboxy-ibox_width))
			return false;
 		// if it is empty and you click while holding something
		// put it in the box
		

		switch (drag_state) {
		case DRAG_NOTHING:
			if (icomp!=null) {
				drag_comp= new component();
				drag_comp.CopyFrom(icomp);
				icomp=null;
				drag_state=DRAG_COMPONENT;
				istate=SHOW_NOTHING;
			}
			break;
		case DRAG_COMPONENT:
			if (icomp==null) {
				istate=SHOW_COMPONENT;
				icomp=new component();
				icomp.CopyFrom(drag_comp);
				icomp.xpos=iboxx+ibox_width/2;
				icomp.ypos=iboxy+ibox_width/2;
				drag_comp=null;
				drag_state=DRAG_NOTHING;
			}
			break;
		case DRAG_PICKUP:
			// this doesn't work, so don't inspect pickups for now
			/*
			if (drag_pickup!=null) {
				ipickup=drag_pickup;
				ipickup_paid_for= pickup_paid_for;
				istate= SHOW_PICKUP;
			}
			*/
			break;
		}
		
		return true;
		// if it is full and your drag is empty and you click on it
		// your drag is what is in the box
	}
	
	
	public void DrawShips() {

		for (int count=0; count < ship_count; count++) {
			if (count==active_ship) {
				port_ships[count].DrawScaled(2.0d);
				DrawActiveShipInfo();  // only do it if there is an active ship
				
				// now draw it once somewhere else
				double oldx=port_ships[count].xpos;
				double oldy=port_ships[count].ypos;
				PositionShip(port_ships[count], count);
				port_ships[count].DrawScaled(0.5d);

				// draw a connecting line
				float slopex=(float) (oldx-port_ships[count].xpos)/6;
				float slopey=(float) (oldy-port_ships[count].ypos)/6;
				
				GL11.glBegin(GL11.GL_LINES);
				GL11.glColor3f(1.0f, 1.0f, 1.0f);
				GL11.glVertex3f((float) port_ships[count].xpos+slopex, (float) port_ships[count].ypos+slopey, 0f);
				GL11.glVertex3f((float)(oldx-slopex), (float)(oldy-slopey), 0f);
				GL11.glEnd();

				
				port_ships[count].xpos=oldx;
				port_ships[count].ypos=oldy;

				
			} else
			port_ships[count].DrawScaled(0.5d);
		}	
	}
	

	public ship CustomizationScreen(ship rship) {	

		
		DrawShips();
		
		DrawComponentBox();
		/*
		for (int count=0; count< port_island.pd.hull_count; count++) {
			port_island.pd.hulls[count].ShowHull(md.win_width-md.win_width/10, md.win_height/2+count*200d, 0, 1.0d);
		}*/
		
		port_text.DrawText((int)(md.win_width-md.win_width/10d), (int)(md.win_height/15d), "EXIT PORT->");
		
		int money_length=Math.max(port_text.GetLength("MONEY "+ String.valueOf(md.money[md.PLAYER_TEAM]), 1.0d), md.win_width/10);
		port_text.DrawText((int) (md.win_width-money_length), (int) (md.win_height- md.win_height/10), "MONEY "+ String.valueOf(md.money[md.PLAYER_TEAM]));
	
		int x = Mouse.getX();
		int y = Mouse.getY();
		DrawDragObj(x,y);
		
		DrawSellBox();
		DrawComponentInspector();
		
		
		boolean clicked_something = false;
		if ((Mouse.isButtonDown(0)) && (click_delay<0.1d)) {
			click_delay=500d/md.time_factor;
	
			int old_active=active_ship;			
			active_ship=ActiveShipInterface(old_active, x, y);	
			if (ComponentSelect(x,y) !=	port_ships[active_ship].component_count)			
				clicked_something = true;
			if (CheckSellBox(x, y))
				clicked_something = true;
			
			if (CheckComponentBox(x,y))
				clicked_something=true;
			if (CheckComponentInspector(x, y))
				clicked_something = true;
			
			if (!clicked_something) {
				if ((drag_comp!=null) && (!icomp_paid_for)) {
					drag_state = DRAG_NOTHING;
					drag_comp = null;
					istate= SHOW_NOTHING;
					icomp = null;
				}
					
				if ((drag_pickup!=null) && (!pickup_paid_for))	{
					drag_state = DRAG_NOTHING;
					drag_pickup = null;
				}
			}

			
			port_ships[old_active].xpos=(md.win_width/10)*(old_active+3);
			port_ships[old_active].ypos=(md.win_height/5);
				
			PositionShip(port_ships[old_active], old_active);
			port_ships[active_ship].xpos=md.win_width/2;
			port_ships[active_ship].ypos=md.win_height/2;		
			CheckRepairBox(x, y);
			
			if ((x> md.win_width-  md.win_width/5) && (y < md.win_height/5)) {
				//LeavePort(active_ship);
				
				port_ships[active_ship].SetTeam(md.PLAYER_TEAM, md);  // make sure it is our ship
				rship=port_ships[active_ship];
				
				/*
				 *  Now fill up the fleet
				 */
				md.userfleet.Empty();
				for (int i=0; i < ship_count; i++) {
					if (port_ships[i]!=rship) {
						md.userfleet.AddShip(port_ships[i]);
						port_ships[i]=null;
					}
				}
				ship_count=0;
			}
		}
		return rship;
	}
	
	public void RemoveActiveShip() {
		// set active ship to null
		// go through the list of ships -1
		// if a ship is null, move the proceeding one up the list
		// ship_count-1
		port_ships[active_ship]=null;
		for (int i = 0; i < ship_count-1; i++) {
			if (port_ships[i]==null) {
				port_ships[i]=port_ships[i+1];
				// you have to move the ship
				PositionShip(port_ships[i],i);
				port_ships[i+1]=null;
			}
		}
		ship_count-=1;
		active_ship=0;
		port_ships[active_ship].xpos=md.win_width/2;
		port_ships[active_ship].ypos=md.win_height/2;	
	}
	
	public int CheckHullClick(int x, int y) {
		int ret_hull= hull_count+1;
		// is y the right band\
		if ((y< md.win_height*2/3+75) && (y>md.win_height*2/3-75)) {
			for (int i=0; i < hull_count; i++) {
				if ((x > (i+1)*md.win_width/5-50) && (x < (i+1)*md.win_width/5 + 50)) 
					return i;
			}
		}
		return hull_count;
	}
	
	public void HullSummaryText(HullData hd, int x, int y, double small_font_scale) {
		// show a summary of the hull for sale and owned hulls.  
		port_text.DrawScaleText(x, y - 2*port_text.GetHeight(small_font_scale)-8, "MAX WEIGHT "+ String.valueOf((int) (hd.CalcMaxWeight())), small_font_scale);
		port_text.DrawScaleText(x, y - 1*port_text.GetHeight(small_font_scale)-4, "MAX SPEED "+ String.valueOf((int) (hd.CalcMaxSpeed())), small_font_scale);
		port_text.DrawScaleText(x, y, "MAX HEALTH "+ String.valueOf(hd.CalcMaxHitpoints()), small_font_scale);
	
	}
	public int CheapestShip() {
		int cheapest = 999999;
		for (int i=0;i < hull_count; i++) {
			if (hulls[i].CalcCost() < cheapest)
				cheapest = hulls[i].CalcCost();
		}
		return cheapest;
	}
	public void ShowHullsForSale() {
		double small_font_scale=0.5d;
		double medium_font_scale = 0.75d;
		port_text.DrawText(md.win_width/4, md.win_height*3/4, "HULLS FOR SALE     CLICK TO BUY ");
		for (int i=0; i < hull_count; i++) {
			//hulls[i].hull.drawat((int)(i+1)*md.win_width/5, md.win_height*2/3);
			hulls[i].ShowHull((int)(i+1)*md.win_width/5, md.win_height*2/3, 0, 1.0d);
			// show max weight
			
			HullSummaryText(hulls[i], (int) ((i+1)*md.win_width/5 + hulls[i].width), (int) (md.win_height*2/3), small_font_scale);

			// show cost			
			port_text.DrawScaleText((int)(i+1)*md.win_width/5, (int) (md.win_height*2/3-100 /*hulls[i].full_length*/), "COST " + String.valueOf(hulls[i].CalcCost()), medium_font_scale);

			
		}
		port_text.DrawText(md.win_width/4, md.win_height/3, "SHIPS IN PORT");
		ship s=localsm.GetShip(playerid);
		/*
		if (s!=null) {
			s.xpos=md.win_width/5;
			s.ypos=md.win_height/4;
			s.rotation=0;
			s.DrawIt();
		}
		*/
		for (int count=0; count < ship_count;count++ ) {
			
			
				port_ships[count].xpos = (int) ((count+1)*md.win_width/5 + port_ships[count].hd.width);//md.win_width/5+(md.win_width/10)*(count+1);
				port_ships[count].ypos = md.win_height/4;
				port_ships[count].rotation=0;
				port_ships[count].DrawIt();
				HullSummaryText(port_ships[count].hd, (int) (port_ships[count].xpos + port_ships[count].hd.width), (int) (port_ships[count].ypos), small_font_scale);
				

		}	
		port_text.DrawText(md.win_width*5/6, md.win_height/3, "CONTINUE->");

	}
	
	public void HullBuyDisplay() {

		
		int money_length=Math.max(port_text.GetLength("MONEY "+ String.valueOf(md.money[md.PLAYER_TEAM]), 1.0d), md.win_width/10);
		port_text.DrawText((int) (md.win_width-money_length), (int) (md.win_height- md.win_height/10), "MONEY "+ String.valueOf(md.money[md.PLAYER_TEAM]));

		ShowHullsForSale();
		
		if (Mouse.isButtonDown(0)) {
			
			// if I click on a hull, add it
			
			int x = Mouse.getX();
			int y = Mouse.getY();
			
			int trybuy=CheckHullClick(x, y);
			
			if (trybuy < hull_count) {
				if (hulls[trybuy].CalcCost() < md.money[md.PLAYER_TEAM]+1)
				{
			
					md.money[md.PLAYER_TEAM]-=hulls[trybuy].cost;
					ship nship=new ship(md.GetUniqieID(md.SHIP));
					HullData hdf = new HullData();
					hulls[trybuy].CopyTo(hdf);
					nship.BuildShip(hdf);
					port_ships[ship_count] = nship;
					

					ship_count++;
					SetupModDisplay();
				}
			}
			if (x > md.win_width*4/5d) {
				SetupModDisplay();
			}


		}
	}
	
	public ship ShowDisplay(double time) {

		//mode = SHIP_CUSTOMIZER;
			
		
		ship rship=null;
		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glLoadIdentity();
		GL11.glOrtho(0, md.win_width, 0, md.win_height, 1, -1);
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		
		port_text.DrawScaleText(md.win_width/20, md.win_height-md.win_height/15, "PORT OF " + port_island.name, 2.0d);
		
		if (mode == HULL_BUY_MODE) {
			
			// do the screen where you can buy hulls.  
			HullBuyDisplay();
		} else
		rship = CustomizationScreen(rship);

		
		
		click_delay=click_delay-time;
		Display.update();
		
		return rship;
	}
	
	PortDisplay(UserInput uin, MissionData mdin) {
		ui=uin;
		md=mdin;
	
		port_text = new RayText();
		port_ships = new ship[10];
		price_pickup= new pickup[4];
	}
}
