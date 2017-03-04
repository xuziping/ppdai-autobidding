package com.ppdai.open.core;

import java.text.ParseException;

/**
 * å±æ?§å¯¹è±?
 * Created by xuzhishen on 2016/5/10.
 */
public class PropertyObject {

    private String name;
    private String lowerName;

    private Object value;
    private ValueTypeEnum valueType;

    /**
     * å±æ?§å¯¹è±?
     * @param name  å±æ?§åç§?
     * @param value å±æ?§å??
     * @param valueType å±æ?§ç±»å?
     */
    public PropertyObject(String name, Object value, ValueTypeEnum valueType)
    {
        this.name = name;
        this.lowerName = name.toLowerCase();
        this.value = value;
        this.valueType = valueType;
    }

    /**
     * è·å–å±æ?§åç§?
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     * è·å–å°å†™çš„å±æ€§åç§?
     * @return
     */
    public String getLowerName() {
        return lowerName;
    }

    /**
     * è·å–å±æ?§å??
     * @return
     */
    public Object getValue() {
        return value;
    }

    /**
     * æ˜¯å¦å‚ä¸ç­¾å
     * @return
     */
    public boolean isSign(){
        return value != null && valueType != ValueTypeEnum.Other;
    }

    @Override
    public String toString() {
        try {
            return PropertyFormater.ObjectFormat(lowerName, value, valueType);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return  null;
    }
}
