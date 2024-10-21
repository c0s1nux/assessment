package org.example;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.Color;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AirbnbTest {

    //At first, I have tried with Dockerized Selenium (testcontainers).
    //However, this wasted me a lot of time, because I was petty and I tried fixing VNC recorder, which for some reason wasn't recording.
    //I couldn't get an idea on what I was doing wrong. (in my testcases, and I didn't want to do it blindly)
    //Therefore, I went with a simpler solution.

    private WebDriver driver;
    private WebDriverWait wait;

    @BeforeEach
    public void setUp() {
        //Setup Chrome Driver
        WebDriverManager.chromedriver().setup();
        WebElement locationInput;
        driver = new ChromeDriver();

        //Wait interval time of 10 seconds for each search.
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        //Open Airbnb website
        driver.get("https://airbnb.com");

        //Wait for DOM to load completely.
        waitForLoad(driver);

        //Search for the Location input area
        locationInput = findAndClickByXPATH("//input[@placeholder='Search destinations']", true);
        //Input location "Rome, Italy"
        locationInput.sendKeys("Rome, Italy");

        //Calculate dates: one week after today for Check-In, and one week after that for Check-Out
        LocalDate checkInDate = LocalDate.now().plusWeeks(1);
        LocalDate checkOutDate = checkInDate.plusWeeks(1);

        //Search the CheckInDateInput element and click it.
        findAndClickByXPATH("//*[contains(text(), 'Add dates')]", true);

        //Search the table data that has the correct aria-label for checkInDate and click it.
        String ariaLabel = returnAriaLabel(checkInDate);
        findAndClickByXPATH("//td[contains(@aria-label, '" + ariaLabel + "')]", true);

        //Search the table data that has the correct aria-label for checkOutDate and click it.
        String ariaLabelEnd = returnAriaLabel(checkOutDate);
        findAndClickByXPATH("//td[contains(@aria-label, '" + ariaLabelEnd +  "')]", true);

        //Search the button to add guests and click it.
        findAndClickByXPATH("//div[contains(text(), 'Who')]", true);

        //Search for the stepper that increases how many adults are participating in the trip and click it twice. (as per the assignment)
        findAndClickByXPATH("//button[@aria-describedby='searchFlow-title-label-adults' and @aria-label='increase value']", true);
        findAndClickByXPATH("//button[@aria-describedby='searchFlow-title-label-adults' and @aria-label='increase value']", true);

        //Search for the stepper that increases how many children are participating in the trip and click it once. (as per the assignment)
        findAndClickByXPATH("//button[@aria-describedby='searchFlow-title-label-children' and @aria-label='increase value']", true);

        //Search for the search button and click it (very annoying to find it, not happy with solution)
        findAndClickByXPATH("//div[@class='snd2ne0 atm_am_12336oc atm_gz_yjp0fh atm_ll_rdoju8 atm_mk_h2mmj6 atm_wq_qfx8er dir dir-ltr']", true);
        waitForLoad(driver);
    }

    //Function that makes sure the DOM is loaded completely.
    void waitForLoad(WebDriver driver) {
        new WebDriverWait(driver, Duration.ofSeconds(30)).until((ExpectedCondition<Boolean>) wd ->
        {
            assert wd != null;
            return Objects.equals(((JavascriptExecutor) wd).executeScript("return document.readyState"), "complete");
        });
    }

    //Function that capitalizes first letter and makes the others lower capital. This is for the aria-label for CheckIn/CheckOut dates.
    public static String capitalizeFirstLetter(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }

    //Instead of searching for TestIDs, we searched by the date itself in the Aria-Label, in the correct format.
    public static String returnAriaLabel(LocalDate date){
        String dayOfWeek = capitalizeFirstLetter(date.getDayOfWeek().toString());
        String month = capitalizeFirstLetter(date.getMonth().toString());
        return date.getDayOfMonth() + ", " + dayOfWeek + ", " + month + " " + date.getYear() + ".";
    }

    //Function to check for second filter, for the first date (to check the correct date was applied)
    //E.g: If the months are the same, then it will show Oct 21 - 22.
    //     If not, then the case is covered, and it will show Oct 28 - Nov 10 (random example)
    public static String returnSecondFilter(LocalDate checkInDate){
        String month = checkInDate.getMonth().toString().toUpperCase().charAt(0) + checkInDate.getMonth().toString().toLowerCase().substring(1, 3);
        int day = checkInDate.getDayOfMonth();
        LocalDate nextDate = checkInDate.plusWeeks(1);
        String secondMonth = nextDate.getMonth().toString().toUpperCase().charAt(0) + nextDate.getMonth().toString().toLowerCase().substring(1, 3);
        int secondDay = nextDate.getDayOfMonth();
        if(month.equals(secondMonth)){
            return month + " " + day + " – " + secondDay;
        }
        return month + " " + day + " – " + secondMonth + " " + secondDay;
    }

    //Function to eliminate lines of code that are very similar, and to find specific elements based on XPath.
    public WebElement findAndClickByXPATH(String xpath, boolean wantToClick){
        WebElement find;
        try{
            find = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(xpath)));
        }
        catch (TimeoutException e){
            find = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(xpath)));
        }
        if(wantToClick) {
            find.click();
        }
        return find;

    }

    //Function to skip translation popup.
    public void skipPopup(String originalWindow){
        for (String windowHandle : driver.getWindowHandles()) {
            if (!originalWindow.contentEquals(windowHandle)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
        //Here we wait for the page to load completely, and we check that by checking if the URL changed, and if yes, we are sure the popup will appear and we remove it.
        wait.until(ExpectedConditions.urlContains("rooms"));
        new Actions(driver).sendKeys(Keys.ESCAPE).perform();
    }

    @Test
    //@RepeatedTest(10)
    public void testOne() {
        WebElement firstFilter, secondFilter, thirdFilter;
        LocalDate checkInDate = LocalDate.now().plusWeeks(1);

        //Check if Rome is written also in the search minibar after searching.
        firstFilter = findAndClickByXPATH("//button[@data-index='0']//div[text()='Rome']", false);
        assertEquals("Rome", firstFilter.getText());

        //Checks if the date is correct in the search minibar.
        String secondFilterText = returnSecondFilter(checkInDate);
        secondFilter = findAndClickByXPATH("//button[@data-index='1']//div", false);
        assertEquals(secondFilterText, secondFilter.getText());

        //Checks if the total number of guests (2 adults + 1 children = 3) is correct in the search minibar.
        //All above are checks for requirement #1, test #1.
        thirdFilter = findAndClickByXPATH("//button[@data-index='2']//div", false);
        assertEquals("3 guests", thirdFilter.getText());

        List<WebElement> propertiesList = wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(By.xpath("//div[@itemprop='itemListElement']")));

        //We get the first main window, to always go back to the main page.
        String originalWindow = driver.getWindowHandle();

        //We go through each property, we click on it, wait until the link fully loads, check for text guests, split it by " ", and check if the number is bigger than 3.
        //We close the tab then focus again the main window, and we repeat.
        //This is requirement #2, test #1.
        for(WebElement property: propertiesList){
            property.click();
            skipPopup(originalWindow);
            WebElement guests;
            try{
                guests = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//li[contains(text(), 'guests')]")));
            }
            catch (TimeoutException e){
                new Actions(driver).sendKeys(Keys.ESCAPE).perform();
                guests = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//li[contains(text(), 'guests')]")));
            }
            assertTrue(Integer.parseInt(guests.getText().split(" ")[0])>=3);
            driver.close();
            driver.switchTo().window(originalWindow);
        }
    }

    @Test
    //@RepeatedTest(10)
    public void testTwo() {
        waitForLoad(driver);
        WebElement bedrooms, numberOfBedrooms, showMore, h2;
        //Test two continues by the setup and after that clicks the Filter button after finding it.
        findAndClickByXPATH("//*[contains(text(), 'Filters')]", true);

        //Here we search for the bedrooms button. Unfortunately again I am not completely happy with the implementation of the number of bedrooms finding.
        //This is used to make sure we click the buttons 5 times. Before, I had a for loop. For some reason (unknown to me) sometimes it wouldn't click correctly.
        //So, I implemented the following solution
        bedrooms = findAndClickByXPATH("//button[@aria-describedby='stepper-filter-item-min_bedrooms-row-title searchFlow-title-label-filter-item-min_bedrooms' and @aria-label='increase value']", false);
        numberOfBedrooms = findAndClickByXPATH("//div[@class='soanjzq atm_r3_1h6ojuz atm_jb_3okqs3 atm_lh_ftgil2 atm_jb_u29brm__oggzyc dir dir-ltr']", false);
        while (!numberOfBedrooms.getText().equals("5+")){
            bedrooms.click();
            numberOfBedrooms = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//div[@class='soanjzq atm_r3_1h6ojuz atm_jb_3okqs3 atm_lh_ftgil2 atm_jb_u29brm__oggzyc dir dir-ltr']")));
        }
        h2 = findAndClickByXPATH("//h2[contains(text(), 'Amenities')]", false);
        new Actions(driver).scrollToElement(h2);
        //Here we search for the pool button and click it. If it doesn't click, sometimes it used to do that, we catch the exception and try again.
        showMore = findAndClickByXPATH("//span[contains(text(), 'Show more')]", false);
        new Actions(driver).scrollToElement(showMore);
        showMore.click();
        findAndClickByXPATH("//button[@id='filter-item-amenities-7']", true);

        //Then, we click the show button.
        findAndClickByXPATH("//a[contains(text(), 'Show')]", true);

        //Here we get ALL elements that contain the text bedrooms, for requirement #1, test #2.
        List<WebElement> bedroomList = wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(
                By.xpath("//span[contains(text(), 'bedrooms')]")));

        //And here we make sure the number of bedrooms is bigger or equal to 5.
        for (WebElement element : bedroomList) {
            assertTrue(Integer.parseInt(element.getText().split(" ")[0]) >= 5);
        }
        List<WebElement> propertiesList = wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(By.xpath("//div[@itemprop='itemListElement']")));

        String originalWindow = driver.getWindowHandle();
        propertiesList.getFirst().click();
        skipPopup(originalWindow);
        WebElement showAllAmenities;

        //We check here for requirement #2, test #2, to check if the pool is actually an amenity of the property.
        try {
            showAllAmenities = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(text(), 'Show all')]")));
        }catch (TimeoutException e){
            new Actions(driver).sendKeys(Keys.ESCAPE).perform();
            showAllAmenities = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(text(), 'Show all')]")));
        }
        new Actions(driver).scrollToElement(showAllAmenities);
        showAllAmenities.click();
        WebElement poolAmenity = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//div[contains(@id, 'pdp_v3_parking_facilities_7')]")));
        new Actions(driver).scrollToElement(poolAmenity);

        //There was a case where the text would be Private pool.
        //Therefore, from assertEquals("Pool", poolAmenity.getText()), this was implemented.
        assertTrue(poolAmenity.getText().toLowerCase().contains("pool"));
    }

    @Test
    //@RepeatedTest(10)
    public void testThree() {
        List<WebElement> propertiesList;
        WebElement pill, firstProperty, secondProperty;

        //Here we get the first item in the list we have, the first property.
        try{
            propertiesList = wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(By.xpath("//div[@itemprop='itemListElement']")));
        }
        catch (TimeoutException e){
            propertiesList = wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(By.xpath("//div[@itemprop='itemListElement']")));
        }

        Actions action = new Actions(driver);

        //Then we hover the mouse over it, to simulate the "pill" on the right, on the map, to turn black.
        action.moveToElement(propertiesList.getFirst()).perform();
        pill = findAndClickByXPATH("//div[contains(@style, 'background-color: var(--linaria-theme_palette-hof);')]", false);

        String colorOfHoveredButton = Color.fromString(pill.getCssValue("background-color")).asHex();

        //I added this because I think it takes some time to become #222222. Sometimes it turned #2f2f2f.
        while(!colorOfHoveredButton.equals("#222222")){
            action.moveToElement(propertiesList.getFirst()).perform();
            colorOfHoveredButton = Color.fromString(pill.getCssValue("background-color")).asHex();
        }
        assertEquals("#222222", Color.fromString(pill.getCssValue("background-color")).asHex());
        action.moveToElement(pill).perform();
        action = new Actions(driver);
        action.moveToElement(pill).click().perform();

        //Now here it was a bit more tricky. At first, I tried getting every span text and compare it to the other texts, from the (same) property on the right, over the map.
        //However, I've learned that I can get ALL the text from all the elements inside the divs, then I compared them
        firstProperty = findAndClickByXPATH("//div[contains(@aria-labelledby, 'title_')]//div[1]", false);
        secondProperty = findAndClickByXPATH("//div[contains(@aria-labelledby, 'title_') and @role='group']//div[1]", false);
        assertEquals(firstProperty.getText(), secondProperty.getText());
    }

    //After each test, quit the driver.
    @AfterEach
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
}