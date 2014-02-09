package com.gamesbykevin.asteroids.menu.option;

import com.gamesbykevin.asteroids.menu.CustomMenu;
import com.gamesbykevin.framework.menu.Option;

/**
 * The setup of this specific option
 * @author GOD
 */
public final class Options extends Option
{
    private static final String TITLE = "Options";
    
    public Options()
    {
        //when this option is selected it will go to another layer
        super(CustomMenu.LayerKey.Options);
        
        super.add(TITLE, null);
    }
}