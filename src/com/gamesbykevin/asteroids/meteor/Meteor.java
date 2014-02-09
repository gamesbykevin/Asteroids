package com.gamesbykevin.asteroids.meteor;

import com.gamesbykevin.asteroids.engine.Engine;
import com.gamesbykevin.asteroids.levelobject.LevelObject;
import com.gamesbykevin.asteroids.shared.IElement;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Polygon;
import java.util.Random;

public final class Meteor extends LevelObject implements IElement
{
    //how big the initial meteor is
    private static final int START_SIZE = 30;
    
    //how many hits before the meteor is destroyed
    private int hits;
    
    //the default start number of hits left
    private static final int START_HITS = 3;
    
    //min and max speed
    private static final double MIN_SPEED = 0;
    private static final double MAX_SPEED = 1.5;
    
    //the rate at which we can turn
    private final double TURN_RATE = .1;
    
    public Meteor(final double x, final double y, final Random random)
    {
        this(x, y, getRandomVelocity(random), getRandomDirection(random), START_SIZE, START_HITS);
    }
    
    /**
     * Create a new meteor with the specified information
     * @param x x-coordinate location
     * @param y y-coordinate location
     * @param v Random velocity
     * @param d Random direction
     * @param size The size of the meteor
     * @param hits How many more hits does this meteor have
     */
    public Meteor(final double x, final double y, final double v, final double d, final int size, final int hits)
    {
        //call parent constructor
        super(size);
        
        //set how many hits are left for this meteor
        this.hits = hits;
        
        //pick a random location
        super.setLocation(x, y);
        
        //set random speed in random direction
        super.setVelocityX(v * Math.cos(d));
        super.setVelocityY(v * Math.sin(d));
        
        //set original coordinates
        final int[] xpoints = {-getSize(), getSize(), getSize(), -getSize()};
        final int[] ypoints = {-getSize(), -getSize(), getSize(), getSize()};
    
        //add body
        super.add(xpoints, ypoints);
    }
    
    public static double getRandomDirection(final Random random)
    {
        return (2 * Math.PI * random.nextDouble());
    }
    
    public static double getRandomVelocity(final Random random)
    {
        return (MIN_SPEED + (random.nextDouble() * MAX_SPEED));
    }
    
    /**
     * How many more hits does this meteor have
     * @return The # of hits the meteor has left
     */
    public int getHits()
    {
        return this.hits;
    }
    
    @Override
    public void update(final Engine engine)
    {
        //check if the meteors have hit each other
        for (Meteor tmp : engine.getManager().getMeteors())
        {
            //don't check the same meteor
            if (getId() == tmp.getId())
                continue;

            if (hasCollision(tmp))
            {
                //switch directions
                final double dx = getVelocityX();
                final double dy = getVelocityY();

                setVelocityX(tmp.getVelocityX());
                setVelocityY(tmp.getVelocityY());

                tmp.setVelocityX(dx);
                tmp.setVelocityY(dy);

                //update the meteor location
                updateCoordinates(engine.getMain().getScreen());

                break;
            }
        }
        
        //rotate the meteor automatically
        setAngle(getAngle() + TURN_RATE);
        
        //move and update coordinates
        updateCoordinates(engine.getMain().getScreen());
    }
    
    @Override
    public void render(final Graphics graphics)
    {
        for (Polygon p : getBoundaries())
        {
            graphics.setColor(Color.GRAY);
            graphics.fillPolygon(p);
        }
    }
}