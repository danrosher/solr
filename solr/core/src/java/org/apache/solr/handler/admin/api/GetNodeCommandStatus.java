/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.solr.handler.admin.api;

import static org.apache.solr.handler.admin.CoreAdminHandler.CoreAdminAsyncTracker.COMPLETED;
import static org.apache.solr.handler.admin.CoreAdminHandler.CoreAdminAsyncTracker.FAILED;

import jakarta.inject.Inject;
import org.apache.solr.client.api.endpoint.GetNodeCommandStatusApi;
import org.apache.solr.client.api.model.GetNodeCommandStatusResponse;
import org.apache.solr.common.params.CoreAdminParams;
import org.apache.solr.core.CoreContainer;
import org.apache.solr.handler.admin.CoreAdminHandler;
import org.apache.solr.handler.admin.CoreAdminHandler.CoreAdminAsyncTracker.TaskObject;
import org.apache.solr.jersey.PermissionName;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.response.SolrQueryResponse;
import org.apache.solr.security.PermissionNameProvider;

/**
 * Implementation of V2 API interface {@link GetNodeCommandStatusApi} for checking the status of a
 * core-level asynchronous command.
 */
public class GetNodeCommandStatus extends CoreAdminAPIBase implements GetNodeCommandStatusApi {

  @Inject
  public GetNodeCommandStatus(
      CoreContainer coreContainer,
      CoreAdminHandler.CoreAdminAsyncTracker coreAdminAsyncTracker,
      SolrQueryRequest req,
      SolrQueryResponse rsp) {
    super(coreContainer, coreAdminAsyncTracker, req, rsp);
  }

  @Override
  @PermissionName(PermissionNameProvider.Name.CORE_READ_PERM)
  public GetNodeCommandStatusResponse getCommandStatus(String requestId) {
    ensureRequiredParameterProvided(CoreAdminParams.REQUESTID, requestId);
    var requestStatusResponse = new GetNodeCommandStatusResponse();

    TaskObject taskObject = coreAdminAsyncTracker.getAsyncRequestForStatus(requestId);
    String status = taskObject != null ? taskObject.getStatus() : null;

    if (status == null) {
      requestStatusResponse.status = "notfound";
      requestStatusResponse.msg = "No task found in running, completed or failed tasks";
    } else {
      requestStatusResponse.status = status;

      if (status.equals(COMPLETED)) {
        requestStatusResponse.response = taskObject.getRspObject();
        requestStatusResponse.response = taskObject.getOperationRspObject();
      } else if (status.equals(FAILED)) {
        requestStatusResponse.response = taskObject.getRspObject();
      }
    }

    return requestStatusResponse;
  }
}
