package s3.ai;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.*;

import s3.base.S3;
import s3.base.S3Map;
import s3.entities.S3PhysicalEntity;
import s3.util.Pair;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.Objects; 

public class AStar {
    
    private class Node implements Comparable<Node> {
        int x, y;
        double gScore; 
        double hScore; 
        double fScore; 
        Node parent;

        public Node(int x, int y) {
            this.x = x;
            this.y = y;
            this.gScore = Double.POSITIVE_INFINITY;
            this.hScore = 0;
            this.fScore = Double.POSITIVE_INFINITY;
            this.parent = null;
        }

        @Override
        public int compareTo(Node other) {
            return Double.compare(this.fScore, other.fScore);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            Node node = (Node) obj;
            return x == node.x && y == node.y; 
        }

        @Override
        public int hashCode() {
            return Objects.hash(x, y); 
        }
    }

    private int start_x;
    private int start_y;
    private int goal_x;
    private int goal_y;
    private S3PhysicalEntity entity;
    private S3 game;    
    
    public static int pathDistance(double start_x, double start_y, double goal_x, double goal_y,
            S3PhysicalEntity i_entity, S3 the_game) {
        AStar a = new AStar(start_x,start_y,goal_x,goal_y,i_entity,the_game);
        List<Pair<Double, Double>> path = a.computePath();
        if (path!=null) return path.size();
        return -1;
    }

    public AStar(double start_x, double start_y, double goal_x, double goal_y,
            S3PhysicalEntity i_entity, S3 the_game) {
        this.start_x = (int) start_x;
        this.start_y = (int) start_y;
        this.goal_x = (int) goal_x;
        this.goal_y = (int) goal_y;
        this.entity = i_entity;
        this.game = the_game;
    }

    public List<Pair<Double, Double>> computePath() {
        if (!isWithinMapBounds(start_x, start_y) || !isWithinMapBounds(goal_x, goal_y)) {
            return null;
        }
        
        PriorityQueue<Node> openList = new PriorityQueue<>();
        Set<Node> closedList = new HashSet<>();
        Map<Node, Double> gScoreMap = new HashMap<>();
        
        Node startNode = new Node(this.start_x, this.start_y);
        startNode.hScore = heuristic(startNode);
        startNode.fScore = startNode.hScore;
        
        openList.add(startNode);
        gScoreMap.put(startNode, 0.0);

        while(!openList.isEmpty()) {
            Node current = openList.poll();

            if (current.x == goal_x && current.y == goal_y) {
                return reconstructPath(current);
            }
            
            if (closedList.contains(current)) {
                continue;
            }

            closedList.add(current);

            List<Node> neighbors = generateValidNeighbors(current);

            for(Node neighbor: neighbors) {
                if(closedList.contains(neighbor)) {
                    continue;
                }

                double current_gScore = gScoreMap.getOrDefault(current, Double.POSITIVE_INFINITY);
                double tentative_gScore = current_gScore + 1.0;
                double neighbor_gScore = gScoreMap.getOrDefault(neighbor, Double.POSITIVE_INFINITY);

                if (tentative_gScore < neighbor_gScore) {
                    neighbor.parent = current;
                    neighbor.hScore = heuristic(neighbor);
                    neighbor.fScore = tentative_gScore + neighbor.hScore;
                    
                    gScoreMap.put(neighbor, tentative_gScore);
                    openList.add(neighbor);
                    
                }
            }
        }

        return null;
    }

    private boolean isWithinMapBounds(int x, int y) {
        S3Map map = game.getMap();
        return x >= 0 && y >= 0 && x < map.getWidth() && y < map.getHeight();
    }


    private List<Node> generateValidNeighbors(Node current) {
        List<Node> neighbors = new ArrayList<>();
        
        int[][] directions = {{1,0}, {-1,0}, {0,1}, {0,-1}};
        
        for (int[] dir : directions) {
            int newX = current.x + dir[0];
            int newY = current.y + dir[1];
            
            if (!isWithinMapBounds(newX, newY)) {
                continue;
            }
            
            Node neighbor = new Node(newX, newY);
            
            if (isNodeValid(neighbor)) {
                neighbors.add(neighbor);
            } 
        }
        
        return neighbors;
    }

    private boolean isNodeValid(Node node) {
        if (!isWithinMapBounds(node.x, node.y)) {
            return false;
        }
        
        int originalX = entity.getX();
        int originalY = entity.getY();

        entity.setX(node.x);
        entity.setY(node.y);

        boolean hasCollision = (game.anyLevelCollision(entity) != null);

        entity.setX(originalX);
        entity.setY(originalY);

        return !hasCollision;
    }

    private List<Pair<Double, Double>> reconstructPath(Node current) {
        List<Pair<Double, Double>> totalPath = new java.util.ArrayList<>();
        
        while(current.parent != null) {
            totalPath.add(0, new Pair<>((double)current.x, (double)current.y));
            current = current.parent;
        }
        
        return totalPath;
    }

    private double heuristic(Node node) {
        return Math.abs(goal_x - node.x) + Math.abs(goal_y - node.y);
    }
}