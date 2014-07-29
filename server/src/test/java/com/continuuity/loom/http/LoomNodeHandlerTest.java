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
package com.continuuity.loom.http;

import com.continuuity.loom.admin.Service;
import com.continuuity.loom.cluster.Node;
import com.google.common.base.Charsets;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.junit.After;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

/**
 *
 */
public class LoomNodeHandlerTest extends LoomServiceTestBase {
  @BeforeClass
  public static void init() {
  }

  protected static List<JsonObject> getJsonListFromResponse(HttpResponse response) throws IOException {
    Reader reader = getInputStreamReaderFromResponse(response);
    return gson.fromJson(reader, new TypeToken<List<JsonObject>>() {}.getType());
  }

  protected static Reader getInputStreamReaderFromResponse(HttpResponse response) throws IOException {
    return new InputStreamReader(response.getEntity().getContent(), Charsets.UTF_8);
  }

  @After
  public void testCleanup() {
    // cleanup
    // TODO: Remove all nodes created
  }

  @Test
  public void testAddNodeAsUser() throws Exception {
    postNodes(1, USER1_HEADERS);
    List<JsonObject> nodes = getNodes(USER1_HEADERS);
    Assert.assertEquals(1, nodes.size());
  }

  @Test
  public void testGetAllNodesAsUser() throws Exception {
    Node postedNode = postNodes(1, USER1_HEADERS).get(0);
    List<JsonObject> nodes = getNodes(USER1_HEADERS);
    Assert.assertEquals(1, nodes.size());
    String nodeId = nodes.get(0).get("id").getAsString();
    String clusterId = nodes.get(0).get("clusterId").getAsString();
    Assert.assertEquals(nodeId, postedNode.getId());
    Assert.assertEquals(clusterId, postedNode.getClusterId());
  }

  @Test
  public void testDeleteNodeAsUser() throws Exception {
    Node node = postNodes(1, USER1_HEADERS).get(0);
    HttpResponse response = doDelete("/v1/loom/nodes/" + node.getId(), USER1_HEADERS);
    assertResponseStatus(response, HttpResponseStatus.NO_CONTENT);
    List<JsonObject> nodes = getNodes(USER1_HEADERS);
    Assert.assertEquals(0, nodes.size());
  }

  @Test
  public void testUpdateNodeAsUser() throws Exception {
    Node node = postNodes(2, USER1_HEADERS).get(0);
    String nodeJsonString = getNodeAsJsonString(node);
    HttpResponse response = doPut("/v1/loom/nodes", nodeJsonString, USER1_HEADERS);
    assertResponseStatus(response, HttpResponseStatus.NO_CONTENT);
    // test to see that it has been updated
  }

  private List<JsonObject> getNodes(final Header[] headers) throws Exception {
    HttpResponse response = doGet("/v1/loom/nodes", headers);
    assertResponseStatus(response, HttpResponseStatus.OK);
    return getJsonListFromResponse(response);
  }

  private List<Node> postNodes(int numberOfNodes, Header[] headers) throws Exception {
    List<Node> nodes = createNodes(numberOfNodes);
    String nodesToPost = getNodeListAsJsonString(nodes);
    HttpResponse response = doPost("/v1/loom/nodes", nodesToPost, headers);
    assertResponseStatus(response, HttpResponseStatus.CREATED);
    return nodes;
  }

  private String getNodeListAsJsonString(final List<Node> nodes) {
    return gson.toJson(nodes);
  }

  private String getNodeAsJsonString(final Node node) {
    return gson.toJson(node);
  }

  private List<Node> createNodes(int numberOfNodesToCreate) {
    List<Node> createdNodes = new ArrayList<Node>();
    for (int i = 0; i < numberOfNodesToCreate; i++) {
      createdNodes.add(createNode(Integer.toString(i)));
    }

    return createdNodes;
  }

  private Node createNode(String id) {
    return new Node(id, "1234", new HashSet<Service>() {}, new HashMap<String, String>());
  }
}
