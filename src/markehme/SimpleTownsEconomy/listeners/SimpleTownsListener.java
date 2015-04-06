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

package markehme.SimpleTownsEconomy.listeners;

import markehme.SimpleTownsEconomy.SimpleTownsEconomy;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import com.gmail.jameshealey1994.simpletowns.events.TownAddEvent;
import com.gmail.jameshealey1994.simpletowns.events.TownAfterReloadEvent;
import com.gmail.jameshealey1994.simpletowns.events.TownClaimEvent;
import com.gmail.jameshealey1994.simpletowns.events.TownCreateEvent;
import com.gmail.jameshealey1994.simpletowns.events.TownDeleteEvent;
import com.gmail.jameshealey1994.simpletowns.events.TownRemoveEvent;
import com.gmail.jameshealey1994.simpletowns.events.TownUnclaimEvent;

public class SimpleTownsListener implements Listener {
	
	/**
	 * Reload our config when SimpleTowns reloads theirs
	 * @param event
	 */
	@EventHandler(priority=EventPriority.LOW)
	public void onReload(TownAfterReloadEvent event) { 
		SimpleTownsEconomy.doReload();
	}
	
	/**
	 * Charge for Town creation
	 * @param event
	 */
	@EventHandler(priority=EventPriority.LOW)
	public void onTownCreate(TownCreateEvent event) {
		if(!(event.getSender() instanceof Player)) return;
		
		if(!SimpleTownsEconomy.getconfig().getBoolean("Payments.enable")) return;
				
		if(SimpleTownsEconomy.getconfig().getDouble("Payments.create") > 0 && SimpleTownsEconomy.shouldCharge((Player) event.getSender(), event.getTown())) {
			if(SimpleTownsEconomy.chargePlayer((Player) event.getSender(), SimpleTownsEconomy.getconfig().getDouble("Payments.create"))) {
				SimpleTownsEconomy.notifyPlayer("charged", (Player) event.getSender(), SimpleTownsEconomy.getconfig().getDouble("Payments.create"));
			} else {
				SimpleTownsEconomy.notifyPlayer("chargefail", (Player) event.getSender(), SimpleTownsEconomy.getconfig().getDouble("Payments.create"));
				event.setCancelled(true);
			}
		}
	}
	
	/**
	 * Charge for Town delete (for whatever reason you're doing that?!)
	 * Or, refunds
	 * @param event
	 */
	@EventHandler(priority=EventPriority.LOW)
	public void onTownDelete(TownDeleteEvent event) {
		if(!(event.getSender() instanceof Player)) return;
		
		// Payment
		if(SimpleTownsEconomy.getconfig().getBoolean("Payments.enable")) {
			if(SimpleTownsEconomy.getconfig().getDouble("Payments.delete") > 0 && SimpleTownsEconomy.shouldCharge((Player) event.getSender(), event.getTown())) {
				if(SimpleTownsEconomy.chargePlayer((Player) event.getSender(), SimpleTownsEconomy.getconfig().getDouble("Payments.delete"))) {
					SimpleTownsEconomy.notifyPlayer("charged", (Player) event.getSender(), SimpleTownsEconomy.getconfig().getDouble("Payments.delete"));
				} else {
					SimpleTownsEconomy.notifyPlayer("chargefail", (Player) event.getSender(), SimpleTownsEconomy.getconfig().getDouble("Payments.delete"));
					event.setCancelled(true);
				}
			}
		
		}
		
		// Refund
		if(SimpleTownsEconomy.getconfig().getBoolean("Refunds.enable")) {
			if(SimpleTownsEconomy.getconfig().getDouble("Refunds.delete") > 0 && SimpleTownsEconomy.shouldCharge((Player) event.getSender(), event.getTown())) {
				SimpleTownsEconomy.refundPlayer((Player) event.getSender(), SimpleTownsEconomy.getconfig().getDouble("Refunds.delete"));
				SimpleTownsEconomy.notifyPlayer("refunded", (Player) event.getSender(), SimpleTownsEconomy.getconfig().getDouble("Refunds.delete"));
			}
		}
	}
	
	/**
	 * Charge for land claim
	 * @param event
	 */
	@EventHandler(priority=EventPriority.LOW)
	public void onLandClaim(TownClaimEvent event) {
		if(!(event.getSender() instanceof Player)) return;
		
		if(!SimpleTownsEconomy.getconfig().getBoolean("Payments.enable")) return;
		Double extraCharge = SimpleTownsEconomy.getconfig().getDouble("Expenses.additionalPerChunkOwned", 0.0) * event.getTown().getTownChunks().size();
		
		if(SimpleTownsEconomy.getconfig().getDouble("Payments.claim")+extraCharge > 0 && SimpleTownsEconomy.shouldCharge((Player) event.getSender(), event.getTown())) {
			if(SimpleTownsEconomy.chargePlayer((Player) event.getSender(), SimpleTownsEconomy.getconfig().getDouble("Payments.claim")+extraCharge)) {
				SimpleTownsEconomy.notifyPlayer("charged", (Player) event.getSender(), SimpleTownsEconomy.getconfig().getDouble("Payments.claim")+extraCharge);
			} else {
				SimpleTownsEconomy.notifyPlayer("chargefail", (Player) event.getSender(), SimpleTownsEconomy.getconfig().getDouble("Payments.claim")+extraCharge);
				event.setCancelled(true);
			}
		}
	}
	
	/**
	 * Refund for land unclaim 
	 * @param event
	 */
	@EventHandler(priority=EventPriority.LOW)
	public void onLandUnclaim(TownUnclaimEvent event) {
		if(!(event.getSender() instanceof Player)) return;
		
		if(SimpleTownsEconomy.getconfig().getBoolean("Refunds.enable") && SimpleTownsEconomy.shouldCharge((Player) event.getSender(), event.getTown()) ) {
			if(SimpleTownsEconomy.getconfig().getDouble("Refunds.unclaim") > 0) {
				SimpleTownsEconomy.refundPlayer((Player) event.getSender(), SimpleTownsEconomy.getconfig().getDouble("Refunds.unclaim"));
			}
		}
	}
	
	/**
	 * Charge for adding a player 
	 * @param event
	 */
	@EventHandler(priority=EventPriority.LOW)
	public void onAddPlayer(TownAddEvent event) {
		if(!(event.getSender() instanceof Player)) return;
		
		if(SimpleTownsEconomy.getconfig().getBoolean("Payments.enable") && SimpleTownsEconomy.shouldCharge((Player) event.getSender(), event.getTown())) {
			if(SimpleTownsEconomy.getconfig().getDouble("Refunds.add") > 0) {
				if(SimpleTownsEconomy.chargePlayer((Player) event.getSender(), SimpleTownsEconomy.getconfig().getDouble("Payments.add"))) {
					SimpleTownsEconomy.notifyPlayer("charged", (Player) event.getSender(), SimpleTownsEconomy.getconfig().getDouble("Payments.add"));
				} else {
					SimpleTownsEconomy.notifyPlayer("chargefail", (Player) event.getSender(), SimpleTownsEconomy.getconfig().getDouble("Payments.add"));
					event.setCancelled(true);
				}
			}
		}
	}
	/**
	 * Charge for removing a player 
	 * @param event
	 */
	@EventHandler(priority=EventPriority.LOW)
	public void onRemovePlayer(TownRemoveEvent event) {
		if(!(event.getSender() instanceof Player)) return;
		
		if(SimpleTownsEconomy.getconfig().getBoolean("Payments.enable") && SimpleTownsEconomy.shouldCharge((Player) event.getSender(), event.getTown())) {
			if(SimpleTownsEconomy.getconfig().getDouble("Refunds.remove") > 0) {
				if(SimpleTownsEconomy.chargePlayer((Player) event.getSender(), SimpleTownsEconomy.getconfig().getDouble("Refunds.remove"))) {
					SimpleTownsEconomy.notifyPlayer("charged", (Player) event.getSender(), SimpleTownsEconomy.getconfig().getDouble("Refunds.remove"));
				} else {
					SimpleTownsEconomy.notifyPlayer("chargefail", (Player) event.getSender(), SimpleTownsEconomy.getconfig().getDouble("Refunds.remove"));
					event.setCancelled(true);
				}
			}
		}
	}
}
