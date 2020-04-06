package com.github.msa.endpoint.response;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 公共返回对象
 * 
 */

@Getter
@Setter
@ToString
@SuppressWarnings("serial")
public class Result implements Serializable {

	private static final Result NOT_FOUND = new Result(ResultCode.ENTITY_NOT_FOUND);
	private static final Result OK = new Result(ResultCode.SUCCESS);

	private int code;

	private String msg;

	public Result() {

	}

	public Result(int code, String msg) {
		setMsg(msg);
		setCode(code);
	}

	public Result(ResultCode responseCode) {
		setMsg(responseCode.getMsg());
		setCode(responseCode.getCode());
	}
	
	public Result(ResultCode responseCode, String msg) {
		setMsg(msg);
		setCode(responseCode.getCode());
	}

	public static Result notFound() {
		return NOT_FOUND;
	}

	public static Result ok() {
		return OK;
	}
}
