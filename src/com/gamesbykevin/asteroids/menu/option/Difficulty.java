package com.gamesbykevin.asteroids.menu.option;

import com.gamesbykevin.framework.menu.Option;
import com.gamesbykevin.framework.resources.Audio;

/**
 * The setup of this specific option
 * @author GOD
 */
public final class Difficulty extends Option
{
    private static final String TITLE = "Difficulty \"# of meteors\": ";
    
    public enum Selections
    {
        Easy("Easy"),
        Medium("Medium"),
        Hard("Hard");
        
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
    
    public Difficulty(final Audio audio)
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