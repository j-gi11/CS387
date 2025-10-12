/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controllers;

import java.util.Enumeration;

import Vector.Vector;
import engine.Car;
import engine.Game;
import engine.GameObject;
import engine.RotatedRectangle;

/**
 *
 * @author santi
 */
public class ObstacleAvoidanceController extends Controller {
    private GameObject target;

    private final double SAFE_DISTANCE = 80.0;
    
    private final double FORTY_FIVE_DEGREES = Math.PI / 4;

    private final double SLOWING_RADIUS = 300.0;
    private final double ARRIVAL_RADIUS = 15.0;
    
    private enum Collision {
        NONE, FRONT, LEFT, RIGHT
    }

    public ObstacleAvoidanceController(GameObject target) {
        this.target = target;
    }

    private boolean checkCollision(Car subject, double angle, Game game, int distance) {
        double carWidth = subject.getImg().getWidth() / 2;
        double carHeight = subject.getImg().getHeight() / 2;

        double x = subject.getX() + (Math.cos(angle) * distance);
        double y = subject.getY() + (Math.sin(angle) * distance);

        RotatedRectangle ray = new RotatedRectangle(x, y, carWidth, carHeight, angle);

        GameObject collidedObject = game.collision(ray);
        if(collidedObject != null && !collidedObject.equals(target) && !collidedObject.equals(subject)) {
            return true;
        }

        return false;
    }

    private Collision rayCast(Car subject, Game game, double distanceToTarget) {
        double lookAheadDistance = Math.min(SAFE_DISTANCE, distanceToTarget);

        for(int i = 1; i < lookAheadDistance; i++) {
            boolean leftCollision = checkCollision(subject, (subject.getAngle() - FORTY_FIVE_DEGREES), game, i);
            if (leftCollision) return Collision.LEFT;

            boolean rightCollision = checkCollision(subject, (subject.getAngle() + FORTY_FIVE_DEGREES), game, i);
            if (rightCollision) return Collision.RIGHT;

            boolean frontCollision = checkCollision(subject, subject.getAngle(), game, i);
            if (frontCollision) return Collision.FRONT;
        }

        return Collision.NONE;
    }



    public void update(Car subject, Game game, double delta_t, double[] controlVariables) {
        Vector targetPos = new Vector(target.getX(), target.getY());
        Vector currentPos = new Vector(subject.getX(), subject.getY());
        Vector vectorToTarget = targetPos.subtract(currentPos);

        double distance = vectorToTarget.magnitude();
        // System.out.println("Distance to target: " + distance);

        if (distance <= ARRIVAL_RADIUS) {
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
        
        double finalSteering, finalThrottle, finalBrake;
        
        if (subject.getSpeed() > targetSpeed) {
            finalThrottle = 0;
            finalBrake = 1;
        } else {
            finalThrottle = .5;
            finalBrake = 0;
        }
        
        Vector carHeading = new Vector(Math.cos(subject.getAngle()), Math.sin(subject.getAngle()));
        Vector perpendicularToHeading = new Vector(carHeading.y, -carHeading.x);
        double sideDotProduct = desiredDirection.dot(perpendicularToHeading);
        
        finalSteering = -sideDotProduct;

        Collision collision = rayCast(subject, game, distance);
        // System.out.println(collision.toString());

        switch (collision) {
            case FRONT:
                if (sideDotProduct > 0) {
                    finalSteering = 1;
                } else {
                    finalSteering = -1;
                }
                finalThrottle = .2;
                break;
            case LEFT:
                finalSteering = 1;
                finalThrottle = 0.2;
                break;
            case RIGHT:
                finalSteering = -1;
                finalThrottle = 0.2;
                break;
            case NONE:
                break;
        }
        controlVariables[VARIABLE_STEERING] = finalSteering;
        controlVariables[VARIABLE_THROTTLE] = finalThrottle;
        controlVariables[VARIABLE_BRAKE] = finalBrake;
    }
    
}
