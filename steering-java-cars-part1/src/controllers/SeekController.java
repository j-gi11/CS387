package controllers;

import Vector.Vector;
import engine.Car;
import engine.Game;
import engine.GameObject;

public class SeekController extends Controller {
    private GameObject target;

    private static final double ARRIVAL_RADIUS = 10.0;

    public SeekController(GameObject target) {
        this.target = target;
    }

    @Override
    public void update(Car subject, Game game, double delta_t, double[] controlVariables) {
        Vector targetPos = new Vector(target.getX(), target.getY());
        Vector currentPos = new Vector(subject.getX(), subject.getY());
        Vector vectorToTarget = targetPos.subtract(currentPos);
        double distance = vectorToTarget.magnitude();

        if (distance <= ARRIVAL_RADIUS) { // prevent over shooting and running into walls
            controlVariables[VARIABLE_STEERING] = 0;
            controlVariables[VARIABLE_THROTTLE] = 0;
            controlVariables[VARIABLE_BRAKE] = 1;
            return;
        }

        Vector desiredDirection = vectorToTarget.normalize();
        Vector carHeading = new Vector(Math.cos(subject.getAngle()), Math.sin(subject.getAngle()));

        Vector perpendicularVector = new Vector(carHeading.y, -carHeading.x);
        double sideDotProduct = perpendicularVector.dot(desiredDirection);

        // output filtering 
        if (sideDotProduct > 0) {
            controlVariables[VARIABLE_STEERING] = -1;
        } else if (sideDotProduct < 0) {
            controlVariables[VARIABLE_STEERING] = 1;
        } else {
            controlVariables[VARIABLE_STEERING] = 0;
        }

        double theta = subject.getAngle() - Math.atan2(carHeading.y, carHeading.x);
        double throttleDotProduct = carHeading.magnitude() * desiredDirection.magnitude() * Math.cos(theta);

        if (throttleDotProduct < 0) {
            controlVariables[VARIABLE_THROTTLE] = 0;
            controlVariables[VARIABLE_BRAKE] = 1;
        } else {
            controlVariables[VARIABLE_THROTTLE] = 1;
            controlVariables[VARIABLE_BRAKE] = 0;
        }
    }
}