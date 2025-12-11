package com.qbit.framework.core.api.model.toolkits.money;


import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Currency {
     USD("USD","$" ,"US Dollar"),
     GBP("GBP","#" ,"British Pound Sterling"),
     EUR("EUR","€" ,"Euro"),
     JPY("JPY","¥" ,"Japanese Yen"),
     CNY("CNY","¥" ,"Chinese Yuan"),
     INR("INR","₹" ,"Indian Rupee"),
     KRW("KRW","₩" ,"South Korean Won"),
     SAR("SAR","﷼" ,"Saudi Riyal"),
     AED("AED","د.إ" ,"United Arab Emirates Dirham"),
     CHF("CHF","CHF" ,"Swiss Franc"),
     ZAR("ZAR","R" ,"South African Rand"),
     IDR("IDR","Rp" ,"Indonesian Rupiah"),
     PHP("PHP","₱" ,"Philippine Peso"),
     SGD("SGD","$" ,"Singapore Dollar"),
     THB("THB","฿" ,"Thai Baht"),
     VND("VND","₫" ,"Vietnamese Dong"),
     
     ;

     private final String code;
     private final String symbol;
     private final String name;
}