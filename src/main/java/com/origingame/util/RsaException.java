package com.origingame.util;

import com.origingame.server.exception.GameException;

public class RsaException extends GameException {

	private static final long serialVersionUID = -6281633547530278269L;

	public RsaException() {
	}

	public RsaException(String message) {
		super(message);
	}

	public RsaException(Throwable cause) {
		super(cause);
	}

	public RsaException(String message, Throwable cause) {
		super(message, cause);
	}

}
