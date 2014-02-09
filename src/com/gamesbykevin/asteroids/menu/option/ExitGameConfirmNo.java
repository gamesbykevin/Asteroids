package com.gamesbykevin.asteroids.menu.option;

import com.gamesbykevin.asteroids.menu.CustomMenu;
import com.gamesbykevin.framework.menu.Option;

/**
 * The setup of this specific option
 * @author GOD
 */
public final class ExitGameConfirmNo extends Option
{
    private static final String TITLE = "No";
    
    public ExitGameConfirmNo()
    {
        //when this option is selected it will go to another layer
        super(CustomMenu.LayerKey.StartGame);
        
        super.add(TITLE, null);
    }
}