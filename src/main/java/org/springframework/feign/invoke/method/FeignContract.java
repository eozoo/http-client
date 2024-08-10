package org.springframework.feign.invoke.method;

import java.util.*;

/**
 *
 * @author shanhuiming
 *
 */
public interface FeignContract {

    List<FeignMethodMetadata> parseAndValidateMetadata(Class<?> targetType);
}
