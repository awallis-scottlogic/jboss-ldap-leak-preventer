package com.scottlogic.ldapleakpreventer;

import org.jboss.as.controller.AbstractBoottimeAddStepHandler;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.ServiceVerificationHandler;
import org.jboss.dmr.ModelNode;
import org.jboss.logging.Logger;
import org.jboss.msc.service.ServiceController;

import java.util.List;

/**
 * Add handler that uses the performBoottime hook to load the LdapPoolManager class.
 */
class SubsystemAdd extends AbstractBoottimeAddStepHandler {

    static final SubsystemAdd INSTANCE = new SubsystemAdd();

    private final Logger log = Logger.getLogger(SubsystemAdd.class.getPackage().getName());

    private SubsystemAdd() { }

    /** {@inheritDoc} */
    @Override
    protected void populateModel(ModelNode operation, ModelNode model) throws OperationFailedException {
        log.info("Populating the model");
        model.setEmptyObject();
    }

    /** {@inheritDoc} */
    @Override
    public void performBoottime(OperationContext context, ModelNode operation, ModelNode model,
            ServiceVerificationHandler verificationHandler, List<ServiceController<?>> newControllers)
            throws OperationFailedException {

        // The system properties defined in standalone.xml should be available at this point
        String property = System.getProperty(LDAPLeakPreventerExtension.POOL_TIMEOUT_PROPERTY);
        log.info("pool timeout property is configured to " + property + " ms");
        LDAPLeakPreventerExtension.loadLdapPoolManagerClass();
    }
}
