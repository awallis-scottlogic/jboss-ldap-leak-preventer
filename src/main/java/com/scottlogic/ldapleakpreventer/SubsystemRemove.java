package com.scottlogic.ldapleakpreventer;

import org.jboss.as.controller.AbstractRemoveStepHandler;

/**
 * Empty remove handler implementation.
 */
class SubsystemRemove extends AbstractRemoveStepHandler {

    static final SubsystemRemove INSTANCE = new SubsystemRemove();

    private SubsystemRemove() { }

}
