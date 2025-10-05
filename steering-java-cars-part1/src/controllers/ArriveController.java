package controllers;

import Vector.Vector;
import engine.Car;
import engine.Game;
import engine.GameObject;

public class ArriveController extends Controller {
    private GameObject target;

    private static final double SLOWING_RADIUS = 150.0;
    private static final double ARRIVAL_RADIUS = 2.0;
    
    private static final double MAX_ACCELERATION = 5;

    public ArriveController(GameObject target) {
        this.target = target;
    }

    public void update(Car subject, Game game, double delta_t, double[] controlVariables) {
        Vector targetPos = new Vector(target.getX(), target.getY());
        Vector currentPos = new Vector(subject.getX(), subject.getY());
        Vector vectorToTarget = targetPos.subtract(currentPos);
        
        double distance = vectorToTarget.magnitude();

        if (distance < ARRIVAL_RADIUS) {
            controlVariables[VARIABLE_STEERING] = 0;
            controlVariables[VARIABLE_THROTTLE] = 0;
            controlVariables[VARIABLE_BRAKE] = 0;
            return;
        }

        double targetSpeed;
        if (distance > SLOWING_RADIUS) {
            targetSpeed = subject.getMaxSpeed();
        } else {
            targetSpeed = subject.getMaxSpeed() * (distance / SLOWING_RADIUS);
        }

        Vector desiredDirection = vectorToTarget.normalize();
        Vector targetVelocity = desiredDirection.multiply(targetSpeed);

        Vector currentVelocity = new Vector(Math.cos(subject.getAngle()), Math.sin(subject.getAngle()));
        currentVelocity = currentVelocity.normalize().multiply(subject.getSpeed());
        Vector desiredAcceleration = targetVelocity.subtract(currentVelocity).multiply(1/delta_t);

        if (desiredAcceleration.magnitude() > MAX_ACCELERATION) {
            desiredAcceleration = desiredAcceleration.normalize().multiply(MAX_ACCELERATION);
        }

        
        // Output filtering
        Vector carHeading = new Vector(Math.cos(subject.getAngle()), Math.sin(subject.getAngle()));
        
        double alignment = carHeading.dot(vectorToTarget.normalize());

        boolean isReversing = alignment < 0;

        if (isReversing) {
            controlVariables[VARIABLE_THROTTLE] = 0;
            controlVariables[VARIABLE_BRAKE] = 1.0;
        } else {
            if (subject.getSpeed() > targetSpeed) {
                controlVariables[VARIABLE_THROTTLE] = 0;
                controlVariables[VARIABLE_BRAKE] = 1.0;
            } else {
                controlVariables[VARIABLE_THROTTLE] = 1.0;
                controlVariables[VARIABLE_BRAKE] = 0;
            }
        }

        Vector perpendicularVector = new Vector(carHeading.y, -carHeading.x);
        double sideDotProduct = desiredAcceleration.dot(perpendicularVector);
        
        if (isReversing) {
            if (sideDotProduct > 0) {
                controlVariables[VARIABLE_STEERING] = 1;
            } else {
                controlVariables[VARIABLE_STEERING] = -1;
            }
        } else {
            if (sideDotProduct > 0) {
                controlVariables[VARIABLE_STEERING] = -1;
            } else {
                controlVariables[VARIABLE_STEERING] = 1;
            }
        }
    }
}