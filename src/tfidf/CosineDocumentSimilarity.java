package tfidf;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.Version;

public class CosineDocumentSimilarity {

	public static final String CONTENT = "entities";

	private final Set<String> terms = new HashSet<String>();
	private final RealVector v1;
	private final RealVector v2;

	CosineDocumentSimilarity(String s1, String s2) throws IOException {
		Directory directory = createIndex(s1, s2);
		IndexReader reader = DirectoryReader.open(directory);
		Map<String, Integer> f1 = getTermFrequencies(reader, 0);
		Map<String, Integer> f2 = getTermFrequencies(reader, 1);
		reader.close();
		v1 = toRealVector(f1);
		v2 = toRealVector(f2);
	}

	Directory createIndex(String s1, String s2) throws IOException {
		Directory directory = new RAMDirectory();
		Analyzer analyzer = new SimpleAnalyzer(Version.LUCENE_48);
		IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_48, analyzer);
		IndexWriter writer = new IndexWriter(directory, iwc);
		addDocument(writer, s1);
		addDocument(writer, s2);
		writer.close();
		return directory;
	}

	/* Indexed, tokenized, stored. */
	public static final FieldType TYPE_STORED = new FieldType();

	static {
		TYPE_STORED.setIndexed(true);
		TYPE_STORED.setTokenized(true);
		TYPE_STORED.setStored(true);
		TYPE_STORED.setStoreTermVectors(true);
		TYPE_STORED.setStoreTermVectorPositions(true);
		TYPE_STORED.freeze();
	}

	void addDocument(IndexWriter writer, String content) throws IOException {
		Document doc = new Document();
		Field field = new Field(CONTENT, content, TYPE_STORED);
		doc.add(field);
		writer.addDocument(doc);
	}

	double getCosineSimilarity() {
		return (v1.dotProduct(v2)) / (v1.getNorm() * v2.getNorm());
	}

	public static double getCosineSimilarity(String s1, String s2) throws IOException {
		return new CosineDocumentSimilarity(s1, s2).getCosineSimilarity();
	}

	Map<String, Integer> getTermFrequencies(IndexReader reader, int docId) throws IOException {
		Terms vector = reader.getTermVector(docId, CONTENT);
		TermsEnum termsEnum = null;
		termsEnum = vector.iterator(termsEnum);
		Map<String, Integer> frequencies = new HashMap<String, Integer>();
		BytesRef text = null;
		while ((text = termsEnum.next()) != null) {
			String term = text.utf8ToString();
			int freq = (int) termsEnum.totalTermFreq();
			frequencies.put(term, freq);
			terms.add(term);
		}
		return frequencies;
	}

	RealVector toRealVector(Map<String, Integer> map) {
		RealVector vector = new ArrayRealVector(terms.size());
		int i = 0;
		for (String term : terms) {
			int value = map.containsKey(term) ? map.get(term) : 0;
			vector.setEntry(i++, value);
		}
		return (RealVector) vector.mapDivide(vector.getL1Norm());
	}
}