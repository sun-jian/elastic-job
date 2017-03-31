/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
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
 * </p>
 */

package com.dangdang.ddframe.job.lite.internal.server;

import com.dangdang.ddframe.job.lite.api.strategy.JobInstance;
import com.dangdang.ddframe.job.lite.internal.execution.ExecutionService;
import com.dangdang.ddframe.job.lite.internal.instance.InstanceService;
import com.dangdang.ddframe.job.lite.internal.schedule.JobRegistry;
import com.dangdang.ddframe.job.lite.internal.schedule.JobScheduleController;
import com.dangdang.ddframe.job.lite.internal.sharding.ShardingService;
import com.dangdang.ddframe.job.lite.internal.storage.JobNodeStorage;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent;
import org.apache.curator.framework.recipes.cache.TreeCacheListener;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.unitils.util.ReflectionUtils;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public final class JobOperationListenerManagerTest {
    
    @Mock
    private JobNodeStorage jobNodeStorage;
    
    @Mock
    private ServerService serverService;
    
    @Mock
    private InstanceService instanceService;
    
    @Mock
    private ShardingService shardingService;
    
    @Mock
    private ExecutionService executionService;
    
    @Mock
    private JobScheduleController jobScheduleController;
    
    private JobOperationListenerManager jobOperationListenerManager;
    
    @Before
    public void setUp() throws NoSuchFieldException {
        JobRegistry.getInstance().addJobInstance("test_job", new JobInstance("127.0.0.1@-@0"));
        jobOperationListenerManager = new JobOperationListenerManager(null, "test_job");
        MockitoAnnotations.initMocks(this);
        ReflectionUtils.setFieldValue(jobOperationListenerManager, "serverService", serverService);
        ReflectionUtils.setFieldValue(jobOperationListenerManager, "instanceService", instanceService);
        ReflectionUtils.setFieldValue(jobOperationListenerManager, "shardingService", shardingService);
        ReflectionUtils.setFieldValue(jobOperationListenerManager, "executionService", executionService);
        ReflectionUtils.setFieldValue(jobOperationListenerManager, jobOperationListenerManager.getClass().getSuperclass().getDeclaredField("jobNodeStorage"), jobNodeStorage);
    }
    
    @Test
    public void assertStart() {
        jobOperationListenerManager.start();
        verify(jobNodeStorage).addDataListener(Matchers.<TreeCacheListener>any());
    }
    
    @Test
    @Ignore
    public void assertJobTriggerStatusJobListenerWhenRemove() {
        jobOperationListenerManager.new JobTriggerStatusJobListener().dataChanged(null, new TreeCacheEvent(
                TreeCacheEvent.Type.NODE_REMOVED, new ChildData("/test_job/servers/" + "127.0.0.1" + "/127.0.0.1@-@0/trigger", null, "".getBytes())),
                "/test_job/servers/" + "127.0.0.1" + "/127.0.0.1@-@0/trigger");
        verify(jobScheduleController, times(0)).triggerJob();
    }
    
    @Test
    @Ignore
    public void assertJobTriggerStatusJobListenerWhenIsAddButNotLocalHostJobTriggerPath() {
        jobOperationListenerManager.new JobTriggerStatusJobListener().dataChanged(null, new TreeCacheEvent(
                TreeCacheEvent.Type.NODE_ADDED, new ChildData("/test_job/servers/" + "127.0.0.1" + "/127.0.0.1@-@0/other", null, "".getBytes())),
                "/test_job/servers/" + "127.0.0.1" + "/127.0.0.1@-@0/other");
        verify(jobScheduleController, times(0)).triggerJob();
    }
    
    @Test
    @Ignore
    public void assertJobTriggerStatusJobListenerWhenIsAddAndIsJobLocalHostTriggerPathButNoJobRegister() {
        jobOperationListenerManager.new JobTriggerStatusJobListener().dataChanged(null, new TreeCacheEvent(
                TreeCacheEvent.Type.NODE_ADDED, new ChildData("/test_job/servers/" + "127.0.0.1" + "/instances/127.0.0.1@-@0/trigger", null, "".getBytes())),
                "/test_job/servers/" + "127.0.0.1" + "/instances/127.0.0.1@-@0/trigger");
        verify(jobScheduleController, times(0)).triggerJob();
    }
    
    @Test
    @Ignore
    public void assertJobTriggerStatusJobListenerWhenIsAddAndIsJobLocalHostTriggerPathAndJobRegisterButServerIsNotReady() {
        JobRegistry.getInstance().addJobScheduleController("test_job", jobScheduleController);
        jobOperationListenerManager.new JobTriggerStatusJobListener().dataChanged(null, new TreeCacheEvent(
                TreeCacheEvent.Type.NODE_ADDED, new ChildData("/test_job/servers/" + "127.0.0.1" + "/instances/127.0.0.1@-@0/trigger", null, "".getBytes())),
                "/test_job/servers/" + "127.0.0.1" + "/instances/127.0.0.1@-@0/trigger");
        verify(jobScheduleController, times(0)).triggerJob();
    }
    
    @Test
    @Ignore
    public void assertJobTriggerStatusJobListenerWhenIsAddAndIsJobLocalHostTriggerPathAndJobRegisterAndServerIsReady() {
        JobRegistry.getInstance().addJobScheduleController("test_job", jobScheduleController);
        jobOperationListenerManager.new JobTriggerStatusJobListener().dataChanged(null, new TreeCacheEvent(
                TreeCacheEvent.Type.NODE_ADDED, new ChildData("/test_job/servers/" + "127.0.0.1" + "/instances/127.0.0.1@-@0/trigger", null, "".getBytes())),
                "/test_job/servers/" + "127.0.0.1" + "/instances/127.0.0.1@-@0/trigger");
        verify(jobScheduleController).triggerJob();
    }
}
