/*
 * Copyright 2020 Software Systems Engineering, University of Hildesheim
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.ssehub.teaching.submission_check.utils;

import java.io.Closeable;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;

/**
 * A helper class for test cases that blocks read-accesses to a file.
 * <p>
 * On Windows, the {@link RandomAccessFile#getChannel()} and {@link FileChannel#lock()} method seems to work reliably.
 * <p>
 * On Unix, we fall back to {@link File#setReadable(boolean)} (which does not work on Windows).
 * 
 * @author Adam
 */
public class FileBlocker implements Closeable {

    private static Boolean useFileBlocker;
    
    private File target;
    
    private RandomAccessFile raFile;
    
    private FileLock fileLock;
    
    public FileBlocker(File target) throws IOException {
        this.target = target;
        if (fileBlockerWorks()) {
            this.raFile = new RandomAccessFile(target, "rwd");
            this.fileLock = raFile.getChannel().lock();
        } else {
            if (!target.setReadable(false) || target.canRead()) {
                throw new IOException("Couldn't block file");
            }
        }
    }
    
    @Override
    public void close() throws IOException {
        if (this.fileLock != null) {
            this.fileLock.release();
            this.raFile.close();
        } else {
            target.setReadable(true);
        }
    }
    
    private static boolean fileBlockerWorks() {
        if (useFileBlocker == null) {
            File tmp = null;
            try  {
                tmp = File.createTempFile("file-block-detector", null);
                
                RandomAccessFile raFile = new RandomAccessFile(tmp, "rw");
                FileLock fileLock = raFile.getChannel().lock();
                
                FileReader reader = null;
                try {
                    reader = new FileReader(tmp);
                    reader.read();
                    useFileBlocker = false;
                } catch (IOException e) {
                    useFileBlocker = true;
                }
                if (reader != null) {
                    reader.close();
                }
                
                fileLock.release();
                raFile.close();
                
            } catch (IOException e) {
                e.printStackTrace();
            }
            
            if (tmp != null) {
                tmp.delete();
            }
            
            System.out.println("fileBlockerWorks() -> " + useFileBlocker);
        }
        return useFileBlocker;
    }
    
}
