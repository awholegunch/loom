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

import com.continuuity.loom.account.Account;
import com.continuuity.loom.provisioner.plugin.PluginResourceType;
import com.google.common.util.concurrent.Service;

/**
 * Service that provides views for reading and writing to and from the plugin resource metadata store.
 */
public interface PluginResourceMetaStoreService extends Service {

  /**
   * Get a view of the metadata store for the given account and resource type.
   *
   * @param account Account that will be accessing the store
   * @param type Type of plugin resource that will be accessed
   * @return View of the metadata store for the given account and resource type
   */
  PluginResourceMetaStoreView getView(Account account, PluginResourceType type);
}
