package com.github.msa.storage.aliyun;

import com.github.msa.storage.Storage;
import com.github.msa.storage.StorageConfig;
import com.github.msa.storage.StorageFactory;

public class AliyunStorageFactory implements StorageFactory {

	@Override
	public Storage createStorage(String id,StorageConfig config) {
		
		if (AliyunStorage.ID.equals(id)) {
			return new AliyunStorage((AliyunOssConfig)config);
		}
		return null;
	}

}
