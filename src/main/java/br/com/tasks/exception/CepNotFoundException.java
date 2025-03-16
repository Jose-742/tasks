package br.com.tasks.exception;

public class CepNotFoundException extends RuntimeException {

    public CepNotFoundException() {
        super("CEP not found");
    }
}
