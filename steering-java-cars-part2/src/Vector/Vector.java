package Vector;

public class Vector {
    public double x;
    public double y;
    
    public Vector(double x, double y) {
        this.x = x;
        this.y = y;
    }
    
    public Vector add(Vector v) {
        return new Vector(this.x + v.x, this.y + v.y);
    }
    
    public Vector subtract(Vector v) {
        return new Vector(this.x - v.x, this.y - v.y);
    }
    
    public Vector multiply(double scalar) {
        return new Vector(this.x * scalar, this.y * scalar);
    }
    
    public double dot(Vector v) {
        return this.x * v.x + this.y * v.y;
    }
    
    public double magnitude() {
        return Math.sqrt(this.x * this.x + this.y * this.y);
    }
    
    public Vector normalize() {
        double mag = magnitude();
        if (mag == 0) {
            return new Vector(0, 0);
        }
        return new Vector(this.x / mag, this.y / mag);
    }
}