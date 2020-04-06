package com.github.msa.storage.fastdfs;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.ResourceUtils;

import com.github.msa.endpoint.response.Result;
import com.github.msa.storage.AbstractStorageIntegrationTests;
import com.github.msa.storage.ObjectMetadata;
import com.github.msa.storage.Storage;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@ActiveProfiles("fastdfs")
@TestMethodOrder(OrderAnnotation.class)
public class FastdfsStorageIntegrationTests extends AbstractStorageIntegrationTests {

	@Test
	@Order(1)
	void putObjectFile() throws Exception {
		File file = ResourceUtils.getFile(UPLOAD_OBJECT);
		storage.putObject(file, OBJECT_KEY);
	}

	@Test
	@Order(2)
	void putObjectInputStreamOverride() throws Exception {
		File file = ResourceUtils.getFile(UPLOAD_OBJECT);
		try (FileInputStream inputStream = new FileInputStream(file)) {
			Result result = storage.putObject(inputStream, file.getName(), OBJECT_KEY, false);
			log.info(result.toString());
			assertThat(result.getCode()).isEqualTo(Storage.CODE_EXIST);
		}
	}

	@Test
	@Order(3)
	void putObjectFileFailed() {
		Exception ex = assertThrows(RuntimeException.class, () -> {
			File file = new File(UPLOAD_OBJECT_FAILED);
			storage.putObject(file, OBJECT_KEY);
		});
		assertThat(ex.getMessage()).contains("FileNotFoundException");
	}

	@Test
	@Order(4)
	void copyObject() {
		boolean result = storage.copyObject(OBJECT_KEY, OBJECT_KEY_FAILED);
		assertThat(result).isEqualTo(true);
		storage.deleteObject(OBJECT_KEY_FAILED);
	}

	@Test
	@Order(5)
	void download() {
		String url = storage.download(OBJECT_KEY);
		assertThat(url).isNotEmpty();
	}

	@Test
	@Order(10)
	void getObjectMetadata() {
		ObjectMetadata metaData = storage.getObjectMetadata(OBJECT_KEY);
		assertThat(metaData).isNotEqualTo(ObjectMetadata.EMPTY);
		log.info(metaData.toString());
		assertThat(metaData.getMd5()).isNotNull();
	}

	@Test
	@Order(10)
	void getObjectMetadataFailed() {
		ObjectMetadata metaData = storage.getObjectMetadata(OBJECT_KEY_FAILED);
		assertThat(metaData).isEqualTo(ObjectMetadata.EMPTY);
		log.info(metaData.toString());
		assertThat(metaData.getMd5()).isEmpty();
	}

	@Test
	@Order(20)
	void getObjectInputStream() {
		InputStream inputStream = storage.getObject(OBJECT_KEY);
		assertThat(inputStream).isNotNull();
		try {
			inputStream.close();
		} catch (IOException e) {
		}
	}

	@Test
	@Order(20)
	void getObjectInputStreamFailed() {
		Exception ex = assertThrows(RuntimeException.class, () -> {
			storage.getObject(OBJECT_KEY_FAILED);
		});
		assertThat(ex.getMessage()).contains("404");
	}

	@Test
	@Order(22)
	void getObjectFile() {
		File file = new File(sharedTempDir, FILE_NAME);
		log.info(file.getAbsolutePath());
		long size = storage.getObject(OBJECT_KEY, file);
		assertThat(size).isGreaterThan(0);
		assertThat(file.exists()).isEqualTo(true);

	}

	@Test
	@Order(23)
	void getObjectFileFailed() {
		Exception ex = assertThrows(RuntimeException.class, () -> {
			File file = new File(sharedTempDir, FILE_NAME);
			log.info(file.getAbsolutePath());
			storage.getObject(OBJECT_KEY_FAILED, file);
		});
		assertThat(ex.getMessage()).contains("404");
	}

	@Test
	@Order(30)
	void doesObjectExist() {
		boolean exist = storage.doesObjectExist(OBJECT_KEY);
		assertThat(exist).isTrue();
	}

	@Test
	@Order(40)
	void deleteObject() {
		boolean result = storage.deleteObject(OBJECT_KEY);
		assertThat(result).isTrue();
	}

	@Test
	@Order(41)
	void deleteObjectFailed() {
		boolean result = storage.deleteObject(OBJECT_KEY_FAILED);
		assertThat(result).isFalse();
	}

	@Tag("go-fastdfs uploadPart有异常 待沟通")
	@Disabled
	@Test
	@Order(50)
	void uploadPart() throws Exception {
		File file = ResourceUtils.getFile("E:\\logs\\mat.zip");
		storage.uploadPart(file, "com/github/msa/storage/mat.zip", null);
	}
}
