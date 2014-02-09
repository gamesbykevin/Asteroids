package com.gamesbykevin.asteroids.ship;

import com.gamesbykevin.framework.util.Timer;
import com.gamesbykevin.framework.util.Timers;

import com.gamesbykevin.asteroids.engine.Engine;
import com.gamesbykevin.asteroids.levelobject.LevelObject;
import com.gamesbykevin.asteroids.meteor.Meteor;

import java.awt.Color;
import java.awt.Graphics;

public abstract class Ship extends LevelObject
{
    //size of the ship
    private static final int SIZE = 15;
    
    //how many bullets can be fired at once
    protected final int BULLET_LIMIT = 5;
    
    //the rate at which we can turn
    protected final double TURN_RATE = .05;
    
    //are we accelerating
    private boolean speeding = false;
    
    //the accelerating rate
    private final double DEFAULT_SPEED_RATE = .20;
    
    //the speed rate
    private double speedRate = DEFAULT_SPEED_RATE;
    
    //slow down rate
    private final double SPEED_DECELERATE = .995;
    
    //relative coordinates for the body facing east by default
    private final int[] XPOINTS_BODY = {SIZE, -(SIZE/2), 0, -(SIZE/2)};
    private final int[] YPOINTS_BODY = {0, -(SIZE/2), 0, (SIZE/2)};
    
    //relative coordinates for the thrust facing east by default
    private final int[] XPOINTS_THRUST = {-SIZE, 0, 0};
    private final int[] YPOINTS_THRUST = {0, -(SIZE/4), (SIZE/4)};
    
    //the # of lives our ship has
    private int lives = 5;
    
    //the amount of time we are invincible to getting hit by a meteor
    private static final long SAFE_SPAWN_DELAY = Timers.toNanoSeconds(5000L);
    
    //the timer that will track how long we are invincible
    private Timer timer;
    
    public Ship()
    {
        //call parent constructor
        super(SIZE);
        
        //add ship body and thrust as our polygon objects
        super.add(XPOINTS_THRUST, YPOINTS_THRUST);
        super.add(XPOINTS_BODY, YPOINTS_BODY);
        
        //create our timer
        this.timer = new Timer(SAFE_SPAWN_DELAY);
    }
    
    public void setLives(final int lives)
    {
        this.lives = lives;
    }
    
    public int getLives()
    {
        return this.lives;
    }
    
    protected boolean hasSpeeding()
    {
        return this.speeding;
    }
    
    protected void setSpeeding(final boolean speeding)
    {
        this.speeding = speeding;
    }
    
    protected void setSpeedRate(final double speedRate)
    {
        this.speedRate = speedRate;
    }
    
    protected double getSpeedRate()
    {
        return this.speedRate;
    }
    
    /**
     * Determine the speed of ship and update the location
     */
    private void calculateVelocity()
    {
        //if we are moving calculate velocity
        if (speeding)
        {
            setVelocityX(getVelocityX() + (getSpeedRate() * Math.cos(getAngle())));
            setVelocityY(getVelocityY() + (getSpeedRate() * Math.sin(getAngle())));
        }
        
        //if we aren't moving slow down the speed
        setVelocityX(getVelocityX() * SPEED_DECELERATE);
        setVelocityY(getVelocityY() * SPEED_DECELERATE);
    }
    
    
    @Override
    public void dispose()
    {
        super.dispose();
    }
    
    /**
     * Reset the timer back to the original countdown
     */
    public void resetTimer()
    {
        this.timer.reset();
        
        //unflag
        unmarkDead();
    }
    
    protected boolean hasInvincibility()
    {
        return (!timer.hasTimePassed());
    }
    
    /**
     * Update basic elements of ship.<br>
     * 1. Check for meteor collision<br>
     * 2. Calculate velocity<br>
     * 3. Update coordinates<br>
     * @param engine Game engine
     */
    protected void updateStandard(final Engine engine)
    {
        //if we aren't invincible check for collision
        if (!hasInvincibility())
        {
            //check if the meteors have hit a ship
            for (Meteor meteor : engine.getManager().getMeteors())
            {
                //if we have hit the meteor
                if (hasCollision(meteor))
                {
                    //flag dead
                    markDead();
                    
                    //no need to check any other meteors
                    break;
                }
            }
        }
        else
        {
            //update timer
            timer.update(engine.getMain().getTime());
        }
        
        //calculate slow down rate
        calculateVelocity();
        
        //move and update polygon coordinates
        updateCoordinates(engine.getMain().getScreen());
    }
    
    @Override
    public boolean hasCollision(final LevelObject object)
    {
        if (hasCollision(getBoundaries().get(1), object.getBoundaries().get(0)))
            return true;
        
        if (hasCollision(object.getBoundaries().get(0), getBoundaries().get(1)))
            return true;

        //no collision was made
        return false;
    }
    
    @Override
    public abstract void update(final Engine engine);
    
    @Override
    public void render(final Graphics graphics)
    {
        //draw each boundary
        for (int i=0; i < getBoundaries().size(); i++)
        {
            //is this the thrust polygon
            if (i == 0)
            {
                //only draw the thrust if we are accelerating
                if (speeding)
                {
                    graphics.setColor(Color.RED);
                    
                    if (!hasInvincibility())
                    {
                        graphics.fillPolygon(getBoundaries().get(i));
                    }
                    else
                    {
                        graphics.drawPolygon(getBoundaries().get(i));
                    }
                }
            }
            else
            {
                graphics.setColor(Color.WHITE);
                
                if (!hasInvincibility())
                {
                    graphics.fillPolygon(getBoundaries().get(i));
                }
                else
                {
                    graphics.drawPolygon(getBoundaries().get(i));
                }
            }
        }
    }
}