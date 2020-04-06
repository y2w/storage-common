package com.github.msa.storage;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
@ToString
@AllArgsConstructor
public class ObjectMetadata {

	public static final ObjectMetadata EMPTY = new ObjectMetadata("",0l);
	
	public ObjectMetadata(String md5,Long size) {
		this.size=size;
		this.md5=md5;
	}
	
	public ObjectMetadata() {
		this(null, 0l);
	}
	
	private String key;
	
	private String md5;
	
	private Long size;
	
	private String name;
	
}
