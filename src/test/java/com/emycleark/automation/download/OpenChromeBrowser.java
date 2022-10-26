package com.emycleark.automation.download;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.edge.EdgeDriver;

import io.github.bonigarcia.wdm.WebDriverManager;

public class OpenChromeBrowser {

	public static void main(String[] args) {
		WebDriver driver = null;
		// TODO Auto-generated method stub
	try {
		WebDriverManager.chromedriver().setup();
	   driver = new ChromeDriver(); // google
		driver.get("https://www.google.com");
		System.out.println(10/2);
	   System.out.println(driver.getTitle());
	  
	}catch(Exception e) {
		e.printStackTrace();
	}finally {
	   
		driver.quit();
	}
	


	}

}
