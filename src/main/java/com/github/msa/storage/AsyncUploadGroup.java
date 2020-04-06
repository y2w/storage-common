package com.github.msa.storage;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.msa.common.ExceptionTool;
import com.github.msa.endpoint.response.Result;

import cn.hutool.core.util.ArrayUtil;

public class AsyncUploadGroup implements Callable<String>{
	
	private static final Logger logger = LoggerFactory.getLogger(AsyncUploadGroup.class);
	
	private ConcurrentLinkedQueue<AsyncUploadEntity> linkedQueue = new ConcurrentLinkedQueue<>();
	
	protected static final String SUCCESS = "SUCCESS";
	protected static final String FAILED = "FAILED";
	
	private final ThreadPoolExecutor  executor;
	
	private Storage storage;
	
	public AsyncUploadGroup(List<AsyncUploadEntity> linkedQueue,
			ThreadPoolExecutor executor, Storage storage) {
		this.linkedQueue=new ConcurrentLinkedQueue<>(linkedQueue);
		this.executor = executor;
		this.storage = storage;
	}



	@Override
	public String call() throws Exception {
		List<Future<String>> result = new ArrayList<>();
		while(true) {
			for(int i=executor.getPoolSize();i<(ArrayUtil.min(ArrayUtil.max(5,linkedQueue.size()),25));i++) {
				result.add(executor.submit(()->{
					while(true) {
						AsyncUploadEntity asyncEntity = linkedQueue.poll();
						logger.info("执行上传 线程池数，{}，任务数{}",executor.getPoolSize(),executor.getQueue().size());
						if(asyncEntity!=null) {
							Result upload = storage.putObject(asyncEntity.getFile(), asyncEntity.getKey());
							String flag =  (upload.getCode()==200||upload.getCode()==201)?SUCCESS:FAILED;
							if(flag.equals(FAILED)) {
								logger.error("批量异步上传失败{}",upload.getMsg());
								return FAILED;
							}
						}else {
							logger.info("上传执行完毕,线程池数，{}，任务数{}",executor.getPoolSize(),executor.getQueue().size());
							return SUCCESS;
						}
					}
				}));
			}
			if(result.isEmpty()) {
				Thread.sleep(300);
				continue;
			}
			if(result.stream().filter(a->{
				try {
					return FAILED.equals(a.get());
				}  catch (InterruptedException|ExecutionException e) {
					logger.info("获取线程上传结果返回失败{}",ExceptionTool.getStackTraceAsString(e));
					Thread.currentThread().interrupt();
					return  false;
				} 
			}).collect(Collectors.toList()).isEmpty()) {
				return FAILED;
			}
			return SUCCESS;
		}
	
	}



	
}
