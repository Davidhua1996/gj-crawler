package com.gj.web.crawler.pool.exc;

import java.io.IOException;
import java.net.ProtocolException;
import java.net.SocketTimeoutException;

/**
 * <p>analysis first, then store</p>
 * @author David
 *
 */
public abstract class ExcReportAnalysisStore implements ExcReportStore{

	public void add(ExcReport report) {
		Exception ex = report.getExcp();
		String reason = null;
		if(ex instanceof SocketTimeoutException){
			reason = ExcReasonConstraints.SOCKET_TIMEOUT;
		}else if(ex instanceof ProtocolException){
			reason = ExcReasonConstraints.PROTOCOL_ERROR;
		}else if(ex instanceof IllegalStateException){
			reason = ExcReasonConstraints.ILLEGAL_STATE;
		}else if(ex instanceof NumberFormatException){
			reason = ExcReasonConstraints.RESP_CODE_ERROR;
		}else if(ex instanceof IOException){
			reason = ExcReasonConstraints.IO_ERROR;
		}else{
			reason = ExcReasonConstraints.OTHER_ERROR;
		}
		report.setReason(reason);
	}
	
}
