package com.github.msa.storage.autoconfigure;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import com.github.msa.storage.Storage;

class StorageAutoConfigurationTest {
	
	private final String ACTIVE="storage.active=%s";

	private final String[] FILESYSTEM_PAIRS = {String.format(ACTIVE, "filesystem"),"storage.filesystem.path=F:\\test\\data"};

	private final String[] FASTDFS_PAIRS =  {String.format(ACTIVE, "fastdfs"),"storage.fastdfs.url=http://127.0.0.1:18080"};
	
	private final String[] ALIYUN_PAIRS = { String.format(ACTIVE, "aliyun"), "storage.aliyun.bucketName=test",
			"storage.aliyun.endpoint=http://oss-cn-shanghai.aliyuncs.com", "storage.aliyun.accessKeyId=test",
			"storage.aliyun.accessKeySecret=test" };
	
	
	private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
			.withConfiguration(AutoConfigurations.of(StorageAutoConfiguration.class));

	@Test
	void testFilesystemConfiguration() {
		this.contextRunner.withPropertyValues(FILESYSTEM_PAIRS)
				.withUserConfiguration(StorageAutoConfiguration.FileSystemConfiguration.class).run((context) -> {
					assertThat(context).hasSingleBean(Storage.class);
					assertThat(context).getBeanNames(Storage.class).hasSize(1);
				});
	}
	
	@Test
	void testFilesystemConfigurationFailed() {
		this.contextRunner.withPropertyValues(String.format(ACTIVE, "filesystem"))
				.withUserConfiguration(StorageAutoConfiguration.FileSystemConfiguration.class).run((context) -> {
					assertThat(context).hasFailed();
				});
	}

	@Test
	void testFastdfsConfiguration() {
		this.contextRunner.withPropertyValues(FASTDFS_PAIRS)
				.withUserConfiguration(StorageAutoConfiguration.FastdfsConfiguration.class).run((context) -> {
					assertThat(context).hasSingleBean(Storage.class);
					assertThat(context).getBeanNames(Storage.class).hasSize(1);
				});
	}
	
	@Test
	void testFastdfsConfigurationFailed() {
		this.contextRunner.withPropertyValues(String.format(ACTIVE, "fastdfs"))
				.withUserConfiguration(StorageAutoConfiguration.FastdfsConfiguration.class).run((context) -> {
					assertThat(context).hasFailed();
				});
	}


	@Test
	void testAliyunConfiguration() {
		this.contextRunner.withPropertyValues(ALIYUN_PAIRS)
				.withUserConfiguration(StorageAutoConfiguration.AliyunConfiguration.class).run((context) -> {
					assertThat(context).hasSingleBean(Storage.class);
					assertThat(context).getBeanNames(Storage.class).hasSize(1);
				});
	}
	
	@Test
	void testAliyunConfigurationFailed() {
		this.contextRunner.withPropertyValues(String.format(ACTIVE, "aliyun"))
				.withUserConfiguration(StorageAutoConfiguration.AliyunConfiguration.class).run((context) -> {
					assertThat(context).hasFailed();
				});
	}
}
