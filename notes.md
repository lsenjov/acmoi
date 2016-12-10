
# Config Files
The simulation will run off a basic engine with .edn configuration files.

## goods
Goods detail the base goods/resources themselves. Note that goods also includes resources (arable land, housing etc)
- Name
- Bulk (how bulky the item is per minimum unit)
- Weight (how weighty the item is per minimum unit)
- Generation (possibility of it appearing in a district, maximum amount of resource if appearing [if it appears, picks a random number between 1 and the maximum). Items like buildable land might have a 1.0 chance of appearing, arable land a 0.4 chance, goldOre a 0.01 chance etc.
- NonMoveable? Can this good/resource be moved from its current region?
- NonTradeable? Can this good be traded? Used specifically for personal needs, and will be filtered as such (Mobile phones that fill the communication need, but only for the owner). NonTradeable reactions can't be stored, but can be used when satisfying Inid's needs.

## reactions
Every reaction is something an individual can perform, including transporting goods.
- Name
- Consumes (each good, per reaction)
- Produces (each good, per reaction)
- Requires (Goods required for the reaction, but are not consumed)
- Catalyst (Consumed good that increases yield/speeds production) (May not use)
- Time (man hours)
- Minimum Tech (minimum local tech factor required for the reaction)

## levels
Slightly more interesting, this details the levels the simulation operates on. Every simulation has a "universe" level at level 0, which contains all of the level 1 items. In one simulation a universe may have galaxies, which have quadrants, which have systems, which have planets. Another simulation may have a universe representing a single country, which has states, which have regions, which have cities, which have districts.

Each level has
- Name
- Level (integer from 1 to n levels)
- Transit time/cost to move to the level above (from level 1 to level 0). Note this is affected by tech level.

## inids
This file details the Inid craves. Specifically, their hierarchy of needs. What inids want and the tech level they require it.

Items can be :consume and/or :require.

Examples:
shelter TL0 (A reaction is turning housing into shelter, but requires labour (housing management). If an Inid owns housing, a separate, non-labour action creates shelter for themselves)
food TL0
communication TL3

# The model
The simulation runs off regions (or whatever they've been named in levels.edn). It starts at the universe level (level 0), then recurses into each level 1 area, then into each level 2 etc till it reaches the lowest level. It performs time checks on each entry up and down (in some simulations it may be possible for items to move multiple levels in a single tick).

## Inids
Individual idiots, the workers and consumers of the simulation. Inids have a hierarchy of needs, to be satisfied by consumes and requires.

The tech lower an item's tech level, the higher the weighting as tech goes up, equal to the difference + 1. A TL5 inid will have TL0 need weighted as a 6, and will have a TL6 need weighted as a 1. It is possible for needs to be satisfied more than 100% with needs of a slightly higher tech value, but anything more than an entire tech level is unusable by the individual.

Also note that created items are of a certain tech level. An individual is pleased by a need as a proportion of its tech level versus the user's tech level. A TL2 housing will only satisfy a TL4 Inid 50% with regard to housing. Obviously, inids will pay more for higher tech items.

### Basic AI
As to work life, once every week they will see if they have the cash on hand to start a profitable business (most efficient businesses require the purchase of expensive equipment), or possibly begin work as a trader (purchasing a vehicle). If not, they will sell their labour on the market.

If they are unable to satisfy their needs decently, and the buy price of labour is significantly higher a level up (accounting for transport), the Inid will sell their resources and book passage upward. They will continue to do this while the price of labour is higher as they go upwards. When they reach a point where the price higher is not worth the travel, they will search among the areas one level lower for the highest buy price of labour and migrate there, going downwards till they reach the lowest level where they will begin work.

If the Inid does not have enough cash to continue their journey or satisfy their needs, they will wait at their location and sell their labour to the market.

Older Inids will generally have better housing/resources, and thus be less likely to migrate.

## Traders
The only moves that can take place between levels are actors moving goods. All goods production takes place on a region level (the lowest level) and trades are calculated. Businesses on the lowest level create buy and sell orders on goods.

AI traders operate on a level boundary. They purchase their transport equipment (usually a vehicle of some kind) and look for the most profitable trade, taking into account transit costs. Generally this means they will purchase a profitable route from their current position to a different position, then calculate a new route from their end position. Keeping to the boundary ensures a limit to the amount of possible trades. This, of course, may not hold true for player AIs who may analyse and exploit entire supply chains.

As an intermediary, some traders may operate as HFTs, operating on a single level and buying/selling goods with a (probably) small margin. These traders create the bulk of buy/sell orders for other traders to operate on, adjusting prices as quantity changes. A well placed HFT that controls a market could easily create a stranglehold. There may be multiple HFTs operating on a single level.

## Tech Levels
Every item has a tech level, which is a combination of the consumed goods, required goods, and labour used to complete the reaction. Inids will pay higher prices for higher tech level goods.

TODO Figure out if floats can be used for the price calculations! Buy orders have minimum tech requirement?
