package com.gamesbykevin.asteroids.bullet;

import com.gamesbykevin.framework.util.*;

import com.gamesbykevin.asteroids.engine.Engine;
import com.gamesbykevin.asteroids.levelobject.LevelObject;
import com.gamesbykevin.asteroids.meteor.Meteor;
import com.gamesbykevin.asteroids.shared.IElement;
import com.gamesbykevin.asteroids.ship.Ship;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Polygon;

/**
 * The bullets that can be fired by the ship
 * @author GOD
 */
public final class Bullet extends LevelObject implements IElement
{
    //the speed of the bullet
    private final double SPEED = 6;
    
    //how big is the bullet
    private static final int SIZE = 1;
    
    //how long should the bullet be active if no collision in milliseconds
    private final long BULLET_LIFE_DURATION = 2000;
    
    //our timer to track life
    private final Timer timer;
    
    //relative coordinates for the body
    private static final int[] XPOINTS_BODY = {-SIZE, SIZE, SIZE, -SIZE};
    private static final int[] YPOINTS_BODY = {-SIZE, -SIZE, SIZE, SIZE};
    
    /**
     * Create a new bullet taking information from it's parent ship
     * @param ship The ship the bullet was fired from
     */
    public Bullet(final Ship ship)
    {
        //call parent constructor
        super(SIZE);
        
        //set the location
        super.setX(ship.getX() - (SIZE / 2));
        super.setY(ship.getY() - (SIZE / 2));
        
        //create new timer with specified duration
        this.timer = new Timer(Timers.toNanoSeconds(BULLET_LIFE_DURATION));
        
        //add the body
        super.add(XPOINTS_BODY, YPOINTS_BODY);
        
        //set the velocity which will be faster than the ship's speed
        super.setVelocityX(SPEED * Math.cos(ship.getAngle()) + ship.getVelocityX());
        super.setVelocityY(SPEED * Math.sin(ship.getAngle()) + ship.getVelocityY());
    }
    
    @Override
    public void update(final Engine engine)
    {
        //if time has passed we will no longer move
        if (isDead())
            return;
        
        //check for meteor collision
        for (Meteor meteor : engine.getManager().getMeteors())
        {
            if (hasCollision(meteor))
            {
                //flag the meteor and bullet to die
                markDead();
                meteor.markDead();

                //split up the meteor
                engine.getManager().addMeteors(meteor, engine.getRandom());

                //don't check any meteors until the next bullet
                break;
            }
        }

        //update x,y coordinate
        updateCoordinates(engine.getMain().getScreen());
        
        if (timer.hasTimePassed())
            markDead();
        
        //deduct time from the timer
        timer.update(engine.getMain().getTime());
    }
    
    @Override
    public void render(final Graphics graphics)
    {
        for (Polygon p : getBoundaries())
        {
            graphics.setColor(Color.YELLOW);
            graphics.fillPolygon(p);
        }
    }
}