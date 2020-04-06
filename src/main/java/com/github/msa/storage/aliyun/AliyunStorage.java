package com.github.msa.storage.aliyun;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aliyun.oss.ClientConfiguration;
import com.aliyun.oss.HttpMethod;
import com.aliyun.oss.OSSClient;
import com.aliyun.oss.common.auth.CredentialsProvider;
import com.aliyun.oss.common.auth.DefaultCredentialProvider;
import com.aliyun.oss.common.utils.BinaryUtil;
import com.aliyun.oss.model.CompleteMultipartUploadRequest;
import com.aliyun.oss.model.GeneratePresignedUrlRequest;
import com.aliyun.oss.model.GetObjectRequest;
import com.aliyun.oss.model.InitiateMultipartUploadRequest;
import com.aliyun.oss.model.InitiateMultipartUploadResult;
import com.aliyun.oss.model.OSSObject;
import com.aliyun.oss.model.PartETag;
import com.aliyun.oss.model.UploadPartRequest;
import com.aliyun.oss.model.UploadPartResult;
import com.github.msa.endpoint.response.Result;
import com.github.msa.endpoint.response.ResultCode;
import com.github.msa.storage.AbstractStorage;
import com.github.msa.storage.ObjectMetadata;

import cn.hutool.core.io.FileUtil;

public class AliyunStorage extends AbstractStorage {

	public static final String ID = "aliyun";

	private static Logger logger = LoggerFactory.getLogger(AliyunStorage.class);

	private ClientConfiguration configuration;

	private CredentialsProvider credsProvider;

	private AliyunOssConfig ossConfig;

	public AliyunStorage(AliyunOssConfig ossConfig) {
		super(ID);
		this.ossConfig = ossConfig;
		if (!StringUtils.isAnyBlank(ossConfig.getEndpoint(), ossConfig.getAccessKeyId(),
				ossConfig.getAccessKeySecret())) {
			credsProvider = new DefaultCredentialProvider(ossConfig.getAccessKeyId(), ossConfig.getAccessKeySecret());
			configuration = new ClientConfiguration();
			configuration.setRequestTimeoutEnabled(ossConfig.isRequestTimeoutEnabled());
			configuration.setSocketTimeout(25000);
			configuration.setConnectionTimeout(25000);
		} else {
			throw new IllegalStateException("'storage.aliyun.*' properties are not set!");
		}
	}

	@Override
	public boolean doesObjectExist(String key) {
		return ossUtil(ossClient -> {
			return ossClient.doesObjectExist(ossConfig.getBucketName(), key);
		});
	}

	@Override
	public Result putObject(File file, String key) {

		return ossUtil(ossClient -> {
			String bucketName = this.ossConfig.getBucketName();
			try (FileInputStream is = new FileInputStream(file)) {
				byte[] md5bytes = DigestUtils.md5(is);
				String md5base64 = BinaryUtil.toBase64String(md5bytes);
				com.aliyun.oss.model.ObjectMetadata meta = new com.aliyun.oss.model.ObjectMetadata();
				meta.setContentMD5(md5base64);
				if (ossClient.doesObjectExist(bucketName, key)
						&& md5base64.equals(ossClient.getObjectMetadata(bucketName, key).getContentMD5())) {
					return new Result(CODE_EXIST, "oss:" + key + "，已存在，本次上传跳过！");
				}
				ossClient.putObject(bucketName, key, file, meta);
				return new Result(ResultCode.SUCCESS, RESULT_SUCC);
			} catch (Exception e) {
				logger.error("oss 上传文件失败：{}", e.getMessage());
				return new Result(ResultCode.APP_FAIL, e.getMessage());
			}
		});
	}

	@Override
	public Result putObject(InputStream instream, String fileName, String key, boolean override) {

		return ossUtil(ossClient -> {
			String bucketName = this.ossConfig.getBucketName();
			if (!override && ossClient.doesObjectExist(bucketName, key)) {
				logger.info("[bucket:{}]-[{}]已存在，本次上传跳过！", bucketName, key);
				return new Result(3, "bucket:" + bucketName + "-" + key + "，已存在，本次上传跳过！");
			}
			try {
				// 创建上传Object的Metadata
				com.aliyun.oss.model.ObjectMetadata objectMetadata = new com.aliyun.oss.model.ObjectMetadata();
				objectMetadata.setContentLength(instream.available());
				objectMetadata.setCacheControl("no-cache");
				objectMetadata.setHeader("Pragma", "no-cache");
				objectMetadata.addUserMetadata("diskurl", key);
				objectMetadata.setContentDisposition(
						"filename/filesize=" + fileName + SLASH + instream.available() + "Byte.");
				// 上传文件
				if (!ossClient.doesObjectExist(bucketName, key) || override) {// 文件存在
					ossClient.putObject(bucketName, key, instream, objectMetadata);
				}
				return new Result(ResultCode.SUCCESS, RESULT_SUCC);
			} catch (IOException e) {
				logger.error(e.getMessage(), e);
				return new Result(ResultCode.APP_FAIL, "上传失败：" + e.getMessage());
			}
		});
	}

	@Override
	public Result uploadPart(File file, String key, Long partSize) {
		return ossUtil(ossClient -> {
			InitiateMultipartUploadRequest request = new InitiateMultipartUploadRequest(ossConfig.getBucketName(), key);
			InitiateMultipartUploadResult result = ossClient.initiateMultipartUpload(request);

//			返回uploadId，它是分片上传事件的唯一标识，您可以根据这个ID来发起相关的操作，如取消分片上传、查询分片上传等。
			String uploadId = result.getUploadId();
//			步骤2：上传分片
//			partETags是PartETag的集合。PartETag由分片的ETag和分片号组成。
			List<PartETag> partETags = new ArrayList<>();
			// 计算文件有多少个分片。
			BufferedInputStream instream = null;
			try {
				long fileLength = file.length();
				long filePartSize = (partSize == null || partSize <= 100) ? PART_SIZE : partSize;
				int partCount = (int) (fileLength / filePartSize);
				if (fileLength % filePartSize != 0) {
					partCount++;
				}
				// 遍历分片上传。
				for (int i = 0; i < partCount; i++) {
					long startPos = i * filePartSize;
					long curPartSize = (i + 1 == partCount) ? (fileLength - startPos) : filePartSize;
					instream = FileUtil.getInputStream(file);
					// 跳过已经上传的分片。
					if (instream.skip(startPos) <= -1) {
						break;
					}

					// 设置分片大小。除了最后一个分片没有大小限制，其他的分片最小为100KB。
					UploadPartRequest uploadPartRequest = new UploadPartRequest(ossConfig.getBucketName(), key,
							uploadId, i + 1, instream, curPartSize);
					// 每个分片不需要按顺序上传，甚至可以在不同客户端上传，OSS会按照分片号排序组成完整的文件。
					UploadPartResult uploadPartResult = ossClient.uploadPart(uploadPartRequest);
					// 每次上传分片之后，OSS的返回结果会包含一个PartETag。PartETag将被保存到partETags中。
					partETags.add(uploadPartResult.getPartETag());

					instream.close();
				}

//				步骤3：完成分片上传
//				排序。partETags必须按分片号升序排列。
				Collections.sort(partETags, (p1, p2) -> p1.getPartNumber() - p2.getPartNumber());
				// 在执行该操作时，需要提供所有有效的partETags。OSS收到提交的partETags后，会逐一验证每个分片的有效性。当所有的数据分片验证通过后，OSS将把这些分片组合成一个完整的文件。
				CompleteMultipartUploadRequest completeMultipartUploadRequest = new CompleteMultipartUploadRequest(
						ossConfig.getBucketName(), key, uploadId, partETags);
				ossClient.completeMultipartUpload(completeMultipartUploadRequest);

				return new Result(ResultCode.SUCCESS, RESULT_SUCC);
			} catch (IOException e) {
				logger.error(e.getMessage(), e);

				return new Result(ResultCode.SUCCESS, RESULT_SUCC);
			} finally {
				if (null != instream) {
					try {
						instream.close();
					} catch (IOException e) {
						logger.error(e.getMessage(), e);
					}
				}
			}
		});
	}

	@Override
	public InputStream getObject(String key) {

		return ossUtil(ossClient -> {
			OSSObject ossObject = ossClient.getObject(ossConfig.getBucketName(), key);
			if (ossObject != null) {
				return ossObject.getObjectContent();
			}
			return null;
		});
	}

	@Override
	public long getObject(String key, File file) {
		return ossUtil(ossClient -> {
			return ossClient.getObject(new GetObjectRequest(ossConfig.getBucketName(), key), file).getContentLength();
		});
	}

	@Override
	public ObjectMetadata getObjectMetadata(String key) {

		return ossUtil(ossClient -> {
			OSSObject ossObject = ossClient.getObject(ossConfig.getBucketName(), key);
			if (ossObject == null)
				return ObjectMetadata.EMPTY;
			com.aliyun.oss.model.ObjectMetadata ossObjectMetadata = ossObject.getObjectMetadata();
			return ObjectMetadata.builder().md5(ossObjectMetadata.getContentMD5()).key(key)
					.size(ossObjectMetadata.getContentLength()).build();
		});
	}

	@Override
	public boolean deleteObject(String key) {
		return ossUtil(ossClient -> {
			ossClient.deleteObject(ossConfig.getBucketName(), key);
			return true;
		});
	}

	@Override
	public String download(String key, boolean flag) {
		if (flag) {
			return ossUtil(ossClient -> {
				Date expiration = new Date(new Date().getTime() + 1000 * ossConfig.getExpiration());
				GeneratePresignedUrlRequest req = new GeneratePresignedUrlRequest(ossConfig.getBucketName(), key,
						HttpMethod.GET);
				req.setExpiration(expiration);
				URL signedUrl = ossClient.generatePresignedUrl(req);

				return signedUrl.getFile();
			});
		} else {
			return "http://" + ossConfig.getBucketName() + "."
					+ ossConfig.getEndpoint().substring(ossConfig.getEndpoint().indexOf("oss")) + SLASH + key;
		}
	}

	@Override
	public boolean copyObject(String srcKey, String destKey) {
		return ossUtil(ossClient -> {
			ossClient.copyObject(ossConfig.getBucketName(), srcKey, ossConfig.getBucketName(), destKey);
			return true;
		});
	}

	interface OssCallBack<T> {
		T execute(OSSClient ossClient);
	}

	public <T> T ossUtil(OssCallBack<T> callBack) {
		OSSClient ossClient = new OSSClient(ossConfig.getEndpoint(), credsProvider, configuration);
		T re = callBack.execute(ossClient);
		ossClient.shutdown();
		return re;
	}
}