package io.datavines.server.coordinator.api.config;

import com.google.code.kaptcha.impl.DefaultKaptcha;
import com.google.code.kaptcha.util.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.Properties;

@Configuration
public class KaptchaConfig {

    @Value("${kaptcha.border:yes}")
    private String kaptchaBorder;

    @Value("${kaptcha.border.color:105,179,90}")
    private String kaptchaBorderColor;

    @Value("${kaptcha.textproducer.font.color:blue}")
    private String kaptchaTextproducerFontColor;

    @Value("${kaptcha.image.width:125}")
    private String kaptchaImageWidth;

    @Value("${kaptcha.image.height:65}")
    private String kaptchaImageHeight;

    @Value("${kaptcha.textproducer.font.size:45}")
    private String kaptchaTextproducerFontSize;

    @Value("${kaptcha.session.key:code}")
    private String kaptchaSessionKey;

    @Value("${kaptcha.textproducer.char.length:4}")
    private String getKaptchaTextproducerCharLength;

    @Value("${kaptcha.textproducer.font.names:STSong,STSong,Microsoft YaHei}")
    private String kaptchaTextproducerFontNames;

    @Bean
    public DefaultKaptcha captchaProducer(){
        DefaultKaptcha defaultKaptcha = new DefaultKaptcha();
        Properties properties = new Properties();
        properties.setProperty("kaptcha.border", kaptchaBorder);
        properties.setProperty("kaptcha.border.color", kaptchaBorderColor);
        properties.setProperty("kaptcha.textproducer.font.color", kaptchaTextproducerFontColor);
        properties.setProperty("kaptcha.image.width", kaptchaImageWidth);
        properties.setProperty("kaptcha.image.height", kaptchaImageHeight);
        properties.setProperty("kaptcha.textproducer.font.size", kaptchaTextproducerFontSize);
        properties.setProperty("kaptcha.session.key", kaptchaSessionKey);
        properties.setProperty("kaptcha.textproducer.char.length", getKaptchaTextproducerCharLength);
        properties.setProperty("kaptcha.textproducer.font.names", kaptchaTextproducerFontNames);
        Config config = new Config(properties);
        defaultKaptcha.setConfig(config);
        return defaultKaptcha;
    }
}
