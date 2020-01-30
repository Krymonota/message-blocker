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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import io.netty.channel.Channel;
import net.md_5.bungee.api.chat.BaseComponent;
import store.mc_resources.message_blocker.lib.com.comphenix.tinyprotocol.Reflection;
import store.mc_resources.message_blocker.lib.com.comphenix.tinyprotocol.TinyProtocol;
import store.mc_resources.message_blocker.lib.com.comphenix.tinyprotocol.Reflection.FieldAccessor;
import store.mc_resources.message_blocker.lib.com.comphenix.tinyprotocol.Reflection.MethodInvoker;

public final class MessageBlocker extends JavaPlugin {

    private static final Class<?> packetPlayOutChatClass = Reflection.getClass("{nms}.PacketPlayOutChat");
    private static final Class<?> iChatBaseComponentClass = Reflection.getClass("{nms}.IChatBaseComponent");
    private static final FieldAccessor<?> chatBaseComponentField = Reflection.getField(packetPlayOutChatClass, iChatBaseComponentClass, 0);
    private static final FieldAccessor<BaseComponent[]> componentsField = Reflection.getField(packetPlayOutChatClass, BaseComponent[].class, 0);
    private static final MethodInvoker chatBaseComponentGetTextMethod = Reflection.getTypedMethod(iChatBaseComponentClass, "getText", String.class);
    private static MethodInvoker iChatBaseComponentGetSiblingsMethod;

    static {
	String iChatBaseComponentGetSiblingsMethodName = null;

	for (Method method : iChatBaseComponentClass.getMethods()) {
	    if (!List.class.isAssignableFrom(method.getReturnType())) {
		continue;
	    }

	    if (method.getParameterCount() != 0) {
		continue;
	    }

	    iChatBaseComponentGetSiblingsMethodName = method.getName();
	    break;
	}

	if (iChatBaseComponentGetSiblingsMethodName != null) {
	    iChatBaseComponentGetSiblingsMethod = Reflection.getTypedMethod(iChatBaseComponentClass, iChatBaseComponentGetSiblingsMethodName, List.class);
	}
    }

    private final List<BlockedEntry> blockedEntries = new ArrayList<BlockedEntry>();

    @Override
    public void onEnable() {
	this.saveDefaultConfig();

	if (iChatBaseComponentGetSiblingsMethod == null) {
	    this.getLogger().log(Level.SEVERE, "Unable to find siblings method for IChatBaseComponent. Your Minecraft server version might be unsupported by this plugin.");

	    Bukkit.getServer().getPluginManager().disablePlugin(this);

	    return;
	}

	this.loadBlockedEntries();

	new TinyProtocol(this) {

	    @Override
	    public Object onPacketOutAsync(Player receiver, Channel channel, Object packet) {
		if (!packetPlayOutChatClass.isInstance(packet)) {
		    return super.onPacketOutAsync(receiver, channel, packet);
		}

		Optional<String> text = Optional.empty();
		final BaseComponent[] components = componentsField.get(packet);

		if (components != null) {
		    System.out.println(components);
		    text = Optional.ofNullable(BaseComponent.toPlainText(components));
		} else {
		    final StringBuilder textBuilder = new StringBuilder();
		    final Object iChatBaseComponent = chatBaseComponentField.get(packet);
		    final List<?> iChatBaseComponentList = (List<?>) iChatBaseComponentGetSiblingsMethod.invoke(iChatBaseComponent);

		    if (iChatBaseComponentList.isEmpty()) {
			textBuilder.append((String) chatBaseComponentGetTextMethod.invoke(iChatBaseComponent));
		    } else {
			for (Object iChatBaseComponentEntry : iChatBaseComponentList) {
			    final String part = (String) chatBaseComponentGetTextMethod.invoke(iChatBaseComponentEntry);

			    if (part != null) {
				textBuilder.append(part);
			    }
			}
		    }

		    text = Optional.of(textBuilder.toString());
		}

		if (text.isPresent()) {
		    for (BlockedEntry entry : MessageBlocker.this.blockedEntries) {
			if (entry.isBlocked(receiver, text.get())) {
			    return null;
			}
		    }
		}

		return super.onPacketOutAsync(receiver, channel, packet);
	    }

	};
    }

    @SuppressWarnings("unchecked")
    private void loadBlockedEntries() {
	this.getConfig().getList("blocked").forEach(obj -> {
	    if (!(obj instanceof Map<?, ?>)) {
		return;
	    }

	    try {
		final Map<String, Object> entry = (Map<String, Object>) obj;

		final String message = (String) entry.getOrDefault("message", "");

		if (message.isEmpty()) {
		    return;
		}

		final CheckMode mode = CheckMode.valueOf(((String) entry.get("mode")).toUpperCase());
		final boolean ignoreCase = (boolean) entry.getOrDefault("ignore_case", true);
		final String permission = (String) entry.get("bypass_permission");

		this.blockedEntries.add(new BlockedEntry(message, mode, ignoreCase, Optional.ofNullable(permission)));
	    } catch (Exception e) {
		this.getLogger().log(Level.SEVERE, "Failed to read a blocked entry from the configuration file!", e);
	    }
	});

	this.getLogger().log(Level.INFO,
		"Loaded " + this.blockedEntries.size() + " blocked entries from the configuration file.");
    }

}
