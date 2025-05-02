package org.killbill.billing.plugin.adyen.api;

import lombok.Getter;
import lombok.Setter;

/**
 * Data Transfer Object for Adyen session output results.
 * Contains the response information from an Adyen session status check.
 */
@Getter
@Setter
public class SessionOutputDTO {

    /**
     * The ID of the Adyen payment session
     */
    private String sessionId;
    
    /**
     * The status of the session (e.g., "completed", "pending", "failed")
     */
    private String sessionStatus;
}
