package com.muke.common.valid;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.HashSet;
import java.util.Set;

/**
 * ListValues注解校验器
 * @author 木可
 * @version 1.0
 * @date 2021/3/1 20:47
 */
public class ListValuesValidator implements ConstraintValidator<ListValues, Integer> {

    private final Set<Integer> set = new HashSet<>();

    /**
     * 进行逻辑校验
     * @param value 被校验值
     * @param context 校验器上下文
     * @return boolean true：通过校验；false：未通过
     */
    @Override
    public boolean isValid(Integer value, ConstraintValidatorContext context) {
        // 判断set是否为空
        if (!set.isEmpty()) {
            // 判断set中是否包含目标值
            return set.contains(value);
        }
        return false;
    }

    /**
     * 初始化
     * @param constraintAnnotation 注解信息
     */
    @Override
    public void initialize(ListValues constraintAnnotation) {
        // 获取注解中需要校验的值
        int[] values = constraintAnnotation.values();
        for (int value : values) {
            set.add(value);
        }
    }
}
