package io.datavines.server.utils;

import com.google.code.kaptcha.impl.DefaultKaptcha;
import io.datavines.server.DataVinesConstants;
import io.datavines.server.coordinator.api.entity.KaptchaResp;
import io.datavines.server.coordinator.api.enums.ApiStatus;
import io.datavines.server.exception.DataVinesServerException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.misc.BASE64Encoder;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class VerificationUtil {

    private static Logger logger = LoggerFactory.getLogger(VerificationUtil.class);
    private static DefaultKaptcha defaultKaptcha;
    private static TokenManager tokenManager;
    private static final Long timeOutMillis = 60000L;

    static {
        defaultKaptcha = SpringApplicationContext.getBean(DefaultKaptcha.class);
        tokenManager = SpringApplicationContext.getBean(TokenManager.class);
    }

    public static KaptchaResp creatVerificationCodeAndImage() throws DataVinesServerException {
        String verificationCode = defaultKaptcha.createText();
        return KaptchaResp.builder()
                .imageByte64(buildImageByte64(verificationCode))
                .verificationCodeJwt(buildJwtVerification(verificationCode))
                .build();
    }

    public static void validVerificationCode(String verificationCode, String verificationCodeJwt) throws DataVinesServerException {
        Claims claims;
        try {
            claims = tokenManager.getClaims(verificationCodeJwt);
        } catch (ExpiredJwtException e) {
            throw new DataVinesServerException(ApiStatus.EXPIRED_VERIFICATION_CODE);
        }
        Date expiration = claims.getExpiration();
        if(null == expiration || expiration.getTime() < System.currentTimeMillis()){
            throw new DataVinesServerException(ApiStatus.EXPIRED_VERIFICATION_CODE);
        }
        String verificationCodeInJwt = null == claims.get(DataVinesConstants.TOKEN_VERIFICATION_CODE) ? null : claims.get(DataVinesConstants.TOKEN_VERIFICATION_CODE).toString();
        if(null == verificationCodeInJwt || !verificationCodeInJwt.equals(verificationCode)){
            throw new DataVinesServerException(ApiStatus.INVALID_VERIFICATION_CODE);
        }
    }

    private static String buildJwtVerification(String verificationCode) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(DataVinesConstants.TOKEN_VERIFICATION_CODE, verificationCode);
        claims.put(DataVinesConstants.TOKEN_CREATE_TIME, System.currentTimeMillis());
        return tokenManager.toTokenString(timeOutMillis, claims);
    }

    private static String buildImageByte64(String verificationCode) throws DataVinesServerException {
        BufferedImage image = defaultKaptcha.createImage(verificationCode);
        ByteArrayOutputStream outputStream = null;
        byte[] imageInByte = null;
        try {
            outputStream = new ByteArrayOutputStream();
            ImageIO.write(image, "jpg", outputStream);
            outputStream.flush();
            imageInByte = outputStream.toByteArray();
        } catch (IOException e) {
            logger.error("image to byte exception cause of :", e);
        } finally {
            if (null != outputStream) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    logger.error("close outputStream exception cause of :", e);
                }
            }
        }

        String imageByte64;
        BASE64Encoder encoder = new BASE64Encoder();
        if(null == imageInByte){
            throw new DataVinesServerException(ApiStatus.CREAT_VERIFICATION_IMAGE_ERROR);
        }
        imageByte64 = encoder.encodeBuffer(imageInByte).replaceAll("\n", "").replaceAll("\r", "");
        return "data:image/jpg;base64,".concat(imageByte64);
    }
}


