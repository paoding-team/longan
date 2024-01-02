package dev.paoding.longan.validation;

public final class Regexps {
    /**
     * 小写字母
     */
    public static final String SMALL_LETTER = "^[a-z]+$";
    /**
     * 小写字母
     */
    public static final String BIG_LETTER = "^[A-Z]+$";
    /**
     * 字母，不区分大小写
     */
    public static final String Alpha = "^[A-Za-z]+$";
    /**
     * 纯数字
     */
    public static final String NUMERIC = "^[0-9]*$";
    /**
     * 字母和数字
     */
    public static final String ALPHA_NUMERIC = "^[0-9a-zA-Z]+$";
    /**
     * 金额
     */
    public static final String MONEY = "^[0-9]+(.[0-9]{2})?$";
    /**
     * 邮箱
     */
    public static final String EMAIL = "^[a-zA-Z0-9_-]+@[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)+$";
    /**
     * 邮编
     */
    public static final String ZIPCODE = "^\\d{6}$";
    /**
     * 国内手机号
     */
    public static final String CHINESE_MOBILE_PHONE = "^1[3456789]\\d{9}$";
    /**
     * 国内身份证号码
     */
    public static final String CHINES_NATIONAL_IDENTIFICATION_NUMBER = "^[1-9]\\d{5}(18|19|20)\\d{2}((0[1-9])|(1[0-2]))(([0-2][1-9])|10|20|30|31)\\d{3}[0-9Xx]$";
    /**
     * 国内固定电话，支持 (3-4位)区号+(6-8位)直播号码+(1-6位)分机号的组合
     */
    public static final String CHINES_TELEPHONE = "^\\d{3,4}-\\d{6,8}(-\\d{1,6})?$";
    /**
     * HTTP 网址
     */
    public static final String HTTP_URL = "http(s)?://([\\w-]+\\.)+[\\w-]+(/[\\w- ./?%&=]*)?";
}
