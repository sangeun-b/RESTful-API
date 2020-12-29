/*****************************************************************c******************o*******v******id********
 * File: OrderSystemTestSuite.java
 * Course materials (20F) CST 8277
 * (Original Author) Mike Norman
 *
 * @date 2020 10
 *
 * (Modified) @author Sangeun Baek 040953608 
 *            @author Hsing-I Wang 040953737
 *            @author Qi Wang 040946448
 */
package com.algonquincollege.cst8277;

import static com.algonquincollege.cst8277.utils.MyConstants.APPLICATION_API_VERSION;
import static com.algonquincollege.cst8277.utils.MyConstants.CUSTOMER_RESOURCE_NAME;
import static com.algonquincollege.cst8277.utils.MyConstants.DEFAULT_ADMIN_USER;
import static com.algonquincollege.cst8277.utils.MyConstants.DEFAULT_ADMIN_USER_PASSWORD;
import static com.algonquincollege.cst8277.utils.MyConstants.DEFAULT_USER_PASSWORD;
import static com.algonquincollege.cst8277.utils.MyConstants.DEFAULT_USER_PREFIX;
import static com.algonquincollege.cst8277.utils.MyConstants.STORE_RESOURCE_NAME;
import static com.algonquincollege.cst8277.utils.MyConstants.PRODUCT_RESOURCE_NAME;
import static com.algonquincollege.cst8277.utils.MyConstants.ORDER_RESOURCE_NAME;
import static com.algonquincollege.cst8277.utils.MyConstants.SLASH;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.collection.IsEmptyCollection.empty;

import java.lang.invoke.MethodHandles;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.glassfish.jersey.logging.LoggingFeature;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

import com.algonquincollege.cst8277.models.BillingAddressPojo;
import com.algonquincollege.cst8277.models.CustomerPojo;
import com.algonquincollege.cst8277.models.OrderLinePk;
import com.algonquincollege.cst8277.models.OrderLinePojo;
import com.algonquincollege.cst8277.models.OrderPojo;
import com.algonquincollege.cst8277.models.ProductPojo;
import com.algonquincollege.cst8277.models.StorePojo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

@TestMethodOrder(MethodOrderer.Alphanumeric.class)
public class OrderSystemTestSuite {
    private static final Class<?> _thisClaz = MethodHandles.lookup().lookupClass();
    private static final Logger logger = LoggerFactory.getLogger(_thisClaz);

    static final String APPLICATION_CONTEXT_ROOT = "rest-orderSystem";
    static final String HTTP_SCHEMA = "http";
    static final String HOST = "localhost";
    
    //TODO - if you changed your Payara's default port (to say for example 9090)
    //       your may need to alter this constant
    static final int PORT = 8080;

    // test fixture(s)
    static URI uri;
    static HttpAuthenticationFeature adminAuth;
    static HttpAuthenticationFeature userAuth;
    protected EntityManager em;

    @BeforeAll
    public static void oneTimeSetUp() throws Exception {
        logger.debug("oneTimeSetUp");
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();
        uri = UriBuilder
            .fromUri(APPLICATION_CONTEXT_ROOT + APPLICATION_API_VERSION)
            .scheme(HTTP_SCHEMA)
            .host(HOST)
            .port(PORT)
            .build();
        adminAuth = HttpAuthenticationFeature.basic(DEFAULT_ADMIN_USER, DEFAULT_ADMIN_USER_PASSWORD);
        userAuth = HttpAuthenticationFeature.basic(DEFAULT_USER_PREFIX, DEFAULT_USER_PASSWORD);
    }

    protected WebTarget webTarget;
    @BeforeEach
    public void setUp() {
        Client client = ClientBuilder.newClient(
            new ClientConfig().register(MyObjectMapperProvider.class).register(new LoggingFeature()));
        webTarget = client.target(uri);
    }

    /**
     * create a new customer without address by admin
     * @throws JsonMappingException
     * @throws JsonProcessingException
     */
    @Test
    public void test01_add_customer_noAddress_with_adminrole() throws JsonMappingException, JsonProcessingException {
        CustomerPojo cust= new CustomerPojo();
        cust.setId(1);
        cust.setFirstName("test fistname");
        cust.setLastName("test lastname");
        cust.setEmail("test@gmail.com");
        cust.setPhoneNumber("111-111-1111");
        Response response = webTarget
            .register(adminAuth)
            .path(CUSTOMER_RESOURCE_NAME)
            .request()
            .post(Entity.entity(cust, MediaType.APPLICATION_JSON));
        assertThat(response.getStatus(), is(200));
    }
    

    /**
     * create a new customer with address by admin
     * @throws JsonMappingException
     * @throws JsonProcessingException
     */
    @Test
    public void test02_add_customer_hasAddress_with_adminrole() throws JsonMappingException, JsonProcessingException {
        CustomerPojo cust= new CustomerPojo();
        cust.setId(2);
        cust.setFirstName("John");
        cust.setLastName("Yong");
        cust.setEmail("yong@gmail.com");
        cust.setPhoneNumber("123-456-0789");
        BillingAddressPojo addr= new BillingAddressPojo();
        addr.setCountry("country A");
        addr.setState("state A");
        addr.setCity("city A");
        addr.setStreet("street A");
        addr.setPostal("1Ab 3V4");
        addr.setAlsoShipping(true);
        cust.setBillingAddress(addr);
        Response response = webTarget
             .register(adminAuth)
             .path(CUSTOMER_RESOURCE_NAME)
             .request()
             .post(Entity.entity(cust, MediaType.APPLICATION_JSON));
        assertThat(response.getStatus(), is(200));
    }
    
    /**
     * add address for an existing customer by admin
     * @throws JsonMappingException
     * @throws JsonProcessingException
     */
    @Test
    public void test03_set_address_for_customer_with_adminrole() throws JsonMappingException, JsonProcessingException {
        BillingAddressPojo addr= new BillingAddressPojo();
        addr.setCountry("Canada update");
        addr.setState("ON update");
        addr.setCity("Toronto update");
        addr.setStreet("street update");
        addr.setPostal("1A2 3B4");
        addr.setAlsoShipping(true);
        
        Response response = webTarget
            .register(adminAuth)
            .path(CUSTOMER_RESOURCE_NAME+"/2")
            .request()
            .put(Entity.entity(addr, MediaType.APPLICATION_JSON));
        assertThat(response.getStatus(), is(200));
    }

    /**
     * add address for an existing customer by user
     * @throws JsonMappingException
     * @throws JsonProcessingException
     */
    @Test
    public void test04_set_address_for_customer_with_userrole() throws JsonMappingException, JsonProcessingException {
        userAuth = HttpAuthenticationFeature.basic(DEFAULT_USER_PREFIX+"2", DEFAULT_USER_PASSWORD);
        BillingAddressPojo addr= new BillingAddressPojo();
        addr.setCountry("update Canada");
        addr.setState("update ON");
        addr.setCity("update Ottawa");
        addr.setStreet("update street");
        addr.setPostal("1A5 6C7");
        addr.setAlsoShipping(true);
        
        Response response = webTarget
            .register(userAuth)
            .path(CUSTOMER_RESOURCE_NAME+"/2")
            .request()
            .put(Entity.entity(addr, MediaType.APPLICATION_JSON));
        assertThat(response.getStatus(), is(403));
    }
     
    /**
     * delete a specific customer by user
     * @throws JsonMappingException
     * @throws JsonProcessingException
     */
    @Test
    public void test05_delete_customer_by_userrole()throws JsonMappingException, JsonProcessingException {
        Response response = webTarget
            .register(userAuth)
            .path(CUSTOMER_RESOURCE_NAME+"/2")
            .request()
            .delete();
        assertThat(response.getStatus(), is(403));
    }
    
    /**
     * get all customers by admin
     * @throws JsonMappingException
     * @throws JsonProcessingException
     */
    @Test
    public void test06_all_customers_with_adminrole() throws JsonMappingException, JsonProcessingException {
        Response response = webTarget
            .register(adminAuth)
            .path(CUSTOMER_RESOURCE_NAME)
            .request()
            .get();
        assertThat(response.getStatus(), is(200));
        List<CustomerPojo> custs = response.readEntity(new GenericType<List<CustomerPojo>>(){});
        assertThat(custs, is(not(empty())));
        assertThat(custs, hasSize(2));
    }
        
    /**
     * get all customer by user
     * @throws JsonMappingException
     * @throws JsonProcessingException
     */
    @Test
    public void test07_all_customers_with_userrole() throws JsonMappingException, JsonProcessingException{
        Response response = webTarget
            .register(userAuth)
            .path(CUSTOMER_RESOURCE_NAME)
            .request()
            .get();
        assertThat(response.getStatus(), is(403));
    }
    
    /**
     * find a specific customer1 by admin
     * @throws JsonMappingException
     * @throws JsonProcessingException
     */
    @Test
    public void test08_find_customer1_with_adminrole()throws JsonMappingException, JsonProcessingException {
        Response response = webTarget
            .register(adminAuth)
            .path(CUSTOMER_RESOURCE_NAME+"/1")
            .request()
            .get();
        assertThat(response.getStatus(), is(200));
    }
    
    /**
     * find a specific customer2 by admin
     * @throws JsonMappingException
     * @throws JsonProcessingException
     */
    @Test
    public void test09_find_cutsomer2_type_with_adminrole()throws JsonMappingException, JsonProcessingException {
        Response response = webTarget
            .register(adminAuth)
            .path(CUSTOMER_RESOURCE_NAME+"/2")
            .request()
            .get();
        assertThat(response.getStatus(), is(200));
        assertThat(response.getMediaType(),is(not(MediaType.APPLICATION_XML)));
    }
    
    /**
     * find another customer by user
     * @throws JsonMappingException
     * @throws JsonProcessingException
     */
    @Test
    public void test10_find_customer1_with_userrole()throws JsonMappingException, JsonProcessingException {
        userAuth = HttpAuthenticationFeature.basic(DEFAULT_USER_PREFIX+"2", DEFAULT_USER_PASSWORD);
        Response response = webTarget
            .register(userAuth)
            .path(CUSTOMER_RESOURCE_NAME+"/1")
            .request()
            .get();
        assertThat(response.getStatus(), is(403));
    }
    
    /**
     * find himself by user
     * @throws JsonMappingException
     * @throws JsonProcessingException
     */
    @Test
    public void test11_find_customer2_with_userrole() throws JsonMappingException, JsonProcessingException {
        userAuth = HttpAuthenticationFeature.basic(DEFAULT_USER_PREFIX+"2", DEFAULT_USER_PASSWORD);
        Response response = webTarget
            .register(userAuth)
            .path(CUSTOMER_RESOURCE_NAME+"/2")
            .request()
            .get();
        assertThat(response.getStatus(), is(200));
    }
    
    /**
     * create a new store by admin
     * @throws JsonMappingException
     * @throws JsonProcessingException
     */
    @Test
    public void test12_add_store_with_adminrole() throws JsonMappingException, JsonProcessingException {
        StorePojo store= new StorePojo();
        store.setStoreName("Walmart");
        Response response = webTarget
            .register(adminAuth)
            .path(STORE_RESOURCE_NAME)
            .request()
            .post(Entity.entity(store, MediaType.APPLICATION_JSON));
        assertThat(response.getStatus(), is(200));
    }
    
    /**
     * add product for an existing store by admin
     * @throws JsonMappingException
     * @throws JsonProcessingException
     */
    @Test
    public void test13_set_product_to_store_with_adminrole() throws JsonMappingException, JsonProcessingException{
        Set<ProductPojo> products= new HashSet<ProductPojo>();
        ProductPojo product= new ProductPojo();
        product.setId(1);
        product.setDescription("test description a");
        product.setSerialNo("No.5");
        products.add(product);
        Response response = webTarget
            .register(adminAuth)
            .path(STORE_RESOURCE_NAME + SLASH + "1")
            .request()
            .put(Entity.entity(products, MediaType.APPLICATION_JSON));
        assertThat(response.getStatus(), is(200));
    }
    
    /**
     * create a new store by user
     * @throws JsonMappingException
     * @throws JsonProcessingException
     */
   @Test
   public void test14_add_store_with_userrole() throws JsonMappingException, JsonProcessingException {
       StorePojo store= new StorePojo();
       store.setStoreName("Loblows");
       Set<ProductPojo> products= new HashSet<ProductPojo>();
       ProductPojo product1=new ProductPojo();
       product1.setDescription("test description b");
       product1.setSerialNo("No.6");
       products.add(product1);
       store.setProducts(products);
       userAuth = HttpAuthenticationFeature.basic(DEFAULT_USER_PREFIX+"2", DEFAULT_USER_PASSWORD);
       Response response = webTarget
           .register(userAuth)
           .path(STORE_RESOURCE_NAME)
           .request()
           .post(Entity.entity(store, MediaType.APPLICATION_JSON));
       assertThat(response.getStatus(), is(403));
   }
   
   /**
    * find a specific store by admin
    * @throws JsonMappingException
    * @throws JsonProcessingException
    */
   @Test
   public void test15_find_store1_with_adminrole() throws JsonMappingException, JsonProcessingException {
       Response response = webTarget
           .register(adminAuth)
           .path(STORE_RESOURCE_NAME+"/1")
           .request()
           .get();
       assertThat(response.getStatus(), is(200));
   }
   
   /**
    * find a specific store by user
    * @throws JsonMappingException
    * @throws JsonProcessingException
    */
   @Test
   public void test16_find_store1_with_userrole() throws JsonMappingException, JsonProcessingException {
       userAuth = HttpAuthenticationFeature.basic(DEFAULT_USER_PREFIX+"1", DEFAULT_USER_PASSWORD);
       Response response = webTarget
           .register(userAuth)
           .path(STORE_RESOURCE_NAME+"/1")
           .request()
           .get();
       assertThat(response.getStatus(), is(200));
   }
   
   /**
    * create new product by admin
    * @throws JsonMappingException
    * @throws JsonProcessingException
    */
   @Test
   public void test17_add_product_with_adminrole() throws JsonMappingException, JsonProcessingException {
       ProductPojo product = new ProductPojo();
       product.setDescription("new product");
       product.setSerialNo("No.12");
   
       Response response = webTarget
           .register(adminAuth)
           .path(PRODUCT_RESOURCE_NAME)
           .request()
           .post(Entity.entity(product, MediaType.APPLICATION_JSON));
       assertThat(response.getStatus(), is(200));
   }
   
   /**
    * create new product by user
    * @throws JsonMappingException
    * @throws JsonProcessingException
    */
   @Test
   public void test18_add_product_with_userrole()throws JsonMappingException, JsonProcessingException {
       userAuth = HttpAuthenticationFeature.basic(DEFAULT_USER_PREFIX+"2", DEFAULT_USER_PASSWORD);
       ProductPojo product=new ProductPojo();
       product.setDescription("laptop");
       product.setSerialNo("No.22");
       
       Response response = webTarget
           .register(userAuth)
           .path(PRODUCT_RESOURCE_NAME)
           .request()
           .post(Entity.entity(product, MediaType.APPLICATION_JSON));
       assertThat(response.getStatus(), is(403));
    }
   
   /**
    * add store for an existing product by admin
    * @throws JsonMappingException
    * @throws JsonProcessingException
    */
   @Test
   public void test19_set_store_to_product_with_adminrole() throws JsonMappingException, JsonProcessingException{
       Set<StorePojo> stores= new HashSet<StorePojo>();
       StorePojo store= new StorePojo();
       store.setStoreName("food basic");
       stores.add(store);

       Response response = webTarget
           .register(adminAuth)
           .path(PRODUCT_RESOURCE_NAME + SLASH + "2")
           .request()
           .put(Entity.entity(stores, MediaType.APPLICATION_JSON));
       assertThat(response.getStatus(), is(200));
   }
    
   /**
    * get all stores by admin
    * @throws JsonMappingException
    * @throws JsonProcessingException
    */
   @Test
   public void test20_all_stores_with_adminrole() throws JsonMappingException, JsonProcessingException {
       Response response = webTarget
           .register(adminAuth)
           .path(STORE_RESOURCE_NAME)
           .request()
           .get();
       assertThat(response.getStatus(), is(200));
       List<StorePojo> stores = response.readEntity(new GenericType<List<StorePojo>>(){});
       assertThat(stores, is(not(empty())));
       assertThat(stores, hasSize(2));
   }
   
   /**
    * get all stores by user
    * @throws JsonMappingException
    * @throws JsonProcessingException
    */
   @Test
    public void test21_all_stores_with_userrole() throws JsonMappingException, JsonProcessingException {
        userAuth = HttpAuthenticationFeature.basic(DEFAULT_USER_PREFIX+"1", DEFAULT_USER_PASSWORD);
        Response response = webTarget
            .register(userAuth)
            .path(STORE_RESOURCE_NAME)
            .request()
            .get();
        assertThat(response.getStatus(), is(403));
    }
   
   /**
    * find a specific store by admin
    * @throws JsonMappingException
    * @throws JsonProcessingException
    */
   @Test
   public void test22_find_store2_type_with_adminrole() throws JsonMappingException, JsonProcessingException {
       Response response = webTarget
           .register(adminAuth)
           .path(STORE_RESOURCE_NAME+"/2")
           .request()
           .get();
        assertThat(response.getMediaType(),is(not(MediaType.APPLICATION_XML)));
   }
      
   /**
    * find a specific product by admin
    * @throws JsonMappingException
    * @throws JsonProcessingException
    */
   @Test
   public void test23_find_product1_with_adminrole() throws JsonMappingException, JsonProcessingException {
       Response response = webTarget
           .register(adminAuth)
           .path(PRODUCT_RESOURCE_NAME+"/1")
           .request()
           .get();
       assertThat(response.getStatus(), is(200));
   }
   
   /**
    * find a specific product by user
    * @throws JsonMappingException
    * @throws JsonProcessingException
    */
   @Test
   public void test24_find_product1_with_userrole() throws JsonMappingException, JsonProcessingException {
       userAuth = HttpAuthenticationFeature.basic(DEFAULT_USER_PREFIX+"2", DEFAULT_USER_PASSWORD);
       Response response = webTarget
           .register(userAuth)
           .path(PRODUCT_RESOURCE_NAME+"/1")
           .request()
           .get();
       assertThat(response.getStatus(), is(200));
   }
   
   /**
    * find a specific product by admin
    * @throws JsonMappingException
    * @throws JsonProcessingException
    */
   @Test
   public void test25_find_product2_type_with_adminrole() throws JsonMappingException, JsonProcessingException {
       Response response = webTarget
           .register(adminAuth)
           .path(PRODUCT_RESOURCE_NAME+"/2")
           .request()
           .get();
       assertThat(response.getMediaType(),is(not(MediaType.APPLICATION_XML)));
   }
    
   /**
    * add product for an existing store by user
    * @throws JsonMappingException
    * @throws JsonProcessingException
    */
   @Test
   public void test26_set_product_for_store2_with_userrole() throws JsonMappingException, JsonProcessingException {
        userAuth = HttpAuthenticationFeature.basic(DEFAULT_USER_PREFIX+"2", DEFAULT_USER_PASSWORD);
        ProductPojo product=new ProductPojo();
        product.setDescription("iphone");
        product.setSerialNo("No.24");
        Set <ProductPojo> p=new HashSet<ProductPojo>();
        p.add(product);

        Response response = webTarget
            .register(userAuth)
            .path(STORE_RESOURCE_NAME+"/2")
            .request()
            .put(Entity.entity(p, MediaType.APPLICATION_JSON));
        assertThat(response.getStatus(), is(403));
    }
    
   /**
    * get all products by admin
    * @throws JsonMappingException
    * @throws JsonProcessingException
    */
    @Test
    public void test27_get_all_products_with_adminrole() throws JsonMappingException, JsonProcessingException {
        Response response = webTarget
            .register(adminAuth)
            .path(PRODUCT_RESOURCE_NAME)
            .request()
            .get();
        assertThat(response.getStatus(), is(200));
        List<ProductPojo> products = response.readEntity(new GenericType<List<ProductPojo>>(){});
        assertThat(products, is(not(empty())));
        assertThat(products, hasSize(2));
    }
    
    /**
     * get all products by user
     * @throws JsonMappingException
     * @throws JsonProcessingException
     */
    @Test
    public void test28_get_all_products_with_userrole() throws JsonMappingException, JsonProcessingException {
        userAuth = HttpAuthenticationFeature.basic(DEFAULT_USER_PREFIX+"2", DEFAULT_USER_PASSWORD);
        Response response = webTarget
            .register(userAuth)
            .path(STORE_RESOURCE_NAME)
            .request()
            .get();
        assertThat(response.getStatus(), is(403));
    }
    
    /**
     * create new order by admin
     * @throws JsonMappingException
     * @throws JsonProcessingException
     */
    @Test
    public void test29_add_order_with_adminrole() throws JsonMappingException, JsonProcessingException  {
        OrderPojo or= new OrderPojo();
        or.setDescription("order laptop");
        Response response = webTarget
            .register(adminAuth)
            .path(CUSTOMER_RESOURCE_NAME+SLASH+ORDER_RESOURCE_NAME)
            .request()
            .post(Entity.entity(or, MediaType.APPLICATION_JSON));
        assertThat(response.getStatus(), is(200));
    }
    
    /**
     * update an existing order with owning customer by admin
     * @throws JsonMappingException
     * @throws JsonProcessingException
     */
    @Test
    public void test30_set_customer_for_order_with_adminrole() throws JsonMappingException, JsonProcessingException {
        CustomerPojo cust= new CustomerPojo();
        cust.setFirstName("test fistname c");
        cust.setLastName("test lastname c");
        cust.setEmail("testc@gmail.com");
        cust.setPhoneNumber("333-333-3333");
        Response response = webTarget
            .register(adminAuth)
            .path(CUSTOMER_RESOURCE_NAME+SLASH+ORDER_RESOURCE_NAME+ SLASH+ "1")
            .request()
            .put(Entity.entity(cust, MediaType.APPLICATION_JSON));
        assertThat(response.getStatus(), is(200));
    }
    
    /**
     * add ordeline for an existing order by admin
     * @throws JsonMappingException
     * @throws JsonProcessingException
     */
    @Test
    public void test31_set_orderline_for_order_adminrole() throws JsonMappingException, JsonProcessingException{
        OrderPojo o = new OrderPojo();
        o.setId(1);
        o.setDescription("ice cream");
        ProductPojo p1 = new ProductPojo();
        p1.setDescription("product description 1");
        p1.setSerialNo("serial number 1");
        ProductPojo p2 = new ProductPojo();
        p2.setDescription("product description 2");
        p2.setSerialNo("serial number 2");
        ProductPojo p3 = new ProductPojo();
        p2.setDescription("product description 3");
        p2.setSerialNo("serial number 3");
                
        OrderLinePojo ol1 = new OrderLinePojo();
        ol1.setAmount(100.00);
        ol1.setOwningOrder(o);
        ol1.setProduct(p1);
        OrderLinePk pk1 = new OrderLinePk();
        pk1.setOrderLineNo(1);
        pk1.setOwningOrderId(o.getId());
        ol1.setPk(pk1);
        
        OrderLinePojo ol2 = new OrderLinePojo();
        ol2.setAmount(200.00);
        ol2.setOwningOrder(o);
        ol2.setProduct(p2);
        OrderLinePk pk2 = new OrderLinePk();
        pk2.setOrderLineNo(2);
        pk2.setOwningOrderId(o.getId());
        ol2.setPk(pk2);
        
        OrderLinePojo ol3= new OrderLinePojo();
        ol3.setAmount(300.00);
        ol3.setOwningOrder(o);
        ol3.setProduct(p3);
        OrderLinePk pk3 = new OrderLinePk();
        pk2.setOrderLineNo(3);
        pk2.setOwningOrderId(o.getId());
        ol3.setPk(pk3);
        
        List<OrderLinePojo> orderlines = new ArrayList<>();
        orderlines.add(ol1);
        orderlines.add(ol2);
        orderlines.add(ol3);
        o.setOrderlines(orderlines);
              
        Response response = webTarget
            .register(adminAuth)
            .path(CUSTOMER_RESOURCE_NAME+SLASH+ ORDER_RESOURCE_NAME + SLASH + "1" + SLASH + "orderline")
            .request()
            .post(Entity.entity(orderlines, MediaType.APPLICATION_JSON));
        assertThat(response.getStatus(), is(200));
    }

    /**
     * get all orders for a specific customer by admin
     * @throws JsonMappingException
     * @throws JsonProcessingException
     */
    @Test
    public void test32_get_orders_for_customer_with_adminrole() throws JsonMappingException, JsonProcessingException {
        Response response = webTarget
            .register(adminAuth)
            .path(CUSTOMER_RESOURCE_NAME+ "/3/"+ ORDER_RESOURCE_NAME)
            .request()
            .get();
        assertThat(response.getStatus(), is(200));
    }
    
    /**
     * get all products for a specific customer by admin
     * @throws JsonMappingException
     * @throws JsonProcessingException
     */
    @Test
    public void test33_get_products_for_customer_with_adminrole() throws JsonMappingException, JsonProcessingException{
        Response response = webTarget
            .register(adminAuth)
            .path(CUSTOMER_RESOURCE_NAME+ "/1/"+ PRODUCT_RESOURCE_NAME)
            .request()
            .get();
        assertThat(response.getStatus(), is(200));
    }
    
    /**
     * get a specific order by admin
     * @throws JsonMappingException
     * @throws JsonProcessingException
     */
    @Test
    public void test34_get_order1_by_id_with_adminrole() throws JsonMappingException, JsonProcessingException  {
        Response response = webTarget
            .register(adminAuth)
            .path(CUSTOMER_RESOURCE_NAME+SLASH+ORDER_RESOURCE_NAME+"/1")
            .request()
            .get();
        assertThat(response.getStatus(), is(200));
    }
    
    /**
     * find a specific ordry by admin
     * @throws JsonMappingException
     * @throws JsonProcessingException
     */
    @Test
    public void test35_find_order1_type_with_adminrole() throws JsonMappingException, JsonProcessingException {
        Response response = webTarget
            .register(adminAuth)
            .path(CUSTOMER_RESOURCE_NAME+SLASH+ORDER_RESOURCE_NAME+"/1")
            .request()
            .get();
        assertThat(response.getMediaType(),is(not(MediaType.APPLICATION_XML)));
    }
    
    /**
     * create a new order by admin
     * @throws JsonMappingException
     * @throws JsonProcessingException
     */
    @Test
    public void test36_add_another_order_with_adminrole() throws JsonMappingException, JsonProcessingException  {
        OrderPojo or= new OrderPojo();
        or.setDescription("order headphone");
        Response response = webTarget
            .register(adminAuth)
            .path(CUSTOMER_RESOURCE_NAME+SLASH+ORDER_RESOURCE_NAME)
            .request()
            .post(Entity.entity(or, MediaType.APPLICATION_JSON));
        assertThat(response.getStatus(), is(200));
    }
    
    /**
     * add a new order for an existing customer by admin
     * @throws JsonMappingException
     * @throws JsonProcessingException
     */
    @Test
    public void test37_set_customer_for_another_order_with_adminrole() throws JsonMappingException, JsonProcessingException {
        CustomerPojo cust= new CustomerPojo();
        cust.setFirstName("test fistname d");
        cust.setLastName("test lastname d");
        cust.setEmail("testd@gmail.com");
        cust.setPhoneNumber("444-444-4444");
        Response response = webTarget
            .register(adminAuth)
            .path(CUSTOMER_RESOURCE_NAME+SLASH+ORDER_RESOURCE_NAME+ SLASH+ "2")
            .request()
            .put(Entity.entity(cust, MediaType.APPLICATION_JSON));
        assertThat(response.getStatus(), is(200));
    }
    
    /**
     * add orderline for an existing order by admin
     * @throws JsonMappingException
     * @throws JsonProcessingException
     */
    @Test
    public void test38_set_orderline_for_another_order_adminrole() throws JsonMappingException, JsonProcessingException{
        OrderPojo o = new OrderPojo();
        o.setId(2);
        o.setDescription("chicken");
        ProductPojo p1 = new ProductPojo();
        p1.setDescription("product description a");
        p1.setSerialNo("serial number a");
        ProductPojo p2 = new ProductPojo();
        p2.setDescription("product description b");
        p2.setSerialNo("serial number b");
        ProductPojo p3 = new ProductPojo();
        p2.setDescription("product description c");
        p2.setSerialNo("serial number c");
                
        OrderLinePojo ol1 = new OrderLinePojo();
        ol1.setAmount(400.00);
        ol1.setOwningOrder(o);
        ol1.setProduct(p1);
        OrderLinePk pk1 = new OrderLinePk();
        pk1.setOrderLineNo(1);
        pk1.setOwningOrderId(o.getId());
        ol1.setPk(pk1);
        
        OrderLinePojo ol2 = new OrderLinePojo();
        ol2.setAmount(500.00);
        ol2.setOwningOrder(o);
        ol2.setProduct(p2);
        OrderLinePk pk2 = new OrderLinePk();
        pk2.setOrderLineNo(2);
        pk2.setOwningOrderId(o.getId());
        ol2.setPk(pk2);
        
        OrderLinePojo ol3= new OrderLinePojo();
        ol3.setAmount(600.00);
        ol3.setOwningOrder(o);
        ol3.setProduct(p3);
        OrderLinePk pk3 = new OrderLinePk();
        pk2.setOrderLineNo(3);
        pk2.setOwningOrderId(o.getId());
        ol3.setPk(pk3);
        
        List<OrderLinePojo> orderlines = new ArrayList<>();
        orderlines.add(ol1);
        orderlines.add(ol2);
        orderlines.add(ol3);
        o.setOrderlines(orderlines);
              
        Response response = webTarget
            .register(adminAuth)
            .path(CUSTOMER_RESOURCE_NAME+SLASH+ ORDER_RESOURCE_NAME + SLASH + "2" + SLASH + "orderline")
            .request()
            .post(Entity.entity(orderlines, MediaType.APPLICATION_JSON));
        assertThat(response.getStatus(), is(200));
    }

    /**
     * get all oreders by admin
     * @throws JsonMappingException
     * @throws JsonProcessingException
     */
    @Test
    public void test39_get_all_orders_with_adminrole() throws JsonMappingException, JsonProcessingException  {
        Response response = webTarget
            .register(adminAuth)
            .path(CUSTOMER_RESOURCE_NAME+SLASH+ORDER_RESOURCE_NAME)
            .request()
            .get();
        assertThat(response.getStatus(), is(200));
        List<OrderPojo> orders= response.readEntity(new GenericType<List<OrderPojo>>(){});
        assertThat(orders, is(not(empty())));
        assertThat(orders, hasSize(2));
    }
    
    /**
     * get all orders by user
     * @throws JsonMappingException
     * @throws JsonProcessingException
     */
    @Test
    public void test40_get_all_orders_with_userrole() throws JsonMappingException, JsonProcessingException  {
        Response response = webTarget
            .register(userAuth)
            .path(CUSTOMER_RESOURCE_NAME+SLASH+ORDER_RESOURCE_NAME)
            .request()
            .get();
        assertThat(response.getStatus(), is(403));
    }
    
    /**
     * delete a customer by admin
     * @throws JsonMappingException
     * @throws JsonProcessingException
     */
    @Test
    public void test41_delete_customer_by_adminrole()throws JsonMappingException, JsonProcessingException {
        Response response = webTarget
            .register(adminAuth)
            .path(CUSTOMER_RESOURCE_NAME+"/1")
            .request()
            .delete();
        assertThat(response.getStatus(), is(204));
    }
    
}