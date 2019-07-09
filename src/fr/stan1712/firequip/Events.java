package fr.stan1712.firequip;

import org.bukkit.event.Listener;

public class Events implements Listener {
    @SuppressWarnings("unused")
	private Main pl;
    
    public Events(Main pl) {
        this.pl = pl;
        pl.getConfig();
    }
	
}
