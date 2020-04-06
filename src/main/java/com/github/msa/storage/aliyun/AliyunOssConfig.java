package com.github.msa.storage.aliyun;

import com.github.msa.storage.StorageConfig;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AliyunOssConfig implements StorageConfig {

	private String bucketName;

	private String endpoint;

	private String accessKeyId;

	private String accessKeySecret;

	private boolean requestTimeoutEnabled;
	
	private long expiration;
}