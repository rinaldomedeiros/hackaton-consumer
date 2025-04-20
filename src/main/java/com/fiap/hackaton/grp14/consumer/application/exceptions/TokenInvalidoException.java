package com.fiap.hackaton.grp14.consumer.application.exceptions;

public class TokenInvalidoException extends RuntimeException {
	private static final long serialVersionUID = 8461886887730658773L;

	public TokenInvalidoException(String message) {
        super(message);
    }
}