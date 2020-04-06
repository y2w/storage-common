package com.github.msa.storage;

import java.io.File;
import java.io.InputStream;
import java.util.List;
import java.util.Objects;

import org.springframework.core.io.support.SpringFactoriesLoader;

import com.github.msa.endpoint.response.Result;

public interface Storage {

	static final int CODE_EXIST = 201;

	/**
	 * 判断key是否存在于存储空间
	 * 
	 * @param key
	 * @return
	 */
	boolean doesObjectExist(String key);

	/**
	 * 文件上传,上传成功返回200,存在则返回201,失败返回500
	 * 
	 * @param file 本地文件
	 * @param key  存储Key
	 * @return
	 */
	Result putObject(File file, String key);

	/**
	 * 流方式上传,返回值同文件上传
	 * 
	 * @param instream 文件流
	 * @param fileName 文件名称
	 * @param key      存储Key
	 * @param override 是否覆盖上传
	 * @return
	 */
	Result putObject(InputStream instream, String fileName, String key, boolean override);

	/**
	 * 流方式上传,返回值同文件上传
	 * 
	 * @param instream 文件流
	 * @param fileName 文件名称
	 * @param key      存储Key
	 * @return
	 */
	Result putObject(InputStream instream, String fileName, String key);

	void asyncPutObjects(List<AsyncUploadEntity> entitys, AsyncUploadCallback callback);

	Result uploadPart(File file, String key, Long partSize);

	/**
	 * 下载文件
	 * 
	 * @param key
	 * @param file
	 * @return
	 */
	long getObject(String key, File file);

	/**
	 * 下载文件流,需要手动关闭
	 * 
	 * @param key 存储Key
	 * @return
	 */
	InputStream getObject(String key);

	/**
	 * 删除文件
	 * 
	 * @param key 存储Key
	 * @return
	 */
	boolean deleteObject(String key);

	/**
	 * 复制文件
	 * 
	 * @param srcKey  源存储Key
	 * @param destKey 目标存储Key
	 * @return
	 */
	boolean copyObject(String srcKey, String destKey);

	/**
	 * 返回存储Key的http连接地址，实例为oss，是flag为true则返回cdn地址，否则返回oss地址
	 * 
	 * @param key  存储Key
	 * @param flag 可变化值
	 * @return
	 */
	String download(String key, boolean flag);

	/**
	 * 返回存储Key的http连接地址，flag为false
	 * 
	 * @param key  存储Key
	 * @param flag false
	 * @return
	 */
	String download(String key);

	/**
	 * 获取对象数据
	 * 
	 * @param key 存储Key
	 * @return
	 */
	ObjectMetadata getObjectMetadata(String key);

	String id();

	static Storage forId(String id, StorageConfig config) {
		return SpringFactoriesLoader.loadFactories(StorageFactory.class, StorageFactory.class.getClassLoader()).stream()
				.map((factory) -> factory.createStorage(id, config)).filter(Objects::nonNull).findFirst()
				.orElseThrow(() -> new IllegalStateException("Unrecognized storage id '" + id + "'"));
	}

}