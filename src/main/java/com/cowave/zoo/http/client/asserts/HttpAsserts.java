package com.cowave.zoo.http.client.asserts;

import com.cowave.zoo.http.client.response.ResponseCode;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

/**
 *
 * @author shanhuiming
 *
 */
public class HttpAsserts {

    public static void isTrue(boolean expression, ResponseCode responseCode){
        if (!expression) {
            throw new HttpException(responseCode);
        }
    }

    public static void isTrue(boolean expression, ResponseCode responseCode, String message, Object... args) {
        if (!expression) {
            throw new HttpException(responseCode.getStatus(), responseCode.getCode(), message, args);
        }
    }

    public static void isTrue(boolean expression, int status, String code, String message, Object... args) {
        if (!expression) {
            throw new HttpException(status, code, message, args);
        }
    }

    public static void isTrue(boolean expression, int status, String code, Supplier<String> errorSupplier) {
        if (!expression) {
            throw new HttpException(status, code, errorSupplier.get());
        }
    }

    public static void isFalse(boolean expression, ResponseCode responseCode){
        if (expression) {
            throw new HttpException(responseCode);
        }
    }

    public static void isFalse(boolean expression, ResponseCode responseCode, String message, Object... args) {
        if (expression) {
            throw new HttpException(responseCode.getStatus(), responseCode.getCode(), message, args);
        }
    }

    public static void isFalse(boolean expression, int status, String code, String message, Object... args) {
        if (expression) {
            throw new HttpException(status, code, message, args);
        }
    }

    public static void isFalse(boolean expression, int status, String code, Supplier<String> errorSupplier) {
        if (expression) {
            throw new HttpException(status, code, errorSupplier.get());
        }
    }

    public static void notEquals(Object a, Object b, ResponseCode responseCode){
        if (Objects.equals(a, b)) {
            throw new HttpException(responseCode);
        }
    }

    public static void notEquals(Object a, Object b, ResponseCode responseCode, String message, Object... args) {
        if (Objects.equals(a, b)) {
            throw new HttpException(responseCode.getStatus(), responseCode.getCode(), message, args);
        }
    }

    public static void notEquals(Object a, Object b, int status, String code, String message, Object... args) {
        if (Objects.equals(a, b)) {
            throw new HttpException(status, code, message, args);
        }
    }

    public static void notEquals(Object a, Object b, int status, String code, Supplier<String> errorSupplier) {
        if (Objects.equals(a, b)) {
            throw new HttpException(status, code, errorSupplier.get());
        }
    }

    public static void equals(Object a, Object b, ResponseCode responseCode){
        if (!Objects.equals(a, b)) {
            throw new HttpException(responseCode);
        }
    }

    public static void equals(Object a, Object b, ResponseCode responseCode, String message, Object... args) {
        if (!Objects.equals(a, b)) {
            throw new HttpException(responseCode.getStatus(), responseCode.getCode(), message, args);
        }
    }

    public static void equals(Object a, Object b, int status, String code, String message, Object... args) {
        if (!Objects.equals(a, b)) {
            throw new HttpException(status, code, message, args);
        }
    }

    public static void equals(Object a, Object b, int status, String code, Supplier<String> errorSupplier) {
        if (!Objects.equals(a, b)) {
            throw new HttpException(status, code, errorSupplier.get());
        }
    }

    public static void notBlank(String text, ResponseCode responseCode){
        if (StringUtils.isBlank(text)) {
            throw new HttpException(responseCode);
        }
    }

    public static void notBlank(String text, ResponseCode responseCode, String message, Object... args) {
        if (StringUtils.isBlank(text)) {
            throw new HttpException(responseCode.getStatus(), responseCode.getCode(), message, args);
        }
    }

    public static void notBlank(String text, int status, String code, String message, Object... args) {
        if (StringUtils.isBlank(text)) {
            throw new HttpException(status, code, message, args);
        }
    }

    public static void notBlank(String text, int status, String code, Supplier<String> errorSupplier) {
        if (StringUtils.isBlank(text)) {
            throw new HttpException(status, code, errorSupplier.get());
        }
    }

    public static void isBlank(String text, ResponseCode responseCode){
        if (StringUtils.isNotBlank(text)) {
            throw new HttpException(responseCode);
        }
    }

    public static void isBlank(String text, ResponseCode responseCode, String message, Object... args) {
        if (StringUtils.isNotBlank(text)) {
            throw new HttpException(responseCode.getStatus(), responseCode.getCode(), message, args);
        }
    }

    public static void isBlank(String text, int status, String code, String message, Object... args) {
        if (StringUtils.isNotBlank(text)) {
            throw new HttpException(status, code, message, args);
        }
    }

    public static void isBlank(String text, int status, String code, Supplier<String> errorSupplier) {
        if (StringUtils.isNotBlank(text)) {
            throw new HttpException(status, code, errorSupplier.get());
        }
    }

    public static void notNull(Object object, ResponseCode responseCode){
        if (object == null) {
            throw new HttpException(responseCode);
        }
    }

    public static void notNull(Object object, ResponseCode responseCode, String message, Object... args) {
        if (object == null) {
            throw new HttpException(responseCode.getStatus(), responseCode.getCode(), message, args);
        }
    }

    public static void notNull(Object object, int status, String code, String message, Object... args) {
        if (object == null) {
            throw new HttpException(status, code, message, args);
        }
    }

    public static void notNull(Object object, int status, String code, Supplier<String> errorSupplier) {
        if (object == null) {
            throw new HttpException(status, code, errorSupplier.get());
        }
    }

    public static void isNull(Object object, ResponseCode responseCode){
        if (object != null) {
            throw new HttpException(responseCode);
        }
    }

    public static void isNull(Object object, ResponseCode responseCode, String message, Object... args) {
        if (object != null) {
            throw new HttpException(responseCode.getStatus(), responseCode.getCode(), message, args);
        }
    }

    public static void isNull(Object object, int status, String code, String message, Object... args) {
        if (object != null) {
            throw new HttpException(status, code, message, args);
        }
    }

    public static void isNull(Object object, int status, String code, Supplier<String> errorSupplier) {
        if (object != null) {
            throw new HttpException(status, code, errorSupplier.get());
        }
    }

    public static void notEmpty(Map<?, ?> map, ResponseCode responseCode){
        if (ObjectUtils.isEmpty(map)) {
            throw new HttpException(responseCode);
        }
    }

    public static void notEmpty(Map<?, ?> map, ResponseCode responseCode, String message, Object... args) {
        if (ObjectUtils.isEmpty(map)) {
            throw new HttpException(responseCode.getStatus(), responseCode.getCode(), message, args);
        }
    }

    public static void notEmpty(Map<?, ?> map, int status, String code, String message, Object... args) {
        if (ObjectUtils.isEmpty(map)) {
            throw new HttpException(status, code, message, args);
        }
    }

    public static void notEmpty(Map<?, ?> map, int status, String code, Supplier<String> errorSupplier) {
        if (ObjectUtils.isEmpty(map)) {
            throw new HttpException(status, code, errorSupplier.get());
        }
    }

    public static void isEmpty(Map<?, ?> map, ResponseCode responseCode){
        if (ObjectUtils.isNotEmpty(map)) {
            throw new HttpException(responseCode);
        }
    }

    public static void isEmpty(Map<?, ?> map, ResponseCode responseCode, String message, Object... args) {
        if (ObjectUtils.isNotEmpty(map)) {
            throw new HttpException(responseCode.getStatus(), responseCode.getCode(), message, args);
        }
    }

    public static void isEmpty(Map<?, ?> map, int status, String code, String message, Object... args) {
        if (ObjectUtils.isNotEmpty(map)) {
            throw new HttpException(status, code, message, args);
        }
    }

    public static void isEmpty(Map<?, ?> map, int status, String code, Supplier<String> errorSupplier) {
        if (ObjectUtils.isNotEmpty(map)) {
            throw new HttpException(status, code, errorSupplier.get());
        }
    }

    public static void notEmpty(Collection<?> collection, ResponseCode responseCode){
        if (CollectionUtils.isEmpty(collection)) {
            throw new HttpException(responseCode);
        }
    }

    public static void notEmpty(Collection<?> collection, ResponseCode responseCode, String message, Object... args) {
        if (CollectionUtils.isEmpty(collection)) {
            throw new HttpException(responseCode.getStatus(), responseCode.getCode(), message, args);
        }
    }

    public static void notEmpty(Collection<?> collection, int status, String code, String message, Object... args) {
        if (CollectionUtils.isEmpty(collection)) {
            throw new HttpException(status, code, message, args);
        }
    }

    public static void notEmpty(Collection<?> collection, int status, String code, Supplier<String> errorSupplier) {
        if (CollectionUtils.isEmpty(collection)) {
            throw new HttpException(status, code, errorSupplier.get());
        }
    }

    public static void isEmpty(Collection<?> collection, ResponseCode responseCode){
        if (!CollectionUtils.isEmpty(collection)) {
            throw new HttpException(responseCode);
        }
    }

    public static void isEmpty(Collection<?> collection, ResponseCode responseCode, String message, Object... args) {
        if (!CollectionUtils.isEmpty(collection)) {
            throw new HttpException(responseCode.getStatus(), responseCode.getCode(), message, args);
        }
    }

    public static void isEmpty(Collection<?> collection, int status, String code, String message, Object... args) {
        if (!CollectionUtils.isEmpty(collection)) {
            throw new HttpException(status, code, message, args);
        }
    }

    public static void isEmpty(Collection<?> collection, int status, String code, Supplier<String> errorSupplier) {
        if (!CollectionUtils.isEmpty(collection)) {
            throw new HttpException(status, code, errorSupplier.get());
        }
    }

    public static void notEmpty(Object[] array, ResponseCode responseCode){
        if (ObjectUtils.isEmpty(array)) {
            throw new HttpException(responseCode);
        }
    }

    public static void notEmpty(Object[] array, ResponseCode responseCode, String message, Object... args) {
        if (ObjectUtils.isEmpty(array)) {
            throw new HttpException(responseCode.getStatus(), responseCode.getCode(), message, args);
        }
    }

    public static void notEmpty(Object[] array, int status, String code, String message, Object... args) {
        if (ObjectUtils.isEmpty(array)) {
            throw new HttpException(status, code, message, args);
        }
    }

    public static void notEmpty(Object[] array, int status, String code, Supplier<String> errorSupplier) {
        if (ObjectUtils.isEmpty(array)) {
            throw new HttpException(status, code, errorSupplier.get());
        }
    }

    public static void isEmpty(Object[] array, ResponseCode responseCode){
        if (!ObjectUtils.isEmpty(array)) {
            throw new HttpException(responseCode);
        }
    }

    public static void isEmpty(Object[] array, ResponseCode responseCode, String message, Object... args) {
        if (!ObjectUtils.isEmpty(array)) {
            throw new HttpException(responseCode.getStatus(), responseCode.getCode(), message, args);
        }
    }

    public static void isEmpty(Object[] array, int status, String code, String message, Object... args) {
        if (!ObjectUtils.isEmpty(array)) {
            throw new HttpException(status, code, message, args);
        }
    }

    public static void isEmpty(Object[] array, int status, String code, Supplier<String> errorSupplier) {
        if (!ObjectUtils.isEmpty(array)) {
            throw new HttpException(status, code, errorSupplier.get());
        }
    }
}
