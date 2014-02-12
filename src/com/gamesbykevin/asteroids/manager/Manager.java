package com.gamesbykevin.asteroids.manager;

import com.gamesbykevin.framework.menu.Menu;
import com.gamesbykevin.framework.util.*;

import com.gamesbykevin.asteroids.bullet.Bullet;
import com.gamesbykevin.asteroids.engine.Engine;
import com.gamesbykevin.asteroids.menu.CustomMenu.*;
import com.gamesbykevin.asteroids.menu.option.*;
import com.gamesbykevin.asteroids.meteor.Meteor;
import com.gamesbykevin.asteroids.resources.*;
import com.gamesbykevin.asteroids.resources.GameImage.Keys;
import com.gamesbykevin.asteroids.ship.*;
import java.awt.Color;

import java.awt.Graphics;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * The parent class that contains all of the game elements
 * @author GOD
 */
public final class Manager implements IManager
{
    //the hero ships in the game
    private List<Ship> ships;
    
    //the bullets shot from the ship(s)
    private List<Bullet> bullets;
    
    //the meteors
    private List<Meteor> meteors;
    
    //the game mode we are playing
    private final Mode.Selections mode;
    
    //the lives each player should have
    private final int startingLives;
    
    //the games difficulty
    private final Difficulty.Selections difficulty;
    
    //the number of meteors to start with determined by difficulty
    private int meteorCount;
    
    //the area where gameplay will occur
    private Rectangle gameWindow;
    
    //the level we are on
    private int level = 1;
    
    //how many bullets can be fired at once
    private static final int DEFAULT_BULLET_LIMIT = 3;
    
    //has the game ended
    private boolean gameOver = false;
    
    //the duration the race lasts for
    private static final long RACE_DELAY = Timers.toNanoSeconds(60000L);
    
    //our timer for race mode
    private Timer timer;
    
    /**
     * Constructor for Manager, this is the point where we load any menu option configurations
     * @param engine
     * @throws Exception 
     */
    public Manager(final Engine engine) throws Exception
    {
        //get the size of the screen
        Rectangle screen = engine.getMain().getScreen();
        
        //calculate the game window where game play will occur
        this.gameWindow = new Rectangle(screen.x, screen.y, screen.width, screen.height);
        
        //get the menu object
        final Menu menu = engine.getMenu();
       
        //determine what mode is being played
        this.mode = Mode.Selections.values()[menu.getOptionSelectionIndex(LayerKey.Options, OptionKey.Mode)];
        
        //determine the game difficulty
        this.difficulty = Difficulty.Selections.values()[menu.getOptionSelectionIndex(LayerKey.Options, OptionKey.Difficulty)];

        //there won't be any meteors in versus mode
        if (mode != Mode.Selections.Vs)
        {
            //determine how many meteors to start with
            switch(this.difficulty)
            {
                case Easy:
                default:
                    this.meteorCount = 1;
                    break;

                case Medium:
                    this.meteorCount = 2;
                    break;

                case Hard:
                    this.meteorCount = 3;
                    break;
            }
        }
        else
        {
            //we are playing vs so there will be 0 meteors
            this.meteorCount = 0;
        }
        
        //determine how many starting lives
        switch(Lives.Selections.values()[menu.getOptionSelectionIndex(LayerKey.Options, OptionKey.Lives)])
        {
            case Five:
                this.startingLives = 5;
                break;
                
            case Seven:
                this.startingLives = 7;
                break;
                
            case Three:
            default:
                this.startingLives = 3;
                break;
        }
        
        //create our new list of bullets
        this.bullets = new ArrayList<>();
        
        //create our list that will contain the ships
        this.ships = new ArrayList<>();
        
        //create our list that will contain the meteors
        this.meteors = new ArrayList<>();
        
        //if race mode create our timer
        if (mode == Mode.Selections.Race)
        {
            this.timer = new Timer(RACE_DELAY);
        }
        
        switch (mode)
        {
            case Original:
                //add to human list
                ships.add(new Human(DEFAULT_BULLET_LIMIT));
                break;
                
            case Race:
                //add human to list
                ships.add(new Human(1));
                
                //add opponent to list
                ships.add(new Cpu(1));
                break;
                
            case Cooperative:
                //add to human list
                ships.add(new Human(DEFAULT_BULLET_LIMIT));

                //add opponent to list
                ships.add(new Cpu(DEFAULT_BULLET_LIMIT));
                break;
                      
                
            case Vs:
                //add to human list
                ships.add(new Human(DEFAULT_BULLET_LIMIT));

                //add opponent to list
                ships.add(new Cpu(DEFAULT_BULLET_LIMIT));
                break;
                
            default:
                throw new Exception("Mode needs to be setup here.");
        }
        
        //set the starting lives for all the players
        for (Ship ship : getShips())
        {
            ship.setLives(startingLives);
        }
        
        //reset the game
        reset(screen, engine.getRandom());
    }
    
    public Mode.Selections getMode()
    {
        return this.mode;
    }
    
    /**
     * Get the game window
     * @return The Rectangle where game play will take place
     */
    public Rectangle getGameWindow()
    {
        return this.gameWindow;
    }
    
    /**
     * Perform the following:<br>
     * 1. Reset the ships to the center of the map.<br>
     * 2. Remove existing bullets.<br>
     * 3. Remove existing meteors.<br>
     * @param screen
     * @param random 
     */
    private void reset(final Rectangle screen, final Random random)
    {
        //if we aren't playing versus then all ships spawn in middle
        if (mode != Mode.Selections.Vs)
        {
            for (Ship ship : getShips())
            {
                //reset back to middle of screen
                ship.resetVelocity();
                ship.resetAngle();
                ship.setLocation(screen.x + (screen.width / 2), screen.y + (screen.height / 2));
            }
        }
        else
        {
            //both ships start without velocity
            getShips().get(0).resetVelocity();
            getShips().get(1).resetVelocity();
            
            //reset the facing angle
            getShips().get(0).resetAngle();
            getShips().get(1).resetAngle();
            
            //now place them on opposite sides
            getShips().get(0).setLocation(screen.x + getShips().get(0).getSize(),                screen.y + random.nextInt(screen.height));
            getShips().get(1).setLocation(screen.x + screen.width - getShips().get(1).getSize(), screen.y + random.nextInt(screen.height));
        }
        
        //remove any existing bullets
        getBullets().clear();
        
        //remove any existing meteors
        getMeteors().clear();
        
        //add the specified number of meteors
        for (int count=0; count < this.meteorCount; count++)
        {
            addMeteor(screen, random);
        }
    }
    
    /**
     * Get the Ship
     * @param id Unique key of the ship we want
     * @return The ship with the id parameter, null is returned if Ship not found
     */
    public Ship getShip(final long id)
    {
        for (Ship ship : getShips())
        {
            if (ship.getId() == id)
                return ship;
        }
        
        return null;
    }
    
    public List<Ship> getShips()
    {
        return this.ships;
    }
    
    public List<Meteor> getMeteors()
    {
        return this.meteors;
    }
    
    public List<Bullet> getBullets()
    {
        return this.bullets;
    }
    
    /**
     * Free up resources
     */
    @Override
    public void dispose()
    {
        for (Ship ship : ships)
        {
            ship.dispose();
            ship = null;
        }
        
        ships.clear();
        ships = null;
        
        for (Meteor meteor : meteors)
        {
            meteor.dispose();
            meteor = null;
        }
        
        meteors.clear();
        meteors = null;
        
        for (Bullet bullet : bullets)
        {
            bullet.dispose();
            bullet = null;
        }
        
        bullets.clear();
        bullets = null;
    }
    
    /**
     * Update all application elements
     * 
     * @param engine Our main game engine
     * @throws Exception 
     */
    @Override
    public void update(final Engine engine) throws Exception
    {
        //don't continue if the game is over
        if (gameOver)
            return;
        
        for (Ship ship : getShips())
        {
            //update ship
            ship.update(engine);
        }
        
        for (Meteor meteor : getMeteors())
        {
            //update meteor
            meteor.update(engine);
        }
        
        for (Bullet bullet : getBullets())
        {
            //update bullet state
            bullet.update(engine);
        }
        
        //check the game mode
        checkMode(engine);
        
        //finally perform cleanup of marked object
        cleanupObjects();
    }
    
    /**
     * Determine the game rules based on the game mode
     * @param engine Game engine
     */
    private void checkMode(final Engine engine) throws Exception
    {
        //are all meteors destroyed
        boolean allDestroyed = true;
        
        //check if all meteors are dead
        for (Meteor meteor : getMeteors())
        {
            if (!meteor.isDead())
            {
                allDestroyed = false;
                break;
            }
        }
        
        switch(mode)
        {
            case Original:
            case Cooperative:
                //are all ships dead
                boolean allDead = true;
                
                for (Ship ship : getShips())
                {
                    if (ship.isDead())
                    {
                        //if a ship has lives then all ships aren't dead
                        if (ship.getLives() > 0)
                            allDead = false;
                        
                        //reset ship to the center
                        ship.setLocation(
                            getGameWindow().x + (getGameWindow().width / 2), 
                            getGameWindow().y + (getGameWindow().height / 2));
                    }
                    else
                    {
                        //there is a ship
                        allDead = false;
                    }
                }
                
                //if all meteors are destroyed add another meteor and restart
                if (allDestroyed && !allDead)
                {
                    //add an extra meteor
                    this.meteorCount++;
                    
                    //change level #
                    level++;
                    
                    //reset new game
                    reset(getGameWindow(), engine.getRandom());
                }
                
                //are all ships dead
                if (allDead)
                    gameOver = true;
                
                break;
                
            case Race:
                
                //update our timer
                timer.update(engine.getMain().getTime());
                
                for (Ship ship : getShips())
                {
                    //if a ship is dead but still has lives, reset location
                    if (ship.isDead() && ship.getLives() > 0)
                    {
                        //reset ship to the center
                        ship.setLocation(
                            getGameWindow().x + (getGameWindow().width / 2), 
                            getGameWindow().y + (getGameWindow().height / 2));
                    }
                }
                
                //if all meteors are destroyed
                if (allDestroyed)
                {
                    //reset new game
                    reset(getGameWindow(), engine.getRandom());
                }
                
                //game is over when time has run up
                if (timer.hasTimePassed())
                {
                    gameOver = true;
                    timer.setRemaining(0);
                }
                
                break;
                
            case Vs:
                
                boolean isDead = false;
                
                for (Ship ship : getShips())
                {
                    if (ship.isDead())
                    {
                        if (ship.getLives() > 0)
                        {
                            isDead = true;
                            break;
                        }
                        else
                        {
                            gameOver = true;
                        }
                    }
                }
                
                //if 1 ship has died
                if (isDead)
                {
                    //reset the spawn protection for the ships
                    for (Ship ship : getShips())
                    {
                        ship.resetTimer();
                    }
                    
                    //reset new game
                    reset(getGameWindow(), engine.getRandom());
                }
                break;
                
            default:
                throw new Exception("Mode needs to be setup here");
        }
    }
    
    /**
     * Split up the meteor into 4 smaller meteors
     * @param meteor The meteor that was hit
     */
    public void addMeteors(final Meteor meteor, final Random random)
    {
        //make sure we are supposed to add more meteors
        if (meteor.getHits() > 1)
        {
            addMeteor(meteor.getX() - meteor.getSize(), meteor.getY() - meteor.getSize(), meteor, random);
            addMeteor(meteor.getX(),                    meteor.getY() - meteor.getSize(), meteor, random);
            addMeteor(meteor.getX() - meteor.getSize(), meteor.getY()                   , meteor, random);
            addMeteor(meteor.getX()                   , meteor.getY()                   , meteor, random);
        }
        
        //mark current meteor as dead
        meteor.markDead();
    }
    
    /**
     * Add meteor to the list
     * @param x Starting x-coordinate
     * @param y Starting y-coordinate
     * @param meteor The parent Meteor where this meteor came from. Can be null.
     * @param random Object used to make random decisions.
     */
    private void addMeteor(final double x, final double y, final Meteor meteor, final Random random)
    {
        //create new meteor
        Meteor tmp;

        if (meteor != null)
        {
            tmp = new Meteor(x, y, Meteor.getRandomVelocity(random), Meteor.getRandomDirection(random), meteor.getSize() / 2, meteor.getHits() - 1);
            
            //mark the parent where it came from
            tmp.setParentId(meteor.getId());
        }
        else
        {
            tmp = new Meteor(x, y, random);
        }

        //add meteor to our list
        getMeteors().add(tmp);
    }
    
    /**
     * Add meteor at random location
     * @param screen Area to spawn meteor in
     * @param random Object used to make random decisions
     */
    public void addMeteor(final Rectangle screen, final Random random)
    {
        //our random location for the meteor
        final double x, y;
        
        //pick random location on one of the edges of the screen
        switch(random.nextInt(4))
        {
            //north
            case 0:
                x = screen.x + (screen.width * random.nextDouble());
                y = screen.y;
                break;
                
            //east
            case 1:
                x = screen.x + screen.width;
                y = screen.y + (screen.height * random.nextDouble());
                break;
                
            //south
            case 2:
                x = screen.x + (screen.width * random.nextDouble());
                y = screen.y + screen.height;
                break;
                
            //west
            case 3:
            default:
                x = screen.x;
                y = screen.y + (screen.height * random.nextDouble());
                break;
        }
        
        //add meteor to list
        addMeteor(x, y, null, random);
    }
    
    /**
     * Remove any bullets, meteors, ships that are marked as dead.
     */
    private void cleanupObjects()
    {
        for (int i=0; i < getBullets().size(); i++)
        {
            //if the bullet is dead remove it
            if (getBullets().get(i).isDead())
            {
                getBullets().remove(i);
                i--;
            }
        }
        
        for (int i=0; i < getMeteors().size(); i++)
        {
            //if the bullet is dead remove it
            if (getMeteors().get(i).isDead())
            {
                getMeteors().remove(i);
                i--;
            }
        }
        
        for (int i=0; i < getShips().size(); i++)
        {
            //if the bullet is dead remove it
            if (getShips().get(i).isDead())
            {
                if (getShips().get(i).getLives() > 0)
                {
                    //deduct a life
                    getShips().get(i).setLives(getShips().get(i).getLives() - 1);
                    
                    //reset the spawn timer for spawn protection
                    getShips().get(i).resetTimer();
                    
                    //no longer dead
                    getShips().get(i).unmarkDead();
                }
            }
        }
    }
    
    /**
     * Draw all of our application elements
     * @param graphics Graphics object used for drawing
     */
    @Override
    public void render(final Graphics graphics)
    {
        for (Meteor meteor : getMeteors())
        {
            meteor.render(graphics);
        }
        
        for (Ship ship : getShips())
        {
            //only draw the ship if it is not dead
            if (!ship.isDead())
            {
                ship.render(graphics);
            }
        }
        
        for (Bullet bullet : getBullets())
        {
            bullet.render(graphics);
        }
        
        //draw the game stats
        switch(mode)
        {
            case Original:
                graphics.setColor(Color.WHITE);
                graphics.drawString("Level: " + level,                         25, 25);
                graphics.drawString("Lives: " + getShips().get(0).getLives(), 125, 25);
                graphics.drawString("Kills: " + getShips().get(0).getKills(), 225, 25);
                break;
                
            case Race:
                graphics.setColor(getShips().get(0).getColor());
                graphics.drawString("Hum Kills: " + getShips().get(0).getKills(), 15, 25);
                graphics.drawString("Time: " + timer.getDescRemaining(Timers.FORMAT_7), 200, 25);
                graphics.setColor(getShips().get(1).getColor());
                graphics.drawString("Cpu Kills: " + getShips().get(1).getKills(), 375, 25);
                break;
                
            case Vs:
                graphics.setColor(getShips().get(0).getColor());
                graphics.drawString("Hum Lives: " + getShips().get(0).getLives(), 50, 25);
                graphics.setColor(getShips().get(1).getColor());
                graphics.drawString("Cpu Lives: " + getShips().get(1).getLives(), 275, 25);
                break;
                
            case Cooperative:
                graphics.setColor(getShips().get(0).getColor());
                graphics.drawString("Hum Lives: " + getShips().get(0).getLives(), 50, 25);
                graphics.setColor(Color.WHITE);
                graphics.drawString("Level: " + level,                            175, 25);
                graphics.setColor(getShips().get(1).getColor());
                graphics.drawString("Cpu Lives: " + getShips().get(1).getLives(), 275, 25);
                break;
        }
        
        //if the game is over draw notification
        if (gameOver)
        {
            graphics.setColor(Color.RED);
            graphics.drawString("GAME OVER, HIT \"ESC\" TO ACCESS MENU.", 25, 100);
            
            //determine who won
            switch(mode)
            {
                case Race:
                    
                    if (getShips().get(0).getKills() > getShips().get(1).getKills())
                    {
                        graphics.setColor(getShips().get(0).getColor());
                        graphics.drawString("Human Wins", 25, 75);
                    }
                    
                    if (getShips().get(0).getKills() < getShips().get(1).getKills())
                    {
                        graphics.setColor(getShips().get(1).getColor());
                        graphics.drawString("Cpu Wins", 25, 75);
                    }
                    
                    if (getShips().get(0).getKills() == getShips().get(1).getKills())
                    {
                        graphics.setColor(Color.WHITE);
                        graphics.drawString("DRAW! WOW!!", 25, 75);
                    }
                    break;

                case Vs:
                    
                    if (getShips().get(0).isDead())
                    {
                        graphics.setColor(getShips().get(1).getColor());
                        graphics.drawString("Cpu Wins", 25, 75);
                    }
                    
                    if (getShips().get(1).isDead())
                    {
                        graphics.setColor(getShips().get(0).getColor());
                        graphics.drawString("Human Wins", 25, 75);
                    }
                    break;
            }
        }
    }
}