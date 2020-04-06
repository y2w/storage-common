package com.github.msa.endpoint.response;

import lombok.Getter;
import lombok.Setter;

/**
 * 公共返回对象
 * 
 */
@Getter
@Setter
@SuppressWarnings({ "serial" })
public class RestResult<T> extends Result {

	private T data;

	/**
	 * 空值，需要单独设置
	 */
	public RestResult() {

	}

	/**
	 * 构造相应类
	 * 
	 * @param status
	 *            状态码
	 * @param msg
	 *            附加的消息
	 */
	public RestResult(int status, String msg) {
		super(status, msg);
	}

	public RestResult(ResultCode responseCode) {
		setMsg(responseCode.getMsg());
		setCode(responseCode.getCode());
	}
	
	public RestResult(ResultCode responseCode,String msg) {
		setMsg(msg);
		setCode(responseCode.getCode());
	}

	/**
	 * 默认成功
	 * 
	 * @param message
	 *            成功结果消息
	 */
	public RestResult(String message) {
		this(ResultCode.SUCCESS.getCode(), message);
	}

	/**
	 * 根据指定数据构造成功响应消息
	 *
	 * @param data
	 *            结果数据
	 * @param <R>
	 *            结果数据对象类型
	 * @return 带指定数据的成功响应消息
	 */
	public static <R> RestResult<R> ok(R data) {
		RestResult<R> result = new RestResult<>(ResultCode.SUCCESS);
		result.setData(data);
		return result;
	}
}
