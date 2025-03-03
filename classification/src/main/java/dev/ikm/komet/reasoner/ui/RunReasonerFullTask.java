/*
 * Copyright © 2015 Integrated Knowledge Management (support@ikm.dev)
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
package dev.ikm.komet.reasoner.ui;

import java.util.concurrent.Future;
import java.util.function.Consumer;

import dev.ikm.tinkar.common.service.TinkExecutor;
import dev.ikm.tinkar.reasoner.service.ClassifierResults;
import dev.ikm.tinkar.reasoner.service.ReasonerService;

public class RunReasonerFullTask extends RunReasonerTaskBase {

	public RunReasonerFullTask(ReasonerService reasonerService, Consumer<ClassifierResults> classifierResultsConsumer) {
		super(reasonerService, classifierResultsConsumer);

	}

	protected void loadData(int workDone) throws Exception {
		updateMessage("Step " + workDone + ": Loading data into reasoner");
		LoadDataTask task = new LoadDataTask(reasonerService);
		Future<ReasonerService> future = TinkExecutor.threadPool().submit(task);
		future.get();
		updateProgress(workDone);
	}

}
