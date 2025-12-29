package com.qbit.framework.core.toolkits.money;


import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * ISO 3166-1 国家码枚举（常用子集）。
 * 包含 Alpha-2、Alpha-3、数字码以及中文名称。
 * 如需更多国家，可按相同格式扩展。
 *
 * @author Qbit Framework
 */
@Getter
@AllArgsConstructor
public enum CountryCode {
    // 常用国家（按中文名大致分布）
    CN("CN", "CHN", 156, "中国"),
    US("US", "USA", 840, "美国"),
    GB("GB", "GBR", 826, "英国"),
    DE("DE", "DEU", 276, "德国"),
    FR("FR", "FRA", 250, "法国"),
    JP("JP", "JPN", 392, "日本"),
    KR("KR", "KOR", 410, "韩国"),
    CA("CA", "CAN", 124, "加拿大"),
    AU("AU", "AUS", 36, "澳大利亚"),
    NZ("NZ", "NZL", 554, "新西兰"),
    SG("SG", "SGP", 702, "新加坡"),
    HK("HK", "HKG", 344, "中国香港"),
    TW("TW", "TWN", 158, "中国台湾"),
    IN("IN", "IND", 356, "印度"),
    RU("RU", "RUS", 643, "俄罗斯"),
    BR("BR", "BRA", 76, "巴西"),
    MX("MX", "MEX", 484, "墨西哥"),
    AR("AR", "ARG", 32, "阿根廷"),
    CL("CL", "CHL", 152, "智利"),
    CO("CO", "COL", 170, "哥伦比亚"),
    ZA("ZA", "ZAF", 710, "南非"),
    AE("AE", "ARE", 784, "阿联酋"),
    SA("SA", "SAU", 682, "沙特阿拉伯"),
    TR("TR", "TUR", 792, "土耳其"),
    IL("IL", "ISR", 376, "以色列"),
    EG("EG", "EGY", 818, "埃及"),
    NG("NG", "NGA", 566, "尼日利亚"),
    PK("PK", "PAK", 586, "巴基斯坦"),
    TH("TH", "THA", 764, "泰国"),
    MY("MY", "MYS", 458, "马来西亚"),
    PH("PH", "PHL", 608, "菲律宾"),
    VN("VN", "VNM", 704, "越南"),
    ID("ID", "IDN", 360, "印度尼西亚"),
    IT("IT", "ITA", 380, "意大利"),
    ES("ES", "ESP", 724, "西班牙"),
    PT("PT", "PRT", 620, "葡萄牙"),
    GR("GR", "GRC", 300, "希腊"),
    NL("NL", "NLD", 528, "荷兰"),
    BE("BE", "BEL", 56, "比利时"),
    AT("AT", "AUT", 40, "奥地利"),
    SE("SE", "SWE", 752, "瑞典"),
    NO("NO", "NOR", 578, "挪威"),
    DK("DK", "DNK", 208, "丹麦"),
    FI("FI", "FIN", 246, "芬兰"),
    CH("CH", "CHE", 756, "瑞士"),
    IE("IE", "IRL", 372, "爱尔兰"),
    PL("PL", "POL", 616, "波兰"),
    CZ("CZ", "CZE", 203, "捷克"),
    HU("HU", "HUN", 348, "匈牙利"),
    RO("RO", "ROU", 642, "罗马尼亚"),
    BG("BG", "BGR", 100, "保加利亚"),
    SK("SK", "SVK", 703, "斯洛伐克"),
    UA("UA", "UKR", 804, "乌克兰");

    // 两位国家码（Alpha-2）
    private final String alpha2;
    // 三位国家码（Alpha-3）
    private final String alpha3;
    // 数字码
    private final int numeric;
    // 中文名称（可按需改为英文名等）
    private final String zhName;

    /**
     * 根据 Alpha-2（不区分大小写）获取枚举值。
     */
    public static CountryCode fromAlpha2(String code) {
        for (CountryCode cc : values()) {
            if (cc.alpha2.equalsIgnoreCase(code)) {
                return cc;
            }
        }
        throw new IllegalArgumentException("Unsupported alpha-2 country code: " + code);
    }

    /**
     * 根据 Alpha-3（不区分大小写）获取枚举值。
     */
    public static CountryCode fromAlpha3(String code) {
        for (CountryCode cc : values()) {
            if (cc.alpha3.equalsIgnoreCase(code)) {
                return cc;
            }
        }
        throw new IllegalArgumentException("Unsupported alpha-3 country code: " + code);
    }

    /**
     * 根据数字码获取枚举值。
     */
    public static CountryCode fromNumeric(int numeric) {
        for (CountryCode cc : values()) {
            if (cc.numeric == numeric) {
                return cc;
            }
        }
        throw new IllegalArgumentException("Unsupported numeric country code: " + numeric);
    }
}