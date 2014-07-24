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
package com.continuuity.loom.store.provisioner;

import com.continuuity.loom.provisioner.plugin.PluginResourceMeta;
import com.continuuity.loom.provisioner.plugin.PluginResourceStatus;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

/**
 * A view of the plugin resource metadata store. Each account and resource type pair will see a different view of
 * the store. Resource metadata essentially serves as a record of what resources have been uploaded, what resources
 * are staged, and what resources are in what state.
 */
public interface PluginResourceMetaStoreView {

  /**
   * Checks whether or not any version of the given resource exists.
   *
   * @param name Name of the resource to check for
   * @return True if the module version exists, false if not
   * @throws IOException
   */
  public boolean exists(String name) throws IOException;

  /**
   * Checks whether or not a specific version of the given resource exists.
   *
   * @param name Name of the resource to check for
   * @param version Version of the resource to check for
   * @return True if the module version exists, false if not
   * @throws IOException
   */
  public boolean exists(String name, int version) throws IOException;

  /**
   * Add the resource metadata to the store. The version given must be unique for the given resource name.
   *
   * @param meta Metadata to add
   * @throws IOException
   */
  public void add(PluginResourceMeta meta) throws IOException;

  /**
   * Get the highest version of given resource, or 0 if the resource does not exist.
   *
   * @param name Name of the resource to get the highest version of
   * @return Highest version of given resource, or 0 if the resource does not exist.
   * @throws IOException
   */
  public int getHighestVersion(String name) throws IOException;

  /**
   * Get the metadata for the given resource name and version.
   *
   * @param name Name of the resource to get
   * @param version Version of the resource to get
   * @return Metadata of the given resource
   * @throws IOException
   */
  public PluginResourceMeta get(String name, int version) throws IOException;

  /**
   * Delete the given version of the given resource from the store.
   *
   * @param name Name of the resource to delete
   * @param version Version of the resource to delete
   * @throws IOException
   */
  public void delete(String name, int version) throws IOException;

  /**
   * Get an immutable map of resource name to all metadata with that name.
   *
   * @return Immutable map of resource name to resource metadata
   * @throws IOException
   */
  public Map<String, Set<PluginResourceMeta>> getAll() throws IOException;

  /**
   * Get an immutable map of resource name to all metadata with that name with the given status.
   *
   * @param status Status of the resources to get
   * @return Immutable map of resource name to resource metadata
   * @throws IOException
   */
  public Map<String, Set<PluginResourceMeta>> getAll(PluginResourceStatus status) throws IOException;

  /**
   * Get an immutable set of all resource metadata for the given resource name.
   *
   * @param name Name of resource to get metadata for
   * @return Immutable set of all resource metadata for the given resource name
   * @throws IOException
   */
  public Set<PluginResourceMeta> getAll(String name) throws IOException;

  /**
   * Get an immutable set of all resource metadata for the given resource name with the given status.
   *
   * @param name Name of resources to get
   * @param status Status of the resources to get
   * @return Immutable set of all resource metadata for the given resource name with the given status
   * @throws IOException
   */
  public Set<PluginResourceMeta> getAll(String name, PluginResourceStatus status) throws IOException;

  /**
   * Atomically stage an inactive resource version and deactivate the current staged version if there is one.
   * Staging an unstage resource puts it back in an active state. This is a no-op if the given resource is already
   * staged or active.
   *
   * @param name Name of resource to stage
   * @param version Version of resource to stage
   * @throws IOException
   */
  public void stage(String name, int version) throws IOException;

  /**
   * Unstage the given resource, moving a resource in the staged state into the inactive state, or moving a resource
   * in the active state into the unstaged state. This a no-op if the given resource is already unstaged or inactive.
   *
   * @param name Name of resource to unstage
   * @param version Version of resource to unstage
   * @throws IOException
   */
  public void unstage(String name, int version) throws IOException;

  /**
   * Atomically promote the staged version of the resource into the active state and the unstaged version of
   * the resource into the inactive state.
   *
   * @param name Name of the resource to sync
   * @throws IOException
   */
  public void syncStatus(String name) throws IOException;
}
