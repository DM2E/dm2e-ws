package eu.dm2e.ws.constants;


/**
 * Possible states of a Job. 
 * NOT_STARTED: The job was created but not started
 * STARTED: The job was started
 * WAITING: The job was started but is currently idle
 * FINISHED: The job finished successfully
 * FAILED: The job failed
 * @author kb
 *
 */
public enum JobStatus {
	NOT_STARTED,
    STARTED,
    ITERATING,
    WAITING,
	FINISHED,
	FAILED
}
