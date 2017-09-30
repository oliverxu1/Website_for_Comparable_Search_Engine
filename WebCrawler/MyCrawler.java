import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;
import com.opencsv.CSVWriter;
import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.url.WebURL;


public class MyCrawler extends WebCrawler {

	private final static Pattern FILTERS = Pattern.compile(".*(\\.(css|js|mp3|zip|gz|xml))$");
	Set<String> set = new HashSet<>();
	
	@Override
	public void onUnexpectedStatusCode(String urlStr, int statusCode, String contentType, String description) {
		if (!set.contains(urlStr)) {
			try {
				CSVWriter fetchWriter = new CSVWriter(new FileWriter("C:/Desktop/fetch.csv", true));
				fetchWriter.writeNext(new String[]{urlStr, "" + statusCode});
				fetchWriter.close();
			} catch (IOException e) { 
				e.printStackTrace();
			}
			set.add(urlStr);
		}
	}
	
	/**
	 * This method receives two parameters. The first parameter is the page in
	 * which we have discovered this new url and the second parameter is the new
	 * url. You should implement this function to specify whether the given url
	 * should be crawled or not (based on your crawling logic). In this example,
	 * we are instructing the crawler to ignore urls that have css, js, git, ...
	 * extensions and to only accept urls that start with
	 * "http://www.ics.uci.edu/". In this case, we didn't need the referringPage
	 * parameter to make the decision.
	 */
	@Override
	public boolean shouldVisit(Page referringPage, WebURL url) {
		String href = url.getURL().toLowerCase();
		String indicator = !FILTERS.matcher(href).matches() && href.startsWith("http://www.usc.edu/") ? "a" : "b";
		try {
			CSVWriter urlsWriter = new CSVWriter(new FileWriter("C:/Desktop/urls.csv", true));
			urlsWriter.writeNext(new String[]{href, indicator});
			urlsWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		String preurl = referringPage.getWebURL().getURL();		
		int status = referringPage.getStatusCode();
		if (!set.contains(preurl)) {
			try {
				CSVWriter fetchWriter = new CSVWriter(new FileWriter("C:/Desktop/fetch.csv", true));
				fetchWriter.writeNext(new String[]{preurl, "" + status});
				fetchWriter.close();
			} catch (IOException e) { 
				e.printStackTrace();
			}
			set.add(preurl);
		}		
		return !FILTERS.matcher(href).matches() && href.startsWith("http://www.usc.edu/");
	}

	/**
	 * This function is called when a page is fetched and ready to be processed
	 * by your program.
	 */
	@Override
	public void visit(Page page) {
		String url = page.getWebURL().getURL();
		int status = page.getStatusCode();
//		System.out.println("URL: " + url);
//		int status = page.getStatusCode();
//		System.out.println("Status: " + status);
		String[] type = page.getContentType().split(";");
//		System.out.println("Type: " + type[0]);
		
		if (!set.contains(url)) {
			try {
				CSVWriter fetchWriter = new CSVWriter(new FileWriter("C:/Desktop/fetch.csv", true));
				fetchWriter.writeNext(new String[]{url, "" + status});
				fetchWriter.close();
			} catch (IOException e) { 
				e.printStackTrace();
			}
			set.add(url);
		}		
		
		if (page.getParseData() instanceof HtmlParseData) {
			HtmlParseData htmlParseData = (HtmlParseData) page.getParseData();
			String html = htmlParseData.getHtml();
			Set<WebURL> links = htmlParseData.getOutgoingUrls();
			
			System.out.println("Html length: " + html.length());
			System.out.println("Number of outgoing links: " + links.size());
			try {
				CSVWriter visitWriter = new CSVWriter(new FileWriter("C:/Desktop/visit.csv", true));
				visitWriter.writeNext(new String[]{url, "" + html.length(), "" + links.size(), type[0]});
				visitWriter.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {			
			try {
				CSVWriter visitWriter = new CSVWriter(new FileWriter("C:/Desktop/visit.csv", true));
				visitWriter.writeNext(new String[]{url, "" + page.getContentData().length, "" + 0, page.getContentType()});				
				visitWriter.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}