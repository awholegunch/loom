package com.continuuity.loom.provisioner.guice;

import com.continuuity.loom.provisioner.HttpProvisionerRequestService;
import com.continuuity.loom.provisioner.ProvisionerRequestService;
import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

/**
 * Guice module for injection of provisioner related classes.
 */
public class ProvisionerModule extends AbstractModule {

  @Override
  protected void configure() {
    bind(ProvisionerRequestService.class).to(HttpProvisionerRequestService.class).in(Scopes.SINGLETON);
  }
}
