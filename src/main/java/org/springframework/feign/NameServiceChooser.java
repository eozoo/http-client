package org.springframework.feign;

/**
 *
 * @author shanhuiming
 *
 */
public interface NameServiceChooser {

    String choose(String name);
}
