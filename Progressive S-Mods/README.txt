This is a Starsector mod that changes the way built-in hull mods work:

- You can no longer build in hull mods using story points.
- Each ship has its own XP counter and gains XP during combat.
    - Civilian ships gain a set percentage of all XP gained at the end of a battle.
    - XP can be gained during combat in the following ways:
        - Dealing the most hull or armor damage to an enemy during a combat interval
        - Taking the most shields, armor, or hull damage from an enemy during a combat interval
        - Taking or dealing any damage to or from an enemy that has taken hull or armor damage during a combat interval
    - (configurable: XP gain multiplier, civilian ship XP gain percentage, combat interval length)
- Spend a ship's XP to build in hull mods, up to the usual limit.
    - The XP cost of a hull mod depends on the ship's class and DP cost,
        as well as the hull mod's OP cost.  
    - (configurable: base XP cost as a polynomial function of the hull mod's OP cost)
- Spend story points and ship XP to increase the number of hull mods a ship can build in.
    - Cost depends on ship's class, and the XP cost increases each time this option is used.
    - (configurable: change the SP and XP cost scaling or disable this feature entirely)