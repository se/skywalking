/*
 * Copyright 2017, OpenSkywalking Organization All rights reserved.
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
 *
 * Project repository: https://github.com/OpenSkywalking/skywalking
 */

package org.skywalking.apm.collector.stream.worker.base;

import org.skywalking.apm.collector.core.CollectorException;
import org.skywalking.apm.collector.core.graph.NodeProcessor;
import org.skywalking.apm.collector.queue.base.QueueEventHandler;
import org.skywalking.apm.collector.queue.base.QueueExecutor;

/**
 * 异步 Worker 引用
 *
 * @author peng-yongsheng
 */
public class LocalAsyncWorkerRef<INPUT, OUTPUT> extends WorkerRef<INPUT, OUTPUT> implements QueueExecutor<INPUT> {

    /**
     * 队列事件处理器
     */
    private QueueEventHandler<INPUT> queueEventHandler;

    LocalAsyncWorkerRef(NodeProcessor<INPUT, OUTPUT> destinationHandler) {
        super(destinationHandler);
    }

    public void setQueueEventHandler(QueueEventHandler<INPUT> queueEventHandler) {
        this.queueEventHandler = queueEventHandler;
    }

    @Override public void execute(INPUT input) throws CollectorException {
        out(input);
    }

    @Override protected void in(INPUT input) {
        queueEventHandler.tell(input);
    }

    @Override protected void out(INPUT input) {
        super.out(input);
    }
}
