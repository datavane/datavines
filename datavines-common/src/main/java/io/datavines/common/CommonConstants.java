package io.datavines.common;


import org.apache.commons.lang3.StringUtils;

public class CommonConstants {

    public static final String DOT = ".";
    /**
     * QUESTION ?
     */
    public static final String QUESTION = "?";
    /**
     * comma ,
     */
    public static final String COMMA = ",";

    /**
     * COLON :
     */
    public static final String COLON = ":";

    /**
     * SINGLE_SLASH /
     */
    public static final String SINGLE_SLASH = "/";

    /**
     * DOUBLE_SLASH //
     */
    public static final String DOUBLE_SLASH = "//";

    /**
     * SEMICOLON ;
     */
    public static final String SEMICOLON = ";";

    /**
     * EQUAL SIGN
     */
    public static final String EQUAL_SIGN = "=";

    /**
     * underline  "_"
     */
    public static final String UNDERLINE = "_";

    /**
     * SINGLE_QUOTES "'"
     */
    public static final String SINGLE_QUOTES = "'";

    /**
     * double brackets left
     */
    public static final String DOUBLE_BRACKETS_LEFT = "{{";

    /**
     * double brackets left
     */
    public static final String DOUBLE_BRACKETS_RIGHT = "}}";

    /**
     * double brackets left
     */
    public static final String DOUBLE_BRACKETS_LEFT_SPACE = "{ {";

    /**
     * double brackets left
     */
    public static final String DOUBLE_BRACKETS_RIGHT_SPACE = "} }";

    /**
     * UTF-8
     */
    public static final String UTF_8 = "UTF-8";

    /**
     * http connect time out
     */
    public static final int HTTP_CONNECT_TIMEOUT = 60 * 1000;

    /**
     * http connect request time out
     */
    public static final int HTTP_CONNECTION_REQUEST_TIMEOUT = 60 * 1000;

    /**
     * httpclient soceket time out
     */
    public static final int SOCKET_TIMEOUT = 60 * 1000;

    /**
     * ACCEPTED
     */
    public static final String ACCEPTED = "ACCEPTED";

    /**
     * SUCCEEDED
     */
    public static final String SUCCEEDED = "SUCCEEDED";
    /**
     * NEW
     */
    public static final String NEW = "NEW";
    /**
     * NEW_SAVING
     */
    public static final String NEW_SAVING = "NEW_SAVING";
    /**
     * SUBMITTED
     */
    public static final String SUBMITTED = "SUBMITTED";
    /**
     * FAILED
     */
    public static final String FAILED = "FAILED";
    /**
     * KILLED
     */
    public static final String KILLED = "KILLED";
    /**
     * RUNNING
     */
    public static final String RUNNING = "RUNNING";

    public static final int SLEEP_TIME_MILLIS = 1000;

    /**
     * date format of yyyy-MM-dd HH:mm:ss
     */
    public static final String YYYY_MM_DD_HH_MM_SS = "yyyy-MM-dd HH:mm:ss";

    /**
     * network interface preferred
     */
    public static final String NETWORK_INTERFACE_PREFERRED = "network.interface.preferred";

    /**
     * network IP gets priority, default inner outer
     */
    public static final String NETWORK_PRIORITY_STRATEGY = "network.priority.strategy";

    public static final Boolean KUBERNETES_MODE = !StringUtils.isEmpty(System.getenv("KUBERNETES_SERVICE_HOST")) && !StringUtils.isEmpty(System.getenv("KUBERNETES_SERVICE_PORT"));


}
