package paquetegranadero;
import robocode.JuniorRobot;

public interface RobotStrategist {
    public abstract RobotStrategy decide(JuniorRobot robot, RobotStrategy currentStrategy);
}
