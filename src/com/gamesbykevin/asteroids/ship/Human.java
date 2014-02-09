package com.gamesbykevin.asteroids.ship;

import com.gamesbykevin.asteroids.bullet.Bullet;
import com.gamesbykevin.asteroids.engine.Engine;
import com.gamesbykevin.asteroids.shared.IElement;

import java.awt.event.KeyEvent;

public class Human extends Ship implements IElement
{
    public Human(final double x, final double y)
    {
        super();
        
        //set the start location
        super.setLocation(x, y);
    }
    
    @Override
    public void update(final Engine engine)
    {
        //update standard things
        super.updateStandard(engine);
        
        if (engine.getKeyboard().hasKeyReleased(KeyEvent.VK_LEFT))
        {
            engine.getKeyboard().removeKeyPressed(KeyEvent.VK_LEFT);
            engine.getKeyboard().removeKeyReleased(KeyEvent.VK_LEFT);
        }
        
        if (engine.getKeyboard().hasKeyReleased(KeyEvent.VK_RIGHT))
        {
            engine.getKeyboard().removeKeyPressed(KeyEvent.VK_RIGHT);
            engine.getKeyboard().removeKeyReleased(KeyEvent.VK_RIGHT);
        }
        
        if (engine.getKeyboard().hasKeyReleased(KeyEvent.VK_UP))
        {
            setSpeeding(false);
            
            engine.getKeyboard().removeKeyPressed(KeyEvent.VK_UP);
            engine.getKeyboard().removeKeyReleased(KeyEvent.VK_UP);
        }
        
        //was left pressed on the keyboard
        if (engine.getKeyboard().hasKeyPressed(KeyEvent.VK_LEFT))
            super.setAngle(super.getAngle() - TURN_RATE);
        
        //was right pressed on the keyboard
        if (engine.getKeyboard().hasKeyPressed(KeyEvent.VK_RIGHT))
            super.setAngle(super.getAngle() + TURN_RATE);
        
        //was up pressed on the keyboard
        if (engine.getKeyboard().hasKeyPressed(KeyEvent.VK_UP))
            setSpeeding(true);
        
        //was space bar pressed and we are not speeding
        if (engine.getKeyboard().hasKeyPressed(KeyEvent.VK_SPACE) && !hasSpeeding())
        {
            //are we able to fire a bullet
            if (engine.getManager().getBullets().size() < BULLET_LIMIT)
            {
                //create new bullet
                Bullet bullet = new Bullet(this);
                
                //mark the bullet where it came from
                bullet.setParentId(this.getId());
                
                engine.getManager().getBullets().add(bullet);
            }
            
            //no longer pressing space
            engine.getKeyboard().removeKeyPressed(KeyEvent.VK_SPACE);
        }
    }
}