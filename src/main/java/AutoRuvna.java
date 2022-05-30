import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.ElementNotInteractableException;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

import javax.mail.*;
import javax.mail.internet.MimeMultipart;
import javax.mail.search.DateTerm;
import javax.mail.search.ReceivedDateTerm;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;
import java.util.Properties;

public class AutoRuvna {
    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("\nPlease pass in email and password.\n");
        } else if (load()) {
            System.out.println("\nRuvna already completed.\n");
        } else {
            String link = getLinkFromEmail("imap.gmail.com", args[0], args[1]);
            if (!link.isEmpty()) {
                System.out.println(ruvna(link) ? "\nRuvna completed.\n" : "\nRuvna already completed.\n");
                save();
            }
        }
    }

    private static String getLinkFromEmail(String host, String emailAddress, String password) {
        Properties properties = new Properties();
        properties.put("mail.imap.host", host);
        properties.put("mail.imap.port", "993");
        properties.put("mail.imap.starttls.enable", "true");
        properties.put("mail.imap.ssl.trust", host);
        Session emailSession = Session.getDefaultInstance(properties);
        try {
            Store store = emailSession.getStore("imaps");
            store.connect(host, emailAddress, password);
            Folder inbox = store.getFolder("Inbox");
            inbox.open(Folder.READ_WRITE);
            Message[] messages = inbox.search(new ReceivedDateTerm(DateTerm.EQ, new Date()));
            for (Message message : messages) {
                if (message.getFrom()[0].toString().equals("Eaglebrook School <health-no-reply@ruvna.com>")) {
                    MimeMultipart mimeMultipart = (MimeMultipart) message.getContent();
                    for (int i = 0; i < mimeMultipart.getCount(); i++) {
                        String messageString = readInputStream(mimeMultipart.getBodyPart(i).getInputStream());
                        int index = messageString.indexOf("Health screening from Eaglebrook School: ");
                        if (index >= 0) {
                            message.setFlag(Flags.Flag.SEEN,true);
                            return messageString.substring(index + 41, messageString.indexOf("\n", index + 41) - 1);
                        }
                    }
                }
            }
        } catch (MessagingException | IOException e) {
            e.printStackTrace();
            System.out.println("\nError while getting email.");
            return "";
        }
        System.out.println("\nRuvna email not found.");
        return "";
    }

    private static String readInputStream(InputStream input) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        for (int length; (length = input.read(buffer)) != -1; ) {
            output.write(buffer, 0, length);
        }
        return output.toString();
    }

    private static boolean ruvna(String link) {
        WebDriverManager.chromedriver().setup();
        WebDriver driver = new ChromeDriver();
        driver.get(link);
        while (true) {
            try {
                driver.findElement(By.xpath("/html/body/div[2]/div/div/div/div[1]/div[3]/a")).click();
                break;
            } catch (NoSuchElementException ignored) {
            }
        }
        while (true) {
            try {
                if (driver.findElement(By.xpath("/html/body/div[2]/div/div/div/div[2]/div[4]/div[2]")).getText().startsWith("You are cleared to come to school today. Have a good day!")) {
                    driver.close();
                    driver.quit();
                    return false;
                }
                driver.findElement(By.xpath("/html/body/div[2]/div/div/div/div[2]/div[3]/div[3]/div[2]/div/div[2]")).click();
                driver.findElement(By.xpath("/html/body/div[2]/div/div/div/div[2]/div[3]/div[3]/div[3]/div/div[2]")).click();
                driver.findElement(By.xpath("/html/body/div[2]/div/div/div/div[2]/div[3]/div[3]/div[4]/div/div[2]")).click();
                driver.findElement(By.xpath("/html/body/div[2]/div/div/div/div[2]/div[3]/div[3]/div[5]/div/div[2]")).click();
                driver.findElement(By.xpath("/html/body/div[2]/div/div/div/div[2]/div[3]/div[3]/div[6]/button")).click();
                break;
            } catch (NoSuchElementException | ElementNotInteractableException ignored) {
            }
        }
        while (true) {
            try {
                if (driver.findElement(By.xpath("/html/body/div[2]/div/div/div/div[2]/div[4]/div[2]")).getText().startsWith("You are cleared to come to school today. Have a good day!")) {
                    driver.close();
                    driver.quit();
                    return true;
                }
            } catch (NoSuchElementException ignored) {
            }
        }
    }

    private static boolean load() {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(".auto_ruvna"));
            if (Objects.equals(reader.readLine(), new SimpleDateFormat("MMM dd yyyy").format(new Date()))) {
                return true;
            }
        } catch (FileNotFoundException e) {
            System.out.println("\nNo ruvna file found. This is normal when using for the first time.\n");
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("\nError while reading ruvna status\n");
        }
        return false;
    }

    private static void save() {
        try {
            PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(".auto_ruvna")));
            writer.println(new SimpleDateFormat("MMM dd yyyy").format(new Date()));
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error while saving ruvna status\n");
        }
    }
}
