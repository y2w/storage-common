package com.github.msa.storage.filesystem;

import java.io.File;

import com.github.msa.storage.StorageConfig;

public class FileSystemConfig implements StorageConfig {

	private String path;

	private String url;

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String fullPath(String file) {
		return path + File.separator + file;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}
}
