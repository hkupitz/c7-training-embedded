package io.camunda.training.services;

import java.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class CreditCardService {

  private static final Logger LOG = LoggerFactory.getLogger(CreditCardService.class);

  public boolean validateExpiryDate(String expiryDate) {
    if (expiryDate.length() != 5) {
      return false;
    }
    try {
      int month = Integer.valueOf(expiryDate.substring(0, 2));
      int year = Integer.valueOf(expiryDate.substring(3, 5)) + 2000;
      LocalDate now = LocalDate.now();
      if (month < 1 || month > 12 || year < now.getYear()) {
        return false;
      }
      if (year > now.getYear() ||
        (year == now.getYear() && month >= now.getMonthValue())) {
        return true;
      } else {
        return false;
      }
    } catch (NumberFormatException | IndexOutOfBoundsException e) {
      return false;
    }
  }

  public void chargeAmount(String cardNumber, String cvc, String expiryDate, Double amount) {
    LOG.info("Charging card {} that expires on {} and has the CVC {} with an amount of {}",
      cardNumber, expiryDate, cvc, amount);

    LOG.info("Payment completed");
  }

}
