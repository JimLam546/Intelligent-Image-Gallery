// package com.jim.yunPicture.config;
//
// import com.aliyun.oss.OSS;
// import com.aliyun.oss.OSSClient;
// import com.aliyun.oss.OSSClientBuilder;
// import com.aliyun.oss.common.auth.*;
// import com.aliyuncs.exceptions.ClientException;
// import lombok.Data;
// import org.springframework.boot.context.properties.ConfigurationProperties;
// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;
//
// /**
//  * @author Jim_Lam
//  * @description OSSClientConfig
//  */
//
// @Configuration
// @ConfigurationProperties(prefix = "oss.client")
// @Data
// public class OSSClientConfig {
//     private String endpoint;
//     private String bucketName;
//
//     private String accessKeyId;
//     private String accessKeySecret;
//     // 替换为您的 Bucket 区域
//     private String region;
//
//     @Bean
//     public OSS ossClient() throws ClientException {
//         // // 创建 OSSClient 实例
//         CredentialsProvider credentialsProvider = new DefaultCredentialProvider(accessKeyId, accessKeySecret);
//         OSS ossClient = OSSClientBuilder.create()
//                 .endpoint(endpoint)
//                 .credentialsProvider(credentialsProvider)
//                 .region(region)
//                 .build();
//         return ossClient;
//         // todo 资源的关闭是每次连接建立后关闭还是全局一个连接
//     }
// }