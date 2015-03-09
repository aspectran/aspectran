package com.aspectran.core.context.builder;

import java.util.List;

public interface ImportHandler {

	public void pending(Importable importable);
	
	public void handle(Importable importable) throws Exception;
	
	public List<Importable> getPendingList();
	
}
