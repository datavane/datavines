package io.datavines.notification.plugin.wecombot.entity;

import io.datavines.common.utils.JSONUtils;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;


@Data
public class WecomBotRes implements Serializable {
    private static final long serialVersionUID = -1L;

    private String errcode;
    private String errmsg;

    /**
     * check req success
     * @return check req success
     */
    public boolean success() {
        return StringUtils.equalsIgnoreCase(errcode, "0");
    }

    public static WecomBotRes parseFromJson(String json) {
        return JSONUtils.parseObject(json, WecomBotRes.class);
    }

}