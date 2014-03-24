package u1l;
import robocode.*;
import java.awt.Color;
import static robocode.util.Utils.normalRelativeAngleDegrees;

// API help : http://robocode.sourceforge.net/docs/robocode/robocode/Robot.html

/**
 * ProtoSpencerTronV2 - a robot by (your name here)
 */
public class ProtoSpencerTronV2 extends AdvancedRobot
{
	private EnemyBot enemy = new EnemyBot();
	private byte moveDirection = 1;
	private int wallMargin = 60;
	private int targetOffset = 9;
	private double targetTurn;

	/**
	 * run: ProtoSpencerTron's default behavior
	 */
	public void run() {
		
		setBodyColor(Color.green);
		setGunColor(Color.yellow);
		setRadarColor(Color.white);
		setBulletColor(Color.cyan);
		setScanColor(Color.cyan);		
		
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
		
		
		//find an enemy
		if(enemy.none() || 
			//track a closer enemy
			e.getDistance() < enemy.getDistance() ||
			//continue tracking the current enemy
			e.getName().equals(enemy.getName())
			){
			enemy.update(e);
		}
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
			//set firepower to change depending on how close the enemy is
			//farther -> lower firepower ; closer -> higher firepower
			setFire(Math.min(400 / enemy.getDistance(),3));

	}

	/**
	 * onHitByBullet: What to do when you're hit by a bullet
	 */
	public void onHitByBullet(HitByBulletEvent e) {
		
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
		//move in one direction for 30 ticks and then switch direction (strafing)
		//NOTE : higher strafing time (e.g 60-90)-> better chasing but higher chance to hit walls
		// 		 smaller strafing time (e.g 30-40) -> lower chance to hit walls but has bad chasing
		//		 very small strafing time (0-20) -> prone to ramming and point-blank shots
		if(getTime() % 90 == 0){
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
	
	public void onCustomEvent(CustomEvent e){
		
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
