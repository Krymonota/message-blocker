# Message Blocker
Block annoying chat messages from the Minecraft vanilla server, plugins or players.


## About
This plugin allows you to block almost all types of chat messages. 
This is especially useful for messages that are sent by the Minecraft server or plugins and that cannot be disabled.

[A use case for this plugin would be the "Respawn point set" message that was introduced with Minecraft 1.15,
which is sent to the player when he clicks on a bed.](https://www.spigotmc.org/threads/bed-spawn-message.415189/)

## Installation & [Configuration](/src/main/resources/config.yml)
Put this plugin into the Spigot plugins folder, start/restart the server and make sure to properly configure this plugin.

The configuration is quite flexible and simple at the same time. 
You can specify not only whole messages, but also parts of messages. 
In addition, you can specify whether the message should only be blocked if it's sent 1:1 to the player,
or if it's sufficient if it only occurs in parts. You can also toggle the case sensitivity.
It's also possible to specify a permission that a player needs to be able to see a blocked message regardless.
```yaml
blocked:
  # Blocked if message equals 'Message Blocker' (case-insensitive).
  - message: 'Message Blocker'
    mode: 'exact'
    ignore_case: true
    bypass_permission:
  # Blocked if message contains 'blockedmessage' (case-sensitive)-
  - message: 'blockedmessage'
    mode: 'contains'
    ignore_case: false
    bypass_permission:
  # Blocked if message contains 'staff-only message' (case-insensitive) and
  # the player doesn't have permission to the /say command.
  - message: 'staff-only message'
    mode: 'contains'
    ignore_case: true
    bypass_permission: 'minecraft.command.say'
```
Tip: Use the `/tellraw <player> <message>` command to test your configuration (e. g. `/tellraw @p "Message Blocker"`).


## Support me
If you like this plugin and would like to support my work, you're welcome to send me a small tip.

[![Send me a small tip](https://www.paypalobjects.com/en_US/DK/i/btn/btn_donateCC_LG.gif)](https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=73SKSSM7XA6U6&source=url)