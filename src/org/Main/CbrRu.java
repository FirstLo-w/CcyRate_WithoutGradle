package org.Main;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class CbrRu {
    private static String doGet(String address) throws CbrRuException {
        try {
            URL url = new URL(address);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            StringBuilder responseContent = new StringBuilder();
            try (
                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(
                                    connection.getInputStream(),
                                    "windows-1251"
                            )
                    )
            ) {
                if (connection.getResponseCode() != 200) {
                    throw new CbrRuException(
                            String.format("Bad HTTP code from API: %d", connection.getResponseCode())
                    );
                }

                String inputLine;
                while ((inputLine = reader.readLine()) != null) {
                    responseContent.append(inputLine);
                }

            }
            String response = responseContent.toString();
            if (response.isEmpty()) {
                throw new CbrRuException("Empty response from API");
            }
            return response;

        } catch (IOException e) {
            throw new CbrRuException(e.toString(), e);
        }
    }

    public static List<Valute> getValutes(LocalDate date) throws CbrRuException {
        try {
            final String url = "http://www.cbr.ru/scripts/XML_daily.asp?date_req=" + DateUtils.formatDate(date);
            final String response = doGet(url);

            final InputSource is = new InputSource(new StringReader(response));
            final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            final DocumentBuilder builder = factory.newDocumentBuilder();
            final Document doc = builder.parse(is);
            doc.getDocumentElement().normalize();

            final LocalDate docDate = DateUtils.parseDate(
                    doc.getDocumentElement().getAttribute("Date")
            );

            final NodeList nodeList = doc.getElementsByTagName("Valute");
            final List<Valute> valuteList = new ArrayList<>(nodeList.getLength());

            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                valuteList.add(
                        Valute.parse(docDate, (Element) node)
                );
            }

            return valuteList;

        } catch (IOException | ParserConfigurationException | SAXException e) {
            throw new CbrRuException(e.toString(), e);
        }
    }

    public static Valute getValute(LocalDate date, int ccy) throws CbrRuException {
        try {
            final String url = "http://www.cbr.ru/scripts/XML_daily.asp?date_req=" + DateUtils.formatDate(date);
            final String response = doGet(url);

            try {
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = factory.newDocumentBuilder();
                Document doc = builder.parse(new ByteArrayInputStream(response.getBytes()));

                XPathFactory xPathFactory = XPathFactory.newInstance();
                XPath xpath = xPathFactory.newXPath();

                DecimalFormat df = new DecimalFormat("000");
                String expression = "//Valute[NumCode='" + df.format(ccy) + "']";
                Node valute = (Node) xpath.evaluate(expression, doc, XPathConstants.NODE);
                if (valute != null) {
                    return Valute.parse(date, (Element) valute);
                }
                return null;
            } catch (XPathExpressionException e) {
                throw new RuntimeException(e);
            }

        } catch (IOException | ParserConfigurationException | SAXException e) {
            throw new CbrRuException(e.toString(), e);
        }
    }

    public static void main(String[] args) throws Exception {
        System.out.println(
                getValutes(DateUtils.parseDate("01.01.2025"))
        );
    }
}
