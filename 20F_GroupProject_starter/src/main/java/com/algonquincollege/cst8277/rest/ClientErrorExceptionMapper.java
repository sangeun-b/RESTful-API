/*****************************************************************c******************o*******v******id********
 * File: ClientErrorExceptionMapper.java
 * Course materials (20F) CST 8277
 * @author Mike Norman
 * @author Sangeun Baek 040953608 
 * @author Hsing-I Wang 040953737
 * @author Qi Wang 040946448
 * 
 * Note: students do NOT need to change anything in this class
 */
package com.algonquincollege.cst8277.rest;

import javax.ws.rs.ClientErrorException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class ClientErrorExceptionMapper implements ExceptionMapper<ClientErrorException> {
    
    @Override
    public Response toResponse(ClientErrorException exception) {
      Response response = exception.getResponse();
      Response.StatusType statusType = response.getStatusInfo();
      int statusCode = statusType.getStatusCode();
      String reasonPhrase = statusType.getReasonPhrase();
      HttpErrorResponse entity = new HttpErrorResponse(statusCode, reasonPhrase);
      return Response.status(statusCode).entity(entity).build();
    }
}