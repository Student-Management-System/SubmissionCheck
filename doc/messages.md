This file contains a list of all messages created by checks that are hard-coded strings.
Note that many messages are created dynamically from the tool output (e.g. compilation errors).

<table>
	<tr>
		<th>Check</th>
		<th>Message</th>
		<th>Type</th>
		<th>Description</th>
	</tr>
	<tr>
		<td><code>hook</code></td>
		<td><code>An internal error occurred</code></td>
		<td>error</td>
		<td>An exception occurred while running the hook; the exception is printed to the log file.</td>
	</tr>
	<tr>
		<td><code>file-size</code></td>
		<td><code>File is too large</code></td>
		<td>error</td>
		<td>The given file has a too high file-size.</td>
	</tr>
	<tr>
		<td><code>file-size</code></td>
		<td><code>Submission size is too large</code></td>
		<td>error</td>
		<td>The submission as a whole is has a too high file-size.</td>
	</tr>
	<tr>
		<td><code>file-size</code></td>
		<td><code>An internal error occurred while checking file-sizes</code></td>
		<td>error</td>
		<td>An IO exception occurred while checking file-sizes; the exception is printed to the log file.</td>
	</tr>
	<tr>
		<td><code>encoding</code></td>
		<td><code>File has invalid encoding; expected <i>encoding name</i></code></td>
		<td>error</td>
		<td>The given file has an invalid encoding.</td>
	</tr>
	<tr>
		<td><code>encoding</code></td>
		<td><code>An internal error occurred while checking file encoding</code></td>
		<td>error</td>
		<td>An IO exception occurred while checking all text files for the correct encoding; the exception is printed to the log file.</td>
	</tr>
	<tr>
		<td><code>eclipse-configuration</code></td>
		<td><code>Does not contain a valid eclipse project</code></td>
		<td>error</td>
		<td>Submission does not contain .classpath and .project or these files are malformed.</td>
	</tr>
	<tr>
		<td><code>eclipse-configuration</code></td>
		<td><code>Submission is not a Java project</code></td>
		<td>error</td>
		<td>The submitted project is not a Java project (this check needs to be enabled in the configuration).</td>
	</tr>
	<tr>
		<td><code>eclipse-configuration</code></td>
		<td><code>Submission does not have Checkstyle enabled</code></td>
		<td>warning</td>
		<td>The submitted project does not have Checkstyle enabled (this check needs to be enabled in the configuration).</td>
	</tr>
	<tr>
		<td><code>eclipse-configuration</code></td>
		<td><code>An internal error occurred while checking eclipse project</code></td>
		<td>error</td>
		<td>An IO exception occurred while parsing the eclipse projet files; the exception is printed to the log file.</td>
	</tr>
	<tr>
		<td><code>javac</code></td>
		<td><code>No Java files found</code></td>
		<td>error</td>
		<td>The submission does not contain any Java files.</td>
	</tr>
	<tr>
		<td><code>javac</code></td>
		<td><code>javac failed without message</code></td>
		<td>error</td>
		<td>The javac process exited with a non-success exit code, but no error message could be created from the output.</td>
	</tr>
	<tr>
		<td><code>javac</code></td>
		<td><code>An internal error occurred while running javac</code></td>
		<td>error</td>
		<td>An exception occurred while running javac; the exception is printed to the log file.</td>
	</tr>
	<tr>
		<td><code>checkstyle</code></td>
		<td><code>An internal error occurred while running checkstyle</code></td>
		<td>error</td>
		<td>An exception occurred while running checkstyle; the exception is printed to the log file.</td>
	</tr>
</table>
