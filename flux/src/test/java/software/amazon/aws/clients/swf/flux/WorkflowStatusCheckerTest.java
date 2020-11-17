/*
 *   Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License").
 *   You may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package software.amazon.aws.clients.swf.flux;

import org.easymock.EasyMock;
import org.easymock.IMocksControl;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import software.amazon.awssdk.services.swf.SwfClient;
import software.amazon.awssdk.services.swf.model.CloseStatus;
import software.amazon.awssdk.services.swf.model.DescribeWorkflowExecutionRequest;
import software.amazon.awssdk.services.swf.model.DescribeWorkflowExecutionResponse;
import software.amazon.awssdk.services.swf.model.ExecutionStatus;
import software.amazon.awssdk.services.swf.model.UnknownResourceException;
import software.amazon.awssdk.services.swf.model.WorkflowExecution;
import software.amazon.awssdk.services.swf.model.WorkflowExecutionInfo;

public class WorkflowStatusCheckerTest {

    private static final String DOMAIN = "test";
    private static final String WORKFLOW_ID = "testWorkflowId";
    private static final String RUN_ID = "testRunId";

    private IMocksControl mockery;
    private SwfClient swf;
    private WorkflowStatusChecker wsc;

    @Before
    public void setup() {
        mockery = EasyMock.createControl();
        swf = mockery.createMock(SwfClient.class);
        wsc = new WorkflowStatusCheckerImpl(swf, DOMAIN, WORKFLOW_ID, RUN_ID);
    }

    @Test
    public void testWorkflowUnknownWhenWorkflowExecutionNotFound() {
        EasyMock.expect(swf.describeWorkflowExecution(buildDescribeRequest())).andThrow(UnknownResourceException.builder().build());
        mockery.replay();
        Assert.assertEquals(WorkflowStatusChecker.WorkflowStatus.UNKNOWN, wsc.checkStatus());
        mockery.verify();
    }

    @Test
    public void testWorkflowInProgress() {
        expectDescribeStatus(ExecutionStatus.OPEN, null);
        mockery.replay();
        Assert.assertEquals(WorkflowStatusChecker.WorkflowStatus.IN_PROGRESS, wsc.checkStatus());
        mockery.verify();
    }

    @Test
    public void testWorkflowCompleted() {
        expectDescribeStatus(ExecutionStatus.CLOSED, CloseStatus.COMPLETED);
        mockery.replay();
        Assert.assertEquals(WorkflowStatusChecker.WorkflowStatus.COMPLETED, wsc.checkStatus());
        mockery.verify();
    }

    @Test
    public void testWorkflowCompleted_ContinuedAsNew() {
        expectDescribeStatus(ExecutionStatus.CLOSED, CloseStatus.CONTINUED_AS_NEW);
        mockery.replay();
        Assert.assertEquals(WorkflowStatusChecker.WorkflowStatus.COMPLETED, wsc.checkStatus());
        mockery.verify();
    }

    @Test
    public void testWorkflowCanceled() {
        expectDescribeStatus(ExecutionStatus.CLOSED, CloseStatus.CANCELED);
        mockery.replay();
        Assert.assertEquals(WorkflowStatusChecker.WorkflowStatus.CANCELED, wsc.checkStatus());
        mockery.verify();
    }

    @Test
    public void testWorkflowFailed() {
        expectDescribeStatus(ExecutionStatus.CLOSED, CloseStatus.FAILED);
        mockery.replay();
        Assert.assertEquals(WorkflowStatusChecker.WorkflowStatus.FAILED, wsc.checkStatus());
        mockery.verify();
    }

    @Test
    public void testWorkflowTimedOut() {
        expectDescribeStatus(ExecutionStatus.CLOSED, CloseStatus.TIMED_OUT);
        mockery.replay();
        Assert.assertEquals(WorkflowStatusChecker.WorkflowStatus.TIMED_OUT, wsc.checkStatus());
        mockery.verify();
    }

    @Test
    public void testWorkflowTerminated() {
        expectDescribeStatus(ExecutionStatus.CLOSED, CloseStatus.TERMINATED);
        mockery.replay();
        Assert.assertEquals(WorkflowStatusChecker.WorkflowStatus.TERMINATED, wsc.checkStatus());
        mockery.verify();
    }

    private DescribeWorkflowExecutionRequest buildDescribeRequest() {
        WorkflowExecution execution = WorkflowExecution.builder().workflowId(WORKFLOW_ID).runId(RUN_ID).build();
        return DescribeWorkflowExecutionRequest.builder().domain(DOMAIN).execution(execution).build();
    }

    private void expectDescribeStatus(ExecutionStatus executionStatus, CloseStatus closeStatus) {
        WorkflowExecutionInfo info = WorkflowExecutionInfo.builder()
                .executionStatus(executionStatus != null ? executionStatus.toString() : null)
                .closeStatus(closeStatus != null ? closeStatus.toString() : null)
                .build();

        DescribeWorkflowExecutionResponse executionDetail = DescribeWorkflowExecutionResponse.builder()
                .executionInfo(info).build();
        EasyMock.expect(swf.describeWorkflowExecution(buildDescribeRequest())).andReturn(executionDetail);
    }
}
