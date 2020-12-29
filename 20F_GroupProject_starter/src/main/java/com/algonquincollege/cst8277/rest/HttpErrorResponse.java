/*****************************************************************c******************o*******v******id********
 * File: ErrorResponse.java
 * Course materials (20F) CST 8277
 *
 * @author (original) Mike Norman
 * @author Sangeun Baek 040953608 
 * @author Hsing-I Wang 040953737
 * @author Qi Wang 040946448
 * 
 * Note: students do NOT need to change anything in this class
 */
package com.algonquincollege.cst8277.rest;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;

public class HttpErrorResponse implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private final int statusCode;
    private final String reasonPhrase;

    public HttpErrorResponse(int code, String reasonPhrase) {
        this.statusCode = code;
        this.reasonPhrase = reasonPhrase;
    }
    
    @JsonProperty("status-code")
    public int getStatusCode() {
        return statusCode;
    }

    @JsonProperty("reason-phrase")
    public String getReasonPhrase() {
        return reasonPhrase;
    }

}