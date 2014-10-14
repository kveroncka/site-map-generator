package com.bondora;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class HTMLParser {

    public static Set<String> uniqueLinks = new HashSet<String>();
    public static String rootURL = "http://bondora.com";
    public static int loanWordCount = 0;
    public static int investWordCount = 0;
    public static int priceCount = 0;
    public static BigDecimal avgPrice = BigDecimal.ZERO;

    public static void main(String[] args) throws IOException {

        PrintWriter writer = new PrintWriter("bondora-site-map.txt", "UTF-8");

        Link rootLink = createRootLink(rootURL);
        rootLink.printSiteMap("", true, writer);
        rootLink.printAveragePricePerPage(writer);

        writer.println("Count of word loan: " + loanWordCount);
        writer.println("Count of word invest: " + investWordCount);
        writer.println(("Average price for all pages: " + avgPrice.divide(new BigDecimal(priceCount), 2, RoundingMode.HALF_UP)));
        writer.close();

        rootLink.writeXMLFile(Integer.toString(loanWordCount), Integer.toString(investWordCount), avgPrice.toString());
    }

    private static Link createRootLink(String url) throws IOException {

        Link rootLink = new Link(url);

        Document doc = getPageSource(url);
        findPrice(doc, rootLink);
        findWordsCount(doc);

        List<String> childUrls = findChildUrls(url);

        for (String childUrl : childUrls) {
            rootLink.addChildLink(createRootLink(childUrl));
        }

        return rootLink;
    }

    private static List<String> findChildUrls(String url) throws IOException {

        List<String> childUrls = new ArrayList<>();

        for (Element link : getPageSource(url).select("a[href]")) {
            final String absUrl = link.absUrl("href");
            if (isValidUrl(absUrl)) {
                childUrls.add(absUrl);
            }
        }
        return childUrls;
    }

    private static Document getPageSource(String url) throws IOException {
        return Jsoup.connect(url).get();
    }

    private static void findPrice(Document doc, Link link) {
        int priceCountPerPage = 0;
        Scanner s = new Scanner(doc.text());
        link.setTitle(doc.title());
        BigDecimal sum = BigDecimal.ZERO;
        BigDecimal price = BigDecimal.ZERO;
        while (s.hasNext()) {
            String next = s.next();
            if (!price.equals(BigDecimal.ZERO) && !next.equals("years") && !next.equals("%")) {
                sum = sum.add(price);
            }
            price = BigDecimal.ZERO;
            try {
                price = new BigDecimal(next.replaceAll(",", "."));
                priceCountPerPage++;
                priceCount = priceCount + priceCountPerPage;
            } catch (NumberFormatException e) {
                // nothing to do, invalid value
            }
        }
        if (priceCountPerPage > 0) {
            avgPrice = avgPrice.add(sum);
            link.setAcgPerPage(sum.divide(new BigDecimal(priceCountPerPage), 2, RoundingMode.HALF_UP));
        }
    }

    private static void findWordsCount(Document doc) {
        Scanner s = new Scanner(doc.text());
        while (s.hasNext()) {
            String next = s.next();
            if (next.equals("loan")) {
                loanWordCount++;
            }
            if (next.equals("invest")) {
                investWordCount++;
            }
        }
        s.close();
    }

    private static boolean isValidUrl(String url) {

        if (url.isEmpty() || url.contains("?") || url.charAt(url.length() - 1) == '#' || url.charAt(url.length() - 1) == '/') {
            return false;
        }
        if (!url.contains("www.bondora")) {
            return false;
        }
        return uniqueLinks.add(url);
    }

}
