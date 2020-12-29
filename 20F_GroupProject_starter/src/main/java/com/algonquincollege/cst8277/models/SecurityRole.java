/*****************************************************************c******************o*******v******id********
 * File: SecurityRole.java
 * Course materials (20F) CST 8277
 *
 * @author (original) Mike Norman
 * 
 * update by : @author Sangeun Baek 040953608 
 *             @author Hsing-I Wang 040953737
 *             @author Qi Wang 040946448
 */
package com.algonquincollege.cst8277.models;

import static com.algonquincollege.cst8277.models.SecurityRole.ROLE_BY_NAME_QUERY;

import java.io.Serializable;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;


/**
 * Role class used for (JSR-375) Java EE Security authorization/authentication
 */
@Entity(name="SecurityRole")
@Table(name="SECURITY_ROLE")
@NamedQuery(name= ROLE_BY_NAME_QUERY, query="select sr from SecurityRole sr where sr.roleName= :param1")
public class SecurityRole implements Serializable {
    /** explicit set serialVersionUID */
    private static final long serialVersionUID = 1L;

    public static final String ROLE_BY_NAME_QUERY = "roleByName";

    protected int id;
    protected String roleName;
    protected Set<SecurityUser> users;

    public SecurityRole() {
        super();
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="ROLE_ID")
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }

    public String getRoleName() {
        return roleName;
    }
    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    @JsonInclude(Include.NON_NULL)
    @ManyToMany(mappedBy="roles")
    public Set<SecurityUser> getUsers() {
        return users;
    }
    public void setUsers(Set<SecurityUser> users) {
        this.users = users;
    }
    public void addUserToRole(SecurityUser user) {
        getUsers().add(user);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + id;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        SecurityRole other = (SecurityRole)obj;
        if (id != other.id) {
            return false;
        }
        return true;
    }
}