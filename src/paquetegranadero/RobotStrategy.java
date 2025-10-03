package paquetegranadero;

import robocode.JuniorRobot;

public interface RobotStrategy {


    void run(JuniorRobot robot);
    /**
     * onScannedRobot: What to do when you see another robot
     */
    void onScannedRobot(JuniorRobot robot);

    /**
     * onHitByBullet: What to do when you're hit by a bullet
     */
    void onHitByBullet(JuniorRobot robot);

    /**
     * onHitWall: What to do when you hit a wall
     */
    void onHitWall(JuniorRobot robot);

    void onHitRobot(JuniorRobot robot);
}
