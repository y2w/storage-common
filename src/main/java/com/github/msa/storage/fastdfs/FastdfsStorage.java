package com.github.msa.storage.fastdfs;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.msa.endpoint.response.Result;
import com.github.msa.endpoint.response.ResultCode;
import com.github.msa.storage.AbstractStorage;
import com.github.msa.storage.ObjectMetadata;

import cn.hutool.core.io.resource.InputStreamResource;
import cn.hutool.http.HttpException;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import io.tus.java.client.ProtocolException;
import io.tus.java.client.TusClient;
import io.tus.java.client.TusExecutor;
import io.tus.java.client.TusURLMemoryStore;
import io.tus.java.client.TusUpload;
import io.tus.java.client.TusUploader;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FastdfsStorage extends AbstractStorage {

	public static final String ID = "fastdfs";

	private FastdfsConfig config;

	private static final String STATUS = "status";
	private static final String STATUS_OK = "ok";

	public FastdfsStorage(FastdfsConfig config) {
		super(ID);
		if (StringUtils.isAnyBlank(config.getUrl()))
			throw new IllegalStateException("'storage.fastdfs.*' properties are not set!");
		this.config = config;
	}

	@Override
	public boolean doesObjectExist(String key) {
		Assert.notNull(key, "key");
		ensureKeyValid(key);
		JSONObject parseObj = getFileInfo(key);
		return STATUS_OK.equals(parseObj.get(STATUS));
	}

	@Override
	public Result putObject(File file, String key) {
		Assert.notNull(file, "file is null");
		Assert.isTrue(file.isFile() && file.exists(), "File FileNotFoundException or Is a Path");
		String newKey = convertKey(key, file.getName());
		Assert.notNull(newKey, "key");
		ensureKeyValid(newKey);
		upload(newKey, file);
		return new Result(ResultCode.SUCCESS, RESULT_SUCC);
	}

	@Override
	public Result putObject(InputStream instream, String fileName, String key, boolean override) {
		Assert.notNull(key, "key");
		ensureKeyValid(key);
		if (key.indexOf(fileName) > -1) {
			key = key.substring(0, key.indexOf(fileName));
		}
		/** go fast 需要管理员才可以查文件存在与否 不开去重情况，可以注释此判断 */
		/**
		 * if (!override && doesObjectExist(key)) {
		 * log.info("[fastdfs]-[{}]已存在，本次上传跳过！", key); return new Result(CODE_EXIST,
		 * "fastdfs:" + key + "，已存在，本次上传跳过！"); }
		 */
		String result = upload(key, new InputStreamResource(instream, fileName));
		return new Result(ResultCode.SUCCESS, result);
	}

	@Override
	public Result uploadPart(File file, String key, Long partSize) {
		Assert.notNull(key, "key");
		ensureKeyValid(key);
		try {
			TusClient client = new TusClient();
			client.setUploadCreationURL(new URL(config.fullUrl(FastdfsConfig.BIG_UPLOAD)));
			client.enableResuming(new TusURLMemoryStore());
			
			final TusUpload upload = new TusUpload();
			upload.setInputStream(new FileInputStream(file));
			TusExecutor executor = new TusExecutor() {
				@Override
				protected void makeAttempt() throws ProtocolException, IOException {
					TusUploader uploader = client.createUpload(upload);
					uploader.uploadChunk();
					uploader.finish();
					if (log.isDebugEnabled()) {
						log.debug("Upload finished.");
						log.debug("Upload available at: %s", uploader.getUploadURL().toString());
					}
				}
			};
			executor.makeAttempts();
			return new Result(ResultCode.SUCCESS, RESULT_SUCC);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return new Result(ResultCode.APP_FAIL, e.getMessage());
		}

	}

	@Override
	public InputStream getObject(String key) {
		Assert.notNull(key, "key");
		ensureKeyValid(key);
		final HttpResponse response = HttpRequest.get(config.fullUrl(groupKey(key))).executeAsync();
		if (!response.isOk()) {
			throw new HttpException("Server response error with status code: [{}]", response.getStatus());
		}
		return response.bodyStream();
	}

	@Override
	public long getObject(String key, File file) {
		return HttpUtil.downloadFile(config.fullUrl(groupKey(key)), file);
	}

	@Override
	public ObjectMetadata getObjectMetadata(String key) {
		Assert.notNull(key, "key");
		ensureKeyValid(key);
		JSONObject parseObj = getFileInfo(key);
		if (STATUS_OK.equals(parseObj.get(STATUS))) {
			JSONObject data = parseObj.getJSONObject("data");
			return ObjectMetadata.builder().md5(data.getString("md5")).key(data.getString("key"))
					.name(data.getString("name")).size(data.getLong("size")).build();
		}

		return ObjectMetadata.EMPTY;
	}

	@Override
	public boolean deleteObject(String key) {
		Assert.notNull(key, "key");
		ensureKeyValid(key);
		JSONObject parseObj = JSON.parseObject(HttpUtil.post(config.fullUrl(FastdfsConfig.DELETE), buildPath(key)));
		return STATUS_OK.equals(parseObj.get(STATUS));
	}

	@Override
	public String download(String key, boolean flag) {
		Assert.notNull(key, "key");
		ensureKeyValid(key);
		String download = config.fullUrl(groupKey(key));
		return flag ? download + "?download=0" : download;
	}

	@Override
	public boolean copyObject(String srcKey, String destKey) {
		InputStream inputream = null;
		try {
			inputream = getObject(srcKey);
			putObject(inputream, "", destKey);
			return true;
		} catch (Exception ex) {
			log.error(ex.getMessage(), ex);
			return false;

		} finally {
			if (inputream != null)
				try {
					inputream.close();
				} catch (IOException e) {
					log.error(e.getMessage(), e);
				}
		}

	}

	private String upload(String key, Object inputStream) {
		Map<String, Object> paramMap = new HashMap<>();
		paramMap.put("file", inputStream);
		paramMap.put("output", "json");
		paramMap.put("path", key);
		paramMap.put("scene", "json");
		return HttpUtil.post(config.fullUrl(FastdfsConfig.UPLOAD), paramMap);
	}

	private JSONObject getFileInfo(String key) {
		return JSON.parseObject(HttpUtil.post(config.fullUrl(FastdfsConfig.GET_FILE_INFO), buildPath(key)));
	}

	private Map<String, Object> buildPath(String key) {
		HashMap<String, Object> param = new HashMap<>(1);
		param.put("path", groupKey(key));
		return param;
	}

	private String convertKey(String key, String fileName) {
		String tempFile = SLASH + fileName;
		if (StringUtils.endsWithIgnoreCase(key, tempFile)) {
			int endIndex = StringUtils.lastIndexOfIgnoreCase(key, tempFile);
			if (endIndex > 0) {
				return key.substring(0, endIndex);
			}
		}
		return key;
	}

	private String groupKey(String key) {
		if (StringUtils.isNotEmpty(config.getGroup())) {
			return SLASH + config.getGroup() + SLASH + key;
		}
		return key;
	}

}
