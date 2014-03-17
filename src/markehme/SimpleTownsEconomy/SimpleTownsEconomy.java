/**
  * Copyright (C) 2014  Mark Hughes <mark@markeh.me>
  *
  *  This program is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU Affero General Public License as published
  *  by the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU Affero General Public License for more details.
  *
  *  You should have received a copy of the GNU Affero General Public License
  *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  *
  */

package markehme.SimpleTownsEconomy;

import java.io.IOException;
import java.util.logging.Level;

import markehme.SimpleTownsEconomy.extras.Metrics;
import markehme.SimpleTownsEconomy.extras.Metrics.Graph;
import net.milkbowl.vault.economy.Economy;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * SimpleTownsEconomy is a simple plugin for adding economy related
 * features to SimpleTowns.
 * 
 * GitHub: https://github.com/MarkehMe/SimpleTowns-Economy
 * 
 * @author Mark Hughes <mark@markeh.me>
 *
 */
public class SimpleTownsEconomy extends JavaPlugin {
	
	static FileConfiguration config;
	
	private static JavaPlugin plugin;
	
	private static Economy economy = null;
	
	private static Metrics metrics = null;
	@Override
	public void onEnable() {
		saveDefaultConfig();
		
		config = getConfig();
		plugin = this;
		
		RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration( net.milkbowl.vault.economy.Economy.class );
		if(economyProvider != null) {
			economy = economyProvider.getProvider();
		} else {
			log("Vault could not find a permission provider, do you have Vault?");
		}
		
        
		try {
			
			metrics = new Metrics(this);
			
            Graph SimpleTownsVersion = metrics.createGraph("SimpleTowns Version");
	        
            SimpleTownsVersion.addPlotter(new Metrics.Plotter(Bukkit.getPluginManager().getPlugin("SimpleTowns").getDescription().getVersion()) {

                @Override
                public int getValue() {
                    return 1;
                }
            });
            
			metrics.start();
			
		} catch (IOException e) {
			
			log("Metrics failed to not start up: " + e.getMessage());
			
		}
	}
	
	/**
	 * Simply used to fetch the plugins configuration file 
	 * @return
	 */
	public static FileConfiguration getconfig() {
		return(config);
	}
	
	/**
	 * Reloads the configuration file
	 */
	public static void doReload() {
		config = plugin.getConfig();
		log("Economy settings re-read from config");
	}
	
	/**
	 * Attempts to charge a player, and returns a boolean with the status
	 * @param player
	 * @param amount
	 * @return
	 */
	public static boolean chargePlayer(Player player, Double amount) {
		if(economy.has(player.getName(), amount)) {
			economy.withdrawPlayer(player.getName(), amount);
			return true;
		}
		return false;
	}
	
	/**
	 * Refunds a player an amount. 
	 * @param player
	 * @param amount
	 */
	public static void refundPlayer(Player player, Double amount) {
		economy.depositPlayer(player.getName(), amount);
	}
	
	/**
	 * Logs to console as INFO 
	 * @param msg
	 */
	public static void log(String msg) {
		plugin.getLogger().log(Level.INFO, msg);
	}
	
	/**
	 * Notifys a player they were charged 
	 * @param player
	 * @param amount
	 */
	public static void notifyPlayer(int pass, Player player, Double amount) {
		if(pass == 1) {
			player.sendMessage(config.getString("Message.charged").replace("<amount>", amount.toString()));
		} else if(pass == 0) {
			player.sendMessage(config.getString("Message.chargefail").replace("<amount>", amount.toString()));
		} else if(pass == 2) {
			player.sendMessage(config.getString("Message.refunded").replace("<amount>", amount.toString()));
		}
	}
 }
