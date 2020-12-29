/***************************************************************************f******************u************zz*******y**
 * File: Assignment3Driver.java
 * Course materials (20W) CST 8277
 * @author Mike Norman
 * @author Sangeun Baek 040953608 
 * @author Hsing-I Wang 040953737
 * @author Qi Wang 040946448
 * @date 2020 04
 *
 */
package com.algonquincollege.cst8277;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

public class DriverTestForJPAErrors {

    public static final String PU_NAME = "test-for-JPA-errors-PU";

    public static void main(String[] args) {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory(PU_NAME);
        emf.close();
    }

}
