package com.fiap.hackaton.grp14.consumer.application.exceptions;

public class TokenExpiradoException extends RuntimeException {
	private static final long serialVersionUID = 3079977926190620301L;

	public TokenExpiradoException(String message) {
        super(message);
    }
}
