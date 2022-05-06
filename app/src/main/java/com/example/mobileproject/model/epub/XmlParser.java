package com.example.mobileproject.model.epub;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class XmlParser {
    public XmlParser() {
    }

    public BookDetails parseEpubDetails(File xmlFile){
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        String bookPublisher = "";
        String bookAuthor = "";
        String bookName = "";
        String bookCoverLink = "";

        try {

            // optional, but recommended
            // process XML securely, avoid attacks like XML External Entities (XXE)
            //dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);

            // parse XML file
            DocumentBuilder db = dbf.newDocumentBuilder();

            Document doc = db.parse(xmlFile);

            // optional, but recommended
            // http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
            doc.getDocumentElement().normalize();

            // get details
            NodeList list = doc.getElementsByTagName("metadata");

            for (int temp = 0; temp < list.getLength(); temp++) {

                Node node = list.item(temp);

                if (node.getNodeType() == Node.ELEMENT_NODE) {

                    Element element = (Element) node;

                    // get text
                    bookPublisher = element.getElementsByTagName("dc:publisher").item(0).getTextContent().replace("'", "");
                    bookAuthor = element.getElementsByTagName("dc:creator").item(0).getTextContent();
                    bookName = element.getElementsByTagName("dc:title").item(0).getTextContent().replace("'", "");
                }
            }

            // get bookCoverLink
            list = doc.getElementsByTagName("item");

            for (int temp = 0; temp < list.getLength(); temp++) {

                Node node = list.item(temp);

                if (node.getNodeType() == Node.ELEMENT_NODE) {

                    Element element = (Element) node;

                    if(element.getAttribute("id").equals("cover")){
                        bookCoverLink = element.getAttribute("href");
                        break;
                    }
                }
            }

            return new BookDetails(bookName, bookAuthor, bookPublisher, bookCoverLink);

        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
        }

        return new BookDetails();
    }

    public ArrayList<EpubChapter> parseChapterList(File xmlFile){
        ArrayList<EpubChapter> chapterList = new ArrayList<>();
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

        try {

            // optional, but recommended
            // process XML securely, avoid attacks like XML External Entities (XXE)
            //dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);

            // parse XML file
            DocumentBuilder db = dbf.newDocumentBuilder();

            Document doc = db.parse(xmlFile);

            // optional, but recommended
            // http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
            doc.getDocumentElement().normalize();

            // get <staff>
            NodeList list = doc.getElementsByTagName("navPoint");


            for (int temp = 0; temp < list.getLength(); temp++) {

                Node node = list.item(temp);

                if (node.getNodeType() == Node.ELEMENT_NODE) {

                    Element element = (Element) node;

                    // get text
                    String chapterName = element.getElementsByTagName("navLabel").item(0).getTextContent()
                            .replace("\n", "")
                            .trim();
                    String chapterLink = ( (Element) element.getElementsByTagName("content").item(0) ).getAttribute("src");

                    chapterList.add(new EpubChapter(chapterName, chapterLink));
                }
            }
        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
        }

        return chapterList;
    }
}
