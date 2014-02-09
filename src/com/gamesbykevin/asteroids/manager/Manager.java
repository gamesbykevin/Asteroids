package com.gamesbykevin.asteroids.manager;

import com.gamesbykevin.framework.menu.Menu;

import com.gamesbykevin.asteroids.bullet.Bullet;
import com.gamesbykevin.asteroids.engine.Engine;
import com.gamesbykevin.asteroids.levelobject.LevelObject;
import com.gamesbykevin.asteroids.menu.CustomMenu.*;
import com.gamesbykevin.asteroids.menu.option.*;
import com.gamesbykevin.asteroids.meteor.Meteor;
import com.gamesbykevin.asteroids.resources.*;
import com.gamesbykevin.asteroids.resources.GameImage.Keys;
import com.gamesbykevin.asteroids.ship.*;

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
    
    /**
     * Constructor for Manager, this is the point where we load any menu option configurations
     * @param engine
     * @throws Exception 
     */
    public Manager(final Engine engine) throws Exception
    {
        //get the menu object
        final Menu menu = engine.getMenu();
        
        //get the index of the game we want to play
        //final int gameIndex = menu.getOptionSelectionIndex(LayerKey.Options, OptionKey.Mode);
        
        //the type of game we want to play
        //this.type = Mode.Types.values()[gameIndex];
        
        
        //get the size of the screen
        Rectangle screen = engine.getMain().getScreen();
        
        //create our new list of bullets
        this.bullets = new ArrayList<>();
        
        //create our list that will contain the ships
        this.ships = new ArrayList<>();
        
        //set in the middle of the map
        //Human ship = new Human(screen.x + (screen.width / 2), screen.y + (screen.height / 2));
        Cpu ship = new Cpu(screen.x + (screen.width / 2), screen.y + (screen.height / 2));
        
        //add to list
        ships.add(ship);
        
        //create our list that will contain the meteors
        this.meteors = new ArrayList<>();
        
        //add at random location
        addMeteor(engine.getMain().getScreen(), engine.getRandom());
        addMeteor(engine.getMain().getScreen(), engine.getRandom());
        addMeteor(engine.getMain().getScreen(), engine.getRandom());
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
        
        //remove unused/dead
        cleanupObjects(
            engine.getMain().getScreen().x + (engine.getMain().getScreen().width / 2), 
            engine.getMain().getScreen().y + (engine.getMain().getScreen().height / 2)
        );
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
     * @param x Center x-coordinate
     * @param y Center y-coordinate
     */
    private void cleanupObjects(final int x, final int y)
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
                    
                    //reset location
                    getShips().get(i).setLocation(x, y);
                    
                    //reset the spawn timer
                    getShips().get(i).resetTimer();
                }
                else
                {
                    getShips().remove(i);
                    i--;
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
            ship.render(graphics);
        }
        
        for (Bullet bullet : getBullets())
        {
            bullet.render(graphics);
        }
    }
}