package com.github.DarkSeraphim.EnderForge.menu;

/**
 *
 * @author DarkSeraphim
 */
public class Slot 
{
    String action;
    String parameter;
    
    protected Slot(String action, String parameter)
    {
        this.action = action;
        this.parameter = parameter;
    }
    
    public String getAction()
    {
        return this.action;
    }
    
    public String getParameter()
    {
        return this.parameter;
    }
}
