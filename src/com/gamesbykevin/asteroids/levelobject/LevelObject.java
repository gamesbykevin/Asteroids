package com.gamesbykevin.asteroids.levelobject;

import com.gamesbykevin.framework.base.Sprite;

import com.gamesbykevin.asteroids.engine.Engine;

import java.awt.Graphics;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

public abstract class LevelObject extends Sprite
{
    //the angle the object should be facing in radians NOT degrees
    private double angle = 0;
    
    //the size of the object
    private final int size;
    
    //store the original coordinates
    private List<int[]> xpoints;
    private List<int[]> ypoints;
    
    //the polygon(s) that make up the object, store the new coordinates where the object will be drawn
    private List<Polygon> boundary;
    
    //is this object dead
    private boolean dead = false;
    
    public LevelObject(final int size)
    {
        //set the size of the object
        this.size = size;
        
        //create the lists that will contain the boundary and original coordinates
        this.xpoints = new ArrayList<>();
        this.ypoints = new ArrayList<>();
        this.boundary = new ArrayList<>();
    }
    
    /**
     * Mark this object as dead
     */
    public void markDead()
    {
        this.dead = true;
    }
    
    /**
     * Un-flag this object
     */
    protected void unmarkDead()
    {
        this.dead = false;
    }
    
    /**
     * Is our object dead
     * @return True if dead, false otherwise
     */
    public boolean isDead()
    {
        return this.dead;
    }
    
    public List<Polygon> getBoundaries()
    {
        return this.boundary;
    }
    
    /**
     * Get the angle this object is supposed to face
     * @return The angle in radians, not degrees
     */
    public double getAngle()
    {
        return this.angle;
    }
    
    /**
     * Set the angle the object should be facing.
     * @param angle The angle in Radians.
     */
    protected void setAngle(final double angle)
    {
        this.angle = angle;
    }
    
    /**
     * Add polygon to level object
     * @param p Polygon object we want to add
     */
    protected void add(final int[] xpoints, final int[] ypoints)
    {
        //add our original coordinates
        this.xpoints.add(xpoints);
        this.ypoints.add(ypoints);
        
        //create our new default polygon
        boundary.add(new Polygon(xpoints, ypoints, xpoints.length));
        
        //call this here so the intial coordinates are set
        updateCoordinates(null);
    }
    
    @Override
    public void dispose()
    {
        super.dispose();
        
        xpoints.clear();
        xpoints = null;
        
        ypoints.clear();
        ypoints = null;
        
        boundary.clear();
        boundary = null;
    }
    
    /**
     * Get the size of the object
     * @return The size in pixels
     */
    public int getSize()
    {
        return this.size;
    }
    
    /**
     * This method will perform 3 tasks<br>
     * 1. Update the location based on the velocity set.<br>
     * 2. Calculate the new position based on the facing angle.<br>
     * 3. Make sure the object is within the specified boundary.<br>
     * @param screen The area deemed inbounds
     */
    protected void updateCoordinates(final Rectangle screen)
    {
        //update x,y based on velocity
        super.update();
        
        //make sure we are within the boundary of the screen
        checkBounds(screen);
        
        //this is to keep the angle within range
        if (getAngle() > (2 * Math.PI))
            setAngle(getAngle() - (2 * Math.PI));
        if (getAngle() < 0)
            setAngle(getAngle() + (2 * Math.PI));
        
        //rotate every polygon
        for (int index=0; index < boundary.size(); index++)
        {
            //calculate every point in the polygon
            for (int i=0; i < boundary.get(index).xpoints.length; i++)
            {
                final int tmpX = xpoints.get(index)[i];
                final int tmpY = ypoints.get(index)[i];
                
                //take original (x,y) and determine new (x,y) based on the current angle
                final double newX = (tmpX * Math.cos(getAngle())) - (tmpY * Math.sin(getAngle()));
                final double newY = (tmpX * Math.sin(getAngle())) + (tmpY * Math.cos(getAngle()));
                
                boundary.get(index).xpoints[i] = (int)(getX() + newX);
                boundary.get(index).ypoints[i] = (int)(getY() + newY);
            }
            
            //call this to reset any cached data
            boundary.get(index).invalidate();
        }
    }
    
    /**
     * Do the polygons collide
     * @param p1 polygon
     * @param p2 polygon
     * @return true if any of p1 (x,y) points are contained within p2, false otherwise
     */
    protected boolean hasCollision(final Polygon p1, final Polygon p2)
    {
        for (int i=0; i < p1.xpoints.length; i++)
        {
            final int x = p1.xpoints[i];
            final int y = p1.ypoints[i];
            
            if (p2.contains(x, y))
                return true;
        }
        
        return false;
    }
    
    /**
     * Does our level object intersect with the given parameter
     * @param object The object we want to check for collision
     * @return true if collision has been detected, false otherwise
     */
    public boolean hasCollision(final LevelObject object)
    {
        for (Polygon p1 : getBoundaries())
        {
            for (Polygon p2 : object.getBoundaries())
            {
                if (hasCollision(p1, p2))
                    return true;
                
                if (hasCollision(p2, p1))
                    return true;
            }
        }

        //no collision was made
        return false;
    }
    
    public abstract void update(final Engine engine);
    
    /**
     * Make sure the object stays within the boundary<br>
     * If the parameter is null it will not check the bounds and it will not throw exception.
     * @param screen The area in bounds. Can be NULL.
     */
    private void checkBounds(final Rectangle screen)
    {
        //if null don't check.
        if (screen == null)
            return;
        
        //check if we are out of bounds
        if (getX() - getSize() < screen.x && getVelocityX() < 0)
            setX(screen.x + screen.width);
        if (getX() + getSize() > screen.x + screen.width && getVelocityX() > 0)
            setX(screen.x);
        if (getY() - getSize() < screen.y && getVelocityY() < 0)
            setY(screen.y + screen.height);
        if (getY() + getSize() > screen.y + screen.height && getVelocityY() > 0)
            setY(screen.y);
    }
    
    /**
     * We want all classes that extend this one to implement their own rendering
     * @param graphics Graphics object
     */
    protected abstract void render(final Graphics graphics);
}