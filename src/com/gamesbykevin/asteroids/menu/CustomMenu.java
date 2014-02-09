package com.gamesbykevin.asteroids.menu;

import com.gamesbykevin.asteroids.menu.layer.*;
import com.gamesbykevin.asteroids.engine.Engine;
import com.gamesbykevin.asteroids.shared.IElement;

import com.gamesbykevin.framework.display.FullScreen;
import com.gamesbykevin.framework.menu.*;

import java.awt.Graphics;
import java.awt.event.KeyEvent;

/**
 * Custom menu setup
 * @author GOD
 */
public final class CustomMenu extends Menu implements IElement
{
    //reset = create a new game
    private boolean reset = true;
    
    //object used to switch container to full screen
    private FullScreen fullScreen;
    
    //previous Layer key used so when container loses focus we remember where we were at
    private Object previousLayerKey;
    
    //unique key to identify each Option
    public enum OptionKey 
    { 
        Sound, FullScreen, StartGame, Options, Controls, Instructions, Credits, GoBack, Resume, 
        NewGame, ExitGame, NewGameConfim, NewGameDeny, ExitGameConfirm, ExitGameDeny, 
        
    } 
    
    //unique key to indentify each Layer
    public enum LayerKey 
    {
        Title, Credits, MainTitle, Options, OptionsInGame, 
        NewGameConfirm, ExitGameConfirm, NoFocus, StartGame, NewGameConfirmed, 
        Controls1,  
        Instructions1, Instructions2, 
    }
    
    /**
     * Basic Option Selection Constants
     */
    public enum Toggle
    {
        Off, On
    }
    
    //is sound enabled, default true
    private Toggle sound = Toggle.On;
    
    //is full screen enabled, default false
    private Toggle fullWindow = Toggle.Off;
    
    //does the container have focus
    private Toggle focus = Toggle.On;
    
    public CustomMenu(final Engine engine) throws Exception
    {
        //set the container the menu will reside within
        super(engine.getMain().getScreen());
        
        //add each layer to menu below
        super.add(LayerKey.Title,           new Title(engine));
        super.add(LayerKey.Credits,         new Credits(engine));
        super.add(LayerKey.MainTitle,       new MainTitle(engine));
        super.add(LayerKey.Options,         new Options(engine));
        super.add(LayerKey.Controls1,       new Controls1(engine));
        super.add(LayerKey.Instructions1,   new Instructions1(engine));
        super.add(LayerKey.Instructions2,   new Instructions2(engine));
        super.add(LayerKey.OptionsInGame,   new OptionsInGame(engine));
        super.add(LayerKey.NewGameConfirm,  new NewGameConfirm(engine));
        super.add(LayerKey.ExitGameConfirm, new ExitGameConfirm(engine));
        super.add(LayerKey.NoFocus,         new NoFocus(engine));
        super.add(LayerKey.StartGame,       new StartGame(engine));
        super.add(LayerKey.NewGameConfirmed,new NewGameConfirmed(engine));
        
        //make sure each layer has been added to the menu
        for (LayerKey key: LayerKey.values())
        {
            //if the Layer does not exist
            if (!hasLayer(key))
                throw new Exception("Layer with corresponding key (" + key.toString() + ") has not been added to menu yet.");
        }
        
        //set the first layer
        super.setLayer(LayerKey.Title);
        
        //set the last layer so we know when the menu has completed
        super.setFinish(LayerKey.StartGame);
    }
    
    /**
     * Update game menu
     * @param engine Our game engine containing all resources etc... needed to update menu
     * 
     * @throws Exception 
     */
    @Override
    public void update(final Engine engine) throws Exception
    {
        //if the menu is not on the last layer we need to check for changes made in the menu
        if (!super.hasFinished())
        {
            //if we are on the main title screen and reset is not enabled
            if (super.hasCurrent(LayerKey.MainTitle) && !reset)
            {
                reset = true;
                engine.getResources().stopAllSound();
            }
            
            //the option selection for the sound and fullscreen
            Toggle tmpSound = sound, tmpFullWindow = fullWindow;
            
            //if on the options screen check if sound/fullScreen enabled
            if (super.hasCurrent(LayerKey.Options))
            {
                //tmpSound = Toggle.values()[getOptionSelectionIndex(LayerKey.Options, OptionKey.Sound)];
                tmpFullWindow = Toggle.values()[getOptionSelectionIndex(LayerKey.Options, OptionKey.FullScreen)];
            }
            
            //if on the in-game options screen check if sound/fullScreen enabled
            if (super.hasCurrent(LayerKey.OptionsInGame))
            {
                tmpSound = Toggle.values()[getOptionSelectionIndex(LayerKey.OptionsInGame, OptionKey.Sound)];
                tmpFullWindow = Toggle.values()[getOptionSelectionIndex(LayerKey.OptionsInGame, OptionKey.FullScreen)];
            }
            
            //if starting a new game change layer, stop all sound
            if (super.hasCurrent(LayerKey.NewGameConfirmed))
            {
                //go to specified layer
                super.setLayer(LayerKey.StartGame);
                
                //mark flag that we can reset
                reset = true;
                
                //stop all sound
                engine.getResources().stopAllSound();
            }
            
            //set all audio collections sound enabled on/off
            engine.getResources().setAudioEnabled(Toggle.On == tmpSound);
                
            //if the sound is off make sure all sounds stop
            if (Toggle.Off == tmpSound)
            {
                //stop all sound
                engine.getResources().stopAllSound();
                
                //disable all audio
                engine.getResources().setAudioEnabled(false);
            }
            
            this.sound = tmpSound;
            
            //if the values are not equal to each other a change was made
            if (tmpFullWindow != fullWindow)
            {
                if (fullScreen == null)
                    fullScreen = new FullScreen();

                //switch from fullscreen to window or vice versa
                fullScreen.switchFullScreen(engine.getMain().getApplet(), engine.getMain().getPanel());
                
                //grab the rectangle coordinates of the full screen
                engine.getMain().setFullScreen();

                this.fullWindow = tmpFullWindow;
            }
            
            //does the container have focus
            final Toggle tmpFocus = (engine.getMain().hasFocus()) ? Toggle.On : Toggle.Off;
            
            //if the values are not equal a change was made
            if (focus != tmpFocus)
            {
                //if the previous Layer is stored
                if (previousLayerKey != null)
                {
                    //set the menu to the previous Layer
                    super.setLayer(previousLayerKey);
                    
                    //there no longer is a previous Layer
                    previousLayerKey = null;
                }
                else
                {
                    //the previous Layer has not been set 
                    previousLayerKey = getKey();
                    
                    //set the current Layer to NoFocus
                    super.setLayer(LayerKey.NoFocus);
                }
                
                this.focus = tmpFocus;
            }
            
            //make sure this Option in all of the Layer(s) have the same value
            setOptionSelectionIndex(OptionKey.Sound, (tmpSound == Toggle.Off) ? 0 : 1);
            setOptionSelectionIndex(OptionKey.FullScreen, (tmpFullWindow == Toggle.Off) ? 0 : 1);
            
            super.update(engine.getMouse(), engine.getKeyboard(), engine.getMain().getTime());
        }
        else
        {
            //if resetGame is enabled and the menu is finished reset all game objects within engine
            if (reset)
            {
                reset = false;
                engine.reset();
            }
            
            //the menu has finished and the user has pressed 'escape' so we will bring up the in game options
            if (engine.getKeyboard().hasKeyPressed(KeyEvent.VK_ESCAPE))
            {
                super.setLayer(LayerKey.OptionsInGame);
                engine.getKeyboard().reset();
            }
        }
    }
    
    public boolean hasFocus()
    {
        return (this.focus == Toggle.On);
    }
    
    @Override
    public void render(final Graphics graphics)
    {
        super.render(graphics);
    }
    
    @Override
    public void dispose()
    {
        super.dispose();
        
        if (fullScreen != null)
            fullScreen.dispose();
        
        fullScreen = null;
        
        previousLayerKey = null;
    }
}