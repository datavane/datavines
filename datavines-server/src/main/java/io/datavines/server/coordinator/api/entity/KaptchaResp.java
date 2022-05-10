package io.datavines.server.coordinator.api.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author TGspace
 * @ClassName KaptchaResp.java
 * @Description
 * @createTime 2022-05-09 16:47:00
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KaptchaResp implements Serializable {

    private String imageByte64;

    private String verificationCodeJwt;

}
