package nz.co.ethan.tsbbanking.domain.enums;

import nz.co.ethan.tsbbanking.common.BizException;
import nz.co.ethan.tsbbanking.common.ErrorCodes;

import java.util.Locale;

public enum AccountType {

    STREAMLINE,
    SAVINGS_ON_CALL,
    BUSINESS,
    FOREIGN_CURRENCY_CALL,
    TERM_DEPOSIT;


    public static AccountType parseFlexible(String value) {
        if (value == null) return null;

        String normalized = value.trim()
                .toUpperCase(Locale.ROOT)
                .replace(" ", "_");

        try {
            return AccountType.valueOf(normalized);
        } catch (IllegalArgumentException e) {
            throw new BizException(ErrorCodes.INVALID_ACCOUNT_TYPE.code(),
                    "Invalid account type: " + value + " (normalized as: " + normalized + ")");
        }
    }
}
