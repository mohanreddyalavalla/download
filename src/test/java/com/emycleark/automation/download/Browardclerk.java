package com.emycleark.automation.download;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.net.URLEncodedUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import io.github.bonigarcia.wdm.WebDriverManager;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;

public class Browardclerk {
	
	static WebDriverWait wait;
	static WebDriver driver;
	static ResponseSpecification responseSpec = null;
	static String caseNumber = null;
	static String href = null;
	static String caseId = null;
	static String caseNum = null;


	public static void main(String[] args) throws InterruptedException, URISyntaxException {
		WebDriverManager.chromedriver().setup();
		driver = new ChromeDriver();
		driver.manage().window().maximize();
		driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(60));
		driver.get("https://www.browardclerk.org");
		wait = new WebDriverWait(driver, Duration.ofSeconds(10));
		wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//a[@title='Case Search Quick Link']"))).click();
		wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//a[contains(text(),'Business Name')]"))).click();
		Thread.sleep(5000);
		wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//input[@id='BusiName']"))).sendKeys("American Integrity");
		wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//input[@id='filingDateOnOrAfterB']"))).sendKeys("01/01/2021");
		wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//input[@id='filingDateOnOrBeforeB']"))).sendKeys("10/06/2022");
		wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(By.xpath(
				"//iframe[starts-with(@title, 'reCAPTCHA') and starts-with(@src, 'https://www.google.com/recaptcha')]")));
		//wait.until(ExpectedConditions.elementToBeClickable(By.id("recaptcha-anchor"))).click();
		driver.switchTo().defaultContent();
		wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//button[@id='BusinessSearchResults']"))).click();
		Thread.sleep(10000);
		  Map<String, String> caseNumbers = null;
		String getTotalItems = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//div[@data-role='pager']/span"))).getText();
		String [] temp = getTotalItems.split("of");
        String [] temp1 = temp[1].trim().split(" ");
        int length = Integer.parseInt(temp1[0]);
        caseNumbers = new HashMap<String, String>();
    	List<WebElement> caseNumbersEle = driver.findElements(By.xpath("//a[@class='anchor-tag-darklink']"));
		for(WebElement e:caseNumbersEle) {
			href = e.getAttribute("href");
			caseNumber = e.getText();
			caseNumbers.put(caseNumber, href);
			List<NameValuePair> params = URLEncodedUtils.parse(new URI(href), Charset.forName("UTF-8"));
			for (NameValuePair param : params) {
				System.out.println(param.getName() + " : " + param.getValue());
				caseNumbers.put(param.getName(), param.getValue());
			}
			RequestSpecification requestSpec = RestAssured.given().baseUri("https://www.browardclerk.org");
			requestSpec.header("Origin", "https://www.browardclerk.org");
			requestSpec.header("Referer", "https://www.browardclerk.org");
			requestSpec.header("Accept", "*/*");
			requestSpec.header("Content-Type", "text/html");
			Response response = requestSpec.with().queryParam("caseid", caseId).queryParam("caseNum", caseNum).queryParam("category", "CV")
					.queryParam("accessLevelCodeOUT", "ANONYMOUS")
					.queryParam("ato", "D")
					.get("/Web2/CaseSearchECA/CaseDetail");
			System.out.println("Status Code :- "+response.getStatusCode());
			Document html = Jsoup.parse(response.asPrettyString());
			System.out.println(html);
			Element comp = null;
			String downloadUrl = null;
			Elements allElements = html.select("a[href*=/Doc");
			
		}


	}

}
