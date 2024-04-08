package ru.netology.web.test;

import com.codeborne.selenide.Configuration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.chrome.ChromeOptions;
import ru.netology.web.data.DataHelper;
import ru.netology.web.page.DashBoardPage;
import ru.netology.web.page.LoginPage;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static ru.netology.web.data.DataHelper.*;
import static com.codeborne.selenide.Selenide.open;

public class MoneyTransferTest {
    DashBoardPage dashBoardPage;

    @BeforeEach
    void setup() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--start-maximized");
        Map<String, Object> prefs = new HashMap<String, Object>();
        prefs.put("credentials_enable_service",false);
        prefs.put("password_manager_enabled",false);
        options.setExperimentalOption("prefs",prefs);
        Configuration.browserCapabilities =options;
        var loginPage = open("http://localhost:9999", LoginPage.class);
        var authInfo = getAuthInfo();
        var verificationPage = loginPage.validLogin(authInfo);
        var verificationCode = getVerificationCode();
        dashBoardPage = verificationPage.validVerify(verificationCode);

    }

    @Test
    @DisplayName("Should Transfer Money From First Card To Second")
    void shouldTransferMoneyFromFirstCardToSecond() {
        var firstCardInfo = getFirstCardInfo();
        var secondCardInfo = getSecondCardInfo();
        var firstCardBalance = dashBoardPage.getCardBalance(firstCardInfo);
        var secondCardBalance = dashBoardPage.getCardBalance(secondCardInfo);
        var amount = generateValidAmount(firstCardBalance);
        var expectedBalanceFirstCard = firstCardBalance - amount;
        var expectedBalanceSecondCard = secondCardBalance + amount;
        var transferPage = dashBoardPage.selectCardToTransfer(secondCardInfo);
        dashBoardPage = transferPage.makeValidTransfer(String.valueOf(amount), firstCardInfo);
        var actualBalanceFirstCard = dashBoardPage.getCardBalance(firstCardInfo);
        var actualBalanceSecondCard = dashBoardPage.getCardBalance(secondCardInfo);
        assertEquals(expectedBalanceFirstCard, actualBalanceFirstCard);
        assertEquals(expectedBalanceSecondCard, actualBalanceSecondCard);
    }

    @Test
    @DisplayName("Should Get Error Message If Amount More Balance")
    void shouldGetErrorMessageIfAmountMoreBalance() {
        var firstCardInfo = getFirstCardInfo();
        var secondCardInfo = getSecondCardInfo();
        var firstCardBalance = dashBoardPage.getCardBalance(firstCardInfo);
        var secondCardBalance = dashBoardPage.getCardBalance(secondCardInfo);
        var amount = generateInvalidAmount(secondCardBalance);
        var transferPage = dashBoardPage.selectCardToTransfer(firstCardInfo);
        transferPage.makeTransfer(String.valueOf(amount), secondCardInfo);
        transferPage.findErrorMessage("На карте недостаточно средств");
        var actualBalanceFirstCard = dashBoardPage.getCardBalance(firstCardInfo);
        var actualBalanceSecondCard = dashBoardPage.getCardBalance(secondCardInfo);
        assertEquals(firstCardBalance, actualBalanceFirstCard);
        assertEquals(secondCardBalance, actualBalanceSecondCard);
    }


}