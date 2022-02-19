<!-- latest begin -->

### 1.3.3
- Fixed Hotkey assigment on Forge 
- Added hints UIs from ~80 mods 

<!-- latest end -->


<!-- rest begin -->

### 1.3.2
- Added blacklist for Auto Refill
- Some fixes in the Hints system and edit UI

### 1.3.1
- Fixed "button locations not saving in some cases"

### 1.3.0
- Added in game Overlay Editor. If IPN buttons overlap with another mod UI you can now move them. Also allows you to influence how IPN detects the UI, for example as a chest to just as player inventory.
- Fixed Profiles switching with hotkeys.
- Fixed Auto Refill clashing with Profiles switching.
- Added new setting to allow Auto Refill to use items from locked slots.


### 1.2.4
- improved compatibility with Inventorio, now sorting works in the extended inventory and the buttons are not overlaping the extra slots.
- added visual and sound alerts when tools run out of durability and are replaced or when there is no replacement tool. Check the "Auto Refill" configuration for more information.
- All Realms worlds will now share configurations for locked slots and profiles. It's impossible to reliably detect the Realms world.


### 1.2.3
- workaround for REI tooltip rendering incompatibility.

### 1.2.2 - just a hotfix
- fixed typo.
- removed a debug dump that looked like it's something important.
- added forge 1.15.2 version

### 1.2.1
- fixes throw matching/all not dropping all expected items.
- "fixed" handling of overstacked items. Now we will just leave them in place.
- forge 1.18 support looks stable enough
- fabric 1.14 gets an update because of the first fix. If no other major problems are found this will be the last update.


### 1.2.0
- added support for Loyalty enchanted items. Throwing a loyalty trident will not trigger refill to allow the thrown one to return to the same slot.
- new auto refill feature. You can now allow non-enchanted items to break if their max durability is below a preset value.
- native 1.18 version.

### 1.1.9
- fixed locked slots behaviour when a chest or other container is opened and swipe drop/move is used.
- native 1.18 version.

# 1.14 END OF LIVE ANNOUNCEMENT

1.14 gets an update because I don't want to leave it with a buggy major feature.
This is the last release of Inventory Profiles Next for Minecraft 1.14.x

### 1.1.8
- it is now possible to disable all user actions for locked slots. It becomes very practical when used in conjunction with "Quick Disable" hotkey.
- fixed locked slots behaviour when a chest or other container is opened.
- rearranged the config screen. Now "Locked Slots" and "Auto Refill" have their own pages. However, the settings to enable or disable them are still in "Mod Settings"
- the configuration file has changed. The old one will automatically be converted but if you downgrade all settings from "Locked Slots" and "Auto Refill" will be lost.


### 1.1.7
- fixed crash when opening the wiki link and config folder.
- fixes incompatibility with MaliLib where sometimes this mod key bindings will stop working.
- fixes the version checker which didn't display the new version message.

### 1.1.6
- fixed crash on 1.17.
- 1.18 is now separate build and 1.17 builds are no longer compatible with 1.18

### 1.1.5
- the hotbar now shows the locked slots.
- new setting (enabled by default) to disable drop for locked slots containing non-stackable items.
  prevents accidental drops of gear.
- fixed "move all" when key binding is set to "ctrl + alt + q"
- some changes under the hood
- added updates check. The check is done once you enter a word/connect ot a server and message is displayed if there is new version available.

<!-- rest end -->

### 1.1.4
- added 1.15+ and 1.14.1+ versions. Just to be on par with the old mod
- the fabric 1.16 version now runs on all minor versions

### 1.1.3
- fixed java 8 incompatibility for 1.16.5 builds

### 1.1.2
- brand new Simplified Chinese translation thanks to @PVWXX
- profiles now support empty slots. For example in one profile you have shield in the offhend slot and another profile want it empty. For items moved out in this case an attempt will be made to move them in a free locked slot.<br>
  Just add the slot name without items in the configuration. 
- added full auto crafting. Just hold shift+alt while clicking on the crafting slot.
- fixed sort in column and row buttons to. They were swapped.
- fixed forge server crash.
- fixed forge client complaining if IPN is not installed on the server. 
- made handling of ignored screens and inventories more robust.

### 1.1.1
- fixed client crash on Forge 1.17.x
- fixed ignored container types handling
- probably fixed server crash on forge when the mod is installed. **NOTE:** Don't install it on servers it doesn't do anything.


### 1.1.0
* Added API for integration with other mods. It is now possible for other mods to:
  * mark their screens to be ignored so IPN won't try to interact with them.
  * give hints to IPN where to put its UI.
  * there is a configuration file that can provide the above information if the for some reason it's not possible for the other mod to provide in via the API. Entries in the default configuration file will be accepted in case official refusal from the other mod to provide integration or if the other mod is abandoned.

### 1.0.3
- fixes a problem where, on some servers, items were moved out of the locked slots on dimension change.

### 1.0.2
- fixes forge 1.17.1 mixin support

### 1.0.1
- fixes situations where the hot bar becomes inaccessible when opening double chest.
- fixed enable picking up items in empty locked slots in single player on some platforms.

### 0.9.0
- Added equipment Profiles! You can now define sets of equipment and easily switch between them with a press of a button. Read more on how to set it up at [Profiles Documentation](https://inventory-profiles-next.github.io/profiles/)
- Added support for over stacked items like [Carpet's](https://github.com/gnembon/fabric-carpet) stacking of empty shulkers
- Fixed problem on Windows if the world name contains special characters.
- Fixed Continuous crafting. Now it's 100% reliable.
- All non-vanilla storages like Dank storage and Backpack will now be ignored by default.
- Removed all "teaks" that might be considered cheating. Like disabling lava fog and various cooldowns. We never made them work in 1.17.x anyway.
- Visit our new site [Inventory Profiles Next](https://inventory-profiles-next.github.io/) where you can find documentation about all the advanced features. 

### Known Issues in Forge 1.17.1 due to missing MixIn support 
- Preventing swipe moving of locked slots doesn't work.
- Preventing item pickups in empty locked slots falls back to the server method i.e. items are picked into the inventory and then thrown out.

### 0.8.6
- Fixed issue with Forge mixin handling that causes incompatibility with [Timeless and Classics](https://www.curseforge.com/minecraft/mc-mods/timeless-and-classic) and [I18nUpdateMod](https://www.curseforge.com/minecraft/mc-mods/i18nupdatemod), and possibly others

### 0.8.5
- Fixed problem in Forge mods that made sorting impossible if the inventory/container has more than 1 enchanted item with same enchants, same item type, and same name and items were arranged in specific way.
- Added hotkey for reloading the mod configurations. It's recommended that it's only used while defining new custom rules and kept unset in other situations.

### 0.8.4
- Fixes locked slots behaviour when changing dimensions/worlds. There is new config that allows the user to control how many client ticks need to pass before the locked slot keeper is reinitialised on dimension/world change. This wait is necessary because the inventory might be empty when the player is spawned in the new dimension/world and we need to wait to receive the inventory content from the server.

### 0.8.3
- Now on servers also items will not be picked into empty locked slots. NOTE! Doesn't work while inventory is open. It's not possible to distinguish between player moving items and server initiated changes.
- Added configuration to disable mod buttons on non-vanilla screens. For better compatibility with othe mods

### 0.8.2
- Added per server locked slots. Enabled by default. Can be disabled in the configuration.

### 0.8.1
- Added support for single click/press inventory throw. Hotkey can be configured in Hotkeys. Disabled by default. This obeys the same rules for hotbar as "move all". 
- Fixed Shift+Click Ctrl+Q skipping locked slots to work on servers. 
- Clarified, in the tooltip, that not picking up items in locked slots works only in single player. Support for servers will require service side mod.
- We now have a discord server **[![2][2]][1]**

### 0.8.0
- Shift+Click Ctrl+Q can now be disabled and enabled (default) for locked slots. This also applies for swipe variant of the actions.
- SINGLE PLAYER ONLY. Items will not be picked into empty locked slots

## Known issues
- When UI scaling is more than 2 or in case of tiny game windows the config screen might not show the anchor header correctly.
- If you make the game window 0x0 pixels while the configuration screen is open, sometimes the game crashes. 


[1]: https://discord.gg/23YCxmveUM
[2]: https://img.shields.io/discord/861171785897738240?label=Discord&logo=discord&style=plastic


