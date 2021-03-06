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

package org.skywalking.apm.plugin.rocketMQ.v4;

import java.lang.reflect.Method;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.skywalking.apm.agent.core.context.ContextManager;
import org.skywalking.apm.agent.core.context.tag.Tags;
import org.skywalking.apm.agent.core.context.trace.AbstractSpan;
import org.skywalking.apm.agent.core.plugin.interceptor.enhance.EnhancedInstance;
import org.skywalking.apm.agent.core.plugin.interceptor.enhance.InstanceMethodsAroundInterceptor;
import org.skywalking.apm.agent.core.plugin.interceptor.enhance.MethodInterceptResult;
import org.skywalking.apm.network.trace.component.ComponentsDefine;
import org.skywalking.apm.plugin.rocketMQ.v4.define.SendCallBackEnhanceInfo;

/**
 * {@link OnSuccessInterceptor} create local span when the method {@link org.apache.rocketmq.client.producer.SendCallback#onSuccess(SendResult)}
 * execute.
 *
 * @author zhang xin
 */
public class OnSuccessInterceptor implements InstanceMethodsAroundInterceptor {

    public static final String CALLBACK_OPERATION_NAME_PREFIX = "RocketMQ/";

    @Override
    public void beforeMethod(EnhancedInstance objInst, Method method, Object[] allArguments, Class<?>[] argumentsTypes,
        MethodInterceptResult result) throws Throwable {
        SendCallBackEnhanceInfo enhanceInfo = (SendCallBackEnhanceInfo)objInst.getSkyWalkingDynamicField();
        AbstractSpan activeSpan = ContextManager.createLocalSpan(CALLBACK_OPERATION_NAME_PREFIX + enhanceInfo.getTopicId() + "/Producer/Callback");
        activeSpan.setComponent(ComponentsDefine.ROCKET_MQ);
        SendStatus sendStatus = ((SendResult)allArguments[0]).getSendStatus();
        if (sendStatus != SendStatus.SEND_OK) {
            activeSpan.errorOccurred();
            Tags.STATUS_CODE.set(activeSpan, sendStatus.name());
        }
        ContextManager.continued(enhanceInfo.getContextSnapshot());
    }

    @Override
    public Object afterMethod(EnhancedInstance objInst, Method method, Object[] allArguments, Class<?>[] argumentsTypes,
        Object ret) throws Throwable {
        ContextManager.stopSpan();
        return ret;
    }

    @Override public void handleMethodException(EnhancedInstance objInst, Method method, Object[] allArguments,
        Class<?>[] argumentsTypes, Throwable t) {
        ContextManager.activeSpan().errorOccurred().log(t);
    }
}
