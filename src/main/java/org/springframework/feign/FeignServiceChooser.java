package org.springframework.feign;

/**
 *
 * @author shanhuiming
 *
 */
public interface FeignServiceChooser {

    String choose(String name);
}
