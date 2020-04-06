package com.github.msa.storage.fastdfs;

import com.github.msa.storage.Storage;
import com.github.msa.storage.StorageConfig;
import com.github.msa.storage.StorageFactory;

public class FastdfsStorageFactory implements StorageFactory {

	@Override
	public Storage createStorage(String id,StorageConfig config) {
		if (FastdfsStorage.ID.equals(id)) {
			return new FastdfsStorage((FastdfsConfig)config);
		}
		return null;
	}

}
