package com.ifast.batch.partitioner;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.core.io.Resource;

public class CustomMultiResourcePartitioner implements Partitioner {
	
	private Resource resource;
	  
	@Override
    public Map<String, ExecutionContext> partition(int gridSize) {
        Map<String, ExecutionContext> map = new HashMap<>(gridSize);
       
        try(
        		final InputStream in = resource.getInputStream();
        		final BufferedReader br = new BufferedReader(new InputStreamReader(in));) {
        	String line = "";
        	while ((line = br.readLine()) != null) {
        		
        	}
        } catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        return map;
    }

	public Resource getResource() {
		return resource;
	}

	public void setResource(Resource resource) {
		this.resource = resource;
	}
	
	private class ByteStreamCursor {
        private long totalLineCount = 0;
        private long lineCount = 0;
        private long skipLineCount = 0;
        private long skipBytesCount = 0;
        private byte lastSeenChar = 0;
        private long currentByteInd = 0L;
        private long startAt = 0;
        
		public boolean lastSeenCharIsNewline(byte lastSeenChar) {
			this.lastSeenChar = lastSeenChar;
			this.currentByteInd++;
			if(skipLineCount > 0) {
				skipBytesCount++;
			}
            // New line is \n on Unix and \r\n on Windows                
            if (lastSeenChar == '\n') {
            	startNewLine();
                return true;
            }
            return false;
		}
		
		public void startNewLine() {
			if(skipLineCount > 0) {
				skipLineCount--;
			}
			else {
				lineCount++;
			}
            totalLineCount++;
		}

		public void startNewPartition() {
            startAt = currentByteInd;
            lineCount = 0;
		}

		public long getCurrentByteInd() {
			return currentByteInd;
		}
		
		public boolean outstandingData() {
			return currentByteInd > 0 && startAt != currentByteInd;
		}
    }
}
