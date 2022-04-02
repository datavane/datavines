package io.datavines.common.parameter;

import java.util.Objects;

import io.datavines.common.enums.DataType;

public class Property {

    private String key;
    private String value;
    private DataType type;

    public Property(){}

    public Property(String key,String value,DataType type){
        this.key = key;
        this.value = value;
        this.type = type;
    }

    @Override
    public boolean equals(Object o){
        if(this == o){
            return true;
        }

        if(o == null || getClass() != o.getClass() ){
            return false;
        }

        Property property = (Property)o;
        return Objects.equals(key,property.getKey())&&Objects.equals(value,property.getValue());
    }

    @Override
    public int hashCode(){
        return Objects.hash(key,value);
    }

    @Override
    public String toString(){
        return "Property{" +
                "key='" + key + '\'' +
                ", value='" + value + '\'' +
                ", type=" + type +
                '}';
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public DataType getType() {
        return type;
    }

    public void setType(DataType type) {
        this.type = type;
    }
}
