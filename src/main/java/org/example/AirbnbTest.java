import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;

public class AirbnbTest {
    public static void main(String[] args) {
        // Set the path to your chromedriver executable
        System.setProperty("webdriver.chrome.driver", "/path/to/chromedriver");

        // Initialize the WebDriver (Chrome)
        WebDriver driver = new ChromeDriver();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        try {
            // Navigate to Airbnb
            driver.get("https://www.airbnb.com");

            // Enter "Rome, Italy" in the search field
            WebElement searchInput = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("bigsearch-query-detached-query")));
            searchInput.sendKeys("Rome, Italy");
            Thread.sleep(2000);  // Allow time for the suggestions to load
            searchInput.sendKeys(Keys.ENTER);

            // Select check-in date (one week from the current date)
            WebElement checkInButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//div[@data-testid='structured-search-input-field-split-dates-0']")));
            checkInButton.click();

            // Select check-in date (e.g., assuming a fixed date, you can adjust dynamically)
            WebElement checkInDate = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//td[@data-testid='datepicker-day-7']")));
            checkInDate.click();

            // Select check-out date (one week after the check-in date)
            WebElement checkOutDate = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//td[@data-testid='datepicker-day-14']")));
            checkOutDate.click();

            // Select 2 adults and 1 child
            WebElement guestButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//div[@data-testid='structured-search-input-field-guests-button']")));
            guestButton.click();

            // Increment the number of adults to 2
            WebElement adultsIncrementButton = driver.findElement(By.xpath("//button[@aria-label='increase adults value']"));
            adultsIncrementButton.click();
            adultsIncrementButton.click();

            // Increment the number of children to 1
            WebElement childrenIncrementButton = driver.findElement(By.xpath("//button[@aria-label='increase children value']"));
            childrenIncrementButton.click();

            // Submit the search
            WebElement submitSearchButton = driver.findElement(By.xpath("//button[@data-testid='structured-search-input-search-button']"));
            submitSearchButton.click();

            // Wait for the results to load and verify
            wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//div[@data-testid='search-results']")));

            // Verify location is Rome
            WebElement filterText = driver.findElement(By.xpath("//span[contains(text(), 'Rome')]"));
            if (filterText.getText().contains("Rome")) {
                System.out.println("Location filter is correctly applied: Rome");
            } else {
                System.out.println("Location filter is incorrect.");
            }

            // Verify guest count (optional - you can verify other filters as well)
            // For now, let's just print the title of the first property
            WebElement firstPropertyTitle = driver.findElement(By.xpath("(//div[@data-testid='property-card-title'])[1]"));
            System.out.println("First property title: " + firstPropertyTitle.getText());

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // Close the browser
            driver.quit();
        }
    }
}