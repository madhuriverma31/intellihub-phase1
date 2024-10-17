package com.selenium.project;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PageSourceExample {

    public static void main(String[] args) {
        // Setup WebDriverManager to handle ChromeDriver setup automatically
        WebDriverManager.chromedriver().setup();

        // Initialize the Chrome WebDriver
        WebDriver driver = new ChromeDriver();

        try {
            // Navigate to the desired URL
            driver.get("https://www.wikipedia.org/");

            // Get the page source (HTML content)
            String pageSource = driver.getPageSource();

            // Print the page source (optional)
            System.out.println("Page Source: \n" + pageSource);

            // Extract locators from the page source
            List<String[]> locators = extractLocators(pageSource);

            // Write the full page source to a CSV file
            writePageSourceToCSV(pageSource);

            // Write the locators to an Excel file
            writeLocatorsToExcel(locators);

        } finally {
            // Close the browser
            driver.quit();
        }
    }

    /**
     * Extract locators from the page source HTML.
     *
     * @param pageSource The HTML page source as a string.
     * @return A list where each entry contains locator attributes.
     */
    private static List<String[]> extractLocators(String pageSource) {
        // Parse the HTML with JSoup
        Document document = Jsoup.parse(pageSource);
        Elements elements = document.select("*"); // Select all elements

        // Use an ArrayList to dynamically store locators
        List<String[]> locatorsList = new ArrayList<>();
        Set<String> processedXpaths = new HashSet<>(); // To track unique XPath entries

        // Loop through each element and extract locators
        for (Element element : elements) {
            String id = element.id();
            String classNames = element.className();
            String name = element.attr("name");
            String tagName = element.tagName();
            String xpath = getXPathWithId(element);
            String cssSelector = getCssSelector(element);

            // Prepare a row for ID if it exists
            if (!id.isEmpty()) {
                locatorsList.add(new String[]{id, "", tagName, classNames, xpath, cssSelector, "", ""});
            }

            // Prepare a row for Name if it exists
            if (!name.isEmpty()) {
                locatorsList.add(new String[]{"", name, tagName, classNames, xpath, cssSelector, "", ""});
            }

            // Prepare a row for all other elements with their details
            if (!processedXpaths.contains(xpath)) {
                locatorsList.add(new String[]{"", "", tagName, classNames, xpath, cssSelector, "", ""});
                processedXpaths.add(xpath); // Add to set to prevent duplicates
            }
        }
        return locatorsList;
    }

    /**
     * Generate a locator in the format //tagname[@id=""] for the given element.
     *
     * @param element The JSoup Element to generate XPath for.
     * @return The generated XPath as a string.
     */
    private static String getXPathWithId(Element element) {
        StringBuilder xpath = new StringBuilder();
        String tagName = element.tagName();

        // Add the tag name with //
        xpath.append("//").append(tagName);

        // Check for ID attribute
        if (!element.id().isEmpty()) {
            xpath.append("[@id='").append(element.id()).append("']");
        }

        // Optionally, add other attributes for a more specific XPath
        String name = element.attr("name");
        if (!name.isEmpty()) {
            xpath.append("[@name='").append(name).append("']");
        }

        return xpath.toString();
    }

    /**
     * Generate a CSS selector for the given element.
     *
     * @param element The JSoup Element to generate the CSS selector for.
     * @return The generated CSS selector as a string.
     */
    private static String getCssSelector(Element element) {
        StringBuilder cssSelector = new StringBuilder();
        String tagName = element.tagName();

        // Add tag name
        cssSelector.append(tagName);

        // Add ID if it exists
        if (!element.id().isEmpty()) {
            cssSelector.append("#").append(element.id());
        }

        // Add class if it exists
        if (!element.className().isEmpty()) {
            cssSelector.append(".").append(element.className().replace(" ", "."));
        }

        return cssSelector.toString();
    }

    /**
     * Write the full page source to a CSV file.
     *
     * @param pageSource The page source to write.
     */
    private static void writePageSourceToCSV(String pageSource) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("PageSource.csv"))) {
            // Write page source to CSV file
            writer.write("Page Source,");
            writer.newLine();
            writer.write(pageSource.replace("\n", "").replace("\r", "")); // Remove line breaks for CSV
            System.out.println("Page source written to PageSource.csv successfully.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Write the extracted locators to an Excel file.
     *
     * @param locators A list of locators to write.
     */
    private static void writeLocatorsToExcel(List<String[]> locators) {
        try (Workbook workbook = new XSSFWorkbook(); // Create workbook in try-with-resources
             FileOutputStream fileOut = new FileOutputStream("Locators.xlsx")) {

            Sheet sheet = workbook.createSheet("Locators");

            // Create header row
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("ID");
            headerRow.createCell(1).setCellValue("Name");
            headerRow.createCell(2).setCellValue("TagName");
            headerRow.createCell(3).setCellValue("ClassName");
            headerRow.createCell(4).setCellValue("XPath");
            headerRow.createCell(5).setCellValue("CSS Selector");
            headerRow.createCell(6).setCellValue("LinkText");
            headerRow.createCell(7).setCellValue("PartialLinkText");

            // Write locators to Excel file
            for (int i = 0; i < locators.size(); i++) {
                String[] locator = locators.get(i);
                Row row = sheet.createRow(i + 1);
                for (int j = 0; j < locator.length; j++) {
                    row.createCell(j).setCellValue(locator[j]); // Write all locator attributes
                }
            }

            // Write to file
            workbook.write(fileOut);
            System.out.println("Locators written to Locators.xlsx successfully.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}