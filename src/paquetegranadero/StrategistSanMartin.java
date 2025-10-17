package paquetegranadero;

import robocode.JuniorRobot;

public class StrategistSanMartin implements RobotStrategist {

    private static StrategistSanMartin instance = null;

    private StrategistSanMartin() {}

    @Override
    public RobotStrategy decide(JuniorRobot robot, RobotStrategy currentStrategy) {
        if (robot.others <= 10 && robot.energy < 50) {
            if (currentStrategy instanceof StrategyAggro) {
                return currentStrategy;
            }
            return new StrategyAggro();
        }

        if (currentStrategy instanceof StrategyPredictiveCorners) {
            return currentStrategy;
        }
        return new StrategyPredictiveCorners();
    }

    public static StrategistSanMartin getInstance() {
        if (instance == null) {
          instance = new StrategistSanMartin();
        }
        return instance;
    }

    // Clases anidadas
    private class StrategyAggro implements RobotStrategy {
        private int lastScannedBearing = -1;

        @Override
        public void run(JuniorRobot robot) {
            robot.setColors(JuniorRobot.blue, JuniorRobot.white, JuniorRobot.white, JuniorRobot.yellow, JuniorRobot.white);

            if (robot.heading != robot.gunHeading) {
                robot.turnGunTo(robot.heading);
            }

            if (lastScannedBearing < 0) {
                robot.turnRight(180);
            } else {
                robot.turnLeft(180);
            }
        }

        @Override
        public void onScannedRobot(JuniorRobot robot) {
            // Obtener información del enemigo
            int enemyDistance = robot.scannedDistance;
            int enemyBearing = robot.scannedBearing;
            int enemyHeading = robot.scannedHeading;
            int enemyVelocity = robot.scannedVelocity;

            lastScannedBearing = enemyBearing;

            // Calcular la posición absoluta del enemigo
            double enemyX = robot.robotX + Math.sin(Math.toRadians(robot.heading + enemyBearing)) * enemyDistance;
            double enemyY = robot.robotY + Math.cos(Math.toRadians(robot.heading + enemyBearing)) * enemyDistance;

            // Hacer la predicción del movimiento
            double[] targetPosition = predictEnemyPosition(enemyX, enemyY, enemyHeading, enemyVelocity, enemyDistance, robot);

            // Calcular el ángulo hacia la posición predicha
            double targetAngle = Math.toDegrees(Math.atan2(
                    targetPosition[0] - robot.robotX,
                    targetPosition[1] - robot.robotY
            ));

            double turnAngle = targetAngle - robot.gunHeading;

            // Normalizar el ángulo (-180 a 180)
            while (turnAngle > 180) turnAngle -= 360;
            while (turnAngle < -180) turnAngle += 360;

            robot.turnRight((int) turnAngle);

            if (enemyDistance < 100 && enemyVelocity < 3) {
                double firePower = computeFirePower(enemyDistance, enemyVelocity, robot.fieldWidth, robot.fieldHeight);
                robot.fire(firePower);
            }
            robot.ahead(Math.min(enemyDistance, robot.fieldWidth / 5));
        }

        private double[] predictEnemyPosition(double enemyX, double enemyY, double enemyHeading,
                                              double enemyVelocity, double distance, JuniorRobot robot) {
            double moveVelocity = 20 - (2 * 3);

            // Tiempo que tardará la bala en llegar
            double timeToTarget = distance / moveVelocity;

            // Calcular la posición futura del enemigo
            double futureX = enemyX + Math.sin(Math.toRadians(enemyHeading)) * enemyVelocity * timeToTarget;
            double futureY = enemyY + Math.cos(Math.toRadians(enemyHeading)) * enemyVelocity * timeToTarget;

            // Verificar límites del campo de batalla
            futureX = Math.max(18, Math.min(robot.fieldWidth - 18, futureX));
            futureY = Math.max(18, Math.min(robot.fieldHeight - 18, futureY));

            return new double[]{futureX, futureY};
        }

        @Override
        public void onHitByBullet(JuniorRobot robot) {
            if (Math.abs(robot.hitByBulletBearing) < 5) {
                robot.fire(3);
            } else {
                robot.ahead(30);
            }
        }

        @Override
        public void onHitWall(JuniorRobot robot) {
            robot.turnTo((robot.hitWallAngle + 180) % 360);
            robot.ahead(robot.fieldHeight / 3);
        }

        @Override
        public void onHitRobot(JuniorRobot robot) {
            robot.turnTo(robot.hitRobotAngle);
            robot.fire(3);
            robot.ahead(10);
        }

        // En tu robot:
        private static double computeFirePower(int scannedDistance, int scannedVelocity, double fieldWidth, double fieldHeight) {
            final double MIN_POWER = 0.1;
            final double MAX_POWER = 3.0;

            double maxDist = Math.hypot(fieldWidth, fieldHeight);
            double norm = Math.min(Math.max(scannedDistance, 0.0) / Math.max(maxDist, 1.0), 1.0);

            double power = MAX_POWER - (MAX_POWER - MIN_POWER) * norm;
            double calculatedPower = Math.max(MIN_POWER, Math.min(MAX_POWER, power));

            if (scannedDistance < 50 && scannedVelocity == 0) {
                calculatedPower = 3;
            }
            return calculatedPower;
        }
    }

public class StrategyPredictiveCorners implements RobotStrategy {
    private int cycles = 0;

    public void run(JuniorRobot robot) {

        robot.setColors(JuniorRobot.blue, JuniorRobot.white, JuniorRobot.white, JuniorRobot.yellow, JuniorRobot.white);
        if (!this.inCorner(robot)) {
            robot.ahead(50);
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
            // Obtener información del enemigo
            double enemyDistance = robot.scannedDistance;
            double enemyBearing = robot.scannedBearing;
            double enemyHeading = robot.scannedHeading;
            double enemyVelocity = robot.scannedVelocity;

            // Calcular la posición absoluta del enemigo
            double enemyX = robot.robotX + Math.sin(Math.toRadians(robot.heading + enemyBearing)) * enemyDistance;
            double enemyY = robot.robotY + Math.cos(Math.toRadians(robot.heading + enemyBearing)) * enemyDistance;

            // Hacer la predicción del movimiento
            double[] targetPosition = predictEnemyPosition(enemyX, enemyY, enemyHeading, enemyVelocity, enemyDistance, robot);

            // Calcular el ángulo hacia la posición predicha
            double targetAngle = Math.toDegrees(Math.atan2(
                    targetPosition[0] - robot.robotX,
                    targetPosition[1] - robot.robotY
            ));

            // Girar el cañón hacia el objetivo predicho
            double gunTurnAngle = targetAngle - robot.gunHeading;

            // Normalizar el ángulo (-180 a 180)
            while (gunTurnAngle > 180) gunTurnAngle -= 360;
            while (gunTurnAngle < -180) gunTurnAngle += 360;

            // Girar y disparar
            robot.turnGunRight((int)gunTurnAngle);
            robot.fire(this.calculateFirePower(robot)); // Potencia de disparo (1-3 para JuniorRobot)
    }

    /**
     * Predice la posición futura del enemigo basándose en su movimiento lineal
     */
    private double[] predictEnemyPosition(double enemyX, double enemyY, double enemyHeading,
                                          double enemyVelocity, double distance, JuniorRobot robot) {

        // Velocidad de la bala (depende de la potencia de disparo)
        double bulletVelocity = 20 - (2 * 3); // Para potencia 2: 20 - 6 = 14

        // Tiempo que tardará la bala en llegar
        double timeToTarget = distance / bulletVelocity;

        // Calcular la posición futura del enemigo
        double futureX = enemyX + Math.sin(Math.toRadians(enemyHeading)) * enemyVelocity * timeToTarget;
        double futureY = enemyY + Math.cos(Math.toRadians(enemyHeading)) * enemyVelocity * timeToTarget;

        // Verificar límites del campo de batalla
        futureX = Math.max(18, Math.min(robot.fieldWidth - 18, futureX));
        futureY = Math.max(18, Math.min(robot.fieldHeight - 18, futureY));

        return new double[]{futureX, futureY};
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

}
