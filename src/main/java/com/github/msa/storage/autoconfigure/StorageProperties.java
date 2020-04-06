package com.github.msa.storage.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import com.github.msa.storage.aliyun.AliyunOssConfig;
import com.github.msa.storage.fastdfs.FastdfsConfig;
import com.github.msa.storage.filesystem.FileSystemConfig;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ConfigurationProperties(prefix = "storage")
public class StorageProperties {

	private String active;

	@NestedConfigurationProperty
	private FileSystemConfig filesystem;

	@NestedConfigurationProperty
	private FastdfsConfig fastdfs;

	@NestedConfigurationProperty
	private AliyunOssConfig aliyun;

}
