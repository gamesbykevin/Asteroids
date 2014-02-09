package com.gamesbykevin.asteroids.ship;

import com.gamesbykevin.asteroids.bullet.Bullet;
import com.gamesbykevin.asteroids.engine.Engine;
import com.gamesbykevin.asteroids.meteor.Meteor;
import com.gamesbykevin.asteroids.shared.IElement;

public class Cpu extends Ship implements IElement
{
    //determine when we are within range to fire
    private static final double FIRE_RANGE_RATE = 10;
    
    //determine when we are too close and need to avoid death
    private static final double DEATH_RANGE_RATE = FIRE_RANGE_RATE - 4;
    
    //the cpu will turn faster than average
    private static final double TURN_RATE_BOOST = 7;
    
    //the meteor we are targeting
    private Meteor target;
    
    //the angle destination we want to turn towards
    private double destination;
    
    //the speed limit
    private static final double DEFAULT_SPEED_RATE = .05;
    
    /**
     * The options for the cpu
     */
    private enum Action
    {
        Attack,
        Escape,
        Approach
    }
    
    //the current assignent for the cpu
    private Action action; 
    
    public Cpu(final double x, final double y)
    {
        super();
        
        //set the start location
        super.setLocation(x, y);
        
        //set speed
        super.setSpeedRate(DEFAULT_SPEED_RATE);
    }
    
    @Override
    public void update(final Engine engine)
    {
        //update standard things
        super.updateStandard(engine);
     
        //shortest distance found, because we want to avoid the closest
        double shortest = Math.pow((getSize() * DEATH_RANGE_RATE), (getSize() * DEATH_RANGE_RATE));
        
        //check if any meteors are within firing range or too close
        for (Meteor meteor : engine.getManager().getMeteors())
        {
            //calculate the distance
            final double distance = super.getDistance(meteor);
            
            //if we are too close that we are in danger
            if (!hasInvincibility() && distance <= meteor.getSize() * DEATH_RANGE_RATE || !hasInvincibility() && distance <= getSize() * DEATH_RANGE_RATE)
            {
                //is this the closest
                if (distance <= shortest)
                {
                    //we have the new shorter distance
                    shortest = distance;
                    
                    //set action and target
                    setTarget(Action.Escape, meteor);
                }
            }
            else
            {
                //escape has priority over attacking so we can't continue
                if (action == Action.Escape)
                    continue;
                    
                //if we are within firing range
                if (distance <= meteor.getSize() * FIRE_RANGE_RATE || distance <= getSize() * FIRE_RANGE_RATE)
                {
                    //is this the closest
                    if (distance <= shortest)
                    {
                        //we have the new shorter distance
                        shortest = distance;

                        //set action and target
                        setTarget(Action.Attack, meteor);
                    }
                }
                else
                {
                    //make sure no action has been set
                    if (action == null || action == Action.Approach)
                    {
                        //is this the closest
                        if (distance <= shortest)
                        {
                            //we have the new shorter distance
                            shortest = distance;

                            //set action and target
                            setTarget(Action.Approach, meteor);
                        }
                    }
                }
            }
        }
        
        //don't speed away yet
        setSpeeding(false);

        //make sure we have an action and an target
        if (action != null && target != null)
        {
            switch(action)
            {
                case Escape:
                    calculateDestination(false);
                    break;
                    
                case Attack:
                    calculateDestination(true);
                    break;
                    
                case Approach:
                    calculateDestination(true);
                    break;
            }
            
            //rotate ship accordingly
            rotateShip();
            
            //if we are at our destination execute next step
            if (getAngle() == getDestination())
            {
                switch(action)
                {
                    case Approach:
                    case Escape:
                        
                        //move
                        setSpeeding(true);
                        break;
                        
                    case Attack:
                        
                        //are we able to fire a bullet
                        if (engine.getManager().getBullets().size() < BULLET_LIMIT)
                        {
                            //add bullet to list
                            engine.getManager().getBullets().add(new Bullet(this));
                        }
                        break;
                }
            }
            
            //reset the target and action
            setTarget(null, null);
        }
    }
    
    private void setTarget(final Action action, final Meteor target)
    {
        this.action = action;
        this.target = target;
    }
    
    /**
     * Rotate the ship toward its destination
     */
    private void rotateShip()
    {
        //turn towards our destination
        if (getAngle() < getDestination())
        {
            setAngle(getAngle() + (TURN_RATE * TURN_RATE_BOOST));

            if (getAngle() > getDestination())
                setAngle(getDestination());
        }

        //turn towards our destination
        if (getAngle() > getDestination())
        {
            setAngle(getAngle() - (TURN_RATE * TURN_RATE_BOOST));

            if (getAngle() < getDestination())
                setAngle(getDestination());
        }
    }
    
    /**
     * Determine the angle we need to face
     * @param attack Are we to attack the target, if false we will turn away
     */
    private void calculateDestination(final boolean attack)
    {
        //get the angle to face
        this.destination = Math.atan((target.getY() - this.getY()) / (target.getX() - this.getX()));
        
        if (target.getX() - this.getX() < 0)
            this.destination += Math.PI;
        
        //if we aren't attacking turn away
        if (!attack)
            this.destination = this.destination + Math.toRadians(180);
        
        //make sure angle doesn't get too high
        if (getDestination() > (2 * Math.PI))
            this.destination -= (2 * Math.PI);
        if (getDestination() < 0)
            this.destination += (2 * Math.PI);
    }
    
    /**
     * Get the angle destination
     * @return The angle where we want to turn our ship towards
     */
    private double getDestination()
    {
        return this.destination;
    }
}