/*****************************************************************c******************o*******v******id********
 * File: CustomerService.java
 * Course materials (20F) CST 8277
 *
 * @author (original) Mike Norman
 * 
 * update by : @author Sangeun Baek 040953608 
 *             @author Hsing-I Wang 040953737
 *             @author Qi Wang 040946448
 *
 */
package com.algonquincollege.cst8277.ejb;

import static com.algonquincollege.cst8277.models.SecurityRole.ROLE_BY_NAME_QUERY;
import static com.algonquincollege.cst8277.utils.MyConstants.DEFAULT_KEY_SIZE;
import static com.algonquincollege.cst8277.utils.MyConstants.DEFAULT_PROPERTY_ALGORITHM;
import static com.algonquincollege.cst8277.utils.MyConstants.DEFAULT_PROPERTY_ITERATIONS;
import static com.algonquincollege.cst8277.utils.MyConstants.DEFAULT_SALT_SIZE;
import static com.algonquincollege.cst8277.utils.MyConstants.DEFAULT_USER_PASSWORD;
import static com.algonquincollege.cst8277.utils.MyConstants.DEFAULT_USER_PREFIX;
import static com.algonquincollege.cst8277.utils.MyConstants.PARAM1;
import static com.algonquincollege.cst8277.utils.MyConstants.PROPERTY_ALGORITHM;
import static com.algonquincollege.cst8277.utils.MyConstants.PROPERTY_ITERATIONS;
import static com.algonquincollege.cst8277.utils.MyConstants.PROPERTY_KEYSIZE;
import static com.algonquincollege.cst8277.utils.MyConstants.PROPERTY_SALTSIZE;
import static com.algonquincollege.cst8277.utils.MyConstants.USER_ROLE;

import static com.algonquincollege.cst8277.models.CustomerPojo.ALL_CUSTOMERS_QUERY_NAME;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ejb.Singleton;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
//import javax.transaction.Transactional;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.security.enterprise.identitystore.Pbkdf2PasswordHash;
import javax.transaction.Transactional;

import com.algonquincollege.cst8277.models.AddressPojo;
import com.algonquincollege.cst8277.models.CustomerPojo;
import com.algonquincollege.cst8277.models.OrderLinePojo;
import com.algonquincollege.cst8277.models.OrderPojo;
import com.algonquincollege.cst8277.models.ProductPojo;
import com.algonquincollege.cst8277.models.SecurityRole;
import com.algonquincollege.cst8277.models.SecurityUser;
import com.algonquincollege.cst8277.models.ShippingAddressPojo;
import com.algonquincollege.cst8277.models.StorePojo;

/**
 * Stateless Singleton Session Bean - CustomerService
 */
@Singleton
public class CustomerService implements Serializable {
    private static final long serialVersionUID = 1L;
    
    public static final String CUSTOMER_PU = "20f-groupProject-PU";

    @PersistenceContext(name = CUSTOMER_PU)
    protected EntityManager em;

    @Inject
    protected Pbkdf2PasswordHash pbAndjPasswordHash;
    
    //TODO

    /**
     * 
     * @return get all customers
     */
    public List<CustomerPojo> getAllCustomers() {
        return em.createNamedQuery(ALL_CUSTOMERS_QUERY_NAME, CustomerPojo.class).getResultList();
    }

    /**
     * 
     * @param custPK customer id
     * @return a specific customer by id
     */
    public CustomerPojo getCustomerById(int custPK) {
        return em.find(CustomerPojo.class, custPK);
    }
    
    @Transactional
    public CustomerPojo persistCustomer(CustomerPojo newCustomer) {
        em.persist(newCustomer);
        return newCustomer;
    }
    
    @Transactional
    public void buildUserForNewCustomer(CustomerPojo newCustomerWithIdTimestamps) {
        SecurityUser userForNewCustomer = new SecurityUser();
        userForNewCustomer.setUsername(DEFAULT_USER_PREFIX + "" + newCustomerWithIdTimestamps.getId());
        Map<String, String> pbAndjProperties = new HashMap<>();
        pbAndjProperties.put(PROPERTY_ALGORITHM, DEFAULT_PROPERTY_ALGORITHM);
        pbAndjProperties.put(PROPERTY_ITERATIONS, DEFAULT_PROPERTY_ITERATIONS);
        pbAndjProperties.put(PROPERTY_SALTSIZE, DEFAULT_SALT_SIZE);
        pbAndjProperties.put(PROPERTY_KEYSIZE, DEFAULT_KEY_SIZE);
        pbAndjPasswordHash.initialize(pbAndjProperties);
        String pwHash = pbAndjPasswordHash.generate(DEFAULT_USER_PASSWORD.toCharArray());
        userForNewCustomer.setPwHash(pwHash);
        userForNewCustomer.setCustomer(newCustomerWithIdTimestamps);
        SecurityRole userRole = em.createNamedQuery(ROLE_BY_NAME_QUERY,
            SecurityRole.class).setParameter(PARAM1, USER_ROLE).getSingleResult();
        userForNewCustomer.getRoles().add(userRole);
        userRole.getUsers().add(userForNewCustomer);
        em.persist(userForNewCustomer);
    }

    /**
     * set address for an existing customer
     * @param custId
     * @param newAddress
     * @return updated customer with new address
     */
    @Transactional
    public CustomerPojo setAddressFor(int custId, AddressPojo newAddress) {
        CustomerPojo updatedCustomer = em.find(CustomerPojo.class, custId);
        if (newAddress instanceof ShippingAddressPojo) {
            updatedCustomer.setShippingAddress(newAddress);
        }
        else {
            updatedCustomer.setBillingAddress(newAddress);
        }
        em.merge(newAddress);
        em.merge(updatedCustomer);
        return updatedCustomer;
    }
    
    /**
     * delete a customer by id
     * @param customerId
     */
    @Transactional
    public void deleteCustomerById(int customerId) {
        CustomerPojo customer = em.find(CustomerPojo.class, customerId);
        String i = Integer.toString(customer.getId());
        SecurityUser u1 = em.createQuery("select s from SecurityUser s where s.username = :param1",
            SecurityUser.class).setParameter(PARAM1,DEFAULT_USER_PREFIX + "" + i).getSingleResult();
        if (customer != null) {
            em.remove(u1);
            em.refresh(customer);
            em.remove(customer);
        }
    }

    /**
     * 
     * @return all products
     */
    public List<ProductPojo> getAllProducts() {
        //example of using JPA Criteria query instead of JPQL
        try {
            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery<ProductPojo> q = cb.createQuery(ProductPojo.class);
            Root<ProductPojo> c = q.from(ProductPojo.class);
            q.select(c);
            TypedQuery<ProductPojo> q2 = em.createQuery(q);
            List<ProductPojo> allProducts = q2.getResultList();
            return allProducts;
        }
        catch (Exception e) {
            return null;
        }
    }

    /**
     * 
     * @param prodId
     * @return a specific product by its id
     */
    public ProductPojo getProductById(int prodId) {
        return em.find(ProductPojo.class, prodId);
    }

    /**
     * 
     * @return all stores
     */
    public List<StorePojo> getAllStores() {
        try {
            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery<StorePojo> q = cb.createQuery(StorePojo.class);
            Root<StorePojo> c = q.from(StorePojo.class);
            q.select(c);
            TypedQuery<StorePojo> q2 = em.createQuery(q);
            List<StorePojo> allStores = q2.getResultList();
            return allStores;
        }
        catch (Exception e) {
            return null;
        }
    }

    /**
     * 
     * @param id
     * @return a specific store by its id
     */
    public StorePojo getStoreById(int id) {
        return em.find(StorePojo.class, id);
    }
    
    /*
    public OrderPojo getAllOrders ... getOrderbyId ... build Orders with OrderLines ...
    */
    
    /**
     * 
     * @return all orders
     */
    public List<OrderPojo> getAllOrders() {
        try {
            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery<OrderPojo> q = cb.createQuery(OrderPojo.class);
            Root<OrderPojo> c = q.from(OrderPojo.class);
            q.select(c);
            TypedQuery<OrderPojo> q2 = em.createQuery(q);
            List<OrderPojo> allOrders = q2.getResultList();
            return allOrders;
        }
        catch (Exception e) {
            return null;
        }
           
    }
    
    /**
     * @param id
     * @return a specific order by its id
     */
    public OrderPojo getOrderById(int id) {
        return em.find(OrderPojo.class, id);
    }
    
    @Transactional
    public OrderPojo persistOrder(OrderPojo newOrder) {
        em.persist(newOrder);
        return newOrder;
    }
    
  /**
   * 
   * @param id
   * @return all orders
   */
    public List<OrderPojo> getAllOrders(int id){
       TypedQuery<OrderPojo> orders= em.createQuery("select o from Order o join o.owningCustomer oc where oc.id = :param1", OrderPojo.class)
           .setParameter(PARAM1, id);
       return orders.getResultList();
    }
      
    @Transactional
    public StorePojo persistStore(StorePojo newStore) {
        em.persist(newStore);
        return newStore;
    }

    /**
     * set product for an existing store
     * @param id
     * @param newProduct
     * @return an updated store with new product
     */
    @Transactional
    public StorePojo setProductFor(int id, Set<ProductPojo> newProduct) {
        StorePojo updatedStore = em.find(StorePojo.class, id);
        Set<StorePojo> stores= new HashSet<>();
        stores.add(updatedStore);
        updatedStore.setProducts(newProduct);
        for(ProductPojo p: newProduct) {
            p.setStores(stores);
            em.merge(p);
        }
        em.merge(updatedStore);
        return updatedStore;
    }

    @Transactional
    public ProductPojo persistProduct(ProductPojo newProduct) {
        em.persist(newProduct);
        return newProduct;
    }

    /**
     * set store for an existing product
     * @param id
     * @param newStore
     * @return an updated product with new store
     */
    @Transactional
    public ProductPojo setStoreFor(int id, HashSet<StorePojo> newStore) {
        ProductPojo updatedProduct = em.find(ProductPojo.class, id);
        Set<ProductPojo> products= new HashSet<>();
        products.add(updatedProduct);
        updatedProduct.setStores(newStore);
        for(StorePojo s: newStore) {
            s.setProducts(products);
            em.merge(s);
        }
        em.merge(updatedProduct);
        return updatedProduct;
    }

    /**
     * delete a product by its id
     * @param id
     */
    @Transactional
    public void deleteProductById(int id) {
        ProductPojo product = em.find(ProductPojo.class, id);
        if (product != null) {
            em.refresh(product);
            em.remove(product);
        }
    }

    /**
     * delete a store by its id
     * @param id
     */
    @Transactional
    public void deleteStoreById(int id) {
        StorePojo store = em.find(StorePojo.class, id);
        if (store != null) {
            em.refresh(store);
            em.remove(store);
        }
    }

    /**
     * get all products
     * @param id
     * @return all products
     */
    public List<ProductPojo> getAllProducts(int id) {
        TypedQuery<OrderLinePojo> q = em.createQuery("select ol from OrderLine ol join ol.owningOrder oo where oo.id = :param1", OrderLinePojo.class)
            .setParameter(PARAM1, id);
        List<OrderLinePojo> orderlines= q.getResultList();
        List<ProductPojo> products= new ArrayList<>();
        for(OrderLinePojo ol: orderlines) {
            products.add(ol.getProduct());
        }
        return products;
    }

    /**
     * create order with owning customer
     * @param id
     * @param cust
     * @return the new order
     */
    @Transactional
    public OrderPojo persistCustomerForOrder(int id, CustomerPojo cust) {
        OrderPojo order = em.find(OrderPojo.class, id);
        order.setOwningCustomer(cust);
        em.merge(cust);
        em.merge(order);
        return order;
    }
    
    /**
     * set orderLine for an existing order
     * @param orderId
     * @param ol
     * @return an updated order with new orderline
     */
    @Transactional
    public OrderPojo setOrderLineFor(int orderId, List<OrderLinePojo> ol) {
        OrderPojo updatedOrder = em.find(OrderPojo.class, orderId);
        for(OrderLinePojo olp: ol) {
            olp.setOwningOrder(updatedOrder);
        }
        updatedOrder.setOrderlines(ol);
        em.merge(updatedOrder);
        return updatedOrder;
    }
 
}