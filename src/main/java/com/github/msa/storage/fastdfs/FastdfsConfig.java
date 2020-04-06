package com.github.msa.storage.fastdfs;

import org.apache.commons.lang3.StringUtils;

import com.github.msa.storage.StorageConfig;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FastdfsConfig implements StorageConfig {

	/** 系统信息api */
	public static final String STATUS = "/status";
	/** 统计信息api */
	public static final String STAT = "/stat";
	/** 上传文件api */
	public static final String UPLOAD = "/upload";

	public static final String BIG_UPLOAD = "/big/upload";
	/** 删除文件api */
	public static final String DELETE = "/delete";
	/** 修复统计信息api */
	public static final String REPAIR_STAT = "/repair_stat";
	/** 删除空目录api */
	public static final String REMOVE_EMPTY_DIR = "/remove_empty_dir";
	/** 备份元数据api */
	public static final String BACKUP = "/backup";
	/** 同步失败修复api */
	public static final String REPAIR = "/repair";
	/** 文件列表api */
	public static final String LIST_DIR = "/list_dir";
	/** 文件信息api */
	public static final String GET_FILE_INFO = "/get_file_info";

	private String url;

	private String group;

	private boolean supportGroupManage;

	public String fullUrl(String api) {
		if (supportGroupManage && StringUtils.isNotEmpty(group) && !api.startsWith("/" + group)) {
			return url + "/" + group + api;
		}
		return url + api;

	}
}
