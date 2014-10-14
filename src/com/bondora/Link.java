package com.bondora;
import java.io.File;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class Link {

    private String url;
    private String text;
    private String title;
    private BigDecimal avgPerPage = BigDecimal.ZERO;
    private List<Link> childLinks = new ArrayList<>();

    public Link(String link) {
        this.url = link;
    }

    public String getUrl() {
        return url;
    }

    public void writeXMLFile(String loan, String invest, String price) {

        try {
            DocumentBuilder docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();

            // root elements
            Document doc = docBuilder.newDocument();
            Element rootElement = doc.createElement("urlset");
            doc.appendChild(rootElement);

            rootElement.setAttribute("loanwordcount", loan);
            rootElement.setAttribute("investWordCount", invest);
            rootElement.setAttribute("avgprice", price);

            createChildNodes(doc, rootElement, this);

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(new File("bondora-site-map.xml"));

            // Output to console for testing
            // StreamResult result = new StreamResult(System.out);

            transformer.transform(source, result);
            System.out.println("File saved!");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createChildNodes(Document doc, Element rootElement, Link link) {
        Element linkNode = doc.createElement("url");
        rootElement.appendChild(linkNode);

        Element locNode = doc.createElement("loc");
        locNode.appendChild(doc.createTextNode(link.getUrl()));
        linkNode.appendChild(locNode);

        Element titleNode = doc.createElement("title");
        titleNode.appendChild(doc.createTextNode(link.getTitle()));
        linkNode.appendChild(titleNode);

        Element avgPricePerPage = doc.createElement("avgpriceperpage");
        avgPricePerPage.appendChild(doc.createTextNode(link.getAvgPerPage().toString()));
        linkNode.appendChild(avgPricePerPage);


        for (Link childLink : link.getChildLinks()) {
            createChildNodes(doc, linkNode, childLink);
        }
    }

    public void printSiteMap(String prefix, boolean isTail, PrintWriter fileName) {
        fileName.println(prefix + (isTail ? "└── " : "├── ") + url);

        for (Iterator<Link> iterator = childLinks.iterator(); iterator.hasNext();) {
            Link link = iterator.next();
            if (iterator.hasNext()) {
                link.printSiteMap(prefix + (isTail ? "    " : "│   "), false, fileName);
            } else {
                link.printSiteMap(prefix + (isTail ? "    " : "│   "), true, fileName);
            }
        }
    }

    public void printAveragePricePerPage(PrintWriter fileName){
        fileName.println(url + " Average price per page: " + avgPerPage);
        for (Iterator<Link> iterator = childLinks.iterator(); iterator.hasNext();) {
            Link link = iterator.next();
            link.printAveragePricePerPage(fileName);
        }
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public List<Link> getChildLinks() {
        return childLinks;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void addChildLink(Link link) {
        childLinks.add(link);
    }

    @Override
    public String toString() {
        return url;
    }

    public BigDecimal getAvgPerPage() {
        return avgPerPage;
    }

    public void setAcgPerPage(BigDecimal sumPerPage) {
        this.avgPerPage = sumPerPage;
    }

}
