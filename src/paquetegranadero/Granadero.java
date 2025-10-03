package paquetegranadero;
import robocode.*;


public class Granadero extends JuniorRobot
{
    private final RobotStrategy strategy = new StrategyPredictiveCorners();

	@Override	
	public void run() {
		strategy.run(this);
	}

	/**
	 * onScannedRobot: What to do when you see another robot
	 */
	@Override
	public void onScannedRobot() {
		strategy.onScannedRobot(this);
	}

	/**
	 * onHitByBullet: What to do when you're hit by a bullet
	 */
	@Override
	public void onHitByBullet() {
		strategy.onHitByBullet(this);
	}
	
	/**
	 * onHitWall: What to do when you hit a wall
	 */
	@Override
	public void onHitWall() {
		strategy.onHitWall(this);
	}

    @Override
    public void onHitRobot() {strategy.onHitRobot(this);}
}