package com.github.msa.storage.filesystem;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;

import com.github.msa.common.ExceptionTool;
import com.github.msa.endpoint.response.Result;
import com.github.msa.endpoint.response.ResultCode;
import com.github.msa.storage.AbstractStorage;
import com.github.msa.storage.ObjectMetadata;

public class FileSystemStorage extends AbstractStorage {

	public static final String ID = "filesystem";
	private FileSystemConfig config;

	public FileSystemStorage(FileSystemConfig config) {
		super(ID);
		if (StringUtils.isAnyBlank(config.getPath()))
			throw new IllegalStateException("'storage.filesystem.*' properties are not set!");
		this.config = config;
	}

	@Override
	public boolean doesObjectExist(String key) {
		Assert.notNull(key, "key");
		ensureKeyValid(key);
		return Files.exists(Paths.get(config.fullPath(key)));
	}

	@Override
	public Result putObject(File file, String key) {
		Assert.notNull(file, "file");
		Assert.notNull(key, "key");
		ensureKeyValid(key);
		try {
			Path destPath = Paths.get(config.fullPath(key));
			createIfNotExist(destPath);
			Files.copy(file.toPath(), destPath, StandardCopyOption.REPLACE_EXISTING);
			return new Result(ResultCode.SUCCESS, RESULT_SUCC);
		} catch (IOException e) {
			throw ExceptionTool.unchecked(e);
		}
	}

	@Override
	public Result putObject(InputStream instream, String fileName, String key, boolean override) {
		Assert.notNull(key, "key");
		Assert.notNull(instream, "instream");
		ensureKeyValid(key);
		Path destPath = Paths.get(config.fullPath(key));
		if (!override && Files.exists(destPath)) {
			return new Result(CODE_EXIST, "fileName:" + fileName + "，已存在，本次上传跳过！");
		}
		try {
			createIfNotExist(destPath);
			Files.copy(instream, destPath);
			return new Result(ResultCode.SUCCESS, RESULT_SUCC);
		} catch (IOException e) {
			throw ExceptionTool.unchecked(e);
		}
	}

	@Override
	public Result uploadPart(File file, String key, Long partSize) {
		try {
			return putObject(new FileInputStream(file), "", key, true);
		} catch (FileNotFoundException e) {
			throw ExceptionTool.unchecked(e);
		}
	}

	@Override
	public InputStream getObject(String key) {
		Assert.notNull(key, "key");
		ensureKeyValid(key);
		try {
			return Files.newInputStream(Paths.get(config.fullPath(key)));
		} catch (IOException e) {
			throw ExceptionTool.unchecked(e);
		}
	}

	@Override
	public ObjectMetadata getObjectMetadata(String key) {
		Assert.notNull(key, "key");
		ensureKeyValid(key);
		File currFile = new File(config.fullPath(key));
		if (currFile.exists()) {
			try (FileInputStream fis = new FileInputStream(currFile)) {
				return ObjectMetadata.builder().md5(DigestUtils.md5Hex(fis)).key(key).name(currFile.getName())
						.size(currFile.length()).build();
			} catch (Exception e) {
				throw ExceptionTool.unchecked(e);
			}
		}
		return ObjectMetadata.EMPTY;
	}

	@Override
	public boolean deleteObject(String key) {
		Assert.notNull(key, "key");
		ensureKeyValid(key);
		try {
			return Files.deleteIfExists(Paths.get(config.fullPath(key)));
		} catch (IOException e) {
			throw ExceptionTool.unchecked(e);
		}
	}

	@Override
	public long getObject(String key, File file) {
		Assert.notNull(key, "key");
		ensureKeyValid(key);
		try {
			File sourceFile = new File(config.fullPath(key));
			Files.copy(sourceFile.toPath(), file.toPath());
			return sourceFile.length();
		} catch (IOException e) {
			throw ExceptionTool.unchecked(e);
		}
	}

	@Override
	public String download(String key, boolean flag) {
		Assert.notNull(key, "key");
		ensureKeyValid(key);
		if (StringUtils.isEmpty(config.getUrl())) {
			throw new IllegalStateException("'storage.filesystem.url' properties are not set!");
		}
		return config.getUrl() + SLASH + key;
	}

	@Override
	public boolean copyObject(String srcKey, String destKey) {
		Assert.notNull(srcKey, "srcKey");
		ensureKeyValid(srcKey);
		Assert.notNull(destKey, "destKey");
		ensureKeyValid(destKey);
		try {
			Files.copy(Paths.get(config.fullPath(srcKey)), Paths.get(config.fullPath(destKey)));
		} catch (IOException e) {
			throw ExceptionTool.unchecked(e);
		}
		return true;
	}

	private void createIfNotExist(Path path) throws IOException {
		if (Files.exists(path))
			return;
		Path parent = path.getParent();
		if (!Files.exists(parent))
			Files.createDirectories(parent);
	}

}
