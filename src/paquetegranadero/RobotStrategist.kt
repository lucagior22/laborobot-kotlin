package paquetegranadero;

import robocode.JuniorRobot;

interface RobotStrategist {
    fun decide(robot : JuniorRobot, currentStrategy: RobotStrategy)
}