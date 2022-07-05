## Summary
The TownyQuests fork fixes a bunch of issues I found with CommunityQuests while also giving a new direction to this questing plugin.

## Features
### Auto Quest
Automatically starts quests which run on a timer for a configurable period of time. The AutoQuest algorithm considers three things when making "random" quest selection:
Previously active quest which AutoQuest started, whether a proposed auto quest selection is already running through manual activation on the server, and whether a quest is on the auto quest exempt list in configuration. The algorithm will do it's best to make the most reasonable decision about which quests it should automatically activate so you will not have to worry about anything but the configuration file.

### Rewards Rework
One flaw I noticed about the CommunityQuests plugin was that competitive quests were always going to be better than collaborative quests because the competitive quest reward distribution algorithm would be significantly more generous than the collaboritive one. I decided to introduce a new algorithm which would take the top X amount of players (configurable) on the leaderboard when the quest would end, and distribute the configured scalable (money, experience etc.) rewards between only this group of people. I found that this made much more sense. However, the configuration still allows you to use the default mode if you prefer it.

### PlaceholderAPI Support
Want to display the active quest which is currently being controlled by AutoQuest in TAB or a scoreboard? This work allows you to do just that, all messages related are configurable. Simply use %quests_message% to display the active auto quest message (or none active message if no quest is running)

### Donation Quest Rework
The donation menu used to donate to quests which require item donation was very flawed and buggy. Shift clicking into this menu would not donate the items you shift click, and if you did this, all other players viewing the donation menu would be able to see YOUR items in their donation menu (the menu was shared between everyone). I went and completely reworked this menu and you are now able to donate multiple stacks of items at once - it is a very flexible menu as you will find.

### Other
Some of the other bugs/additions this fork fixes include:
- timeToCompleteComp and timeToCompleteCoop are now able to be configured for each quest. The original "timeToComplete" value is no longer valid. I figured that sometimes you want shorter/longer periods of time for different event types. If you configure timeToCompleteCoop but not timeToCompleteComp, if a competitive quest starts, it will be assumed that it continues indefinitely until the quest is over and vice versa. 
- StopGUI now displays whether a running quest was auto quest activated or manually activated.
- StartGUI now displays completeTimeCoop and completeTimeComp.
- Multi line descriptions now supported. For this reason you will have to change your configurations a bit (test configuration shown in messages.yml file of this repository for reference on how to do this).
- RGB support added for all configurable messages (&#HEX_CODE)
- Fixed donate algorithm sometimes not allowing people to donate for competitive quests.
- Fixed bug where rewards/chat would spam indefinitely if a quest was forcefully stopped using /cq stop.
- A ton of other tiny bug fixes I found along the way.

## Commands
Refer to the CommunityQuests spigot page, but note that the command has been edited to /townyquests instead of /communityquests (I made this fork for someone who hired me and they wanted it that way sorry, so all the user interface related stuff is renamed).

## Permissions
Refer to the CommunityQuests spigot page, but note that instead of communityquests.donate for example, it would be townyquests.donate.
