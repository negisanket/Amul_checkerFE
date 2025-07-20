package Amul;

import io.github.bonigarcia.wdm.WebDriverManager;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.testng.annotations.*;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

public class Amul {

    private WebDriver driver;
    private final Map<String, String> productUrls = new LinkedHashMap<>();
    private final StringBuilder resultBuilder = new StringBuilder();
    boolean sendEmail = false;

    @BeforeClass
    public void setup() {
        WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new", "--no-sandbox", "--disable-dev-shm-usage");
        driver = new ChromeDriver(options);

        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));


        productUrls.put("Rose Lassi", "https://shop.amul.com/en/product/amul-high-protein-rose-lassi-200-ml-or-pack-of-30");
        productUrls.put("Plain Lassi", "https://shop.amul.com/en/product/amul-high-protein-plain-lassi-200-ml-or-pack-of-30");
        productUrls.put("Buttermilk", "https://shop.amul.com/en/product/amul-high-protein-buttermilk-200-ml-or-pack-of-30");

    }

    @Test
    public void verifyHighProteinRoseLassiAvailability() throws InterruptedException {
        driver.get("https://shop.amul.com/en/browse/protein");

        // Enter pincode
        WebElement pincodeInput = driver.findElement(By.xpath("//*[@id='search']"));
        pincodeInput.sendKeys("400103");

        // Select from suggestion
        WebElement suggestion = driver.findElement(By.xpath("//*[@id='automatic']/div[2]/a/p"));
        suggestion.click();

        // Wait for products to load
        Thread.sleep(3000);


        for (Map.Entry<String, String> entry : productUrls.entrySet()) {
            String productName = entry.getKey();
            String url = entry.getValue();

            driver.navigate().to(url);
            Thread.sleep(2000); // allow page to load

            try {
                WebElement notifyMe = driver.findElement(By.xpath("//button[normalize-space()='Notify Me']"));
                // Check if the button is enabled
                System.out.println("notifyMe +" + (notifyMe.isEnabled()));
                if (notifyMe.isEnabled()) {
                    System.out.println(" Product is SOLD OUT (Add to Cart is disabled).");
                    resultBuilder.append("❌ ").append(productName).append(": SOLD OUT\n");
                } else {
                    System.out.println("✅ Product is AVAILABLE (Notify Me is disabled). Sending email...");
                    resultBuilder.append("✅ ").append(productName).append(": AVAILABLE\n");
                     sendEmail = true;
//        sendEmailNotification("sanket@gmail.com", "*****Amul Product Available***",
//                "🎉 Amul High Protein Rose Lassi is now available. Hurry up and order!");

                }
            } catch (NoSuchElementException e) {
                System.out.println("⚠️ 'NotifyMe' button not found on the page.");
            }

            // Send summary email

        }
        if(sendEmail)
            sendEmail("negisanket@gmail.com", "Amul Product Availability Update", resultBuilder.toString());
    }
        private void sendEmail(String toEmail, String subject, String body) {
    final String fromEmail = System.getenv("SENDER_EMAIL"); // From GitHub Secrets
    final String appPassword = System.getenv("APP_PASS"); // From GitHub Secrets

    if (fromEmail == null || appPassword == null) {
        System.out.println("❌ Email credentials not found in environment variables.");
        return;
    }

            Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", "smtp.gmail.com");
            props.put("mail.smtp.port", "587");

            Session session = Session.getInstance(props,
                    new Authenticator() {
                        protected PasswordAuthentication getPasswordAuthentication() {
                            return new PasswordAuthentication(fromEmail, appPassword);
                        }
                    });

            try {
                Message message = new MimeMessage(session);
                message.setFrom(new InternetAddress(fromEmail));
                message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
                message.setSubject(subject);
                message.setText(body);

                Transport.send(message);
                System.out.println("✅ Email sent successfully.");
            } catch (MessagingException e) {
                e.printStackTrace();
                System.out.println("❌ Failed to send email.");
            }
        }

    @AfterClass
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
}
