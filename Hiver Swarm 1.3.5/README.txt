<-Instructions on how to enable the various optional features included in the mod, must be enabled before a new game start->

By default the Hiver faction is unplayable but if you follow these steps you will be able to play as a Hiver with a fully functional market. You will be on good standing with the Hivers however the faction itself will remain enemies with all other factions. As the player you will have some factions that are neutral to favorable as well as several enemies. 

Each step is modular and it is up to you what level of functionality you would like. For example. if you just want to flag them as playable so they show up on random core world mode but want them still hidden on the intel tab and don't care for the portraits being used then just enable the first change.
--------------------------------------------------------------------------------------------------------------------
First off, use Notepad++ or a java complier. If you make changes with standard notepad or some other editor it may mess with the Java code.

From the mods tab in you Starsector folder go to Hiver Swarm\data\config\exerelinfactionConfig\HIVER.json and open it with your editor. Navigate to line 3 "startingFaction":false and set it to true, and save. Now go to Hiver Swarm\data\campaign and open rules.csv and delete lines 27-31 see below spoiler for the lines to delete if you are unsure:

hiver_cmsn_askForCommissionOpt_disable,PopulateOptions,"$isPerson
Nex_Commission personCanGiveCommission
!Nex_Commission hasFactionCommission
$faction.id == HIVER","SetEnabled cmsn_askCommission false
SetTooltip cmsn_askCommission ""The Hivers do not take in outsiders.""",,,
--------------------------------------------------------------------------------------------------------------------
To enable the Hiver faction to show on the intel tab: Go to Hiver Swarm\data\world\factions\HIVER.faction and open it with your editor on line 14 you will see "showInIntelTab":false,  change the false, to true, and save the file.
--------------------------------------------------------------------------------------------------------------------
To enable the Hiver playable portraits shown below when you make a new character: Go to Hiver Swarm\data\world\factions\ - now the tricky part, you will see a file named xxplayer.faction - rename that to player.faction
--------------------------------------------------------------------------------------------------------------------
The mod comes with two optional files that make Ships and Weapons purchasable and available at the Arms Dealer for players that play as Hivers or just want them at the Arms Dealer. In order to enable this feature;

-Find a file named purchasable_ship_data.csv located in data/hulls - If you delete the current ship_data.csv and rename the optional file (purchasable_ship_data.csv) to ship_data.csv it will make all Hiver ships purchasable and available at the Arms Dealer
	Essentially you are deleting the original "ship_Data.csv" then renaming the "purchasable_ship_data.csv" optional file to "ship_Data.csv" to replace it

-Find a file named purchasable_weapon.data_csv located in data/weapons. If you delete the current weapon_data.csv and rename the optional file (purchasable_weapon.data_csv) to weapon_data.csv it will make all Hiver weapons purchasable and available at the Arms Dealer
	Essentially you are deleting the original "weapon_Data.csv" then renaming the "purchasable_weapon_data.csv" optional file to "weapon_Data.csv" to replace it
--------------------------------------------------------------------------------------------------------------------
The mod comes with three optional files that make Ships, wings and Weapons unrecoverable for players that prefer not to have Hiver Technology in their fleets. In order to enable this feature;

-Find a file named unrecoverable_ship_data.csv located in data/hulls - If you delete the current ship_data.csv and rename the optional file (unrecoverable_ship_data.csv) to ship_data.csv it will make all Hiver ships and their BP unrecoverable

-Find a file named unrecoverable_weapon.data_csv located in data/weapons. If you delete the current weapon_data.csv and rename the optional file (unrecoverable_weapon.data_csv) to weapon_data.csv it will make all Hiver weapons unrecoverable

-Find a file named unrecoverable_wings.data_csv located in data/hulls. If you delete the current wing_data.csv and rename the optional file (unrecoverable_wing.data_csv) to wing_data.csv it will make all Hiver wings unrecoverable
--------------------------------------------------------------------------------------------------------------------
The mod comes with an optional file that lowers weapon prices to base vanilla levels. Good for players who do not want high value loot and/or expensive manufacturing costs

-Find a file named vanilla_weapon.data_csv located in data/weapons. If you delete the current weapon_data.csv and rename the optional file (vanilla_weapon.data_csv) to weapon_data.csv it will make all Hiver weapons base value set to vanilla standard
--------------------------------------------------------------------------------------------------------------------
To disable their blueprints from dropping in salvage or as loot

-Find a file named special_items.csv located in data/campaign and delete,rename or move it - select the file named nodrop_special_items.csv in the folder, rename it to special_items.csv and you are good to go
--------------------------------------------------------------------------------------------------------------------
The mod comes with an all mechanical looking alternate ship and weapon pack for players that prefer not to have bugs on their screen.To install the alternate pack:

Unpack the Replacement Ships.rar found in the main folder over your Hiver Swarm folder and accept overwrite. This will install both the ship and weapon pack. If you only want one or the other then you would first need to unzip into a temporary folder, delete the subfolders you do not want (data/hulls, data/variants and graphics/ships to delete the ships or data/weapons and graphics/weapons to delete the weapons then re-zip and install as above or just move the folders manually if desired
--------------------------------------------------------------------------------------------------------------------
The mod comes with an AI made Hi-resolution looking alternate portrait pack for players that prefer not to have bugs on their screen.To install the alternate pack:

Unpack the Hi Rez.zip found in the main folder over your Hiver Swarm folder and accept overwrite. This will overwrite the current portraits and install the new portrait pack. 
--------------------------------------------------------------------------------------------------------------------
Instructions to manually adjust the difficulty level of the mod

There are a couple different ways to manually modify the mod:

Go into the mod folder data\config\exerelinFactionConfg and open a file called HIVER.json with an editor. If you do not have one you can download notepad++ it is free and easy to use and automatically recognizes Java code.
Once you have that file open and ready for editing, navigate to:
	Line 21 "invasionPointMult":1.25 and lower the number to lessen their invasion fleets. If set to 0 it will disable their invasions all together.
	Line 58 "colonyExpeditionChance":1.75 determines their chance to sending a colony mission. Lower it and they will be less likely to be picked by Nexerelin to start a colony.

You can also open data\world\factions and open HIVER.faction the same way:
At lines 259-261 the composition of their fleets are set to max so lower those 5's and it will effect the strength of their fleets and officers.
At line 264 "aggression":5, determines how aggressive the faction will be in invasions and expansion and 5 is the maximum. Lower that and they will be less aggressive.
At line 327 "canBombard":true, enables their ability to saturation bomb, set that to false and they will no longer.

If you feel uncomfortable making these changes manually I included an optional ZIP in the main folder called Vplus.zip that has preconfigured settings for a vanilla plus gameplay with few faction mods. 
Just extract Vplus.zip into the mods folder and overwrite the main files.
	-The files in Vplus.zip lower their aggression, halves the invasion and colony fleet frequency, lowers their officer & ship quality, the number and size of ships in a fleet, and turns off saturation bombing.
NOTE: If you want to revert back to the base settings you can either make backups for those files first or you could just reinstall the mod.

There is an optional Hard Mode that increases Hiver ship and wing stats by 20% but makes them unrecoverable due the OP nature of the change:
First navigate to data\hulls and delete the hulls folder. In the mod folder you will see a ZIP labeled "Hard Mode hulls" and extract that into the data folder, essentially replacing the former hulls folder you just deleted.
Note: This can be done mid game but only newly generated Hiver ships will have the new hull mod

Lastly, there is sadly no way to specifically delay Hiver invasions but before you start a new game you can go to the "mod settings" button that LunaLib adds on the top left of the game menu. 
Scroll down to the Nexerelin button, select the "fleet and battles" tab and look for an option "invasion grade period". The default is set to 90 which equates to 90 in game days until any invasions occur. 
You can use that slide bar on the right and increase it up to 1825 days which is 5 years. Enter the # of days you feel you need to prepare before invasions occur.
NOTE: this will effect ALL faction invasions, not just Hivers. For example, if you set it to say 365 days it will delay ALL faction invasions for 1 in-game year after game start. 
--------------------------------------------------------------------------------------------------------------------
To enable them to appear in Nexerelin's random core world setting: 

When you start a new game and get to the screen to enable random core worlds, go to option 4)faction settings then 1)Enable/disable factions. A screen will appear on the right and depending on how may faction mods you have installed you may have to scroll down but look for HIVER. It will be disabled by default, just press the Enabled button and go back. The game will remember this setting for this game only and you will need to do this each time you start a new RCW game.

You can enable them for RCW permanently but be aware this will also make them a starting faction so do not do this in a normal Corvus sector game.
From the mods tab in you Starsector folder go to Hiver Swarm\data\config\exerelinfactionConfig\HIVER.json and open it with your editor. Navigate to line 3 "startingFaction":false and set it to true, and save.

A note about adding Hiver Swarm to your Nexerelin game that has Random Core Worlds enabled:
I do not recommend enabling them in RCW because Hivers may be a threat in the early game but due to a lack of trading partners, random planet/placement negatively affecting the Hiver economy, and everyone being at war with them basically permanently, the Hiver faction will become starved of resources, begin to lose worlds and not be much of a threat to anyone. On the positive side, it makes them a source of easy to obtain valuable salvage and some early game XP.


