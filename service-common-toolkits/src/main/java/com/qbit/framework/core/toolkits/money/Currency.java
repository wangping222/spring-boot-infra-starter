package com.qbit.framework.core.toolkits.money;


import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

/**
 * ISO 4217标准的货币数字代码
 * 
 * @author Qbit Framework
 */

@Getter
@AllArgsConstructor
public enum Currency {
    USD("USD", 840, "$", "US Dollar", "美元", 2),
    GBP("GBP", 826, "£", "British Pound Sterling", "英镑", 2),
    EUR("EUR", 978, "€", "Euro", "欧元", 2),
    JPY("JPY", 392, "¥", "Japanese Yen", "日元", 0),
    CNY("CNY", 156, "¥", "Chinese Yuan", "人民币", 2),
    HKD("HKD", 344, "HK$", "Hong Kong Dollar", "港币", 2),
    TWD("TWD", 901, "NT$", "New Taiwan Dollar", "新台币", 2),
    INR("INR", 356, "₹", "Indian Rupee", "印度卢比", 2),
    KRW("KRW", 410, "₩", "South Korean Won", "韩元", 0),
    SAR("SAR", 682, "﷼", "Saudi Riyal", "沙特里亚尔", 2),
    AED("AED", 784, "د.إ", "United Arab Emirates Dirham", "阿联酋迪拉姆", 2),
    CHF("CHF", 756, "CHF", "Swiss Franc", "瑞士法郎", 2),
    CAD("CAD", 124, "C$", "Canadian Dollar", "加元", 2),
    AUD("AUD", 036, "A$", "Australian Dollar", "澳元", 2),
    NZD("NZD", 554, "NZ$", "New Zealand Dollar", "新西兰元", 2),
    ZAR("ZAR", 710, "R", "South African Rand", "南非兰特", 2),
    IDR("IDR", 360, "Rp", "Indonesian Rupiah", "印尼盾", 2),
    MYR("MYR", 458, "RM", "Malaysian Ringgit", "马来西亚林吉特", 2),
    PHP("PHP", 608, "₱", "Philippine Peso", "菲律宾比索", 2),
    SGD("SGD", 702, "S$", "Singapore Dollar", "新加坡元", 2),
    THB("THB", 764, "฿", "Thai Baht", "泰铢", 2),
    VND("VND", 704, "₫", "Vietnamese Dong", "越南盾", 0),
    BRL("BRL", 986, "R$", "Brazilian Real", "巴西雷亚尔", 2),
    MXN("MXN", 484, "Mex$", "Mexican Peso", "墨西哥比索", 2),
    RUB("RUB", 643, "₽", "Russian Ruble", "俄罗斯卢布", 2),
    TRY("TRY", 949, "₺", "Turkish Lira", "土耳其里拉", 2),
    SEK("SEK", 752, "kr", "Swedish Krona", "瑞典克朗", 2),
    NOK("NOK", 578, "kr", "Norwegian Krone", "挪威克朗", 2),
    DKK("DKK", 208, "kr", "Danish Krone", "丹麦克朗", 2),
    PLN("PLN", 985, "zł", "Polish Zloty", "波兰兹罗提", 2),
    ILS("ILS", 376, "₪", "Israeli New Shekel", "以色列新谢克尔", 2),
    EGP("EGP", 818, "E£", "Egyptian Pound", "埃及镑", 2),

    ;

    private final String code;
    private final int numericCode;
    private final String symbol;
    private final String name;
    private final String chineseName;
    private final int decimalPlaces;

    private static final Map<String, Currency> CODE_MAP = new HashMap<>();
    private static final Map<Integer, Currency> NUMERIC_CODE_MAP = new HashMap<>();

    static {
        for (Currency currency : values()) {
            CODE_MAP.put(currency.code, currency);
            NUMERIC_CODE_MAP.put(currency.numericCode, currency);
        }
    }

    /**
     * 根据货币代码获取Currency
     *
     * @param code 货币代码，如 "USD", "CNY"
     * @return Currency对象，如果不存在返回null
     */
    public static Currency fromCode(String code) {
        return code == null ? null : CODE_MAP.get(code.toUpperCase());
    }

    /**
     * 根据数字代码获取Currency
     *
     * @param numericCode ISO 4217数字代码
     * @return Currency对象，如果不存在返回null
     */
    public static Currency fromNumericCode(int numericCode) {
        return NUMERIC_CODE_MAP.get(numericCode);
    }

    /**
     * 格式化金额，使用货币符号
     *
     * @param amount 金额
     * @return 格式化后的金额字符串，如 "$100.00"
     */
    public String format(BigDecimal amount) {
        if (amount == null) {
            return symbol + "0";
        }
        BigDecimal rounded = amount.setScale(decimalPlaces, RoundingMode.HALF_UP);
        DecimalFormat format = new DecimalFormat();
        format.setMinimumFractionDigits(decimalPlaces);
        format.setMaximumFractionDigits(decimalPlaces);
        format.setGroupingUsed(true);
        return symbol + format.format(rounded);
    }

    /**
     * 格式化金额，使用货币符号
     *
     * @param amount 金额
     * @return 格式化后的金额字符串
     */
    public String format(double amount) {
        return format(BigDecimal.valueOf(amount));
    }

    /**
     * 四舍五入到当前货币的小数位
     *
     * @param amount 金额
     * @return 四舍五入后的金额
     */
    public BigDecimal round(BigDecimal amount) {
        return amount == null ? BigDecimal.ZERO : amount.setScale(decimalPlaces, RoundingMode.HALF_UP);
    }

    /**
     * 转换为最小货币单位（如美分、分）
     *
     * @param amount 金额
     * @return 最小单位的金额
     */
    public long toMinorUnits(BigDecimal amount) {
        if (amount == null) {
            return 0;
        }
        return amount.multiply(BigDecimal.TEN.pow(decimalPlaces))
                .setScale(0, RoundingMode.HALF_UP)
                .longValue();
    }

    /**
     * 从最小货币单位转换为标准金额
     *
     * @param minorUnits 最小单位的金额
     * @return 标准金额
     */
    public BigDecimal fromMinorUnits(long minorUnits) {
        return BigDecimal.valueOf(minorUnits)
                .divide(BigDecimal.TEN.pow(decimalPlaces), decimalPlaces, RoundingMode.HALF_UP);
    }
}