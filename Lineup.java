import java.io.IOException;

import java.util.HashMap;
import java.util.Scanner;
import java.util.Set;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.Collections;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Comment;
import org.jsoup.select.Elements;

public class Lineup {
	
	// ranking among all 2018 playoffs lineups according to net rating per 100 posessions
	private int playoffs2018LineupsRank;
	
	// Map each player in lineup to a Jsoup Document linked to his unique webpage on basketball-reference
	private Map<String, Document> players;
	
	// team of lineup
	private String team;
	
	private String season;
	
	// array containing data on lineup
	private double[] statistics;
	
	// single HTML parsing object eligible for use with any Element, Node, Document, etc.
	private HtmlToPlainText pt;
	
	// constructor takes in first Element from a row of data
	// containing statistics for the lineup that row represents
	public Lineup(Element lineupData) throws IOException {
		players = new HashMap<String, Document>();
		statistics = new double[16]; // 16 distinct statistics per lineup on webpage
		pt = new HtmlToPlainText();
		
		int nodeSiblingIndex = 0;
		// exactly 20 Elements in lineupData (known through HTML inspection)
		for (Element e : lineupData.children()) {
			String text = pt.getPlainText(e);
			//System.out.println(text + "\n" + nodeSiblingIndex +  "\n");
			switch (nodeSiblingIndex) {
				case 0: playoffs2018LineupsRank = Integer.parseInt(text);
						break;
				case 1: parsePlayers(new Scanner(text));
						break;
				case 2: team = text;
						break;
				case 3: season = text;
						break;
				case 4: case 5: case 6: case 7:
				case 8: case 9: case 10: case 11:
				case 12: case 13: case 14: case 15:
				case 16: case 17: case 18: case 19:
						statistics[nodeSiblingIndex - 4] = Double.parseDouble(text);
						break;
				default: System.out.println("Something went wrong!!!"); // 20 child nodes per lineup (known through HTML inspection)
						break;
			}
			nodeSiblingIndex++;
		}
	}
	
	// Given a Scanner connected to the text of the lineup on the webpage,
	// iterate through the text and map each player to a Jsoup document
	// linked to his specific webpage on Basketball-Reference
	private void parsePlayers(Scanner s) throws IOException {
		int numPlayersPerLineup = 5;
		int numTokensPerPlayer = 4; // known through HTML inspection
		for (int player = 0; player < numPlayersPerLineup; player++) {
			StringBuilder name = new StringBuilder();
			StringBuilder link = new StringBuilder();
			for (int token = 0; token < numTokensPerPlayer; token++) {
				if (player == 4 && token == 3) // no fourth token for last player
					break;
				String temp = s.next();
				if (token == 0 || token == 1) // first name or last name
					name.append(temp);
				else if (token == 2) {
					link.append(temp);
					link.deleteCharAt(0); // delete initial '<'
					link.deleteCharAt(link.length() - 1); // delete '>' at end
				}
				// fourth token is simply a separator between players
			}
			Document doc = Jsoup.connect(link.toString()).get();
			
			// uncomment the treasured data we want to scrape from Basketball-Reference
			// so we can actually use it (why even is it commented out at all?!?)
			// MANY THANKS TO Kai Sternad for this bit of code (lines 101-106 inclusive),
			// taken from https://stackoverflow.com/questions/20747333/how-to-uncomment-html-tags-using-jsoup
			List<Comment> comments = findAllComments(doc);
			for (Comment c : comments) {
				String data = c.getData();
				c.after(data);
				c.remove();
			}
			players.put(name.toString(), doc);
		}
		s.close();
	}
	
	// MANY THANKS TO Kai Sternad for this bit of code, taken from:
	// https://stackoverflow.com/questions/20747333/how-to-uncomment-html-tags-using-jsoup
	private List<Comment> findAllComments(Document doc) {
        List<Comment> comments = new ArrayList<Comment>();
        for (Element element : doc.getAllElements()) {
            for (Node n : element.childNodes()) {
                if (n.nodeName().equals("#comment")) {
                    comments.add((Comment)n);
                }
            }
        }
        return Collections.unmodifiableList(comments);
    }
	
	public Set<String> getPlayers() {
		return players.keySet();
	}
	
	public Document getPlayerDoc(String player) {
		if (players.containsKey(player))
			return players.get(player);
		else {
			System.out.println(player + " is not listed in the data. Null value returned");
			return null;
		}
	}
	
	// Really, the below two methods are just wrapper functions for Jsoup Document functions
	
	// Find and return a specific statistic on a specific player in this Lineup,
	// given the Element ID of that statistic Element's parent and the index of its
	// place in the list of children Elements.
	// This is extra work, I know, but for some reason getElementById() wouldn't work
	// for me when I called it on certain leaves (i.e. statistics) of the greater Element "tree" structure
	public double getPlayerStatByParentElement(String playerName, String parentElementID,
			int siblingIndex) throws IOException {
		
		if (!players.containsKey(playerName))
			throw new IllegalArgumentException(playerName + " isn't in this Lineup!");
		Document doc = players.get(playerName);
		Element e = doc.getElementById(parentElementID);
		double stat = Double.parseDouble(pt.getPlainText(e.child(siblingIndex)));
		return stat;
	}
	
	// Another way of grabbing a particular stat for a particular player,
	// based on the proper HTML attribute and value, only this prints
	// it rather than returning a value
	public void printPlayerStatByAttribute(String playerName, String attributeName,
			String attributeValue) throws IOException {
		
		if (!players.containsKey(playerName))
			throw new IllegalArgumentException(playerName + " isn't in this Lineup!");
		Document doc = players.get(playerName);
		Elements e = doc.getElementsByAttributeValue(attributeName, attributeValue);
		for (Element f : e)
			System.out.println(pt.getPlainText(f));
	}
	
	public int getRank() {
		return playoffs2018LineupsRank;
	}
	
	public String getTeam() {
		return team;
	}
	
	public double[] getStatistics() {
		return statistics;
	}
	
	// override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Lineup\n");
		Set<String> playerNames = players.keySet();
		sb.append("[");
		for (String s : playerNames) {
			sb.append(s);
			sb.append(", ");
		}
		sb.delete(sb.length() - 2, sb.length() - 1);
		sb.append("]");
		sb.append("\nTeam: ");
		sb.append(team);
		sb.append("\n");
		return sb.toString();
	}
	
}
