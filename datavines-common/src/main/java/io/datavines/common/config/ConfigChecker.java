package io.datavines.common.config;

import java.util.*;
import java.util.stream.Collectors;

public class ConfigChecker {

    public static CheckResult checkConfig(Map<String,Object> config, Set<String> requiredOptions) {

        List<String> nonExistsOptions = new ArrayList<>();
        requiredOptions.forEach(x->{
            if(!config.containsKey(x)){
                nonExistsOptions.add(x);
            }
        });

        if (!nonExistsOptions.isEmpty()) {
            return new CheckResult(
                    false,
                    "please specify " + nonExistsOptions.stream().map(option ->
                            "[" + option + "]").collect(Collectors.joining(",")) + " as non-empty string");
        } else {
            return new CheckResult(true, "");
        }
    }
}
