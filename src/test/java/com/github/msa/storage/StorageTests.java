package com.github.msa.storage;

import org.junit.jupiter.api.Test;

import com.github.msa.storage.aliyun.AliyunOssConfig;
import com.github.msa.storage.aliyun.AliyunStorage;
import com.github.msa.storage.fastdfs.FastdfsConfig;
import com.github.msa.storage.fastdfs.FastdfsStorage;
import com.github.msa.storage.filesystem.FileSystemConfig;
import com.github.msa.storage.filesystem.FileSystemStorage;

import static org.assertj.core.api.Assertions.assertThat;

public class StorageTests {

	@Test
	void filesystemStorage() {
		FileSystemConfig config = new FileSystemConfig();
		config.setPath("filesystem-path");
		Storage fileSystem = Storage.forId(FileSystemStorage.ID, config);
		assertThat(fileSystem).isInstanceOf(FileSystemStorage.class);
		assertThat(fileSystem.id()).isEqualTo("filesystem");
		assertThat(fileSystem.toString()).isEqualTo("filesystem");
	}

	@Test
	void filesystemStorageFailed() {
		FileSystemConfig config = new FileSystemConfig();
		config.setPath("");
		try {
			Storage.forId(FileSystemStorage.ID, config);
		} catch (IllegalStateException ex) {
			assertThat(ex.getMessage()).contains("properties are not set!");
		}
	}

	@Test
	void aliyunStorage() {
		AliyunOssConfig config = new AliyunOssConfig();
		config.setAccessKeyId("aliyun-id");
		config.setAccessKeySecret("aliyun-secret");
		config.setEndpoint("aliyun-endpoint");
		config.setBucketName("aliyun-bucket");
		Storage aliyun = Storage.forId(AliyunStorage.ID, config);
		assertThat(aliyun).isInstanceOf(AliyunStorage.class);
		assertThat(aliyun.id()).isEqualTo("aliyun");
		assertThat(aliyun.toString()).isEqualTo("aliyun");
	}

	@Test
	void aliyunStorageFailed() {
		AliyunOssConfig config = new AliyunOssConfig();
		config.setAccessKeyId("aliyun-id");
		config.setAccessKeySecret("aliyun-secret");
		config.setEndpoint("");
		config.setBucketName("aliyun-bucket");
		try {
			Storage.forId(AliyunStorage.ID, config);
		} catch (IllegalStateException ex) {
			assertThat(ex.getMessage()).contains("properties are not set!");
		}
	}

	@Test
	void fastdfsStorage() {
		FastdfsConfig config = new FastdfsConfig();
		config.setUrl("fastdfs-url");
		Storage fastdfs = Storage.forId(FastdfsStorage.ID, config);
		assertThat(fastdfs).isInstanceOf(FastdfsStorage.class);
		assertThat(fastdfs.id()).isEqualTo("fastdfs");
	}

	@Test
	void fastdfsStorageFailed() {
		FastdfsConfig config = new FastdfsConfig();
		config.setUrl("");
		try {
			Storage.forId(FastdfsStorage.ID, config);
		} catch (IllegalStateException ex) {
			assertThat(ex.getMessage()).contains("properties are not set!");
		}
	}

}
