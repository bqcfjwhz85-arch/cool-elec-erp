package com.cool.core.util;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;

import java.util.Date;
import java.util.function.Function;
import java.util.regex.Pattern;

/**
 * 编码自动生成工具类
 * 生成格式：前缀 + 日期(YYYYMMDD) + 序号(3位)
 * 例如：SKU20251105001、BR20251105001
 */
public class CodeGenerator {
    
    /**
     * 生成编码
     * 
     * @param prefix 前缀（如：SKU、BR、MDL、CAT）
     * @param maxCodeProvider 获取当天最大编码的函数（查询数据库）
     * @return 新生成的编码
     */
    public static String generateCode(String prefix, Function<String, String> maxCodeProvider) {
        // 获取当前日期 YYYYMMDD
        String dateStr = DateUtil.format(new Date(), "yyyyMMdd");
        
        // 查询当天该前缀的最大编码
        String todayPrefix = prefix + dateStr;
        String maxCode = maxCodeProvider.apply(todayPrefix);
        
        // 计算下一个序号
        int nextSerial = 1;
        if (StrUtil.isNotBlank(maxCode)) {
            // 提取序号部分（最后3位）
            String serialStr = maxCode.substring(maxCode.length() - 3);
            try {
                int currentSerial = Integer.parseInt(serialStr);
                nextSerial = currentSerial + 1;
            } catch (NumberFormatException e) {
                // 如果解析失败，从1开始
                nextSerial = 1;
            }
        }
        
        // 生成新编码：前缀 + 日期 + 序号（补齐3位）
        return String.format("%s%s%03d", prefix, dateStr, nextSerial);
    }
    
    /**
     * 验证编码格式是否正确
     * 
     * @param code 编码
     * @param prefix 期望的前缀
     * @return true-格式正确 false-格式错误
     */
    public static boolean validateCodeFormat(String code, String prefix) {
        if (StrUtil.isBlank(code)) {
            return false;
        }
        // 格式：前缀 + 8位日期 + 3位数字
        String pattern = "^" + prefix + "\\d{8}\\d{3}$";
        return Pattern.matches(pattern, code);
    }
    
    /**
     * 从编码中提取日期
     * 
     * @param code 编码
     * @param prefix 前缀
     * @return 日期字符串（YYYYMMDD）
     */
    public static String extractDate(String code, String prefix) {
        if (StrUtil.isBlank(code) || code.length() < prefix.length() + 8) {
            return null;
        }
        return code.substring(prefix.length(), prefix.length() + 8);
    }
    
    /**
     * 从编码中提取序号
     * 
     * @param code 编码
     * @return 序号
     */
    public static int extractSerial(String code) {
        if (StrUtil.isBlank(code) || code.length() < 3) {
            return 0;
        }
        String serialStr = code.substring(code.length() - 3);
        try {
            return Integer.parseInt(serialStr);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}

