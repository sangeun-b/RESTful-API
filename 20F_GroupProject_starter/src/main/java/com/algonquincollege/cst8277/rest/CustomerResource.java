/*****************************************************************c******************o*******v******id********
 * File: CustomerResource.java
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
import static com.algonquincollege.cst8277.utils.MyConstants.CUSTOMER_RESOURCE_NAME;
import static com.algonquincollege.cst8277.utils.MyConstants.ORDER_RESOURCE_NAME;
import static com.algonquincollege.cst8277.utils.MyConstants.RESOURCE_PATH_ID_ELEMENT;
import static com.algonquincollege.cst8277.utils.MyConstants.RESOURCE_PATH_ID_PATH;
import static com.algonquincollege.cst8277.utils.MyConstants.SLASH;
import static com.algonquincollege.cst8277.utils.MyConstants.PRODUCT_RESOURCE_NAME;
import static com.algonquincollege.cst8277.utils.MyConstants.USER_ROLE;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static javax.ws.rs.core.Response.Status.OK;

import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.inject.Inject;
import javax.security.enterprise.SecurityContext;
import javax.servlet.ServletContext;
import javax.transaction.Transactional;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.glassfish.soteria.WrappingCallerPrincipal;

import com.algonquincollege.cst8277.ejb.CustomerService;
import com.algonquincollege.cst8277.models.AddressPojo;
import com.algonquincollege.cst8277.models.CustomerPojo;
import com.algonquincollege.cst8277.models.OrderLinePojo;
import com.algonquincollege.cst8277.models.OrderPojo;
import com.algonquincollege.cst8277.models.ProductPojo;
import com.algonquincollege.cst8277.models.SecurityUser;

@Path(CUSTOMER_RESOURCE_NAME)
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class CustomerResource {

    @EJB
    protected CustomerService customerServiceBean;

    @Inject
    protected ServletContext servletContext;

    @Inject
    protected SecurityContext sc;

    /**
     * the status for getting all customers
     * @return
     */
    @RolesAllowed(ADMIN_ROLE)
    @GET
    public Response getCustomers() {
        servletContext.log("retrieving all customers ...");
        List<CustomerPojo> custs = customerServiceBean.getAllCustomers();
        Response response = Response.ok(custs).build();
        return response;
    }

    /**
     * 
     * @param id
     * @return the status for getting a specific customer by id
     */
    @GET
    @Path(RESOURCE_PATH_ID_PATH)
    public Response getCustomerById(@PathParam(RESOURCE_PATH_ID_ELEMENT) int id) {
        servletContext.log("try to retrieve specific customer " + id);
        Response response = null;
        CustomerPojo cust = null;

        if (sc.isCallerInRole(ADMIN_ROLE)) {
            cust = customerServiceBean.getCustomerById(id);
            response = Response.status( cust == null ? NOT_FOUND : OK).entity(cust).build();
        }
        else if (sc.isCallerInRole(USER_ROLE)) {
            WrappingCallerPrincipal wCallerPrincipal = (WrappingCallerPrincipal)sc.getCallerPrincipal();
            SecurityUser sUser = (SecurityUser)wCallerPrincipal.getWrapped();
            cust = sUser.getCustomer();
            if (cust != null && cust.getId() == id) {
                response = Response.status(OK).entity(cust).build();
            }
            else {
                throw new ForbiddenException();
            }
        }
        else {
            response = Response.status(BAD_REQUEST).build();
        }
        return response;
    }

    /**
     * 
     * @param newCustomer
     * @return the status for creating a new customer
     */
    @RolesAllowed(ADMIN_ROLE)
    @POST
    @Transactional
    public Response addCustomer(CustomerPojo newCustomer) {
      servletContext.log("try to add customer");
      Response response = null;
      CustomerPojo newCustomerWithIdTimestamps = customerServiceBean.persistCustomer(newCustomer);
      //build a SecurityUser linked to the new customer
      customerServiceBean.buildUserForNewCustomer(newCustomerWithIdTimestamps);
      response = Response.ok(newCustomerWithIdTimestamps).build();
      return response;
    }

    /**
     * add address for a customer
     * @param id
     * @param newAddress
     * @return the status for updating an existing customer
     */
    @RolesAllowed(ADMIN_ROLE)
    @PUT
    @Path(RESOURCE_PATH_ID_PATH)
    public Response addAddressForCustomer(@PathParam(RESOURCE_PATH_ID_ELEMENT) int id, AddressPojo newAddress) {
      Response response = null;
      CustomerPojo updatedCustomer = customerServiceBean.setAddressFor(id, newAddress);
      response = Response.ok(updatedCustomer).build();
      return response;
    }
    
    /**
     * 
     * @param id
     * @return the status for deleting a specific customer by id
     */
    @RolesAllowed(ADMIN_ROLE)
    @DELETE
    @Path(RESOURCE_PATH_ID_PATH)
    public Response deleteCustomerById(@PathParam(RESOURCE_PATH_ID_ELEMENT) int id) {
        servletContext.log("try to delete specific customer " + id);
        Response response = null;
        CustomerPojo cust = customerServiceBean.getCustomerById(id);
        if (cust == null) {
            response= Response.status(Status.NOT_FOUND).build();
        }else {
            customerServiceBean.deleteCustomerById(id);
            response= Response.noContent().build();
        }
        return response;
    }
    //TODO - endpoints for setting up Orders/OrderLines
    
    /**
     * 
     * @return the status for getting all orders
     */
    @RolesAllowed(ADMIN_ROLE)
    @GET
    @Path(SLASH+ORDER_RESOURCE_NAME)
    public Response getOrders() {
        servletContext.log("retrieving all orders ...");
        List<OrderPojo> orders = customerServiceBean.getAllOrders();
        Response response = Response.ok(orders).build();
        return response;
    }
    
    /**
     * 
     * @param id
     * @return the status for getting a specific order by its id
     */
    @GET
    @Path(SLASH+ORDER_RESOURCE_NAME+RESOURCE_PATH_ID_PATH)
    public Response getOrderById(@PathParam(RESOURCE_PATH_ID_ELEMENT) int id) {
        servletContext.log("try to retrieve specific order " + id);
        OrderPojo or = customerServiceBean.getOrderById(id);
        Response response = Response.ok(or).build();
        return response;
    }
    
    /**
     * the status for creating a new order
     */
    @Transactional
    @RolesAllowed(ADMIN_ROLE)
    @POST
    @Path(SLASH+ORDER_RESOURCE_NAME)
    public Response addOrder(OrderPojo newOrder) {
      Response response = null;
      OrderPojo or= customerServiceBean.persistOrder(newOrder);
      response = Response.ok(or).build();
      return response;
    }
    
   
    /**
     * 
     * @param id
     * @param cust
     * @return the status for updating an order with owning customer
     */
    @RolesAllowed(ADMIN_ROLE)
    @PUT
    @Path(SLASH+ORDER_RESOURCE_NAME+RESOURCE_PATH_ID_PATH)
    public Response addCustomerForOrder(@PathParam(RESOURCE_PATH_ID_ELEMENT)int id, CustomerPojo cust) {
      Response response = null;
      OrderPojo or= customerServiceBean.persistCustomerForOrder(id, cust);
      response = Response.ok(or).build();
      return response;
    }
    
    /**
     * 
     * @param id
     * @param ol
     * @return the status for updating an order with orderLine
     */
    @RolesAllowed(ADMIN_ROLE)
    @POST
    @Path(SLASH+ORDER_RESOURCE_NAME+RESOURCE_PATH_ID_PATH + SLASH + "orderline")
    public Response addOrderlineForOrder(@PathParam(RESOURCE_PATH_ID_ELEMENT) int id, List<OrderLinePojo> ol){
      Response response = null;
      OrderPojo updatedOrder = customerServiceBean.setOrderLineFor(id, ol);
      response = Response.ok(updatedOrder).build();
      return response;
    }
    
    /**
     * 
     * @param id
     * @return the status for getting all orders belong to a specific customer
     */
    @RolesAllowed(ADMIN_ROLE)
    @GET
    @Path(RESOURCE_PATH_ID_PATH + SLASH + ORDER_RESOURCE_NAME)
    public Response getOrdersForCustomer(@PathParam(RESOURCE_PATH_ID_ELEMENT) int id) {
        servletContext.log("try to retireve orders for specific customer " + id);
        List<OrderPojo> orders = customerServiceBean.getAllOrders(id);
        Response response = Response.ok(orders).build();
        return response;
    }
   
    /**
     * 
     * @param id
     * @return the status for getting all products belong to a specific order
     */
    @RolesAllowed(ADMIN_ROLE)
    @GET
    @Path(RESOURCE_PATH_ID_PATH + SLASH + PRODUCT_RESOURCE_NAME)
    public Response getProductsForOrder(@PathParam(RESOURCE_PATH_ID_ELEMENT) int id) {
        servletContext.log("try to retireve products for specific order " + id);
        List<ProductPojo> products = customerServiceBean.getAllProducts(id);
        Response response = Response.ok(products).build();
        return response;
    }
    

}