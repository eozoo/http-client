package com.cowave.zoo.http.client.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Expands headers supplied in the {@code value}.  Variables to the the right of the colon are expanded. <br>
 * <pre>
 * &#64;Headers("Content-Type: application/xml")
 * interface SoapApi {
 * ...
 * &#64;RequestLine("GET /")
 * &#64;Headers("Cache-Control: max-age=640000")
 * ...
 *
 * &#64;RequestLine("POST /")
 * &#64;Headers({
 *   "X-Foo: Bar",
 *   "X-Ping: {token}"
 * }) void post(&#64;HttpParam("token") String token);
 * ...
 * </pre>
 * <br> <strong>Notes:</strong>
 * <ul>
 *   <li>If you'd like curly braces literally in the header, urlencode them first.</li>
 *   <li>Headers do not overwrite each other. All headers with the same name will be included
 *   in the request.</li>
 * </ul>
 * <br><b>Relationship to JAXRS</b><br> <br> The following two forms are identical. <br><br>
 * <pre>
 * &#64;RequestLine("POST /")
 * &#64;Headers({
 *   "X-Ping: {token}"
 * }) void post(&#64;Named("token") String token);
 * ...
 * </pre>
 * <br>
 * <pre>
 * &#64;POST &#64;Path("/")
 * void post(&#64;HttpParam("X-Ping") String token);
 * ...
 * </pre>
 */
@Target({METHOD, TYPE})
@Retention(RUNTIME)
@Documented
public @interface HttpHeaders {

    String[] value();
}
