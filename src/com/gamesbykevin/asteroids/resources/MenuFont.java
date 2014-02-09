package com.gamesbykevin.asteroids.resources;

import static com.gamesbykevin.asteroids.resources.Resources.RESOURCE_DIR;
import com.gamesbykevin.framework.resources.FontManager;

/**
 *
 * @author GOD
 */
public class MenuFont extends FontManager
{
    //location of resources
    private static final String DIRECTORY = "font/menu/{0}.ttf";
    
    //description for progress bar
    private static final String DESCRIPTION = "Loading Menu Font Resources";
    
    public enum Keys
    {
        Menu
    }
    
    public MenuFont() throws Exception
    {
        super(RESOURCE_DIR + DIRECTORY, Keys.values());
        
        //the description that will be displayed for the progress bar
        super.setDescription(DESCRIPTION);
    }
}