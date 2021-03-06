/*
 * Copyright 2012-2014, Continuuity, Inc.
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
package com.continuuity.loom.http.handler;

import com.continuuity.http.AbstractHttpHandler;
import com.continuuity.http.HttpResponder;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

/**
 * Serves status URL.
 */
public class StatusHandler extends AbstractHttpHandler{

  /**
   * Returns the status of the server, which just returns OK if it is handling requests. Used as a healthcheck.
   *
   * @param request The request for loom status.
   * @param responder
   */
  @Path("/status")
  @GET
  public void status(@SuppressWarnings("UnusedParameters") HttpRequest request, HttpResponder responder) {
    responder.sendString(HttpResponseStatus.OK, "OK\n");
  }
}
