# NBA-2018-Playoffs-Lineup-Trends
Simple data scraping and analysis on lineups from the 2018 NBA playoffs

Primary website used for scraping:
https://www.basketball-reference.com/play-index/tiny.fcgi?id=jw1tU

This is essentially something of a mini library wrapper tool I made for myself that parses data from the above url and computes/displays some simple statistical summaries on a few of the statistics of my choosing (usage rates, assist rates, positional tracking). 

HtmlToPlainText.java is NOT my work; I borrowed it from here: https://github.com/jhy/jsoup/blob/master/src/main/java/org/jsoup/examples/HtmlToPlainText.java and am very grateful to user jhy. LineupTrends.java is the main class and Lineup.java supports it as a custom Object storing data on each lineup that is parsed. There's also 15-20 lines of code I borrowed in Lineup.java that points itself out via commenting (Thank you Kai Sternad!), which I found when searching for a way to uncomment the source HTML code containing the data I wanted to scrape. I'm not ~positive~ that comments are what was preventing me from scraping the data initially, but uncommenting it seemed to do the trick.

I used the SelectorGadget extension on Chrome to find queries for specific HTML Elements, and then used those queries to scrape via Jsoup. I tried to point out everytime I use a seemingly arbitrary SelectorGadget query with something like "// known through HTML inspection" but I may have missed a couple things. Maven was used for the Jsoup dependency.

If you're a hooper and are actually interested in the results of this tool, check out the output file for the what happens when the code is run as it stands now. Feel free to uncomment as you please between lines 22-25 of LineupTrends.java for diffrent statistical output, or (more interestingly) see what you can do yourself with the Lineup.java wrapper.
