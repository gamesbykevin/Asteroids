package com.gamesbykevin.asteroids.ship;

import com.gamesbykevin.asteroids.engine.Engine;
import com.gamesbykevin.asteroids.resources.GameAudio;
import com.gamesbykevin.asteroids.shared.IElement;

import java.awt.Color;
import java.awt.event.KeyEvent;

public class Human extends Ship implements IElement
{
    public Human(final int bulletLimit)
    {
        super(Color.MAGENTA, bulletLimit);
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
            //stop sound effect
            engine.getResources().stopGameAudio(GameAudio.Keys.Thrusters);
            
            //no longer speeding
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
        if (engine.getKeyboard().hasKeyPressed(KeyEvent.VK_UP) && !hasSpeeding())
        {
            //play sound effect
            engine.getResources().playGameAudio(GameAudio.Keys.Thrusters, true);
            
            //we are speeding
            setSpeeding(true);
        }
        
        //was space bar pressed and we are not speeding
        if (engine.getKeyboard().hasKeyPressed(KeyEvent.VK_SPACE) && !hasSpeeding())
        {
            //are we able to fire a bullet
            if (hasShot(engine.getManager().getBullets()))
            {
                if (engine.getManager().getBullets().isEmpty())
                {
                    //play sound effect
                    engine.getResources().playGameAudio(GameAudio.Keys.Fire, false);
                }
                
                //add bullet
                addBullet(engine.getManager().getBullets());
            }
            
            //no longer pressing space
            engine.getKeyboard().removeKeyPressed(KeyEvent.VK_SPACE);
        }
    }
}