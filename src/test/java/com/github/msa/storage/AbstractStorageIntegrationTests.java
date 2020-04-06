package com.github.msa.storage;

import com.github.msa.storage.autoconfigure.StorageAutoConfiguration;

import java.io.File;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = StorageAutoConfiguration.class)
public abstract class AbstractStorageIntegrationTests {

	protected static String UPLOAD_OBJECT = "classpath:favicon.ico";

	protected static String UPLOAD_OBJECT_FAILED = "classpath:error.ico";

	protected static String OBJECT_KEY = "product/com/github/msa/storage/favicon.ico";
	
	protected static String OBJECT_KEY_FAILED = "product/com/github/msa/storage/error.ico";
	
	protected static String FILE_NAME = "favicon_new.ico";

	@TempDir
	protected static File sharedTempDir;

	@Autowired
	protected Storage storage;

}
