package com.ppdai.open.core;

import org.apache.commons.codec.binary.Hex;

import java.nio.ByteBuffer;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

/**
 * 属�?�格式化�?
 * Created by xuzhishen on 2016/5/10.
 */
public class PropertyFormater {

    private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

    /**
     * 对象格式�?
     * @param name  属�?�名�?
     * @param value 属�?��??
     * @param valueType 属�?�类�?
     * @return  格式化后的字符创
     * @throws ParseException
     */
    public static String ObjectFormat(String name, Object value, ValueTypeEnum valueType) throws ParseException {
        Object formatValue = null;
        switch (valueType) {
            case DateTime:
                formatValue = dateTimeFormat(value);
                break;
            case Single:
                formatValue = floatFormat(value);
                break;
            case Double:
                formatValue = doubleFormat(value);
                break;
            case Decimal:
                formatValue = decimalFormat(value);
                break;
            case Boolean:
                formatValue = booleanFormat(value);
                break;
            case Guid:
                formatValue = guidFormat(value);
                break;
            case SByte:
            case Int16:
            case Int32:
            case Int64:
            case Byte:
            case UInt16:
            case UInt32:
            case UInt64:
            case Char:
            case String:
                formatValue = value;
                break;
            default:
                break;
        }

        return formatValue == null ? "" : String.format("%s%s", name, formatValue);
    }

    /**
     * 日期时间格式�?
     * @param obj   待格式化对象
     * @return
     * @throws ParseException
     */
    public static long dateTimeFormat(Object obj) throws ParseException {
        Date real = null;
        if (obj instanceof Date)
            real = (Date) obj;
        else
            real = sdf.parse(obj.toString());

        return (real.getTime() - sdf.parse("1970-01-01").getTime())/1000;
    }

    /**
     * 浮点格式�?
     * @param obj   待格式化对象
     * @return
     */
    public static String floatFormat(Object obj) {
        Float real = null;
        if (obj instanceof Float)
            real = (Float) obj;
        else
            real = Float.parseFloat(obj.toString());

        return Hex.encodeHexString(toByteArray(real)).toUpperCase();
    }

    /**
     * 双精度格式化
     * @param obj   待格式化对象
     * @return
     */
    public static String doubleFormat(Object obj) {
        Double real = null;
        if (obj instanceof Double)
            real = (Double) obj;
        else
            real = Double.parseDouble(obj.toString());

        return Hex.encodeHexString(toByteArray(real)).toUpperCase();
    }

    /**
     * 布尔格式�?
     * @param obj   待格式化对象
     * @return
     */
    public static int booleanFormat(Object obj) {
        Boolean real = null;
        if (obj instanceof Boolean)
            real = (Boolean) obj;
        else
            real = Boolean.parseBoolean(obj.toString());

        return real ? 1 : 0;
    }

    /**
     * Decimal格式�?
     * @param obj   待格式化对象
     * @return
     */
    public static String decimalFormat(Object obj) {
        return doubleFormat(obj);
    }

    /**
     * Guid格式�?
     * @param obj   待格式化对象
     * @return
     */
    public static String guidFormat(Object obj) {
        UUID real = null;
        if (obj instanceof UUID)
            real = (UUID) obj;
        else
            real = UUID.fromString(obj.toString());

        return real.toString();
    }

    /**
     * 浮点类型转换成字节数�?
     * @param val
     * @return
     */
    public static byte[] toByteArray(float val) {
        byte[] bytes = new byte[4];
        ByteBuffer.wrap(bytes).putFloat(val);
        return bytes;
    }

    /**
     * 双精度类型转换成字节数组
     * @param val
     * @return
     */
    public static byte[] toByteArray(double val) {
        byte[] bytes = new byte[8];
        ByteBuffer.wrap(bytes).putDouble(val);
        return bytes;
    }
}
