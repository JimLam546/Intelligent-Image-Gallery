package com.jim.yunPicture.config;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.region.Region;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Jim_Lam
 * @description COSClientConfig
 */

@Configuration
@ConfigurationProperties(prefix = "cos.client")
@Data
public class COSClientConfig {

    /**
     * 域名
     */
    private String host;

    private String secretId;

    /**
     * 秘钥
     */
    private String secretKey;

    /**
     * 桶名
     */
    private String bucketName;

    /**
     * 地域
     */
    private String region;

    // 创建 COSClient 实例，这个实例用来后续调用请求
    @Bean
    public COSClient cosClient() {
        // 设置用户身份信息
        COSCredentials cred = new BasicCOSCredentials(secretId, secretKey);

        // ClientConfig 中包含了后续请求 COS 的客户端设置
        ClientConfig clientConfig = new ClientConfig();

        // 设置 bucket 的地域
        clientConfig.setRegion(new Region(region));

        // 生成 cos 客户端
        return new COSClient(cred, clientConfig);
    }
}