package dev.ikm.komet.framework.performance;

/**
 * Performance: the action or process of carrying out or accomplishing an action, task, or function.
 * Making Statements, observations, and requests are actions. Performing a procedure is also consists
 * of carrying out one or more actions.
 */
public interface Performance {

    Topic topic();

    /**
     * @return the subject of this observation.
     */
    Object subject();

}
