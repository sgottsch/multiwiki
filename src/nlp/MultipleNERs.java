package nlp;

import java.io.IOException;
import java.util.List;

import edu.stanford.nlp.ie.NERClassifierCombiner;
import edu.stanford.nlp.io.IOUtils;
import edu.stanford.nlp.ling.CoreLabel;

public class MultipleNERs {

	public static void main(String[] args) throws IOException {
		String serializedClassifier = "english.all.3class.distsim.crf.ser.gz";
		String serializedClassifier2 = "english.all.3class.distsim.crf.ser.gz";

		if (args.length > 0) {
			serializedClassifier = args[0];
		}

		NERClassifierCombiner classifier = new NERClassifierCombiner(false, false, serializedClassifier,
				serializedClassifier2);

		String fileContents = IOUtils.slurpFile("input.txt");
		List<List<CoreLabel>> out = classifier.classify(fileContents);

		int i = 0;
		for (List<CoreLabel> lcl : out) {
			i++;
			int j = 0;
			for (CoreLabel cl : lcl) {
				j++;
				System.out.printf("%d:%d: %s%n", i, j,
						cl.toShorterString("Text", "CharacterOffsetBegin", "CharacterOffsetEnd", "NamedEntityTag"));
			}
		}
	}

}