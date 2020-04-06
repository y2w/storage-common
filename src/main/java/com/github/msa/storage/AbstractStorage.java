package com.github.msa.storage;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.msa.common.ExceptionTool;
import com.github.msa.endpoint.response.Result;

import cn.hutool.core.thread.ThreadFactoryBuilder;

public abstract class AbstractStorage implements Storage {

	protected static final long PART_SIZE = 2 * 1024 * 1024L; // 2MB

	protected static final String RESULT_SUCC = "上传文件成功";

	public static final int OBJECT_NAME_MAX_LENGTH = 1024;
	
	public static final String SLASH="/";
	
	private   Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private final String id;
	
	private ThreadPoolExecutor  executor = new ThreadPoolExecutor(5, 30, 5, TimeUnit.SECONDS, new LinkedBlockingDeque<Runnable>(20),
			new ThreadFactoryBuilder().setNamePrefix("Storage Async Upload -").build(),
			new ThreadPoolExecutor.AbortPolicy());
	

	public AbstractStorage(String id) {
		this.id = id;
	}

	@Override
	public String id() {
		return this.id;
	}

	@Override
	public String toString() {
		return id();
	}

	@Override
	public Result putObject(InputStream instream, String fileName, String key) {
		return putObject(instream, fileName, key, true);
	}
	
	
	public void asyncPutObjects(List<AsyncUploadEntity> entitys,AsyncUploadCallback callback) {
		if(entitys!=null&&!entitys.isEmpty()) {
			logger.info("线程池数，{}，任务数{}",executor.getPoolSize(),executor.getQueue().size());
			Future<String> result = executor.submit(new AsyncUploadGroup(entitys, executor, this));
			executor.execute(()->{
				boolean flag = true;
				try {
					flag= AsyncUploadGroup.FAILED.equals(result.get());
				} catch (InterruptedException|ExecutionException e) {
					logger.info("获取线程返回结果失败{}",ExceptionTool.getStackTraceAsString(e));
					Thread.currentThread().interrupt();
					flag= false;
				} 
				if(flag&&callback!=null) {
					callback.execute();
				}
			});
		}
	}
	
	@Override
	public String download(String key) {
		return download(key,true);
	}

	protected boolean validateKey(String key) {

		if (key == null || key.length() == 0) {
			return false;
		}
		byte[] bytes = key.getBytes(StandardCharsets.UTF_8);
		char keyChars[] = key.toCharArray();
		char firstChar = keyChars[0];
		if (firstChar == '/' || firstChar == '\\') {
			return false;
		}
		return (bytes.length > 0 && bytes.length < OBJECT_NAME_MAX_LENGTH);
	}

	protected void ensureKeyValid(String key) {
		if (!validateKey(key)) {
			throw new IllegalArgumentException(
					"The object key {" + key + "} is invalid. An object name cannot begin with / or \\");
		}
	}
}
