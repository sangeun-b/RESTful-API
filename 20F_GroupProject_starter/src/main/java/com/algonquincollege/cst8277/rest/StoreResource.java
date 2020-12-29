/*****************************************************************c******************o*******v******id********
 * File: StoreResource.java
 * Course materials (20F) CST 8277
 *
 * @author (original) Mike Norman
 * 
 * update by : @author Sangeun Baek 040953608 
 *             @author Hsing-I Wang 040953737
 *             @author Qi Wang 040946448
 *
 */
package com.algonquincollege.cst8277.rest;

import static com.algonquincollege.cst8277.utils.MyConstants.ADMIN_ROLE;
import static com.algonquincollege.cst8277.utils.MyConstants.RESOURCE_PATH_ID_ELEMENT;
import static com.algonquincollege.cst8277.utils.MyConstants.RESOURCE_PATH_ID_PATH;
import static com.algonquincollege.cst8277.utils.MyConstants.STORE_RESOURCE_NAME;

import java.util.HashSet;
import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.inject.Inject;
import javax.security.enterprise.SecurityContext;
import javax.servlet.ServletContext;
import javax.transaction.Transactional;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.algonquincollege.cst8277.ejb.CustomerService;
import com.algonquincollege.cst8277.models.ProductPojo;
import com.algonquincollege.cst8277.models.StorePojo;

@Path(STORE_RESOURCE_NAME)
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class StoreResource {

    @EJB
    protected CustomerService customerServiceBean;

    @Inject
    protected ServletContext servletContext;
    
    @Inject
    protected SecurityContext sc;

    /**
     * 
     * @return the response for getting all stores
     */
    @RolesAllowed(ADMIN_ROLE)
    @GET
    public Response getStores() {
        servletContext.log("retrieving all stores ...");
        List<StorePojo> stores = customerServiceBean.getAllStores();
        Response response = Response.ok(stores).build();
        return response;
    }

    /**
     * 
     * @param id
     * @return the response for getting a specific store by its id
     */
    @GET
    @Path(RESOURCE_PATH_ID_PATH)
    public Response getStoreById(@PathParam(RESOURCE_PATH_ID_ELEMENT) int id) {
        servletContext.log("try to retrieve specific store " + id);
        StorePojo theStore = customerServiceBean.getStoreById(id);
        Response response = Response.ok(theStore).build();
        return response;
    }
    
    /**
     * the response for creating a new store
     */
    @RolesAllowed(ADMIN_ROLE)
    @POST
    @Transactional
    public Response addStore(StorePojo newStore) {
      servletContext.log("try to add store");
      Response response = null;
      StorePojo newStoreWithIdTimestamps = customerServiceBean.persistStore(newStore);
      response = Response.ok(newStoreWithIdTimestamps).build();
      return response;
    }

    /**
     * 
     * @param id
     * @param newProduct
     * @return the response for updating a store with new product
     */
    @RolesAllowed(ADMIN_ROLE)
    @PUT
    @Path(RESOURCE_PATH_ID_PATH)
    public Response addProductForStore(@PathParam(RESOURCE_PATH_ID_ELEMENT) int id, HashSet<ProductPojo> newProduct) {
      Response response = null;
      StorePojo updatedStore = customerServiceBean.setProductFor(id, newProduct);
      response = Response.ok(updatedStore).build();
      return response;
    }
    
    /**
     * 
     * @param id
     * @return the response for deleting a specific store by its id
     */
    @RolesAllowed(ADMIN_ROLE)
    @DELETE
    @Path(RESOURCE_PATH_ID_PATH)
    public Response deleteStoreById(@PathParam(RESOURCE_PATH_ID_ELEMENT) int id) {
        servletContext.log("try to delete specific store " + id);
        Response response = null;
        StorePojo store = customerServiceBean.getStoreById(id);
        if (store == null) {
            response= Response.status(Status.NOT_FOUND).build();
        }else {
            customerServiceBean.deleteStoreById(id);
            response= Response.noContent().build();
        }
        return response;
    }
}