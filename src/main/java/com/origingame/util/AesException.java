package com.origingame.util;

import com.origingame.server.exception.GameException;

public class AesException extends GameException {

	private static final long serialVersionUID = 4126730595253316306L;

	public AesException() {
		super();
	}

	public AesException(String message, Throwable cause) {
		super(message, cause);
	}

	public AesException(String message) {
		super(message);
	}

	public AesException(Throwable cause) {
		super(cause);
	}

	
	
}
