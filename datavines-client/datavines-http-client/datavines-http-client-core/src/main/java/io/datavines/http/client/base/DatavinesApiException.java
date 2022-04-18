package io.datavines.http.client.base;

public class DatavinesApiException extends Exception {
    public DatavinesApiException(){
        super();
    }
    public DatavinesApiException(String message){
        super(message);
    }

}
