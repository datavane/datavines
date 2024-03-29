/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.datavines.common.param.form;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;

import static java.util.Objects.requireNonNull;

/**
 * plugin params
 */
@JsonDeserialize(builder = PluginParams.Builder.class)
public class PluginParams {

    private static final String NULL_LOG = "{0} is null";

    /**
     * param name
     */
    @JsonProperty("field")
    protected String field;

    @JsonProperty("props")
    protected Object props;

    @JsonProperty("type")
    protected String type;

    /**
     * Name displayed on the page
     */
    @JsonProperty("title")
    protected String title;

    /**
     * default value or value input by user in the page
     */
    @JsonProperty("value")
    protected Object value;

    @JsonProperty("validate")
    protected List<Validate> validateList;

    @JsonProperty("emit")
    protected List<String> emit;

    protected PluginParams(Builder builder) {

        requireNonNull(builder, MessageFormat.format(NULL_LOG,"builder"));
        requireNonNull(builder.field, MessageFormat.format(NULL_LOG,"field"));
        requireNonNull(builder.type, MessageFormat.format(NULL_LOG,"type"));
        requireNonNull(builder.title, MessageFormat.format(NULL_LOG,"title"));

        this.field = builder.field;
        this.type = builder.type.getDescription();
        this.title = builder.title;
        this.props = builder.props;
        this.value = builder.value;
        this.validateList = builder.validateList;
        this.emit = builder.emit;

    }

    @JsonPOJOBuilder(buildMethodName = "build", withPrefix = "set")
    public static class Builder {
        //Must have
        private static final HashMap<String, FormType> types = new HashMap<>();

        static {
            for(FormType ft: FormType.values()){
                types.put(ft.getDescription(), ft);
            }
        }

        protected String field;

        protected FormType type;

        protected String title;

        protected Object props;

        protected Object value;

        protected List<Validate> validateList;

        protected List<String> emit;

        public Builder(String field,
                       FormType type,
                       String title) {
            requireNonNull(field, "field is null");
            requireNonNull(type, "type is null");
            requireNonNull(title, "title is null");
            this.field = field;
            this.type = type;
            this.title = title;
        }

        //for json deserialize to POJO
        @JsonCreator
        public Builder(@JsonProperty("field") String field,
                       @JsonProperty("type") String type,
                       @JsonProperty("title") String title,
                       @JsonProperty("props") Object props,
                       @JsonProperty("value") Object value,
                       @JsonProperty("validate") List<Validate> validateList,
                       @JsonProperty("emit") List<String> emit
        ) {
            requireNonNull(field, "field is null");
            requireNonNull(type, "type is null");
            requireNonNull(title, "title is null");
            this.field = field;
            this.title = title;
            this.props = props;
            this.value = value;
            this.validateList = validateList;
            this.emit = emit;
            this.type = types.get(type);
        }

        public PluginParams build() {
            return new PluginParams(this);
        }

    }

    public String getField() {
        return field;
    }

    public Object getProps() {
        return props;
    }

    public String getType() {
        return type;
    }

    public String getTitle() {
        return title;
    }

    public Object getValue() {
        return value;
    }

    public List<Validate> getValidateList() {
        return validateList;
    }

    public List<String> getEmit() {
        return emit;
    }
}


