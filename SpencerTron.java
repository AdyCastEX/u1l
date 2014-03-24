package u1l;
import robocode.*;
import java.awt.Color;
import static robocode.util.Utils.normalRelativeAngleDegrees;
import robocode.RobotStatus;
import java.awt.geom.Point2D;

// API help : http://robocode.sourceforge.net/docs/robocode/robocode/Robot.html

/**
 * SpencerTron - a robot by Carl Adrian P. Castueras and Mary Aracelli S. Basbas
 */
public class SpencerTron extends AdvancedRobot
{
	
	private AdvancedEnemyBot enemy = new AdvancedEnemyBot();
	private byte moveDirection = 1;
	private int wallMargin = 60;
	private int targetOffset = 9;
	private double targetTurn;
	private int mode;
	private int strafTime;
	
	private final int CLOSE_RANGE = 0;
	private final int BERSERK = 1;	
	private final int CHASER = 2;

	/**
	 * -----------------MODES---------------------
	 * CLOSE RANGE
	 * ->designed to close-in to an enemy while moving side-to-side
	 * ->priority is avoiding enemy shots while tracking and shooting an enemy 
	 * CHASER
	 * ->designed to chase a faraway enemy and shoot with predictive firing
	 * ->priority is following an enemy to get close enough while shooting
	 */

	/**
	 * run: ProtoSpencerTronV4's default behavior
	 */
	public void run() {
		
		//default mode is close range
		this.mode = CLOSE_RANGE;
		
		setBodyColor(Color.green);
		setGunColor(Color.yellow);
		setRadarColor(Color.white);
		setBulletColor(Color.cyan);
		setScanColor(Color.cyan);	
		
		//custom event declarations start
		addCustomEvent(new Condition("low_health") {
			public boolean test() {
				if(getEnergy() < 30)
				{
					return true;
				}
				
				else return false;
			}
		});
		
		addCustomEvent(new Condition("long_range_enemy"){
			public boolean test(){
			
				if(enemy.getDistance() > getBattleFieldWidth()/2)
				{
					return true;
				}
				else return false;
			}
		});	
		
		addCustomEvent(new Condition("short_range_enemy"){
			public boolean test(){
				if(enemy.getDistance() < getBattleFieldWidth()/16)
				{
					return true;
				}
				
				else return false; 
			}
		});
		
		//custom event declarations start

		//make the radar and gun move independent of the robot's body turn
		setAdjustRadarForRobotTurn(true);
		setAdjustGunForRobotTurn(true);
		enemy.reset();
		// Robot main loop
		while(true) {
			
			//keep spinning the radar to always be updated of the enemies around 
			setTurnRadarRightRadians(Double.POSITIVE_INFINITY);
			doMove();
			execute();
			
		}
	}

	/**
	 * onScannedRobot: What to do when you see another robot
	 */
	public void onScannedRobot(ScannedRobotEvent e) {
		
		if(enemy.none() || 
			//track a closer enemy
			e.getDistance() < enemy.getDistance() ||
			//continue tracking the current enemy
			e.getName().equals(enemy.getName())
			){
			enemy.update(e,this);
		}

		switch(this.mode)
		{
			case CLOSE_RANGE:
			{
			
				//a gun tracking algorithm based on Track Fire
				
				double absoluteBearing = getHeading() + enemy.getBearing();
				double gunTurn = normalRelativeAngleDegrees(absoluteBearing - getGunHeading());
				//use an offset to make up for any change caused by body movement 
				gunTurn -= (targetOffset*moveDirection);
				//turn the gun to an approximation of where the enemy is
				setTurnGunRight(gunTurn);
			
				//always turn 90 degrees relative to the enemy for ease of avoiding enemy shots
				targetTurn = enemy.getBearing() + 90; 
				//get closer to the enemy by adding positive or negative 45 degrees to turning angle
				targetTurn -= (45*moveDirection);
				setTurnRight(targetTurn);
				
				//fire only if the gun is not hot and if the gun is relatively close to the enemy 	
				if(getGunHeat() == 0 && Math.abs(getGunTurnRemaining()) < 10)
				{
					//set firepower to change depending on how close the enemy is
					//farther -> lower firepower ; closer -> higher firepower
					setFire(Math.min(400 / enemy.getDistance(),3));	
				}
				break;
			}//end case CLOSE_RANGE
			
			case CHASER:
			{
				
				// calculate firepower based on distance
				double firePower = Math.min(500 / enemy.getDistance(), 3);
				// calculate speed of bullet
				double bulletSpeed = 20 - firePower * 3;
				// distance = rate * time, solved for time
				long time = (long)(enemy.getDistance() / bulletSpeed);
				
				// calculate gun turn to predicted x,y location
				double futureX = enemy.getFutureX(time);
				double futureY = enemy.getFutureY(time);
				double absDeg = absoluteBearing(getX(), getY(), futureX, futureY);
				// turn the gun to the predicted x,y location
				setTurnGunRight(normalizeBearing(absDeg - getGunHeading()));
				
				//chase the enemy by charging straight into it
				targetTurn = enemy.getBearing(); 
				setTurnRight(targetTurn);
				
				//fire only if the gun is not hot and if the gun is relatively close to the enemy 	
				if(getGunHeat() == 0 && Math.abs(getGunTurnRemaining()) < 10)
				{
					//set firepower to change depending on how close the enemy is
					//farther -> lower firepower ; closer -> higher firepower
					setFire(Math.min(400 / enemy.getDistance(),3));	
				}
				break;
			}
			
		
		
		}	
	}

	/**
	 * onHitByBullet: What to do when you're hit by a bullet
	 */
	public void onHitByBullet(HitByBulletEvent e) {
		
	}
	
	public void onHitRobot(HitRobotEvent e){
		if(this.mode == BERSERK){
			
		}
		
	}

	/**
	 * onHitWall: What to do when you hit a wall
	 */
	public void onHitWall(HitWallEvent e) {
		// Replace the next line with any behavior you would like
		
		moveDirection *= -1;
		setAhead(20 * moveDirection);
	}	
	
	public void doMove(){	
		
		if(this.getVelocity() == 0){ 
			//moveDirection *= -1;
		}
		if(this.mode == CLOSE_RANGE){
			strafTime = 30;
		} else if(this.mode == CHASER){
			strafTime = 1000;
		} else if(this.mode == BERSERK){
			strafTime = 10000;
		}

		//move in one direction for 30 ticks and then switch direction (strafing)
		//NOTE : higher strafing time (e.g 60-90)-> better chasing but higher chance to hit walls
		// 		 smaller strafing time (e.g 30-40) -> lower chance to hit walls but has bad chasing
		//		 very small strafing time (0-20) -> prone to ramming and point-blank shots
		if(getTime() % strafTime == 0){
			moveDirection *= -1;
		}
		
		setAhead(100 * moveDirection);
			
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

	//computes the absolute bearing between two points (from http://mark.random-article.com/weber/java/robocode/lesson4.html)
	double absoluteBearing(double x1, double y1, double x2, double y2) {
		double xo = x2-x1;
		double yo = y2-y1;
		double hyp = Point2D.distance(x1, y1, x2, y2);
		double arcSin = Math.toDegrees(Math.asin(xo / hyp));
		double bearing = 0;
	
		if (xo > 0 && yo > 0) { // both pos: lower-Left
			bearing = arcSin;
		} else if (xo < 0 && yo > 0) { // x neg, y pos: lower-right
			bearing = 360 + arcSin; // arcsin is negative here, actuall 360 - ang
		} else if (xo > 0 && yo < 0) { // x pos, y neg: upper-left
			bearing = 180 - arcSin;
		} else if (xo < 0 && yo < 0) { // both neg: upper-right
			bearing = 180 - arcSin; // arcsin is negative here, actually 180 + ang
		}
	
		return bearing;
	}	

	public void onCustomEvent(CustomEvent e){
		//mode shifter events start
		if(e.getCondition().getName().equals("low_health"))
		{
			if(mode != BERSERK)
			{	
				//mode = BERSERK;
				//setBodyColor(Color.red);
				
			}
		}
		
		if(e.getCondition().getName().equals("long_range_enemy"))
		{
			if(mode != CHASER)
			{
				mode = CHASER;
				setBodyColor(Color.blue);
			}
		}
		
		if(e.getCondition().getName().equals("short_range_enemy"))
		{
			if(mode != CLOSE_RANGE)
			{
				mode = CLOSE_RANGE;
				setBodyColor(Color.green);
			}
		}
		//mode shifter events end
	}
}

//idea of enemy bot based on tutorial http://mark.random-article.com/weber/java/robocode/
class EnemyBot
{
	private double bearing;
	private double distance;
	private double energy;
	private double heading;
	private double velocity;
	private String name;
	
	public EnemyBot()
	{
		reset();
	}
	
	public void reset()
	{
		this.bearing = 0.0;
		this.distance = 0.0;
		this.energy = 0.0;
		this.heading = 0.0;
		this.velocity = 0.0;
		this.name = "";
	}
	
	public void update(ScannedRobotEvent e)
	{
		this.bearing = e.getBearing();
		this.distance = e.getDistance();
		this.energy = e.getEnergy();
		this.heading = e.getHeading();
		this.velocity = e.getVelocity();
		this.name = e.getName();
	}
	
	public boolean none()
	{
		if(this.name.equals("")) return true;
		else return false;
	}

	//getter-setter section start
	public double getBearing()
	{
		return this.bearing;
	}
	
	public void setBearing(double newBearing)
	{
		this.bearing = newBearing;
	}
	
	public double getDistance()
	{
		return this.distance;
	}
	
	public void setDistance(double newDistance)
	{
		this.distance = newDistance;
	}
	
	public double getEnergy()
	{
		return this.energy;
	}
	
	public void setEnergy(double newEnergy)
	{
		this.energy = newEnergy;
	}
	
	public double getHeading()
	{
		return this.heading;
	}
	
	public void setHeading(double newHeading)
	{
		this.heading = newHeading;
	}
	
	public double getVelocity()
	{
		return this.velocity;
	}
	
	public void setVelocity(double newVelocity)
	{
		this.velocity = newVelocity;
	}
	
	public String getName()
	{
		return this.name;
	}
	
	public void setName(String newName)
	{
		this.name = newName;
	}
	//getter-setter section end
}

//Advanced EnemyBot class from http://mark.random-article.com/weber/java/ch5/lab4.html
class AdvancedEnemyBot extends EnemyBot
{
	double x;
	double y;
	
	public AdvancedEnemyBot()
	{
		this.reset();
	}

	public void reset()
	{
		super.reset();
		this.x = 0;
		this.y = 0;
	}
	
	public void update(ScannedRobotEvent e, Robot robot)
	{
		super.update(e);
		double absBearingDeg = (robot.getHeading() + e.getBearing());
		if (absBearingDeg < 0) absBearingDeg += 360;
		
		// yes, you use the _sine_ to get the X value because 0 deg is North
		x = robot.getX() + Math.sin(Math.toRadians(absBearingDeg)) * e.getDistance();
		// yes, you use the _cosine_ to get the Y value because 0 deg is North
		y = robot.getY() + Math.cos(Math.toRadians(absBearingDeg)) * e.getDistance();
	} 
	
	public double getFutureX(long when)
	{
		return x + Math.sin(Math.toRadians(getHeading())) * getVelocity() * when;
	}
	
	public double getFutureY(long when)
	{
		return y + Math.cos(Math.toRadians(getHeading())) * getVelocity() * when;
	}
	
	//getter-setter section start
	public double getX()
	{
		return this.x;
	}
	
	public double getY()
	{
		return this.y;
	}
	//getter-setter section end
}

