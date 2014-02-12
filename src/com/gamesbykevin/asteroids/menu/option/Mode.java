package com.gamesbykevin.asteroids.menu.option;

import com.gamesbykevin.framework.menu.Option;
import com.gamesbykevin.framework.resources.Audio;

/**
 * The setup of this specific option
 * @author GOD
 */
public final class Mode extends Option
{
    //the description
    private static final String TITLE = "Mode: ";
    
    public enum Selections
    {
        Original("Original"),
        Cooperative("Co-op"),
        Race("Race"),
        Vs("Versus");
        
        private final String desc;
        
        private Selections(final String desc)
        {
            this.desc = desc;
        }
        
        private String getDesc()
        {
            return this.desc;
        }
    }
    
    public Mode(final Audio audio)
    {
        super(TITLE);
        
        for (Selections selection : Selections.values())
        {
            super.add(selection.getDesc(), audio);
        }
        
        //default to first selection
        super.setIndex(0);
    }
}