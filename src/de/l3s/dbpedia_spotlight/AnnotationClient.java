/**
 * Copyright 2011 Pablo Mendes, Max Jakob
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.l3s.dbpedia_spotlight;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.params.HttpMethodParams;

/**
 * This class has been translate to scala. Please use the
 * AnnotationClientScala.scala for new External Clients!
 * (AnnotationClientScala.scala is at
 * eval/src/main/scala/org/dbpedia/spotlight/evaluation/external/)
 *
 * @author pablomendes
 */

public abstract class AnnotationClient {

	// public Logger LOG = Logger.getLogger(this.getClass());

	// Create an instance of HttpClient.
	private static HttpClient client;

	public String request(HttpMethod method) throws MyAnnotationException {

		if (client == null) {
			MultiThreadedHttpConnectionManager connectionManager = new MultiThreadedHttpConnectionManager();
			client = new HttpClient(connectionManager);
		}

		String response = null;

		// Provide custom retry handler is necessary
		method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(3, false));

		try {
			// System.out.println(method.getURI());
			method.getURI();
		} catch (URIException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		try {
			// Execute the method.
			int statusCode = client.executeMethod(method);

			if (statusCode != HttpStatus.SC_OK) {
				// LOG.error("Method failed: " + method.getStatusLine());
			}

			// Read the response body.
			InputStream responseBody = method.getResponseBodyAsStream();
			// TODO Going to buffer response body of large or unknown size.
			// Using getResponseBodyAsStream instead is recommended.

			// Deal with the response.
			// Use caution: ensure correct character encoding and is not binary
			// data

			OutputStream writer = new ByteArrayOutputStream();

			MyIOUtils.copy(responseBody, writer);
			response = writer.toString();

			// response = new String(responseBody);

		} catch (HttpException e) {
			// LOG.error("Fatal protocol violation: " + e.getMessage());
			throw new MyAnnotationException("Protocol error executing HTTP request.", e);
		} catch (IOException e) {
			// LOG.error("Fatal transport error: " + e.getMessage());
			// LOG.error(method.getQueryString());
			System.err.println(e.getMessage());
			System.err.println(method.getQueryString());
			throw new MyAnnotationException("Transport error executing HTTP request.", e);
		} finally {
			// Release the connection.
			method.releaseConnection();
		}
		return response;

	}

	// protected static String readFileAsString(String filePath) throws
	// java.io.IOException {
	// return readFileAsString(new File(filePath));
	// }
	//
	// protected static String readFileAsString(File file) throws IOException {
	// byte[] buffer = new byte[(int) file.length()];
	// BufferedInputStream f = new BufferedInputStream(new
	// FileInputStream(file));
	// f.read(buffer);
	// f.close();
	// return new String(buffer);
	// }
	//
	// static abstract class LineParser {
	//
	// public abstract String parse(String s) throws ParseException;
	//
	// static class ManualDatasetLineParser extends LineParser {
	// public String parse(String s) throws ParseException {
	// return s.trim();
	// }
	// }
	//
	// static class OccTSVLineParser extends LineParser {
	// public String parse(String s) throws ParseException {
	// String result = s;
	// try {
	// result = s.trim().split("\t")[3];
	// } catch (ArrayIndexOutOfBoundsException e) {
	// throw new ParseException(e.getMessage(), 3);
	// }
	// return result;
	// }
	// }
	// }
	//
	// public void saveExtractedEntitiesSet(File inputFile, File outputFile,
	// LineParser parser, int restartFrom)
	// throws Exception {
	// PrintWriter out = new PrintWriter(outputFile);
	// // LOG.info("Opening input file " + inputFile.getAbsolutePath());
	// String text = readFileAsString(inputFile);
	// int i = 0;
	// int correct = 0;
	// int error = 0;
	// int sum = 0;
	// for (String snippet : text.split("\n")) {
	// String s = parser.parse(snippet);
	// if (s != null && !s.equals("")) {
	// i++;
	//
	// if (i < restartFrom)
	// continue;
	//
	// List<MyDBpediaResource> entities = new ArrayList<MyDBpediaResource>();
	// final long startTime = System.nanoTime();
	// //entities = extract(new Text(snippet.replaceAll("\\s+", " ")));
	// entities = null; // Manually changed
	// final long endTime = System.nanoTime();
	// sum += endTime - startTime;
	// // LOG.info(String.format("(%s) Extraction ran in %s ns.",
	// // i, endTime - startTime));
	// correct++;
	// for (MyDBpediaResource e : entities) {
	// out.println(e.uri());
	// }
	// out.println();
	// out.flush();
	// }
	// }
	// out.close();
	// // LOG.info(String.format("Extracted entities from %s text items, with %s
	// successes and %s errors.",
	// // i, correct, error));
	// // LOG.info("Results saved to: " + outputFile.getAbsolutePath());
	// double avg = (new Double(sum) / i);
	// // LOG.info(String.format("Average extraction time: %s ms", avg *
	// // 1000000));
	// }
	//
	// public void evaluate(File inputFile, File outputFile) throws Exception {
	// evaluateManual(inputFile, outputFile, 0);
	// }
	//
	// public void evaluateManual(File inputFile, File outputFile, int
	// restartFrom) throws Exception {
	// saveExtractedEntitiesSet(inputFile, outputFile, new
	// LineParser.ManualDatasetLineParser(), restartFrom);
	// }
	//
	// // public void evaluateCurcerzan(File inputFile, File outputFile) throws
	// // Exception {
	// // saveExtractedEntitiesSet(inputFile, outputFile, new
	// // LineParser.OccTSVLineParser());
	// // }
	//
	// /**
	// * Entity extraction code.
	// *
	// * @param text
	// * @return
	// */
	// //public abstract List<DBpediaResource> extract(Text text) throws
	// AnnotationException;
	//
	// public List<MyDBpediaResource> extract(String text) throws
	// MyAnnotationException {
	// // TODO Auto-generated method stub
	// return null;
	// }

}