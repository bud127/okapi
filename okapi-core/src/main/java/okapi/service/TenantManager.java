/*
 * Copyright (c) 2015-2016, Index Data
 * All rights reserved.
 * See the file LICENSE for details.
 */

package okapi.service;

import io.vertx.core.json.Json;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import okapi.bean.Tenant;
import okapi.util.ErrorType;
import static okapi.util.ErrorType.*;



/**
 * Manages the tenants in the run-time system.
 * These will be modified by the web service, and (more often) reloaded from
 * the storage. Note that these are all in-memory operations, so there is no
 * need to use vert.x callbacks for this.
 */

public class TenantManager {
  // TODO Pass a ModuleManager, and validate the modules we try to enable etc. Or do that in the web service?
  Map<String, Tenant> tenants = new HashMap<>();

  public boolean insert(Tenant t) {
    String id = t.getId();
    if ( tenants.containsKey(id)) {
      System.out.println("TenantManager: Not inserting duplicate '" + id + "':" + Json.encode(t));
      return false;
    }
    tenants.put(id, t);
    System.out.println("TenantManager: Inserted '" + id + "':" + Json.encode(t));
    return true;
  }

  public boolean update(Tenant t) {
    String id = t.getId();
    if ( ! tenants.containsKey(id)) {
      System.out.println("TenantManager: Tenant '" + id + "' not found, can not update");
      return false;
    }
    tenants.put(id, t);
    System.out.println("TenantManager: Updated '" + id + "':" + Json.encode(t));
    return true;
  }

  public Set<String> getIds() {
    Set<String> ids = tenants.keySet();
    System.out.println("TenantManager: getIds: " + Json.encode(ids));
    return ids;
  }

  public Tenant get(String id) {
    return tenants.get(id);
  }

  /**
   * Delete a tenant.
   * @param id
   * @return true on success, false if not there.
   */
  public boolean delete(String id) {
    if (!tenants.containsKey(id)) {
      System.out.println("TenantManager: Tenant '" + id + "' not found, can not delete");
      return false;
    }
    tenants.remove(id);
    System.out.println("TenantManager: Deleted '" + id + "'");
    return true;
  }

  /**
   * Enable a module for a given tenant.
   *
   * @param id
   * @param module
   * @return
   */
  public ErrorType enableModule(String id, String module ) {
    Tenant tenant = tenants.get(id);
    if (tenant == null) {
      return NOT_FOUND;
    }
    // TODO - Check if we know about the module. 
    tenant.enableModule(module);
    return OK;
  }
  /**
   * Disable a module for a given tenant.
   *
   * @param id
   * @param module
   * @return
   */
  public ErrorType disableModule(String id, String module ) {
    Tenant tenant = tenants.get(id);
    if (tenant == null) {
      return USER; // Indicates tenant not found
    }
    if (tenant.isEnabled(module)) {
      tenant.disableModule(module);
      return OK;
    } else
      return NOT_FOUND; // tenant ok, but no such module
  }


  /**
   * List modules for a given tenant.
   * @param id
   * @return null if no such tenant, or a list (possibly empty)
   */
  public Set<String> listModules(String id) {
    Tenant tenant = tenants.get(id);
    if (tenant == null)
      return null;
    return tenant.listModules();
  }

} // class