package com.github.msa.storage;

public interface StorageFactory {

	public Storage createStorage(String id,StorageConfig config);
}
