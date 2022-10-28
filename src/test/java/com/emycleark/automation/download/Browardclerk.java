package com.emycleark.automation.download;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.time.Duration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.net.URLEncodedUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.interactions.Action;
import org.openqa.selenium.interactions.Actions;
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
	static String href = null;
	static String caseId = null;
	static String caseNum = null;


	public static void main(String[] args) throws InterruptedException, URISyntaxException, AWTException {
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
        
        int pagecount = length/10;
        for(int i=1;i<=pagecount;i++) {
        	List<WebElement> caseNumbersEle = driver.findElements(By.xpath("//a[@class='anchor-tag-darklink']"));
        	for(WebElement element:caseNumbersEle) {
        		href = element.getAttribute("href");
    			String caseNumber = element.getText();
    			WebElement caseElement = driver.findElement(By.xpath("//a[contains(text(),'"+caseNumber+"')]"));
    			JavascriptExecutor executor = (JavascriptExecutor)driver;
    			executor.executeScript("arguments[0].click();", caseElement);
    			//WebDriverWait wait = new WebDriverWait(driver,Duration.ofSeconds(60));
    			//WebElement flag = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//a[@title='Click to open Complaint (eFiled) document']/i")));
    			if(isElementPresent()) {
    			WebElement complaintNumber = driver.findElement(By.xpath("//a[@title='Click to open Complaint (eFiled) document']/i"));
    			JavascriptExecutor executor1 = (JavascriptExecutor)driver;
    			executor1.executeScript("arguments[0].click();", complaintNumber);
    		    String mainWindowHandle  = driver.getWindowHandle();
    		    Set<String> allwindows = driver.getWindowHandles();
    		    Iterator<String> iterator = allwindows.iterator();
    		    while(iterator.hasNext()) {
    		    	String childWindow = iterator.next();
    		    	if(!mainWindowHandle.equalsIgnoreCase(childWindow)) {
    		    		driver.switchTo().window(childWindow);
    	    	Thread.sleep(1000);
    	    	Robot robot = new Robot();
    	    	robot.keyPress(KeyEvent.VK_CONTROL);
    	    	robot.keyPress(KeyEvent.VK_S);
    	    	Thread.sleep(1000);
    	    	robot.keyRelease(KeyEvent.VK_CONTROL);
    	    	robot.keyRelease(KeyEvent.VK_S);
    	    	robot.keyPress(KeyEvent.VK_ENTER);
    	    	Thread.sleep(5000);
    	    	robot.keyRelease(KeyEvent.VK_ENTER);
    	    	Thread.sleep(10000);
    	    	driver.close();
    		    	}
    		    }
        		driver.switchTo().window(mainWindowHandle);
        		driver.navigate().back();
        	} else {
        		driver.navigate().back();
        		break;
        	}
        	}
        }
        //click on Right Arrow button to move next page
     WebElement nextPageElem = driver.findElement(By.xpath("//span[contains(text(),'Go to the next page')]"));
		JavascriptExecutor executor = (JavascriptExecutor)driver;
		executor.executeScript("arguments[0].click();", nextPageElem);
    		 
    	
		


	}
	
	
	/**
	* If a element is present, returns true, else return false
	* @param WebElement whose presence is being checked
	* @return true if webElement is present, else false
	*/
	public static boolean isElementPresent() {
	  boolean exists = false;
	  try {
		WebDriverWait wait = new WebDriverWait(driver,Duration.ofSeconds(10));
		 wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//a[@title='Click to open Complaint (eFiled) document']/i")));
	    exists = true;
	  } catch (Exception e) {
		  exists = false;
	  }
	  return exists;
	}

}
