package com.gamesbykevin.asteroids.bullet;

import com.gamesbykevin.framework.util.*;

import com.gamesbykevin.asteroids.engine.Engine;
import com.gamesbykevin.asteroids.levelobject.LevelObject;
import com.gamesbykevin.asteroids.menu.option.Mode;
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
    
    //how long should the bullet be active if no collision in nanoseconds
    private final long BULLET_LIFE_DURATION = Timers.toNanoSeconds(1500L);
    
    //our timer to track life
    private final Timer timer;
    
    //relative coordinates for the body
    private static final int[] XPOINTS_BODY = {-SIZE, SIZE, SIZE, -SIZE};
    private static final int[] YPOINTS_BODY = {-SIZE, -SIZE, SIZE, SIZE};
    
    //a bullet will have a different color depending on the source where it came from
    private final Color color;
    
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
        
        //store the ship's color
        this.color = ship.getColor();
        
        //create new timer with specified duration
        this.timer = new Timer(BULLET_LIFE_DURATION);
        
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
        
        //if versus mode we will check if the bullets hit the ships
        if (engine.getManager().getMode() == Mode.Selections.Vs)
        {
            for (Ship ship : engine.getManager().getShips())
            {
                //don't check if the ship is invincible
                if (ship.hasInvincibility())
                    continue;
                
                //if the bullet came from the ship don't check for collision
                if (ship.getId() == getParentId())
                    continue;
                
                //if the bullet hit the ship body
                if (hasCollision(getBoundaries().get(0), ship.getBoundaries().get(1)))
                {
                    //flag the bullet to be removed
                    markDead();

                    //mark the ship as dead
                    ship.markDead();
                }
            }
        }
        else
        {
            //check for meteor collision
            for (Meteor meteor : engine.getManager().getMeteors())
            {
                if (hasCollision(meteor))
                {
                    //flag the bullet to be removed
                    markDead();

                    //flag the meteor to be removed
                    meteor.markDead();

                    //split up the meteor
                    engine.getManager().addMeteors(meteor, engine.getRandom());

                    //credit the kill
                    engine.getManager().getShip(getParentId()).addKill();

                    //don't check any more meteors until the next bullet
                    break;
                }
            }
        }
        
        //update x,y coordinate
        updateCoordinates(engine.getManager().getGameWindow());
        
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
            graphics.setColor(color);
            graphics.fillPolygon(p);
        }
    }
}