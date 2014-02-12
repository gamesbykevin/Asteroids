package com.gamesbykevin.asteroids.ship;

import com.gamesbykevin.asteroids.engine.Engine;
import com.gamesbykevin.asteroids.levelobject.LevelObject;
import com.gamesbykevin.asteroids.menu.option.Mode;
import com.gamesbykevin.asteroids.shared.IElement;

import java.awt.Color;

public class Cpu extends Ship implements IElement
{
    //determine when we are within range to fire
    private static final double FIRE_RANGE_RATE = 10;
    
    //determine when we are too close and need to avoid death
    private static final double DEATH_RANGE_RATE = FIRE_RANGE_RATE - 5;
    
    //the cpu will turn faster than average
    private static final double TURN_RATE_BOOST = 6;
    
    //the meteor we are targeting
    private LevelObject target;
    
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
    
    public Cpu(final int bulletLimit)
    {
        super(Color.YELLOW, bulletLimit);
        
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
        
        //if we are playing race mode we target the meteors
        if (engine.getManager().getMode() == Mode.Selections.Race || engine.getManager().getMode() == Mode.Selections.Cooperative)
        {
            //check if any meteors are within firing range or too close
            for (LevelObject opponent : engine.getManager().getMeteors())
            {
                shortest = checkOpponent(opponent, shortest, hasShot(engine.getManager().getBullets()));
            }
        }
        else
        {
            //we are targeting the other ship
            for (Ship opponent : engine.getManager().getShips())
            {
                //we don't want to attack our own ship
                if (opponent.getId() == getId())
                    continue;
                
                //if our opponent is invincible avoid them
                if (opponent.hasInvincibility())
                {
                    setTarget(Action.Approach, opponent);
                }
                else
                {
                    shortest = checkOpponent(opponent, shortest, hasShot(engine.getManager().getBullets()));
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
                        if (hasShot(engine.getManager().getBullets()))
                            addBullet(engine.getManager().getBullets());
                        
                        break;
                }
            }
            
            //reset the target and action
            setTarget(null, null);
        }
    }
    
    /**
     * Check this opponent to determine what is the next target/action
     * @param opponent Opponent to check
     * @param shortest The shortest distance found so far
     * @param hasShot Do we have the ability to shoot
     * @return the distance of the shortest path found
     */
    private double checkOpponent(final LevelObject opponent, double shortest, final boolean hasShot)
    {
        //calculate the distance
        final double distance = super.getDistance(opponent);

        //if we aren't invisible or if it will be running out soon and we are too close to opponent
        if ((!hasInvincibility() || isInvisibleAlmostOver()) && (distance <= opponent.getSize() * DEATH_RANGE_RATE || distance <= getSize() * DEATH_RANGE_RATE))
        {
            //is this the closest
            if (distance <= shortest)
            {
                //we have the new shorter distance
                shortest = distance;

                //set action and target
                setTarget(Action.Escape, opponent);
            }
        }
        else
        {
            //escape has priority over attacking so we can't continue
            if (action == Action.Escape)
                return shortest;

            //if we are within firing range
            if (distance <= opponent.getSize() * FIRE_RANGE_RATE || distance <= getSize() * FIRE_RANGE_RATE)
            {
                //is this the closest
                if (distance <= shortest)
                {
                    //we have the new shorter distance
                    shortest = distance;

                    //set action and target
                    setTarget(Action.Attack, opponent);
                }
            }
            else
            {
                //make sure no action has been set
                if (action == null || action == Action.Approach)
                {
                    //we don't want to approach if we don't have a shot
                    if (hasShot)
                    {
                        //is this the closest
                        if (distance <= shortest)
                        {
                            //we have the new shorter distance
                            shortest = distance;

                            //set action and target
                            setTarget(Action.Approach, opponent);
                        }
                    }
                }
            }
        }
        
        //return shortest distance found
        return shortest;
    }
    
    private void setTarget(final Action action, final LevelObject target)
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
            //if escaping we want the turning to be faster to allow escape
            if (action == Action.Escape)
            {
                setAngle(getAngle() + (TURN_RATE * TURN_RATE_BOOST));
            }
            else
            {
                setAngle(getAngle() + TURN_RATE);
            }

            if (getAngle() > getDestination())
                setAngle(getDestination());
        }

        //turn towards our destination
        if (getAngle() > getDestination())
        {
            //if escaping we want the turning to be faster to allow escape
            if (action == Action.Escape)
            {
                setAngle(getAngle() - (TURN_RATE * TURN_RATE_BOOST));
            }
            else
            {
                setAngle(getAngle() - TURN_RATE);
            }
            
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