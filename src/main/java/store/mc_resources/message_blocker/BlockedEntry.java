/*
 * Message Blocker
 * Copyright (C) 2020  MC-Resources <https://mc-resources.store>
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package store.mc_resources.message_blocker;

import java.util.Optional;

import org.bukkit.entity.Player;

import lombok.Getter;
import net.md_5.bungee.api.ChatColor;

@Getter
public final class BlockedEntry {

    private final String message;
    private final CheckMode mode;
    private final boolean ignoreCase;
    private final Optional<String> bypassPermission;

    public BlockedEntry(String message, CheckMode mode, boolean ignoreCase, Optional<String> bypassPermission) {
	this.message = ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', message));
	this.mode = mode;
	this.ignoreCase = ignoreCase;
	this.bypassPermission = bypassPermission;
    }

    public boolean isBlocked(Player player, String message) {
	if (this.bypassPermission.isPresent() && player.hasPermission(this.bypassPermission.get())) {
	    return false;
	}

	String blocked = this.message;

	if (ignoreCase) {
	    blocked = blocked.toLowerCase();
	    message = message.toLowerCase();
	}

	switch (mode) {
	case CONTAINS:
	    return message.contains(blocked);
	case EXACT:
	    return message.equals(blocked);
	case STARTS_WITH:
	    return message.startsWith(blocked);
	case ENDS_WITH:
	    return message.endsWith(blocked);
	default:
	    return false;
	}
    }

}
