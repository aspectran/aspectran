/**
 * Copyright 2008-2016 Juho Jeong
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.aspectran.core.context.builder.importer;

import java.util.ArrayList;
import java.util.List;

import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;

/**
 * The Class AbstractImportHandler.
 */
abstract class AbstractImportHandler implements ImportHandler {
	
	protected final Log log = LogFactory.getLog(getClass());
	
	private List<Importer> pendingList;
	
	AbstractImportHandler() {
	}

	@Override
	public void pending(Importer importer) {
		if(pendingList == null)
			pendingList = new ArrayList<Importer>();
		
		pendingList.add(importer);
		
		if(log.isDebugEnabled())
			log.debug("Import pending " + importer);
	}
	
	protected void handle() throws Exception {
		if(pendingList != null) {
			List<Importer> pendedList = pendingList;
			pendingList = null;
			
			for(Importer importer : pendedList) {
				if(log.isDebugEnabled())
					log.debug("Import " + importer);
				
				handle(importer);
			}
		}
	}

	@Override
	public List<Importer> getPendingList() {
		return pendingList;
	}

}
