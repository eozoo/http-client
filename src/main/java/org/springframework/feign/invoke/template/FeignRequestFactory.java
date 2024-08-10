package org.springframework.feign.invoke.template;

import feign.Param;
import feign.RequestTemplate;
import org.springframework.feign.invoke.method.FeignMethodMetadata;

import java.io.IOException;
import java.util.*;

import static feign.Util.checkArgument;
import static feign.Util.checkState;

/**
 *
 * @author shanhuiming
 *
 */
public class FeignRequestFactory {

    protected final FeignMethodMetadata metadata;
    private final Map<Integer, Param.Expander> indexToExpander = new LinkedHashMap<>();

    @SuppressWarnings("deprecation")
    public FeignRequestFactory(FeignMethodMetadata metadata) {
        this.metadata = metadata;
        if (metadata.indexToExpander() != null) {
            indexToExpander.putAll(metadata.indexToExpander());
            return;
        }
        if (metadata.indexToExpanderClass().isEmpty()) {
            return;
        }
        for (Map.Entry<Integer, Class<? extends Param.Expander>> indexToExpanderClass : metadata.indexToExpanderClass().entrySet()) {
            try {
                indexToExpander.put(indexToExpanderClass.getKey(), indexToExpanderClass.getValue().newInstance());
            } catch (InstantiationException | IllegalAccessException e) {
                throw new IllegalStateException(e);
            }
        }
    }

    public FeignRequestTemplate create(Object[] argv) throws IOException {
        RequestTemplate template = new RequestTemplate(metadata.template());
        if (metadata.urlIndex() != null) {
            int urlIndex = metadata.urlIndex();
            checkArgument(argv[urlIndex] != null, "URI parameter %s was null", urlIndex);
            template.insert(0, String.valueOf(argv[urlIndex]));
        }

        Map<String, Object> varBuilder = new LinkedHashMap<>();
        for (Map.Entry<Integer, Collection<String>> entry : metadata.indexToName().entrySet()) {
            int i = entry.getKey();
            Object value = argv[entry.getKey()];
            if (value != null) { // Null values are skipped.
                if (indexToExpander.containsKey(i)) {
                    value = expandElements(indexToExpander.get(i), value);
                }
                for (String name : entry.getValue()) {
                    varBuilder.put(name, value);
                }
            }
        }

        FeignRequestTemplate feignTemplate = resolve(argv, template, varBuilder);

        if (metadata.queryMapIndex() != null) {
            // add query map parameters after initial resolve so that they take
            // precedence over any predefined values
            feignTemplate.setTemplate(addQueryMapQueryParameters(argv, feignTemplate.getTemplate()));
        }

        if (metadata.headerMapIndex() != null) {
            feignTemplate.setTemplate(addHeaderMapHeaders(argv, feignTemplate.getTemplate()));
        }
        return feignTemplate;
    }

    @SuppressWarnings("rawtypes")
    private Object expandElements(Param.Expander expander, Object value) {
        if (value instanceof Iterable) {
            return expandIterable(expander, (Iterable) value);
        }
        return expander.expand(value);
    }

    @SuppressWarnings("rawtypes")
    private List<String> expandIterable(Param.Expander expander, Iterable value) {
        List<String> values = new ArrayList<>();
        for (Object element : value) {
            if (element!=null) {
                values.add(expander.expand(element));
            }
        }
        return values;
    }

    @SuppressWarnings("unchecked")
    private RequestTemplate addHeaderMapHeaders(Object[] argv, RequestTemplate template) {
        Map<Object, Object> headerMap = (Map<Object, Object>) argv[metadata.headerMapIndex()];
        for (Map.Entry<Object, Object> currEntry : headerMap.entrySet()) {
            checkState(currEntry.getKey().getClass() == String.class, "HeaderMap key must be a String: %s", currEntry.getKey());
            Collection<String> values = new ArrayList<String>();
            Object currValue = currEntry.getValue();
            if (currValue instanceof Iterable<?>) {
                Iterator<?> iter = ((Iterable<?>) currValue).iterator();
                while (iter.hasNext()) {
                    Object nextObject = iter.next();
                    values.add(nextObject == null ? null : nextObject.toString());
                }
            } else {
                values.add(currValue == null ? null : currValue.toString());
            }

            template.header((String) currEntry.getKey(), values);
        }
        return template;
    }

    @SuppressWarnings("unchecked")
    private RequestTemplate addQueryMapQueryParameters(Object[] argv, RequestTemplate template) {
        Map<Object, Object> queryMap = (Map<Object, Object>) argv[metadata.queryMapIndex()];
        for (Map.Entry<Object, Object> currEntry : queryMap.entrySet()) {
            checkState(currEntry.getKey().getClass() == String.class, "QueryMap key must be a String: %s", currEntry.getKey());
            Collection<String> values = new ArrayList<>();
            Object currValue = currEntry.getValue();
            if (currValue instanceof Iterable<?>) {
                Iterator<?> iter = ((Iterable<?>) currValue).iterator();
                while (iter.hasNext()) {
                    Object nextObject = iter.next();
                    values.add(nextObject == null ? null : nextObject.toString());
                }
            } else {
                values.add(currValue == null ? null : currValue.toString());
            }
            template.query(metadata.queryMapEncoded(), (String) currEntry.getKey(), values);
        }
        return template;
    }

    protected FeignRequestTemplate resolve(Object[] argv, RequestTemplate template, Map<String, Object> variables) throws IOException {
        FeignRequestTemplate feignRequestTemplate = new FeignRequestTemplate();
        feignRequestTemplate.setTemplate(template.resolve(variables));
        if(metadata.hostIndex() != null){
            Object url = argv[metadata.hostIndex()];
            feignRequestTemplate.setHostUrl(url.toString());
        }
        return feignRequestTemplate;
    }
}
