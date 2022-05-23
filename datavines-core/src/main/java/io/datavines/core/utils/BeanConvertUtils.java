package io.datavines.core.utils;

import org.springframework.beans.BeanUtils;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BeanConvertUtils {

    public static <T>  T convertBean(Object source, Supplier<T> supplier){
        T target = supplier.get();
        BeanUtils.copyProperties(source, target);
        return target;
    }

    /**
     * convert bean collection to another type bean collection
     * @param source
     * @param supplier
     * @param <T>
     * @return
     */
    public static <T> List<T> convertBeanCollection(Collection source, Supplier<T> supplier){
        if (CollectionUtils.isEmpty(source)){
            return new ArrayList<T>();
        }
        Stream<T> stream = source.stream().map(x -> {
            T target = supplier.get();
            BeanUtils.copyProperties(source, target);
            return target;
        });
        List<T> collect = stream.collect(Collectors.toList());
        return collect;
    }
}
