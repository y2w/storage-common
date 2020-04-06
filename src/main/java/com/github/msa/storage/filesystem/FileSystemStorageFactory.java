package com.github.msa.storage.filesystem;

import com.github.msa.storage.Storage;
import com.github.msa.storage.StorageConfig;
import com.github.msa.storage.StorageFactory;

public class FileSystemStorageFactory implements StorageFactory {
	
	@Override
	public Storage createStorage(String id,StorageConfig config) {
		if (FileSystemStorage.ID.equals(id)) {
			return new FileSystemStorage((FileSystemConfig)config);
		}
		return null;
	}
}
