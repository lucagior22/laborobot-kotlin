package paquetegranadero;

import robocode.JuniorRobot;

public class StrategistRoca implements RobotStrategist {
    private static StrategistRoca instance = null;

    private StrategistRoca() {}

    @Override
    public RobotStrategy decide(JuniorRobot robot, RobotStrategy currentStrategy) {
        if (robot.others >= 3) {
            if (currentStrategy instanceof StrategyWallHugger) {
                return currentStrategy;
            }
            return new StrategyWallHugger();
        }
        if (currentStrategy instanceof StrategyCentralControl) {
            return currentStrategy;
        }
        return new StrategyCentralControl();
    }

    public static StrategistRoca getInstance() {
        if (instance == null) {
            instance = new StrategistRoca();
        }
        return instance;
    }

    private class StrategyWallHugger implements RobotStrategy {
        private boolean clockwise = true;

        @Override
        public void run(JuniorRobot robot) {
            robot.setColors(JuniorRobot.red, JuniorRobot.black, JuniorRobot.black, JuniorRobot.orange, JuniorRobot.black);

            // Moverse siguiendo las paredes del campo
            if (nearWall(robot)) {
                if (clockwise) {
                    robot.turnRight(90);
                } else {
                    robot.turnLeft(90);
                }
            }
            robot.ahead(40);

            robot.turnGunRight(10);
        }

        @Override
        public void onScannedRobot(JuniorRobot robot) {
            double firepower = calculateWallHuggerFirePower(robot);

            robot.turnGunTo(robot.scannedAngle);
            robot.fire(firepower);

            if (robot.scannedDistance < 80) {
                clockwise = !clockwise;
            }
        }

        @Override
        public void onHitWall(JuniorRobot robot) {
            if (clockwise) {
                robot.turnRight(90);
            } else {
                robot.turnLeft(90);
            }
            robot.ahead(30);
        }

        @Override
        public void onHitByBullet(JuniorRobot robot) {
            clockwise = !clockwise;
            robot.turnGunTo(robot.hitByBulletAngle);
            robot.fire(2);
        }

        @Override
        public void onHitRobot(JuniorRobot robot) {
            robot.turnGunTo(robot.hitRobotAngle);
            robot.fire(3);
            robot.back(20);
        }

        private boolean nearWall(JuniorRobot robot) {
            double margin = 80;
            return robot.robotX < margin || robot.robotY < margin ||
                    robot.robotX > robot.fieldWidth - margin ||
                    robot.robotY > robot.fieldHeight - margin;
        }

        private double calculateWallHuggerFirePower(JuniorRobot robot) {
            double firepower = 1.5;

            if (robot.scannedDistance < 100) firepower += 1.0;

            if (robot.scannedVelocity > 4) firepower -= 0.5;

            if (robot.energy > 60) firepower += 0.5;

            return Math.min(3, Math.max(0.1, firepower));
        }
    }

    private class StrategyCentralControl implements RobotStrategy {
        private int scanDirection = 1;
        private boolean inCenter = false;

        @Override
        public void run(JuniorRobot robot) {
            robot.setColors(JuniorRobot.green, JuniorRobot.white, JuniorRobot.white, JuniorRobot.red, JuniorRobot.white);

            if (!inCenter) {
                double centerX = robot.fieldWidth / 2;
                double centerY = robot.fieldHeight / 2;

                double angleToCenter = Math.toDegrees(Math.atan2(
                        centerX - robot.robotX,
                        centerY - robot.robotY
                ));

                robot.turnTo((int)angleToCenter);

                double distanceToCenter = Math.hypot(
                        centerX - robot.robotX,
                        centerY - robot.robotY
                );

                if (distanceToCenter > 50) {
                    robot.ahead(30);
                } else {
                    inCenter = true;
                }
            } else {
                robot.turnRight(45);
                robot.ahead(20);
            }

            robot.turnGunRight(30 * scanDirection);
        }

        @Override
        public void onScannedRobot(JuniorRobot robot) {
            double firepower = calculateCentralFirePower(robot);

            robot.turnGunTo(robot.scannedAngle);
            robot.fire(firepower);

            if (Math.random() < 0.3) {
                scanDirection *= -1;
            }
        }

        @Override
        public void onHitByBullet(JuniorRobot robot) {
            // Moverse un poco cuando nos golpeen
            robot.turnRight(90);
            robot.ahead(30);

            // Disparar de vuelta
            robot.turnGunTo(robot.hitByBulletAngle);
            robot.fire(2);
        }

        @Override
        public void onHitWall(JuniorRobot robot) {
            // Esto no debería pasar mucho si estamos en el centro
            inCenter = false;
            robot.turnRight(180);
            robot.ahead(50);
        }

        @Override
        public void onHitRobot(JuniorRobot robot) {
            robot.turnGunTo(robot.hitRobotAngle);
            robot.fire(3);
            robot.back(30);
        }

        private double calculateCentralFirePower(JuniorRobot robot) {
            double firepower = 2.0; // Base más alta desde posición central

            // Ajustar por distancia (más potencia a distancia media)
            if (robot.scannedDistance > 200 && robot.scannedDistance < 400) {
                firepower += 0.5;
            }

            // Menos potencia si el enemigo se mueve rápido
            if (robot.scannedVelocity > 5) firepower -= 1.0;
            else if (robot.scannedVelocity == 0) firepower += 0.5;

            // Ajustar por energía propia
            if (robot.energy < 30) firepower -= 0.5;

            return Math.min(3, Math.max(0.1, firepower));
        }
    }
}