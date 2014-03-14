package u1l;
import robocode.*;
import java.awt.Color;

// API help : http://robocode.sourceforge.net/docs/robocode/robocode/Robot.html

/**
 * ProtoSpencerTron - a robot by Carl Adrian P. Castueras and Mary Aracelli S. Basbas
 */
public class ProtoSpencerTron extends AdvancedRobot
{
	private EnemyBot enemy = new EnemyBot();
	private byte moveDirection = 1;
	private int wallMargin = 60;
	private int tooCloseToWall = 0;
	

	/**
	 * run: ProtoSpencerTron's default behavior
	 */
	public void run() {
		// Initialization of the robot should be put here

		// After trying out your robot, try uncommenting the import at the top,
		// and the next line:

		// setColors(Color.red,Color.blue,Color.green); // body,gun,radar
		setBodyColor(Color.green);
		setGunColor(Color.yellow);
		setRadarColor(Color.white);
		setBulletColor(Color.cyan);
		setScanColor(Color.cyan);		
		
		addCustomEvent(new Condition("too_close_to_walls"){
					public boolean test(){
						return(
							(getX() <= wallMargin ||
							getX() >= getBattleFieldWidth() - wallMargin ||
							getY() <= wallMargin ||
							getY() >= getBattleFieldWidth() - wallMargin)
						);
					}
				});

		setAdjustRadarForRobotTurn(true);
		setAdjustGunForRobotTurn(true);
		enemy.reset();
		// Robot main loop
		while(true) {
		
			setTurnRadarRight(360);
			doMove();
			execute();
			
		}
	}

	/**
	 * onScannedRobot: What to do when you see another robot
	 */
	public void onScannedRobot(ScannedRobotEvent e) {
		// Replace the next line with any behavior you would like
		
		//find an enemy
		if(enemy.none() || 
			//track a closer enemy
			e.getDistance() < enemy.getDistance() ||
			//continue tracking the current enemy
			e.getName().equals(enemy.getName())
			){
			enemy.update(e);
		}
		setTurnRight(normalizeBearing(enemy.getBearing() + 90) - (15*moveDirection));
		//setTurnGunRight(getHeading() - getGunHeading() + enemy.getBearing());	
		fire(1);

	}

	/**
	 * onHitByBullet: What to do when you're hit by a bullet
	 */
	public void onHitByBullet(HitByBulletEvent e) {
		// Replace the next line with any behavior you would like
		//back(10);
	}
	
	/**
	 * onHitWall: What to do when you hit a wall
	 */
	public void onHitWall(HitWallEvent e) {
		// Replace the next line with any behavior you would like
		
		moveDirection *= -1;
		ahead(20 * moveDirection);
	}	
	
	public void doMove(){	
		if(this.getVelocity() == 0) moveDirection *= -1;
		if(getTime() % 20 == 0){
			moveDirection *= -1;
			
		}
		setAhead(100 * moveDirection);
		//setTurnRight(normalizeBearing(enemy.getBearing() + 90) - (15*moveDirection));
		setTurnGunRight(getHeading() - getGunHeading() + normalizeBearing(enemy.getBearing()));		
		setFire(Math.min(400 / enemy.getDistance(),3));	
	}
	
	public void onRobotDeath(RobotDeathEvent e){
		if(e.getName().equals(enemy.getName())){
			enemy.reset();
		}
	}
	double normalizeBearing(double angle){
		while(angle > 180) angle -= 360;
		while(angle < -180) angle += 360;
		return angle;
		
	}	
	
	public void onCustomEvent(CustomEvent e)
	{
		if(e.getCondition().getName().equals("too_close_to_walls")){
			if(tooCloseToWall <= 0){
				tooCloseToWall += wallMargin;
				setMaxVelocity(0);
			}
		}
	}
	//test
	//test
	//test
	//test
	//test
	
}
