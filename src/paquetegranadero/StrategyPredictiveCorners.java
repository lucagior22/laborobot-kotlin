package paquetegranadero;
import robocode.*;

public class StrategyPredictiveCorners implements RobotStrategy {
    private int cycles = 0;

    public void run(JuniorRobot robot) {

        robot.setColors(JuniorRobot.blue, JuniorRobot.white, JuniorRobot.white, JuniorRobot.yellow, JuniorRobot.white);
        if (!this.inCorner(robot)) {
            robot.ahead(30);
        }
        if (cycles >= 10) {
            robot.turnGunRight(360 - (robot.others * 5));
        }

        cycles++;
    }

    private boolean inCorner(JuniorRobot robot) {

        double margin = 100;

        boolean esquinaAbajoIzq = (robot.robotX <= margin && robot.robotY <= margin);
        boolean esquinaAbajoDer = (robot.robotX >= robot.fieldWidth - margin && robot.robotY <= margin);
        boolean esquinaArribaIzq = (robot.robotX <= margin && robot.robotY >= robot.fieldHeight - margin);
        boolean esquinaArribaDer = (robot.robotX >= robot.fieldWidth - margin && robot.robotY >= robot.fieldHeight - margin);

        return esquinaAbajoIzq || esquinaAbajoDer || esquinaArribaIzq || esquinaArribaDer;
    }

    private double calculateFirePower(JuniorRobot robot) {
        double auxFirePower = 1;

        if (robot.energy > 80) {
            auxFirePower += 1;
        } else if (robot.energy > 50) {
            auxFirePower += 0.5;
        } else {
            auxFirePower -= 0.7;
        }

        if (robot.scannedVelocity > 5) {
            auxFirePower -= 1.5;
        } else if (robot.scannedVelocity == 0) {
            auxFirePower += 1;
        }

        if (robot.scannedDistance > 400) {
            auxFirePower -= 1.5;
        } else if (robot.scannedDistance > 200) {
            auxFirePower -= 0.5;
        } else if (robot.scannedDistance < 20 && Math.abs(robot.scannedBearing) < 5) {
            auxFirePower += 2;
        }

        auxFirePower += robot.others * 0.3;

        return Math.min(3, Math.max(0.1, auxFirePower));
    }

    public void onScannedRobot(JuniorRobot robot) {

        if (this.cycles >= 3) {
            this.cycles = 0;
            robot.turnGunTo(robot.scannedAngle);
            robot.fire(this.calculateFirePower(robot));
        }

    }

    public void onHitByBullet(JuniorRobot robot) {

        if (this.inCorner(robot)) {
            robot.ahead(100);
        }
        if (Math.abs(robot.hitByBulletBearing) < 90) {
            robot.turnGunTo(robot.hitByBulletAngle);
        }
    }

    public void onHitWall(JuniorRobot robot) {
        int turnToAngle;

        if (robot.hitWallBearing > 0) {
            turnToAngle = (robot.hitWallAngle + 90) % 360;
        } else {
            turnToAngle = (robot.hitWallAngle - 90) % 360;
        }

        robot.turnTo(turnToAngle);
        robot.turnGunTo((robot.hitWallAngle + 180) % 360);

        robot.ahead(100);
    }


    public void onHitRobot(JuniorRobot robot) {
        robot.turnGunTo(robot.hitRobotAngle);
        robot.fire(3);
    }
}
