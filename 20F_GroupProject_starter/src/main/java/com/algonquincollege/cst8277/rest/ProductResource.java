/*****************************************************************c******************o*******v******id********
 * File: ProductResource.java
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
import static com.algonquincollege.cst8277.utils.MyConstants.PRODUCT_RESOURCE_NAME;
import static com.algonquincollege.cst8277.utils.MyConstants.RESOURCE_PATH_ID_ELEMENT;
import static com.algonquincollege.cst8277.utils.MyConstants.RESOURCE_PATH_ID_PATH;

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

@Path(PRODUCT_RESOURCE_NAME)
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ProductResource {

    @EJB
    protected CustomerService customerServiceBean;

    @Inject
    protected ServletContext servletContext;
    
    @Inject
    protected SecurityContext sc;

    /**
     * 
     * @return the reponse for getting all products
     */
    @RolesAllowed(ADMIN_ROLE)
    @GET
    public Response getProducts() {
        servletContext.log("retrieving all products ...");
        List<ProductPojo> custs = customerServiceBean.getAllProducts();
        Response response = Response.ok(custs).build();
        return response;
    }

    /**
     * 
     * @param id
     * @return the response for getting a specific product by its id
     */
    @GET
    @Path(RESOURCE_PATH_ID_PATH)
    public Response getProductById(@PathParam(RESOURCE_PATH_ID_ELEMENT) int id) {
        servletContext.log("try to retrieve specific product " + id);
        ProductPojo theProduct = customerServiceBean.getProductById(id);
        Response response = Response.ok(theProduct).build();
        return response;
    }
    
    /**
     * 
     * @param newProduct
     * @return the response for creating a ne product
     */
    @RolesAllowed(ADMIN_ROLE)
    @POST
    @Transactional
    public Response addProduct(ProductPojo newProduct) {
      servletContext.log("try to product store");
      Response response = null;
      ProductPojo newProductWithIdTimestamps = customerServiceBean.persistProduct(newProduct);
      response = Response.ok(newProductWithIdTimestamps).build();
      return response;
    }

    /**
     * 
     * @param id
     * @param newStore
     * @return the response for updating a product with new store
     */
    @RolesAllowed(ADMIN_ROLE)
    @PUT
    @Path(RESOURCE_PATH_ID_PATH)
    public Response addStoreForProduct(@PathParam(RESOURCE_PATH_ID_ELEMENT) int id, HashSet<StorePojo> newStore) {
      Response response = null;
      ProductPojo updatedProduct = customerServiceBean.setStoreFor(id, newStore);
      response = Response.ok(updatedProduct).build();
      return response;
    }
    
    /**
     * 
     * @param id
     * @return the response for deleting a specific product by its id
     */
    @RolesAllowed(ADMIN_ROLE)
    @DELETE
    @Path(RESOURCE_PATH_ID_PATH)
    public Response deleteProductById(@PathParam(RESOURCE_PATH_ID_ELEMENT) int id) {
        servletContext.log("try to delete specific product " + id);
        Response response = null;
        ProductPojo prod = customerServiceBean.getProductById(id);
        if (prod == null) {
            response= Response.status(Status.NOT_FOUND).build();
        }else {
            customerServiceBean.deleteProductById(id);
            response= Response.noContent().build();
        }
        return response;
    }

}