package com.gamesbykevin.asteroids.menu.layer;

import com.gamesbykevin.asteroids.resources.MenuAudio;
import com.gamesbykevin.asteroids.resources.MenuImage;
import com.gamesbykevin.framework.menu.*;
import com.gamesbykevin.framework.resources.Audio;
import com.gamesbykevin.framework.util.Timer;
import com.gamesbykevin.framework.util.Timers;

import com.gamesbykevin.asteroids.engine.Engine;
import com.gamesbykevin.asteroids.manager.Manager.*;
import com.gamesbykevin.asteroids.menu.CustomMenu.*;
import com.gamesbykevin.asteroids.menu.option.*;

public final class Options extends Layer implements LayerRules
{
    public Options(final Engine engine) throws Exception
    {
        //the layer will have the given transition and screen size
        super(Layer.Type.SCROLL_HORIZONTAL_WEST_REPEAT, engine.getMain().getScreen());
        
        //this layer will have a title at the top
        setTitle("Options");
        
        //set the background image of the Layer
        setImage(engine.getResources().getMenuImage(MenuImage.Keys.OptionBackground));
        
        //what is the duration of the current layer
        setTimer(new Timer(Timers.toNanoSeconds(10000L)));
        
        //should we force the user to view this layer
        setForce(false);
        
        //when the layer is complete should we transition to the next or pause
        setPause(true);
        
        //since there are options how big should the container be
        setOptionContainerRatio(RATIO);
        
        Audio audio = engine.getResources().getMenuAudio(MenuAudio.Keys.OptionChange);
        
        //add options
        super.add(OptionKey.Difficulty, new Difficulty(audio));
        super.add(OptionKey.Lives,      new Lives(audio));
        super.add(OptionKey.Mode,       new Mode(audio));
        
        super.add(OptionKey.FullScreen,             new FullScreen(audio));
        super.add(OptionKey.GoBack,                 new OptionsGoBack());
    }
}