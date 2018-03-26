package com.ifast.batch.listener;

import org.springframework.batch.core.job.AbstractJob;
import org.springframework.batch.core.step.AbstractStep;

public class ListenerService {
	
	private boolean addProtocolListener;
	private boolean addLoggingListener;
	private ProtocolListener protocolListener;
	private RunningExecutionTrackerListener runningExecutionTrackerListener;
	private LoggingListener loggingListener;
	private LoggingAfterJobListener loggingAfterJobListener;

	public ListenerService(boolean addProtocolListener,
			boolean addLoggingListener, ProtocolListener protocolListener,
			RunningExecutionTrackerListener runningExecutionTrackerListener,
			LoggingListener loggingListener,
			LoggingAfterJobListener loggingAfterJobListener) {
		super();
		this.addProtocolListener = addProtocolListener;
		this.addLoggingListener = addLoggingListener;
		this.protocolListener = protocolListener;
		this.runningExecutionTrackerListener = runningExecutionTrackerListener;
		this.loggingListener = loggingListener;
		this.loggingAfterJobListener = loggingAfterJobListener;
	}

	public void addListenerToJob(AbstractJob job){
		if (addProtocolListener){
			job.registerJobExecutionListener(protocolListener);
		}
		job.registerJobExecutionListener(runningExecutionTrackerListener);
		if (addLoggingListener){
			job.registerJobExecutionListener(loggingListener);
			job.registerJobExecutionListener(loggingAfterJobListener);
			for (String stepName: job.getStepNames()){
				AbstractStep step = (AbstractStep)job.getStep(stepName);
				step.registerStepExecutionListener(loggingListener);
			}
		}
	}

}
