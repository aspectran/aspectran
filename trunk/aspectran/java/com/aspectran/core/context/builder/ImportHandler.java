package com.aspectran.core.context.builder;

public interface ImportHandler {

	public void pending(Importable importable);
	
	public void handle(Importable importable) throws Exception;
	
}
