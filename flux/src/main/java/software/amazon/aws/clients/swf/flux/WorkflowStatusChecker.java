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

import software.amazon.awssdk.services.swf.SwfClient;

/**
 * Allows checking the status of a workflow execution. Obtained from a FluxCapacitor or RemoteWorkflowExecutor.
 */
public interface WorkflowStatusChecker {

    /**
     * Represents the status of a workflow. This is a composite of the workflow's ExecutionStatus and CloseStatus.
     */
    enum WorkflowStatus { IN_PROGRESS, COMPLETED, FAILED, CANCELED, TERMINATED, TIMED_OUT, UNKNOWN }

    /**
     * Queries SWF to determine the status of the associated workflow execution.
     */
    WorkflowStatus checkStatus();

    /**
     * Useful primarily in integration tests, this allows the raw SWF client to be retrieved from the status checker.
     */
    SwfClient getSwfClient();
}
