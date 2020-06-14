# Waypoint Beacons

This is a simple beacon based, rpg-like waypoint system. It is my first attempt at a Paper plugin and Java--so feel free to edit and optimize it.

Feel free to take the source code, jar files are available for download under releases.

## Requirements
- Java and Paper 1.15.2 API are required. (May work on future versions)

**Requirements for compiling**

1. AnvilGUI, made by WesJD
1. Maven
    1. Used for compiling and shading
1. Minecraft Development Plugin

## Commands
The commands are not perfect, but there are some for server operators.

Spaces in the names of waypoints are replaced with "%_". 
>(Ex: Name of beacon is "Inside Treehouse", internal name is "Inside%_Treehouse")

/waypoints
1. settings *(Dangerous, can break the plugin)* `Changes a value in the config file`
    1. Path In Config File
    1. Desired Value
    1. Value type (boolean/integer/string)
1. forceName `Forces a new name to a beacon`
    1. Original Name
    1. Desired Name
1. forcePublic *(Dangerous, name must be exact)* `Forces a beacon to become public`
    1. Name of Beacon
1. forcePrivate *(Dangerous, name must be exact)* `Forces a beacon to become private`
    1. Name of Beacon
1. appendPrivate `Adds a player to the list of players able to see the provided beacon`
    1. Name of Beacon
    1. Name of Player (Player must have played before and the name must be the last seen name)
1. removePrivate `Removes a player from the list of players able to see the provided beacon`
    1. Name of Beacon
    1. Name of Player (Player must have played before and the name must be the last seen name)
1. forceDisplay `Forces a new block to be displayed in the paged menu for the provided beacon`
    1. Name of Beacon
    1. Name of Material (Bukkit material, not minecraft)
1. remove `Removes a beacon entirely, does not break block`
    1. Name of Beacon
1. giveBlock `Gives the waypoint beacon block to the provided player`
    1. Name of Player

## Features
The plugin, again, is quite simple, but has some basic features for players to use:

- Nameable Beacons
- Configurable representative block in menu
- Private/Public Beacons
- Operator commands
- Adjustable Recipe
- Configurable Radius

##
Made with ItelliJ IDEA CE, using Minecraft Development Plugin.

Thanks for checking this out. Have fun!
- Gabriel (Obverser)
