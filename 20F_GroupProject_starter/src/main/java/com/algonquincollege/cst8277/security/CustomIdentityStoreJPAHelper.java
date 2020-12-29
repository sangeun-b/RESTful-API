/*****************************************************************c******************o*******v******id********
 * File: CustomIdentityStoreJPAHelper.java
 * Course materials (20F) CST 8277
 *
 * @author (original) Mike Norman
 * 
 * update by : @author Sangeun Baek 040953608 
 *             @author Hsing-I Wang 040953737
 *             @author Qi Wang 040946448
 */
package com.algonquincollege.cst8277.security;

import static com.algonquincollege.cst8277.models.SecurityUser.SECURITY_USER_BY_NAME_QUERY;
import static com.algonquincollege.cst8277.utils.MyConstants.PARAM1;
import static java.util.Collections.emptySet;

import java.util.Set;
import java.util.stream.Collectors;

import javax.ejb.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;

import com.algonquincollege.cst8277.models.SecurityRole;
import com.algonquincollege.cst8277.models.SecurityUser;

/*
 * Stateless Session bean should also be a Singleton
 */
@Singleton
public class CustomIdentityStoreJPAHelper {
    
    public static final String CUSTOMER_PU = "20f-groupProject-PU";

    @PersistenceContext(name=CUSTOMER_PU)
    protected EntityManager em;
    
    
    /**
     * find user by user name
     * @param username
     * @return a specific user
     */
    public SecurityUser findUserByName(String username) {
        SecurityUser user = null;
        try {
            TypedQuery<SecurityUser> findUser = em.createNamedQuery(SECURITY_USER_BY_NAME_QUERY, SecurityUser.class).setParameter(PARAM1, username);
            user = findUser.getSingleResult();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return user;
    }

    /**
     * find role name for a user
     * @param username
     * @return the role name for a user
     */
    public Set<String> findRoleNamesForUser(String username) {
        Set<String> roleNames = emptySet();
        SecurityUser securityUser = findUserByName(username);
        if (securityUser != null) {
            roleNames = securityUser.getRoles().stream().map(s -> s.getRoleName()).collect(Collectors.toSet());
        }
        return roleNames;
    }

    @Transactional
    public void saveSecurityUser(SecurityUser user) {
        em.persist(user);
    }

    @Transactional
    public void saveSecurityRole(SecurityRole role) {
        em.persist(role);
    }
}