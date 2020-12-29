/*****************************************************************c******************o*******v******id********
 * File: OrderLinePk.java
 * Course materials (20F) CST 8277
 *
 * @author (original) Mike Norman
 * 
 * update by : @author Sangeun Baek 040953608 
 *             @author Hsing-I Wang 040953737
 *             @author Qi Wang 040946448
 */
package com.algonquincollege.cst8277.models;

import java.io.Serializable;
import java.util.Objects;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
@Access(AccessType.PROPERTY)
public class OrderLinePk implements Serializable {
    /** explicit set serialVersionUID */
    private static final long serialVersionUID = 1L;

    protected int owningOrderId;
    protected int orderLineNo;

    @Column(name="OWNING_ORDERLINE_NO")
    public int getOwningOrderId() {
        return owningOrderId;
    }
    public void setOwningOrderId(int owningOrderId) {
        this.owningOrderId = owningOrderId;
    }

    @Column(name="ORDERLINE_NO")
    public int getOrderLineNo() {
        return orderLineNo;
    }
    public void setOrderLineNo(int orderLineNo) {
        this.orderLineNo = orderLineNo;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(orderLineNo, owningOrderId);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof OrderLinePk)) {
            return false;
        }
        OrderLinePk other = (OrderLinePk) obj;
        return orderLineNo == other.orderLineNo && owningOrderId == other.owningOrderId;
    }

}