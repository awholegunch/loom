package com.continuuity.loom.scheduler.task;

import com.continuuity.loom.BaseTest;
import com.continuuity.loom.Entities;
import com.continuuity.loom.account.Account;
import com.continuuity.loom.cluster.Cluster;
import com.continuuity.loom.common.conf.Constants;
import com.continuuity.loom.http.request.AddServicesRequest;
import com.continuuity.loom.http.request.ClusterConfigureRequest;
import com.continuuity.loom.http.request.ClusterCreateRequest;
import com.continuuity.loom.http.request.ClusterOperationRequest;
import com.continuuity.loom.provisioner.Provisioner;
import com.continuuity.loom.provisioner.TenantProvisionerService;
import com.continuuity.loom.scheduler.ClusterAction;
import com.continuuity.loom.spec.HardwareType;
import com.continuuity.loom.spec.ImageType;
import com.continuuity.loom.spec.Provider;
import com.continuuity.loom.spec.Tenant;
import com.continuuity.loom.spec.TenantSpecification;
import com.continuuity.loom.spec.plugin.FieldSchema;
import com.continuuity.loom.spec.plugin.ParameterType;
import com.continuuity.loom.spec.plugin.ParametersSpecification;
import com.continuuity.loom.spec.plugin.ProviderType;
import com.continuuity.loom.spec.service.Service;
import com.continuuity.loom.spec.template.Administration;
import com.continuuity.loom.spec.template.ClusterDefaults;
import com.continuuity.loom.spec.template.ClusterTemplate;
import com.continuuity.loom.spec.template.Compatibilities;
import com.continuuity.loom.spec.template.Constraints;
import com.continuuity.loom.store.entity.EntityStoreView;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.gson.JsonObject;
import org.junit.After;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

/**
 *
 */
public class ClusterServiceTest extends BaseTest {
  private static ClusterService clusterService;
  private static Account account;
  private static Provider provider;
  private static ProviderType providerType;
  private static ClusterTemplate basicTemplate;
  private static Service service1 = Entities.ServiceExample.HOSTS;
  private static Service service2 = Entities.ServiceExample.NAMENODE;
  private static ImageType imageType = Entities.ImageTypeExample.UBUNTU_12;
  private static HardwareType hardwareType = Entities.HardwareTypeExample.LARGE;

  @Override
  protected boolean shouldClearDataBetweenTests() {
    return false;
  }

  @BeforeClass
  public static void setupClusterServiceTests() throws Exception {
    clusterService = injector.getInstance(ClusterService.class);
    TenantProvisionerService tenantProvisionerService = injector.getInstance(TenantProvisionerService.class);
    // setup data
    tenantProvisionerService.writeProvisioner(new Provisioner("p1", "host", 50056, 100, null, null));
    tenantProvisionerService.writeTenantSpecification(new TenantSpecification("tenantX", 10, 1, 10));
    Tenant tenant = tenantStore.getTenantByName("tenantX");
    account = new Account("user9", tenant.getId());
    // write relevant entities
    EntityStoreView entityStoreView = entityStoreService.getView(new Account(Constants.ADMIN_USER, tenant.getId()));
    providerType = new ProviderType(
      "providertype",
      "some description",
      ImmutableMap.of(
        ParameterType.ADMIN, new ParametersSpecification(
          ImmutableMap.of(
            "region", FieldSchema.builder().setLabel("region").setType("text").setOverride(true).build(),
            "url", FieldSchema.builder().setLabel("url").setType("text").setOverride(true).setSensitive(true).build()),
          ImmutableSet.<Set<String>>of()
        ),
        ParameterType.USER, new ParametersSpecification(
          ImmutableMap.of(
            "keyname", FieldSchema.builder().setLabel("keyname").setType("text").setSensitive(false).build(),
            "key", FieldSchema.builder().setLabel("key").setType("text").setSensitive(true).build()),
          ImmutableSet.<Set<String>>of(ImmutableSet.of("keyname", "key"))
        )
      ),
      null
    );
    entityStoreService.getView(Account.SUPERADMIN).writeProviderType(providerType);
    provider = new Provider("provider", "description", providerType.getName(),
                            ImmutableMap.of("region", "iad", "url", "http://abc.com/api"));
    entityStoreView.writeProvider(provider);
    entityStoreView.writeHardwareType(hardwareType);
    entityStoreView.writeImageType(imageType);
    entityStoreView.writeService(service1);
    entityStoreView.writeService(service2);
    basicTemplate = new ClusterTemplate(
      "basic",
      "description",
      new ClusterDefaults(
        ImmutableSet.of(service1.getName()),
        provider.getName(),
        hardwareType.getName(),
        imageType.getName(),
        null,
        null
      ),
      new Compatibilities(
        ImmutableSet.of(hardwareType.getName()),
        ImmutableSet.of(imageType.getName()),
        ImmutableSet.of(service1.getName(), service2.getName())
      ),
      Constraints.EMPTY_CONSTRAINTS,
      Administration.EMPTY_ADMINISTRATION
    );
    entityStoreView.writeClusterTemplate(basicTemplate);
  }

  @After
  public void cleanupClusterServiceTest() throws Exception {
    credentialStore.wipe();
    clusterStoreService.clearData();
  }

  @Test
  public void testValidClusterCreate() throws Exception {
    String name = "clusty";
    Map<String, String> providerFields = Maps.newHashMap();
    providerFields.put("keyname", "myname");
    providerFields.put("key", "keycontents");
    providerFields.put("region", "dfw");
    providerFields.put("url", "internal.net/api");
    ClusterCreateRequest createRequest = ClusterCreateRequest.builder()
      .setName(name)
      .setClusterTemplateName(basicTemplate.getName())
      .setNumMachines(1)
      .setProviderFields(providerFields)
      .build();
    String clusterId = clusterService.requestClusterCreate(createRequest, account);
    Cluster cluster = clusterStore.getCluster(clusterId);
    Assert.assertEquals(basicTemplate, cluster.getClusterTemplate());
    Assert.assertEquals(account, cluster.getAccount());
    Assert.assertEquals(name, cluster.getName());
    Assert.assertEquals(provider.getName(), cluster.getProvider().getName());
    // key is a sensitive field, should be in the credential store and not in the cluster object
    // url is a weird case. admin fields that are overridable and sensitive means the user provided override should
    // never be persisted, but the admin given default can be persisted.
    Map<String, String> expectedSensitiveFields = ImmutableMap.of(
      "key", "keycontents",
      "url", "internal.net/api"
    );
    Map<String, String> expectedNonsensitiveFields = ImmutableMap.of(
      "keyname", "myname",
      "region", "dfw",
      "url", "http://abc.com/api"
    );
    Assert.assertEquals(expectedNonsensitiveFields, cluster.getProvider().getProvisionerFields());
    Assert.assertEquals(expectedSensitiveFields, credentialStore.get(account.getTenantId(), clusterId));
  }

  @Test(expected = MissingEntityException.class)
  public void testInvalidProviderClusterCreate() throws Exception {
    String name = "clusty";
    Map<String, String> providerFields = ImmutableMap.of("keyname", "ec2", "key", "keycontents");
    ClusterCreateRequest createRequest = ClusterCreateRequest.builder()
      .setName(name)
      .setClusterTemplateName(basicTemplate.getName())
      .setProviderName("not" + provider.getName())
      .setNumMachines(1)
      .setProviderFields(providerFields)
      .build();
    clusterService.requestClusterCreate(createRequest, account);
  }

  @Test(expected = MissingEntityException.class)
  public void testInvalidTemplateClusterCreate() throws Exception {
    String name = "clusty";
    Map<String, String> providerFields = ImmutableMap.of("keyname", "ec2", "key", "keycontents");
    ClusterCreateRequest createRequest = ClusterCreateRequest.builder()
      .setName(name)
      .setClusterTemplateName("not" + basicTemplate.getName())
      .setNumMachines(1)
      .setProviderFields(providerFields)
      .build();
    clusterService.requestClusterCreate(createRequest, account);
  }

  @Test
  public void testClusterConfigure() throws Exception {
    Cluster cluster = createActiveCluster();

    Map<String, String> providerFields = Maps.newHashMap();
    providerFields.put("keyname", "somename");
    providerFields.put("key", "somecontents");
    providerFields.put("url", "internal.net/api");
    ClusterConfigureRequest configureRequest = new ClusterConfigureRequest(providerFields, new JsonObject(), false);
    clusterService.requestClusterReconfigure(cluster.getId(), account, configureRequest);

    cluster = clusterStore.getCluster(cluster.getId());
    // key and url are both sensitive fields
    Map<String, String> expectedSensitiveFields = ImmutableMap.of(
      "key", "somecontents",
      "url", "internal.net/api"
    );
    // nonsensitive fields should be everything currently in the provider plus the nonsensitive user fields
    // given in the request
    Map<String, String> expectedNonsensitiveFields = Maps.newHashMap(provider.getProvisionerFields());
    expectedNonsensitiveFields.put("keyname", "somename");
    Assert.assertEquals(expectedNonsensitiveFields, cluster.getProvider().getProvisionerFields());
    Assert.assertEquals(expectedSensitiveFields, credentialStore.get(account.getTenantId(), cluster.getId()));
  }

  @Test
  public void testRequiredUserFields() throws Exception {
    Cluster cluster = createActiveCluster();

    // the "key" user field is required. Should throw an except if its not set.
    Map<String, String> providerFields = Maps.newHashMap();
    ClusterConfigureRequest configureRequest = new ClusterConfigureRequest(providerFields, new JsonObject(), false);
    boolean failed = false;
    try {
      clusterService.requestClusterReconfigure(cluster.getId(), account, configureRequest);
    } catch (IllegalArgumentException e) {
      // this is expected
      failed = true;
    }
    Assert.assertTrue(failed);

    // now try with required user field set
    providerFields.put("key", "keycontents");
    configureRequest = new ClusterConfigureRequest(providerFields, new JsonObject(), false);
    clusterService.requestClusterReconfigure(cluster.getId(), account, configureRequest);

    // nonsensitive fields should be everything currently in the provider before we get the updated cluster
    Map<String, String> expectedNonsensitiveFields = cluster.getProvider().getProvisionerFields();
    // get the updated cluster
    cluster = clusterStore.getCluster(cluster.getId());
    // key and url are both sensitive fields
    Map<String, String> expectedSensitiveFields = ImmutableMap.of(
      "key", "keycontents"
    );
    Assert.assertEquals(expectedNonsensitiveFields, cluster.getProvider().getProvisionerFields());
    Assert.assertEquals(expectedSensitiveFields, credentialStore.get(account.getTenantId(), cluster.getId()));
  }

  @Test
  public void testSensitiveUserFields() throws Exception {
    Map<String, String> sensitiveFields = Maps.newHashMap();
    sensitiveFields.put("key", "keycontents");
    AddServicesRequest addRequest = new AddServicesRequest(sensitiveFields, ImmutableSet.of(service2.getName()));
    ClusterOperationRequest opRequest = new ClusterOperationRequest(sensitiveFields);

    Cluster cluster = createActiveCluster();
    clusterService.requestAddServices(cluster.getId(), account, addRequest);
    testSensitiveFieldsAdded(cluster, sensitiveFields);
    clusterStore.deleteCluster(cluster.getId());

    cluster = createActiveCluster();
    clusterService.requestClusterDelete(cluster.getId(), account, opRequest);
    testSensitiveFieldsAdded(cluster, sensitiveFields);
    clusterStore.deleteCluster(cluster.getId());

    cluster = createActiveCluster();
    clusterService.requestServiceRuntimeAction(cluster.getId(), account, ClusterAction.RESTART_SERVICES,
                                               service1.getName(), opRequest);
    testSensitiveFieldsAdded(cluster, sensitiveFields);
    clusterStore.deleteCluster(cluster.getId());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testMissingRequestThrowsException() throws Exception {
    Cluster cluster = createActiveCluster();
    clusterService.requestClusterDelete(cluster.getId(), cluster.getAccount(), null);
  }

  // test that sensitive user fields were added to the credential store
  private void testSensitiveFieldsAdded(Cluster cluster, Map<String, String> sensitiveFields) throws Exception {
    // nonsensitive fields should be everything currently in the provider before we get the updated cluster
    Map<String, String> expectedNonsensitiveFields = cluster.getProvider().getProvisionerFields();
    // get the updated cluster
    cluster = clusterStore.getCluster(cluster.getId());
    // nonsensitive fields should be everything currently in the provider plus the nonsensitive user fields
    // given in the request
    Assert.assertEquals(expectedNonsensitiveFields, cluster.getProvider().getProvisionerFields());
    Assert.assertEquals(sensitiveFields, credentialStore.get(account.getTenantId(), cluster.getId()));
    credentialStore.wipe();
  }

  @Test
  public void testAddServices() throws Exception {
    Cluster cluster = createActiveCluster();
    // add required sensitive user field
    Map<String, String> sensitiveFields = Maps.newHashMap();
    sensitiveFields.put("key", "keycontents");
    AddServicesRequest addServicesRequest =
      new AddServicesRequest(sensitiveFields, ImmutableSet.of(service2.getName()));
    clusterService.requestAddServices(cluster.getId(), account, addServicesRequest);

    // nonsensitive fields should be everything currently in the provider before we get the updated cluster
    Map<String, String> expectedNonsensitiveFields = cluster.getProvider().getProvisionerFields();
    // get the updated cluster
    cluster = clusterStore.getCluster(cluster.getId());
    // nonsensitive fields should be everything currently in the provider plus the nonsensitive user fields
    // given in the request
    Assert.assertEquals(expectedNonsensitiveFields, cluster.getProvider().getProvisionerFields());
    Assert.assertEquals(sensitiveFields, credentialStore.get(account.getTenantId(), cluster.getId()));
  }

  @Test
  public void testUsesExistingCredentials() throws Exception {
    Cluster cluster = createActiveCluster();
    // add required sensitive user field
    Map<String, String> sensitiveFields = Maps.newHashMap();
    sensitiveFields.put("key", "keycontents");
    credentialStore.set(account.getTenantId(), cluster.getId(), sensitiveFields);

    // request doesn't contain the required key field, but it should be picked up from the credential store
    // so this should go through without throwing an exception.
    AddServicesRequest addServicesRequest = new AddServicesRequest(null, ImmutableSet.of(service2.getName()));
    clusterService.requestAddServices(cluster.getId(), account, addServicesRequest);
  }

  private Cluster createActiveCluster() throws IOException, IllegalAccessException {
    // Simulates an active cluster. The existing cluster will have
    // some user fields already specified from the initial cluster create operation, but another user field will
    // not exist because it is a sensitive field and was thus never persisted.
    // create a provider that already has the 'keyname' user field specified.
    Provider provider1 = new Provider("provider", "description", providerType.getName(),
                                      ImmutableMap.of(
                                        "region", "iad",
                                        "url", "http://abc.com/api",
                                        "keyname", "mykey"));
    // write the cluster to the store
    String clusterId = "123";
    Cluster cluster = Cluster.builder()
      .setName("cluster1")
      .setID(clusterId)
      .setProvider(provider1)
      .setClusterTemplate(basicTemplate)
      .setServices(ImmutableSet.of(service1.getName()))
      .setNodes(ImmutableSet.of("node1"))
      .setAccount(account)
      .setStatus(Cluster.Status.ACTIVE)
      .setLatestJobID(new JobId(clusterId, 1).getId())
      .build();
    clusterStore.writeCluster(cluster);
    return cluster;
  }
}
