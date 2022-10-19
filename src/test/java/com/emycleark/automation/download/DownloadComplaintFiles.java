package com.emycleark.automation.download;


import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

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
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import io.github.bonigarcia.wdm.WebDriverManager;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;

public class DownloadComplaintFiles {

	static WebDriverWait wait;
	static WebDriver driver;
	static ResponseSpecification responseSpec = null;
	static String caseNumber = null;
	static String href = null;
	static String caseId = null;
	static String caseIdEnc = null;

	public static void main(String[] args) throws InterruptedException, URISyntaxException {
		WebDriverManager.chromedriver().setup();
		driver = new ChromeDriver();
		driver.manage().window().maximize();
		driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(60));
		driver.get("https://myeclerk.myorangeclerk.com");
		wait = new WebDriverWait(driver, Duration.ofSeconds(10));
		wait.until(ExpectedConditions.presenceOfElementLocated(By.linkText("Records Search"))).click();
    	wait.until(ExpectedConditions.presenceOfElementLocated(By.id("businessName"))).sendKeys("safepoint");
		wait.until(ExpectedConditions.presenceOfElementLocated(By.id("DateFrom"))).sendKeys("01/01/2021");
		wait.until(ExpectedConditions.presenceOfElementLocated(By.id("DateTo"))).sendKeys("12/31/2021");
		wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(By.xpath(
				"//iframe[starts-with(@title, 'reCAPTCHA') and starts-with(@src, 'https://www.google.com/recaptcha')]")));
		wait.until(ExpectedConditions.elementToBeClickable(By.id("recaptcha-anchor"))).click();


		driver.switchTo().defaultContent();
		wait.until(ExpectedConditions.presenceOfElementLocated(By.id("caseSearch"))).click();
		Thread.sleep(10000);
		WebElement testDropDown = wait.until(ExpectedConditions.presenceOfElementLocated(By.name("caseList_length")));
		Select showElement = new Select(testDropDown);
		showElement.selectByVisibleText("All");
    	List<WebElement> caseNumbersEle = driver.findElements(By.className("caseLink"));
	    Map<String, String> caseNumbers = null;
		ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(20);
		int i = 0;
		System.out.println("Total Number of CaseNumber: = "+caseNumbersEle.size());
		for (WebElement e : caseNumbersEle) {
			caseNumbers = new HashMap<String, String>();
			href = e.getAttribute("href");
			caseNumber = e.getText();
			caseNumbers.put(caseNumber, href);
			List<NameValuePair> params = URLEncodedUtils.parse(new URI(href), Charset.forName("UTF-8"));
			for (NameValuePair param : params) {
				System.out.println(param.getName() + " : " + param.getValue());
				caseNumbers.put(param.getName(), param.getValue());
			}
			i++;
			String caseId= caseNumbers.get("caseId");
			 String caseIdEnc= caseNumbers.get("caseIdEnc");
			executor.submit(() -> {
			downloadComplaintFiles(caseId, caseIdEnc);
			return null;
			});
		}
		List<Callable<Object>> todo = new ArrayList<Callable<Object>>(i);
		executor.invokeAll(todo);
		executor.shutdown();
		driver.quit();
	}
   
	/*
	 * downloadComplaintFiles :- This method is used to connect Endpoint and download the file
	 * @params : @caseID
	 * @params : @caseIdEnc
	 */
	public static void downloadComplaintFiles(String caseId, String caseIdEnc)  {
		RequestSpecification requestSpec = RestAssured.given().baseUri("https://myeclerk.myorangeclerk.com");
		requestSpec.header("Origin", "https://myeclerk.myorangeclerk.com");
		requestSpec.header("Referer", "https://myeclerk.myorangeclerk.com");
		requestSpec.header("Accept", "*/*");
		requestSpec.header("Content-Type", "text/html");
		Response response = requestSpec.with().queryParam("caseId", caseId).queryParam("caseIdEnc", caseIdEnc)
				.get("/CaseDetails");
		System.out.println("Status Code :- "+response.getStatusCode());
		Document html = Jsoup.parse(response.asPrettyString());
		Element comp = null;
		String downloadUrl = null;
		Elements allElements = html.select("a[href*=/Doc");
		for (Element element : allElements) {
			if (element.ownText().equalsIgnoreCase("Complaint")) {
				comp = element;
				downloadUrl = comp.attr("href");
				System.out.println(downloadUrl);
				byte[] response2 = getDownloadUrl(downloadUrl);
				downloadLocally(response2, "Complaint_"+caseId);
			}
			
			if (element.ownText().equalsIgnoreCase("Statement of Claim")) {
				comp = element;
				downloadUrl = comp.attr("href");
				System.out.println(downloadUrl);
				byte[] response2 = getDownloadUrl(downloadUrl);
				downloadLocally(response2, "Statement of Claim_"+caseId);
			}

		}
        
	}

	private static byte[] getDownloadUrl(String downloadUrl) {
		try {
			downloadUrl = java.net.URLDecoder.decode(downloadUrl, StandardCharsets.UTF_8.name());
		} catch (UnsupportedEncodingException e) {
			System.out.println(e.getMessage());
		}
		RequestSpecification requestSpec1 = RestAssured.given().baseUri("https://myeclerk.myorangeclerk.com");
		requestSpec1.header("Origin", "https://my"
				+ "eclerk.myorangeclerk.com");
		requestSpec1.header("Referer", "https://myeclerk.myorangeclerk.com");
		requestSpec1.header("Accept", "*/*");
		byte[] response1 = requestSpec1.get(downloadUrl).then().statusCode(200).extract().asByteArray();
		return response1;
	}

	private static void downloadLocally(byte[] pdfFile, String caseId) {
		FileOutputStream fos;
		try {
			fos = new FileOutputStream(System.getProperty("user.home") + "/" + "DownloadsPDF"+"/"+ caseId + ".pdf");
			fos.write(pdfFile);
			fos.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
