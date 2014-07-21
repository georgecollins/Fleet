
import java.util.ArrayList;
import java.util.List;
import org.lwjgl.opengl.GL11;

public class AIControl {
/* 
 * This is an object that can get added to things
 * like ships or projectiles.  
 * I do not plan to have an AIControl manager.  
 * 
 * But the ship manager (and maybe the Projectile Manager) will update
 * the Ai.  
 */
	ShipManager sm;
	IslandManager im;
	MissionData md;
	sketch ghost;
	

	double turnaim;
	
	int skill=0;
	
	WorldThing follow_offset;
	WorldThing goal;
	
	enum aiType { SHIPAI, ISLANDAI, CAPTAINAI, MISSILEAI
	}
	
	enum strategy { FREIGHTER, GUNBOAT, TORPEDO, BARGE, FLEET }
	// FLEET Strategy is just to test a new AI with Tasks packed up and down the stack 
	
	aiType type;
	strategy mystrat;
	
	
	class taskitem {
		int priority;
		int task;
	//	int mode;
		int targetid;
		int controlid;
		WorldThing target;
		ship me;
		WorldThing endobj;  // in case I end if I get too far from the end thi
		int endcase;  // what makes me stop
		double time_out;  // for tasks that time out
		Path mypath;
		int onpoint;
		int path_dir;  // how do I do the path, circular, reverse, one imt
		int current_dir;
		
	
		WorldThing follow_offset;
		List	<WorldThing>trail_points;
		double trail_timer;
		
		void CopyTo(taskitem ti) {
			ti.priority=priority;
			ti.task=task;
//			ti.mode=mode;
			ti.targetid=targetid;
			ti.controlid = controlid;
			ti.target=target;
			ti.me=me;
			ti.endobj=endobj;
			ti.endcase=endcase;
			ti.time_out = time_out;
			ti.mypath=mypath;
			ti.onpoint=onpoint;
			ti.path_dir = path_dir;  // how do I do the path, circular, reverse, one imt
			ti.current_dir = current_dir;

			if (follow_offset!=null) {
				ti.follow_offset=new WorldThing();
				follow_offset.CopyTo(ti.follow_offset);
			}
			ti.trail_points= new ArrayList<WorldThing>();

			for (int i=0; i < trail_points.size(); i++)
				ti.trail_points.add(i, trail_points.get(i));
			
			ti.trail_timer= trail_timer;
			
		}
		
		taskitem() {
			trail_points= new ArrayList<WorldThing>();
		}
	}
	
	List <taskitem>tasklist;

	int controlid;  // the thing I am controlling
	int mode;
//	int task;
//	int targetid;
	double timer;
	
	double attack_angle;  // what is my best attack angle, sideways, backwards, etc.  
	
	static final int STOP_MODE = 0;
	static final int CRUISE_LEFT_MODE = 1 ;
	static final int CRUISE_RIGHT_MODE =2 ;
	static final int COLLIDE_MODE = 3;
	static final int AVOID_RIGHT_MODE = 4;  // hard right to avoid a crash
	static final int CRUISE_STRAIGHT_MODE = 5;
	static final int BACK_UP_MODE = 6;
	static final int BACK_UP_LEFT_MODE = 7;
	static final int SPIN_RIGHT_MODE =8;
	static final int SPIN_LEFT_MODE = 9;
	static final int TARGET_TURN_MODE =10;  // turn toward a target
	
	static final int TASK_WAIT=0;
	static final int TASK_PATH = 1;
	static final int TASK_FLEE = 2;
	static final int TASK_ATTACK = 3;
	static final int TASK_PROTECT= 4;
	static final int TASK_EXPLORE = 5;
	static final int TASK_FOLLOW = 6;
	static final int TASK_FLEET_FOLLOW = 7;  // test new follow by trail of points, add task list for fighting
	static final int TASK_CRATE_GRAB = 8;
	static final int TASK_MINE_LAY = 9;
	
	
	
	// ways a task can do a path
	static final int PATH_ONE_TIME = 1;  // end after one pay
	static final int PATH_CIRCULAR = 2;
	static final int PATH_REVERSE = 3;
	
	final int DIR_FORWARD = 1;
	final int DIR_BACKWARD = 2;
	
	RayText aidebug;
	
	public void ShowAI(ship s ) {
		if (md.show_ai==0)
			return;
		
		// if the ships AI has a path, show the path
		if (tasklist.size()==0)
			return;
		taskitem ti=tasklist.get(0);
		
		double txt_size =0.5d;
		WorldThing info = new WorldThing();
		info.xpos = 40;
		info.ypos = 40;
		int x = (int) (s.xpos + info.rotX(s.rotation));
		int y = (int) (s.ypos + info.rotY(s.rotation));
		switch (ti.task) {
		case TASK_WAIT:
			aidebug.DrawScaleText(x, y, "WAIT", txt_size);
			break;
		case TASK_PATH:
			aidebug.DrawScaleText(x, y, "PATH", txt_size);
			break;
		case TASK_FLEE:
			aidebug.DrawScaleText(x, y, "FLEE", txt_size);
			break;
		case TASK_ATTACK:
			aidebug.DrawScaleText(x, y, "ATTACK", txt_size);
			break;
		case TASK_PROTECT:
			aidebug.DrawScaleText(x, y, "PROTECT", txt_size);			
			break;
		case TASK_EXPLORE:
			aidebug.DrawScaleText(x, y, "EXPLORE", txt_size);
			break;
		case TASK_FOLLOW:
			aidebug.DrawScaleText(x, y, "FOLLOW", txt_size);
			break;
		case TASK_FLEET_FOLLOW:
			aidebug.DrawScaleText(x, y, "FLEET F", txt_size);
			break;
		case TASK_CRATE_GRAB:
			aidebug.DrawScaleText(x, y, "CRATE", txt_size);
			break;
/*
		case TASK_PATH_FOLLOW:
			aidebug.DrawScaleText(x, y, "PATH FOLLOW", txt_size);
			break;
			*/
		}
		
		info.ypos-=aidebug.GetHeight(txt_size)+4;
		aidebug.ChangeFontColor(0.0f, 1.0f, 0.0f);
		
		x = (int) (s.xpos + info.rotX(s.rotation));
		y = (int) (s.ypos + info.rotY(s.rotation));

		switch (mode) {
		case STOP_MODE:
			aidebug.DrawScaleText(x, y, "STOP", txt_size);	
			break;
		case CRUISE_LEFT_MODE:
			aidebug.DrawScaleText(x, y, "CRUISE LEFT", txt_size);
			break;
		case CRUISE_RIGHT_MODE:
			aidebug.DrawScaleText(x, y, "CRUISE RIGHT", txt_size);
			break;
		case COLLIDE_MODE:
			aidebug.DrawScaleText(x, y, "COLLIDE", txt_size);
			break;
		case AVOID_RIGHT_MODE:  // hard right to avoid a crash
			aidebug.DrawScaleText(x, y, "AVOID RIGHT", txt_size);
			break;
		case CRUISE_STRAIGHT_MODE:
			aidebug.DrawScaleText(x, y, "CRUISE STRAIGHT", txt_size);
			break;
		case BACK_UP_MODE:
			aidebug.DrawScaleText(x, y, "BACK UP", txt_size);
			break;		
		case BACK_UP_LEFT_MODE:
			aidebug.DrawScaleText(x, y, "BACK LEFT", txt_size);
			break;		
		case SPIN_RIGHT_MODE:
			aidebug.DrawScaleText(x, y, "SPIN RIGHT", txt_size);
			break;
		case SPIN_LEFT_MODE:
			aidebug.DrawScaleText(x, y, "SPIN LEFT", txt_size);
			break;
		case TARGET_TURN_MODE: // turn toward a target
			aidebug.DrawScaleText(x, y, "TARGET TURN", txt_size);
			break;
		}
		aidebug.ChangeFontColor(1.0f, 1.0f, 1.0f);

		
		if ((ti.mypath!=null) && (ti.mypath.numpoints> 0)) {
				GL11.glLineWidth(1.0f);
				GL11.glBegin(GL11.GL_LINES);
				GL11.glColor3f(0.0f, 0.8f, 0.0f);
				for (int i=ti.onpoint; i < ti.mypath.numpoints-1; i++) {
					GL11.glVertex3d(ti.mypath.xmat[i], ti.mypath.ymat[i], 0.0d);
					GL11.glVertex3d(ti.mypath.xmat[i+1], ti.mypath.ymat[i+1], 0.0d);
				}
				GL11.glEnd();
			}
	
	
	}
	
	

	
	public void SetSkill(int ns) {
		skill=ns;
	}
	
	
	public void AddTask(taskitem ti) {
		taskitem nt=new taskitem();
		ti.CopyTo(nt);
		tasklist.add(nt);
		SortTasks();
	}
	
	public void DeleteTask(taskitem ti) {
		tasklist.remove(ti);
		SortTasks();
	}
	// like, remove all wait tasks
	// this is slower than delete task (I think)
	public void RemoveTaskType(int task_type ) {
		for (int count = 0; count < tasklist.size()-1; count++)
			if (tasklist.get(count).task == task_type)
				tasklist.remove(count);
	
	}
	
	public void SortTasks() {
		boolean changed= true;
		taskitem t1;
		taskitem t2;
		while (changed == true) {
			changed=false;


			for (int count = 0; count < tasklist.size()-1; count++)
			{
				t1=tasklist.get(count);
				t2=tasklist.get(count+1);
	
				if (t1.priority > t2.priority) {
					changed=true;
					tasklist.set(count, t2);  // swap them
					tasklist.set(count+1, t1);
				}


			}
		}
	}
	
	public int GetCurrentTask() {
		if (tasklist.size()==0)
			return 0;
		taskitem ti=tasklist.get(0);
		return ti.task;
	}
	
	public void AdjustThrottle(ship me, double goal_speed) {
		if (me.speed < goal_speed)
			me.throttle = 1.0d;
		if (me.speed > goal_speed)
			me.throttle = -1.0d;
	}

	public void NotifyDamage(projectile phit) {
		ship s=sm.GetShip(controlid);
		if (s==null) 
			return;
			
		if (phit.pd.team==s.team)
			return;  // friendly fire
		
		// md.MakeEnemy(s.team, phit.pd.team);
		ship user = md.FindShip(md.userid);
		if ((user!=null ) && (user.DistanceSq(s)< md.win_width*md.win_height ) && 
			(phit.pd.owner== md.userid))
			md.StartWarTimer(s.team);
		// if the ships AI has a path, show the path
		if (tasklist.size()==0)
			return;
		taskitem ti=tasklist.get(0);
		
		if (((mystrat==strategy.GUNBOAT ) || (mystrat==strategy.TORPEDO)) && (ti.targetid==0)
				&& (md.relations[s.team][phit.pd.team]== md.ENEMIES)){
			StartAttackTask(s, phit.pd.owner);
//			mypath=im.GetPath(s.xpos, s.ypos,
		}
		
		
		// this should be start flee
		if ((mystrat==strategy.FREIGHTER) && (ti.task!=TASK_FLEE)) {
			ship  tship = sm.GetShip(phit.pd.owner);  // is the shooter still alive?
			if (tship!=null) {       // if it's alive RUN
				md.SendSOS(s.xpos, s.ypos, s.team);
			

				double bestdist=0;// I want the best place to run to be a further distance
				double gx, gy;
				for (int i= -1; i < 2; i++)
					for (int j=-1; j < 2; j++)
					{
						gx=s.xpos+ i *500;  // should be world width
						gy=s.ypos+ j*500;
						double dist = (tship.xpos-gx)*(tship.xpos-gx)+(tship.ypos-gy)*(tship.ypos-gy);
						if ((InBounds(gx, gy)) && (dist> bestdist)) {
							goal.xpos=gx;
							goal.ypos=gy;
							bestdist=dist;
						}				
					}
				
				double startx = s.xpos+Math.cos(s.rotation)*s.speed*5d;
				double starty = s.ypos+Math.sin(s.rotation)*s.speed*5d;

				Path p = im.GetPath(startx, starty, goal.xpos, goal.ypos);
				
				StartFleeTask(s, p, 5);  // 5= a high priority
				

			}
			
			
		}
	}
	
	
	int ShipCollisionPrediction(ship s, float t) {
		/*
		 *  Predict if I am about to collide with something in t seconds
		 */
		
		double xvel= s.xvel;
		double yvel= s.yvel;
		// if very slow, assume we will accelerate
		if (xvel*yvel < 3.0d) {
			xvel = Math.cos(s.rotation+Math.PI/2)*10d;
			yvel = Math.sin(s.rotation+Math.PI/2)*10d;
		}
		double x= s.xpos + xvel*t;
		double y= s.ypos + yvel*t;
		

		// System.out.println("xvel "+ s.xvel + " yvel " + s.yvel);
			
		ghost.SetWorldPoints(x, y, s.rotation);
		if (md.show_ai!=0) 
			ghost.drawrot(x,y, s.rotation);

		//System.out.println("Collision prediction  x " + s.xpos + " y " + s.ypos +  " = "+ sm.TestCollision(ghost));
		int bump = sm.TestCollision(ghost);  // returns the id of the colliding ship
		if (bump==0) {
			x = s.xpos + s.xvel*t/2;
			y = s.ypos + s.yvel*t/2;
			ghost.SetWorldPoints(x, y, s.rotation);
			if (md.show_ai!=0) 
				ghost.drawrot(x,y, s.rotation);
			bump = sm.TestCollision(ghost);
		}
		return bump;
	}
	
	int IslandCollisionPrediction(ship s, float t) {
		double x= s.xpos + s.xvel*t;
		double y= s.ypos + s.yvel*t;
		
		
		ghost.SetWorldPoints(x, y, s.rotation);
		//System.out.println("Islands Collision prediction  x " + s.xpos + " y " + s.ypos +  " = "+ im.TestCollision(ghost));		
		int bump= im.TestCollision(ghost);
		// one last test
		if (bump==0) {
			if (im.MapValue(x, y) >0) 
				bump=1;
		}
		
		if (bump==0) {
			x = s.xpos + s.xvel*t/2;
			y = s.ypos + s.yvel*t/2;
			ghost.SetWorldPoints(x, y, s.rotation);
			bump = im.TestCollision(ghost);
		}
		
		
		for (int count=0; count< 5; count++) {
			x = s.xpos + s.xvel*t*count;
			y = s.ypos + s.yvel*t*count;
			ghost.SetWorldPoints(x, y, s.rotation);
			if (bump==0) {
				bump=im.TestCollision(ghost);
			}
		}

		return bump;
		
	}

	
	// used for making a path.
	public boolean InBounds(double x, double y) {
		if (((x-sm.offsetx) >  md.world_width/2) || 
			 ((x-sm.offsetx) < -md.world_width/2) ||
			 ((y-sm.offsety) > md.world_height/2) ||
			 ((y-sm.offsety) < -md.world_height/2))	{
			return false;
		} 
		return true;
	}

	int CheckForObstacles(ship s, double dist) {
		// check if there is an obstacle ahead, to the left and to the right
		double x=s.xpos+Math.cos(s.rotation)*dist;
		double y=s.ypos+Math.sin(s.rotation)*dist;
		
		if (im.MapValue(x, y) > 0) {

			return 1;
		}
		return 0;
	}
	
	void GetStrategy(ship s) {
		// how should I fight
		
		// put something on the task list
		taskitem ti = new taskitem();
		ti=new taskitem();
		ti.controlid=s.id;
		ti.task=TASK_EXPLORE;
		ti.priority=110;
		AddTask(ti);	
		

		if (s.hd.hull_type== HullData.BARGE_HULL) {
			skill= 1;
			mystrat= strategy.BARGE;
			return;
		}
		
		int guns_found=0;  // how many guns
		int cargo_found=0;
		// go through the components of s
		// if no guns or torpedos I am a freighter
		mystrat=strategy.TORPEDO;  // if I only have fixed guns I will do torpedo strate
		for (int i=0; i< s.component_count; i++) {
			if (s.scomponent[i].cd.type==ComponentData.ctype.GUN) {
				guns_found++;
				mystrat=strategy.GUNBOAT;
			}
			if (s.scomponent[i].cd.type==ComponentData.ctype.TORPEDO) {
				attack_angle=s.scomponent[i].min_arc;
				mystrat=strategy.TORPEDO;	
				return;
			}
			if (s.scomponent[i].cd.type==ComponentData.ctype.FIXEDGUN) {
				guns_found++;
				attack_angle=s.scomponent[i].min_arc;
			}
			if (s.scomponent[i].cd.type==ComponentData.ctype.CARGO) {
				cargo_found++;
			}
		}
		if ((guns_found==0) && (cargo_found> 0)) {
			mystrat=strategy.FREIGHTER;
		}
	
	
	}
	
	void UpdateMode(ship s, double t) {
		// debug test of rotation
		/*
		double new_angle2 = s.rotation+Math.PI/5d;  // essentially forward+ 1/4 PI
		ghost.drawrot(s.xpos+ Math.cos(new_angle2)*75, s.ypos+Math.sin(new_angle2)*75, s.rotation-Math.PI/5);
	*/
		if (timer<0.0f) {
			if (mode==BACK_UP_MODE)  {
				
				// check here to see if you go right or left
				if (md.show_ai!=0) {
					double new_angle2 = s.rotation+Math.PI/4d;  // essentially forward+ 1/4 PI
					ghost.drawrot(s.xpos+ Math.cos(new_angle2)*75, s.ypos+Math.sin(new_angle2)*75, s.rotation-Math.PI/4);
				}	
				double new_angle = s.rotation+Math.PI/4d;  // essentially forward+ 1/4 PI
				ghost.SetWorldPoints(s.xpos+ Math.cos(new_angle)*75d, s.ypos+Math.sin(new_angle)*75d, new_angle-Math.PI/4d);
				int sbump=sm.TestCollision(ghost);
				int ibump=im.TestCollision(ghost);
				// cheat a bit to make sure I catch collisions
				// don't rotate the ship and move it less, do I still collide
				if ((sbump==0) && (ibump==0)) {
					ghost.SetWorldPoints(s.xpos+ Math.cos(new_angle)*5d, s.ypos+Math.sin(new_angle)*5d, s.rotation);
				}
				
				if (((sbump!= 0) && (sbump!=s.id))
						|| ((ibump != 0) && (ibump!=s.id))) {
					mode=SPIN_LEFT_MODE;
					timer= 1500.0d/md.time_factor;
				} else {
					mode = SPIN_RIGHT_MODE;
					timer=500.0d/md.time_factor; // the timer can't be the same or it will deadlock
				} 
				
			}
			else {	

				mode=CRUISE_STRAIGHT_MODE;
				timer=1000.0d/md.time_factor;
			}
			
				
		}
	}
	
	void DoMode(ship s) {
		int will_bump=0;
		if (tasklist.size()==0)
			return;
		taskitem ti=tasklist.get(0);

		
		switch (mode) {
			case CRUISE_STRAIGHT_MODE:
				s.steer=0d;
				if (s.speed< s.GetMaxSpeed()/2)
					s.throttle = 0.5d;	
				
				will_bump=ShipCollisionPrediction(s, 10.0f);
				if (will_bump==s.id) {
					will_bump=0;
				}

				if (will_bump==0) {
					will_bump=IslandCollisionPrediction(s, 10.0f);

				}

				if (will_bump!=0) { 	
					mode=BACK_UP_MODE;
					timer=5;
					
				}
				if (s.collided!=0) {
					mode = BACK_UP_MODE;
					timer= 3;
				}
				if (timer<1d)  // try straight for a while 
					if ((ti.task!=TASK_FOLLOW) &&( ((s.xpos-sm.offsetx) > md.world_width/2.3) ||
							((s.xpos-sm.offsetx) < -md.world_width/2.3) || 
							((s.ypos-sm.offsety) > md.world_height/2.3) ||
							((s.ypos-sm.offsety)< -md.world_height/2.3) )){
						mode=SPIN_RIGHT_MODE;
						timer=400.0d/md.time_factor;
					}

				break;
			case BACK_UP_MODE:
				s.steer=0d;
				s.throttle=-1d;
				break;
			case BACK_UP_LEFT_MODE:
				s.steer=0d;
				s.throttle=-1d;
				break;	
			case SPIN_LEFT_MODE:
				s.steer=1.0d;
				if (s.speed>0.25) {
					s.throttle=-0.5f;
				} else {
					if (s.speed< -0.25f) {
						s.throttle=0.5f;
					}
				}
				break;	
			case SPIN_RIGHT_MODE:
				s.steer=-1.0d;
				if (s.speed>0.25) {
					s.throttle=-0.5f;
				} else {
					if (s.speed< -0.25f) {
						s.throttle=0.5f;
					}
				}
				break;				
		}
	}
	
	void StartAttackTask(ship s, int tid) {
	// this actually seems like it would break start attack task
		
		/*
		targetid=tid;
		task=TASK_ATTACK;
	*/
		taskitem sat=new taskitem();
		sat.priority=50;
		sat.task= TASK_ATTACK;
//		sat.mode= CRUISE_STRAIGHT_MODE;
		sat.me = s;
		sat.controlid= s.id;
		sat.targetid = tid;
		sat.target = sm.GetShip(tid);
		// fff.me\
		sat.endobj = sm.GetShip(tid);
		sat.mypath=null;
		sat.onpoint=0;  // I can use this for the other kind of follow
		AddTask(sat);
	
	}
	

	
	private void DoAttackTaskItem(taskitem ti) {
		ship tkill= md.FindShip(ti.targetid);  // can cross a line
		ship me = sm.GetShip(ti.controlid);
		if (me==null)
			return;  // why am I doing this

		if (tkill==null) {
			ti.targetid=0;
		//	task=TASK_EXPLORE;
			DeleteTask(ti);		
			return;
		}
		
		
		// Can I head toward my opponent?
		
		if (mode!=CRUISE_STRAIGHT_MODE)
			return;  // you need to deal with collisions before fighting
		
		
		double dist= me.DistanceSq(tkill);
		if ((tkill!=null) && (dist> 40000)) {
			if (im.LineCollides(me.xpos, me.ypos, tkill.xpos, tkill.ypos)) {
			// make a path
				
				taskitem pt=new taskitem();

		
				pt.priority=10;  // ahead of attack, which is 50
				pt.task= TASK_PATH;
			//	pt.mode= CRUISE_STRAIGHT_MODE;
				pt.target = sm.GetShip(ti.targetid);
				// fff.me
				pt.endobj = sm.GetShip(ti.targetid);
				pt.mypath=im.GetPath(me.xpos, me.ypos, tkill.xpos, tkill.ypos);
				pt.onpoint=0;  // I can use this for the other kind of follow
				pt.controlid=me.id;
				pt.me=me;
				pt.path_dir= PATH_ONE_TIME;
				pt.current_dir = DIR_FORWARD;
				AddTask(pt);
			
			
			}
			else {
				turnaim=Math.atan2((me.xpos-tkill.xpos), (me.ypos-tkill.ypos))+Math.PI;	
				me.gunaim1=FireSolution(me);
				me.throttle=1.0d;
				DoTargetTurnMode(me,tkill,0);
				
				if (dist < md.win_width*md.win_height)
					me.trigger=1;
				
			}
		}
		else {
			
			DoTargetTurnMode(me, tkill, attack_angle);
			me.gunaim1=FireSolution(me);
			me.throttle=0;
			me.trigger=1;
	//		DeleteTask(ti);
			// task explore is always a low priotity
			//  task=TASK_EXPLORE;  // which means you fight
			
		}
		
	}
	
	public void StartFleeTask(ship me, Path p, int priority) {
		// now try adding this as a high priority TASK
		taskitem ft=new taskitem();
		ft.task = TASK_FLEE;  
		ft.priority = priority;
		ft.controlid = me.id;
		ft.me = me;
		ft.mypath=p;
		ft.onpoint= 0;
		ft.path_dir = PATH_ONE_TIME;
		ft.current_dir = DIR_FORWARD;
		
		ft.time_out = 40000d/md.time_factor;
		
		AddTask(ft);	
	}
	
	
	public void DoFleeTask(taskitem tp, double t) {
		ship s = sm.GetShip(controlid);
		if (s==null)
			return;
		double goal_speed=s.GetMaxSpeed();  // optimal turning
		
		if (tp.mypath!=null) {
			if (s.speed< goal_speed) {
				s.throttle=1.0;
			} else s.throttle=0.0d; 
			WorldThing wt= new WorldThing();
			wt.xpos=tp.mypath.xmat[tp.onpoint];
			wt.ypos=tp.mypath.ymat[tp.onpoint];
			turnaim=Math.atan2((s.xpos-wt.xpos), (s.ypos-wt.ypos))+Math.PI;	
			DoTargetTurnMode(s, wt, 0);
			
			double dist= (s.xpos-wt.xpos)*(s.xpos-wt.xpos)+(s.ypos-wt.ypos)*(s.ypos-wt.ypos);
			if (dist < 4000) {
				tp.onpoint++;
				if (tp.onpoint> tp.mypath.numpoints-1) {
					tp.onpoint=0;
					tasklist.remove(0);  // get rid of this task
				}
			}
		} else
		{
			tasklist.remove(tp);
		}
		
		tp.time_out-=t;
		if (tp.time_out < 0d) {
			tasklist.remove(tp);  // this should prevent infinite flee because I can't get 
		}

	}
		public void StartFleetFollow(ship me, ship target, int priority, int offx, int offy) {
			taskitem ff=new taskitem();
			ff.priority=priority;
			ff.task= TASK_FLEET_FOLLOW;
//			ff.mode= CRUISE_STRAIGHT_MODE;
			ff.me = md.FindShip(controlid);
			ff.target = target;
			ff.targetid= target.id;
			
			ff.follow_offset = new WorldThing();
			ff.follow_offset.xpos = offx;
			ff.follow_offset.ypos = offy;
			// fff.me\
			ff.endobj = target;
			ff.mypath=null;
			ff.onpoint=0;  // I can use this for the other kind of follow
			ff.trail_points.clear();
			
			ff.trail_points.add(me);
			
			AddTask(ff);
			timer=0;
		}
	
		
	public void DoFleetFollow(taskitem ti, double t) {
	
		ship tship=md.FindShip(ti.targetid);
		

		// as opposed to sm getship
		
		if ((tship==null) || (tship.type!=ship.stype.SHIP)) {
			DeleteTask(ti);
			return;
		}
		boolean backup=false;
		
		ship me=sm.GetShip(controlid);
		if (me==null) 
			return;
		ti.trail_timer+=t;
		
		//ti.follow_offset.xpos = -100.0d;
		//ti.follow_offset.ypos = 0.0d;
		
		if (true) {//(ti.trail_timer > 32d/md.time_factor) {
			ti.trail_timer=0;
			WorldThing startpt=new WorldThing();
			startpt.xpos=tship.xpos+ti.follow_offset.rotX(tship.rotation+Math.PI/2);
			startpt.ypos=tship.ypos+ti.follow_offset.rotY(tship.rotation+Math.PI/2);
			startpt.rotation= tship.rotation;
			
			int n = ti.trail_points.size();
			if (n > 1) {
				WorldThing testpt = ti.trail_points.get(ti.trail_points.size()-1);
				if (testpt.DistanceSq(startpt)> 10)	{// don't pile up the same point in space		
					ti.trail_points.add(startpt);
					n++;
				}
			} else
				ti.trail_points.add(startpt);
	
			if (n > 5) {
				ghost.SetWorldPoints(ti.trail_points.get(0).xpos, ti.trail_points.get(0).ypos, ti.trail_points.get(0).rotation);
				int bump = sm.TestCollision(ghost);
				if (bump!=ti.targetid) {
					ti.trail_points.get(0).CopyTo(me.mmove);
					ti.trail_points.remove(0);
				} else
					ti.trail_points.remove(startpt);  // don't keep adding follow points if you are crashing into the guy
			}
			

		}
		
		me.magic_move=true;
		if (me.mmove==null) {
			me.mmove= new WorldThing();
			me.CopyTo(me.mmove);
		}
		me.steer=0;
		me.throttle=1;	
	}
	
	
	public void DoFleetFireControl(taskitem ti, double t) {
		
		
		ship tship=md.FindShip(ti.targetid);
		

		// as opposed to sm getship
		
		if ((tship==null) || (tship.type!=ship.stype.SHIP)) {
			DeleteTask(ti);
			return;
		}
		// set my aim to leader's aim
		// my fire button to the leaders
		ship me=sm.GetShip(controlid);
		if (me==null) 
			return;
		
		me.gunaim1= tship.gunaim1;
		me.trigger = tship.trigger;
		if (me.trigger!=0) 
			if (FriendlyFire(me, me.gunaim1))
				me.trigger=0;
	
	}
	
	public void StartFollow(ship me, ship t, double x, double y) {
		taskitem ti = new taskitem();
		ti.follow_offset=new WorldThing();
		ti.follow_offset.xpos=x;
		ti.follow_offset.ypos=y;
		ti.targetid=t.id;
		ti.task= TASK_FOLLOW;
		ti.priority=50;

		
		
		// start trail path timer
		// add first point
		ti.trail_timer=0;
		ti.trail_points = new ArrayList<WorldThing>();
		
		WorldThing startpt=new WorldThing();
	//	startpt.xpos=t.xpos+x;
	//	startpt.ypos=t.ypos+y;
	//	startpt.xpos=me.xpos;
	//	startpt.ypos=me.ypos;
	//	ti.trail_points.add(startpt);
		AddTask(ti);
	}
	
	
	// make this compare my vector to the vector of who I am following
	
	double DoFollowTask(taskitem ti, double t) {
		// follow a target ship
		// shoot back but don't stray from following
		// you can follow ahead or follow to the side
		
		
		// every time the trail path timer gets to 30
		// add a path point to the back + follow offset
		// if I can line past a point in IslandManager, delete it
		// aim to the trailing point
		

		
		// I need to find Ship across a variety of sm's
		ship tship=md.FindShip(ti.targetid);
		

		// as opposed to sm getship
		
		if ((tship==null) || (tship.type!=ship.stype.SHIP)) {
			DeleteTask(ti);
			return 0;
		}
		boolean backup=false;
		
		ship me=sm.GetShip(controlid);
		if (me==null) return 0;
		
		if (mode== BACK_UP_MODE) {
			timer=timer-t;
			if (me.collided!=0) 
				me.steer=1;
			
			if (timer<0)
				mode=CRUISE_STRAIGHT_MODE;
			return me.DistanceSq(tship);
		}		
		

		ti.trail_timer+=t;
		
		//follow_offset.xpos = -150.0d;
		
		
		if (ti.trail_timer > 500d/md.time_factor) {
			ti.trail_timer=0;
			WorldThing startpt=new WorldThing();
			startpt.xpos=tship.xpos+follow_offset.rotX(tship.rotation+Math.PI/2);
			startpt.ypos=tship.ypos+follow_offset.rotY(tship.rotation+Math.PI/2);
			startpt.rotation= tship.rotation;
	/*		
			if (me.mmove!=null) {
				startpt.CopyTo(me.mmove);
			}
			else me.mmove=new WorldThing();
		*/	
			
			if ((im.LineCollides(me.xpos, me.ypos, startpt.xpos, startpt.ypos)) || (ti.trail_points.size() < 1)) {		
				ti.trail_points.add(startpt);
			} else {
				// really I want to remove every point
				//ti.trail_points.remove(0);
				ti.trail_points.clear();
				ti.trail_points.add(startpt);
			}
		}
		/*
		goal.xpos=follow_offset.rotX(tship.rotation)+tship.xpos;
		goal.ypos=follow_offset.rotY(tship.rotation)+tship.ypos;
		*/
		me.throttle=0d;
		
		if (ti.trail_points.size()< 1) 
			return me.DistanceSq(tship);
		ti.trail_points.get(0).CopyTo(goal);
		
		
		
		turnaim=Math.atan2((me.xpos-goal.xpos), (me.ypos-goal.ypos))+Math.PI;		
		// Navigate to point mode
		
		//DoTargetTurnMode(me, tship, 0);
		DoTargetTurnMode(me, goal, 0);

		
		WorldThing myvec = new WorldThing();
		WorldThing tvec = new WorldThing();
		myvec.xpos=me.yvel*10d;
		myvec.ypos=me.xvel*10d;
		/*
		 * Since a path point isn't moving, don't pay attention to target speed
		 * 
		tvec.xpos=tship.speed*Math.cos(tship.rotation)*10d;
		tvec.ypos=tship.speed*Math.sin(tship.rotation)*10d;
		goal.xpos+=tvec.xpos-myvec.xpos;
		goal.ypos+=tvec.ypos-myvec.ypos;
		*/

	//	goal.xpos-=myvec.xpos;
	//	goal.ypos-=myvec.ypos;
		
		double dist=me.DistanceSq(goal);
		if (dist <20000) {
			/*
			if (ti.trail_points.size()==1) {	
				me.throttle=-0.8*dist/20000;
			} else
			*/
			if (ti.trail_points.size() > 1)
			{
				// im at a point
				ti.trail_points.remove(0);
				ti.trail_points.get(0).CopyTo(goal);
				/*
				if ((!im.LineCollides(me.xpos, me.ypos, goal.xpos, goal.ypos)) && (ti.trail_points.size() > 1)) {
					ti.trail_points.remove(0);
					// I could do this recursively
				}
				*/

			}
		} else me.throttle=Math.min(1.0d,0.5*(dist-50000)/50000);
		
		// Show trail points
		if (md.show_ai!=0) {
			RayText trailtxt = new RayText();
			for (int i=0; i < ti.trail_points.size(); i++) {
				trailtxt.DrawScaleText((int)ti.trail_points.get(i).xpos, (int)ti.trail_points.get(i).ypos, String.valueOf(i), 0.3f);
			}
		}
		/*
		// if you crash do something
		if (me.collided==1) {
			me.throttle=-1;
			if (me.steer > 0)	{
				me.steer=1;
			} else me.steer=-1;
		}
		*/
		int will_bump=ShipCollisionPrediction(me, 5.0f);  // a little tighter 5.0
		if (will_bump==me.id) {
			will_bump=0;
		}

		if (will_bump==0) {
			will_bump=IslandCollisionPrediction(me, 5.0f);

		}

		if (will_bump!=0) { 	
			
			if (will_bump!=ti.targetid)
			{
				mode=BACK_UP_MODE;
				timer=5;
			}
			else {
				me.throttle=-1;
				backup=true;
			}
		}

		

		return me.DistanceSq(tship);
		
	}
	
	
	void StartFollowPath(ship s, Path p, int priority, int direction) {
		taskitem ti=new taskitem();
		ti.controlid=s.id;
		ti.task=TASK_PATH;
		ti.mypath= new Path();
		ti.mypath=p.CopyTo(ti.mypath);//  bad idea!
		ti.priority=priority;
		ti.path_dir=direction;
		ti.current_dir = DIR_FORWARD;
		AddTask(ti);
		
	}
	
	void DoAvoid(taskitem tp, double t) {
		// Here is wher I should check for collision
		if (mode== BACK_UP_MODE) {
			timer=timer-t;
			tp.me.throttle=-1;
			//if (tp.me.collided!=0) 
				tp.me.steer=1;
			
			if (timer<0)
				mode=CRUISE_STRAIGHT_MODE;
			return ;
		}		
		int will_bump= ShipCollisionPrediction(tp.me, 5.0f);  // a little tighter 5.0
		if (will_bump==tp.me.id) {
			will_bump=0;
		}

		if (will_bump==0) {
			will_bump= IslandCollisionPrediction(tp.me, 5.0f);

		}

		if (will_bump!=0) { 	
			mode=BACK_UP_MODE;
			tp.me.steer = -1;  // do some turning so you don't cycle
			timer=1600/md.time_factor;
		}		
	}
	void DoPathTask(taskitem tp, double t) {
		
		ship s = sm.GetShip(controlid);
		if (s==null)
			return;
		double goal_speed=s.GetMaxSpeed()/2;  // optimal turning
		
		tp.me=s;
		
		if (tp.mypath!=null) {
			AdjustThrottle(s, goal_speed);
			WorldThing wt= new WorldThing();
			wt.xpos=tp.mypath.xmat[tp.onpoint];
			wt.ypos=tp.mypath.ymat[tp.onpoint];
			turnaim=Math.atan2((s.xpos-wt.xpos), (s.ypos-wt.ypos))+Math.PI;	
			DoTargetTurnMode(s, wt, 0);
			
			if ((tp.onpoint < tp.mypath.numpoints-2) || (tp.path_dir!=PATH_ONE_TIME))
				DoAvoid(tp, t);
			
			
			double dist= (s.xpos-wt.xpos)*(s.xpos-wt.xpos)+(s.ypos-wt.ypos)*(s.ypos-wt.ypos);
			if (dist < 4000) {
				
				island isl=im.AtPort(s);
				if (isl!=null) {
					md.SendGotToPort(s.id, isl.id); // money for cargo?
					s.type=ship.stype.EMPTY;
					sm.DeleteShip(s.id);
					return;
				}
				
			
				if (tp.current_dir== DIR_FORWARD) {
					tp.onpoint++;
				}
				else 
					tp.onpoint--;
			


				
				
				
				if ((tp.onpoint> tp.mypath.numpoints-1) || (tp.onpoint < 0)) { 
					if (tp.path_dir== PATH_ONE_TIME) {
					
						tasklist.remove(tp);  // get rid of this task
					}
					if (tp.path_dir== PATH_CIRCULAR) {
						tp.onpoint=0;
					}
					if (tp.path_dir == PATH_REVERSE) {
						if (tp.onpoint < 0) {
							tp.onpoint=0;
							tp.current_dir=DIR_FORWARD;
						}
						if (tp.onpoint > tp.mypath.numpoints-1) {
							tp.onpoint=tp.mypath.numpoints-1;
							tp.current_dir=DIR_BACKWARD;
						}
						
					}
					
				}
			}
		} else
		{
			tasklist.remove(tp);
		}
		
		
	}
	
	
	Path GetCurrentPath() {
		taskitem ti  = tasklist.get(0);
		return ti.mypath;
	}
	
	int GetOnPoint() {
		taskitem ti  = tasklist.get(0);
		return ti.onpoint;	
	}
	
	public void StartWait(int priority, double duration) {
		ship me=sm.GetShip(controlid);
		if (me==null) return;
		me.steer=0.0d;
		me.throttle=0.0d;
		
	//	task=TASK_WAIT;
		taskitem ti= new taskitem();
		ti.task=TASK_WAIT;
		ti.time_out = duration;
		ti.priority = priority;  // higher than default
		AddTask(ti);
		
	}
	
	public void DoWait(taskitem ti, double t) {
		ship me=sm.GetShip(controlid);
		if (me==null) return;
		me.steer=0.0d;
		me.throttle=0.0d;
		
		ti.time_out-=t;

		if (ti.time_out < 0)
			DeleteTask(ti);		
	}
	
	public void StartCrateGrab(int priority, double duration, WorldThing crate) {
		ship me=sm.GetShip(controlid);
		if (me==null) return;

		
	//	task=TASK_WAIT;
		taskitem ti= new taskitem();
		ti.me = me;
		ti.task=TASK_CRATE_GRAB;
		ti.time_out = duration;
		ti.priority = priority;  // higher than default
		ti.endobj = crate;
		AddTask(ti);
	
	}
	

	
	public void DoCrateGrab(taskitem ti, double t) {
		if (ti.me==null)
			return;
		pickup pk = md.FindPickup(ti.endobj.id);
		
		if ((ti.endobj==null) || (ti.time_out < 0d) || (pk==null)) {
			DeleteTask(ti);		
		}
		


		
		
		if (ti.mypath!=null) {
			if (!FollowPath(ti))  // set goal to path pt 
			{
				tasklist.remove(ti);
				return;
			}
			if (!im.LineCollides(ti.me.xpos, ti.me.ypos, goal.xpos, goal.ypos))
				ti.mypath = null;
		} else {
			ti.endobj.CopyTo(goal);	
			if (im.LineCollides(ti.me.xpos, ti.me.ypos, goal.xpos, goal.ypos));
				ti.mypath = im.GetPath(ti.me.xpos, ti.me.ypos, goal.xpos, goal.ypos);
		}
			
		

		turnaim=Math.atan2((ti.me.xpos-goal.xpos), (ti.me.ypos-goal.ypos))+Math.PI;		
		// Navigate to point mode
		
		//DoTargetTurnMode(me, tship, 0);
		DoTargetTurnMode(ti.me, goal, 0);
		DoAvoid(ti, t);
		
		double goal_speed = ti.me.GetMaxSpeed()/2;  // best for turning
		if (goal.DistanceSq(ti.me) > 10000) {
			goal_speed = ti.me.GetMaxSpeed();
			AdjustThrottle(ti.me, goal_speed);
		}
		
			
	}
	
	public void StartMinelaying(int priority, double duration, WorldThing place) {
		ship me=sm.GetShip(controlid);
		if (me==null) return;

		taskitem ti= new taskitem();
		ti.me = me;
		ti.task=TASK_MINE_LAY;
		ti.time_out = duration;
		ti.priority = priority;  // higher than default
		ti.endobj = place;
		AddTask(ti);
	}	
	
	public void DoMinelaying(taskitem ti, double t) {
		// just spin around with trigger down for now
		ti.me.steer = 0.2d;
		ti.me.trigger = 1;
		AdjustThrottle(ti.me, 0.1d);	
		
	}
	
	public boolean FollowPath(taskitem ti) {
		goal.xpos = ti.mypath.xmat[ti.onpoint];
		goal.ypos = ti.mypath.ymat[ti.onpoint];
		if (ti.me.DistanceSq(goal) < 2000) {
			ti.onpoint++;
			if (ti.onpoint > ti.mypath.numpoints-1) {
				return false;
			}
		}
		return true;
	}
	void SetGunAim(ship s, ship target, double dist) {
		WorldThing fp= new WorldThing();
		if (skill==0) {
			fp.xpos=target.xpos;
			fp.ypos=target.ypos;
		}
		else
		{
			// the 100 ought to be the shot speed
			fp.xpos=target.xpos-Math.cos(target.rotation-Math.PI/2)*target.speed*dist/100 +
					Math.cos(s.rotation-Math.PI/2)*s.speed*dist/100;
			fp.ypos=target.ypos-Math.sin(target.rotation-Math.PI/2)*target.speed*dist/100 +
					Math.sin(s.rotation-Math.PI/2)*s.speed*dist/100;		
		}
		
		
		if (md.show_ai!=0) {
			GL11.glLineWidth(1.0f);
			GL11.glBegin(GL11.GL_LINES);
			GL11.glColor3f(0.9f, 0.9f, 0.0f);		
			GL11.glVertex3d(fp.xpos-10, fp.ypos,0f);
			GL11.glVertex3d(fp.xpos+10, fp.ypos, 0f);
			GL11.glVertex3d(fp.xpos, fp.ypos+10, 0f);
			GL11.glVertex3d(fp.xpos, fp.ypos-10, 0f);
			GL11.glEnd();
			
		}

	
	
		s.gunaim1=Math.atan2((s.xpos-fp.xpos), (s.ypos-fp.ypos))+Math.PI;
	}
	

	
	public ship PickEnemy(ship s) {
		ship target;
		
		if (tasklist.size()==0)
			return null;
		taskitem ti=tasklist.get(0);
		
		if (ti.targetid!=0) {
			target=sm.GetShip(ti.targetid);
		}
		else {
			ti.targetid=0;
			target=sm.GetShip(md.userid);
			if ((s!=null) && (target!=null) && (s.team==target.team)) {
				int tid=sm.FindClosestEnemyShip(s, controlid, s.team);
				target = sm.GetShip(tid);
			}	
		}
		
		if ((s!=null) && (target!=null) && (md.relations[s.team][target.team]!=md.ENEMIES)) {
		//	target=null;
			
			int tid=sm.FindClosestEnemyShip(s, controlid, s.team);
			target = sm.GetShip(tid);
			if ((target!=null) && (target.type!=ship.stype.SHIP)) {
				target=null;
			}

		}
		
		return target;
	}
	// this just pulls a trigger
	double FireSolution(ship s) {
		if (s==null) return 0;
		int tid=sm.FindClosestEnemyShip(s, controlid, s.team);
		ship target = sm.GetShip(tid);

		if (target== null)
			return 0;  // just aim ahead
		return Math.atan2((s.xpos-target.xpos), (s.ypos-target.ypos))+Math.PI;
	}
	
	boolean FriendlyFire(ship me, double aim) {

		WorldThing wt = new WorldThing();
		boolean friend = false;
		for (int i= 0; i < 6; i++) {
			wt.xpos= me.xpos + 100*Math.cos(aim)*i;
			wt.ypos= me.ypos + 100* Math.sin(aim)*i;	
			if (sm.CheckFriendlyFire(me, wt, me.id, me.team))
				friend = true;
		}
		return friend;
	}

	
	// This should be able to turn off throttle, turning in some cases
	void FireControl(ship s) {
		s.trigger=0;
		ship target=PickEnemy(s);
		if (target!=null) {

			
			double dist= Math.sqrt(s.DistanceSq(target));
			if (dist<800) {
				if (mode== CRUISE_STRAIGHT_MODE) {
					SetGunAim(s, target, dist);
					turnaim= s.gunaim1;
					DoTargetTurnMode(s, target, 0);
					s.throttle=0.5f;
					if (mystrat==strategy.TORPEDO) {
						if (!FriendlyFire(s, s.gunaim1))
							s.trigger=1;
						return;
					}
				}
			}
			
			if ((mystrat==strategy.GUNBOAT)  && 
					(target.xpos-s.xpos)*(target.xpos-s.xpos)+(target.ypos-s.ypos)*(target.ypos-s.ypos)<200000) {
				s.gunaim1=Math.atan2((s.xpos-target.xpos), (s.ypos-target.ypos))+Math.PI;
				turnaim=s.gunaim1;
				if (!FriendlyFire(s, s.gunaim1))
					s.trigger=1;
				if (mode == CRUISE_STRAIGHT_MODE) {
					DoTargetTurnMode(s, target, Math.PI/2d);
				}
			}

		}	
	}
	

	
	// this is what you call when you want to turn to an angle
	
	// why does GunAim work but not targetpos?
	void DoTargetTurnMode(ship s, WorldThing target, double roffset) {
		  double unitx = target.xpos- s.xpos;
		  double unity = target.xpos- s.ypos;
		  double mag=Math.sqrt(unitx*unitx+unity*unity);
		  if (mag <0.001d) mag=0.001d;  // never 0
		  unitx = -1*unitx/mag;
		  unity = -1*unity/mag;
		  
		  // alternate
		  unitx=Math.cos(turnaim);
		  unity=Math.sin(turnaim);
		  
		  double dotF = Math.cos(s.rotation+roffset) * unitx + Math.sin(s.rotation+roffset) * unity;
		  double dotR = Math.cos(s.rotation+roffset) * unity + Math.sin(s.rotation+roffset) * unitx;
		  
		  if (dotF>0.99) {
			  // you are on track
			  s.steer=0;
			  return;
		  }
		  
		  // target to the left
		  if (dotR > 0) {
			  s.steer=-1d;
		  }
		  else
			  s.steer=1d;
	}
	
	/*
	 * If someone is close, on my team and I am facing them and they aren't facing me, follow them.  
	 * 
	 * If the player is medium close, turn toward him and go forward.  
	 * 
	 * If the player is in firing range, turn sideways (optimal fire direction TBD).  
	 */
	
	
	void TorpedoStrategy(ship s, ship target, double t) {
		if ((s!=null) && (target!=null)) {
			double dist=Math.sqrt(s.DistanceSq(target));
			SetGunAim(s, target, dist);
			turnaim=s.gunaim1;
			s.throttle=0d;
			s.trigger=0;
			DoTargetTurnMode(s, target, attack_angle);

			if (dist<Math.sqrt(md.win_width*md.win_height)) {
				


					if ((CheckForObstacles(s, 50d)==0) && (dist > 200))  // check for obstacles ahead
					{
						s.throttle=0.5d;
					}
					else s.throttle=-0.5;
					
					if (attack_angle > (Math.PI*0.75)) {// in other words backwards	
						s.throttle=s.throttle*(-1);
					}
				
				s.trigger=1;
			}
		} else {
			if (s!=null) {
				UpdateMode(s, t);// so we don't get stuck in it
				// super hacky
				
				if (tasklist.size()==0) {
					return;  // should never happen, but 
				}
				taskitem ti=tasklist.get(0);
				
				
				if (ti.task==TASK_EXPLORE) {
					DoMode(s);
				}
				if (ti.task==TASK_ATTACK) {
					DoAttackTaskItem(ti);
				}
				if (ti.task==TASK_FLEE) {
					DoFleeTask(ti, t);
				}
				if (ti.task==TASK_FOLLOW) {
					DoFollowTask(ti, t);
				}
				if (ti.task==TASK_FLEET_FOLLOW) {
					DoFleetFollow(ti, t);
				}
				if (ti.task==TASK_PATH) {
					DoPathTask(ti, t);
				}
				if (ti.task == TASK_CRATE_GRAB) {
					DoCrateGrab(ti, t);
					
				}		
				if (ti.task == TASK_MINE_LAY) {
					DoMinelaying(ti, t);
				}
			}
		}
	}
	
	void FreighterStrategy(ship s, double t) {
		
		// send distress to Fleet Command / MP? when you are being attacked, call in reinforcements

		taskitem ti=tasklist.get(0);
		
		int currenttask= ti.task;
		
		//mypath=ti.mypath;  // so it will display correctly
		//onpoint=ti.onpoint;

		if (mode== BACK_UP_MODE) {
			timer=timer-t;
			if (timer<0)
				mode=CRUISE_STRAIGHT_MODE;
			return;
		}
	
		if (currenttask== TASK_FOLLOW) {
			// this cheats the usual freighter strategy for when it is following
			DoFollowTask(ti, t);
			return;
		}
		if (ti.task==TASK_FLEET_FOLLOW) {
			DoFleetFollow(ti, t);
		}		
		if (currenttask== TASK_WAIT) {
			DoWait(ti, t);
			return;
		}
		
		if (currenttask == TASK_PATH) {
			DoPathTask(ti, t);
			return;
		}
		
		if (currenttask == TASK_FLEE) {
			DoFleeTask(ti,t);
		}
		
		if (currenttask == TASK_CRATE_GRAB) {
			DoCrateGrab(ti, t);
			
		}
		if (ti.task == TASK_MINE_LAY) {
			DoMinelaying(ti, t);
		}	
		/* 
		 *  SHOULD I INCLUDE DO MODE + UPDATE MODE?
		 */
		/*

		
		
		
		double goal_speed=s.GetMaxSpeed()/2;
		if (currenttask== TASK_FLEE) goal_speed=s.GetMaxSpeed();
		if (ti.mypath!=null) {
			if (s.speed< goal_speed) {
				s.throttle=0.5d;
			} else s.throttle=0.0d; 
			WorldThing wt= new WorldThing();
			wt.xpos=ti.mypath.xmat[ti.onpoint];
			wt.ypos=ti.mypath.ymat[ti.onpoint];
			turnaim=Math.atan2((s.xpos-wt.xpos), (s.ypos-wt.ypos))+Math.PI;	
			DoTargetTurnMode(s, wt, 0);
			
			int will_bump=ShipCollisionPrediction(s, 10.0f);
			if (will_bump==s.id) {
				will_bump=0;
			}

			if (will_bump==0) {
				will_bump=IslandCollisionPrediction(s, 10.0f);

			}

			// don't evade if we are heading to our goal
			if ((will_bump!=0) && (ti.onpoint< ti.mypath.numpoints-1)) { 	
				mode=BACK_UP_MODE;
				timer=5;
				s.throttle=0;
				s.steer=-1;
			} else
			{
			
				double dist= (s.xpos-wt.xpos)*(s.xpos-wt.xpos)+(s.ypos-wt.ypos)*(s.ypos-wt.ypos);
				if (dist < 2000) {
					ti.onpoint++;
					if (ti.onpoint> ti.mypath.numpoints-1) {
						ti.onpoint=0;
						island isl=im.AtPort(s);
						if (isl!=null) {
							md.SendGotToPort(s.id, isl.id); // money for cargo?
							s.type=ship.stype.EMPTY;
							sm.DeleteShip(s.id);
						}
						if (currenttask==TASK_FLEE) {
							tasklist.remove(0);
						}
					}
				}
						
				
			}
	
		}
		else {
			s.throttle=0;
			
			// pop old goal in
			
			// I have no path
		}
	

		*/
			
	}
	
	void BargeStrategy(ship s, double t) {
		// keep your eye out for very distant enemies, shoot at them.  
		ship me = md.FindShip(controlid);
		ship target = PickEnemy(s);
		
		if (target!=null) {
			double dist= Math.sqrt(s.DistanceSq(target));
			if (dist<md.win_width*md.win_height) {
				SetGunAim(me, target, dist);
				if (!FriendlyFire(s, s.gunaim1))
					s.trigger=1;
			}
		}
	}
	
	void Update(double t) {
		// Based on what I am, update the controls or whatever for me.  
		// this will be how I make the turrets spin.

		timer=timer-t;
		
		ship s=sm.GetShip(controlid);
		
		if (controlid==md.userid)
			return;	
		
		ship target=sm.GetShip(md.userid);  // later pick closest enemy
		if ((s!=null) && (target!=null) && (md.relations[s.team][target.team]!=md.ENEMIES)) 
			target=null;
		
		if (s!=null) {
			ShowAI(s);
			/*
			if (!InBounds(s.xpos, s.ypos))
			{
				
				System.out.println(" sm.x  " + sm.offsetx + " sm.y " + sm.offsety + " xpos " + s.xpos + " ypos " + s.ypos);
				s.TakeDamage(s.max_health, null);  // destroy me

			}
			*/		
			if (mystrat==strategy.TORPEDO) {
				TorpedoStrategy(s, target, t);
				return;
		
			}
			
			if (mystrat==strategy.FREIGHTER) {
				FreighterStrategy(s, t);
				return;
			}
			if (mystrat == strategy.BARGE) {
				BargeStrategy(s, t);
				return;
			}
			
		}



		UpdateMode(s, t);

		//ship s=sm.GetShip(controlid);
		
		double fdist=0;
		if (s!=null) {

			taskitem ti=tasklist.get(0);
			
			if (ti.task==TASK_EXPLORE) {
				DoMode(s);
			}
			if (ti.task==TASK_ATTACK) {
				DoAttackTaskItem(ti);
			}
			if (ti.task==TASK_FLEE) {
				DoFleeTask(ti,t);
			}
			if (ti.task==TASK_FOLLOW) {
				fdist = DoFollowTask(ti,t);
			}
			if (ti.task==TASK_FLEET_FOLLOW) {
				DoFleetFollow(ti, t);
				fdist = 5000*5000;
				DoFleetFireControl(ti, t);
			}
			if (ti.task==TASK_PATH) {
				DoPathTask(ti, t);
			}
			if (ti.task == TASK_CRATE_GRAB) {
				DoCrateGrab(ti, t);
			}			
			if (ti.task == TASK_MINE_LAY) {
				DoMinelaying(ti, t);
			}
			if (fdist < 500*500) {
				FireControl(s);
			}
			else 
				FireSolution(s);
			

		}
		
	}
	
	public void Setup(ShipManager s, IslandManager i) {
		sm=s;
		im=i;
		ship me= s.GetShip(controlid);
		if (me!=null)  // very weird if it is null  // probably assigning the wrong sm
			me.hd.hull.CopyTo(ghost);
	}
	

	AIControl(aiType a, ShipManager s, IslandManager i, MissionData mdin, int id) {
		type=a;
		sm=s;

		im=i;
		md=mdin;
		controlid=id;

		mode=CRUISE_STRAIGHT_MODE;
		ghost = new sketch("hull.dat");
		timer=10.0d;

		goal = new WorldThing();
		follow_offset = new WorldThing();
		
		aidebug = new RayText();
		tasklist = new ArrayList<taskitem>();
	}
}


/*
 * I like concepts of FRONT and RIGHT vectors

    FRONT vector (x,y)
    RIGHT vector constructed as (y,-x) from FRONT vector

Then if you goal vector  (goalPos - yourPos) is

    GOAL gx,gy

You can use the following:

if dot(GOAL,FRONT) < 0
    then target is behind you and you need to turn around. Say by going right if dot(GOAL,RIGHT)>delta and left otherwise
else if dot(GOAL, RIGHT) < delta
     then goal is to your left
else if dot(GOAL,RIGHT) > delta
    then goal is to your right
else
    you are going more or less towards the goal

And don't jitter back and forth!!!  Use some kind of damping (e.g. look for slowTurn below)...

Here is my ObjectiveC code:

// Common strategy parameters
#define SLOW_TURN          0.1f                        // Thrust value for turning
#define FAST_TURN          0.5f
#define FAST_RUN           1.0f
#define GOAL_DELTA_SQR     (20.0f*20.0f)

- (BOOL) turnToGoal:(int)objID physInfo:(PhysicsInfo *)physInfo goal:(float*)aimDir
{
    // If aiming right at the target, then DONE
    BOOL result = NO;
    float turn = 0.0f;
    float dotF = physInfo->dir[0] * aimDir[0] + physInfo->dir[1] * aimDir[1];
    float dotR = physInfo->dir[0] * aimDir[1] - physInfo->dir[1] * aimDir[0];
    const float kFastCos = 0.97f;
    float slowTurn = FAST_TURN * (1.0f - _CLAMP_((dotF-kFastCos)/(1.0f-kFastCos), 0.0f, 1.0)); //...damping
    if (dotF > 0.9998f)    // cos(1deg)
    {
        // Force perfect aim - force my tank to face the way I want
        {
            physInfo->dir[0] = aimDir[0];
            physInfo->dir[1] = aimDir[1];
            [parentView->objectMgr updateControlThrust:objID thrustL:0 thrustR:0];
        }
        result = YES;
    }
    
    // If aiming towards right of target, then turn LEFT
    else if ( dotR > 0)
    {
        if ( dotF < kFastCos) // cos(15deg)
            turn = -FAST_TURN;
        else
            turn = -slowTurn;
    }
    // If aiming towards left of target, then turn RIGHT
    else
    {
        if ( dotF < kFastCos)
            turn = FAST_TURN;
        else
            turn = slowTurn;
    }
    // Don't overshoot (this is related to how far the treads are from the center of rotation, and speed of that tread,
    // from which I inverse compute the max thrust I can apply without making tread to travel too far i.e. turn too far...)
    // This is only important if you want to shoot a distant target...
    if (!result)
    {
        CraftInfo* craftInfo = craftPool+objID;
        CraftDef* craftDef = craftTable + craftInfo->craftDefID;
        
        // Compute new angle
        GLfloat dist = frameTime * turn * craftInfo->physicsInfo.speed;
        GLfloat a = (-dist / (2.0*craftDef->halfWidth)) + DEGREES_TO_RADIANS(craftInfo->physicsInfo.angle + 90.0f);

        // Compute new direction
        GLfloat dir[2];
        dir[0] = cosf(a);
        dir[1] = sinf(a);
        
        // If overshot, then clamp
        float newDotR = dir[0] * aimDir[1] - dir[1] * aimDir[0];
        if (newDotR * dotR < 0.0f && dotF > 0.0f)
        {
            physInfo->dir[0] = aimDir[0];
            physInfo->dir[1] = aimDir[1];
            physInfo->angle = RADIANS_TO_DEGREES(atan2(aimDir[1],aimDir[0])) - 90.0;
            [parentView->objectMgr updateControlThrust:objID thrustL:0 thrustR:0];
            turn = 0;
            result = YES;
        }

        DEBUG_TRACE4(("   turn %d %s %3f (dotFR=%4f %4f)\n", objID, turn < 0 ? "RIGHT" : "LEFT ", turn, dotF, dotR));
    }

    // Set controls    
    [parentView->objectMgr updateControlThrust:objID thrustL:turn thrustR:-turn];
    return result;
}

- (BOOL) moveToGoal:(int)objID physInfo:(PhysicsInfo *)physInfo goal:(float*)tgtPos
{
    // If close to the goal point, quit with YES
    float dx = tgtPos[0] - physInfo->pos[0];
    float dy = tgtPos[1] - physInfo->pos[1];
    float dsqr = dx * dx + dy * dy;
    if (dsqr < GOAL_DELTA_SQR)
        return YES;

    // Turn towards the goal first
    float dist = sqrtf(dsqr);
    dx /= dist;
    dy /= dist;
    float dotR = dx * physInfo->dir[1] - dy * physInfo->dir[0];
    float dotF = dx * physInfo->dir[0] + dy * physInfo->dir[1];
    
    if (dotF < 0.0)                //... turn around
    {
        if (dotR > 0.1)
            [parentView->objectMgr updateControlThrust:objID thrustL:SLOW_TURN thrustR:-SLOW_TURN];
        else
            [parentView->objectMgr updateControlThrust:objID thrustL:-SLOW_TURN thrustR:SLOW_TURN];
        DEBUG_TRACE4(("   move %d TURN AROUND %3f (dotR=%f)\n", objID, -SLOW_TURN, dotR));
    }
    else if (dotR > 0.1)        //... turn LEFT
    {
        [parentView->objectMgr updateControlThrust:objID thrustL:SLOW_TURN thrustR:-SLOW_TURN];
        DEBUG_TRACE4(("   move %d LEFT  %3f (dotR=%f)\n", objID, SLOW_TURN, dotR));
    }
    else if (dotR < -0.1)        //... turn RIGHT
    {
        [parentView->objectMgr updateControlThrust:objID thrustL:-SLOW_TURN thrustR:SLOW_TURN];
        DEBUG_TRACE4(("   move %d RIGHT %3f (dotR=%f)\n", objID, -SLOW_TURN, dotR));
    }
    else                        //... run FORWARD
    {
        [parentView->objectMgr updateControlThrust:objID thrustL:FAST_RUN thrustR:FAST_RUN];
        DEBUG_TRACE4(("   move %d FWD   %3f (dotR=%f)\n", objID, FAST_RUN, dotR));
    }
    return NO;
}

*/
