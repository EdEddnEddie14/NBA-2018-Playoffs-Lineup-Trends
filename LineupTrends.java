import java.io.IOException;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class LineupTrends {

	public static void main(String[] args) throws IOException, ParseException {
		// represents best lineups in 2018 playoffs according to net rating,
		// minimum 48 minutes played together in playoffs
		String url = "http://bkref.com/tiny/jw1tU";
		Document doc = Jsoup.connect(url).get();
		ArrayList<Lineup> lineups = readLineups(doc);
		
		// comment or uncomment what you're interested in
		//computeLineupSummaries(lineups);
		//computeLineupSummariesByCareerStats(lineups);
		computeLineupAmbiguityIndex(lineups);
		//computeAssistRateSums(lineups);
	}
	
	// Given a Jsoup Document connected to the url in main(),
	// read in all the lineups on the page and create a Lineup object for each one
	public static ArrayList<Lineup> readLineups(Document doc) throws IOException {
		ArrayList<Lineup> result = new ArrayList<Lineup>();
		Elements allLineupData = doc.select("tr"); // known through HTML inspection
		int index = 0;
		for (Element e : allLineupData) {
			// Elements with indices 0, 1, 22, and 23 do not contain lineup data
			// (known through HTML inspection)
			if ((index > 1 && index < 22) || index > 23)
				result.add(new Lineup(e));
			index++;
		}
		return result;
		
	}
	
	// Was interested in the distribution of usage rates in lineups and whether there
	// were any trends (i.e. Did the best lineups tend to have a single ball dominant player
	// or a more balanced attack?) so I made this function to look at the usage rate
	// statistical summaries in each lineup
	public static void computeLineupSummaries(ArrayList<Lineup> lineups) throws IOException {
		HtmlToPlainText pt = new HtmlToPlainText();
		String playoffs2018AdvancedElementID = "playoffs_advanced.2018"; // found through HTML inspection
		int usageRateSiblingIndex = 18; // found through HTML inspection
		for (Lineup l : lineups) {
			System.out.println(l);
			double careerAvgUsageRate = 0;
			double avgUsageRate = 0;
			double maxUsgRate = 0;
			double minUsgRate = 100;
			for (String player : l.getPlayers()) {
				double playoffsTemp = l.getPlayerStatByParentElement(player, playoffs2018AdvancedElementID, usageRateSiblingIndex);
				Element careerElement = l.getPlayerDoc(player).selectFirst("table#advanced tfoot .right:nth-child(19)");
				double careerTemp = Double.parseDouble(pt.getPlainText(careerElement));
				if (playoffsTemp > maxUsgRate)
					maxUsgRate = playoffsTemp;
				if (playoffsTemp < minUsgRate)
					minUsgRate = playoffsTemp;
				avgUsageRate += playoffsTemp;
				careerAvgUsageRate += careerTemp;
			}
			avgUsageRate /= 5;
			careerAvgUsageRate /= 5;
			double playoffDiff = avgUsageRate - careerAvgUsageRate;
			System.out.println("Average Usage Rate In 2018 Playoffs: " + avgUsageRate);
			if (playoffDiff > 0) {
				System.out.println("Difference between lineup's average usage rate in the 2018 playoffs"
									+ " and the average career usage rate for each player in the lineup: +"
									+ playoffDiff);
			} else {
				System.out.println("Difference between lineup's average usage rate in the 2018 playoffs"
						+ " and the average career usage rate for each player in the lineup: -"
						+ playoffDiff);
			}
			System.out.println("Max Usage Rate: " + maxUsgRate);
			System.out.println("Min Usage Rate: " + minUsgRate);
			System.out.println("Range of Usage Rates: " + (maxUsgRate - minUsgRate) + "\n");
		}
	}

	// Mostly the same as above function, only this method focuses solely on the
	// career usage rates of the players in each lineup
	public static void computeLineupSummariesByCareerStats(ArrayList<Lineup> lineups) {
		HtmlToPlainText pt = new HtmlToPlainText();
		for (Lineup l : lineups) {
			System.out.println(l);
			double avgUsageRate = 0;
			double maxUsgRate = 0;
			double minUsgRate = 100;
			for (String player : l.getPlayers()) {
				Element e = l.getPlayerDoc(player).selectFirst("table#advanced tfoot .right:nth-child(19)");
				double temp = Double.parseDouble(pt.getPlainText(e));
				if (temp > maxUsgRate)
					maxUsgRate = temp;
				if (temp < minUsgRate)
					minUsgRate = temp;
				avgUsageRate += temp;
			}
			avgUsageRate /= 5;
			System.out.println("Average Career Usage Rate: " + avgUsageRate);
			System.out.println("Max Usage Rate: " + maxUsgRate);
			System.out.println("Min Usage Rate: " + minUsgRate);
			System.out.println("Range of Career Usage Rates: " + (maxUsgRate - minUsgRate) + "\n");
		}
	}

	/* 	Was interested in whether there were trends in positional flexibility of the lineups,
		as the NBA has moved to a positionless league according to many people (especially in the playoffs).
		Created the amiguityIndex variable as a simple estimate for myself, using the percentage of time
		each player spent on the floor during the 2018 playoffs at each of the five positions.
		(Basketball-Reference tracks this in their Play-by-Play (Pbp) data.)
		I just weighted each position by the percentage of time a player spent there, where point guard is 1
		and center is 5. So if all five players in a given lineup spent 100% of time on the floor at their designated
		positions, the amiguityIndex would be 15 since 1+2+3+4+5 = 15.
	*/
	public static void computeLineupAmbiguityIndex(ArrayList<Lineup> lineups) throws IOException, ParseException {
		HtmlToPlainText pt = new HtmlToPlainText();
		String playoffs2018PbpId = "playoffs_pbp.2018";
		for (Lineup l : lineups) {
			System.out.println(l);
			double ambiguityIndex = 0;
			for (String player : l.getPlayers()) {
				double positionEstimate = 0;
				int siblingIndex = 7; // position estimate for point guard would be 8
				while (siblingIndex <= 11) { // iterate over all five positions
					int actualPosition = siblingIndex - 7;
					// weight each position by percentage of time player spent there
					Element e = l.getPlayerDoc(player).getElementById(playoffs2018PbpId);
					NumberFormat defaultFormat = NumberFormat.getPercentInstance();
					String rawData = pt.getPlainText(e.child(siblingIndex));
					if (!rawData.equals("")) { // when player spends no time at a position, empty string is returned
						Number value = defaultFormat.parse(rawData);
						double fraction = value.doubleValue();
						positionEstimate += actualPosition * fraction;
						if (fraction > .2) // significant positional flexibility (based on there being five positions)
							positionEstimate *= fraction; // pretty arbitrary but oh well
					}
					siblingIndex++;
				}
				ambiguityIndex += positionEstimate;
			}
			System.out.println("Positional Ambiguity Index: " + ambiguityIndex + "\n");
		}
	}
	
	// Similar to the above functions on usage rate, I wanted to see if there was a trend
	// in the assist rates of players in lineups only at this point. I also commented out the
	// avgAssistRate variable to try to maximize the difference in the numbers I was comparing
	// and just went with the sum of the five players' assist rates
	public static void computeAssistRateSums(ArrayList<Lineup> lineups) throws IOException {
		String playoffs2018AdvancedElementID = "playoffs_advanced.2018"; // found through HTML inspection
		int assistRateSiblingIndex = 14; // found through HTML inspection
		for (Lineup l : lineups) {
			System.out.println(l);
			double assistRateSum = 0;
			 // double avgAssistRate = 0;
			for (String player : l.getPlayers()) {
				double temp = l.getPlayerStatByParentElement(player, playoffs2018AdvancedElementID, assistRateSiblingIndex);
				assistRateSum += temp;
			}
			// avgAssistRate /= 5;
			System.out.println("Sum of five players' assist rates In 2018 Playoffs: " + assistRateSum);
		}
	}
	
}
