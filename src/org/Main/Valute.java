package org.Main;

import org.w3c.dom.Element;

import java.text.MessageFormat;
import java.time.LocalDate;

public class Valute {
    public LocalDate date;
    public LocalDate createDate;
    public int numCode;
    public String charCode;
    public int nominal;
    public String name;
    public float value;
    public float vUnitRate;

    public Valute(
            LocalDate date,
            LocalDate createDate,
            int numCode,
            String charCode,
            int nominal,
            String name,
            float value,
            float vUnitRate
    ) {
        this.date = date;
        this.createDate = createDate;
        this.numCode = numCode;
        this.charCode = charCode;
        this.nominal = nominal;
        this.name = name;
        this.value = value;
        this.vUnitRate = vUnitRate;
    }

    private static String getTagText(Element element, String tag) {
        return element.getElementsByTagName(tag).item(0).getTextContent();
    }

    private static float parseFloat(String value) {
        try {
            return Float.parseFloat(value);
        } catch (NumberFormatException e) {
            return Float.parseFloat(value.replace(',', '.'));
        }
    }

    public static Valute parse(LocalDate date, Element element) {
        return new Valute(
                date,
                DateUtils.parseDateTime(getTagText(element, "CreateDate")),
                Integer.parseInt(getTagText(element, "NumCode")),
                getTagText(element, "CharCode"),
                Integer.parseInt(getTagText(element, "Nominal")),
                getTagText(element, "Name"),
                parseFloat(getTagText(element, "Value")),
                parseFloat(getTagText(element, "VunitRate"))
        );
    }

    @Override
    public String toString(){
        return MessageFormat.format(
                "Valute<{0};{1};{2};{3};{4};{5};{6}>",
                DateUtils.formatDateTime(createDate), numCode, charCode, nominal, name, value, vUnitRate
        );
    }
}
