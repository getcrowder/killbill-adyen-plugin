package org.killbill.billing.plugin.adyen.api;

import lombok.Getter;
import lombok.Setter;

/**
 * Data Transfer Object for Adyen session input parameters.
 * Used to pass session information to retrieve results from Adyen.
 */
@Getter
@Setter
public class SessionInputDTO {

    /**
     * The ID of the Adyen payment session
     */
    private String sessionId;
    
    /**
     * The session result data provided by Adyen
     */
    private String sessionResult;
}
