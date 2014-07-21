
public class ProjectileData {
	public enum pType {
		EMPTY, BULLET, MISSILE, TORPEDO, MINE, EXPLOSION, SPLASH   // a splash is when you don't hit but you don't last longer
	}
	pType type;  // empty, bullet, missile
	sketch psketch;
	DamageSketch dsketch;
	double xvel, yvel;
	double speed;  // in case it changed direction
	double acceleration;
	double lifetime;  // until I splash
	
	double depth;  // what depth am I (if I am a torpedo or a mine I sink)
	double depth_timer;  // do I need this?

	int target; // guided
	int owner;  // don't let yourself collide with owner
	int team;  // for visibillty rules for mines, torpedos

	int aim;  // from compoenent data aim
	
	int damage;  // projectile , torpedo 2
}
