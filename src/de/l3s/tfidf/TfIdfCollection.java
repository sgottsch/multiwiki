package de.l3s.tfidf;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.DocsEnum;
import org.apache.lucene.index.Fields;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.search.DocIdSetIterator;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Bits;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.Version;

import de.l3s.model.BinaryComparison;
import de.l3s.model.Revision;
import de.l3s.model.Sentence;
import de.l3s.model.SentenceType;
import de.l3s.translate.Language;
import de.l3s.util.MapSorter;

public abstract class TfIdfCollection implements Collection {

	protected Revision revision1;
	protected Revision revision2;

	private PerFieldAnalyzerWrapper analyzer;

	private int numberOfDocs;

	private IndexReader ir;

	private Map<String, Integer> docTermFrequencies;

	private RealVector idfVector;

	private RealVector tfVector;
	private int numberOfTerms;

	private RealVector dfNormalizer;
	protected String titleName;

	protected FieldType TYPE_STORED = new FieldType();
	private BinaryComparison comparison;

	private Double alpha;
	private Integer k;

	private HashMap<String, Integer> termIndexes;
	private RealVector zeroVector;

	private Map<Integer, String> termNames;

	protected static final boolean PRINT = false;

	public TfIdfCollection(BinaryComparison comparison, Revision revision1, Revision revision2) {
		this(comparison, revision1, revision2, null, null);
	}

	public TfIdfCollection(BinaryComparison comparison, Revision revision1, Revision revision2, Double alpha,
			Integer k) {
		this.comparison = comparison;
		this.revision1 = revision1;
		this.revision2 = revision2;
		this.alpha = alpha;
		this.k = k;

		this.termNames = new HashMap<Integer, String>();

		this.docTermFrequencies = new HashMap<String, Integer>();

		if (revision1 != null && revision1.getTitle() != null) {
			titleName = revision1.getTitle().toLowerCase().replace(" ", "_");

			if (revision2.getLanguage() == Language.EN)
				titleName = revision2.getTitle().toLowerCase().replace(" ", "_");

			try {
				titleName = URLDecoder.decode(titleName, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				System.err.println("Unsupported encoding.");
			}
		}

		// titleName = titleName.toLowerCase();
		// this.titleNames = new ArrayList<String>();
		// this.titleNames.add(titleName.replace("_", " "));
		// this.titleNames.add(titleName.replace(" ", "_"));
		// try {
		// this.titleNames.add(java.net.URLDecoder.decode(titleName, "UTF-8"));
		// this.titleNames.add(java.net.URLEncoder.encode(titleName, "UTF-8"));
		// } catch (UnsupportedEncodingException e) {
		// e.printStackTrace();
		// }

		TYPE_STORED.setIndexed(true);
		TYPE_STORED.setTokenized(true);
		TYPE_STORED.setStored(true);
		TYPE_STORED.setStoreTermVectors(true);
		TYPE_STORED.setStoreTermVectorPositions(true);
		TYPE_STORED.freeze();
	}

	public double getAlpha() {
		return alpha;
	}

	public double getK() {
		return k;
	}

	public void createLuceneIndex() {

		numberOfDocs = 0;

		Map<String, Analyzer> analyzerPerField = new HashMap<String, Analyzer>();
		// special analyzer for field "abstract"
		analyzerPerField.put("abstract", new StandardAnalyzer(Version.LUCENE_48));

		// WhitespaceAnalyzer for all other fields
		analyzer = new PerFieldAnalyzerWrapper(new WhitespaceAnalyzer(Version.LUCENE_48), analyzerPerField);

		comparison.addTfIdfCollection(getCollectionType(), this);

		try {

			int sentenceCounter = 0;
			for (Sentence sentence : revision1.getSentences()) {
				if (!sentence.isImportant() || sentence.isInInfobox()
						|| (sentence.getType() != SentenceType.SENTENCE)) {
					continue;
				}
				sentenceCounter += 1;
			}
			for (Sentence sentence : revision2.getSentences()) {
				if (!sentence.isImportant() || sentence.isInInfobox()
						|| (sentence.getType() != SentenceType.SENTENCE)) {
					continue;
				}
				sentenceCounter += 1;
			}
			sentenceCounter = 1;
			// System.out.println("sentenceCounter: " + sentenceCounter);

			Directory directory = new RAMDirectory();

			IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_48, analyzer);
			IndexWriter iw = new IndexWriter(directory, config);

			iw.deleteAll();

			for (Sentence sentence : revision1.getSentences()) {
				if (!sentence.isImportant() || sentence.isInInfobox()
						|| (sentence.getType() != SentenceType.SENTENCE)) {
					// (conditions should be synchronized with the similar
					// sentence finder)
					continue;
				}

				Document doc = buildDocument(sentence, 1, revision1);
				iw.addDocument(doc);
			}

			for (Sentence sentence : revision2.getSentences()) {
				if (!sentence.isImportant() || sentence.isInInfobox()
						|| (sentence.getType() != SentenceType.SENTENCE)) {
					// (conditions should be synchronized with the similar
					// sentence finder)
					continue;
				}

				Document doc = buildDocument(sentence, 2, revision2);
				iw.addDocument(doc);
			}

			iw.close();

			ir = DirectoryReader.open(directory);
			numberOfDocs = ir.numDocs();
			Bits liveDocs = MultiFields.getLiveDocs(ir);
			final int maxDoc = ir.maxDoc();

			// Collect all terms
			Fields fields = MultiFields.getFields(ir);
			Terms allTermsTmp = fields.terms(getTermName());

			// no terms -> don't proceed (seems to work!?)
			if (allTermsTmp == null)
				return;

			List<String> allTerms = new ArrayList<String>();

			TermsEnum termsEnumTmp = allTermsTmp.iterator(null);
			// this field
			BytesRef termTmp = null;

			numberOfTerms = (int) allTermsTmp.size();

			idfVector = new ArrayRealVector(numberOfTerms);
			zeroVector = new ArrayRealVector(numberOfTerms);
			tfVector = zeroVector.copy();
			dfNormalizer = zeroVector.copy();

			termIndexes = new HashMap<String, Integer>();

			int i = 0;
			// explore the terms for this field
			while ((termTmp = termsEnumTmp.next()) != null) {

				String termName = termTmp.utf8ToString();

				Term term2 = new Term(getTermName(), termTmp);

				docTermFrequencies.put(termName, (int) ir.totalTermFreq(term2));
				idfVector.setEntry(i, Math.log(numberOfDocs / ir.docFreq(term2)));

				// double normalizerValue = Math.pow(Math.max(0.1, 1d -
				// ((((double) ir.docFreq(term2) - 2)) / alpha)),
				// beta);

				double normalizerValue;

				if (alpha == null)
					normalizerValue = 1;
				else
					normalizerValue = Math.exp(-((double) (ir.docFreq(term2) - 2)) / (sentenceCounter * alpha));

				dfNormalizer.setEntry(i, normalizerValue);
				// tfVector.setEntry(i, 1d / ir.totalTermFreq(term2));
				zeroVector.setEntry(i, 0);

				if (PRINT)
					System.out.println("term: " + termName + "(" + ir.docFreq(term2) + " -> " + normalizerValue + ")");

				termIndexes.put(termName, i);
				allTerms.add(termName);

				termNames.put(i, termName);

				i += 1;
			}

			// make sure that each sentence has at least a vector full of zeroes
			for (Sentence annotation : revision1.getAnnotations()) {
				annotation.setTermVector(getCollectionType(), zeroVector);
			}

			for (Sentence annotation : revision2.getAnnotations()) {
				annotation.setTermVector(getCollectionType(), zeroVector);
			}

			if (PRINT)
				System.out.println("idf vector: " + idfVector);

			for (int docId = 0; docId < maxDoc; docId++) {

				// skip deleted docs
				if (liveDocs != null && !liveDocs.get(docId)) {
					continue;
				}

				Document d = ir.document(docId);

				if (PRINT)
					System.out.println(d);

				Terms terms = ir.getTermVector(docId, getTermName());
				if (terms != null && terms.size() > 0) {

					Revision revision = revision1;
					if (d.getField("revision").stringValue().equals("2")) {
						revision = revision2;
					}

					Sentence annotation = revision
							.getSentenceById(Integer.parseInt(d.getField("annotation_id").stringValue()));

					if (annotation == null)
						continue;

					if (PRINT) {
						System.out.println(
								annotation.getNumber() + ", " + "TermString: " + d.getField(getTermName()).stringValue()
										+ " - Text: " + annotation.getEnglishRawText());
						System.out.println(annotation.getHtmlText());
					}
					TermsEnum termsEnum = terms.iterator(null); // access the
																// terms for
																// this field
					BytesRef term = null;

					// explore the terms for this field
					RealVector docTfVector = zeroVector.copy();

					while ((term = termsEnum.next()) != null) {

						String termName = term.utf8ToString();

						// enumerate through documents, in this case only one
						DocsEnum docsEnum = termsEnum.docs(null, null);
						int docIdEnum;
						while ((docIdEnum = docsEnum.nextDoc()) != DocIdSetIterator.NO_MORE_DOCS) {
							// get the term frequency in the document
							if (PRINT)
								System.out.println(termName + " " + docIdEnum + " " + docsEnum.freq());
							// annotation.addTf(termName, docsEnum.freq());

							docTfVector.setEntry(termIndexes.get(termName), docsEnum.freq());

							tfVector.setEntry(termIndexes.get(termName),
									tfVector.getEntry(termIndexes.get(termName)) + docsEnum.freq());
						}

					}

					annotation.setTermVector(getCollectionType(), docTfVector);

					if (PRINT)
						System.out.println(docTfVector);
				}

			}

		} catch (IOException e) {
			e.printStackTrace();
		}

		if (this.k != null) {

			// System.out.println("\n\n-----\n");
			List<Integer> frequentEntries = new ArrayList<Integer>();
			for (int i = 0; i < numberOfTerms; i++) {
				// System.out.println(termNames.get(i) + " -> " +
				// tfVector.getEntry(i) + ", " + idfVector.getEntry(i));

				if (tfVector.getEntry(i) >= this.k) {
					// System.out.println(termNames.get(i) + " -> " +
					// tfVector.getEntry(i) + ", " + idfVector.getEntry(i));
					tfVector.setEntry(i, 0);
					frequentEntries.add(i);
				} else
					tfVector.setEntry(i, 1d / tfVector.getEntry(i));
			}

			for (Sentence s : revision1.getSentences()) {
				s.setTermVector(getCollectionType(),
						removeEntries(s.getTermVector(getCollectionType()), frequentEntries, s));
			}
			for (Sentence s : revision2.getSentences()) {
				s.setTermVector(getCollectionType(),
						removeEntries(s.getTermVector(getCollectionType()), frequentEntries, s));
			}

			tfVector = removeEntries(tfVector, frequentEntries);
			idfVector = removeEntries(idfVector, frequentEntries);
			dfNormalizer = removeEntries(dfNormalizer, frequentEntries);
			numberOfTerms = numberOfTerms - frequentEntries.size();
		}
	}

	private RealVector removeEntries(RealVector vector, List<Integer> entries) {
		return removeEntries(vector, entries, null);
	}

	private RealVector removeEntries(RealVector vector, List<Integer> entries, Sentence sentence) {

		RealVector newVector = new ArrayRealVector(numberOfTerms - entries.size());

		int j = 0;
		boolean hasEntry = false;
		for (int i = 0; i < numberOfTerms; i++) {
			double value = vector.getEntry(i);
			if (!entries.contains(i)) {
				newVector.setEntry(j, value);
				if (!hasEntry && value > 0)
					hasEntry = true;
				j += 1;
			}
		}

		if (!hasEntry) {
			newVector = null;
			if (sentence != null) {
				sentence.setEntityAllString("");
			}
		}

		return newVector;
	}

	public void addTermVectorForPartSentence(Sentence sentence) {

		Document doc = buildDocument(sentence, 1, sentence.getRevision());

		RealVector tfVector = zeroVector.copy();

		for (String termName : doc.getField(getTermName()).stringValue().split(" ")) {
			if (termName.trim().isEmpty())
				continue;
			// System.out.println(termName);
			tfVector.setEntry(termIndexes.get(termName), tfVector.getEntry(termIndexes.get(termName)) + 1);
		}

		sentence.setTermVector(getCollectionType(), tfVector);
	}

	private Document buildDocument(Sentence annotation, int revisionNumber, Revision revision) {
		Document doc = new Document();
		doc.add(new StoredField("annotation_id", annotation.getNumber()));
		doc.add(new StoredField("language", annotation.getLanguage().getLanguage()));
		doc.add(new StoredField("revision", revisionNumber));

		doc.add(new Field(getTermName(), createTerms(annotation, revision), TYPE_STORED));

		return doc;
	}

	abstract String createTerms(Sentence sentence, Revision revision);

	abstract String getTermName();

	abstract CollectionType getCollectionType();

	public RealVector getIdfVector() {
		return idfVector;
	}

	public RealVector getTfVector() {
		return tfVector;
	}

	public Map<String, Integer> getDocTermFrequencies() {
		return docTermFrequencies;
	}

	public void sortDocFreq() {
		docTermFrequencies = MapSorter.sortByValues(docTermFrequencies);
	}

	public RealVector getDfNormalizer() {
		return this.dfNormalizer;
	}

	public double getIdfOfTerm(String term) {
		int idx = this.termIndexes.get(term);
		return this.idfVector.getEntry(idx);
	}

}
