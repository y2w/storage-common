package com.github.msa.storage.autoconfigure;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.aliyun.oss.OSSClient;
import com.github.msa.storage.Storage;

import cn.hutool.http.HttpUtil;

@Configuration
@EnableConfigurationProperties(StorageProperties.class)
public class StorageAutoConfiguration {

	public StorageAutoConfiguration() {}
	
	@Configuration
	@ConditionalOnClass(OSSClient.class)
	@ConditionalOnExpression("'${storage.active}'.equals('aliyun')")
	static class AliyunConfiguration {
		@Bean
		public Storage storage(StorageProperties properties) {
			return Storage.forId(properties.getActive(), properties.getAliyun());
		}
	}

	@Configuration
	@ConditionalOnClass(HttpUtil.class)
	@ConditionalOnExpression("'${storage.active}'.equals('fastdfs')")
	static class FastdfsConfiguration {
		@Bean
		public Storage storage(StorageProperties properties) {
			return Storage.forId(properties.getActive(), properties.getFastdfs());
		}
	}

	@Configuration
	@ConditionalOnExpression("'${storage.active}'.equals('filesystem')")
	static class FileSystemConfiguration {
		@Bean
		public Storage storage(StorageProperties properties) {
			return Storage.forId(properties.getActive(), properties.getFilesystem());
		}
	}
}
