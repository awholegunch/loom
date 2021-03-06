package com.continuuity.loom.store.guice;

import com.continuuity.loom.store.credential.CredentialStore;
import com.continuuity.loom.store.credential.InProcessCredentialStore;
import com.continuuity.loom.store.provisioner.MemoryPluginStore;
import com.continuuity.loom.store.provisioner.PluginStore;
import com.google.inject.Scopes;

/**
 *
 */
public class TestStoreModule extends AbstractStoreModule {

  @Override
  void bindPluginStore() {
    bind(PluginStore.class).to(MemoryPluginStore.class).in(Scopes.SINGLETON);
    bind(CredentialStore.class).to(InProcessCredentialStore.class).in(Scopes.SINGLETON);
    bind(InProcessCredentialStore.class).in(Scopes.SINGLETON);
    bind(MemoryPluginStore.class).in(Scopes.SINGLETON);
  }
}
