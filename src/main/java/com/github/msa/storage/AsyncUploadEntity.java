package com.github.msa.storage;

import java.io.File;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class AsyncUploadEntity {

	private String key;
	
	private File file;

	
}
